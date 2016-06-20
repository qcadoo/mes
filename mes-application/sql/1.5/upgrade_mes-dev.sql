-- rename fields in settings
-- last touched 2.06.2016 by pako

UPDATE materialflowresources_documentpositionparametersitem SET name = 'productionDate' WHERE name = 'productiondate';
UPDATE materialflowresources_documentpositionparametersitem SET name = 'expirationDate' WHERE name = 'expirationdate';

-- end
