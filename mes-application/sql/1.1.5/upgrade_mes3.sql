UPDATE 
	costnormsforoperation_calculationoperationcomponent
SET 
	isTjDivisible=false, 
	areProductQuantitiesDivisible=false;


UPDATE 
	technologies_technologyinstanceoperationcomponent 
SET 
	isTjDivisible=false, 
	areProductQuantitiesDivisible=false;

	
UPDATE 
	technologies_technologyoperationcomponent
SET 
	isTjDivisible=false, 
	areProductQuantitiesDivisible=false;

	
UPDATE 
	technologies_operation 
SET 
	isTjDivisible=false, 
	areProductQuantitiesDivisible=false;