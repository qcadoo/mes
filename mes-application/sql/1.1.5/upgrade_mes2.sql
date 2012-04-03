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
