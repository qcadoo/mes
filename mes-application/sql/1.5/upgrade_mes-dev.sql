-- new ordered product fields
﻿-- last touched 1.06.2016 by pako

ALTER TABLE deliveries_orderedproduct ADD COLUMN additionalquantity numeric(12,5);
ALTER TABLE deliveries_orderedproduct ADD COLUMN conversion numeric(12,5);
ALTER TABLE deliveries_orderedproduct ADD COLUMN additionalcode_id bigint;
ALTER TABLE deliveries_orderedproduct
  ADD CONSTRAINT orderedproduct_additionalcode_fkey FOREIGN KEY (additionalcode_id)
      REFERENCES basic_additionalcode (id) DEFERRABLE;
UPDATE deliveries_orderedproduct SET additionalquantity = orderedquantity, conversion = 1;

-- end

-- new delivered product fields
﻿-- last touched 9.06.2016 by pako

ALTER TABLE deliveries_deliveredproduct ADD COLUMN palletnumber_id bigint;
ALTER TABLE deliveries_deliveredproduct ADD COLUMN pallettype character varying(255);
ALTER TABLE deliveries_deliveredproduct ADD COLUMN storagelocation_id bigint;
ALTER TABLE deliveries_deliveredproduct ADD COLUMN additionalcode_id bigint;

ALTER TABLE deliveries_deliveredproduct
  ADD CONSTRAINT deliveredproduct_additionalcode_fkey FOREIGN KEY (additionalcode_id)
      REFERENCES basic_additionalcode (id) DEFERRABLE;
ALTER TABLE deliveries_deliveredproduct
  ADD CONSTRAINT deliveredproduct_palletnumber_fkey FOREIGN KEY (palletnumber_id)
      REFERENCES basic_palletnumber (id) DEFERRABLE;
ALTER TABLE deliveries_deliveredproduct
  ADD CONSTRAINT deliveredproduct_storagelocation_fkey FOREIGN KEY (storagelocation_id)
      REFERENCES materialflowresources_storagelocation (id) DEFERRABLE;

-- end