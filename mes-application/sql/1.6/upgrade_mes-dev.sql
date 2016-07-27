-- reservations
-- last touched 28.06.2016 by kama

CREATE TABLE materialflowresources_reservation
(
  id bigint NOT NULL,
  location_id bigint,
  product_id bigint,
  quantity numeric(12,5),
  position_id bigint,
  CONSTRAINT materialflowresources_reservation_pkey PRIMARY KEY (id),
  CONSTRAINT reservation_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT reservation_location_fkey FOREIGN KEY (location_id)
      REFERENCES materialflow_location (id) DEFERRABLE,
  CONSTRAINT reservation_position_fkey FOREIGN KEY (position_id)
      REFERENCES materialflowresources_position (id) DEFERRABLE
);

ALTER TABLE materialflowresources_documentpositionparameters ADD COLUMN draftmakesreservation boolean DEFAULT false;

-- end


-- deliveries changes
-- last touched 20.06.2016 by pako

ALTER TABLE deliveries_deliveredproduct ADD COLUMN additionalquantity numeric(12,5);
ALTER TABLE deliveries_deliveredproduct ADD COLUMN conversion numeric(12,5);
ALTER TABLE deliveries_deliveredproduct ADD COLUMN iswaste boolean;
ALTER TABLE deliveries_deliveredproduct ADD COLUMN additionalunit character varying(255);

CREATE TABLE deliveries_deliveredproductmulti
(
  id bigint NOT NULL,
  delivery_id bigint,
  palletnumber_id bigint,
  pallettype character varying(255),
  storagelocation_id bigint,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT deliveries_deliveredproductmulti_pkey PRIMARY KEY (id),
  CONSTRAINT deliveredproductmulti_delivery_fkey FOREIGN KEY (delivery_id)
      REFERENCES deliveries_delivery (id) DEFERRABLE,
  CONSTRAINT deliveredproductmulti_palletnumber_fkey FOREIGN KEY (palletnumber_id)
      REFERENCES basic_palletnumber (id) DEFERRABLE,
  CONSTRAINT deliveredproductmulti_storagelocation_fkey FOREIGN KEY (storagelocation_id)
      REFERENCES materialflowresources_storagelocation (id) DEFERRABLE
);

CREATE TABLE deliveries_deliveredproductmultiposition
(
  id bigint NOT NULL,
  deliveredproductmulti_id bigint,
  product_id bigint,
  quantity numeric(12,5),
  additionalquantity numeric(12,5),
  conversion numeric(12,5),
  iswaste boolean,
  expirationdate date,
  unit character varying(255),
  additionalunit character varying(255),
  additionalcode_id bigint,
  CONSTRAINT deliveries_deliveredproductmultiposition_pkey PRIMARY KEY (id),
  CONSTRAINT deliveredproductmp_deliveredproductmulti_fkey FOREIGN KEY (deliveredproductmulti_id)
      REFERENCES deliveries_deliveredproductmulti (id) DEFERRABLE,
  CONSTRAINT deliveredproductmp_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT deliveredproductmp_additionalcode_fkey FOREIGN KEY (additionalcode_id)
      REFERENCES basic_additionalcode (id) DEFERRABLE
);

-- end

-- last touched 18.07.2016 by kasi

ALTER TABLE qcadoosecurity_user ADD COLUMN factory_id bigint;
ALTER TABLE qcadoosecurity_user
  ADD CONSTRAINT user_factory_fkey FOREIGN KEY (factory_id)
      REFERENCES basic_factory (id) DEFERRABLE;
-- end

-- work time for users view
-- last touched 20.07.2016 by kama

CREATE SEQUENCE cmmsMachineParts_workTimeForUserDto_id_seq;


CREATE OR REPLACE VIEW cmmsmachineparts_worktimeforuserdto_internal AS
    select u.username as username, swt.effectiveexecutiontimestart as startdate, swt.effectiveexecutiontimeend as finishdate,
        swt.labortime as duration, me.number as eventNumber, me.type as eventtype,
        coalesce(s.number, w.number, p.number, d.number, f.number) as objectnumber, null as actionname
    from cmmsmachineparts_staffworktime swt
        join qcadoosecurity_user u on swt.worker_id = u.staff_id
        join cmmsmachineparts_maintenanceevent me on me.id = swt.maintenanceevent_id
        join basic_factory f on me.factory_id = f.id
        join basic_division d on me.division_id = d.id
        left join productionlines_productionline p on me.productionline_id = p.id
        left join basic_workstation w on me.workstation_id = w.id
        left join basic_subassembly s on me.subassembly_id = s.id
    union all
    select u.username as username, per.startdate as startdate, per.finishdate as finishdate,
        per.duration as duration, pe.number as eventnumber, pe.type as eventtype,
        coalesce(s.number, w.number, p.number, d.number, f.number) as objectnumber, a.name as actionname
    from cmmsmachineparts_plannedeventrealization per
        join qcadoosecurity_user u on per.worker_id = u.staff_id
        join cmmsmachineparts_plannedevent pe on pe.id = per.plannedevent_id
        join basic_factory f on pe.factory_id = f.id
        join basic_division d on pe.division_id = d.id
        left join productionlines_productionline p on pe.productionline_id = p.id
        left join basic_workstation w on pe.workstation_id = w.id
        left join basic_subassembly s on pe.subassembly_id = s.id
        left join cmmsmachineparts_actionforplannedevent afpe on per.action_id = afpe.id
        left join cmmsmachineparts_action a on afpe.action_id = a.id;

CREATE OR REPLACE VIEW cmmsmachineparts_worktimeforuserdto AS
    select row_number() OVER () as id, internal.*
    from cmmsmachineparts_worktimeforuserdto_internal internal;

-- end

-- last touched 26.07.2016 by kasi
INSERT INTO materialflowresources_documentpositionparametersitem(id, name, checked, editable, ordering, parameters_id) VALUES (18, 'productName', false, true, 18, 1);
-- end

-- delivery reservation tables
-- last touched 27.07.2016 by kama
CREATE TABLE deliveries_deliveredproductreservation
(
  id bigint NOT NULL,
  deliveredproduct_id bigint,
  location_id bigint,
  deliveredquantity numeric(12,5),
  additionalquantity numeric(12,5),
  CONSTRAINT deliveries_deliveredproductreservation_pkey PRIMARY KEY (id),
  CONSTRAINT deliveredproductreservation_deliveredproduct_fkey FOREIGN KEY (deliveredproduct_id)
      REFERENCES deliveries_deliveredproduct (id) DEFERRABLE,
  CONSTRAINT deliveredproductreservation_location_fkey FOREIGN KEY (location_id)
      REFERENCES materialflow_location (id) DEFERRABLE
);

CREATE TABLE deliveries_orderedproductreservation
(
  id bigint NOT NULL,
  orderedproduct_id bigint,
  location_id bigint,
  orderedquantity numeric(12,5),
  additionalquantity numeric(12,5),
  CONSTRAINT deliveries_orderedproductreservation_pkey PRIMARY KEY (id),
  CONSTRAINT orderedproductreservation_orderedproduct_fkey FOREIGN KEY (orderedproduct_id)
      REFERENCES deliveries_orderedproduct (id) DEFERRABLE,
  CONSTRAINT orderedproductreservation_location_fkey FOREIGN KEY (location_id)
      REFERENCES materialflow_location (id) DEFERRABLE
);
-- end