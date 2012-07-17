-- Table: ...
-- changed: ...

ALTER TABLE basic_substituteComponent ALTER COLUMN quantity TYPE numeric(9,5);

ALTER TABLE basicproductioncounting_basicproductioncounting ALTER COLUMN plannedquantity TYPE numeric(12,5);
ALTER TABLE basicproductioncounting_basicproductioncounting ALTER COLUMN usedquantity TYPE numeric(12,5);
ALTER TABLE basicproductioncounting_basicproductioncounting ALTER COLUMN usedquantity TYPE numeric(12,5);

ALTER TABLE costcalculation_costcalculation ALTER COLUMN quantity TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totalmaterialcosts TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totalmachinehourlycosts TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totalpieceworkcosts TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totallaborhourlycosts TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totaltechnicalproductioncosts TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN productioncostmargin TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN productioncostmarginvalue TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN materialcostmargin TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN materialcostmarginvalue TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN additionaloverhead TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN additionaloverheadvalue TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totaloverhead TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totalcosts TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totalcostperunit TYPE numeric(12,5);

ALTER TABLE costnormsformaterials_technologyinstoperproductincomp ALTER COLUMN costfornumber TYPE numeric(12,5);
ALTER TABLE costnormsformaterials_technologyinstoperproductincomp ALTER COLUMN nominalcost TYPE numeric(12,5);
ALTER TABLE costnormsformaterials_technologyinstoperproductincomp ALTER COLUMN lastpurchasecost TYPE numeric(12,5);
ALTER TABLE costnormsformaterials_technologyinstoperproductincomp ALTER COLUMN averagecost TYPE numeric(12,5);
ALTER TABLE costnormsformaterials_technologyinstoperproductincomp ALTER COLUMN costfororder TYPE numeric(12,5);

ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN machineutilization TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN laborutilization TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN productioninonecycle TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN countmachine TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN pieces TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN operationcost TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN operationmargincost TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN totaloperationcost TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN pieceworkcost TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN laborhourlycost TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN machinehourlycost TYPE numeric(12,5);

ALTER TABLE materialflow_stockcorrection ALTER COLUMN found TYPE numeric(12,5);
ALTER TABLE materialflow_transfer ALTER COLUMN quantity TYPE numeric(12,5);

ALTER TABLE orders_order ALTER COLUMN plannedquantity TYPE numeric(10,5);
ALTER TABLE orders_order ALTER COLUMN donequantity TYPE numeric(10,5);

ALTER TABLE productioncounting_balanceoperationproductincomponent ALTER COLUMN plannedquantity TYPE numeric(10,5);
ALTER TABLE productioncounting_balanceoperationproductincomponent ALTER COLUMN usedquantity TYPE numeric(10,5);
ALTER TABLE productioncounting_balanceoperationproductincomponent ALTER COLUMN balance TYPE numeric(12,5);

ALTER TABLE productioncounting_balanceoperationproductoutcomponent ALTER COLUMN plannedquantity TYPE numeric(10,5);
ALTER TABLE productioncounting_balanceoperationproductoutcomponent ALTER COLUMN usedquantity TYPE numeric(10,5);
ALTER TABLE productioncounting_balanceoperationproductoutcomponent ALTER COLUMN balance TYPE numeric(12,5);

ALTER TABLE productioncounting_operationpieceworkcomponent ALTER COLUMN plannedcycles TYPE numeric(12,5);
ALTER TABLE productioncounting_operationpieceworkcomponent ALTER COLUMN cycles TYPE numeric(12,5);
ALTER TABLE productioncounting_operationpieceworkcomponent ALTER COLUMN cyclesbalance TYPE numeric(12,5);

ALTER TABLE productioncounting_productionrecord ALTER COLUMN executedoperationcycles TYPE numeric(12,5);

ALTER TABLE productioncounting_recordoperationproductincomponent ALTER COLUMN plannedquantity TYPE numeric(10,5);
ALTER TABLE productioncounting_recordoperationproductincomponent ALTER COLUMN usedquantity TYPE numeric(10,5);
ALTER TABLE productioncounting_recordoperationproductincomponent ALTER COLUMN balance TYPE numeric(12,5);

ALTER TABLE productioncounting_recordoperationproductoutcomponent ALTER COLUMN plannedquantity TYPE numeric(10,5);
ALTER TABLE productioncounting_recordoperationproductoutcomponent ALTER COLUMN usedquantity TYPE numeric(10,5);
ALTER TABLE productioncounting_recordoperationproductoutcomponent ALTER COLUMN balance TYPE numeric(12,5);

ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN plannedmachinecosts TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN machinecosts TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN machinecostsbalance TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN plannedlaborcosts TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN laborcosts TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN laborcostsbalance TYPE numeric(12,5);

ALTER TABLE productioncountingwithcosts_operationpieceworkcostcomponent ALTER COLUMN plannedcyclescosts TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_operationpieceworkcostcomponent ALTER COLUMN cyclescosts TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_operationpieceworkcostcomponent ALTER COLUMN cyclescostsbalance TYPE numeric(12,5);

ALTER TABLE productioncountingwithcosts_technologyinstoperproductincomp ALTER COLUMN plannedcost TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_technologyinstoperproductincomp ALTER COLUMN registeredcost TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_technologyinstoperproductincomp ALTER COLUMN balance TYPE numeric(12,5);

ALTER TABLE productionpershift_dailyprogress ALTER COLUMN quantity TYPE numeric(12,5);

ALTER TABLE qualitycontrols_qualitycontrol ALTER COLUMN controlledquantity TYPE numeric(12,5);
ALTER TABLE qualitycontrols_qualitycontrol ALTER COLUMN takenforcontrolquantity TYPE numeric(12,5);
ALTER TABLE qualitycontrols_qualitycontrol ALTER COLUMN rejectedquantity TYPE numeric(12,5);
ALTER TABLE qualitycontrols_qualitycontrol ALTER COLUMN accepteddefectsquantity TYPE numeric(12,5);

ALTER TABLE technologies_operationproductincomponent ALTER COLUMN quantity TYPE numeric(10,5);

ALTER TABLE technologies_operationproductoutcomponent ALTER COLUMN quantity TYPE numeric(10,5);

-- Extension model
ALTER TABLE technologies_operation ALTER COLUMN pieceworkcost TYPE numeric(12,5);
ALTER TABLE technologies_operation ALTER COLUMN laborhourlycost TYPE numeric(12,5);
ALTER TABLE technologies_operation ALTER COLUMN machinehourlycost TYPE numeric(12,5);

ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN pieceworkcost TYPE numeric(12,5);
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN laborhourlycost TYPE numeric(12,5);
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN machinehourlycost TYPE numeric(12,5);

ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN pieceworkcost TYPE numeric(12,5);
ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN laborhourlycost TYPE numeric(12,5);
ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN machinehourlycost TYPE numeric(12,5);

ALTER TABLE basic_product ALTER COLUMN costfornumber TYPE numeric(12,5);
ALTER TABLE basic_product ALTER COLUMN nominalcost TYPE numeric(12,5);
ALTER TABLE basic_product ALTER COLUMN lastpurchasecost TYPE numeric(12,5);
ALTER TABLE basic_product ALTER COLUMN averagecost TYPE numeric(12,5);

ALTER TABLE technologies_technology ALTER COLUMN minimalquantity TYPE numeric(10,5);

ALTER TABLE productioncounting_productionbalance ALTER COLUMN averagemachinehourlycost TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN averagelaborhourlycost TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN productioncostmargin TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN materialcostmargin TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN additionaloverhead TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN plannedcomponentscosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN componentscosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN componentscostsbalance TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN plannedmachinecosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN machinecosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN machinecostsbalance TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN plannedlaborcosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN laborcosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN laborcostsbalance TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN plannedcyclescosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN cyclescosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN cyclescostsbalance TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN registeredtotaltechnicalproductioncosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN registeredtotaltechnicalproductioncostperunit TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN totaltechnicalproductioncosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN totaltechnicalproductioncostperunit TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN balancetechnicalproductioncosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN balancetechnicalproductioncostperunit TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN productioncostmarginvalue TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN materialcostmarginvalue TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN additionaloverheadvalue TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN totaloverhead TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN totalcosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN totalcostperunit TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN quantity TYPE numeric(12,5);

-- end


-- Table: productioncounting_productionbalance --> ADD COLUMN active boolean
-- changed: 9.07.2012

ALTER TABLE productioncounting_productionbalance ADD COLUMN active boolean;
ALTER TABLE productioncounting_productionbalance ALTER COLUMN active SET DEFAULT true;


ALTER TABLE technologies_technology ALTER COLUMN unitsamplingnr TYPE numeric(12,5);

ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN productioninonecycle TYPE numeric(12,5);
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN countmachine TYPE numeric(12,5);
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN machineutilization TYPE numeric(8,5);
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN laborutilization TYPE numeric(8,5);

ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN productioninonecycle TYPE numeric(12,5);
ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN countmachine TYPE numeric(12,5);
ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN machineutilization TYPE numeric(8,5);
ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN laborutilization TYPE numeric(8,5);

ALTER TABLE technologies_operation ALTER COLUMN productioninonecycle TYPE numeric(12,5);
ALTER TABLE technologies_operation ALTER COLUMN countmachine TYPE numeric(12,5);
ALTER TABLE technologies_operation ALTER COLUMN machineutilization TYPE numeric(8,5);
ALTER TABLE technologies_operation ALTER COLUMN laborutilization TYPE numeric(8,5);

-- end


-- Table: advancedgenealogy_batchlogging --> advancedgenealogy_batchstatechange
-- changed: 28.06.2012

ALTER TABLE advancedgenealogy_batchlogging RENAME TO advancedgenealogy_batchstatechange;

ALTER TABLE advancedgenealogy_batchstatechange RENAME COLUMN previousstate TO sourcestate;
ALTER TABLE advancedgenealogy_batchstatechange RENAME COLUMN currentstate TO targetstate;

ALTER TABLE advancedgenealogy_batchstatechange ADD COLUMN status character varying(255);
ALTER TABLE advancedgenealogy_batchstatechange ALTER COLUMN status SET DEFAULT '01inProgress'::character varying;

ALTER TABLE advancedgenealogy_batchstatechange ADD COLUMN phase integer;

ALTER TABLE advancedgenealogy_batchstatechange ADD COLUMN shift_id bigint;

ALTER TABLE advancedgenealogy_batchstatechange 
	ADD CONSTRAINT basic_shift_fkey 
		FOREIGN KEY (shift_id)
    	REFERENCES basic_shift (id) 
    	DEFERRABLE;

UPDATE advancedgenealogy_batchstatechange SET status = '03successful';

UPDATE advancedgenealogy_batchstatechange SET phase = 4;

-- end


-- Table: advancedgenealogy_trackingrecordlogging --> advancedgenealogy_trackingrecordstatechange
-- changed: 28.06.2012

ALTER TABLE advancedgenealogy_trackingrecordlogging RENAME TO advancedgenealogy_trackingrecordstatechange;

ALTER TABLE advancedgenealogy_trackingrecordstatechange RENAME COLUMN previousstate TO sourcestate;
ALTER TABLE advancedgenealogy_trackingrecordstatechange RENAME COLUMN currentstate TO targetstate;

ALTER TABLE advancedgenealogy_trackingrecordstatechange ADD COLUMN status character varying(255);
ALTER TABLE advancedgenealogy_trackingrecordstatechange ALTER COLUMN status SET DEFAULT '01inProgress'::character varying;

ALTER TABLE advancedgenealogy_trackingrecordstatechange ADD COLUMN phase integer;

ALTER TABLE advancedgenealogy_trackingrecordstatechange ADD COLUMN shift_id bigint;

ALTER TABLE advancedgenealogy_trackingrecordstatechange 
	ADD CONSTRAINT basic_shift_fkey 
		FOREIGN KEY (shift_id)
    	REFERENCES basic_shift (id) 
    	DEFERRABLE;

UPDATE advancedgenealogy_trackingrecordstatechange SET status = '03successful';

UPDATE advancedgenealogy_trackingrecordstatechange SET phase = 4;

-- end


-- Table: states_message
-- changed: 28.06.2012

ALTER TABLE states_message 
	ADD COLUMN batchstatechange_id bigint;

ALTER TABLE states_message 
	ADD CONSTRAINT message_batchstatechange_fkey 
		FOREIGN KEY (batchstatechange_id)
		REFERENCES advancedgenealogy_batchstatechange (id) 
		DEFERRABLE;

ALTER TABLE states_message 
	ADD COLUMN trackingrecordstatechange_id bigint;

ALTER TABLE states_message 
	ADD CONSTRAINT message_trackingrecordstatechange_fkey 
		FOREIGN KEY (trackingrecordstatechange_id)
		REFERENCES advancedgenealogy_trackingrecordstatechange (id) 
		DEFERRABLE;
		
-- end

		
-- Table: wagegroups_wagegroup
-- changed: 3.07.2012

CREATE TABLE wagegroups_wagegroup
(
  id bigint NOT NULL,
  "number" character varying(255),
  "name" character varying(255),
  superiorwagegroup character varying(255),
  laborhourlycostfromwagegroup numeric(12,5),
  determinedindividually boolean,
  individuallaborhourlycost numeric(12,5),
  laborhourlycost numeric(12,5),
  laborhourlycostunit character varying(255),
  CONSTRAINT wagegroups_wagegroup_pkey PRIMARY KEY (id)
);

-- end


-- Table: basic_staff
-- changed: 4.07.2012

ALTER TABLE basic_staff ADD COLUMN individuallaborcost numeric(12,5);
ALTER TABLE basic_staff ADD COLUMN determinedindividual boolean;
ALTER TABLE basic_staff ADD COLUMN email character varying(255);
ALTER TABLE basic_staff ADD COLUMN phone character varying(255);
ALTER TABLE basic_staff ADD COLUMN workfor_id bigint;
ALTER TABLE basic_staff  
	ADD CONSTRAINT basic_company_fkey 
		FOREIGN KEY (workfor_id)
    	REFERENCES basic_company (id) 
    	DEFERRABLE;
ALTER TABLE basic_staff ADD COLUMN wagegroup_id bigint;
ALTER TABLE basic_staff  
	ADD CONSTRAINT wagegroups_wagegroup_fkey 
		FOREIGN KEY (wagegroup_id)
    	REFERENCES wagegroups_wagegroup (id) 
    	DEFERRABLE;
ALTER TABLE basic_staff ADD COLUMN laborhourlycost numeric(12,5);

-- end


-- Table: qcadoomodel_dictionaryitem
-- changed: 11.07.2012

ALTER TABLE qcadoomodel_dictionaryitem ADD COLUMN technicalcode character varying(255);

-- end 


-- Table: assignmenttoshift_assignmenttoshift
-- changed: 11.07.2012

CREATE TABLE assignmenttoshift_assignmenttoshift
(
  id bigint NOT NULL,
  startdate date,
  shift_id bigint,
  state character varying(255) DEFAULT '01draft'::character varying,
  approvedattendancelist boolean,
  CONSTRAINT assignmenttoshift_assignmenttoshift_pkey PRIMARY KEY (id),
  CONSTRAINT basic_shift_fkey FOREIGN KEY (shift_id)
      REFERENCES basic_shift (id) DEFERRABLE
);

-- end 


-- Table: assignmenttoshift_assignmenttoshiftstatechange
-- changed: 11.07.2012

CREATE TABLE assignmenttoshift_assignmenttoshiftstatechange
(
  id bigint NOT NULL,
  dateandtime timestamp without time zone,
  sourcestate character varying(255),
  targetstate character varying(255),
  status character varying(255),
  phase integer,
  worker character varying(255),
  assignmenttoshift_id bigint,
  shift_id bigint,
  additionalinformation character varying(255),
  CONSTRAINT assignmenttoshift_assignmenttoshiftstatechange_pkey PRIMARY KEY (id),
  CONSTRAINT basic_shift_fkey FOREIGN KEY (shift_id)
      REFERENCES basic_shift DEFERRABLE,
  CONSTRAINT assignmenttoshift_assignmenttoshift_fkey FOREIGN KEY (assignmenttoshift_id)
      REFERENCES assignmenttoshift_assignmenttoshift DEFERRABLE
);

-- end


-- Table: assignmenttoshift_staffassignmenttoshift
-- changed: 11.07.2012

CREATE TABLE assignmenttoshift_staffassignmenttoshift
(
  id bigint NOT NULL,
  assignmenttoshift_id bigint,
  worker_id bigint,
  productionline_id bigint,
  occupationtype character varying(255),
  occupationtypename character varying(255),
  state character varying(255) DEFAULT '01simple'::character varying,
  occupationtypeenum character varying(255),
  occupationtypevalueforgrid character varying(255),
  CONSTRAINT assignmenttoshift_staffassignmenttoshift_pkey PRIMARY KEY (id),
  CONSTRAINT basic_staff_fkey FOREIGN KEY (worker_id)
      REFERENCES basic_staff DEFERRABLE,
  CONSTRAINT productionlines_productionline_fkey FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline DEFERRABLE,
  CONSTRAINT assignmenttoshift_assignmenttoshift_fkey FOREIGN KEY (assignmenttoshift_id)
      REFERENCES assignmenttoshift_assignmenttoshift DEFERRABLE
);

-- end 


-- Table: states_message
-- changed: 11.07.2012

ALTER TABLE states_message ADD COLUMN assignmenttoshiftstatechange_id bigint;

-- end 


-- Table: technologies_technologyinstanceoperationcomponent
-- changed: 13.07.2012

ALTER TABLE technologies_technologyinstanceoperationcomponent ADD COLUMN createdate timestamp without time zone;
ALTER TABLE technologies_technologyinstanceoperationcomponent ADD COLUMN updatedate timestamp without time zone;
ALTER TABLE technologies_technologyinstanceoperationcomponent ADD COLUMN "createuser" character varying(255);
ALTER TABLE technologies_technologyinstanceoperationcomponent ADD COLUMN updateuser character varying(255);

-- end


-- Table: materialflow_transfer
-- changed: 06.06.2012

ALTER TABLE materialflow_transfer ADD COLUMN price numeric(12,5);
ALTER TABLE materialflow_transfer ALTER COLUMN price SET DEFAULT 0::numeric;

ALTER TABLE materialflow_transfer ADD COLUMN batch character varying(255);

-- end


-- Table: materialflowmultitransfers_productquantity
-- changed: 06.06.2012

ALTER TABLE materialflowmultitransfers_productquantity ADD COLUMN price numeric(12,5);
ALTER TABLE materialflowmultitransfers_productquantity ALTER COLUMN price SET DEFAULT 0::numeric;

-- end


-- Table: materialflowresources_resource
-- changed: 06.06.2012

ALTER TABLE materialflowresources_resource ADD COLUMN price numeric(12,5);
ALTER TABLE materialflowresources_resource ALTER COLUMN price SET DEFAULT 0::numeric;

ALTER TABLE materialflowresources_resource ADD COLUMN batch character varying(255);

-- end
