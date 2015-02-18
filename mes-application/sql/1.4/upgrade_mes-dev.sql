-- Added 'priceBasedOn' to parameter
-- last touched 10.02.2015 by kama

ALTER TABLE basic_parameter ADD COLUMN pricebasedon character varying(255);
ALTER TABLE basic_parameter ALTER COLUMN pricebasedon SET DEFAULT '01nominalProductCost'::character varying;

-- end

-- Added dates audit to orders
-- last touched 13.02.2015 by kama


ALTER TABLE orders_order ADD COLUMN dateschanged boolean;
ALTER TABLE orders_order ALTER COLUMN dateschanged SET DEFAULT false;
ALTER TABLE orders_order ADD COLUMN sourcecorrecteddatefrom timestamp without time zone;
ALTER TABLE orders_order ADD COLUMN sourcecorrecteddateto timestamp without time zone;
ALTER TABLE orders_order ADD COLUMN sourcestartdate timestamp without time zone;
ALTER TABLE orders_order ADD COLUMN sourcefinishdate timestamp without time zone;
ALTER TABLE orders_orderstatechange ADD COLUMN sourcecorrecteddatefrom timestamp without time zone;
ALTER TABLE orders_orderstatechange ADD COLUMN sourcecorrecteddateto timestamp without time zone;
ALTER TABLE orders_orderstatechange ADD COLUMN sourcestartdate timestamp without time zone;
ALTER TABLE orders_orderstatechange ADD COLUMN sourcefinishdate timestamp without time zone;
ALTER TABLE orders_orderstatechange ADD COLUMN targetcorrecteddatefrom timestamp without time zone;
ALTER TABLE orders_orderstatechange ADD COLUMN targetcorrecteddateto timestamp without time zone;
ALTER TABLE orders_orderstatechange ADD COLUMN targetstartdate timestamp without time zone;
ALTER TABLE orders_orderstatechange ADD COLUMN targetfinishdate timestamp without time zone;
ALTER TABLE orders_orderstatechange ADD COLUMN dateschanged boolean;
ALTER TABLE orders_orderstatechange ALTER COLUMN dateschanged SET DEFAULT false;


-- end