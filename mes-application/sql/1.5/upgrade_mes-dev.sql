
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


-- worker to change in order
-- last touched 27.01.2016 by kama

ALTER TABLE orders_order ADD COLUMN workertochange character varying(255);

-- end


