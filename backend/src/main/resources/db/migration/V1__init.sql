CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE admin_users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE anonymous_users (
    id UUID PRIMARY KEY,
    token_version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    expires_at TIMESTAMP,
    last_verified_at TIMESTAMP
);

CREATE TABLE campus_boundaries (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    boundary GEOMETRY(POLYGON, 4326) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE campus_zones (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    boundary GEOMETRY(POLYGON, 4326) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE rooms (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE messages (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES rooms(id),
    anonymous_user_id UUID NOT NULL REFERENCES anonymous_users(id),
    content TEXT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE reports (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL REFERENCES messages(id),
    reported_by UUID NOT NULL REFERENCES anonymous_users(id),
    reason VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    resolved_at TIMESTAMP,
    resolved_by UUID REFERENCES admin_users(id)
);

CREATE INDEX idx_messages_room_created ON messages (room_id, created_at DESC);
CREATE INDEX idx_messages_user_created ON messages (anonymous_user_id, created_at DESC);
CREATE INDEX idx_reports_status ON reports (status, created_at DESC);
CREATE INDEX idx_campus_boundaries_geom ON campus_boundaries USING GIST (boundary);
CREATE INDEX idx_campus_zones_geom ON campus_zones USING GIST (boundary);