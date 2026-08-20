UPDATE campus_boundaries
SET boundary = ST_GeomFromText(
    'POLYGON((72.8500 19.1900, 72.8950 19.1900, 72.8950 19.2250, 72.8500 19.2250, 72.8500 19.1900))',
    4326
),
    updated_at = now()
WHERE id = 'c0000000-0000-0000-0000-000000000001';