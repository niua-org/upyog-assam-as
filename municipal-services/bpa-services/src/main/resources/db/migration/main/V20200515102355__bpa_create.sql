-- ========================================================
-- Building Plan Table
-- ========================================================
CREATE TABLE IF NOT EXISTS ug_bpa_buildingplans (
    /** Unique Identifier(UUID) of the BPA application for internal reference. */
    id VARCHAR(64) PRIMARY KEY,

    /** Formatted unique identifier of the building permit application. */
    application_no VARCHAR(64),

    /** Unique ULB identifier. */
    tenant_id VARCHAR(256) NOT NULL,

    /** Unique identifier of the scrutinized EDCR number. */
    edcr_number VARCHAR(64),

    /** Status of the application. */
    status VARCHAR(64),

    /** Application submission date. */
    application_date BIGINT,

    /** Approval number based on workflow status. */
    approval_no VARCHAR(64),

    /** Approval date based on workflow status. */
    approval_date BIGINT,

    /** Business service associated with the application. */
    business_service VARCHAR(64),

    /** Initiator user UUID. */
    account_id VARCHAR(64),

    /** Type of application. */
    application_type VARCHAR(64),

    /** Risk type derived from MDMS configuration. */
    risk_type VARCHAR(64),

    /** Unique Identifier(UUID) of the land for internal reference. */
    land_id VARCHAR(64),

    /** Audit Fields */
    created_by VARCHAR(64),
    last_modified_by VARCHAR(64),
    created_time BIGINT,
    last_modified_time BIGINT,

    /** Additional details JSON for extensibility */
    additional_details JSONB
);

-- ========================================================
-- Building Plan Audit Table
-- ========================================================
CREATE TABLE IF NOT EXISTS ug_bpa_buildingplans_audit (
    /** Unique Identifier(UUID) of the BPA application for internal reference. */
    id VARCHAR(64) NOT NULL,

    /** Formatted unique identifier of the building permit application. */
    application_no VARCHAR(64),

    /** Unique ULB identifier. */
    tenant_id VARCHAR(256) NOT NULL,

    /** Unique identifier of the scrutinized EDCR number. */
    edcr_number VARCHAR(64),

    /** Status of the application. */
    status VARCHAR(64),

    /** Application submission date. */
    application_date BIGINT,

    /** Approval number based on workflow status. */
    approval_no VARCHAR(64),

    /** Approval date based on workflow status. */
    approval_date BIGINT,

    /** Business service associated with the application. */
    business_service VARCHAR(64),

    /** Initiator user UUID. */
    account_id VARCHAR(64),

    /** Type of application. */
    application_type VARCHAR(64),

    /** Risk type derived from MDMS configuration. */
    risk_type VARCHAR(64),

    /** Unique Identifier(UUID) of the land for internal reference. */
    land_id VARCHAR(64),

    /** Audit Fields */
    created_by VARCHAR(64),
    last_modified_by VARCHAR(64),
    created_time BIGINT,
    last_modified_time BIGINT,

    /** Additional details JSON for extensibility */
    additional_details JSONB
);

-- ========================================================
-- BPA Document Table
-- ========================================================
CREATE TABLE IF NOT EXISTS ug_bpa_documents (
    /** Unique Identifier(UUID) for the document. */
    id VARCHAR(64) PRIMARY KEY,

    /** Type of the document (ownership proof, NOC, etc.). */
    document_type VARCHAR(64),

    /** Filestore reference ID. */
    filestore_id VARCHAR(64),

    /** Unique document identifier. */
    document_uid VARCHAR(64),

    /** Foreign key reference to the building plan. */
    buildingplan_id VARCHAR(64),

    /** Audit Fields */
    created_by VARCHAR(64),
    last_modified_by VARCHAR(64),
    created_time BIGINT,
    last_modified_time BIGINT,

    /** Additional details JSON for extensibility */
    additional_details JSONB,

    CONSTRAINT fk_ug_bpa_documents_buildingplans FOREIGN KEY (buildingplan_id)
        REFERENCES ug_bpa_buildingplans (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

-- ========================================================
-- BPA RTP details
-- ========================================================

create table IF NOT EXISTS ug_bpa_rtp_detail (
    /** Unique Identifier(UUID) for the RTP detail. */
    id VARCHAR(64) PRIMARY KEY,

    /** Foreign key reference to the building plan. */
    buildingplan_id VARCHAR(64),

    /** RTP category */
    rtp_category VARCHAR(100),

    /** RTP id */
    rtp_id varchar(64),

    /** RTP expiry date */
    rtp_name varchar(200),

    assignment_status VARCHAR(64),

    assignment_date BIGINT,
    changed_date BIGINT,

    remarks VARCHAR(1000),

    /** Audit Fields */
    created_by VARCHAR(64),
    last_modified_by VARCHAR(64),
    created_time BIGINT, --assignment_date
    last_modified_time BIGINT,

    /** Additional details JSON for extensibility */
    additional_details JSONB,

    CONSTRAINT fk_ug_bpa_rtp_detail_buildingplans FOREIGN KEY (buildingplan_id)
        REFERENCES ug_bpa_buildingplans (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);


CREATE TABLE IF NOT EXISTS ug_bpa_rtp_detail_audit (
    /** Unique Identifier(UUID) for the RTP detail. */
    id VARCHAR(64) NOT NULL,

    /** Foreign key reference to the building plan. */
    buildingplan_id VARCHAR(64),

    /** RTP category */
    rtp_category VARCHAR(100),

    /** RTP id */
    rtp_id VARCHAR(64),

    /** RTP name */
    rtp_name VARCHAR(200),

    assignment_status VARCHAR(64),

    assignment_date BIGINT,
    changed_date BIGINT,

    remarks VARCHAR(1000),

    /** Audit Fields */
    created_by VARCHAR(64),
    last_modified_by VARCHAR(64),
    created_time BIGINT,
    last_modified_time BIGINT,

    /** Additional details JSON for extensibility */
    additional_details JSONB
);
-- ========================================================


-- First create ENUM types
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'planning_permit_authority_enum') THEN
        CREATE TYPE planning_permit_authority_enum AS ENUM ('DEVELOPMENT_AUTHORITY', 'TACP', 'GMDA', 'CMA');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'building_permit_authority_enum') THEN
        CREATE TYPE building_permit_authority_enum AS ENUM ('MUNICIPAL_BOARD', 'GRAM_PANCHAYAT', 'GMC', 'NGMB');
    END IF;
END$$;

-- Now create the table
CREATE TABLE IF NOT EXISTS ug_bpa_area_mapping_detail (
    id                        VARCHAR(64) PRIMARY KEY,
    buildingplan_id            VARCHAR(64) NOT NULL,
    district                  VARCHAR(128),
    planning_area             VARCHAR(128),
    concerned_authority       VARCHAR(128),
    village_name              VARCHAR(128),
    planning_permit_authority planning_permit_authority_enum NOT NULL,
    building_permit_authority building_permit_authority_enum NOT NULL,
    revenue_village           VARCHAR(128),
    mouza                     VARCHAR(128),
    ward                      VARCHAR(128),

    /** Audit Fields */
    created_by          VARCHAR(64),
    last_modified_by    VARCHAR(64),
    created_time        BIGINT, -- assignment_date
    last_modified_time  BIGINT,
    CONSTRAINT fk_ug_bpa_area_mapping_detail_buildingplans FOREIGN KEY (buildingplan_id)
            REFERENCES ug_bpa_buildingplans (id)
            ON UPDATE NO ACTION
            ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS ug_bpa_area_mapping_detail_audit (
    id                        VARCHAR(64) ,
    buildingplan_id           VARCHAR(64) NOT NULL,
    district                  VARCHAR(128),
    planning_area             VARCHAR(128),
    concerned_authority       VARCHAR(128),
    village_name              VARCHAR(128),
    planning_permit_authority planning_permit_authority_enum NOT NULL,
    building_permit_authority building_permit_authority_enum NOT NULL,
    revenue_village           VARCHAR(128),
    mouza                     VARCHAR(128),
    ward                      VARCHAR(128),

    /** Audit Fields */
    created_by          VARCHAR(64),
    last_modified_by    VARCHAR(64),
    created_time        BIGINT, -- assignment_date
    last_modified_time  BIGINT
);

CREATE TABLE IF NOT EXISTS ug_bpa_documents_audit (
    /** Unique Identifier(UUID) for the document. */
    id VARCHAR(64),

    /** Type of the document (ownership proof, NOC, etc.). */
    document_type VARCHAR(64),

    /** Filestore reference ID. */
    filestore_id VARCHAR(64),

    /** Unique document identifier. */
    document_uid VARCHAR(64),

    /** Foreign key reference to the building plan. */
    buildingplan_id VARCHAR(64),

    /** Audit Fields */
    created_by VARCHAR(64),
    last_modified_by VARCHAR(64),
    created_time BIGINT,
    last_modified_time BIGINT,

    /** Additional details JSON for extensibility */
    additional_details JSONB
);
