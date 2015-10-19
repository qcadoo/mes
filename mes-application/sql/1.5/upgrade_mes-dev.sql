-- menu storage locations by kasi 08.10.2015

INSERT INTO qcadooview_view(pluginidentifier, name, view, entityversion) VALUES ('materialFlowResources', 'storageLocationList', 'storageLocationList', 0);
INSERT INTO qcadooview_item(pluginidentifier, name, active, category_id, view_id, succession, authrole, entityversion)
VALUES ('materialFlowResources', 'storageLocationList', true, (SELECT id FROM qcadooview_category WHERE name = 'materialFlow' LIMIT 1), (
		SELECT id FROM qcadooview_view WHERE name = 'storageLocationList' LIMIT 1),(
		SELECT max(succession) + 1 FROM qcadooview_item WHERE category_id = (SELECT id FROM qcadooview_category WHERE name = 'administration' LIMIT 1)),'ROLE_MATERIAL_FLOW',0);

-- end


-- table: qcadoomodel_dictionary
-- last touched: 18.10.2015 by lupo

UPDATE qcadoomodel_dictionary SET pluginidentifier = 'basic' WHERE name = 'typeOfPallet' AND pluginidentifier = 'goodFood';

-- end
