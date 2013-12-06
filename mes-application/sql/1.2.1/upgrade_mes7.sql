-- Table: qcadoomodel_dictionary
-- changed: 18.10.2013

INSERT INTO qcadoomodel_dictionary(id, name, pluginidentifier, active)
    VALUES (nextval('hibernate_sequence') + 1000, 'paymentForm', 'deliveries', true);

-- end


-- Table: deliveries_delivery
-- changed: 18.10.2013

ALTER TABLE deliveries_delivery DROP COLUMN paymentForm;

-- end


-- Table: productioncounting_productionrecord
-- changed: 19.11.2013 [maku]

ALTER TABLE productioncounting_productionrecord ADD COLUMN laststatechangefails boolean DEFAULT false;
ALTER TABLE productioncounting_productionrecord ADD COLUMN laststatechangefailcause character varying(255);
ALTER TABLE productioncounting_productionrecord ADD COLUMN isexternalsynchronized boolean DEFAULT true;
ALTER TABLE productioncounting_productionrecord ADD COLUMN timerangefrom date;
ALTER TABLE productioncounting_productionrecord ADD COLUMN timerangeto date;
ALTER TABLE productioncounting_productionrecord ADD COLUMN shiftstartday date;

-- end


-- Table: basic_company
-- changed: 28.11.2013

ALTER TABLE basic_company ADD COLUMN paymentform character varying(255);

-- end
