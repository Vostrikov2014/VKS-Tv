-- V1.0.0__Initial_Schema.sql
-- Initial database schema for Jitsi Management Platform
-- Complies with PostgreSQL 16+ standards

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set timezone to UTC
SET timezone = 'UTC';

-- ============================================================================
-- TENANTS TABLE
-- Multi-tenant isolation: each tenant represents an organization
-- ============================================================================
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    domain VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    max_participants INTEGER DEFAULT 100,
    max_duration_minutes INTEGER DEFAULT 480,
    max_recordings INTEGER DEFAULT 50,
    recording_retention_days INTEGER DEFAULT 90,
    jitsi_domain VARCHAR(255),
    jibri_enabled BOOLEAN DEFAULT FALSE,
    s3_bucket VARCHAR(255),
    s3_region VARCHAR(100),
    subscription_tier VARCHAR(50) DEFAULT 'FREE',
    subscription_expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT tenants_status_check CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING', 'DELETED'))
);

-- Indexes for tenants
CREATE INDEX idx_tenants_slug ON tenants(slug);
CREATE INDEX idx_tenants_status ON tenants(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_tenants_created_at ON tenants(created_at);

-- ============================================================================
-- USERS TABLE
-- Platform users with role-based access control
-- ============================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(50),
    avatar_url VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verified_at TIMESTAMP WITH TIME ZONE,
    two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    two_factor_secret VARCHAR(255),
    last_login_at TIMESTAMP WITH TIME ZONE,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT users_status_check CHECK (status IN ('ACTIVE', 'INACTIVE', 'PENDING', 'SUSPENDED', 'DELETED')),
    CONSTRAINT users_role_check CHECK (role IN ('SUPER_ADMIN', 'TENANT_ADMIN', 'MODERATOR', 'USER', 'AUDITOR'))
);

-- Indexes for users
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_tenant_id ON users(tenant_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_status ON users(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_created_at ON users(created_at);

-- ============================================================================
-- CONFERENCES TABLE
-- Video conference rooms managed by the platform
-- ============================================================================
CREATE TABLE conferences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    room_id VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    scheduled_start_at TIMESTAMP WITH TIME ZONE,
    scheduled_end_at TIMESTAMP WITH TIME ZONE,
    actual_start_at TIMESTAMP WITH TIME ZONE,
    actual_end_at TIMESTAMP WITH TIME ZONE,
    max_participants INTEGER,
    current_participants INTEGER NOT NULL DEFAULT 0,
    recording_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    auto_start_recording BOOLEAN NOT NULL DEFAULT FALSE,
    chat_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    screen_sharing_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    lobby_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    password_hash VARCHAR(255),
    join_url VARCHAR(500),
    moderator_join_url VARCHAR(500),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    created_by_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    moderator_id UUID REFERENCES users(id) ON DELETE SET NULL,
    jitsi_domain VARCHAR(255),
    external_id VARCHAR(255),
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT conferences_status_check CHECK (status IN ('SCHEDULED', 'ACTIVE', 'COMPLETED', 'CANCELLED', 'FAILED'))
);

-- Indexes for conferences
CREATE INDEX idx_conferences_room_id ON conferences(room_id);
CREATE INDEX idx_conferences_tenant_id ON conferences(tenant_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_conferences_status ON conferences(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_conferences_scheduled_start ON conferences(scheduled_start_at);
CREATE INDEX idx_conferences_created_at ON conferences(created_at);
CREATE INDEX idx_conferences_active ON conferences(status, tenant_id) WHERE status = 'ACTIVE' AND deleted_at IS NULL;

-- ============================================================================
-- RECORDINGS TABLE
-- Video conference recordings captured by Jibri
-- ============================================================================
CREATE TABLE recordings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    recording_type VARCHAR(50) NOT NULL DEFAULT 'FILE',
    s3_bucket VARCHAR(255),
    s3_key VARCHAR(500),
    s3_url VARCHAR(1000),
    download_url VARCHAR(1000),
    file_size_bytes BIGINT,
    duration_seconds BIGINT,
    mime_type VARCHAR(100),
    thumbnail_url VARCHAR(1000),
    hash_sha256 VARCHAR(64),
    encrypted BOOLEAN NOT NULL DEFAULT TRUE,
    encryption_key_id VARCHAR(255),
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    retention_until TIMESTAMP WITH TIME ZONE,
    legal_hold BOOLEAN NOT NULL DEFAULT FALSE,
    download_count INTEGER NOT NULL DEFAULT 0,
    metadata JSONB,
    conference_id UUID NOT NULL REFERENCES conferences(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT recordings_status_check CHECK (status IN ('PENDING', 'RECORDING', 'PROCESSING', 'READY', 'FAILED', 'DELETED', 'ARCHIVED')),
    CONSTRAINT recordings_type_check CHECK (recording_type IN ('FILE', 'STREAM'))
);

-- Indexes for recordings
CREATE INDEX idx_recordings_conference_id ON recordings(conference_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_recordings_status ON recordings(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_recordings_retention ON recordings(retention_until) WHERE legal_hold = FALSE AND status NOT IN ('DELETED', 'ARCHIVED');
CREATE INDEX idx_recordings_created_at ON recordings(created_at);
CREATE INDEX idx_recordings_title_search ON recordings USING gin(to_tsvector('english', title));

-- ============================================================================
-- AUDIT_LOGS TABLE
-- Immutable audit trail for compliance (GDPR/152-FZ)
-- Note: Consider partitioning this table by date in production
-- ============================================================================
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_type VARCHAR(100) NOT NULL,
    action VARCHAR(255) NOT NULL,
    resource_type VARCHAR(100),
    resource_id VARCHAR(255),
    description TEXT NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_method VARCHAR(20),
    request_uri VARCHAR(500),
    request_body TEXT,
    response_status INTEGER,
    response_body TEXT,
    duration_ms BIGINT,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message TEXT,
    correlation_id VARCHAR(100),
    trace_id VARCHAR(100),
    span_id VARCHAR(100),
    metadata JSONB,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0
);

-- Indexes for audit logs
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_audit_logs_tenant_id ON audit_logs(tenant_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_audit_logs_event_type ON audit_logs(event_type);
CREATE INDEX idx_audit_logs_occurred_at ON audit_logs(occurred_at);
CREATE INDEX idx_audit_logs_correlation_id ON audit_logs(correlation_id);
CREATE INDEX idx_audit_logs_trace_id ON audit_logs(trace_id);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_type, resource_id);
CREATE INDEX idx_audit_logs_composite_query ON audit_logs(tenant_id, occurred_at DESC, event_type) WHERE deleted_at IS NULL;

-- Composite index for common query patterns
CREATE INDEX idx_audit_logs_tenant_date ON audit_logs(tenant_id, occurred_at) WHERE deleted_at IS NULL;

-- ============================================================================
-- COMMENTS FOR DOCUMENTATION
-- ============================================================================
COMMENT ON TABLE tenants IS 'Multi-tenant organizations using the platform';
COMMENT ON TABLE users IS 'Platform users with RBAC roles';
COMMENT ON TABLE conferences IS 'Scheduled and active video conferences';
COMMENT ON TABLE recordings IS 'Video conference recordings with retention policies';
COMMENT ON TABLE audit_logs IS 'Immutable audit trail for compliance and forensics';

-- ============================================================================
-- INITIAL DATA
-- ============================================================================

-- Create default super admin tenant
INSERT INTO tenants (id, name, slug, status, max_participants, max_duration_minutes, max_recordings, recording_retention_days, subscription_tier)
VALUES 
    ('00000000-0000-0000-0000-000000000001', 'System', 'system', 'ACTIVE', 1000, 1440, 1000, 365, 'ENTERPRISE')
ON CONFLICT (slug) DO NOTHING;

-- Note: Default super admin user should be created via secure initialization script
-- Password hash should be generated using BCrypt with cost factor >= 10
