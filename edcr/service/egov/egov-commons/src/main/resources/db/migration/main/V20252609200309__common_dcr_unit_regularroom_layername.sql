insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_REGULAR_ROOM',
       'BLK_%s_FLR_%s_UNIT_%s_REGULAR_ROOM',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_REGULAR_ROOM'
);

insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT',
       'UNIT',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT'
);
