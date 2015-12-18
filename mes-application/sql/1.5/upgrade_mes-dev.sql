-- view for events
-- last touched 09.12.2015 by kama

CREATE SEQUENCE cmmsmachineparts_plannedeventlistdto_id_seq;

create or replace view cmmsmachineparts_plannedEventListDto as
select
    e.id, e.number, e.type, owner.name || ' ' || owner.surname  as ownerName,
    e.description, factory.number as factoryNumber, factory.id as factory_id, division.number as divisionNumber, division.id as division_id,
    productionLine.number as productionLineNumber, workstation.number as workstationNumber,
    subassembly.number as subassemblyNumber, e.date, e.counter, e.createUser, e.createDate, e.state, context.id as plannedEventContext_id
    from cmmsmachineparts_plannedevent e
    left join basic_staff owner on (e.owner_id = owner.id)
    join basic_factory factory on (e.factory_id = factory.id)
    join basic_division division on (e.division_id = division.id)
    left join productionLines_productionLine productionLine on (e.productionline_id = productionLine.id)
    left join basic_workstation workstation on (e.workstation_id = workstation.id)
    left join basic_subassembly subassembly on (e.subassembly_id = subassembly.id)
    left join cmmsmachineparts_plannedeventcontext context on (e.plannedeventcontext_id = context.id);


CREATE SEQUENCE cmmsmachineparts_maintenanceeventlistdto_id_seq;

create or replace view cmmsmachineparts_maintenanceEventListDto as
select
    e.id, e.number, e.type, staff.name || ' ' || staff.surname  as personReceivingName,
    e.description, faultType.name as faultTypeNumber, factory.number as factoryNumber, division.number as divisionNumber,
    factory.id as factory_id, division.id as division_id,
    productionLine.number as productionLineNumber, workstation.number as workstationNumber,
    subassembly.number as subassemblyNumber, e.createUser, e.createDate, e.state, context.id as maintenanceEventContext_id
    from cmmsmachineparts_maintenanceevent e
    left join basic_staff staff on (e.personreceiving_id = staff.id)
    join cmmsmachineparts_faulttype faultType on (e.faulttype_id = faultType.id)
    join basic_factory factory on (e.factory_id = factory.id)
    join basic_division division on (e.division_id = division.id)
    left join productionLines_productionLine productionLine on (e.productionline_id = productionLine.id)
    left join basic_workstation workstation on (e.workstation_id = workstation.id)
    left join basic_subassembly subassembly on (e.subassembly_id = subassembly.id)
    left join cmmsmachineparts_maintenanceeventcontext context on (e.maintenanceeventcontext_id = context.id);

-- end