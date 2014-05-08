-- Table: qcadooview_category
-- changed: ...

ALTER TABLE qcadooview_category ADD COLUMN authrole character varying(255);

-- end


-- Table: qcadooview_item
-- changed: ...

ALTER TABLE qcadooview_item ADD COLUMN authrole character varying(255);

-- end


-- Table: technologies_technologyattachment
-- changed: 20.02.2014

CREATE TABLE technologies_technologyattachment
(
  id bigint NOT NULL,
  technology_id bigint,
  attachment character varying(255),
  name character varying(255),
  size numeric(12,5),
  ext character varying(255),
  CONSTRAINT technologies_technologyattachment_pkey PRIMARY KEY (id),
  CONSTRAINT technologies_technologyattachment_fkey FOREIGN KEY (technology_id)
      REFERENCES technologies_technology (id) DEFERRABLE
);

-- end


-- Table: qcadoosecurity_user
-- changed: ...
-- QCADOO-391 - mark first & last name as required

BEGIN;

UPDATE qcadoosecurity_user SET firstname = username WHERE firstname IS null OR length(trim(both ' ' from firstname)) = 0;
UPDATE qcadoosecurity_user SET lastname = username WHERE lastname IS null OR length(trim(both ' ' from lastname)) = 0;

END;

-- end

