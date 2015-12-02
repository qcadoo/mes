-- subassembly list dto
-- last touched 01.12.2015 by kama
CREATE SEQUENCE basic_subassemblylistdto_id_seq;
create or replace view basic_subassemblyListDto as
select
    s.id, s.active, s.number, s.name, workstation.number as workstationNumber, workstationType.number as workstationTypeNumber,
    s.productionDate, event.maxDate as lastRepairsDate
    from basic_subassembly s
    left join basic_workstation workstation on (s.workstation_id = workstation.id)
    join basic_workstationType workstationType on (s.workstationtype_id = workstationType.id)
    left join (
        select subassembly_id as subassemblyId, max(date) as maxDate
        from cmmsmachineparts_plannedevent e
        where e.state = '05realized' and e.basedon = '01date'
        group by subassemblyId
    ) event
    on event.subassemblyId = s.id;

-- end

-- changed description length in maintenance event
-- last touched 01.12.2015 by kama
alter table cmmsmachineparts_maintenanceevent alter column description character varying(600);

-- end

-- is default for actions and fault types
-- last touched 02.12.2015 by kama

ALTER TABLE cmmsmachineparts_action ADD COLUMN isdefault boolean;
ALTER TABLE cmmsmachineparts_action ALTER COLUMN isdefault SET DEFAULT false;

UPDATE cmmsmachineparts_action SET isdefault = true WHERE name = 'Inne';
UPDATE cmmsmachineparts_action SET isdefault = false WHERE name != 'Inne';


ALTER TABLE cmmsmachineparts_faulttype ADD COLUMN isdefault boolean;
ALTER TABLE cmmsmachineparts_faulttype ALTER COLUMN isdefault SET DEFAULT false;

UPDATE cmmsmachineparts_faulttype SET isdefault = true WHERE name = 'Inne';
UPDATE cmmsmachineparts_faulttype SET isdefault = false WHERE name != 'Inne';

-- end