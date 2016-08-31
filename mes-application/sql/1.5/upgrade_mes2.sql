-- menu storage locations by kasi 08.10.2015

INSERT INTO qcadooview_view(pluginidentifier, name, view, entityversion) VALUES ('materialFlowResources', 'storageLocationList', 'storageLocationList', 0);
INSERT INTO qcadooview_item(pluginidentifier, name, active, category_id, view_id, succession, authrole, entityversion)
VALUES ('materialFlowResources', 'storageLocations', true, (SELECT id FROM qcadooview_category WHERE name = 'materialFlow' LIMIT 1), (
		SELECT id FROM qcadooview_view WHERE name = 'storageLocationList' LIMIT 1),(
		SELECT max(succession) + 1 FROM qcadooview_item WHERE category_id = (SELECT id FROM qcadooview_category WHERE name = 'materialFlow' LIMIT 1)), 'ROLE_MATERIAL_FLOW', 0);

-- end

-- table: qcadoomodel_dictionary
-- last touched: 18.10.2015 by lupo

UPDATE qcadoomodel_dictionary SET pluginidentifier = 'basic' WHERE name = 'typeOfPallet' AND pluginidentifier = 'goodFood';

-- end

-- update storage locations
-- last touched 19.10.2015 by kama

ALTER TABLE materialflowresources_position ADD COLUMN storagelocation_id bigint;
ALTER TABLE materialflowresources_position
  ADD CONSTRAINT position_storagelocation_fkey FOREIGN KEY (storagelocation_id)
      REFERENCES materialflowresources_storagelocation (id) DEFERRABLE;

ALTER TABLE materialflowresources_resource ADD COLUMN storagelocation_id bigint;
ALTER TABLE materialflowresources_resource
  ADD CONSTRAINT resource_storagelocation_fkey FOREIGN KEY (storagelocation_id)
      REFERENCES materialflowresources_storagelocation (id) DEFERRABLE;

ALTER TABLE materialflowresources_resourcecorrection ADD COLUMN oldstoragelocation_id bigint;
ALTER TABLE materialflowresources_resourcecorrection
  ADD CONSTRAINT resourcecorrection_oldstoragelocation_fkey FOREIGN KEY (oldstoragelocation_id)
      REFERENCES materialflowresources_storagelocation (id) DEFERRABLE;

ALTER TABLE materialflowresources_resourcecorrection ADD COLUMN newstoragelocation_id bigint;
ALTER TABLE materialflowresources_resourcecorrection
  ADD CONSTRAINT resourcecorrection_newstoragelocation_fkey FOREIGN KEY (newstoragelocation_id)
      REFERENCES materialflowresources_storagelocation (id) DEFERRABLE;

-- end

-- #GOODFOOD-960

ALTER TABLE cmmsmachineparts_plannedeventrealization DROP CONSTRAINT plannedeventrealization_action;

ALTER TABLE cmmsmachineparts_plannedeventrealization ADD
  CONSTRAINT plannedeventrealization_actionforplannedevent FOREIGN KEY (action_id)
      REFERENCES cmmsmachineparts_actionforplannedevent (id) DEFERRABLE;


UPDATE cmmsmachineparts_plannedeventrealization SET action_id = null;

ALTER TABLE cmmsmachineparts_actionforplannedevent ADD actionname character varying(255);


CREATE OR REPLACE FUNCTION updateEventActionName() RETURNS VOID AS $$ DECLARE row record;
BEGIN FOR row IN SELECT actionevent.id, action.name FROM cmmsmachineparts_actionforplannedevent actionevent JOIN cmmsmachineparts_action action ON (actionevent.action_id = action.id)
    LOOP
        UPDATE cmmsmachineparts_actionforplannedevent SET actionname = row.name WHERE id = row.id;
    END LOOP;
END;
$$ LANGUAGE 'plpgsql';

SELECT * FROM updateEventActionName();

DROP FUNCTION updateEventActionName();
