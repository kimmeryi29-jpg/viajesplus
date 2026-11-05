-- Catálogo de demostración para el entorno de pruebas (datos ficticios).
INSERT INTO travel_package (code, name, destination, description, days, price_per_person) VALUES
 ('CTG-01', 'Cartagena histórica', 'Cartagena', 'Ciudad amurallada, islas del Rosario y noche en Getsemaní. Incluye hotel 4 noches, desayunos y traslados.', 5, 1200000),
 ('ADZ-01', 'San Andrés todo incluido', 'San Andrés', 'Hotel frente al mar con alimentación completa, vuelta a la isla y visita a Johnny Cay.', 5, 1850000),
 ('EJC-01', 'Eje Cafetero', 'Salento y Filandia', 'Valle del Cocora, finca cafetera con cata y pueblos de colores. Incluye alojamiento y transporte terrestre.', 4, 980000),
 ('SMR-01', 'Santa Marta y Tayrona', 'Santa Marta', 'Parque Tayrona, Minca y centro histórico. Incluye guía, entradas y hotel 3 noches.', 4, 1350000),
 ('VDL-01', 'Villa de Leyva', 'Villa de Leyva', 'Plaza mayor, Pozos Azules y desierto de la Candelaria. Fin de semana con hotel boutique.', 3, 650000),
 ('AMZ-01', 'Amazonas', 'Leticia', 'Selva, río Amazonas, comunidades indígenas y avistamiento de delfines rosados. Incluye vuelos internos.', 6, 2400000);

INSERT INTO departure (package_id, departure_date, capacity)
SELECT p.id, d.fecha, d.cupo
FROM travel_package p
JOIN (VALUES
  ('CTG-01', DATE '2026-12-04', 20), ('CTG-01', DATE '2027-01-15', 20), ('CTG-01', DATE '2027-03-19', 20),
  ('ADZ-01', DATE '2026-12-11', 24), ('ADZ-01', DATE '2027-01-22', 24),
  ('EJC-01', DATE '2026-11-27', 16), ('EJC-01', DATE '2027-02-12', 16),
  ('SMR-01', DATE '2026-12-18', 18), ('SMR-01', DATE '2027-02-26', 18),
  ('VDL-01', DATE '2026-11-20', 12), ('VDL-01', DATE '2027-01-08', 12),
  ('AMZ-01', DATE '2027-01-29', 10), ('AMZ-01', DATE '2027-04-02', 10)
) AS d(codigo, fecha, cupo) ON d.codigo = p.code;
