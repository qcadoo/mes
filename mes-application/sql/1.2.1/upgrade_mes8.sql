-- Table: productioncounting_staffworktime
-- changed: 05.12.2013 [maku]

CREATE TABLE productioncounting_staffworktime
(
  id bigint NOT NULL,
  productionrecord_id bigint,
  worker_id bigint,
  labortime integer DEFAULT 0,
  CONSTRAINT productioncounting_staffworktime_pkey PRIMARY KEY (id),
  CONSTRAINT pc_swt_basic_staff_fkey FOREIGN KEY (worker_id)
      REFERENCES basic_staff (id) DEFERRABLE,
  CONSTRAINT pc_swt_pc_productionrecord_fkey FOREIGN KEY (productionrecord_id)
      REFERENCES productioncounting_productionrecord (id) DEFERRABLE
);

-- end


-- Table: basic_parameter
-- changed: 24.01.2014

ALTER TABLE basic_parameter ADD COLUMN generateproductionrecordnumberfromordernumber boolean;
ALTER TABLE basic_parameter ALTER COLUMN generateproductionrecordnumberfromordernumber SET DEFAULT false;

ALTER TABLE basic_parameter ADD COLUMN printoperationatfirstpageinworkplans boolean;
ALTER TABLE basic_parameter ALTER COLUMN printoperationatfirstpageinworkplans SET DEFAULT false;

-- end


-- Table: basic_parameter
-- changed: 28.01.2014

ALTER TABLE basic_parameter ADD COLUMN averagelaborhourlycostpb numeric(12,5);

ALTER TABLE basic_parameter ADD COLUMN calculatematerialcostsmodepb character varying(255);
ALTER TABLE basic_parameter ALTER COLUMN calculatematerialcostsmodepb SET DEFAULT '01nominal'::character varying;

ALTER TABLE basic_parameter ADD COLUMN additionaloverheadpb numeric(12,5);
ALTER TABLE basic_parameter ALTER COLUMN additionaloverheadpb SET DEFAULT 0::numeric;

ALTER TABLE basic_parameter ADD COLUMN calculateoperationcostsmodepb character varying(255);
ALTER TABLE basic_parameter ALTER COLUMN calculateoperationcostsmodepb SET DEFAULT '01hourly'::character varying;

ALTER TABLE basic_parameter ADD COLUMN materialcostmarginpb numeric(12,5);
ALTER TABLE basic_parameter ALTER COLUMN materialcostmarginpb SET DEFAULT 0::numeric;

ALTER TABLE basic_parameter ADD COLUMN includetpzpb boolean;
ALTER TABLE basic_parameter ALTER COLUMN includetpzpb SET DEFAULT true;

ALTER TABLE basic_parameter ADD COLUMN printoperationnormspb boolean;
ALTER TABLE basic_parameter ALTER COLUMN printoperationnormspb SET DEFAULT true;

ALTER TABLE basic_parameter ADD COLUMN printcostnormsofmaterialspb boolean;
ALTER TABLE basic_parameter ALTER COLUMN printcostnormsofmaterialspb SET DEFAULT true;

ALTER TABLE basic_parameter ADD COLUMN productioncostmarginpb numeric(12,5);
ALTER TABLE basic_parameter ALTER COLUMN productioncostmarginpb SET DEFAULT 0::numeric;

ALTER TABLE basic_parameter ADD COLUMN sourceofmaterialcostspb character varying(255);
ALTER TABLE basic_parameter ALTER COLUMN sourceofmaterialcostspb SET DEFAULT '01currentGlobalDefinitionsInProduct'::character varying;

ALTER TABLE basic_parameter ADD COLUMN averagemachinehourlycostpb numeric(12,5);

ALTER TABLE basic_parameter ADD COLUMN includeadditionaltimepb boolean;
ALTER TABLE basic_parameter ALTER COLUMN includeadditionaltimepb SET DEFAULT true;

-- end

