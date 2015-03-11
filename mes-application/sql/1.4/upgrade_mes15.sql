-- Added MBR models to orders
-- last touched 26.02.2015 by rawa

ALTER TABLE productioncounting_staffworktime ADD COLUMN effectiveexecutiontimestart timestamp without time zone;

ALTER TABLE productioncounting_staffworktime ADD COLUMN effectiveexecutiontimeend timestamp without time zone;


CREATE TABLE orders_recipe
(
 id bigint NOT NULL,
 product_id bigint,
 name character varying(1024),
 version character varying(255),
 isdefault boolean,
 strength character varying(255),
 batchquantity numeric(19,5),
 batchsize character varying(255),
 description character varying(2048),
 state character varying(255) DEFAULT '01draft'::character varying,
 active boolean DEFAULT true,
 createdate timestamp without time zone,
 updatedate timestamp without time zone,
 createuser character varying(255),
 updateuser character varying(255),
 CONSTRAINT orders_recipe_pkey PRIMARY KEY (id),
 CONSTRAINT orders_recipe_basic_product FOREIGN KEY (product_id)
     REFERENCES basic_product (id) DEFERRABLE
);


 -- Table: orders_instruction

 -- DROP TABLE orders_instruction;

 CREATE TABLE orders_instruction
 (
   id bigint NOT NULL,
   orderid integer,
   description character varying(1024),
   rangefrom numeric(19,5),
   rangeto numeric(19,5),
   rangeunit character varying(255),
   targetvalue numeric(19,5),
   verificationrequired boolean,
   recipe_id bigint,
   state character varying(255) DEFAULT '01draft'::character varying,
   result character varying(255),
   comments character varying(255),
   createdate timestamp without time zone,
   updatedate timestamp without time zone,
   createuser character varying(255),
   updateuser character varying(255),
   CONSTRAINT orders_instruction_pkey PRIMARY KEY (id),
   CONSTRAINT orders_instruction_orders_recipe FOREIGN KEY (recipe_id)
       REFERENCES orders_recipe (id) DEFERRABLE
 );


-- Table: orders_mbrstatechange

-- DROP TABLE orders_mbrstatechange;

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
 CONSTRAINT orders_mbrstatechange_orders_recipe FOREIGN KEY (recipe_id)
     REFERENCES orders_recipe (id) DEFERRABLE,
 CONSTRAINT orders_mbrstatechange_basic_shift FOREIGN KEY (shift_id)
     REFERENCES basic_shift (id) DEFERRABLE
);


 -- Table: orders_material

 -- DROP TABLE orders_material;

 CREATE TABLE orders_material
 (
   id bigint NOT NULL,
   "number" character varying(255),
   product_id bigint,
   quantity numeric(12,5),
   recipe_id bigint,
   instruction_id bigint,
   createdate timestamp without time zone,
   updatedate timestamp without time zone,
   createuser character varying(255),
   updateuser character varying(255),
   materials_id bigint,
   CONSTRAINT orders_material_pkey PRIMARY KEY (id),
   CONSTRAINT orders_material_orders_recipe FOREIGN KEY (recipe_id)
       REFERENCES orders_recipe (id) DEFERRABLE,
   CONSTRAINT orders_material_orders_instruction FOREIGN KEY (instruction_id)
       REFERENCES orders_instruction (id) DEFERRABLE,
   CONSTRAINT orders_material_basic_product FOREIGN KEY (product_id)
       REFERENCES basic_product (id) DEFERRABLE,
   CONSTRAINT orders_material_basic_product_material FOREIGN KEY (materials_id)
       REFERENCES basic_product (id) DEFERRABLE
 );

 ALTER TABLE orders_order ADD COLUMN recipe_id bigint;
 ALTER TABLE orders_order
   ADD CONSTRAINT order_recipe_fkey FOREIGN KEY (recipe_id)
       REFERENCES orders_recipe (id) DEFERRABLE;

 ALTER TABLE orders_order ADD COLUMN batchnumber character varying(255);
-- end