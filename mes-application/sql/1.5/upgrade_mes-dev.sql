-- assignment to shift changes
-- last touched 11.02.2016 by pako

CREATE TABLE assignmenttoshift_multiassignmenttoshift
(
  id bigint NOT NULL,
  productionline_id bigint,
  occupationtype character varying(255),
  occupationtypename character varying(255),
  occupationtypeenum character varying(255),
  masterorder_id bigint,
  assignmenttoshift_id bigint,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT assignmenttoshift_multiassignmenttoshift_pkey PRIMARY KEY (id),
  CONSTRAINT multiassignmenttoshift_masterorder_fkey FOREIGN KEY (masterorder_id)
      REFERENCES masterorders_masterorder (id) DEFERRABLE,
  CONSTRAINT multiassignmenttoshift_productionline_fkey FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline (id) DEFERRABLE,
  CONSTRAINT multiassignmenttoshift_assignmenttoshift_fkey FOREIGN KEY (assignmenttoshift_id)
      REFERENCES assignmenttoshift_assignmenttoshift (id) DEFERRABLE
);

CREATE TABLE jointable_multiassignmenttoshift_staff
(
  multiassignmenttoshift_id bigint NOT NULL,
  staff_id bigint NOT NULL,
  CONSTRAINT jointable_multiassignmenttoshift_staff_pkey PRIMARY KEY (multiassignmenttoshift_id, staff_id),
  CONSTRAINT staff_multiassignmenttoshift_fkey FOREIGN KEY (staff_id)
      REFERENCES basic_staff (id) DEFERRABLE,
  CONSTRAINT assignmenttoshift_multiassignmenttoshift_staff_fkey FOREIGN KEY (multiassignmenttoshift_id)
      REFERENCES assignmenttoshift_multiassignmenttoshift (id) DEFERRABLE
);

ALTER TABLE assignmenttoshift_staffassignmenttoshift ADD COLUMN description character varying(255);

-- end

-- cmmsmachineparts_plannedevent
-- last touched 29.01.2016 by wesi
CREATE OR REPLACE VIEW cmmsmachineparts_plannedeventlistdto as
    select e.id, e.number, e.type, owner.name || ' ' || owner.surname  as ownerName,
            ''::varchar(255) as description, factory.number as factoryNumber, factory.id as factory_id, division.number as divisionNumber,
            division.id as division_id, productionLine.number as productionLineNumber, workstation.number as workstationNumber,
            subassembly.number as subassemblyNumber, e.date, e.counter, e.createUser, e.createDate, e.state, context.id as plannedEventContext_id
    from cmmsmachineparts_plannedevent e left join basic_staff owner on (e.owner_id = owner.id) join basic_factory factory on (e.factory_id = factory.id)
        join basic_division division on (e.division_id = division.id) left join productionLines_productionLine productionLine on (e.productionline_id = productionLine.id)
        left join basic_workstation workstation on (e.workstation_id = workstation.id) left join basic_subassembly subassembly on (e.subassembly_id = subassembly.id)
        left join cmmsmachineparts_plannedeventcontext context on (e.plannedeventcontext_id = context.id);

ALTER TABLE cmmsmachineparts_plannedevent ALTER COLUMN description TYPE character varying(600);

create or replace view cmmsmachineparts_plannedEventListDto as select e.id, e.number, e.type, owner.name || ' ' || owner.surname  as ownerName, e.description, factory.number as factoryNumber, factory.id as factory_id, division.number as divisionNumber, division.id as division_id, productionLine.number as productionLineNumber, workstation.number as workstationNumber, subassembly.number as subassemblyNumber, e.date, e.counter, e.createUser, e.createDate, e.state, context.id as plannedEventContext_id from cmmsmachineparts_plannedevent e left join basic_staff owner on (e.owner_id = owner.id) join basic_factory factory on (e.factory_id = factory.id) join basic_division division on (e.division_id = division.id) left join productionLines_productionLine productionLine on (e.productionline_id = productionLine.id) left join basic_workstation workstation on (e.workstation_id = workstation.id) left join basic_subassembly subassembly on (e.subassembly_id = subassembly.id) left join cmmsmachineparts_plannedeventcontext context on (e.plannedeventcontext_id = context.id);
-- end