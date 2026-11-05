package co.viajesplus.api;

import co.viajesplus.infrastructure.persistence.AppUser;
import co.viajesplus.infrastructure.persistence.AppUserRepository;
import co.viajesplus.infrastructure.security.SimulatedOidcProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Token del proveedor OIDC simulado (IMP-01). HU-06: credenciales válidas entran; inválidas reciben 401. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación (OIDC simulado)")
public class AuthController {

    public record TokenRequest(@NotBlank(message = "Es obligatorio") String username,
                               @NotBlank(message = "Es obligatorio") String password) {
    }

    public record TokenResponse(String access_token, String token_type, long expires_in, String role, String name) {
    }

    private final AuthenticationManager authentication;
    private final AppUserRepository users;
    private final SimulatedOidcProvider oidc;

    public AuthController(UserDetailsService userDetails, PasswordEncoder encoder, AppUserRepository users,
                          SimulatedOidcProvider oidc) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetails);
        provider.setPasswordEncoder(encoder);
        this.authentication = new ProviderManager(provider);
        this.users = users;
        this.oidc = oidc;
    }

    @PostMapping("/token")
    @SecurityRequirements
    @Operation(summary = "Obtener un token", description = "Usuarios de prueba: cliente@viajesplus.co, asesor@viajesplus.co, ...")
    public ResponseEntity<?> token(@Valid @RequestBody TokenRequest request) {
        try {
            authentication.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiError("CREDENCIALES_INVALIDAS", "Usuario o contraseña incorrectos", Map.of()));
        }
        AppUser user = users.findByUsernameIgnoreCase(request.username()).orElseThrow();
        SimulatedOidcProvider.IssuedToken token = oidc.issue(user.getUsername(), user.getFullName(), user.getRole());
        return ResponseEntity.ok(new TokenResponse(token.value(), "Bearer", token.expiresInSeconds(),
                user.getRole().name(), user.getFullName()));
    }

    @GetMapping("/jwks")
    @SecurityRequirements
    @Operation(summary = "Llaves públicas del proveedor simulado (JWKS)")
    public Map<String, Object> jwks() {
        return oidc.jwks();
    }
}
