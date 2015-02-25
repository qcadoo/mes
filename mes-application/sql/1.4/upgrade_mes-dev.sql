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
-- last touched 16.02.2015 by kama

CREATE TABLE orders_recipe
(
  id bigint NOT NULL,
  "number" character varying(255),
  product_id bigint,
  name character varying(1024),
  version character varying(255),
  isdefault boolean,
  strenght character varying(255),
  batchquantity integer,
  batchsize character varying(255),
  description character varying(2048),
  state character varying(255) DEFAULT '01draft'::character varying,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT orders_recipe_pkey PRIMARY KEY (id),
  CONSTRAINT recipe_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE
);

CREATE TABLE orders_instruction
(
  id bigint NOT NULL,
  orderid integer,
  description character varying(1024),
  rangefrom integer,
  rangeto integer,
  rangeunit character varying(255),
  targetvalue integer,
  verificationrequired boolean,
  recipe_id bigint,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT orders_instruction_pkey PRIMARY KEY (id),
  CONSTRAINT instruction_recipe_fkey FOREIGN KEY (recipe_id)
      REFERENCES orders_recipe (id) DEFERRABLE
);

CREATE TABLE orders_material
(
  id bigint NOT NULL,
  product_id bigint,
  quantity numeric(12,5),
  recipe_id bigint,
  instruction_id bigint,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  materials_id bigint,
  "number" character varying(255),
  CONSTRAINT orders_material_pkey PRIMARY KEY (id),
  CONSTRAINT material_recipe_fkey FOREIGN KEY (recipe_id)
      REFERENCES orders_recipe (id) DEFERRABLE,
  CONSTRAINT material_instruction_fkey FOREIGN KEY (instruction_id)
      REFERENCES orders_instruction (id) DEFERRABLE,
  CONSTRAINT material_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT material_materials_fkey FOREIGN KEY (materials_id)
      REFERENCES basic_product (id) DEFERRABLE
);

CREATE TABLE orders_mbrstatechange
(
  id bigint NOT NULL,
  dateandtime timestamp without time zone,
  sourcestate character varying(255),
  targetstate character varying(255),
  status character varying(255),
  phase integer,
  worker character varying(255),
  recipe_id bigint,
  shift_id bigint,
  CONSTRAINT orders_mbrstatechange_pkey PRIMARY KEY (id),
  CONSTRAINT mbrstatechange_recipe_fkey FOREIGN KEY (recipe_id)
      REFERENCES orders_recipe (id) DEFERRABLE,
  CONSTRAINT mbrstatechange_shift_fkey FOREIGN KEY (shift_id)
      REFERENCES basic_shift (id) DEFERRABLE
);

ALTER TABLE orders_order ADD COLUMN recipe_id bigint;
ALTER TABLE orders_order
  ADD CONSTRAINT order_recipe_fkey FOREIGN KEY (recipe_id)
      REFERENCES orders_recipe (id) DEFERRABLE;

ALTER TABLE orders_order ADD COLUMN batchnumber character varying(255);

-- end

-- Added state and document type to position for presentation purpose
-- last touched 25.02.2015 by kama

ALTER TABLE materialflowresources_position ADD COLUMN state character varying(255);
ALTER TABLE materialflowresources_position ALTER COLUMN state SET DEFAULT '01draft'::character varying;

ALTER TABLE materialflowresources_position ADD COLUMN type character varying(255);

-- end