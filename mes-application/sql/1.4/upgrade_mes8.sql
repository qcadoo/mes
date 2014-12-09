-- add locked attribute to PPS' daily plan
-- last touched 1.XII

ALTER TABLE productionpershift_dailyprogress ADD COLUMN locked boolean DEFAULT false;

-- end
