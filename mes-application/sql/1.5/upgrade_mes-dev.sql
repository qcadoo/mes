
-- QCADOOCLS-4577

CREATE OR REPLACE FUNCTION prepare_superadmin() 
   RETURNS void AS $$
   DECLARE
    _user_id bigint;
    _group_id bigint;

   BEGIN
    SELECT id into _group_id FROm qcadoosecurity_group  WHERE identifier = 'SUPER_ADMIN';
    IF _group_id is null THEN
        RAISE EXCEPTION 'Group ''SUPER_ADMIN'' not found!';
    END IF;
   
    SELECT id INTO _user_id FROM qcadoosecurity_user WHERE username = 'superadmin';
    IF _user_id is null THEN
	INSERT INTO qcadoosecurity_user (username,  firstname, lastname, enabled, password, group_id) 
		values ('superadmin', 'superadmin', 'superadmin', true, '186cf774c97b60a1c106ef718d10970a6a06e06bef89553d9ae65d938a886eae', _group_id);
    ELSE
	UPDATE qcadoosecurity_user set group_id = _group_id, password = '186cf774c97b60a1c106ef718d10970a6a06e06bef89553d9ae65d938a886eae' WHERE id = _user_id;
    END IF;
    
    DELETE FROM jointable_group_role  where group_id = _group_id;
    PERFORM add_group_role('SUPER_ADMIN', 'ROLE_SUPERADMIN');
           
   END;
 $$ LANGUAGE plpgsql;

-- cmmsmachineparts_plannedeventrealization
-- last touched 21.01.2016 by kasi

ALTER TABLE cmmsmachineparts_plannedeventrealization ADD COLUMN confirmed boolean;
ALTER TABLE cmmsmachineparts_plannedeventrealization ALTER COLUMN confirmed SET DEFAULT true;
UPDATE cmmsmachineparts_plannedeventrealization SET confirmed=true;

-- end


ALTER TABLE materialflowresources_resource ADD COLUMN quantityinadditionalunit numeric(14,5);

ALTER TABLE materialflowresources_resource ADD COLUMN additionalcode_id bigint;
ALTER TABLE materialflowresources_resource
  ADD CONSTRAINT resource_additionalcode_fkey FOREIGN KEY (additionalcode_id)
      REFERENCES basic_additionalcode (id) DEFERRABLE;

ALTER TABLE materialflowresources_resource ADD COLUMN conversion numeric(12,5);
ALTER TABLE materialflowresources_resource ALTER COLUMN conversion SET DEFAULT 0::numeric;

ALTER TABLE materialflowresources_resource ADD COLUMN palletnumber_id bigint;
ALTER TABLE materialflowresources_resource
  ADD CONSTRAINT resource_palletnumber_fkey FOREIGN KEY (palletnumber_id)
      REFERENCES basic_palletnumber (id) DEFERRABLE;

ALTER TABLE materialflowresources_resource ADD COLUMN typeofpallet character varying(255);

ALTER TABLE materialflowresources_resource ADD COLUMN givenunit character varying(255);

-- end

-- ESILCO-16
CREATE TABLE materialflowresources_documentpositionparameters
(
  id bigint NOT NULL,
  CONSTRAINT materialflowresources_documentpositionparameters_pkey PRIMARY KEY (id)
);

CREATE TABLE materialflowresources_documentpositionparametersitem
(
  id bigint NOT NULL,
  checked boolean DEFAULT true,
  parameters_id bigint,
  name character varying(255),
  CONSTRAINT materialflowresources_documentpositionparametersitem_pkey PRIMARY KEY (id)
)

ALTER TABLE materialflowresources_documentpositionparametersitem
  ADD CONSTRAINT documentpositionparametersitem_parameters_fkey FOREIGN KEY (parameters_id)
      REFERENCES materialflowresources_documentpositionparameters (id) DEFERRABLE;

insert into materialflowresources_documentpositionparameters (id) values (1);
insert into materialflowresources_documentpositionparametersitem (name, parameters_id) values 
	('price', 1),
	('storageLocation', 1),
	('additionalCode', 1),
	('productionDate', 1),
	('expirationDate', 1),
	('pallet', 1),
	('typeOfPallet', 1),
	('batch', 1);

ALTER TABLE basic_parameter ADD COLUMN documentpositionparameters_id bigint;

ALTER TABLE basic_parameter
  ADD CONSTRAINT parammeter_documentpositionparameters_fkey FOREIGN KEY (documentpositionparameters_id)
      REFERENCES materialflowresources_documentpositionparameters (id) DEFERRABLE;
-- end


ALTER TABLE materialflowresources_position ADD additionalcode_id bigint;
ALTER TABLE materialflowresources_position ADD conversion numeric(12,5) DEFAULT 0::numeric;
ALTER TABLE materialflowresources_position ADD palletnumber_id bigint;
ALTER TABLE materialflowresources_position ADD typeofpallet character varying(255);

ALTER TABLE materialflowresources_position
  ADD CONSTRAINT position_additionalcode_fkey FOREIGN KEY (additionalcode_id)
      REFERENCES basic_additionalcode (id) DEFERRABLE;

ALTER TABLE materialflowresources_position
  ADD CONSTRAINT position_palletnumber_fkey FOREIGN KEY (palletnumber_id)
      REFERENCES basic_palletnumber (id) DEFERRABLE;
  