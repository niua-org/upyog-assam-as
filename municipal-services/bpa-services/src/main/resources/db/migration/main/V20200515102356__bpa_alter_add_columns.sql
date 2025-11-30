ALTER TABLE ug_bpa_buildingplans
    ADD COLUMN IF NOT EXISTS planning_permit_no VARCHAR(64),
    ADD COLUMN IF NOT EXISTS planning_permit_date BIGINT,
    ADD COLUMN IF NOT EXISTS pp_filestore_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS building_permit_no VARCHAR(64),
    ADD COLUMN IF NOT EXISTS building_permit_date BIGINT,
    ADD COLUMN IF NOT EXISTS bp_filestore_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS occupancy_certificate_no VARCHAR(64),
    ADD COLUMN IF NOT EXISTS occupancy_certificate_date BIGINT,
    ADD COLUMN IF NOT EXISTS oc_filestore_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS property_no VARCHAR(64),
    ADD COLUMN IF NOT EXISTS property_details JSONB,
    ADD COLUMN IF NOT EXISTS property_vendor VARCHAR(100);
	
ALTER TABLE ug_bpa_buildingplans_audit 
    ADD COLUMN IF NOT EXISTS planning_permit_no VARCHAR(64),
    ADD COLUMN IF NOT EXISTS planning_permit_date BIGINT,
    ADD COLUMN IF NOT EXISTS pp_filestore_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS building_permit_no VARCHAR(64),
    ADD COLUMN IF NOT EXISTS building_permit_date BIGINT,
    ADD COLUMN IF NOT EXISTS bp_filestore_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS occupancy_certificate_no VARCHAR(64),
    ADD COLUMN IF NOT EXISTS occupancy_certificate_date BIGINT,
    ADD COLUMN IF NOT EXISTS oc_filestore_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS property_no VARCHAR(64),
    ADD COLUMN IF NOT EXISTS property_details JSONB,
    ADD COLUMN IF NOT EXISTS property_vendor VARCHAR(100);