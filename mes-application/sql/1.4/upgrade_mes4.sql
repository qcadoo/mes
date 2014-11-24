-- create role and group models (ACL)
-- last touched 16.10.2014 by maku

CREATE TABLE qcadoosecurity_role
(
  id bigint NOT NULL,
  identifier character varying(255),
  description text,
  CONSTRAINT qcadoosecurity_role_pkey PRIMARY KEY (id)
);

CREATE TABLE qcadoosecurity_group
(
  id bigint NOT NULL,
  name character varying(255),
  description text,
  identifier character varying(255),
  CONSTRAINT qcadoosecurity_group_pkey PRIMARY KEY (id)
);

CREATE TABLE jointable_group_role
(
  group_id bigint NOT NULL,
  role_id bigint NOT NULL,
  CONSTRAINT jointable_group_role_pkey PRIMARY KEY (role_id, group_id),
  CONSTRAINT group_role_role_fkey FOREIGN KEY (role_id)
      REFERENCES qcadoosecurity_role (id) DEFERRABLE,
  CONSTRAINT group_role_group_fkey FOREIGN KEY (group_id)
      REFERENCES qcadoosecurity_group (id) DEFERRABLE
);

ALTER TABLE qcadoosecurity_user ADD COLUMN group_id bigint;

ALTER TABLE qcadoosecurity_user ADD CONSTRAINT user_group_fkey FOREIGN KEY (group_id)
      REFERENCES qcadoosecurity_group (id) DEFERRABLE;

-- end

-- make dictionary item (de)activable
-- las touched 16.10.2014 by maku

ALTER TABLE qcadoomodel_dictionaryitem ADD COLUMN active BOOLEAN DEFAULT true;
UPDATE qcadoomodel_dictionaryitem SET active = true where active is null;

-- end
