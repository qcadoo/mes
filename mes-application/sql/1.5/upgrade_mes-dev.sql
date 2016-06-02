-- last touched 24.05.2016 by kama

ALTER TABLE materialflowresources_resource ADD COLUMN username character varying(255);

-- end

-- rename fields in settings
﻿-- last touched 2.06.2016 by pako

UPDATE materialflowresources_documentpositionparametersitem SET name = 'productionDate' WHERE name = 'productiondate';
UPDATE materialflowresources_documentpositionparametersitem SET name = 'expirationDate' WHERE name = 'expirationdate';

-- end