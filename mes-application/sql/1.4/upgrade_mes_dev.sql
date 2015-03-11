-- Added state and document type to position for presentation purpose
-- last touched 25.02.2015 by kama

ALTER TABLE materialflowresources_position ADD COLUMN state character varying(255);
ALTER TABLE materialflowresources_position ALTER COLUMN state SET DEFAULT '01draft'::character varying;

ALTER TABLE materialflowresources_position ADD COLUMN type character varying(255);

-- end


-- table: basic_parameter
-- last touched 11.03.2015 by lupo

UPDATE basic_parameter SET pricebasedon = '01nominalProductCost' WHERE pricebasedon IS NULL;

--
