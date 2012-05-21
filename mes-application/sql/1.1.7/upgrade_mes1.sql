-- table: orders_order
-- changed: 17.05.2012
ALTER TABLE orders_order ADD COLUMN correcteddatefrom timestamp without time zone;
ALTER TABLE orders_order ADD COLUMN correcteddateto timestamp without time zone;
ALTER TABLE orders_order ADD COLUMN reasontypecorrectiondatefrom character varying(255);
ALTER TABLE orders_order ADD COLUMN reasontypecorrectiondateto character varying(255);
ALTER TABLE orders_order ADD COLUMN commentreasontypecorrectiondatefrom character varying(255);
ALTER TABLE orders_order ADD COLUMN commentreasontypecorrectiondateto character varying(255);
--end

-- table: orders_logging
-- changed: 21.05.2012
ALTER TABLE orders_logging ADD COLUMN reasontype character varying(255);
ALTER TABLE orders_logging ADD COLUMN "comment" character varying(255);
--end