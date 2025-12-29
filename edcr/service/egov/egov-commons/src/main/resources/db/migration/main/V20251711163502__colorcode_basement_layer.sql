UPDATE egbpa_occupancy
SET colorcode = 40
WHERE name = 'Factories'
  AND code = 'K'
  AND colorcode = 30;

UPDATE state.egbpa_occupancy
SET colorcode = 40
WHERE name = 'Factories'
  AND code = 'K'
  AND colorcode = 30;

insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) 
select nextval('state.seq_egdcr_layername'),'LAYER_NAME_SPLIT_LEVEL_BASEMENT_PARKING_HEIGHT_COLOUR_CODE','COLOUR_CODE_LEVEL_OF_SPLIT_LEVEL_BASEMENT',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_SPLIT_LEVEL_BASEMENT_PARKING_HEIGHT_COLOUR_CODE');
