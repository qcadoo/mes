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

-- Table: productioncounting_staffworktime
-- changed: 19.11.2013 [maku]

CREATE TABLE productioncounting_staffworktime
(
  id bigint NOT NULL,
  productionrecord_id bigint,
  worker_id bigint,
  labortime integer DEFAULT 0,
  CONSTRAINT productioncounting_staffworktime_pkey PRIMARY KEY (id),
  CONSTRAINT pc_swt_basic_staff_fkey FOREIGN KEY (worker_id)
      REFERENCES basic_staff (id) DEFERRABLE,
  CONSTRAINT pc_swt_pc_productionrecord_fkey FOREIGN KEY (productionrecord_id)
      REFERENCES productioncounting_productionrecord (id) DEFERRABLE
)

-- end