-- table: qcadooview_item
-- last touched 24.08.2016 by lupo

UPDATE qcadooview_item SET category_id = (SELECT id FROM qcadooview_category WHERE name = 'basic' LIMIT 1) WHERE pluginidentifier = 'productionLines';

-- end
