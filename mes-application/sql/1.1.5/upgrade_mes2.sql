-- Table: costnormsformaterials_calculationoperationcomponent
-- changed: 3.04.2012
ALTER TABLE costnormsforoperation_calculationoperationcomponent RENAME TO costnormsformaterials_calculationoperationcomponent;

--end

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

-- Table: technologies_technologyoperationcomponent
-- changed: 03.04.2012
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN effectivedateto timestamp without time zone;
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN effectivedatefrom timestamp without time zone;
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN effectiveoperationrealizationtime integer;
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN operationoffset integer;
-- end