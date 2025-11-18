-- Create table for GIS processing logs
CREATE TABLE ug_gis_log (
    id BIGSERIAL PRIMARY KEY,
    application_no VARCHAR(128),
    rtpi_id VARCHAR(128),
    file_store_id VARCHAR(512) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    tenant_id VARCHAR(128),
    status VARCHAR(50) NOT NULL,
    response_status VARCHAR(50),
    response_json TEXT,
    createdby VARCHAR(128),
    createdtime BIGINT NOT NULL,
    lastmodifiedby VARCHAR(128),
    lastmodifiedtime BIGINT,
    details JSONB
);

-- Create indexes for better query performance
CREATE INDEX idx_ug_gis_log_application_no ON ug_gis_log(application_no);
CREATE INDEX idx_ug_gis_log_rtpi_id ON ug_gis_log(rtpi_id);
CREATE INDEX idx_ug_gis_log_file_store_id ON ug_gis_log(file_store_id);
CREATE INDEX idx_ug_gis_log_tenant_id ON ug_gis_log(tenant_id);
CREATE INDEX idx_ug_gis_log_status ON ug_gis_log(status);
CREATE INDEX idx_ug_gis_log_createdtime ON ug_gis_log(createdtime);

-- Create GIN index for JSONB details column for efficient JSON queries
CREATE INDEX idx_ug_gis_log_details_gin ON ug_gis_log USING GIN (details);
