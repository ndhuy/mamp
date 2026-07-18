-- ===========================================================================
-- Microstock Asset Management Platform — initial schema (PRD 1.1)
-- Public identifiers are UUIDs. Enums are modelled as VARCHAR + CHECK so JPA
-- mapping and future value additions stay simple. Ownership isolation is
-- anchored on owner_id columns for all private data.
-- ===========================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pg_trgm;    -- trigram search for title/description

-- ---------------------------------------------------------------------------
-- Accounts & security
-- ---------------------------------------------------------------------------
CREATE TABLE app_user (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER'   CHECK (role IN ('USER','ADMIN')),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','DISABLED')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_email    UNIQUE (email),
    CONSTRAINT uq_user_username UNIQUE (username)
);
CREATE INDEX idx_user_status ON app_user (status);

-- Refresh tokens (rotation + revocation). Access tokens are stateless JWTs.
CREATE TABLE refresh_token (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash)
);
CREATE INDEX idx_refresh_token_user ON refresh_token (user_id);

-- ---------------------------------------------------------------------------
-- Global master data (managed by Administrators)
-- ---------------------------------------------------------------------------
CREATE TABLE capture_device (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    brand          VARCHAR(120) NOT NULL,
    model          VARCHAR(120) NOT NULL,
    normalized_key VARCHAR(255) NOT NULL,          -- normalized "brand|model"
    device_type    VARCHAR(30)  NOT NULL CHECK (device_type IN
                     ('INTERCHANGEABLE_LENS','FIXED_LENS','SMARTPHONE','DRONE',
                      'ACTION_CAMERA','CAMERA_360','OTHER')),
    mount          VARCHAR(80),
    serial_number  VARCHAR(120),
    notes          TEXT,
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_capture_device_key UNIQUE (normalized_key)
);

CREATE TABLE lens (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    brand            VARCHAR(120) NOT NULL,
    model            VARCHAR(120) NOT NULL,
    normalized_key   VARCHAR(255) NOT NULL,
    mount            VARCHAR(80),
    lens_type        VARCHAR(20) CHECK (lens_type IN ('PRIME','ZOOM','MACRO','TELEPHOTO','OTHER')),
    min_focal_length INTEGER,
    max_focal_length INTEGER,
    max_aperture     VARCHAR(40),
    notes            TEXT,
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_lens_key UNIQUE (normalized_key)
);

CREATE TABLE stock_site (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(120) NOT NULL,
    normalized_name     VARCHAR(120) NOT NULL,
    website             VARCHAR(255),
    dashboard_url       VARCHAR(255),
    notes               TEXT,
    display_order       INTEGER NOT NULL DEFAULT 0,
    categories_required INTEGER NOT NULL DEFAULT 0 CHECK (categories_required BETWEEN 0 AND 2),
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_stock_site_name UNIQUE (normalized_name)
);

CREATE TABLE site_category (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stock_site_id UUID NOT NULL REFERENCES stock_site (id) ON DELETE CASCADE,
    name          VARCHAR(120) NOT NULL,
    parent_id     UUID REFERENCES site_category (id) ON DELETE SET NULL,
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_site_category UNIQUE (stock_site_id, name)
);
CREATE INDEX idx_site_category_site ON site_category (stock_site_id);

CREATE TABLE rejection_category (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(120) NOT NULL,
    active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_rejection_category UNIQUE (name)
);

-- ---------------------------------------------------------------------------
-- Private data (owned by a User)
-- ---------------------------------------------------------------------------
CREATE TABLE concept (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id    UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    name        VARCHAR(120) NOT NULL,
    description TEXT,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_concept_owner_name UNIQUE (owner_id, name)
);
CREATE INDEX idx_concept_owner ON concept (owner_id);

CREATE TABLE keyword (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id         UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    value            VARCHAR(120) NOT NULL,
    normalized_value VARCHAR(120) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_keyword_owner_norm UNIQUE (owner_id, normalized_value)
);
CREATE INDEX idx_keyword_owner ON keyword (owner_id);
CREATE INDEX idx_keyword_norm_trgm ON keyword USING gin (normalized_value gin_trgm_ops);

-- Immutable business code MED-000001, minted by a sequence (race-safe).
CREATE SEQUENCE media_code_seq START 1;

CREATE TABLE media_asset (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                VARCHAR(20) NOT NULL DEFAULT ('MED-' || lpad(nextval('media_code_seq')::text, 6, '0')),
    owner_id            UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    title               VARCHAR(120) NOT NULL,
    media_type          VARCHAR(10)  NOT NULL CHECK (media_type IN ('PHOTO','FOOTAGE')),
    thumbnail_key       VARCHAR(512),                       -- object-storage key
    description         TEXT,
    notes               TEXT,
    capture_date        DATE,
    location            VARCHAR(255),
    capture_device_id   UUID REFERENCES capture_device (id),
    lens_id             UUID REFERENCES lens (id),
    content_usage_type  VARCHAR(12) CHECK (content_usage_type IN ('COMMERCIAL','EDITORIAL')),
    workflow_status     VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (workflow_status IN
                          ('DRAFT','EDITING','METADATA_PENDING','READY','UPLOADING','COMPLETED','ARCHIVED')),
    original_file_path  TEXT,
    export_file_path    TEXT,
    storage_type        VARCHAR(20) CHECK (storage_type IN
                          ('LOCAL_DISK','EXTERNAL_DRIVE','NAS','GOOGLE_DRIVE','DROPBOX','ONEDRIVE','OTHER')),
    is_ai_generated     BOOLEAN NOT NULL DEFAULT FALSE,
    -- Editorial-only fields
    editorial_caption      TEXT,
    event_date             DATE,
    editorial_location     VARCHAR(255),
    event_subject_name     VARCHAR(255),
    editorial_notes        TEXT,
    deleted_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_media_code UNIQUE (code)
);
CREATE INDEX idx_media_owner            ON media_asset (owner_id);
CREATE INDEX idx_media_owner_deleted    ON media_asset (owner_id, deleted_at);
CREATE INDEX idx_media_type             ON media_asset (media_type);
CREATE INDEX idx_media_workflow_status  ON media_asset (workflow_status);
CREATE INDEX idx_media_device           ON media_asset (capture_device_id);
CREATE INDEX idx_media_lens             ON media_asset (lens_id);
CREATE INDEX idx_media_title_trgm       ON media_asset USING gin (title gin_trgm_ops);
CREATE INDEX idx_media_desc_trgm        ON media_asset USING gin (description gin_trgm_ops);

CREATE TABLE media_concept (
    media_id   UUID NOT NULL REFERENCES media_asset (id) ON DELETE CASCADE,
    concept_id UUID NOT NULL REFERENCES concept (id)     ON DELETE CASCADE,
    PRIMARY KEY (media_id, concept_id)
);
CREATE INDEX idx_media_concept_concept ON media_concept (concept_id);

CREATE TABLE media_keyword (
    media_id   UUID NOT NULL REFERENCES media_asset (id) ON DELETE CASCADE,
    keyword_id UUID NOT NULL REFERENCES keyword (id)     ON DELETE CASCADE,
    PRIMARY KEY (media_id, keyword_id)
);
CREATE INDEX idx_media_keyword_keyword ON media_keyword (keyword_id);

-- ---------------------------------------------------------------------------
-- Submission tracking (private through parent media)
-- ---------------------------------------------------------------------------
CREATE TABLE submission_record (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    media_id              UUID NOT NULL REFERENCES media_asset (id) ON DELETE CASCADE,
    stock_site_id         UUID NOT NULL REFERENCES stock_site (id),
    status                VARCHAR(20) NOT NULL DEFAULT 'NOT_SUBMITTED' CHECK (status IN
                            ('NOT_SUBMITTED','SUBMITTED','IN_REVIEW','ACCEPTED','REJECTED',
                             'RESUBMIT_REQUIRED','RESUBMITTED','REMOVED')),
    primary_category_id   UUID REFERENCES site_category (id),
    secondary_category_id UUID REFERENCES site_category (id),
    contributor_asset_id  VARCHAR(120),
    asset_url             TEXT,
    submitted_date        DATE,
    reviewed_date         DATE,
    rejection_category_id UUID REFERENCES rejection_category (id),
    rejection_detail      TEXT,
    notes                 TEXT,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- BR-009: at most one record per media/site pair
    CONSTRAINT uq_submission_media_site UNIQUE (media_id, stock_site_id),
    -- BR-011 / VAL-014: reviewed date cannot precede submitted date
    CONSTRAINT ck_submission_dates CHECK (
        reviewed_date IS NULL OR submitted_date IS NULL OR reviewed_date >= submitted_date
    )
);
CREATE INDEX idx_submission_media  ON submission_record (media_id);
CREATE INDEX idx_submission_site   ON submission_record (stock_site_id);
CREATE INDEX idx_submission_status ON submission_record (status);
