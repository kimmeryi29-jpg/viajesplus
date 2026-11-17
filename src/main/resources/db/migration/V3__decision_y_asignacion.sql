-- Sprint 2 · T010 a T015. Decisión del agente, disponibilidad del proveedor, asignación y avisos.
-- Trazabilidad: REQ-VIA-004 a 009, RN-02 (spec v1.0.1, IMP-02), RN-05, RN-06, NFR-VIA-003, ADR-004.

-- Hoteles de cada destino y su proveedor (HU-20).
CREATE TABLE hotel (
    id                BIGSERIAL PRIMARY KEY,
    code              VARCHAR(20)  NOT NULL UNIQUE,
    name              VARCHAR(120) NOT NULL,
    destination       VARCHAR(80)  NOT NULL,
    provider_username VARCHAR(120) NOT NULL
);

-- RN-05: disponibilidad y tarifa que confirma el proveedor para cada salida.
CREATE TABLE hotel_availability (
    id             BIGSERIAL PRIMARY KEY,
    hotel_id       BIGINT         NOT NULL REFERENCES hotel (id),
    departure_id   BIGINT         NOT NULL REFERENCES departure (id),
    rooms          SMALLINT       NOT NULL CHECK (rooms >= 0),
    rate_per_night NUMERIC(12, 2) NOT NULL CHECK (rate_per_night >= 0),
    confirmed      BOOLEAN        NOT NULL DEFAULT FALSE,
    updated_by     VARCHAR(120)   NOT NULL,
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT uq_hotel_availability UNIQUE (hotel_id, departure_id)
);

-- Decisión del agente (REQ-VIA-007, 008) y asignación del coordinador (REQ-VIA-009).
ALTER TABLE reservation
    ADD COLUMN decided_by       VARCHAR(120),
    ADD COLUMN decided_at       TIMESTAMPTZ,
    ADD COLUMN rejection_reason VARCHAR(300),
    ADD COLUMN hotel_id         BIGINT REFERENCES hotel (id),
    ADD COLUMN guide_username   VARCHAR(120),
    ADD COLUMN driver_username  VARCHAR(120),
    ADD COLUMN assigned_at      TIMESTAMPTZ;

CREATE INDEX ix_reservation_guide ON reservation (guide_username);
CREATE INDEX ix_reservation_driver ON reservation (driver_username);

-- ADR-004: avisos por correo con reintento. El envío en el entorno de pruebas es simulado.
CREATE TABLE notification (
    id                 BIGSERIAL PRIMARY KEY,
    recipient_username VARCHAR(120) NOT NULL,
    channel            VARCHAR(20)  NOT NULL DEFAULT 'CORREO',
    subject            VARCHAR(200) NOT NULL,
    body               VARCHAR(1000) NOT NULL,
    related_code       VARCHAR(20),
    status             VARCHAR(20)  NOT NULL CHECK (status IN ('PENDIENTE', 'ENVIADO', 'FALLIDO')),
    attempts           SMALLINT     NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    sent_at            TIMESTAMPTZ
);
CREATE INDEX ix_notification_recipient ON notification (recipient_username, created_at DESC);

-- IMP-02 / spec v1.0.1: RN-02 manda. Las reservas PENDIENTE y CONFIRMADA ocupan cupo.
-- Se recalculan las plazas ocupadas con las reservas que ya existían del Sprint 1.
UPDATE departure d
SET occupied_seats = LEAST(d.capacity, COALESCE((
        SELECT SUM(r.travelers) FROM reservation r
        WHERE r.departure_id = d.id AND r.status IN ('PENDIENTE', 'CONFIRMADA')), 0));
