package co.viajesplus.infrastructure.security;

import co.viajesplus.domain.Role;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Proveedor OIDC simulado: mitigación de IMP-01 (no hay tenant OIDC de pruebas). Emite tokens JWT firmados con
 * RS256 y los mismos claims que emitirá el proveedor real (sub, name, roles). En el Sprint 3 (D21) basta con
 * configurar VIAJESPLUS_OIDC_ISSUER_URI para validar los tokens del tenant real; este componente deja de usarse.
 */
@Component
public class SimulatedOidcProvider {

    public static final String ISSUER = "https://viajesplus.local/oidc-simulado";

    private final RSAKey rsaKey;
    private final NimbusJwtEncoder encoder;
    private final Duration ttl;

    public SimulatedOidcProvider(@Value("${viajesplus.oidc.token-minutes:480}") long minutes) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            this.rsaKey = new RSAKey.Builder((RSAPublicKey) pair.getPublic())
                    .privateKey((RSAPrivateKey) pair.getPrivate())
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA no disponible", e);
        }
        this.encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
        this.ttl = Duration.ofMinutes(minutes);
    }

    public record IssuedToken(String value, long expiresInSeconds) {
    }

    public IssuedToken issue(String username, String fullName, Role role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .claim("name", fullName)
                .claim("roles", List.of(role.name()))
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(rsaKey.getKeyID()).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, ttl.toSeconds());
    }

    public RSAPublicKey publicKey() {
        try {
            return rsaKey.toRSAPublicKey();
        } catch (com.nimbusds.jose.JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    public Map<String, Object> jwks() {
        return new JWKSet(rsaKey.toPublicJWK()).toJSONObject();
    }
}
