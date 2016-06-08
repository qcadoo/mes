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
