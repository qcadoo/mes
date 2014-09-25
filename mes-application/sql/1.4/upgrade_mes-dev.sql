-- Columns needed by OzgoHome
-- last touched 19.09.2014 by kama

ALTER TABLE materialflowresources_position ADD COLUMN resource_id bigint;
ALTER TABLE materialflowresources_position ADD CONSTRAINT position_resource_fkey FOREIGN KEY (resource_id)
      REFERENCES materialflowresources_resource (id) DEFERRABLE;

-- end

-- Added resource related columns in deliveredProduct
-- last touched 19.09.2014 by kama

ALTER TABLE deliveries_deliveredproduct ADD COLUMN batch character varying(255);
ALTER TABLE deliveries_deliveredproduct ADD COLUMN productiondate date;
ALTER TABLE deliveries_deliveredproduct ADD COLUMN expirationdate date;

-- end