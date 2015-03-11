-- Added state and document type to position for presentation purpose
-- last touched 25.02.2015 by kama

ALTER TABLE materialflowresources_position ADD COLUMN state character varying(255);
ALTER TABLE materialflowresources_position ALTER COLUMN state SET DEFAULT '01draft'::character varying;

ALTER TABLE materialflowresources_position ADD COLUMN type character varying(255);

-- end


-- table: basic_parameter
-- last touched 11.03.2015 by lupo

UPDATE basic_parameter SET pricebasedon = '01nominalProductCost' WHERE pricebasedon IS NULL;

-- end


-- table: orders_formula
-- last touched 11.03.2015 by lupo

CREATE TABLE orders_formula
(
  id bigint NOT NULL,
  value numeric(19,5),
  instruction_id bigint,
  choseninstruction_id bigint,
  operator character varying(255),
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT orders_formula_pkey PRIMARY KEY (id),
  CONSTRAINT formula_instruction_fkey FOREIGN KEY (instruction_id)
      REFERENCES orders_instruction (id) DEFERRABLE,
  CONSTRAINT formula_choseninstruction_fkey FOREIGN KEY (choseninstruction_id)
      REFERENCES orders_instruction (id) DEFERRABLE
);

-- end
