-- assignment to shift changes
-- last touched 11.02.2016 by pako

CREATE TABLE assignmenttoshift_multiassignmenttoshift
(
  id bigint NOT NULL,
  productionline_id bigint,
  occupationtype character varying(255),
  occupationtypename character varying(255),
  occupationtypeenum character varying(255),
  masterorder_id bigint,
  assignmenttoshift_id bigint,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT assignmenttoshift_multiassignmenttoshift_pkey PRIMARY KEY (id),
  CONSTRAINT multiassignmenttoshift_masterorder_fkey FOREIGN KEY (masterorder_id)
      REFERENCES masterorders_masterorder (id) DEFERRABLE,
  CONSTRAINT multiassignmenttoshift_productionline_fkey FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline (id) DEFERRABLE,
  CONSTRAINT multiassignmenttoshift_assignmenttoshift_fkey FOREIGN KEY (assignmenttoshift_id)
      REFERENCES assignmenttoshift_assignmenttoshift (id) DEFERRABLE
);

CREATE TABLE jointable_multiassignmenttoshift_staff
(
  multiassignmenttoshift_id bigint NOT NULL,
  staff_id bigint NOT NULL,
  CONSTRAINT jointable_multiassignmenttoshift_staff_pkey PRIMARY KEY (multiassignmenttoshift_id, staff_id),
  CONSTRAINT staff_multiassignmenttoshift_fkey FOREIGN KEY (staff_id)
      REFERENCES basic_staff (id) DEFERRABLE,
  CONSTRAINT assignmenttoshift_multiassignmenttoshift_staff_fkey FOREIGN KEY (multiassignmenttoshift_id)
      REFERENCES assignmenttoshift_multiassignmenttoshift (id) DEFERRABLE
);

ALTER TABLE assignmenttoshift_staffassignmenttoshift ADD COLUMN description character varying(255);

-- end
