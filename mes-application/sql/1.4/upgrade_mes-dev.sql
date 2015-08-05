-- Added machine parts for event
-- last touched 23.07.2015 by kama

CREATE TABLE cmmsmachineparts_machinepartforevent
(
  id bigint NOT NULL,
  maintenanceevent_id bigint,
  machinepart_id bigint,
  warehouse_id bigint,
  plannedquantity numeric(14,5),
  availablequantity numeric(14,5),
  CONSTRAINT cmmsmachineparts_machinepartforevent_pkey PRIMARY KEY (id),
  CONSTRAINT machinepartforevent_warehouse_fkey FOREIGN KEY (warehouse_id)
      REFERENCES materialflow_location (id) DEFERRABLE,
  CONSTRAINT machinepartforevent_maintenanceevent_fkey FOREIGN KEY (maintenanceevent_id)
      REFERENCES cmmsmachineparts_maintenanceevent (id) DEFERRABLE,
  CONSTRAINT machinepartforevent_machinepart_fkey FOREIGN KEY (machinepart_id)
      REFERENCES basic_product (id) DEFERRABLE
);
-- GOODFOOD-614

CREATE TABLE cmmsmachineparts_maintenanceeventcontext
(
  id bigint NOT NULL,
  factory_id bigint,
  division_id bigint,
  generated boolean DEFAULT false,
  confirmed boolean DEFAULT false,
  CONSTRAINT cmmsmachineparts_maintenanceeventcontext_pkey PRIMARY KEY (id),
  CONSTRAINT maintenanceeventcontext_division_fkey FOREIGN KEY (division_id) REFERENCES basic_division (id) DEFERRABLE,
  CONSTRAINT maintenanceeventcontext_factory_fkey FOREIGN KEY (factory_id) REFERENCES basic_factory (id) DEFERRABLE
);

ALTER TABLE cmmsmachineparts_maintenanceevent ADD maintenanceeventcontext_id bigint;
ALTER TABLE cmmsmachineparts_maintenanceevent ADD CONSTRAINT maintenanceevent_maintenanceeventcontext_fkey FOREIGN KEY (maintenanceeventcontext_id)
      REFERENCES cmmsmachineparts_maintenanceeventcontext (id) DEFERRABLE;

-- end


-- QCADOOCLS-4315
CREATE OR REPLACE VIEW materialflowresources_orderedquantity AS
 SELECT COALESCE(sum(orderedproduct.orderedquantity), 0::numeric) AS orderedquantity, resource.id as resource_id
   FROM materialflowresources_resource resource
	 JOIN deliveries_orderedproduct orderedproduct ON (orderedproduct.product_id = resource.product_id )
	 JOIN deliveries_delivery delivery ON (orderedproduct.delivery_id = delivery.id AND delivery.active = true AND delivery.location_id = resource.location_id AND (delivery.state::text = ANY (ARRAY['01draft'::character varying::text,
		'02prepared'::character varying::text, '03duringCorrection'::character varying::text, '05approved'::character varying::text])))
group by resource.id;

CREATE OR REPLACE VIEW materialflowresources_warehousestocklistdto_internal AS
 SELECT row_number() OVER () AS id,
    resource.location_id,
    resource.product_id,
    sum(resource.quantity) AS quantity,
    COALESCE(orderedquantity.orderedquantity, 0::numeric) as orderedquantity,
    ( SELECT sum(warehouseminimalstate_warehouseminimumstate.minimumstate) AS sum
           FROM warehouseminimalstate_warehouseminimumstate
          WHERE warehouseminimalstate_warehouseminimumstate.product_id = resource.product_id AND warehouseminimalstate_warehouseminimumstate.location_id = resource.location_id) AS minimumstate
   FROM materialflowresources_resource resource
   left join materialflowresources_orderedquantity orderedquantity ON (orderedquantity.resource_id = resource.id)
  GROUP BY resource.location_id, resource.product_id, orderedquantity.orderedquantity;


CREATE OR REPLACE VIEW materialflowresources_warehousestocklistdto AS
select internal.*, location.number as locationNumber, location.name as locationName, product.number as productNumber, product.name as productName, product.unit as productUnit
	from materialflowresources_warehousestocklistdto_internal internal
	join materialflow_location location ON (location.id = internal.location_id)
	join basic_product product ON (product.id = internal.product_id);

-- end