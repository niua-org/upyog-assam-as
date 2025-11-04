insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version)
select nextval('state.seq_egdcr_layername'),'LAYER_NAME_LANDSCAPING_AREA','BLK_%s_LANDSCAPING_AREA',1,now(),1,now(),0
where not exists(select key from state.egdcr_layername where key='LAYER_NAME_LANDSCAPING_AREA');

insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version)
select nextval('state.seq_egdcr_layername'),'LAYER_NAME_UNPAVED_AREA','BLK_%s_UNPAVED_AREA',1,now(),1,now(),0
where not exists(select key from state.egdcr_layername where key='LAYER_NAME_UNPAVED_AREA');