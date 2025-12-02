INSERT INTO egbpa_sub_occupancy (
    id, code, name, ordernumber, isactive, createdby, createddate,
    lastmodifieddate, lastmodifiedby, version, maxfar, occupancy,
    description, colorcode
) VALUES 
(
    nextval('seq_egbpa_sub_occupancy'), 'B-NS', 'Pre primary or Nursery',
    (SELECT MAX(ordernumber) + 1 FROM egbpa_sub_occupancy), 't', 1, NOW(), NOW(),
    1, 0, 0.5, (SELECT id FROM egbpa_occupancy WHERE code = 'B'),
    'Pre primary or Nursery', 80
);

UPDATE egbpa_sub_occupancy
SET colorcode = 40
WHERE code = 'B2';

insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version)
select nextval('state.seq_egdcr_layername'),'LAYER_NAME_TERRACE','BLK_%s_TERRACE',1,now(),1,now(),0
where not exists(select key from state.egdcr_layername where key='LAYER_NAME_TERRACE');
