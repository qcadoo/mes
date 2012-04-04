-- Table: technologies_technologygroup
-- changed: 03.04.2012

CREATE TABLE technologies_technologygroup
(
  id bigint NOT NULL,
  "number" character varying(255),
  name character varying(2048),
  active boolean DEFAULT true,
  CONSTRAINT technologies_technologygroup_pkey PRIMARY KEY (id )
);

-- end


-- Table: technologies_technology
-- changed: 03.04.2012

ALTER TABLE technologies_technology ADD COLUMN technologygroup_id bigint;
ALTER TABLE technologies_technology
  ADD CONSTRAINT technologies_technology_technologygroup_fkey FOREIGN KEY (technologygroup_id)
      REFERENCES technologies_technologygroup (id) DEFERRABLE;
      
-- end

-- Table: technologies_operation
-- changed: 03.04.2012

ALTER TABLE technologies_operation ADD COLUMN areproductquantitiesdivisible boolean DEFAULT true;
ALTER TABLE technologies_operation ADD COLUMN istjdivisible boolean DEFAULT true;

-- end

-- Table: technologies_technologyoperationcomponent
-- changed: 03.04.2012

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN areproductquantitiesdivisible boolean DEFAULT true;
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN istjdivisible boolean DEFAULT true;

-- end

CREATE TABLE productionlines_productionline
(
  id bigint NOT NULL,
  "number" character varying(255),
  "name" character varying(2048),
  division_id bigint,
  place character varying(255),
  description character varying(2048),
  supportsalltechnologies boolean DEFAULT true,
  documentation character varying(255),
  active boolean DEFAULT true,
  CONSTRAINT productionlines_productionline_pkey PRIMARY KEY (id),
  CONSTRAINT productionlines_division_fkey FOREIGN KEY (division_id)
      REFERENCES basic_division (id) DEFERRABLE;
)

CREATE TABLE jointable_productionline_technology
(
  technology_id bigint NOT NULL,
  productionline_id bigint NOT NULL,
  CONSTRAINT jointable_productionline_technology_pkey PRIMARY KEY (productionline_id, technology_id),
  CONSTRAINT jointable_pl_tech_productionline_fkey FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline (id) DEFERRABLE;
  CONSTRAINT jointable_pl_tech_technology_fkey FOREIGN KEY (technology_id)
      REFERENCES technologies_technology (id) DEFERRABLE;
)

REATE TABLE jointable_productionline_technologygroup
(
  technologygroup_id bigint NOT NULL,
  productionline_id bigint NOT NULL,
  CONSTRAINT jointable_productionline_technologygroup_pkey PRIMARY KEY (productionline_id, technologygroup_id),
  CONSTRAINT jointable_pl_techgroup_productionline_fkey FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline (id) DEFERRABLE;
  CONSTRAINT jointable_pl_techgroup_technologygroup_fkey FOREIGN KEY (technologygroup_id)
      REFERENCES technologies_technologygroup (id) DEFERRABLE;
)

-- Table: basic_product
-- changed: 04.04.2012

ALTER TABLE basic_product ADD COLUMN technologygroup_id bigint;
ALTER TABLE basic_product
  ADD CONSTRAINT basic_product_technologygroup_fkey FOREIGN KEY (technologygroup_id)
      REFERENCES technologies_technologygroup (id) DEFERRABLE;
      
-- end
