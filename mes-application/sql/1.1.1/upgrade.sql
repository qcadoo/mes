-- Table: basic_company
-- changed 06.12.2011

ALTER TABLE basic_company
		ADD COLUMN number character varying(255),
        ADD COLUMN owner boolean DEFAULT false;

-- end
        
        
-- Table: technologies_technology

ALTER TABLE technologies_technology 
	ALTER COLUMN state SET DEFAULT '01draft'::character varying;

BEGIN;
	UPDATE technologies_technology SET state = '01draft' WHERE state = 'draft';
	UPDATE technologies_technology SET state = '02accepted' WHERE state = 'accepted';
	UPDATE technologies_technology SET state = '03declined' WHERE state = 'declined';
	UPDATE technologies_technology SET state = '04outdated' WHERE state = 'outdated';
COMMIT;

-- end


-- Table: basic_currency

ALTER TABLE basic_currency DROP COLUMN isactive;


-- Table: company
-- changed 09.12.2011

UPDATE basic_company SET owner = true;

-- end


-- Table: qcadooview_view
-- changed 09.12.2011

UPDATE qcadooview_view SET name='company' WHERE name='companyDetails';

-- end


-- Table: qcadooview_item
-- changed 09.12.2011

UPDATE qcadooview_item SET name='company' WHERE name='companyDetails';

-- end


-- Table: basic_company
-- changed 12.12.2011

ALTER TABLE basic_company
        RENAME COLUMN companyfullname TO name;
ALTER TABLE basic_company
        RENAME COLUMN addresswww TO website;
        
ALTER TABLE basic_company
        ALTER COLUMN tax TYPE character varying(255),
        ALTER COLUMN zipcode TYPE character varying(6),
        ALTER COLUMN state TYPE character varying(255),
        ALTER COLUMN country TYPE character varying(255),
        ALTER COLUMN email TYPE character varying(255),
        ALTER COLUMN phone TYPE character varying(255);
        
UPDATE basic_company SET number = '1';

-- end


-- Table: orders_order
-- changed 15.12.2011
        
ALTER TABLE orders_order
        ADD COLUMN trackingrecordtreatment character varying(255) DEFAULT '01duringProduction'::character varying;

-- end


----- WORKPLANS ----

-- Table: basic_division

CREATE TABLE basic_division
(
  id bigint NOT NULL,
  "number" character varying(40),
  "name" character varying(255),
  supervisor_id bigint,
  CONSTRAINT basic_division_pkey PRIMARY KEY (id),
  CONSTRAINT fkf0b0619e64280dc0 FOREIGN KEY (supervisor_id)
      REFERENCES basic_staff (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);

ALTER TABLE basic_machines RENAME TO basic_workstationType;

ALTER TABLE basic_workstationType ADD COLUMN division_id bigint;

ALTER TABLE basic_workstationType ADD CONSTRAINT basic_workstationType_fkey_divisions  FOREIGN KEY (divisions_id)
	REFERENCES basic_division (id) MATCH SIMPLE 
	ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE technologies_operation DROP COLUMN staff_id;

ALTER TABLE workplans_workplan ADD COLUMN type character varying(255) DEFAULT '01allOperations'::character varying;
