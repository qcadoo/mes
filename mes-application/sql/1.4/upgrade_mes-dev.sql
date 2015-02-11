-- Added 'priceBasedOn' to parameter
-- last touched 10.02.2015 by kama

ALTER TABLE basic_parameter ADD COLUMN pricebasedon character varying(255);
ALTER TABLE basic_parameter ALTER COLUMN pricebasedon SET DEFAULT '01nominalProductCost'::character varying;

-- end