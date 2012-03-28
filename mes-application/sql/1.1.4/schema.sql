--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

CREATE PROCEDURAL LANGUAGE plpgsql;


ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO postgres;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: basic_company; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_company (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(255) DEFAULT 'Company'::character varying,
    tax character varying(255),
    street character varying(255),
    house character varying(30),
    flat character varying(30),
    zipcode character varying(6),
    city character varying(255),
    state character varying(255),
    country character varying(255),
    email character varying(255),
    website character varying(255),
    phone character varying(255),
    owner boolean DEFAULT false,
    externalnumber character varying(255),
    active boolean DEFAULT true
);


ALTER TABLE public.basic_company OWNER TO postgres;

--
-- Name: basic_currency; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_currency (
    id bigint NOT NULL,
    currency character varying(255),
    alphabeticcode character varying(3),
    isocode integer,
    minorunit integer
);


ALTER TABLE public.basic_currency OWNER TO postgres;

--
-- Name: basic_division; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_division (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(1024),
    supervisor_id bigint
);


ALTER TABLE public.basic_division OWNER TO postgres;

--
-- Name: basic_parameter; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_parameter (
    id bigint NOT NULL,
    currency_id bigint,
    dontprintinputproductsinworkplans boolean,
    allowtoclose boolean,
    registerproductiontime boolean,
    hidedetailsinworkplans boolean,
    hidetechnologyandorderinworkplans boolean,
    imageurlinworkplan character varying(255),
    dontprintordersinworkplans boolean,
    justone boolean,
    hidedescriptioninworkplans boolean,
    batchfordoneorder character varying(255) DEFAULT '01none'::character varying,
    registerquantityoutproduct boolean,
    registerquantityinproduct boolean,
    autogeneratequalitycontrol boolean,
    dontprintoutputproductsinworkplans boolean,
    checkdoneorderforquality boolean,
    autocloseorder boolean,
    registerpiecework boolean
);


ALTER TABLE public.basic_parameter OWNER TO postgres;

--
-- Name: basic_product; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_product (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(1024),
    globaltypeofmaterial character varying(255),
    ean character varying(255),
    category character varying(255),
    unit character varying(255),
    externalnumber character varying(255),
    description character varying(2048),
    lastusedbatch character varying(255),
    lastpurchasecost numeric(10,3) DEFAULT 0::numeric,
    genealogybatchreq boolean,
    batch character varying(255),
    averagecost numeric(10,3) DEFAULT 0::numeric,
    costfornumber numeric(10,3) DEFAULT 1::numeric,
    nominalcost numeric(10,3) DEFAULT 0::numeric,
    active boolean DEFAULT true,
    createdate timestamp without time zone,
    updatedate timestamp without time zone,
    createuser character varying(255),
    updateuser character varying(255)
);


ALTER TABLE public.basic_product OWNER TO postgres;

--
-- Name: basic_shift; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_shift (
    id bigint NOT NULL,
    name character varying(1024),
    mondayworking boolean DEFAULT true,
    mondayhours character varying(255),
    tuesdayworking boolean DEFAULT true,
    tuesdayhours character varying(255),
    wensdayworking boolean DEFAULT true,
    wensdayhours character varying(255),
    thursdayworking boolean DEFAULT true,
    thursdayhours character varying(255),
    fridayworking boolean DEFAULT true,
    fridayhours character varying(255),
    saturdayworking boolean DEFAULT false,
    saturdayhours character varying(255),
    sundayworking boolean DEFAULT false,
    sundayhours character varying(255)
);


ALTER TABLE public.basic_shift OWNER TO postgres;

--
-- Name: basic_shifttimetableexception; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_shifttimetableexception (
    id bigint NOT NULL,
    name character varying(1024),
    fromdate timestamp without time zone,
    todate timestamp without time zone,
    type character varying(255) DEFAULT '01freeTime'::character varying,
    shift_id bigint
);


ALTER TABLE public.basic_shifttimetableexception OWNER TO postgres;

--
-- Name: basic_staff; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_staff (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(255),
    surname character varying(255),
    post character varying(255),
    shift_id bigint,
    division_id bigint,
    active boolean DEFAULT true
);


ALTER TABLE public.basic_staff OWNER TO postgres;

--
-- Name: basic_substitute; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_substitute (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(1024),
    product_id bigint,
    priority integer
);


ALTER TABLE public.basic_substitute OWNER TO postgres;

--
-- Name: basic_substitutecomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_substitutecomponent (
    id bigint NOT NULL,
    product_id bigint,
    substitute_id bigint,
    quantity numeric(7,3)
);


ALTER TABLE public.basic_substitutecomponent OWNER TO postgres;

--
-- Name: basic_workstationtype; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_workstationtype (
    id bigint NOT NULL,
    name character varying(1024),
    number character varying(255),
    description character varying(2048),
    division_id bigint,
    active boolean DEFAULT true
);


ALTER TABLE public.basic_workstationtype OWNER TO postgres;

--
-- Name: basicproductioncounting_basicproductioncounting; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basicproductioncounting_basicproductioncounting (
    id bigint NOT NULL,
    order_id bigint,
    product_id bigint,
    plannedquantity numeric(10,3),
    usedquantity numeric(10,3),
    producedquantity numeric(10,3)
);


ALTER TABLE public.basicproductioncounting_basicproductioncounting OWNER TO postgres;

--
-- Name: costcalculation_costcalculation; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE costcalculation_costcalculation (
    id bigint NOT NULL,
    number character varying(255),
    product_id bigint,
    defaulttechnology_id bigint,
    technology_id bigint,
    quantity numeric(10,3),
    order_id bigint,
    totalmaterialcosts numeric(10,3),
    totalmachinehourlycosts numeric(10,3),
    totalpieceworkcosts numeric(10,3),
    totallaborhourlycosts numeric(10,3),
    totaltechnicalproductioncosts numeric(10,3),
    productioncostmargin numeric(10,3) DEFAULT 0::numeric,
    productioncostmarginvalue numeric(10,3),
    materialcostmargin numeric(10,3) DEFAULT 0::numeric,
    materialcostmarginvalue numeric(10,3),
    additionaloverhead numeric(10,3) DEFAULT 0::numeric,
    additionaloverheadvalue numeric(10,3) DEFAULT 0::numeric,
    totaloverhead numeric(10,3),
    totalcosts numeric(10,3),
    totalcostperunit numeric(10,3),
    description character varying(2024),
    includetpz boolean DEFAULT true,
    includeadditionaltime boolean DEFAULT true,
    printcostnormsofmaterials boolean DEFAULT true,
    printoperationnorms boolean DEFAULT true,
    sourceofmaterialcosts character varying(255) DEFAULT '01currentGlobalDefinitionsInProduct'::character varying,
    calculatematerialcostsmode character varying(255) DEFAULT '01nominal'::character varying,
    calculateoperationcostsmode character varying(255) DEFAULT '01hourly'::character varying,
    date timestamp without time zone,
    generated boolean,
    filename character varying(255)
);


ALTER TABLE public.costcalculation_costcalculation OWNER TO postgres;

--
-- Name: costnormsforoperation_calculationoperationcomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE costnormsforoperation_calculationoperationcomponent (
    id bigint NOT NULL,
    nodenumber character varying(255),
    operation_id bigint,
    technologyoperationcomponent_id bigint,
    parent_id bigint,
    entitytype character varying(255) DEFAULT 'operation'::character varying,
    priority integer,
    tpz integer,
    tj integer,
    machineutilization numeric(10,3),
    laborutilization numeric(10,3),
    productioninonecycle numeric(10,3) DEFAULT 1::numeric,
    countrealized character varying(255) DEFAULT '01all'::character varying,
    countmachine numeric(10,3),
    timenextoperation integer,
    operationoffset integer,
    effectiveoperationrealizationtime integer,
    effectivedatefrom timestamp without time zone,
    effectivedateto timestamp without time zone,
    duration integer DEFAULT 0,
    pieces numeric(10,3) DEFAULT 0::numeric,
    operationcost numeric(10,3) DEFAULT 0::numeric,
    operationmargincost numeric(10,3) DEFAULT 0::numeric,
    totaloperationcost numeric(10,3) DEFAULT 0::numeric,
    pieceworkcost numeric(10,3) DEFAULT 0::numeric,
    laborhourlycost numeric(10,3) DEFAULT 0::numeric,
    machinehourlycost numeric(10,3) DEFAULT 0::numeric,
    numberofoperations integer DEFAULT 1,
    productionbalance_id bigint,
    costcalculation_id bigint
);


ALTER TABLE public.costnormsforoperation_calculationoperationcomponent OWNER TO postgres;

--
-- Name: costnormsforproduct_orderoperationproductincomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE costnormsforproduct_orderoperationproductincomponent (
    id bigint NOT NULL,
    order_id bigint,
    product_id bigint,
    costfornumber numeric(10,3) DEFAULT 1::numeric,
    nominalcost numeric(10,3) DEFAULT 0::numeric,
    lastpurchasecost numeric(10,3) DEFAULT 0::numeric,
    averagecost numeric(10,3) DEFAULT 0::numeric,
    costfororder numeric(10,3) DEFAULT 0::numeric
);


ALTER TABLE public.costnormsforproduct_orderoperationproductincomponent OWNER TO postgres;

--
-- Name: genealogies_currentattribute; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE genealogies_currentattribute (
    id bigint NOT NULL,
    shift_id bigint,
    shiftreq boolean,
    post character varying(255),
    postreq boolean,
    other character varying(255),
    otherreq boolean,
    lastusedshift character varying(255),
    lastusedpost character varying(255),
    lastusedother character varying(255)
);


ALTER TABLE public.genealogies_currentattribute OWNER TO postgres;

--
-- Name: genealogies_genealogy; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE genealogies_genealogy (
    id bigint NOT NULL,
    batch character varying(255),
    order_id bigint,
    date date,
    worker character varying(255)
);


ALTER TABLE public.genealogies_genealogy OWNER TO postgres;

--
-- Name: genealogies_otherfeature; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE genealogies_otherfeature (
    id bigint NOT NULL,
    value character varying(255),
    date date,
    worker character varying(255),
    genealogy_id bigint
);


ALTER TABLE public.genealogies_otherfeature OWNER TO postgres;

--
-- Name: genealogies_postfeature; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE genealogies_postfeature (
    id bigint NOT NULL,
    value character varying(255),
    date date,
    worker character varying(255),
    genealogy_id bigint
);


ALTER TABLE public.genealogies_postfeature OWNER TO postgres;

--
-- Name: genealogies_shiftfeature; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE genealogies_shiftfeature (
    id bigint NOT NULL,
    value_id bigint,
    date date,
    worker character varying(255),
    genealogy_id bigint
);


ALTER TABLE public.genealogies_shiftfeature OWNER TO postgres;

--
-- Name: genealogiesforcomponents_genealogyproductincomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE genealogiesforcomponents_genealogyproductincomponent (
    id bigint NOT NULL,
    genealogy_id bigint,
    productincomponent_id bigint
);


ALTER TABLE public.genealogiesforcomponents_genealogyproductincomponent OWNER TO postgres;

--
-- Name: genealogiesforcomponents_productinbatch; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE genealogiesforcomponents_productinbatch (
    id bigint NOT NULL,
    batch character varying(255),
    date date,
    worker character varying(255),
    productincomponent_id bigint
);


ALTER TABLE public.genealogiesforcomponents_productinbatch OWNER TO postgres;

--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO postgres;

--
-- Name: jointable_materialrequirement_order; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE jointable_materialrequirement_order (
    order_id bigint NOT NULL,
    materialrequirement_id bigint NOT NULL
);


ALTER TABLE public.jointable_materialrequirement_order OWNER TO postgres;

--
-- Name: jointable_order_workplan; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE jointable_order_workplan (
    order_id bigint NOT NULL,
    workplan_id bigint NOT NULL
);


ALTER TABLE public.jointable_order_workplan OWNER TO postgres;

--
-- Name: materialflow_materialsinstockareas; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflow_materialsinstockareas (
    id bigint NOT NULL,
    name character varying(1024),
    materialflowfordate timestamp without time zone,
    "time" timestamp without time zone,
    worker character varying(255),
    generated boolean,
    filename character varying(255),
    active boolean DEFAULT true
);


ALTER TABLE public.materialflow_materialsinstockareas OWNER TO postgres;

--
-- Name: materialflow_materialsinstockareascomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflow_materialsinstockareascomponent (
    id bigint NOT NULL,
    materialsinstockareas_id bigint,
    stockareas_id bigint
);


ALTER TABLE public.materialflow_materialsinstockareascomponent OWNER TO postgres;

--
-- Name: materialflow_stockareas; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflow_stockareas (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(255)
);


ALTER TABLE public.materialflow_stockareas OWNER TO postgres;

--
-- Name: materialflow_stockcorrection; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflow_stockcorrection (
    id bigint NOT NULL,
    number character varying(255),
    stockcorrectiondate timestamp without time zone,
    stockareas_id bigint,
    product_id bigint,
    found numeric(10,3),
    staff_id bigint
);


ALTER TABLE public.materialflow_stockcorrection OWNER TO postgres;

--
-- Name: materialflow_transfer; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflow_transfer (
    id bigint NOT NULL,
    number character varying(255),
    type character varying(255),
    "time" timestamp without time zone,
    stockareasfrom_id bigint,
    stockareasto_id bigint,
    product_id bigint,
    quantity numeric(10,3),
    staff_id bigint,
    transformationsconsumption_id bigint,
    transformationsproduction_id bigint
);


ALTER TABLE public.materialflow_transfer OWNER TO postgres;

--
-- Name: materialflow_transformations; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflow_transformations (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(1024),
    "time" timestamp without time zone,
    stockareasfrom_id bigint,
    stockareasto_id bigint,
    staff_id bigint
);


ALTER TABLE public.materialflow_transformations OWNER TO postgres;

--
-- Name: materialrequirements_materialrequirement; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialrequirements_materialrequirement (
    id bigint NOT NULL,
    name character varying(1024),
    date timestamp without time zone,
    worker character varying(255),
    onlycomponents boolean DEFAULT true,
    generated boolean,
    filename character varying(255),
    active boolean DEFAULT true
);


ALTER TABLE public.materialrequirements_materialrequirement OWNER TO postgres;

--
-- Name: ordergroups_ordergroup; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE ordergroups_ordergroup (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(1024),
    datefrom date,
    dateto date,
    active boolean DEFAULT true
);


ALTER TABLE public.ordergroups_ordergroup OWNER TO postgres;

--
-- Name: orders_logging; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE orders_logging (
    id bigint NOT NULL,
    order_id bigint,
    dateandtime timestamp without time zone,
    previousstate character varying(255),
    currentstate character varying(255),
    shift_id bigint,
    worker character varying(255),
    active boolean DEFAULT true
);


ALTER TABLE public.orders_logging OWNER TO postgres;

--
-- Name: orders_order; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE orders_order (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(1024),
    description character varying(2048),
    datefrom timestamp without time zone,
    dateto timestamp without time zone,
    state character varying(255),
    company_id bigint,
    product_id bigint,
    technology_id bigint,
    plannedquantity numeric(8,3),
    donequantity numeric(8,3),
    effectivedatefrom timestamp without time zone,
    effectivedateto timestamp without time zone,
    externalnumber character varying(255),
    externalsynchronized boolean DEFAULT true,
    ordergroup_id bigint,
    autocloseorder boolean,
    registerpiecework boolean,
    ordergroupname character varying(255),
    registerquantityoutproduct boolean,
    realizationtime integer,
    calculate boolean,
    allowtoclose boolean,
    typeofproductionrecording character varying(255) DEFAULT '02cumulated'::character varying,
    alert character varying(255),
    registerproductiontime boolean,
    registerquantityinproduct boolean,
    justone boolean,
    active boolean DEFAULT true
);


ALTER TABLE public.orders_order OWNER TO postgres;

--
-- Name: productioncounting_balanceoperationproductincomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_balanceoperationproductincomponent (
    id bigint NOT NULL,
    productionrecord_id bigint,
    productionbalance_id bigint,
    product_id bigint,
    plannedquantity numeric(8,3),
    usedquantity numeric(8,3),
    balance numeric(10,3)
);


ALTER TABLE public.productioncounting_balanceoperationproductincomponent OWNER TO postgres;

--
-- Name: productioncounting_balanceoperationproductoutcomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_balanceoperationproductoutcomponent (
    id bigint NOT NULL,
    productionrecord_id bigint,
    productionbalance_id bigint,
    product_id bigint,
    plannedquantity numeric(8,3),
    usedquantity numeric(8,3),
    balance numeric(10,3)
);


ALTER TABLE public.productioncounting_balanceoperationproductoutcomponent OWNER TO postgres;

--
-- Name: productioncounting_operationtimecomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_operationtimecomponent (
    id bigint NOT NULL,
    productionbalance_id bigint,
    orderoperationcomponent_id bigint,
    plannedmachinetime integer,
    machinetime integer,
    machinetimebalance integer,
    plannedlabortime integer,
    labortime integer,
    labortimebalance integer
);


ALTER TABLE public.productioncounting_operationtimecomponent OWNER TO postgres;

--
-- Name: productioncounting_productionbalance; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_productionbalance (
    id bigint NOT NULL,
    generated boolean,
    order_id bigint,
    product_id bigint,
    name character varying(1024),
    date timestamp without time zone,
    worker character varying(255),
    recordsnumber integer,
    description character varying(255),
    filename character varying(255),
    printoperationnorms boolean DEFAULT true,
    calculateoperationcostsmode character varying(255) DEFAULT '01hourly'::character varying,
    includetpz boolean DEFAULT true,
    includeadditionaltime boolean DEFAULT true,
    plannedmachinetime integer,
    machinetime integer,
    machinetimebalance integer,
    plannedlabortime integer,
    labortime integer,
    labortimebalance integer,
    machinecosts numeric(10,3),
    totaloverhead numeric(10,3),
    productioncostmargin numeric(10,3) DEFAULT 0::numeric,
    machinecostsbalance numeric(10,3),
    plannedmachinecosts numeric(10,3),
    technology_id bigint,
    materialcostmarginvalue numeric(10,3) DEFAULT 0::numeric,
    plannedcomponentscosts numeric(10,3),
    quantity numeric(10,3),
    totaltechnicalproductioncosts numeric(10,3),
    balancetechnicalproductioncostperunit numeric(10,3),
    balancetechnicalproductioncosts numeric(10,3),
    totalcostperunit numeric(10,3),
    totalcosts numeric(10,3),
    productioncostmarginvalue numeric(10,3) DEFAULT 0::numeric,
    componentscostsbalance numeric(10,3),
    plannedlaborcosts numeric(10,3),
    sourceofmaterialcosts character varying(255) DEFAULT '01currentGlobalDefinitionsInProduct'::character varying,
    laborcosts numeric(10,3),
    averagemachinehourlycost numeric(10,3),
    printcostnormsofmaterials boolean DEFAULT true,
    componentscosts numeric(10,3),
    calculatematerialcostsmode character varying(255) DEFAULT '01nominal'::character varying,
    registeredtotaltechnicalproductioncosts numeric(10,3),
    registeredtotaltechnicalproductioncostperunit numeric(10,3),
    additionaloverheadvalue numeric(10,3) DEFAULT 0::numeric,
    materialcostmargin numeric(10,3) DEFAULT 0::numeric,
    averagelaborhourlycost numeric(10,3),
    additionaloverhead numeric(10,3) DEFAULT 0::numeric,
    generatedwithcosts boolean,
    totaltechnicalproductioncostperunit numeric(10,3),
    laborcostsbalance numeric(10,3)
);


ALTER TABLE public.productioncounting_productionbalance OWNER TO postgres;

--
-- Name: productioncounting_productioncounting; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_productioncounting (
    id bigint NOT NULL,
    generated boolean,
    order_id bigint,
    product_id bigint,
    name character varying(1024),
    date timestamp without time zone,
    worker character varying(255),
    description character varying(255),
    filename character varying(255),
    active boolean DEFAULT true
);


ALTER TABLE public.productioncounting_productioncounting OWNER TO postgres;

--
-- Name: productioncounting_productionrecord; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_productionrecord (
    id bigint NOT NULL,
    number character varying(255),
    order_id bigint,
    orderoperationcomponent_id bigint,
    shift_id bigint,
    state character varying(255) DEFAULT '01draft'::character varying,
    lastrecord boolean,
    plannedmachinetime integer,
    machinetime integer,
    machinetimebalance integer,
    plannedlabortime integer,
    labortime integer,
    labortimebalance integer,
    plannedtime integer,
    executedoperationcycles integer,
    staff_id bigint,
    workstationtype_id bigint,
    division_id bigint,
    active boolean DEFAULT true
);


ALTER TABLE public.productioncounting_productionrecord OWNER TO postgres;

--
-- Name: productioncounting_productionrecordlogging; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_productionrecordlogging (
    id bigint NOT NULL,
    productionrecord_id bigint,
    dateandtime timestamp without time zone,
    previousstate character varying(255),
    currentstate character varying(255),
    worker character varying(255)
);


ALTER TABLE public.productioncounting_productionrecordlogging OWNER TO postgres;

--
-- Name: productioncounting_recordoperationproductincomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_recordoperationproductincomponent (
    id bigint NOT NULL,
    productionrecord_id bigint,
    product_id bigint,
    plannedquantity numeric(8,3),
    usedquantity numeric(8,3),
    balance numeric(10,3)
);


ALTER TABLE public.productioncounting_recordoperationproductincomponent OWNER TO postgres;

--
-- Name: productioncounting_recordoperationproductoutcomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_recordoperationproductoutcomponent (
    id bigint NOT NULL,
    productionrecord_id bigint,
    product_id bigint,
    plannedquantity numeric(8,3),
    usedquantity numeric(8,3),
    balance numeric(10,3)
);


ALTER TABLE public.productioncounting_recordoperationproductoutcomponent OWNER TO postgres;

--
-- Name: productioncountingwithcosts_operationcostcomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncountingwithcosts_operationcostcomponent (
    id bigint NOT NULL,
    productionbalance_id bigint,
    orderoperationcomponent_id bigint,
    plannedmachinecosts numeric(10,3),
    machinecosts numeric(10,3),
    machinecostsbalance numeric(10,3),
    plannedlaborcosts numeric(10,3),
    laborcosts numeric(10,3),
    laborcostsbalance numeric(10,3)
);


ALTER TABLE public.productioncountingwithcosts_operationcostcomponent OWNER TO postgres;

--
-- Name: productioncountingwithcosts_orderoperationproductincomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncountingwithcosts_orderoperationproductincomponent (
    id bigint NOT NULL,
    productionbalance_id bigint,
    product_id bigint,
    plannedcost numeric(10,3),
    registeredcost numeric(10,3),
    balance numeric(10,3)
);


ALTER TABLE public.productioncountingwithcosts_orderoperationproductincomponent OWNER TO postgres;

--
-- Name: productionscheduling_orderoperationcomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productionscheduling_orderoperationcomponent (
    id bigint NOT NULL,
    order_id bigint,
    technology_id bigint,
    operation_id bigint,
    technologyoperationcomponent_id bigint,
    parent_id bigint,
    entitytype character varying(255) DEFAULT 'operation'::character varying,
    priority integer,
    tpz integer DEFAULT 0,
    tj integer DEFAULT 0,
    productioninonecycle numeric(10,3) DEFAULT 1::numeric,
    countrealized character varying(255) DEFAULT '01all'::character varying,
    countmachine numeric(10,3) DEFAULT 0.0,
    timenextoperation integer DEFAULT 0,
    machineutilization numeric(6,3) DEFAULT 1.0,
    laborutilization numeric(6,3) DEFAULT 1.0,
    operationoffset integer,
    effectiveoperationrealizationtime integer,
    effectivedatefrom timestamp without time zone,
    effectivedateto timestamp without time zone,
    nodenumber character varying(255),
    dontprintinputproductsinworkplans boolean,
    hidedetailsinworkplans boolean,
    laborhourlycost numeric(10,3) DEFAULT 0::numeric,
    imageurlinworkplan character varying(255),
    machinehourlycost numeric(10,3) DEFAULT 0::numeric,
    pieceworkcost numeric(10,3) DEFAULT 0::numeric,
    hidetechnologyandorderinworkplans boolean,
    hidedescriptioninworkplans boolean,
    numberofoperations integer DEFAULT 1,
    dontprintoutputproductsinworkplans boolean
);


ALTER TABLE public.productionscheduling_orderoperationcomponent OWNER TO postgres;

--
-- Name: qcadoomodel_dictionary; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qcadoomodel_dictionary (
    id bigint NOT NULL,
    name character varying(255),
    pluginidentifier character varying(255),
    active boolean
);


ALTER TABLE public.qcadoomodel_dictionary OWNER TO postgres;

--
-- Name: qcadoomodel_dictionaryitem; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qcadoomodel_dictionaryitem (
    id bigint NOT NULL,
    name character varying(255),
    externalnumber character varying(255),
    description character varying(2048),
    dictionary_id bigint
);


ALTER TABLE public.qcadoomodel_dictionaryitem OWNER TO postgres;

--
-- Name: qcadooplugin_plugin; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qcadooplugin_plugin (
    id bigint NOT NULL,
    identifier character varying(255),
    issystem boolean,
    state character varying(255),
    version character varying(255)
);


ALTER TABLE public.qcadooplugin_plugin OWNER TO postgres;

--
-- Name: qcadoosecurity_persistenttoken; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qcadoosecurity_persistenttoken (
    id bigint NOT NULL,
    username character varying(255),
    series character varying(255),
    token character varying(255),
    lastused timestamp without time zone
);


ALTER TABLE public.qcadoosecurity_persistenttoken OWNER TO postgres;

--
-- Name: qcadoosecurity_user; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qcadoosecurity_user (
    id bigint NOT NULL,
    username character varying(255),
    role character varying(255),
    email character varying(255),
    firstname character varying(255),
    lastname character varying(255),
    enabled boolean DEFAULT true,
    description character varying(255),
    password character varying(255),
    lastactivity timestamp without time zone
);


ALTER TABLE public.qcadoosecurity_user OWNER TO postgres;

--
-- Name: qcadooview_category; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qcadooview_category (
    id bigint NOT NULL,
    pluginidentifier character varying(255),
    name character varying(255),
    succession integer
);


ALTER TABLE public.qcadooview_category OWNER TO postgres;

--
-- Name: qcadooview_item; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qcadooview_item (
    id bigint NOT NULL,
    pluginidentifier character varying(255),
    name character varying(255),
    active boolean DEFAULT true,
    category_id bigint,
    view_id bigint,
    succession integer
);


ALTER TABLE public.qcadooview_item OWNER TO postgres;

--
-- Name: qcadooview_view; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qcadooview_view (
    id bigint NOT NULL,
    pluginidentifier character varying(255),
    name character varying(255),
    view character varying(255),
    url character varying(255)
);


ALTER TABLE public.qcadooview_view OWNER TO postgres;

--
-- Name: qualitycontrols_qualitycontrol; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qualitycontrols_qualitycontrol (
    id bigint NOT NULL,
    order_id bigint,
    operation_id bigint,
    number character varying(255),
    controlresult character varying(255),
    batchnr character varying(255),
    comment character varying(255),
    controlinstruction character varying(255),
    controlledquantity numeric(10,3),
    takenforcontrolquantity numeric(10,3),
    rejectedquantity numeric(10,3),
    accepteddefectsquantity numeric(10,3),
    staff character varying(255),
    date date,
    closed boolean,
    qualitycontroltype character varying(255)
);


ALTER TABLE public.qualitycontrols_qualitycontrol OWNER TO postgres;

--
-- Name: simplematerialbalance_simplematerialbalance; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE simplematerialbalance_simplematerialbalance (
    id bigint NOT NULL,
    name character varying(1024),
    date timestamp without time zone,
    worker character varying(255),
    onlycomponents boolean DEFAULT true,
    generated boolean,
    filename character varying(255)
);


ALTER TABLE public.simplematerialbalance_simplematerialbalance OWNER TO postgres;

--
-- Name: simplematerialbalance_simplematerialbalanceorderscomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE simplematerialbalance_simplematerialbalanceorderscomponent (
    id bigint NOT NULL,
    simplematerialbalance_id bigint,
    order_id bigint
);


ALTER TABLE public.simplematerialbalance_simplematerialbalanceorderscomponent OWNER TO postgres;

--
-- Name: simplematerialbalance_simplematerialbalancestockareascomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE simplematerialbalance_simplematerialbalancestockareascomponent (
    id bigint NOT NULL,
    simplematerialbalance_id bigint,
    stockareas_id bigint
);


ALTER TABLE public.simplematerialbalance_simplematerialbalancestockareascomponent OWNER TO postgres;

--
-- Name: stoppage_stoppage; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE stoppage_stoppage (
    id bigint NOT NULL,
    order_id bigint,
    duration integer,
    reason text,
    active boolean DEFAULT true
);


ALTER TABLE public.stoppage_stoppage OWNER TO postgres;

--
-- Name: technologies_logging; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE technologies_logging (
    id bigint NOT NULL,
    technology_id bigint,
    dateandtime timestamp without time zone,
    previousstate character varying(255),
    currentstate character varying(255),
    shift_id bigint,
    worker character varying(255),
    active boolean DEFAULT true
);


ALTER TABLE public.technologies_logging OWNER TO postgres;

--
-- Name: technologies_operation; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE technologies_operation (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(1024),
    comment character varying(2048),
    workstationtype_id bigint,
    attachment character varying(255),
    hidedescriptioninworkplans boolean,
    countmachineoperation numeric(6,3) DEFAULT 0::numeric,
    hidedetailsinworkplans boolean,
    countrealizedoperation character varying(255) DEFAULT '01all'::character varying,
    machinehourlycost numeric(10,3) DEFAULT 0::numeric,
    tpz integer DEFAULT 0,
    laborutilization numeric(6,3) DEFAULT 1.0,
    hidetechnologyandorderinworkplans boolean,
    timenextoperation integer DEFAULT 0,
    machineutilization numeric(6,3) DEFAULT 1.0,
    numberofoperations integer DEFAULT 1,
    dontprintinputproductsinworkplans boolean,
    productioninonecycle numeric(10,3) DEFAULT 1::numeric,
    tj integer DEFAULT 0,
    laborhourlycost numeric(10,3) DEFAULT 0::numeric,
    imageurlinworkplan character varying(255),
    pieceworkcost numeric(10,3) DEFAULT 0::numeric,
    dontprintoutputproductsinworkplans boolean,
    active boolean DEFAULT true
);


ALTER TABLE public.technologies_operation OWNER TO postgres;

--
-- Name: technologies_operationproductincomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE technologies_operationproductincomponent (
    id bigint NOT NULL,
    operationcomponent_id bigint,
    product_id bigint,
    quantity numeric(8,3),
    batchrequired boolean
);


ALTER TABLE public.technologies_operationproductincomponent OWNER TO postgres;

--
-- Name: technologies_operationproductoutcomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE technologies_operationproductoutcomponent (
    id bigint NOT NULL,
    operationcomponent_id bigint,
    product_id bigint,
    quantity numeric(8,3)
);


ALTER TABLE public.technologies_operationproductoutcomponent OWNER TO postgres;

--
-- Name: technologies_technology; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE technologies_technology (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(2048),
    product_id bigint,
    master boolean DEFAULT true,
    description character varying(2048),
    state character varying(255) DEFAULT '01draft'::character varying,
    unitsamplingnr numeric(10,3),
    postfeaturerequired boolean,
    otherfeaturerequired boolean,
    shiftfeaturerequired boolean,
    minimalquantity numeric(8,3),
    qualitycontrolinstruction character varying(255),
    qualitycontroltype character varying(255),
    batchrequired boolean,
    active boolean DEFAULT true
);


ALTER TABLE public.technologies_technology OWNER TO postgres;

--
-- Name: technologies_technologyoperationcomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE technologies_technologyoperationcomponent (
    id bigint NOT NULL,
    nodenumber character varying(255),
    technology_id bigint,
    parent_id bigint,
    entitytype character varying(255),
    operation_id bigint,
    referencetechnology_id bigint,
    priority integer,
    attachment character varying(255),
    laborutilization numeric(6,3) DEFAULT 1.0,
    machineutilization numeric(6,3) DEFAULT 1.0,
    pieceworkcost numeric(10,3) DEFAULT 0::numeric,
    dontprintoutputproductsinworkplans boolean,
    hidetechnologyandorderinworkplans boolean,
    timenextoperation integer DEFAULT 0,
    tpz integer DEFAULT 0,
    tj integer DEFAULT 0,
    hidedescriptioninworkplans boolean,
    dontprintinputproductsinworkplans boolean,
    machinehourlycost numeric(10,3) DEFAULT 0::numeric,
    countmachine numeric(10,3) DEFAULT 0::numeric,
    laborhourlycost numeric(10,3) DEFAULT 0::numeric,
    productioninonecycle numeric(10,3) DEFAULT 1::numeric,
    countrealized character varying(255) DEFAULT '01all'::character varying,
    imageurlinworkplan character varying(255),
    hidedetailsinworkplans boolean,
    numberofoperations integer DEFAULT 1,
    qualitycontrolrequired boolean
);


ALTER TABLE public.technologies_technologyoperationcomponent OWNER TO postgres;

--
-- Name: workplans_columnforinputproducts; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_columnforinputproducts (
    id bigint NOT NULL,
    identifier character varying(255),
    name character varying(1024),
    description character varying(255),
    columnfiller character varying(2048)
);


ALTER TABLE public.workplans_columnforinputproducts OWNER TO postgres;

--
-- Name: workplans_columnfororders; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_columnfororders (
    id bigint NOT NULL,
    identifier character varying(255),
    name character varying(1024),
    description character varying(2048),
    columnfiller character varying(2048)
);


ALTER TABLE public.workplans_columnfororders OWNER TO postgres;

--
-- Name: workplans_columnforoutputproducts; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_columnforoutputproducts (
    id bigint NOT NULL,
    identifier character varying(255),
    name character varying(1024),
    description character varying(2048),
    columnfiller character varying(255)
);


ALTER TABLE public.workplans_columnforoutputproducts OWNER TO postgres;

--
-- Name: workplans_operationinputcolumn; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_operationinputcolumn (
    id bigint NOT NULL,
    operation_id bigint,
    columnforinputproducts_id bigint,
    succession integer
);


ALTER TABLE public.workplans_operationinputcolumn OWNER TO postgres;

--
-- Name: workplans_operationoutputcolumn; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_operationoutputcolumn (
    id bigint NOT NULL,
    operation_id bigint,
    columnforoutputproducts_id bigint,
    succession integer
);


ALTER TABLE public.workplans_operationoutputcolumn OWNER TO postgres;

--
-- Name: workplans_orderoperationinputcolumn; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_orderoperationinputcolumn (
    id bigint NOT NULL,
    orderoperationcomponent_id bigint,
    columnforinputproducts_id bigint,
    succession integer
);


ALTER TABLE public.workplans_orderoperationinputcolumn OWNER TO postgres;

--
-- Name: workplans_orderoperationoutputcolumn; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_orderoperationoutputcolumn (
    id bigint NOT NULL,
    orderoperationcomponent_id bigint,
    columnforoutputproducts_id bigint,
    succession integer
);


ALTER TABLE public.workplans_orderoperationoutputcolumn OWNER TO postgres;

--
-- Name: workplans_parameterinputcolumn; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_parameterinputcolumn (
    id bigint NOT NULL,
    parameter_id bigint,
    columnforinputproducts_id bigint,
    succession integer
);


ALTER TABLE public.workplans_parameterinputcolumn OWNER TO postgres;

--
-- Name: workplans_parameterordercolumn; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_parameterordercolumn (
    id bigint NOT NULL,
    parameter_id bigint,
    columnfororders_id bigint,
    succession integer
);


ALTER TABLE public.workplans_parameterordercolumn OWNER TO postgres;

--
-- Name: workplans_parameteroutputcolumn; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_parameteroutputcolumn (
    id bigint NOT NULL,
    parameter_id bigint,
    columnforoutputproducts_id bigint,
    succession integer
);


ALTER TABLE public.workplans_parameteroutputcolumn OWNER TO postgres;

--
-- Name: workplans_technologyoperationinputcolumn; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_technologyoperationinputcolumn (
    id bigint NOT NULL,
    technologyoperationcomponent_id bigint,
    columnforinputproducts_id bigint,
    succession integer
);


ALTER TABLE public.workplans_technologyoperationinputcolumn OWNER TO postgres;

--
-- Name: workplans_technologyoperationoutputcolumn; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_technologyoperationoutputcolumn (
    id bigint NOT NULL,
    technologyoperationcomponent_id bigint,
    columnforoutputproducts_id bigint,
    succession integer
);


ALTER TABLE public.workplans_technologyoperationoutputcolumn OWNER TO postgres;

--
-- Name: workplans_workplan; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_workplan (
    id bigint NOT NULL,
    name character varying(1024),
    date timestamp without time zone,
    worker character varying(255),
    generated boolean,
    filename character varying(255),
    type character varying(255) DEFAULT '01allOperations'::character varying,
    dontprintordersinworkplans boolean,
    active boolean DEFAULT true
);


ALTER TABLE public.workplans_workplan OWNER TO postgres;

--
-- Name: workplans_workplanordercolumn; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_workplanordercolumn (
    id bigint NOT NULL,
    workplan_id bigint,
    columnfororders_id bigint,
    succession integer
);


ALTER TABLE public.workplans_workplanordercolumn OWNER TO postgres;

--
-- Name: basic_company_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_company
    ADD CONSTRAINT basic_company_pkey PRIMARY KEY (id);


--
-- Name: basic_currency_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_currency
    ADD CONSTRAINT basic_currency_pkey PRIMARY KEY (id);


--
-- Name: basic_division_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_division
    ADD CONSTRAINT basic_division_pkey PRIMARY KEY (id);


--
-- Name: basic_parameter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_parameter
    ADD CONSTRAINT basic_parameter_pkey PRIMARY KEY (id);


--
-- Name: basic_product_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_product
    ADD CONSTRAINT basic_product_pkey PRIMARY KEY (id);


--
-- Name: basic_shift_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_shift
    ADD CONSTRAINT basic_shift_pkey PRIMARY KEY (id);


--
-- Name: basic_shifttimetableexception_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_shifttimetableexception
    ADD CONSTRAINT basic_shifttimetableexception_pkey PRIMARY KEY (id);


--
-- Name: basic_staff_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_staff
    ADD CONSTRAINT basic_staff_pkey PRIMARY KEY (id);


--
-- Name: basic_substitute_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_substitute
    ADD CONSTRAINT basic_substitute_pkey PRIMARY KEY (id);


--
-- Name: basic_substitutecomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_substitutecomponent
    ADD CONSTRAINT basic_substitutecomponent_pkey PRIMARY KEY (id);


--
-- Name: basic_workstationtype_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_workstationtype
    ADD CONSTRAINT basic_workstationtype_pkey PRIMARY KEY (id);


--
-- Name: basicproductioncounting_basicproductioncounting_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basicproductioncounting_basicproductioncounting
    ADD CONSTRAINT basicproductioncounting_basicproductioncounting_pkey PRIMARY KEY (id);


--
-- Name: costcalculation_costcalculation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY costcalculation_costcalculation
    ADD CONSTRAINT costcalculation_costcalculation_pkey PRIMARY KEY (id);


--
-- Name: costnormsforoperation_calculationoperationcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY costnormsforoperation_calculationoperationcomponent
    ADD CONSTRAINT costnormsforoperation_calculationoperationcomponent_pkey PRIMARY KEY (id);


--
-- Name: costnormsforproduct_orderoperationproductincomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY costnormsforproduct_orderoperationproductincomponent
    ADD CONSTRAINT costnormsforproduct_orderoperationproductincomponent_pkey PRIMARY KEY (id);


--
-- Name: genealogies_currentattribute_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY genealogies_currentattribute
    ADD CONSTRAINT genealogies_currentattribute_pkey PRIMARY KEY (id);


--
-- Name: genealogies_genealogy_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY genealogies_genealogy
    ADD CONSTRAINT genealogies_genealogy_pkey PRIMARY KEY (id);


--
-- Name: genealogies_otherfeature_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY genealogies_otherfeature
    ADD CONSTRAINT genealogies_otherfeature_pkey PRIMARY KEY (id);


--
-- Name: genealogies_postfeature_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY genealogies_postfeature
    ADD CONSTRAINT genealogies_postfeature_pkey PRIMARY KEY (id);


--
-- Name: genealogies_shiftfeature_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY genealogies_shiftfeature
    ADD CONSTRAINT genealogies_shiftfeature_pkey PRIMARY KEY (id);


--
-- Name: genealogiesforcomponents_genealogyproductincomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY genealogiesforcomponents_genealogyproductincomponent
    ADD CONSTRAINT genealogiesforcomponents_genealogyproductincomponent_pkey PRIMARY KEY (id);


--
-- Name: genealogiesforcomponents_productinbatch_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY genealogiesforcomponents_productinbatch
    ADD CONSTRAINT genealogiesforcomponents_productinbatch_pkey PRIMARY KEY (id);


--
-- Name: jointable_materialrequirement_order_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY jointable_materialrequirement_order
    ADD CONSTRAINT jointable_materialrequirement_order_pkey PRIMARY KEY (materialrequirement_id, order_id);


--
-- Name: jointable_order_workplan_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY jointable_order_workplan
    ADD CONSTRAINT jointable_order_workplan_pkey PRIMARY KEY (workplan_id, order_id);


--
-- Name: materialflow_materialsinstockareas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflow_materialsinstockareas
    ADD CONSTRAINT materialflow_materialsinstockareas_pkey PRIMARY KEY (id);


--
-- Name: materialflow_materialsinstockareascomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflow_materialsinstockareascomponent
    ADD CONSTRAINT materialflow_materialsinstockareascomponent_pkey PRIMARY KEY (id);


--
-- Name: materialflow_stockareas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflow_stockareas
    ADD CONSTRAINT materialflow_stockareas_pkey PRIMARY KEY (id);


--
-- Name: materialflow_stockcorrection_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflow_stockcorrection
    ADD CONSTRAINT materialflow_stockcorrection_pkey PRIMARY KEY (id);


--
-- Name: materialflow_transfer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT materialflow_transfer_pkey PRIMARY KEY (id);


--
-- Name: materialflow_transformations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflow_transformations
    ADD CONSTRAINT materialflow_transformations_pkey PRIMARY KEY (id);


--
-- Name: materialrequirements_materialrequirement_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialrequirements_materialrequirement
    ADD CONSTRAINT materialrequirements_materialrequirement_pkey PRIMARY KEY (id);


--
-- Name: ordergroups_ordergroup_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ordergroups_ordergroup
    ADD CONSTRAINT ordergroups_ordergroup_pkey PRIMARY KEY (id);


--
-- Name: orders_logging_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY orders_logging
    ADD CONSTRAINT orders_logging_pkey PRIMARY KEY (id);


--
-- Name: orders_order_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY orders_order
    ADD CONSTRAINT orders_order_pkey PRIMARY KEY (id);


--
-- Name: productioncounting_balanceoperationproductincomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncounting_balanceoperationproductincomponent
    ADD CONSTRAINT productioncounting_balanceoperationproductincomponent_pkey PRIMARY KEY (id);


--
-- Name: productioncounting_balanceoperationproductoutcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncounting_balanceoperationproductoutcomponent
    ADD CONSTRAINT productioncounting_balanceoperationproductoutcomponent_pkey PRIMARY KEY (id);


--
-- Name: productioncounting_operationtimecomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncounting_operationtimecomponent
    ADD CONSTRAINT productioncounting_operationtimecomponent_pkey PRIMARY KEY (id);


--
-- Name: productioncounting_productionbalance_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncounting_productionbalance
    ADD CONSTRAINT productioncounting_productionbalance_pkey PRIMARY KEY (id);


--
-- Name: productioncounting_productioncounting_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncounting_productioncounting
    ADD CONSTRAINT productioncounting_productioncounting_pkey PRIMARY KEY (id);


--
-- Name: productioncounting_productionrecord_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncounting_productionrecord
    ADD CONSTRAINT productioncounting_productionrecord_pkey PRIMARY KEY (id);


--
-- Name: productioncounting_productionrecordlogging_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncounting_productionrecordlogging
    ADD CONSTRAINT productioncounting_productionrecordlogging_pkey PRIMARY KEY (id);


--
-- Name: productioncounting_recordoperationproductincomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncounting_recordoperationproductincomponent
    ADD CONSTRAINT productioncounting_recordoperationproductincomponent_pkey PRIMARY KEY (id);


--
-- Name: productioncounting_recordoperationproductoutcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncounting_recordoperationproductoutcomponent
    ADD CONSTRAINT productioncounting_recordoperationproductoutcomponent_pkey PRIMARY KEY (id);


--
-- Name: productioncountingwithcosts_operationcostcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncountingwithcosts_operationcostcomponent
    ADD CONSTRAINT productioncountingwithcosts_operationcostcomponent_pkey PRIMARY KEY (id);


--
-- Name: productioncountingwithcosts_orderoperationproductincompone_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncountingwithcosts_orderoperationproductincomponent
    ADD CONSTRAINT productioncountingwithcosts_orderoperationproductincompone_pkey PRIMARY KEY (id);


--
-- Name: productionscheduling_orderoperationcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productionscheduling_orderoperationcomponent
    ADD CONSTRAINT productionscheduling_orderoperationcomponent_pkey PRIMARY KEY (id);


--
-- Name: qcadoomodel_dictionary_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qcadoomodel_dictionary
    ADD CONSTRAINT qcadoomodel_dictionary_pkey PRIMARY KEY (id);


--
-- Name: qcadoomodel_dictionaryitem_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qcadoomodel_dictionaryitem
    ADD CONSTRAINT qcadoomodel_dictionaryitem_pkey PRIMARY KEY (id);


--
-- Name: qcadooplugin_plugin_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qcadooplugin_plugin
    ADD CONSTRAINT qcadooplugin_plugin_pkey PRIMARY KEY (id);


--
-- Name: qcadoosecurity_persistenttoken_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qcadoosecurity_persistenttoken
    ADD CONSTRAINT qcadoosecurity_persistenttoken_pkey PRIMARY KEY (id);


--
-- Name: qcadoosecurity_user_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qcadoosecurity_user
    ADD CONSTRAINT qcadoosecurity_user_pkey PRIMARY KEY (id);


--
-- Name: qcadooview_category_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qcadooview_category
    ADD CONSTRAINT qcadooview_category_pkey PRIMARY KEY (id);


--
-- Name: qcadooview_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qcadooview_item
    ADD CONSTRAINT qcadooview_item_pkey PRIMARY KEY (id);


--
-- Name: qcadooview_view_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qcadooview_view
    ADD CONSTRAINT qcadooview_view_pkey PRIMARY KEY (id);


--
-- Name: qualitycontrols_qualitycontrol_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qualitycontrols_qualitycontrol
    ADD CONSTRAINT qualitycontrols_qualitycontrol_pkey PRIMARY KEY (id);


--
-- Name: simplematerialbalance_simplematerialbalance_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY simplematerialbalance_simplematerialbalance
    ADD CONSTRAINT simplematerialbalance_simplematerialbalance_pkey PRIMARY KEY (id);


--
-- Name: simplematerialbalance_simplematerialbalanceorderscomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY simplematerialbalance_simplematerialbalanceorderscomponent
    ADD CONSTRAINT simplematerialbalance_simplematerialbalanceorderscomponent_pkey PRIMARY KEY (id);


--
-- Name: simplematerialbalance_simplematerialbalancestockareascompo_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY simplematerialbalance_simplematerialbalancestockareascomponent
    ADD CONSTRAINT simplematerialbalance_simplematerialbalancestockareascompo_pkey PRIMARY KEY (id);


--
-- Name: stoppage_stoppage_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY stoppage_stoppage
    ADD CONSTRAINT stoppage_stoppage_pkey PRIMARY KEY (id);


--
-- Name: technologies_logging_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY technologies_logging
    ADD CONSTRAINT technologies_logging_pkey PRIMARY KEY (id);


--
-- Name: technologies_operation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY technologies_operation
    ADD CONSTRAINT technologies_operation_pkey PRIMARY KEY (id);


--
-- Name: technologies_operationproductincomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY technologies_operationproductincomponent
    ADD CONSTRAINT technologies_operationproductincomponent_pkey PRIMARY KEY (id);


--
-- Name: technologies_operationproductoutcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY technologies_operationproductoutcomponent
    ADD CONSTRAINT technologies_operationproductoutcomponent_pkey PRIMARY KEY (id);


--
-- Name: technologies_technology_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY technologies_technology
    ADD CONSTRAINT technologies_technology_pkey PRIMARY KEY (id);


--
-- Name: technologies_technologyoperationcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY technologies_technologyoperationcomponent
    ADD CONSTRAINT technologies_technologyoperationcomponent_pkey PRIMARY KEY (id);


--
-- Name: workplans_columnforinputproducts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY workplans_columnforinputproducts
    ADD CONSTRAINT workplans_columnforinputproducts_pkey PRIMARY KEY (id);


--
-- Name: workplans_columnfororders_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY workplans_columnfororders
    ADD CONSTRAINT workplans_columnfororders_pkey PRIMARY KEY (id);


--
-- Name: workplans_columnforoutputproducts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY workplans_columnforoutputproducts
    ADD CONSTRAINT workplans_columnforoutputproducts_pkey PRIMARY KEY (id);


--
-- Name: workplans_operationinputcolumn_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY workplans_operationinputcolumn
    ADD CONSTRAINT workplans_operationinputcolumn_pkey PRIMARY KEY (id);


--
-- Name: workplans_operationoutputcolumn_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY workplans_operationoutputcolumn
    ADD CONSTRAINT workplans_operationoutputcolumn_pkey PRIMARY KEY (id);


--
-- Name: workplans_orderoperationinputcolumn_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY workplans_orderoperationinputcolumn
    ADD CONSTRAINT workplans_orderoperationinputcolumn_pkey PRIMARY KEY (id);


--
-- Name: workplans_orderoperationoutputcolumn_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY workplans_orderoperationoutputcolumn
    ADD CONSTRAINT workplans_orderoperationoutputcolumn_pkey PRIMARY KEY (id);


--
-- Name: workplans_parameterinputcolumn_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY workplans_parameterinputcolumn
    ADD CONSTRAINT workplans_parameterinputcolumn_pkey PRIMARY KEY (id);


--
-- Name: workplans_parameterordercolumn_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY workplans_parameterordercolumn
    ADD CONSTRAINT workplans_parameterordercolumn_pkey PRIMARY KEY (id);


--
-- Name: workplans_parameteroutputcolumn_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY workplans_parameteroutputcolumn
    ADD CONSTRAINT workplans_parameteroutputcolumn_pkey PRIMARY KEY (id);


--
-- Name: workplans_technologyoperationinputcolumn_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY workplans_technologyoperationinputcolumn
    ADD CONSTRAINT workplans_technologyoperationinputcolumn_pkey PRIMARY KEY (id);


--
-- Name: workplans_technologyoperationoutputcolumn_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY workplans_technologyoperationoutputcolumn
    ADD CONSTRAINT workplans_technologyoperationoutputcolumn_pkey PRIMARY KEY (id);


--
-- Name: workplans_workplan_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY workplans_workplan
    ADD CONSTRAINT workplans_workplan_pkey PRIMARY KEY (id);


--
-- Name: workplans_workplanordercolumn_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY workplans_workplanordercolumn
    ADD CONSTRAINT workplans_workplanordercolumn_pkey PRIMARY KEY (id);


--
-- Name: fk1e593aac154b936c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_technologyoperationoutputcolumn
    ADD CONSTRAINT fk1e593aac154b936c FOREIGN KEY (technologyoperationcomponent_id) REFERENCES technologies_technologyoperationcomponent(id);


--
-- Name: fk1e593aacf3551292; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_technologyoperationoutputcolumn
    ADD CONSTRAINT fk1e593aacf3551292 FOREIGN KEY (columnforoutputproducts_id) REFERENCES workplans_columnforoutputproducts(id);


--
-- Name: fk2337e2f7574cfe41; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technologyoperationcomponent
    ADD CONSTRAINT fk2337e2f7574cfe41 FOREIGN KEY (referencetechnology_id) REFERENCES technologies_technology(id);


--
-- Name: fk2337e2f7b1e1a8a8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technologyoperationcomponent
    ADD CONSTRAINT fk2337e2f7b1e1a8a8 FOREIGN KEY (operation_id) REFERENCES technologies_operation(id);


--
-- Name: fk2337e2f7b4851f44; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technologyoperationcomponent
    ADD CONSTRAINT fk2337e2f7b4851f44 FOREIGN KEY (parent_id) REFERENCES technologies_technologyoperationcomponent(id);


--
-- Name: fk2337e2f7e3afcbac; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technologyoperationcomponent
    ADD CONSTRAINT fk2337e2f7e3afcbac FOREIGN KEY (technology_id) REFERENCES technologies_technology(id);


--
-- Name: fk29b24045f52c9469; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogiesforcomponents_productinbatch
    ADD CONSTRAINT fk29b24045f52c9469 FOREIGN KEY (productincomponent_id) REFERENCES genealogiesforcomponents_genealogyproductincomponent(id);


--
-- Name: fk2c25dcf1ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costnormsforproduct_orderoperationproductincomponent
    ADD CONSTRAINT fk2c25dcf1ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fk2c25dcf1b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costnormsforproduct_orderoperationproductincomponent
    ADD CONSTRAINT fk2c25dcf1b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fk2c8b41f6ac0386c6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogies_postfeature
    ADD CONSTRAINT fk2c8b41f6ac0386c6 FOREIGN KEY (genealogy_id) REFERENCES genealogies_genealogy(id);


--
-- Name: fk2e265efd479bb3a8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_parameterinputcolumn
    ADD CONSTRAINT fk2e265efd479bb3a8 FOREIGN KEY (parameter_id) REFERENCES basic_parameter(id);


--
-- Name: fk2e265efdb18b142; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_parameterinputcolumn
    ADD CONSTRAINT fk2e265efdb18b142 FOREIGN KEY (columnforinputproducts_id) REFERENCES workplans_columnforinputproducts(id);


--
-- Name: fk31da647fb64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY stoppage_stoppage
    ADD CONSTRAINT fk31da647fb64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fk3daecd745d8d5ea8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY orders_order
    ADD CONSTRAINT fk3daecd745d8d5ea8 FOREIGN KEY (company_id) REFERENCES basic_company(id);


--
-- Name: fk3daecd74ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY orders_order
    ADD CONSTRAINT fk3daecd74ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fk3daecd74c7f35254; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY orders_order
    ADD CONSTRAINT fk3daecd74c7f35254 FOREIGN KEY (ordergroup_id) REFERENCES ordergroups_ordergroup(id);


--
-- Name: fk3daecd74e3afcbac; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY orders_order
    ADD CONSTRAINT fk3daecd74e3afcbac FOREIGN KEY (technology_id) REFERENCES technologies_technology(id);


--
-- Name: fk41206acab17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_logging
    ADD CONSTRAINT fk41206acab17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- Name: fk41206acae3afcbac; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_logging
    ADD CONSTRAINT fk41206acae3afcbac FOREIGN KEY (technology_id) REFERENCES technologies_technology(id);


--
-- Name: fk47ea461210813cc8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_operation
    ADD CONSTRAINT fk47ea461210813cc8 FOREIGN KEY (workstationtype_id) REFERENCES basic_workstationtype(id);


--
-- Name: fk4964c163b17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogies_currentattribute
    ADD CONSTRAINT fk4964c163b17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- Name: fk510629c1ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technology
    ADD CONSTRAINT fk510629c1ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fk5ac920f5ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basicproductioncounting_basicproductioncounting
    ADD CONSTRAINT fk5ac920f5ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fk5ac920f5b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basicproductioncounting_basicproductioncounting
    ADD CONSTRAINT fk5ac920f5b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fk5bdc58bbad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_recordoperationproductoutcomponent
    ADD CONSTRAINT fk5bdc58bbad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fk5bdc58bbfeff14c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_recordoperationproductoutcomponent
    ADD CONSTRAINT fk5bdc58bbfeff14c FOREIGN KEY (productionrecord_id) REFERENCES productioncounting_productionrecord(id);


--
-- Name: fk5d2719fd10813cc8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionrecord
    ADD CONSTRAINT fk5d2719fd10813cc8 FOREIGN KEY (workstationtype_id) REFERENCES basic_workstationtype(id);


--
-- Name: fk5d2719fd1e9fcb48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionrecord
    ADD CONSTRAINT fk5d2719fd1e9fcb48 FOREIGN KEY (staff_id) REFERENCES basic_staff(id);


--
-- Name: fk5d2719fd7728be4c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionrecord
    ADD CONSTRAINT fk5d2719fd7728be4c FOREIGN KEY (division_id) REFERENCES basic_division(id);


--
-- Name: fk5d2719fd7f0f0b28; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionrecord
    ADD CONSTRAINT fk5d2719fd7f0f0b28 FOREIGN KEY (orderoperationcomponent_id) REFERENCES productionscheduling_orderoperationcomponent(id);


--
-- Name: fk5d2719fdb17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionrecord
    ADD CONSTRAINT fk5d2719fdb17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- Name: fk5d2719fdb64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionrecord
    ADD CONSTRAINT fk5d2719fdb64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fk5d2c0c861533ae2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY jointable_order_workplan
    ADD CONSTRAINT fk5d2c0c861533ae2 FOREIGN KEY (workplan_id) REFERENCES workplans_workplan(id);


--
-- Name: fk5d2c0c86b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY jointable_order_workplan
    ADD CONSTRAINT fk5d2c0c86b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fk5f6c432154b936c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productionscheduling_orderoperationcomponent
    ADD CONSTRAINT fk5f6c432154b936c FOREIGN KEY (technologyoperationcomponent_id) REFERENCES technologies_technologyoperationcomponent(id);


--
-- Name: fk5f6c43248833c02; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productionscheduling_orderoperationcomponent
    ADD CONSTRAINT fk5f6c43248833c02 FOREIGN KEY (parent_id) REFERENCES productionscheduling_orderoperationcomponent(id);


--
-- Name: fk5f6c432b1e1a8a8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productionscheduling_orderoperationcomponent
    ADD CONSTRAINT fk5f6c432b1e1a8a8 FOREIGN KEY (operation_id) REFERENCES technologies_operation(id);


--
-- Name: fk5f6c432b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productionscheduling_orderoperationcomponent
    ADD CONSTRAINT fk5f6c432b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fk5f6c432e3afcbac; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productionscheduling_orderoperationcomponent
    ADD CONSTRAINT fk5f6c432e3afcbac FOREIGN KEY (technology_id) REFERENCES technologies_technology(id);


--
-- Name: fk686fa361b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogies_genealogy
    ADD CONSTRAINT fk686fa361b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fk6bb812367f0f0b28; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_orderoperationoutputcolumn
    ADD CONSTRAINT fk6bb812367f0f0b28 FOREIGN KEY (orderoperationcomponent_id) REFERENCES productionscheduling_orderoperationcomponent(id);


--
-- Name: fk6bb81236f3551292; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_orderoperationoutputcolumn
    ADD CONSTRAINT fk6bb81236f3551292 FOREIGN KEY (columnforoutputproducts_id) REFERENCES workplans_columnforoutputproducts(id);


--
-- Name: fk6c887326308f12ec; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_materialsinstockareascomponent
    ADD CONSTRAINT fk6c887326308f12ec FOREIGN KEY (stockareas_id) REFERENCES materialflow_stockareas(id);


--
-- Name: fk6c887326d11277a8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_materialsinstockareascomponent
    ADD CONSTRAINT fk6c887326d11277a8 FOREIGN KEY (materialsinstockareas_id) REFERENCES materialflow_materialsinstockareas(id);


--
-- Name: fk71357f46308f12ec; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY simplematerialbalance_simplematerialbalancestockareascomponent
    ADD CONSTRAINT fk71357f46308f12ec FOREIGN KEY (stockareas_id) REFERENCES materialflow_stockareas(id);


--
-- Name: fk71357f464525613e; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY simplematerialbalance_simplematerialbalancestockareascomponent
    ADD CONSTRAINT fk71357f464525613e FOREIGN KEY (simplematerialbalance_id) REFERENCES simplematerialbalance_simplematerialbalance(id);


--
-- Name: fk77e41fdc7f0f0b28; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_operationtimecomponent
    ADD CONSTRAINT fk77e41fdc7f0f0b28 FOREIGN KEY (orderoperationcomponent_id) REFERENCES productionscheduling_orderoperationcomponent(id);


--
-- Name: fk77e41fdcbf1b2d48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_operationtimecomponent
    ADD CONSTRAINT fk77e41fdcbf1b2d48 FOREIGN KEY (productionbalance_id) REFERENCES productioncounting_productionbalance(id);


--
-- Name: fk78f5fa302f1f24c8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY jointable_materialrequirement_order
    ADD CONSTRAINT fk78f5fa302f1f24c8 FOREIGN KEY (materialrequirement_id) REFERENCES materialrequirements_materialrequirement(id);


--
-- Name: fk78f5fa30b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY jointable_materialrequirement_order
    ADD CONSTRAINT fk78f5fa30b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fk79747e2b154b936c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_technologyoperationinputcolumn
    ADD CONSTRAINT fk79747e2b154b936c FOREIGN KEY (technologyoperationcomponent_id) REFERENCES technologies_technologyoperationcomponent(id);


--
-- Name: fk79747e2bb18b142; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_technologyoperationinputcolumn
    ADD CONSTRAINT fk79747e2bb18b142 FOREIGN KEY (columnforinputproducts_id) REFERENCES workplans_columnforinputproducts(id);


--
-- Name: fk79b7ec6ca29938f8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_operationproductoutcomponent
    ADD CONSTRAINT fk79b7ec6ca29938f8 FOREIGN KEY (operationcomponent_id) REFERENCES technologies_technologyoperationcomponent(id);


--
-- Name: fk79b7ec6cad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_operationproductoutcomponent
    ADD CONSTRAINT fk79b7ec6cad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fk89ba7c08b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qualitycontrols_qualitycontrol
    ADD CONSTRAINT fk89ba7c08b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fk89ba7c08bcc5da25; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qualitycontrols_qualitycontrol
    ADD CONSTRAINT fk89ba7c08bcc5da25 FOREIGN KEY (operation_id) REFERENCES productionscheduling_orderoperationcomponent(id);


--
-- Name: fk8c32fdf51e9fcb48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT fk8c32fdf51e9fcb48 FOREIGN KEY (staff_id) REFERENCES basic_staff(id);


--
-- Name: fk8c32fdf5403d0e8f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT fk8c32fdf5403d0e8f FOREIGN KEY (transformationsproduction_id) REFERENCES materialflow_transformations(id);


--
-- Name: fk8c32fdf5ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT fk8c32fdf5ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fk8c32fdf5b0a8fa91; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT fk8c32fdf5b0a8fa91 FOREIGN KEY (stockareasto_id) REFERENCES materialflow_stockareas(id);


--
-- Name: fk8c32fdf5b2277b02; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT fk8c32fdf5b2277b02 FOREIGN KEY (stockareasfrom_id) REFERENCES materialflow_stockareas(id);


--
-- Name: fk8c32fdf5d8bb7bc1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT fk8c32fdf5d8bb7bc1 FOREIGN KEY (transformationsconsumption_id) REFERENCES materialflow_transformations(id);


--
-- Name: fk8c778d617f0f0b28; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_orderoperationinputcolumn
    ADD CONSTRAINT fk8c778d617f0f0b28 FOREIGN KEY (orderoperationcomponent_id) REFERENCES productionscheduling_orderoperationcomponent(id);


--
-- Name: fk8c778d61b18b142; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_orderoperationinputcolumn
    ADD CONSTRAINT fk8c778d61b18b142 FOREIGN KEY (columnforinputproducts_id) REFERENCES workplans_columnforinputproducts(id);


--
-- Name: fk8f123e06ac0386c6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogies_otherfeature
    ADD CONSTRAINT fk8f123e06ac0386c6 FOREIGN KEY (genealogy_id) REFERENCES genealogies_genealogy(id);


--
-- Name: fk95347f3fb17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_shifttimetableexception
    ADD CONSTRAINT fk95347f3fb17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- Name: fk96f3c32dad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncountingwithcosts_orderoperationproductincomponent
    ADD CONSTRAINT fk96f3c32dad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fk96f3c32dbf1b2d48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncountingwithcosts_orderoperationproductincomponent
    ADD CONSTRAINT fk96f3c32dbf1b2d48 FOREIGN KEY (productionbalance_id) REFERENCES productioncounting_productionbalance(id);


--
-- Name: fk9b37ca9470d83278; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qcadoomodel_dictionaryitem
    ADD CONSTRAINT fk9b37ca9470d83278 FOREIGN KEY (dictionary_id) REFERENCES qcadoomodel_dictionary(id);


--
-- Name: fk9ee8f8914525613e; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY simplematerialbalance_simplematerialbalanceorderscomponent
    ADD CONSTRAINT fk9ee8f8914525613e FOREIGN KEY (simplematerialbalance_id) REFERENCES simplematerialbalance_simplematerialbalance(id);


--
-- Name: fk9ee8f891b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY simplematerialbalance_simplematerialbalanceorderscomponent
    ADD CONSTRAINT fk9ee8f891b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fka193db81479bb3a8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_parameterordercolumn
    ADD CONSTRAINT fka193db81479bb3a8 FOREIGN KEY (parameter_id) REFERENCES basic_parameter(id);


--
-- Name: fka193db81f2989672; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_parameterordercolumn
    ADD CONSTRAINT fka193db81f2989672 FOREIGN KEY (columnfororders_id) REFERENCES workplans_columnfororders(id);


--
-- Name: fka223986cad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_recordoperationproductincomponent
    ADD CONSTRAINT fka223986cad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fka223986cfeff14c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_recordoperationproductincomponent
    ADD CONSTRAINT fka223986cfeff14c FOREIGN KEY (productionrecord_id) REFERENCES productioncounting_productionrecord(id);


--
-- Name: fka83409401e9fcb48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transformations
    ADD CONSTRAINT fka83409401e9fcb48 FOREIGN KEY (staff_id) REFERENCES basic_staff(id);


--
-- Name: fka8340940b0a8fa91; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transformations
    ADD CONSTRAINT fka8340940b0a8fa91 FOREIGN KEY (stockareasto_id) REFERENCES materialflow_stockareas(id);


--
-- Name: fka8340940b2277b02; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transformations
    ADD CONSTRAINT fka8340940b2277b02 FOREIGN KEY (stockareasfrom_id) REFERENCES materialflow_stockareas(id);


--
-- Name: fkb39e4a9ba29938f8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_operationproductincomponent
    ADD CONSTRAINT fkb39e4a9ba29938f8 FOREIGN KEY (operationcomponent_id) REFERENCES technologies_technologyoperationcomponent(id);


--
-- Name: fkb39e4a9bad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_operationproductincomponent
    ADD CONSTRAINT fkb39e4a9bad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fkb70ac547cbfd0b9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogies_shiftfeature
    ADD CONSTRAINT fkb70ac547cbfd0b9 FOREIGN KEY (value_id) REFERENCES basic_shift(id);


--
-- Name: fkb70ac54ac0386c6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogies_shiftfeature
    ADD CONSTRAINT fkb70ac54ac0386c6 FOREIGN KEY (genealogy_id) REFERENCES genealogies_genealogy(id);


--
-- Name: fkbebf5d4bad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_substitute
    ADD CONSTRAINT fkbebf5d4bad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fkbf24a028154b936c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costnormsforoperation_calculationoperationcomponent
    ADD CONSTRAINT fkbf24a028154b936c FOREIGN KEY (technologyoperationcomponent_id) REFERENCES technologies_technologyoperationcomponent(id);


--
-- Name: fkbf24a0282ee8598c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costnormsforoperation_calculationoperationcomponent
    ADD CONSTRAINT fkbf24a0282ee8598c FOREIGN KEY (costcalculation_id) REFERENCES costcalculation_costcalculation(id);


--
-- Name: fkbf24a028b1e1a8a8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costnormsforoperation_calculationoperationcomponent
    ADD CONSTRAINT fkbf24a028b1e1a8a8 FOREIGN KEY (operation_id) REFERENCES technologies_operation(id);


--
-- Name: fkbf24a028bf1b2d48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costnormsforoperation_calculationoperationcomponent
    ADD CONSTRAINT fkbf24a028bf1b2d48 FOREIGN KEY (productionbalance_id) REFERENCES productioncounting_productionbalance(id);


--
-- Name: fkbf24a028eeb36669; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costnormsforoperation_calculationoperationcomponent
    ADD CONSTRAINT fkbf24a028eeb36669 FOREIGN KEY (parent_id) REFERENCES costnormsforoperation_calculationoperationcomponent(id);


--
-- Name: fkbf47c580ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_balanceoperationproductoutcomponent
    ADD CONSTRAINT fkbf47c580ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fkbf47c580bf1b2d48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_balanceoperationproductoutcomponent
    ADD CONSTRAINT fkbf47c580bf1b2d48 FOREIGN KEY (productionbalance_id) REFERENCES productioncounting_productionbalance(id);


--
-- Name: fkbf47c580feff14c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_balanceoperationproductoutcomponent
    ADD CONSTRAINT fkbf47c580feff14c FOREIGN KEY (productionrecord_id) REFERENCES productioncounting_productionrecord(id);


--
-- Name: fkc93f6b1fad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productioncounting
    ADD CONSTRAINT fkc93f6b1fad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fkc93f6b1fb64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productioncounting
    ADD CONSTRAINT fkc93f6b1fb64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fkc9d449ca1e9fcb48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_stockcorrection
    ADD CONSTRAINT fkc9d449ca1e9fcb48 FOREIGN KEY (staff_id) REFERENCES basic_staff(id);


--
-- Name: fkc9d449ca308f12ec; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_stockcorrection
    ADD CONSTRAINT fkc9d449ca308f12ec FOREIGN KEY (stockareas_id) REFERENCES materialflow_stockareas(id);


--
-- Name: fkc9d449caad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_stockcorrection
    ADD CONSTRAINT fkc9d449caad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fkcbbea3f2717076ac; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_substitutecomponent
    ADD CONSTRAINT fkcbbea3f2717076ac FOREIGN KEY (substitute_id) REFERENCES basic_substitute(id);


--
-- Name: fkcbbea3f2ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_substitutecomponent
    ADD CONSTRAINT fkcbbea3f2ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fkcc305bf47f0f0b28; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncountingwithcosts_operationcostcomponent
    ADD CONSTRAINT fkcc305bf47f0f0b28 FOREIGN KEY (orderoperationcomponent_id) REFERENCES productionscheduling_orderoperationcomponent(id);


--
-- Name: fkcc305bf4bf1b2d48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncountingwithcosts_operationcostcomponent
    ADD CONSTRAINT fkcc305bf4bf1b2d48 FOREIGN KEY (productionbalance_id) REFERENCES productioncounting_productionbalance(id);


--
-- Name: fkcd94e8d8b1e1a8a8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_operationoutputcolumn
    ADD CONSTRAINT fkcd94e8d8b1e1a8a8 FOREIGN KEY (operation_id) REFERENCES technologies_operation(id);


--
-- Name: fkcd94e8d8f3551292; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_operationoutputcolumn
    ADD CONSTRAINT fkcd94e8d8f3551292 FOREIGN KEY (columnforoutputproducts_id) REFERENCES workplans_columnforoutputproducts(id);


--
-- Name: fkcfbe2739ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costcalculation_costcalculation
    ADD CONSTRAINT fkcfbe2739ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fkcfbe2739b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costcalculation_costcalculation
    ADD CONSTRAINT fkcfbe2739b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fkcfbe2739be57e70b; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costcalculation_costcalculation
    ADD CONSTRAINT fkcfbe2739be57e70b FOREIGN KEY (defaulttechnology_id) REFERENCES technologies_technology(id);


--
-- Name: fkcfbe2739e3afcbac; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costcalculation_costcalculation
    ADD CONSTRAINT fkcfbe2739e3afcbac FOREIGN KEY (technology_id) REFERENCES technologies_technology(id);


--
-- Name: fkd0aca6a41533ae2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_workplanordercolumn
    ADD CONSTRAINT fkd0aca6a41533ae2 FOREIGN KEY (workplan_id) REFERENCES workplans_workplan(id);


--
-- Name: fkd0aca6a4f2989672; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_workplanordercolumn
    ADD CONSTRAINT fkd0aca6a4f2989672 FOREIGN KEY (columnfororders_id) REFERENCES workplans_columnfororders(id);


--
-- Name: fkd14459ab80ff2f6f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogiesforcomponents_genealogyproductincomponent
    ADD CONSTRAINT fkd14459ab80ff2f6f FOREIGN KEY (productincomponent_id) REFERENCES technologies_operationproductincomponent(id);


--
-- Name: fkd14459abac0386c6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogiesforcomponents_genealogyproductincomponent
    ADD CONSTRAINT fkd14459abac0386c6 FOREIGN KEY (genealogy_id) REFERENCES genealogies_genealogy(id);


--
-- Name: fkd6e4ff07ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_balanceoperationproductincomponent
    ADD CONSTRAINT fkd6e4ff07ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fkd6e4ff07bf1b2d48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_balanceoperationproductincomponent
    ADD CONSTRAINT fkd6e4ff07bf1b2d48 FOREIGN KEY (productionbalance_id) REFERENCES productioncounting_productionbalance(id);


--
-- Name: fkd6e4ff07feff14c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_balanceoperationproductincomponent
    ADD CONSTRAINT fkd6e4ff07feff14c FOREIGN KEY (productionrecord_id) REFERENCES productioncounting_productionrecord(id);


--
-- Name: fke2345a7fb18b142; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_operationinputcolumn
    ADD CONSTRAINT fke2345a7fb18b142 FOREIGN KEY (columnforinputproducts_id) REFERENCES workplans_columnforinputproducts(id);


--
-- Name: fke2345a7fb1e1a8a8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_operationinputcolumn
    ADD CONSTRAINT fke2345a7fb1e1a8a8 FOREIGN KEY (operation_id) REFERENCES technologies_operation(id);


--
-- Name: fke3299f8f7728be4c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_staff
    ADD CONSTRAINT fke3299f8f7728be4c FOREIGN KEY (division_id) REFERENCES basic_division(id);


--
-- Name: fke3299f8fb17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_staff
    ADD CONSTRAINT fke3299f8fb17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- Name: fke98b2005b17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY orders_logging
    ADD CONSTRAINT fke98b2005b17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- Name: fke98b2005b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY orders_logging
    ADD CONSTRAINT fke98b2005b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fkecdc2ccc7728be4c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_workstationtype
    ADD CONSTRAINT fkecdc2ccc7728be4c FOREIGN KEY (division_id) REFERENCES basic_division(id);


--
-- Name: fkf0b0619e64280dc0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_division
    ADD CONSTRAINT fkf0b0619e64280dc0 FOREIGN KEY (supervisor_id) REFERENCES basic_staff(id);


--
-- Name: fkf2fd76b0ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionbalance
    ADD CONSTRAINT fkf2fd76b0ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fkf2fd76b0b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionbalance
    ADD CONSTRAINT fkf2fd76b0b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fkf2fd76b0e3afcbac; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionbalance
    ADD CONSTRAINT fkf2fd76b0e3afcbac FOREIGN KEY (technology_id) REFERENCES technologies_technology(id);


--
-- Name: fkf78382a2feff14c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionrecordlogging
    ADD CONSTRAINT fkf78382a2feff14c FOREIGN KEY (productionrecord_id) REFERENCES productioncounting_productionrecord(id);


--
-- Name: fkf7f1a0d8db69d3cc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_parameter
    ADD CONSTRAINT fkf7f1a0d8db69d3cc FOREIGN KEY (currency_id) REFERENCES basic_currency(id);


--
-- Name: fkf855759847760b8c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qcadooview_item
    ADD CONSTRAINT fkf855759847760b8c FOREIGN KEY (view_id) REFERENCES qcadooview_view(id);


--
-- Name: fkf85575986065f7ec; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qcadooview_item
    ADD CONSTRAINT fkf85575986065f7ec FOREIGN KEY (category_id) REFERENCES qcadooview_category(id);


--
-- Name: fkffe3741a479bb3a8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_parameteroutputcolumn
    ADD CONSTRAINT fkffe3741a479bb3a8 FOREIGN KEY (parameter_id) REFERENCES basic_parameter(id);


--
-- Name: fkffe3741af3551292; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_parameteroutputcolumn
    ADD CONSTRAINT fkffe3741af3551292 FOREIGN KEY (columnforoutputproducts_id) REFERENCES workplans_columnforoutputproducts(id);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

