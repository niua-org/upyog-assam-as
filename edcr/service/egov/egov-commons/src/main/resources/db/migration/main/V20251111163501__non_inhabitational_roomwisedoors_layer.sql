insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) 
select nextval('state.seq_egdcr_layername'),'LAYER_NAME_UNIT_NONINHABITATIONAL_ROOM_DOOR','BLK_%s_FLR_%s_NON_INHABITATIONAL_ROOM_%s_DOOR_%s',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_UNIT_NONINHABITATIONAL_ROOM_DOOR');

insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) 
select nextval('state.seq_egdcr_layername'),'LAYER_NAME_DRIVEWAY','PARKING_DRIVEWAY',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_DRIVEWAY');