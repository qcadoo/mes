-- fix problem with unwanted rounding decimal values in document's position
-- last touched: 24.09.2014 by maku

ALTER TABLE materialflowresources_position ALTER COLUMN quantity TYPE NUMERIC(12,5);
ALTER TABLE materialflowresources_position ALTER COLUMN price TYPE NUMERIC(12,5);

-- end
