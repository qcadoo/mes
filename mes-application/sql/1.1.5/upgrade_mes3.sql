ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN areproductquantitiesdivisible SET DEFAULT false;
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN istjdivisible SET DEFAULT false;
UPDATE 
	costnormsforoperation_calculationoperationcomponent
SET 
	isTjDivisible=false, 
	areProductQuantitiesDivisible=false;

ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN areproductquantitiesdivisible SET DEFAULT false;
ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN istjdivisible SET DEFAULT false;
UPDATE 
	technologies_technologyinstanceoperationcomponent 
SET 
	isTjDivisible=false, 
	areProductQuantitiesDivisible=false;

ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN areproductquantitiesdivisible SET DEFAULT false;
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN istjdivisible SET DEFAULT false;
UPDATE 
	technologies_technologyoperationcomponent
SET 
	isTjDivisible=false, 
	areProductQuantitiesDivisible=false;

ALTER TABLE technologies_operation ALTER COLUMN areproductquantitiesdivisible SET DEFAULT false;
ALTER TABLE technologies_operation ALTER COLUMN istjdivisible SET DEFAULT false;
UPDATE 
	technologies_operation 
SET 
	isTjDivisible=false, 
	areProductQuantitiesDivisible=false;