--QCADOOCLS-4341 order list optimizations

DROP TABLE IF EXISTS orders_orderlistdto;

CREATE SEQUENCE orders_orderlistdto_id_seq;

CREATE OR REPLACE VIEW orders_orderlistdto AS SELECT o.id, o.active, o.number, o.name,  o.datefrom,  o.dateto, o.startdate,  o.finishdate, o.state, o.externalnumber, o.externalsynchronized, o.issubcontracted,  o.plannedquantity, o.workplandelivered,  o.deadline,  product.number AS productnumber,  tech.number AS technologynumber,  product.unit,  master.number AS masterordernumber, division.name AS divisionname,  company.name AS companyname, masterdefinition.number AS masterorderdefinitionnumber FROM orders_order o JOIN basic_product product ON o.product_id = product.id LEFT JOIN technologies_technology tech ON o.technology_id = tech.id LEFT JOIN basic_company company ON o.company_id = company.id LEFT JOIN masterorders_masterorder master ON o.masterorder_id = master.id LEFT JOIN masterorders_masterorderdefinition masterdefinition ON master.masterorderdefinition_id = masterdefinition.id LEFT JOIN basic_division division ON tech.division_id = division.id;

-- end


-- add source cost
-- last touched 02.09.2015 by kasi

CREATE TABLE cmmsmachineparts_sourcecost
(
  id bigint NOT NULL,
  "number" character varying(255),
  name character varying(1024),
  factory_id bigint,
  defaultcost boolean DEFAULT false,
  CONSTRAINT cmmsmachineparts_sourcecost_pkey PRIMARY KEY (id),
  CONSTRAINT sourcecost_factory_fkey FOREIGN KEY (factory_id)
      REFERENCES basic_factory (id) DEFERRABLE
);

ALTER TABLE cmmsmachineparts_maintenanceevent ADD COLUMN sourcecost_id bigint;

ALTER TABLE cmmsmachineparts_maintenanceevent
  ADD CONSTRAINT maintenanceevent_sourcecost_fkey FOREIGN KEY (sourcecost_id)
      REFERENCES cmmsmachineparts_sourcecost (id) DEFERRABLE;

ALTER TABLE cmmsmachineparts_plannedevent ADD COLUMN sourcecost_id bigint;
ALTER TABLE cmmsmachineparts_plannedevent
  ADD CONSTRAINT plannedevent_sourcecost_fkey FOREIGN KEY (sourcecost_id)
      REFERENCES cmmsmachineparts_sourcecost (id) DEFERRABLE;

-- end

-- added crews && missing scripts
-- last touched 15.09.2015 by kama
CREATE TABLE basic_crew
(
  id bigint NOT NULL,
  "number" character varying(255),
  leader_id bigint,
  active boolean DEFAULT true,
  CONSTRAINT basic_crew_pkey PRIMARY KEY (id),
  CONSTRAINT crew_staff_fkey FOREIGN KEY (leader_id)
      REFERENCES basic_staff (id) DEFERRABLE
);

ALTER TABLE basic_staff ADD COLUMN crew_id bigint;
ALTER TABLE basic_staff
  ADD CONSTRAINT staff_crew_fkey FOREIGN KEY (crew_id)
      REFERENCES basic_crew (id);

ALTER TABLE assignmenttoshift_assignmenttoshift ADD COLUMN crew_id bigint;
ALTER TABLE assignmenttoshift_assignmenttoshift
  ADD CONSTRAINT assignmenttoshift_crew_fkey FOREIGN KEY (crew_id)
      REFERENCES basic_crew (id) DEFERRABLE;

ALTER TABLE cmmsmachineparts_sourcecost ADD COLUMN active boolean;
ALTER TABLE cmmsmachineparts_sourcecost ALTER COLUMN active SET DEFAULT true;


-- end