-- menu storage locations by kasi 08.10.2015

INSERT INTO qcadooview_view(pluginidentifier, name, view, entityversion) VALUES ('materialFlowResources', 'storageLocationList', 'storageLocations', 0);
INSERT INTO qcadooview_item(pluginidentifier, name, active, category_id, view_id, succession, authrole, entityversion)
VALUES ('materialFlowResources', 'storageLocations', true, (SELECT id FROM qcadooview_category WHERE name = 'materialFlow' LIMIT 1), (
		SELECT id FROM qcadooview_view WHERE name = 'storageLocations' LIMIT 1),(
		SELECT max(succession) + 1 FROM qcadooview_item WHERE category_id = (SELECT id FROM qcadooview_category WHERE name = 'administration' LIMIT 1)),'ROLE_MATERIAL_FLOW',0);

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

// #GOODFOOD-960
alter table cmmsmachineparts_plannedeventrealization drop constraint plannedeventrealization_action;

alter table cmmsmachineparts_plannedeventrealization add
  CONSTRAINT plannedeventrealization_actionforplannedevent FOREIGN KEY (action_id)
      REFERENCES cmmsmachineparts_actionforplannedevent (id) DEFERRABLE;


update cmmsmachineparts_plannedeventrealization set action_id=null;

alter table cmmsmachineparts_actionforplannedevent add actionname character varying(255);


CREATE OR REPLACE FUNCTION updateEventActionName() RETURNS VOID AS $$ DECLARE row record;
BEGIN FOR row IN select actionevent.id, action.name from cmmsmachineparts_actionforplannedevent actionevent join cmmsmachineparts_action action on (actionevent.action_id = action.id)
    LOOP
        update cmmsmachineparts_actionforplannedevent set actionname = row.name where id = row.id;
    END LOOP;
END;
$$ LANGUAGE 'plpgsql';

SELECT * FROM updateEventActionName();
drop function updateEventActionName();


