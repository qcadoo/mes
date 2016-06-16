-- new ordered product fields
-- last touched 01.06.2016 by pako

ALTER TABLE deliveries_orderedproduct ADD COLUMN additionalquantity numeric(12,5);
ALTER TABLE deliveries_orderedproduct ADD COLUMN conversion numeric(12,5);
ALTER TABLE deliveries_orderedproduct ADD COLUMN additionalcode_id bigint;
ALTER TABLE deliveries_orderedproduct
  ADD CONSTRAINT orderedproduct_additionalcode_fkey FOREIGN KEY (additionalcode_id)
      REFERENCES basic_additionalcode (id) DEFERRABLE;
UPDATE deliveries_orderedproduct SET additionalquantity = orderedquantity, conversion = 1;

-- end

-- resource stock view
-- last touched 30.05.2016 by kama

CREATE TABLE materialflowresources_resourcestock
(
  id bigint NOT NULL,
  location_id bigint,
  product_id bigint,
  quantity numeric(12,5),
  reservedquantity numeric(12,5),
  availablequantity numeric(12,5),
  CONSTRAINT materialflowresources_resourcestock_pkey PRIMARY KEY (id),
  CONSTRAINT resourcestock_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT resourcestock_location_fkey FOREIGN KEY (location_id)
      REFERENCES materialflow_location (id) DEFERRABLE
);

CREATE OR REPLACE VIEW materialflowresources_resourcestockdto AS
    SELECT row_number() OVER () AS id, product_id, location_id,
    product.name AS productName, product.number AS productNumber, product.unit AS productUnit,
    location.number AS locationNumber, location.name AS locationName,
    (SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) FROM warehouseminimalstate_warehouseminimumstate
        WHERE warehouseminimalstate_warehouseminimumstate.product_id = stock.product_id
            AND warehouseminimalstate_warehouseminimumstate.location_id = stock.location_id) AS minimumstate,
    ( SELECT COALESCE(SUM(deliveries_orderedproduct.orderedquantity),0) AS sum
        FROM deliveries_orderedproduct, deliveries_delivery
        WHERE deliveries_orderedproduct.delivery_id = deliveries_delivery.id
            AND deliveries_delivery.active = true AND deliveries_delivery.location_id = stock.location_id
            AND deliveries_orderedproduct.product_id = stock.product_id
            AND deliveries_delivery.state IN ('01draft', '02prepared', '03duringCorrection', '05approved')) AS orderedquantity,
    quantity, reservedQuantity, availableQuantity
    FROM materialflowresources_resourcestock stock
    LEFT JOIN basic_product product ON stock.product_id = product.id
    LEFT JOIN materialflow_location location ON stock.location_id = location.id;

-- end

-- wastes
-- last touched 03.06.2016 by kama

ALTER TABLE productioncounting_trackingoperationproductoutcomponent ADD COLUMN wastesquantity numeric(14,5);
ALTER TABLE orders_order ADD COLUMN wastesquantity numeric(12,5);

-- end

-- moved fault types to basic
-- last touched 08.06.16 by lupo

ALTER TABLE cmmsmachineparts_faulttype RENAME TO basic_faulttype;

ALTER TABLE basic_faulttype RENAME CONSTRAINT cmmsmachineparts_faulttype_pkey TO basic_faulttype_pkey;

ALTER SEQUENCE cmmsmachineparts_faulttype_id_seq RENAME TO basic_faulttype_id_seq;

UPDATE qcadooview_view SET pluginidentifier = 'basic' WHERE name = 'faultTypesList';
UPDATE qcadooview_item SET pluginidentifier = 'basic' WHERE name = 'faultTypes';

DROP VIEW cmmsmachineparts_maintenanceeventlistdto;

CREATE OR replace VIEW cmmsmachineparts_maintenanceeventlistdto AS
    SELECT
        maintenanceevent.id AS id,
        maintenanceevent.number AS number,
        maintenanceevent.type AS type,
        maintenanceevent.createuser AS createuser,
        maintenanceevent.createdate As createdate,
        maintenanceevent.state AS state,
        maintenanceevent.description AS description,
        context.id AS maintenanceeventcontext_id,
        staff.name || ' ' || staff.surname  as personreceivingname,
        factory.id::integer as factory_id,
        factory.number as factorynumber,
        division.id::integer as division_id,
        division.number as divisionnumber,
        workstation.number as workstationnumber,
        subassembly.number as subassemblynumber,
        faultType.name AS faulttypename,
        productionLine.number as productionlinenumber
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
    LEFT JOIN basic_faulttype faultType
        ON maintenanceevent.faulttype_id = faultType.id
    LEFT JOIN productionLines_productionLine productionLine
        ON maintenanceevent.productionline_id = productionLine.id;

-- end

-- new delivered product fields
-- last touched 9.06.2016 by pako

ALTER TABLE deliveries_deliveredproduct ADD COLUMN palletnumber_id bigint;
ALTER TABLE deliveries_deliveredproduct ADD COLUMN pallettype character varying(255);
ALTER TABLE deliveries_deliveredproduct ADD COLUMN storagelocation_id bigint;
ALTER TABLE deliveries_deliveredproduct ADD COLUMN additionalcode_id bigint;

ALTER TABLE deliveries_deliveredproduct
  ADD CONSTRAINT deliveredproduct_additionalcode_fkey FOREIGN KEY (additionalcode_id)
      REFERENCES basic_additionalcode (id) DEFERRABLE;
ALTER TABLE deliveries_deliveredproduct
  ADD CONSTRAINT deliveredproduct_palletnumber_fkey FOREIGN KEY (palletnumber_id)
      REFERENCES basic_palletnumber (id) DEFERRABLE;
ALTER TABLE deliveries_deliveredproduct
  ADD CONSTRAINT deliveredproduct_storagelocation_fkey FOREIGN KEY (storagelocation_id)
      REFERENCES materialflowresources_storagelocation (id) DEFERRABLE;

-- end
