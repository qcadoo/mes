-- Table: basic_parameter
-- changed: 07.11.2012

ALTER TABLE basic_parameter ADD COLUMN canchangedatewhentransfertowarehouse boolean;
ALTER TABLE basic_parameter ALTER COLUMN canchangedatewhentransfertowarehouse SET DEFAULT false;

-- end

-- Table: basic_parameter
-- changed: 08.11.2012
ALTER TABLE basic_parameter ADD COLUMN inputproductsrequiredfortype character varying(255);

-- end


-- Table: orders_order
-- changed: 08.11.2012
ALTER TABLE orders_order ADD COLUMN inputproductsrequiredfortype character varying(255);

-- end