-- Salida de demostración para mostrar RN-06 (cruce de guía o conductor) en la Review del Sprint 2:
-- San Andrés sale el 4 de diciembre de 2026, el mismo día que Cartagena histórica.
INSERT INTO departure (package_id, departure_date, capacity)
SELECT id, DATE '2026-12-04', 24 FROM travel_package WHERE code = 'ADZ-01'
ON CONFLICT (package_id, departure_date) DO NOTHING;

INSERT INTO hotel_availability (hotel_id, departure_id, rooms, rate_per_night, confirmed, updated_by)
SELECT h.id, d.id, 12, 280000, TRUE, 'proveedor@viajesplus.co'
FROM hotel h
JOIN travel_package p ON p.destination = h.destination AND p.code = 'ADZ-01'
JOIN departure d ON d.package_id = p.id AND d.departure_date = DATE '2026-12-04'
ON CONFLICT (hotel_id, departure_id) DO NOTHING;
