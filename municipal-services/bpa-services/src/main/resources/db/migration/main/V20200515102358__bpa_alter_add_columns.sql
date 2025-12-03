ALTER TABLE ug_bpa_buildingplans
    ADD COLUMN IF NOT EXISTS pp_fee_receipt_filestore_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS bp_fee_receipt_filestore_id VARCHAR(100);
	
ALTER TABLE ug_bpa_buildingplans_audit 
    ADD COLUMN IF NOT EXISTS pp_fee_receipt_filestore_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS bp_fee_receipt_filestore_id VARCHAR(100);