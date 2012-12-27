--- Table: productioncounting_productionbalance
--- change: 21.12.2012
UPDATE productioncounting_productionbalance SET calculatematerialcostsmode='04averageOfferCost' where calculatematerialcostsmode='04costForOrder' ;
---end


--- Table: costcalculation_costcalculation
--- change: 21.12.2012
UPDATE costcalculation_costcalculation SET calculatematerialcostsmode='04averageOfferCost' where calculatematerialcostsmode='04costForOrder' ;
---end
