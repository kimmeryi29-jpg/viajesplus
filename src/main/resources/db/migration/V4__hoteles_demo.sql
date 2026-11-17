-- Hoteles ficticios para el entorno de pruebas. Proveedor de prueba: proveedor@viajesplus.co.
INSERT INTO hotel (code, name, destination, provider_username) VALUES
 ('HTL-CTG1', 'Hotel Mar Azul Cartagena', 'Cartagena', 'proveedor@viajesplus.co'),
 ('HTL-CTG2', 'Casa Colonial Getsemaní', 'Cartagena', 'proveedor@viajesplus.co'),
 ('HTL-ADZ1', 'Hotel Mar Azul San Andrés', 'San Andrés', 'proveedor@viajesplus.co'),
 ('HTL-EJC1', 'Finca Hotel El Cafetal', 'Salento y Filandia', 'proveedor@viajesplus.co'),
 ('HTL-SMR1', 'Hotel Bahía Santa Marta', 'Santa Marta', 'proveedor@viajesplus.co'),
 ('HTL-VDL1', 'Posada La Candelaria', 'Villa de Leyva', 'proveedor@viajesplus.co'),
 ('HTL-AMZ1', 'Ecohotel Selva Viva', 'Leticia', 'proveedor@viajesplus.co');

-- Disponibilidad confirmada para casi todas las salidas. Dos salidas quedan sin confirmar
-- (Santa Marta 26/02/2027 y Amazonas 02/04/2027) para mostrar SIN_DISPONIBILIDAD en la demo.
INSERT INTO hotel_availability (hotel_id, departure_id, rooms, rate_per_night, confirmed, updated_by)
SELECT h.id, d.id, 12, 280000, NOT (p.code = 'SMR-01' AND d.departure_date = DATE '2027-02-26')
                               AND NOT (p.code = 'AMZ-01' AND d.departure_date = DATE '2027-04-02'),
       'proveedor@viajesplus.co'
FROM hotel h
JOIN travel_package p ON p.destination = h.destination
JOIN departure d ON d.package_id = p.id;
