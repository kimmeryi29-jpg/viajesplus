-- T003 · Migración V1: paquetes, salidas, reservas, auditoría y usuarios del proveedor OIDC simulado.
-- Trazabilidad: REQ-VIA-005, REQ-VIA-011, RN-03, ADR-002.

CREATE TABLE travel_package (
    id               BIGSERIAL PRIMARY KEY,
    code             VARCHAR(20)    NOT NULL UNIQUE,
    name             VARCHAR(120)   NOT NULL,
    destination      VARCHAR(80)    NOT NULL,
    description      VARCHAR(600)   NOT NULL,
    days             SMALLINT       NOT NULL CHECK (days > 0),
    price_per_person NUMERIC(12, 2) NOT NULL CHECK (price_per_person > 0),
    active           BOOLEAN        NOT NULL DEFAULT TRUE
);

-- Cada salida tiene un cupo. RN-03 / ADR-002: las plazas ocupadas nunca superan el cupo,
-- ni siquiera con operaciones simultáneas; la base de datos es la última protección.
CREATE TABLE departure (
    id             BIGSERIAL PRIMARY KEY,
    package_id     BIGINT   NOT NULL REFERENCES travel_package (id),
    departure_date DATE     NOT NULL,
    capacity       SMALLINT NOT NULL CHECK (capacity > 0),
    occupied_seats SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_departure UNIQUE (package_id, departure_date),
    CONSTRAINT ck_departure_cupo CHECK (occupied_seats >= 0 AND occupied_seats <= capacity)
);

CREATE TABLE reservation (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(20)    NOT NULL UNIQUE,
    client_username VARCHAR(120)   NOT NULL,
    package_id      BIGINT         NOT NULL REFERENCES travel_package (id),
    departure_id    BIGINT         NOT NULL REFERENCES departure (id),
    travelers       SMALLINT       NOT NULL CHECK (travelers > 0),
    price_per_person NUMERIC(12, 2) NOT NULL,
    total           NUMERIC(12, 2) NOT NULL,
    status          VARCHAR(20)    NOT NULL CHECK (status IN ('PENDIENTE', 'CONFIRMADA', 'RECHAZADA', 'CANCELADA')),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now()
);
CREATE INDEX ix_reservation_client ON reservation (client_username);
CREATE SEQUENCE reservation_code_seq START 1;

-- REQ-VIA-011: cada cambio de estado queda auditado con usuario, fecha-hora UTC y motivo.
CREATE TABLE audit_log (
    id          BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(40)  NOT NULL,
    entity_id   VARCHAR(40)  NOT NULL,
    action      VARCHAR(40)  NOT NULL,
    from_status VARCHAR(20),
    to_status   VARCHAR(20),
    username    VARCHAR(120) NOT NULL,
    occurred_at TIMESTAMPTZ  NOT NULL,
    reason      VARCHAR(300)
);
CREATE INDEX ix_audit_entity ON audit_log (entity_type, entity_id);

-- Usuarios del proveedor OIDC simulado (IMP-01). Se reemplaza por el tenant real en el Sprint 3 (D21).
CREATE TABLE app_user (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(120) NOT NULL UNIQUE,
    full_name     VARCHAR(120) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE
);
