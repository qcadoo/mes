ALTER TABLE qcadooview_category ADD COLUMN authrole CHARACTER VARYING(255);
ALTER TABLE qcadooview_item ADD COLUMN authrole CHARACTER VARYING(255);

-- Table: technologies_technologyattachment
-- changed: 20.02.2014
CREATE TABLE technologies_technologyattachment
(
  id            BIGINT NOT NULL,
  technology_id BIGINT,
  attachment    CHARACTER VARYING(255),
  name          CHARACTER VARYING(255),
  size          NUMERIC(12, 5),
  ext           CHARACTER VARYING(255),
  CONSTRAINT technologies_technologyattachment_pkey PRIMARY KEY (id),
  CONSTRAINT technologies_technologyattachment_fkey FOREIGN KEY (technology_id)
  REFERENCES technologies_technology (id) DEFERRABLE
);
-- end

-- QCADOO-391 - mark first & last name as required
BEGIN;
UPDATE qcadoosecurity_user
SET firstname = username
WHERE firstname IS NULL OR length(trim(BOTH ' ' FROM firstname)) = 0;
UPDATE qcadoosecurity_user
SET lastname = username
WHERE lastname IS NULL OR length(trim(BOTH ' ' FROM lastname)) = 0;
END;
-- end
