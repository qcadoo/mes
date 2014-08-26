--  Add 'description' field to the warehouse document models.
-- last touched at 26.08.2014 by tola

ALTER TABLE materialflowresources_document ADD COLUMN description character varying(2048);