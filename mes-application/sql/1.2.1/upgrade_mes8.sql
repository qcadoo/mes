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
)

-- end

