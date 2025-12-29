ALTER TABLE public.ug_bpa_buildingplans ADD signed_bp_filestore_id varchar(100) NULL;
ALTER TABLE public.ug_bpa_buildingplans ADD signed_pp_filestore_id varchar(100) NULL;
ALTER TABLE public.ug_bpa_buildingplans ADD signed_oc_filestore_id varchar(100) NULL;

ALTER TABLE public.ug_bpa_buildingplans_audit ADD signed_bp_filestore_id varchar(100) NULL;
ALTER TABLE public.ug_bpa_buildingplans_audit ADD signed_pp_filestore_id varchar(100) NULL;
ALTER TABLE public.ug_bpa_buildingplans_audit ADD signed_oc_filestore_id varchar(100) NULL;
