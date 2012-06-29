
-- Table: advancedgenealogy_batchlogging --> advancedgenealogy_batchstatechange
-- changed: 28.06.2012

ALTER TABLE advancedgenealogy_batchlogging RENAME TO advancedgenealogy_batchstatechange;

ALTER TABLE advancedgenealogy_batchstatechange RENAME COLUMN previousstate TO sourcestate;
ALTER TABLE advancedgenealogy_batchstatechange RENAME COLUMN currentstate TO targetstate;

ALTER TABLE advancedgenealogy_batchstatechange ADD COLUMN status character varying(255);
ALTER TABLE advancedgenealogy_batchstatechange ALTER COLUMN status SET DEFAULT '01inProgress'::character varying;

ALTER TABLE advancedgenealogy_batchstatechange ADD COLUMN phase integer;

ALTER TABLE advancedgenealogy_batchstatechange ADD COLUMN shift_id bigint;

ALTER TABLE advancedgenealogy_batchstatechange 
	ADD CONSTRAINT basic_shift_fkey 
		FOREIGN KEY (shift_id)
    	REFERENCES basic_shift (id) 
    	DEFERRABLE;

UPDATE advancedgenealogy_batchstatechange SET status = '03successful';

UPDATE advancedgenealogy_batchstatechange SET phase = 4;

-- end

-- Table: advancedgenealogy_trackingrecordlogging --> advancedgenealogy_trackingrecordstatechange
-- changed: 28.06.2012

ALTER TABLE advancedgenealogy_trackingrecordlogging RENAME TO advancedgenealogy_trackingrecordstatechange;

ALTER TABLE advancedgenealogy_trackingrecordstatechange RENAME COLUMN previousstate TO sourcestate;
ALTER TABLE advancedgenealogy_trackingrecordstatechange RENAME COLUMN currentstate TO targetstate;

ALTER TABLE advancedgenealogy_trackingrecordstatechange ADD COLUMN status character varying(255);
ALTER TABLE advancedgenealogy_trackingrecordstatechange ALTER COLUMN status SET DEFAULT '01inProgress'::character varying;

ALTER TABLE advancedgenealogy_trackingrecordstatechange ADD COLUMN phase integer;

ALTER TABLE advancedgenealogy_trackingrecordstatechange ADD COLUMN shift_id bigint;

ALTER TABLE advancedgenealogy_trackingrecordstatechange 
	ADD CONSTRAINT basic_shift_fkey 
		FOREIGN KEY (shift_id)
    	REFERENCES basic_shift (id) 
    	DEFERRABLE;

UPDATE advancedgenealogy_trackingrecordstatechange SET status = '03successful';

UPDATE advancedgenealogy_trackingrecordstatechange SET phase = 4;

-- end

-- Table: states_message
-- changed: 28.06.2012

ALTER TABLE states_message 
	ADD COLUMN batchstatechange_id bigint;

ALTER TABLE states_message 
	ADD CONSTRAINT message_batchstatechange_fkey 
		FOREIGN KEY (batchstatechange_id)
		REFERENCES advancedgenealogy_batchstatechange (id) 
		DEFERRABLE;

ALTER TABLE states_message 
	ADD COLUMN trackingrecordstatechange_id bigint;

ALTER TABLE states_message 
	ADD CONSTRAINT message_trackingrecordstatechange_fkey 
		FOREIGN KEY (trackingrecordstatechange_id)
		REFERENCES advancedgenealogy_trackingrecordstatechange (id) 
		DEFERRABLE;
		
-- end
