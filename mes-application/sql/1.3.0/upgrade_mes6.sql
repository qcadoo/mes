-- Add colun representing an effective date of a given order realization day.
-- last touched: 25.07.2014 by maku

ALTER TABLE productionpershift_progressforday ADD COLUMN actualDateOfDay DATE;

-- end
