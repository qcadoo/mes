-- Uodated materials in MBR, added brackets to formula
-- last touched 19.03.2015 by kama

ALTER TABLE orders_material DROP CONSTRAINT orders_material_orders_instruction;
ALTER TABLE orders_material DROP COLUMN instruction_id;

CREATE TABLE orders_materialforinstruction
(
  id bigint NOT NULL,
  material_id bigint,
  quantity numeric(12,5),
  instruction_id bigint,
  CONSTRAINT orders_materialforinstruction_pkey PRIMARY KEY (id),
  CONSTRAINT materialforinstruction_instruction FOREIGN KEY (instruction_id)
      REFERENCES orders_instruction (id) DEFERRABLE,
  CONSTRAINT materialforinstruction_material FOREIGN KEY (material_id)
      REFERENCES orders_material (id) DEFERRABLE
);

ALTER TABLE orders_formula ADD COLUMN prefix character varying(255);
ALTER TABLE orders_formula ADD COLUMN suffix character varying(255);

-- end