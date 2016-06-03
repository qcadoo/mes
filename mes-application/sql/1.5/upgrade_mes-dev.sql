-- last touched 24.05.2016 by kama

ALTER TABLE materialflowresources_resource ADD COLUMN username character varying(255);

-- end

-- wastes
-- last touched 03.06.2016 by kama

ALTER TABLE productioncounting_trackingoperationproductoutcomponent ADD COLUMN wastesquantity numeric(14,5);
ALTER TABLE orders_order ADD COLUMN wastesquantity numeric(12,5);

-- end