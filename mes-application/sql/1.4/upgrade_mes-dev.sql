-- Added field to PPS
-- last touched 01.06.2015 by kama

ALTER TABLE productionpershift_productionpershift ADD COLUMN orderfinishdate timestamp without time zone;

-- end