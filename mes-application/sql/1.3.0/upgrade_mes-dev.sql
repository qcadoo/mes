--  Add 'description' field to the warehouse document models.
-- last touched at 26.08.2014 by tola

ALTER TABLE materialflowresources_document ADD COLUMN description character varying(2048);

--  Update 'zipcode' field type in company model.
-- last touched at 26.08.2014 by tola

ALTER TABLE basic_company ALTER COLUMN zipcode TYPE character varying(255);
