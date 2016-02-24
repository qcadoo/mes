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
  parameters_id bigint,
  name character varying(255),
  CONSTRAINT materialflowresources_documentpositionparametersitem_pkey PRIMARY KEY (id)
);

ALTER TABLE materialflowresources_documentpositionparametersitem
  ADD CONSTRAINT documentpositionparametersitem_parameters_fkey FOREIGN KEY (parameters_id)
      REFERENCES materialflowresources_documentpositionparameters (id) DEFERRABLE;

insert into materialflowresources_documentpositionparameters (id) values (1);
insert into materialflowresources_documentpositionparametersitem (name, parameters_id) values 
	('price', 1),
	('storageLocation', 1),
	('additionalCode', 1),
	('productionDate', 1),
	('expirationDate', 1),
	('pallet', 1),
	('typeOfPallet', 1),
	('batch', 1);

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

insert into materialflowresources_documentpositionparametersitem (name, parameters_id) values 
	('resource', 1);

ALTER TABLE materialflowresources_documentpositionparameters ADD COLUMN suggestresource boolean;
ALTER TABLE materialflowresources_documentpositionparameters ALTER COLUMN suggestresource SET DEFAULT true;
-- end