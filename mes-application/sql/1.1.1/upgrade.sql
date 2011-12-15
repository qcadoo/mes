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


-- Table: basic_parameter
-- changed 15.12.2011
ALTER TABLE basic_parameter
        ADD COLUMN trackingrecordforordertreatment character varying(255) DEFAULT '01duringProduction'::character varying,
        ADD COLUMN batchnumberrequiredproducts boolean,
        ADD COLUMN batchnumberuniqueness character varying(255) DEFAULT '01globally'::character varying;

-- end

-- Table: technologies_technology
-- changed 15.12.2011
ALTER TABLE technologies_technology
        ADD COLUMN technologybatchrequired boolean;

-- end

        
-- Table: technologies_operationproductincomponent
-- changed 15.12.2011
ALTER TABLE technologies_operationproductincomponent 
		ADD COLUMN productbatchrequired boolean;

-- end


-- Table: orders_order
-- changed 15.12.2011
        
ALTER TABLE orders_order
        ADD COLUMN trackingrecordtreatment character varying(255) DEFAULT '01duringProduction'::character varying;

-- end
