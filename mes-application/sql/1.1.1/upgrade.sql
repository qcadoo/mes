-- Table: basic_company

-- changed 30.11.2011

ALTER TABLE basic_company
		ADD COLUMN number character varying(255),
        ADD COLUMN owner boolean DEFAULT false;

-- end
        
-- Table: technologies_technology

ALTER TABLE technologies_technology 
	ALTER COLUMN state SET DEFAULT '01draft'::character varying

BEGIN;
	UPDATE technologies_technology SET state = '01draft' WHERE state = 'draft';
	UPDATE technologies_technology SET state = '02accepted' WHERE state = 'accepted';
	UPDATE technologies_technology SET state = '03declined' WHERE state = 'declined';
	UPDATE technologies_technology SET state = '04outdated' WHERE state = 'outdated';
COMMIT;

-- end