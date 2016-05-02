
ALTER TABLE materialflowresources_resource ADD COLUMN quantityinadditionalunit numeric(14,5);

ALTER TABLE materialflowresources_resource ADD COLUMN additionalcode_id bigint;
ALTER TABLE materialflowresources_resource
  ADD CONSTRAINT resource_additionalcode_fkey FOREIGN KEY (additionalcode_id)
      REFERENCES basic_additionalcode (id) DEFERRABLE;

ALTER TABLE materialflowresources_resource ADD COLUMN conversion numeric(12,5);
ALTER TABLE materialflowresources_resource ALTER COLUMN conversion SET DEFAULT 0::numeric;

ALTER TABLE materialflowresources_resource ADD COLUMN palletnumber_id bigint;
ALTER TABLE materialflowresources_resource
  ADD CONSTRAINT resource_palletnumber_fkey FOREIGN KEY (palletnumber_id)
      REFERENCES basic_palletnumber (id) DEFERRABLE;

ALTER TABLE materialflowresources_resource ADD COLUMN typeofpallet character varying(255);

ALTER TABLE materialflowresources_resource ADD COLUMN givenunit character varying(255);

-- end

-- ESILCO-16
CREATE TABLE materialflowresources_documentpositionparameters
(
  id bigint NOT NULL,
  CONSTRAINT materialflowresources_documentpositionparameters_pkey PRIMARY KEY (id)
);

CREATE TABLE materialflowresources_documentpositionparametersitem
(
  id bigint NOT NULL,
  checked boolean DEFAULT true,
  editable boolean DEFAULT true,
  parameters_id bigint,
  name character varying(255),
  ordering integer,
  CONSTRAINT materialflowresources_documentpositionparametersitem_pkey PRIMARY KEY (id)
);

ALTER TABLE materialflowresources_documentpositionparametersitem
  ADD CONSTRAINT documentpositionparametersitem_parameters_fkey FOREIGN KEY (parameters_id)
      REFERENCES materialflowresources_documentpositionparameters (id) DEFERRABLE;

insert into materialflowresources_documentpositionparameters (id) values (1);
insert into materialflowresources_documentpositionparametersitem (id,ordering,name, parameters_id, editable) values         
        (1,1,'act', 1, false),
        (2,2,'number', 1, false),
        (3,3,'product', 1, false),
	(4,4,'additionalCode', 1, true),
        (5,5,'quantity', 1, false),
        (6,6,'unit', 1, false),
        (7,7,'givenquantity', 1, false),
        (8,8,'givenunit', 1, false),
        (9,9,'conversion', 1, false),
        (10,10,'resource', 1, true),
	(11,11,'price', 1, true),
	(12,12,'batch', 1, true),
	(13,13,'productiondate', 1, true),
	(14,14,'expirationdate', 1, true),
	(15,15,'storageLocation', 1, true),
	(16,16,'palletNumber', 1, true),
	(17,17,'typeOfPallet', 1, true);

ALTER TABLE basic_parameter ADD COLUMN documentpositionparameters_id bigint;

ALTER TABLE basic_parameter
  ADD CONSTRAINT parammeter_documentpositionparameters_fkey FOREIGN KEY (documentpositionparameters_id)
      REFERENCES materialflowresources_documentpositionparameters (id) DEFERRABLE;
-- end


ALTER TABLE materialflowresources_position ADD additionalcode_id bigint;
ALTER TABLE materialflowresources_position ADD conversion numeric(12,5) DEFAULT 0::numeric;
ALTER TABLE materialflowresources_position ADD palletnumber_id bigint;
ALTER TABLE materialflowresources_position ADD typeofpallet character varying(255);

ALTER TABLE materialflowresources_position
  ADD CONSTRAINT position_additionalcode_fkey FOREIGN KEY (additionalcode_id)
      REFERENCES basic_additionalcode (id) DEFERRABLE;

ALTER TABLE materialflowresources_position
  ADD CONSTRAINT position_palletnumber_fkey FOREIGN KEY (palletnumber_id)
      REFERENCES basic_palletnumber (id) DEFERRABLE;

-- resource lookup changes
-- last touched 23.02.2016 by pako
ALTER TABLE materialflowresources_documentpositionparameters ADD COLUMN suggestresource boolean;
ALTER TABLE materialflowresources_documentpositionparameters ALTER COLUMN suggestresource SET DEFAULT false;
-- end

-- added helper for master orders
-- last touched 22.04.2016 by kama

ALTER TABLE orders_order ADD COLUMN masterorderproductcomponent_id bigint;
ALTER TABLE orders_order
  ADD CONSTRAINT order_masterorderproduct_fkey FOREIGN KEY (masterorderproductcomponent_id)
      REFERENCES masterorders_masterorderproduct (id) DEFERRABLE;

-- end

-- last touched 18.04.2016 by kasi
-- Table: productionpershift_dailyprogress

ALTER TABLE productionpershift_dailyprogress ADD COLUMN efficiencytime integer;

-- end

-- last touched 02.05.2016 by kasi
-- delete simpleMaterialBalance view
DELETE FROM qcadooview_item WHERE name='simpleMaterialBalance';
DELETE FROM qcadooview_view WHERE name='simpleMaterialBalanceList';
-- end
