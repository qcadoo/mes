-- Columns needed by OzgoHome
-- last touched 19.09.2014 by kama

ALTER TABLE materialflowresources_position ADD COLUMN resource_id bigint;
ALTER TABLE materialflowresources_position ADD CONSTRAINT position_resource_fkey FOREIGN KEY (resource_id)
      REFERENCES materialflowresources_resource (id) DEFERRABLE;

-- end