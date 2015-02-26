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

-- Added MBR models to orders
-- last touched 26.02.2015 by rawa

CREATE TABLE "orders_instruction" (
  "id" int8 NOT NULL,
  "orderid" int4,
  "description" varchar(1024) COLLATE "default",
  "rangefrom" numeric(19,5),
  "rangeto" numeric(19,5),
  "rangeunit" varchar(255) COLLATE "default",
  "targetvalue" numeric(19,5),
  "verificationrequired" bool,
  "recipe_id" int8,
  "state" varchar(255) DEFAULT '01draft'::character varying COLLATE "default",
  "result" varchar(255) COLLATE "default",
  "comments" varchar(255) COLLATE "default",
  "createdate" timestamp(6) NULL,
  "updatedate" timestamp(6) NULL,
  "createuser" varchar(255) COLLATE "default",
  "updateuser" varchar(255) COLLATE "default",
  CONSTRAINT order_instructionions_orders_recipe_fkey FOREIGN KEY ("recipe_id")
  REFERENCES "orders_recipe" ("id") DEFERRABLE
);


CREATE TABLE "orders_material" (
  "id" int8 NOT NULL,
  "number" varchar(255) COLLATE "default",
  "product_id" int8,
  "quantity" numeric(12,5),
  "recipe_id" int8,
  "instruction_id" int8,
  "createdate" timestamp(6) NULL,
  "updatedate" timestamp(6) NULL,
  "createuser" varchar(255) COLLATE "default",
  "updateuser" varchar(255) COLLATE "default",
  "materials_id" int8,
  CONSTRAINT orders_material_pkey PRIMARY KEY (id),
  CONSTRAINT orders_material_orders_recipe_fkey FOREIGN KEY ("recipe_id")
  REFERENCES "orders_recipe" ("id") DEFERRABLE,
  CONSTRAINT orders_material_basic_product_fkey FOREIGN KEY ("product_id")
  REFERENCES "basic_product" ("id") DEFERRABLE,
  CONSTRAINT orders_material_basic_product_materials_fkey FOREIGN KEY ("materials_id")
  REFERENCES "basic_product" ("id") DEFERRABLE,
  CONSTRAINT orders_material_orders_instruction_fkey FOREIGN KEY ("instruction_id")
  REFERENCES "orders_instruction" ("id") DEFERRABLE,
);


CREATE TABLE "orders_mbrstatechange" (
  "id" int8 NOT NULL,
  "dateandtime" timestamp(6) NULL,
  "sourcestate" varchar(255) COLLATE "default",
  "targetstate" varchar(255) COLLATE "default",
  "status" varchar(255) COLLATE "default",
  "phase" int4,
  "worker" varchar(255) COLLATE "default",
  "recipe_id" int8,
  "shift_id" int8,
  CONSTRAINT orders_mbrstatechange_pkey PRIMARY KEY (id),
  CONSTRAINT orders_mbrstatechange_orders_recipe_fkey FOREIGN KEY ("recipe_id")
  REFERENCES "orders_recipe" ("id") DEFERRABLE,
  CONSTRAINT orders_mbrstatechange_basic_shift_fkey FOREIGN KEY ("shift_id")
  REFERENCES "basic_shift" ("id") DEFERRABLE
);


CREATE TABLE "orders_recipe" (
  "id" int8 NOT NULL,
  "product_id" int8,
  "name" varchar(1024) COLLATE "default",
  "version" varchar(255) COLLATE "default",
  "isdefault" bool,
  "strenght" varchar(255) COLLATE "default",
  "batchquantity" numeric(19,5),
  "batchsize" varchar(255) COLLATE "default",
  "description" varchar(2048) COLLATE "default",
  "state" varchar(255) DEFAULT '01draft'::character varying COLLATE "default",
  "active" bool DEFAULT true,
  "createdate" timestamp(6) NULL,
  "updatedate" timestamp(6) NULL,
  "createuser" varchar(255) COLLATE "default",
  "updateuser" varchar(255) COLLATE "default",
  CONSTRAINT orders_recipe_pkey PRIMARY KEY (id)
);


ALTER TABLE orders_order ADD COLUMN recipe_id bigint;
ALTER TABLE orders_order
  ADD CONSTRAINT order_recipe_fkey FOREIGN KEY (recipe_id)
      REFERENCES orders_recipe (id) DEFERRABLE;

ALTER TABLE orders_order ADD COLUMN batchnumber character varying(255);

-- end