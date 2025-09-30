insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_MEZZANINE_AT_ROOM',
       'BLK_%s_FLR_%s_UNIT_%s_ROOM_%s_MEZ_AREA_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_MEZZANINE_AT_ROOM'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_MEZZANINE_AT_ACROOM',
       'BLK_%s_FLR_%s_UNIT_%s_ACROOM_%s_MEZ_AREA_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_MEZZANINE_AT_ACROOM'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_AC_ROOM',
       'BLK_%s_FLR_%s_UNIT_%s_AC_ROOM_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_AC_ROOM'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_AC_ROOM',
       'BLK_%s_FLR_%s_UNIT_%s_AC_ROOM_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_AC_ROOM'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_NON_INHABITABLE_ROOM',
       'BLK_%s_FLR_%s_UNIT_%s_NON_INHABITABLE_ROOM_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_NON_INHABITABLE_ROOM'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_MEZZANINE_AT_NON_INHABITABLE_ROOM',
       'BLK_%s_FLR_%s_UNIT_%s_ROOM_%s_NONINHABITABLE_ROOM_MEZ_AREA_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_MEZZANINE_AT_NON_INHABITABLE_ROOM'
);
