INSERT INTO admin_users (id, username, password_hash)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'admin',
    '$2a$10$nt5f.n2Sd.hcKXa5lK587eZN6NLq2jr3VbzvnMXkEbSgEQNLHpMZu'
);

INSERT INTO campus_boundaries (id, name, boundary)
VALUES (
    'c0000000-0000-0000-0000-000000000001',
    'Matunga Campus Area',
    ST_GeomFromText(
        'POLYGON((72.8400 19.0100, 72.8900 19.0100, 72.8900 19.0450, 72.8400 19.0450, 72.8400 19.0100))',
        4326
    )
);

INSERT INTO rooms (id, name, slug, description) VALUES
    ('11111111-1111-1111-1111-111111111111', 'General', 'general', 'General campus discussions'),
    ('22222222-2222-2222-2222-222222222222', 'Placements', 'placements', 'Placement drives, offers, and interview experiences'),
    ('33333333-3333-3333-3333-333333333333', 'Academics', 'academics', 'Courses, exams, and study discussions'),
    ('44444444-4444-4444-4444-444444444444', 'Events', 'events', 'Campus events, fests, and meetups'),
    ('55555555-5555-5555-5555-555555555555', 'Random', 'random', 'Anything and everything'),
    ('66666666-6666-6666-6666-666666666666', 'Hostel', 'hostel', 'Hostel life, food, and roommates');