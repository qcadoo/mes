-- view for events
-- last touched 09.12.2015 by kama

CREATE SEQUENCE cmmsmachineparts_plannedeventlistdto_id_seq;

CREATE OR REPLACE VIEW cmmsmachineparts_plannedeventlistdto AS
    SELECT
        plannedevent.id AS id,
        plannedevent.number AS number,
        plannedevent.type AS type,
        plannedevent.description AS description,
        plannedevent.date::TIMESTAMP WITHOUT time ZONE AS date,
        plannedevent.counter AS counter,
        plannedevent.createUser AS createuser,
        plannedevent.createDate AS createdate,
        plannedevent.state AS state,
        context.id AS plannedeventcontext_id,
        sourcecost.id AS sourcecost_id,
        staff.name || ' ' || staff.surname AS ownername,
        factory.id::integer AS factory_id,
        factory.number AS factorynumber,
        division.id::integer AS division_id,
        division.number AS divisionnumber,
        workstation.id::integer AS workstation_id,
        workstation.number AS workstationnumber,
        subassembly.id::integer AS subassembly_id,
        subassembly.number AS subassemblynumber,
        company.id::integer AS company_id,
        productionline.number AS productionlinenumber
    FROM cmmsmachineparts_plannedevent plannedevent
    LEFT JOIN cmmsmachineparts_plannedeventcontext context
        ON plannedevent.plannedeventcontext_id = context.id
    LEFT JOIN cmmsmachineparts_sourcecost sourcecost
        ON plannedevent.sourcecost_id = sourcecost.id
    LEFT JOIN basic_staff staff
        ON plannedevent.owner_id = staff.id
    LEFT JOIN basic_factory factory
        ON plannedevent.factory_id = factory.id
    LEFT JOIN basic_division division
        ON plannedevent.division_id = division.id
    LEFT JOIN basic_workstation workstation
        ON plannedevent.workstation_id = workstation.id
    LEFT JOIN basic_subassembly subassembly
        ON plannedevent.subassembly_id = subassembly.id
    LEFT JOIN basic_company company
        ON plannedevent.company_id = company.id
    LEFT JOIN productionlines_productionline productionline
        ON plannedevent.productionline_id = productionline.id;

CREATE SEQUENCE cmmsmachineparts_maintenanceeventlistdto_id_seq;

CREATE OR REPLACE VIEW cmmsmachineparts_maintenanceeventlistdto AS
    SELECT
        maintenanceevent.id AS id,
        maintenanceevent.number AS number,
        maintenanceevent.type AS type,
        maintenanceevent.createuser AS createuser,
        maintenanceevent.createdate AS createdate,
        maintenanceevent.state AS state,
        maintenanceevent.description AS description,
        context.id AS maintenanceeventcontext_id,
        staff.name || ' ' || staff.surname AS personreceivingname,
        factory.id::integer AS factory_id,
        factory.number AS factorynumber,
        division.id::integer AS division_id,
        division.number AS divisionnumber,
        workstation.number AS workstationnumber,
        subassembly.number AS subassemblynumber,
        faulttype.name AS faulttypename,
        productionline.number AS productionlinenumber
    FROM cmmsmachineparts_maintenanceevent maintenanceevent
    LEFT JOIN cmmsmachineparts_maintenanceeventcontext context
        ON maintenanceevent.maintenanceeventcontext_id = context.id
    LEFT JOIN basic_staff staff
        ON maintenanceevent.personreceiving_id = staff.id
    LEFT JOIN basic_factory factory
        ON maintenanceevent.factory_id = factory.id
    LEFT JOIN basic_division division
        ON maintenanceevent.division_id = division.id
    LEFT JOIN basic_workstation workstation
        ON maintenanceevent.workstation_id = workstation.id
    LEFT JOIN basic_subassembly subassembly
        ON maintenanceevent.subassembly_id = subassembly.id
    LEFT JOIN cmmsmachineparts_faulttype faulttype
        ON maintenanceevent.faulttype_id = faulttype.id
    LEFT JOIN productionlines_productionline productionline
        ON maintenanceevent.productionline_id = productionline.id;

-- end