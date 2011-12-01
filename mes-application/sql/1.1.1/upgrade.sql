-- Table: basic_company

-- changed 30.11.2011

ALTER TABLE basic_company
		ADD COLUMN number character varying(255),
        ADD COLUMN owner boolean DEFAULT false;

-- end