---- ONLY FOR LOCAL INSTANCE ----      

-- Table: workplans_...
-- changed: 13.01.2012

TRUNCATE workplans_parameterinputcolumn;
TRUNCATE workplans_parameteroutputcolumn;
TRUNCATE workplans_operationinputcolumn;
TRUNCATE workplans_operationinputcolumn;
TRUNCATE workplans_technologyoperationinputcolumn;
TRUNCATE workplans_technologyoperationinputcolumn;
TRUNCATE workplans_orderoperationinputcolumn;
TRUNCATE workplans_orderoperationinputcolumn;
TRUNCATE workplans_columnforinputproducts CASCADE;
TRUNCATE workplans_columnforoutputproducts CASCADE;

-- end


-- Table: workplans_columndefinition
-- changed: 13.01.2012

INSERT INTO workplans_columndefinition (id, identifier, name, description, pluginidentifier) VALUES (nextval('hibernate_sequence'), 'productName','workPlans.columnDefinition.name.value.productName', 'workPlans.columnDefinition.description.value.productName', 'workPlans');
			
INSERT INTO workplans_columndefinition (id, identifier, name, description, pluginidentifier) VALUES (nextval('hibernate_sequence'), 'plannedQuantity','workPlans.columnDefinition.name.value.plannedQuantity', 'workPlans.columnDefinition.description.value.plannedQuantity', 'workPlans');

-- end


-- Table: workplans_columnforinputproducts
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_columforinputproducts() RETURNS INTEGER AS 
'
	DECLARE
		columndefinition RECORD;
		
	BEGIN  
		FOR columndefinition IN SELECT * FROM workplans_columndefinition LOOP  
			INSERT INTO workplans_columnforinputproducts (id, columndefinition_id) 
			VALUES (
				nextval(''hibernate_sequence''), 
				columndefinition."id"
			);
		END LOOP;
		
		RETURN 1; 
	END;
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_columforinputproducts();

-- end


-- Table: workplans_columnforinputproducts
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_columforoutputproducts() RETURNS INTEGER AS 
'
	DECLARE
		columndefinition RECORD;
		
	BEGIN  
		FOR columndefinition IN SELECT * FROM workplans_columndefinition LOOP  
			INSERT INTO workplans_columnforoutputproducts (id, columndefinition_id) 
			VALUES (
				nextval(''hibernate_sequence''), 
				columndefinition."id"
			);
		END LOOP;
		
		RETURN 1; 
	END;
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_columforoutputproducts();

-- end

      
-- Table: workplans_parameterinputcolumn
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_parameterinputcolumn() RETURNS INTEGER AS 
'
	DECLARE
		priority INTEGER;
		parameter RECORD;
		columnforinputproducts RECORD;  
	
	BEGIN  
		FOR parameter IN SELECT id FROM basic_parameter LOOP
			priority := 1;

			FOR columnforinputproducts IN SELECT id FROM workplans_columnforinputproducts LOOP  
				INSERT INTO workplans_parameterinputcolumn (id, parameter_id, columnforinputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					parameter."id",
					columnforinputproducts."id",
					priority
				);

				priority := priority + 1;
			END LOOP;

		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_parameterinputcolumn();

-- end


-- Table: workplans_parameteroutputcolumn
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_parameteroutputcolumn() RETURNS INTEGER AS 
'
	DECLARE
		priority INTEGER;
		parameter RECORD;
		columnforoutputproducts RECORD;  
	
	BEGIN  
		FOR parameter IN SELECT id FROM basic_parameter LOOP
			priority := 1;
			FOR columnforoutputproducts IN SELECT id FROM workplans_columnforoutputproducts LOOP  
				INSERT INTO workplans_parameteroutputcolumn (id, parameter_id, columnforoutputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					parameter."id",
					columnforoutputproducts."id",
					priority
				);

				priority := priority + 1;
			END LOOP;
		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_parameteroutputcolumn();

-- end


-- Table: workplans_operationinputcolumn
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_operationinputcolumn() RETURNS INTEGER AS 
'
	DECLARE
		priority INTEGER;
		operation RECORD;
		columnforinputproducts RECORD;  
	
	BEGIN  
		FOR operation IN SELECT id FROM technologies_operation LOOP
			priority := 1;

			FOR columnforinputproducts IN SELECT id FROM workplans_columnforinputproducts LOOP  
				INSERT INTO workplans_operationinputcolumn (id, operation_id, columnforinputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					operation."id",
					columnforinputproducts."id",
					priority
				);

				priority := priority + 1;
			END LOOP;

		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_operationinputcolumn();

-- end


-- Table: workplans_operationoutputcolumn
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_operationoutputcolumn() RETURNS INTEGER AS 
'
	DECLARE
		priority INTEGER;
		operation RECORD;
		columnforoutputproducts RECORD;  
	
	BEGIN  
		FOR operation IN SELECT id FROM technologies_operation LOOP
			priority := 1;
			FOR columnforoutputproducts IN SELECT id FROM workplans_columnforoutputproducts LOOP  
				INSERT INTO workplans_operationoutputcolumn (id, operation_id, columnforoutputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					operation."id",
					columnforoutputproducts."id",
					priority
				);

				priority := priority + 1;
			END LOOP;
		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_operationoutputcolumn();

-- end


-- Table: workplans_technologyoperationinputcolumn
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_technologyoperationinputcolumn() RETURNS INTEGER AS 
'
	DECLARE
		priority INTEGER;
		technologyoperationcomponent RECORD;
		columnforinputproducts RECORD;  
	
	BEGIN  
		FOR technologyoperationcomponent IN SELECT id FROM technologies_technologyoperationcomponent LOOP
			priority := 1;

			FOR columnforinputproducts IN SELECT id FROM workplans_columnforinputproducts LOOP  
				INSERT INTO workplans_technologyoperationinputcolumn (id, technologyoperationcomponent_id, columnforinputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					technologyoperationcomponent."id",
					columnforinputproducts."id",
					priority
				);

				priority := priority + 1;
			END LOOP;

		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_technologyoperationinputcolumn();

-- end


-- Table: workplans_technologyoperationoutputcolumn
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_technologyoperationoutputcolumn() RETURNS INTEGER AS 
'
	DECLARE
		priority INTEGER;
		technologyoperationcomponent RECORD;
		columnforoutputproducts RECORD;  
	
	BEGIN  
		FOR technologyoperationcomponent IN SELECT id FROM technologies_technologyoperationcomponent LOOP
			priority := 1;
			FOR columnforoutputproducts IN SELECT id FROM workplans_columnforoutputproducts LOOP  
				INSERT INTO workplans_technologyoperationoutputcolumn (id, technologyoperationcomponent_id, columnforoutputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					technologyoperationcomponent."id",
					columnforoutputproducts."id",
					priority
				);

				priority := priority + 1;
			END LOOP;
		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_technologyoperationoutputcolumn();

-- end


-- Table: workplans_orderoperationinputcolumn
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_orderoperationinputcolumn() RETURNS INTEGER AS 
'
	DECLARE
		priority INTEGER;
		orderoperationcomponent RECORD;
		columnforinputproducts RECORD;  
	
	BEGIN  
		FOR orderoperationcomponent IN SELECT id FROM productionscheduling_orderoperationcomponent LOOP
			priority := 1;

			FOR columnforinputproducts IN SELECT id FROM workplans_columnforinputproducts LOOP  
				INSERT INTO workplans_orderoperationinputcolumn (id, orderoperationcomponent_id, columnforinputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					orderoperationcomponent."id",
					columnforinputproducts."id",
					priority
				);

				priority := priority + 1;
			END LOOP;

		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_orderoperationinputcolumn();

-- end


-- Table: workplans_operationoutputcolumn
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_orderoperationoutputcolumn() RETURNS INTEGER AS 
'
	DECLARE
		priority INTEGER;
		orderoperationcomponent RECORD;
		columnforoutputproducts RECORD;  
	
	BEGIN  
		FOR orderoperationcomponent IN SELECT id FROM productionscheduling_orderoperationcomponent LOOP
			priority := 1;
			FOR columnforoutputproducts IN SELECT id FROM workplans_columnforoutputproducts LOOP  
				INSERT INTO workplans_orderoperationoutputcolumn (id, orderoperationcomponent_id, columnforoutputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					orderoperationcomponent."id",
					columnforoutputproducts."id",
					priority
				);

				priority := priority + 1;
			END LOOP;
		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_orderoperationoutputcolumn();

-- end

---- END ----