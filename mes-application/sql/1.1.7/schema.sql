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
-- Name: assignmenttoshift_assignmenttoshift; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE assignmenttoshift_assignmenttoshift (
    id bigint NOT NULL,
    startdate date,
    shift_id bigint,
    state character varying(255) DEFAULT '01draft'::character varying,
    approvedattendancelist boolean
);


ALTER TABLE public.assignmenttoshift_assignmenttoshift OWNER TO postgres;

--
-- Name: assignmenttoshift_assignmenttoshiftreport; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE assignmenttoshift_assignmenttoshiftreport (
    id bigint NOT NULL,
    number character varying(1024),
    name character varying(1024),
    datefrom date,
    dateto date,
    shift_id bigint,
    filename character varying(255),
    generated boolean DEFAULT false,
    createdate timestamp without time zone,
    updatedate timestamp without time zone,
    createuser character varying(255),
    updateuser character varying(255)
);


ALTER TABLE public.assignmenttoshift_assignmenttoshiftreport OWNER TO postgres;

--
-- Name: assignmenttoshift_assignmenttoshiftstatechange; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE assignmenttoshift_assignmenttoshiftstatechange (
    id bigint NOT NULL,
    dateandtime timestamp without time zone,
    sourcestate character varying(255),
    targetstate character varying(255),
    status character varying(255),
    phase integer,
    worker character varying(255),
    assignmenttoshift_id bigint,
    shift_id bigint,
    additionalinformation character varying(255)
);


ALTER TABLE public.assignmenttoshift_assignmenttoshiftstatechange OWNER TO postgres;

--
-- Name: assignmenttoshift_staffassignmenttoshift; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE assignmenttoshift_staffassignmenttoshift (
    id bigint NOT NULL,
    assignmenttoshift_id bigint,
    worker_id bigint,
    productionline_id bigint,
    occupationtype character varying(255),
    occupationtypename character varying(255),
    state character varying(255) DEFAULT '01simple'::character varying,
    occupationtypeenum character varying(255),
    occupationtypevalueforgrid character varying(255)
);


ALTER TABLE public.assignmenttoshift_staffassignmenttoshift OWNER TO postgres;

--
-- Name: avglaborcostcalcfororder_assignmentworkertoshift; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE avglaborcostcalcfororder_assignmentworkertoshift (
    id bigint NOT NULL,
    worker_id bigint,
    assignmenttoshift_id bigint,
    avglaborcostcalcfororder_id bigint
);


ALTER TABLE public.avglaborcostcalcfororder_assignmentworkertoshift OWNER TO postgres;

--
-- Name: avglaborcostcalcfororder_avglaborcostcalcfororder; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE avglaborcostcalcfororder_avglaborcostcalcfororder (
    id bigint NOT NULL,
    startdate timestamp without time zone,
    finishdate timestamp without time zone,
    order_id bigint,
    productionline_id bigint,
    basedon character varying(255) DEFAULT '01assignment'::character varying,
    averagelaborhourlycost numeric(12,5)
);


ALTER TABLE public.avglaborcostcalcfororder_avglaborcostcalcfororder OWNER TO postgres;

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
    unit character varying(255),
    autogeneratequalitycontrol boolean,
    registerquantityoutproduct boolean,
    registerpiecework boolean,
    reasonneededwhenchangingstatetoabandoned boolean DEFAULT false,
    reasonneededwhendelayedeffectivedateto boolean DEFAULT false,
    reasonneededwhenchangingstatetodeclined boolean DEFAULT false,
    reasonneededwhenchangingstatetointerrupted boolean DEFAULT false,
    dontprintinputproductsinworkplans boolean,
    imageurlinworkplan character varying(255),
    dontprintoutputproductsinworkplans boolean,
    typeofproductionrecording character varying(255) DEFAULT '02cumulated'::character varying,
    hidetechnologyandorderinworkplans boolean,
    delayedeffectivedatefromtime integer DEFAULT 900,
    reasonneededwhencorrectingdatefrom boolean DEFAULT false,
    reasonneededwhenearliereffectivedateto boolean DEFAULT false,
    reasonneededwhendelayedeffectivedatefrom boolean DEFAULT false,
    autocloseorder boolean,
    earliereffectivedatefromtime integer DEFAULT 900,
    registerquantityinproduct boolean,
    delayedeffectivedatetotime integer DEFAULT 900,
    dontprintordersinworkplans boolean,
    hidedetailsinworkplans boolean,
    checkdoneorderforquality boolean,
    reasonneededwhenearliereffectivedatefrom boolean DEFAULT false,
    justone boolean,
    reasonneededwhencorrectingdateto boolean DEFAULT false,
    defaultproductionline_id bigint,
    allowtoclose boolean,
    sampleswereloaded boolean DEFAULT true,
    registerproductiontime boolean,
    batchfordoneorder character varying(255) DEFAULT '01none'::character varying,
    hidedescriptioninworkplans boolean,
    earliereffectivedatetotime integer DEFAULT 900
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
    lastpurchasecost numeric(12,5) DEFAULT 0::numeric,
    technologygroup_id bigint,
    nominalcost numeric(12,5) DEFAULT 0::numeric,
    batch character varying(255),
    lastusedbatch character varying(255),
    averagecost numeric(12,5) DEFAULT 0::numeric,
    costfornumber numeric(12,5) DEFAULT 1::numeric,
    genealogybatchreq boolean,
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
    email character varying(255),
    phone character varying(255),
    workfor_id bigint,
    post character varying(255),
    shift_id bigint,
    division_id bigint,
    determinedindividual boolean,
    individuallaborcost numeric(12,5),
    wagegroup_id bigint,
    laborhourlycost numeric(12,5),
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
    quantity numeric(9,5)
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
    plannedquantity numeric(12,5),
    usedquantity numeric(12,5),
    producedquantity numeric(12,5)
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
    productionline_id bigint,
    quantity numeric(12,5),
    order_id bigint,
    totalmaterialcosts numeric(12,5),
    totalmachinehourlycosts numeric(12,5),
    totalpieceworkcosts numeric(12,5),
    totallaborhourlycosts numeric(12,5),
    totaltechnicalproductioncosts numeric(12,5),
    productioncostmargin numeric(12,5) DEFAULT 0::numeric,
    productioncostmarginvalue numeric(12,5),
    materialcostmargin numeric(12,5) DEFAULT 0::numeric,
    materialcostmarginvalue numeric(12,5),
    additionaloverhead numeric(12,5) DEFAULT 0::numeric,
    additionaloverheadvalue numeric(12,5) DEFAULT 0::numeric,
    totaloverhead numeric(12,5),
    totalcosts numeric(12,5),
    totalcostperunit numeric(12,5),
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
-- Name: costnormsformaterials_technologyinstoperproductincomp; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE costnormsformaterials_technologyinstoperproductincomp (
    id bigint NOT NULL,
    order_id bigint,
    product_id bigint,
    costfornumber numeric(12,5) DEFAULT 1::numeric,
    nominalcost numeric(12,5) DEFAULT 0::numeric,
    lastpurchasecost numeric(12,5) DEFAULT 0::numeric,
    averagecost numeric(12,5) DEFAULT 0::numeric,
    costfororder numeric(12,5) DEFAULT 0::numeric
);


ALTER TABLE public.costnormsformaterials_technologyinstoperproductincomp OWNER TO postgres;

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
    areproductquantitiesdivisible boolean DEFAULT false,
    istjdivisible boolean DEFAULT false,
    machineutilization numeric(12,5),
    laborutilization numeric(12,5),
    productioninonecycle numeric(12,5) DEFAULT 1::numeric,
    countrealized character varying(255) DEFAULT '01all'::character varying,
    countmachine numeric(12,5),
    timenextoperation integer,
    operationoffset integer,
    effectiveoperationrealizationtime integer,
    effectivedatefrom timestamp without time zone,
    effectivedateto timestamp without time zone,
    duration integer DEFAULT 0,
    pieces numeric(12,5) DEFAULT 0::numeric,
    operationcost numeric(12,5) DEFAULT 0::numeric,
    operationmargincost numeric(12,5) DEFAULT 0::numeric,
    totaloperationcost numeric(12,5) DEFAULT 0::numeric,
    pieceworkcost numeric(12,5) DEFAULT 0::numeric,
    laborhourlycost numeric(12,5) DEFAULT 0::numeric,
    machinehourlycost numeric(12,5) DEFAULT 0::numeric,
    numberofoperations integer DEFAULT 1,
    costcalculation_id bigint,
    productionbalance_id bigint
);


ALTER TABLE public.costnormsforoperation_calculationoperationcomponent OWNER TO postgres;

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
    materialrequirement_id bigint NOT NULL,
    order_id bigint NOT NULL
);


ALTER TABLE public.jointable_materialrequirement_order OWNER TO postgres;

--
-- Name: jointable_order_workplan; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE jointable_order_workplan (
    workplan_id bigint NOT NULL,
    order_id bigint NOT NULL
);


ALTER TABLE public.jointable_order_workplan OWNER TO postgres;

--
-- Name: jointable_productionline_technology; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE jointable_productionline_technology (
    technology_id bigint NOT NULL,
    productionline_id bigint NOT NULL
);


ALTER TABLE public.jointable_productionline_technology OWNER TO postgres;

--
-- Name: jointable_productionline_technologygroup; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE jointable_productionline_technologygroup (
    technologygroup_id bigint NOT NULL,
    productionline_id bigint NOT NULL
);


ALTER TABLE public.jointable_productionline_technologygroup OWNER TO postgres;

--
-- Name: linechangeovernorms_linechangeovernorms; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE linechangeovernorms_linechangeovernorms (
    id bigint NOT NULL,
    number character varying(255),
    changeovertype character varying(255) DEFAULT '01forTechnology'::character varying,
    fromtechnology_id bigint,
    totechnology_id bigint,
    fromtechnologygroup_id bigint,
    totechnologygroup_id bigint,
    productionline_id bigint,
    duration integer
);


ALTER TABLE public.linechangeovernorms_linechangeovernorms OWNER TO postgres;

--
-- Name: materialflow_location; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflow_location (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(255),
    type character varying(255) DEFAULT '01controlPoint'::character varying
);


ALTER TABLE public.materialflow_location OWNER TO postgres;

--
-- Name: materialflow_materialsinlocation; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflow_materialsinlocation (
    id bigint NOT NULL,
    name character varying(1024),
    materialflowfordate timestamp without time zone,
    "time" timestamp without time zone,
    worker character varying(255),
    generated boolean,
    filename character varying(255),
    active boolean DEFAULT true
);


ALTER TABLE public.materialflow_materialsinlocation OWNER TO postgres;

--
-- Name: materialflow_materialsinlocationcomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflow_materialsinlocationcomponent (
    id bigint NOT NULL,
    materialsinlocation_id bigint,
    location_id bigint
);


ALTER TABLE public.materialflow_materialsinlocationcomponent OWNER TO postgres;

--
-- Name: materialflow_stockcorrection; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflow_stockcorrection (
    id bigint NOT NULL,
    number character varying(255),
    stockcorrectiondate timestamp without time zone,
    location_id bigint,
    product_id bigint,
    found numeric(12,5),
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
    locationfrom_id bigint,
    locationto_id bigint,
    product_id bigint,
    quantity numeric(12,5),
    staff_id bigint,
    transformationsconsumption_id bigint,
    transformationsproduction_id bigint,
    price numeric(12,5) DEFAULT 0::numeric
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
    locationfrom_id bigint,
    locationto_id bigint,
    staff_id bigint
);


ALTER TABLE public.materialflow_transformations OWNER TO postgres;

--
-- Name: materialflowmultitransfers_productquantity; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflowmultitransfers_productquantity (
    id bigint NOT NULL,
    product_id bigint,
    quantity numeric(12,5),
    transfer_id bigint
);


ALTER TABLE public.materialflowmultitransfers_productquantity OWNER TO postgres;

--
-- Name: materialflowmultitransfers_transfertemplate; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflowmultitransfers_transfertemplate (
    id bigint NOT NULL,
    locationfrom_id bigint,
    locationto_id bigint,
    product_id bigint
);


ALTER TABLE public.materialflowmultitransfers_transfertemplate OWNER TO postgres;

--
-- Name: materialflowresources_resource; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflowresources_resource (
    id bigint NOT NULL,
    location_id bigint,
    product_id bigint,
    quantity numeric(12,5),
    price numeric(12,5) DEFAULT 0::numeric,
    batch character varying(255),
    "time" timestamp without time zone
);


ALTER TABLE public.materialflowresources_resource OWNER TO postgres;

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
    plannedquantity numeric(10,5),
    donequantity numeric(10,5),
    effectivedatefrom timestamp without time zone,
    effectivedateto timestamp without time zone,
    externalnumber character varying(255),
    externalsynchronized boolean DEFAULT true,
    productionline_id bigint,
    deadline timestamp without time zone,
    correcteddatefrom timestamp without time zone,
    correcteddateto timestamp without time zone,
    reasontypecorrectiondatefrom character varying(255),
    reasontypecorrectiondateto character varying(255),
    commentreasontypecorrectiondatefrom character varying(255),
    commentreasontypecorrectiondateto character varying(255),
    startdate timestamp without time zone,
    finishdate timestamp without time zone,
    ordergroup_id bigint,
    ownlinechangeover boolean DEFAULT false,
    ordergroupname character varying(255),
    registerquantityoutproduct boolean,
    ownlinechangeoverduration integer,
    calculate boolean,
    justone boolean,
    realizationtime integer,
    generatedenddate timestamp without time zone,
    allowtoclose boolean,
    registerquantityinproduct boolean,
    operationdurationquantityunit character varying(255),
    typeofproductionrecording character varying(255),
    registerproductiontime boolean,
    autocloseorder boolean,
    registerpiecework boolean,
    active boolean DEFAULT true
);


ALTER TABLE public.orders_order OWNER TO postgres;

--
-- Name: orders_orderstatechange; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE orders_orderstatechange (
    id bigint NOT NULL,
    reasontype character varying(255),
    comment character varying(255),
    reasonrequired boolean DEFAULT false,
    dateandtime timestamp without time zone,
    sourcestate character varying(255),
    targetstate character varying(255),
    status character varying(255),
    phase integer,
    worker character varying(255),
    order_id bigint,
    shift_id bigint,
    additionalinformation character varying(255)
);


ALTER TABLE public.orders_orderstatechange OWNER TO postgres;

--
-- Name: productioncounting_balanceoperationproductincomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_balanceoperationproductincomponent (
    id bigint NOT NULL,
    productionbalance_id bigint,
    product_id bigint,
    plannedquantity numeric(10,5),
    usedquantity numeric(10,5),
    balance numeric(12,5)
);


ALTER TABLE public.productioncounting_balanceoperationproductincomponent OWNER TO postgres;

--
-- Name: productioncounting_balanceoperationproductoutcomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_balanceoperationproductoutcomponent (
    id bigint NOT NULL,
    productionbalance_id bigint,
    product_id bigint,
    plannedquantity numeric(10,5),
    usedquantity numeric(10,5),
    balance numeric(12,5)
);


ALTER TABLE public.productioncounting_balanceoperationproductoutcomponent OWNER TO postgres;

--
-- Name: productioncounting_operationpieceworkcomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_operationpieceworkcomponent (
    id bigint NOT NULL,
    productionbalance_id bigint,
    technologyinstanceoperationcomponent_id bigint,
    plannedcycles numeric(12,5),
    cycles numeric(12,5),
    cyclesbalance numeric(12,5)
);


ALTER TABLE public.productioncounting_operationpieceworkcomponent OWNER TO postgres;

--
-- Name: productioncounting_operationtimecomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_operationtimecomponent (
    id bigint NOT NULL,
    productionbalance_id bigint,
    technologyinstanceoperationcomponent_id bigint,
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
    quantity numeric(12,5),
    laborcostsbalance numeric(12,5),
    machinecosts numeric(12,5),
    materialcostmarginvalue numeric(12,5) DEFAULT 0::numeric,
    printcostnormsofmaterials boolean DEFAULT true,
    generatedwithcosts boolean,
    totalcostperunit numeric(12,5),
    plannedmachinecosts numeric(12,5),
    machinecostsbalance numeric(12,5),
    plannedcomponentscosts numeric(12,5),
    totaltechnicalproductioncostperunit numeric(12,5),
    plannedlaborcosts numeric(12,5),
    sourceofmaterialcosts character varying(255) DEFAULT '01currentGlobalDefinitionsInProduct'::character varying,
    laborcosts numeric(12,5),
    productioncostmargin numeric(12,5) DEFAULT 0::numeric,
    cyclescostsbalance numeric(12,5),
    balancetechnicalproductioncosts numeric(12,5),
    additionaloverhead numeric(12,5) DEFAULT 0::numeric,
    totalcosts numeric(12,5),
    componentscostsbalance numeric(12,5),
    averagemachinehourlycost numeric(12,5),
    productionline_id bigint,
    totaltechnicalproductioncosts numeric(12,5),
    calculatematerialcostsmode character varying(255) DEFAULT '01nominal'::character varying,
    balancetechnicalproductioncostperunit numeric(12,5),
    registeredtotaltechnicalproductioncostperunit numeric(12,5),
    plannedcyclescosts numeric(12,5),
    averagelaborhourlycost numeric(12,5),
    componentscosts numeric(12,5),
    productioncostmarginvalue numeric(12,5) DEFAULT 0::numeric,
    totaloverhead numeric(12,5),
    materialcostmargin numeric(12,5) DEFAULT 0::numeric,
    registeredtotaltechnicalproductioncosts numeric(12,5),
    additionaloverheadvalue numeric(12,5) DEFAULT 0::numeric,
    technology_id bigint,
    cyclescosts numeric(12,5),
    active boolean DEFAULT true
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
    technologyinstanceoperationcomponent_id bigint,
    shift_id bigint,
    state character varying(255) DEFAULT '01draft'::character varying,
    lastrecord boolean,
    machinetime integer,
    labortime integer,
    executedoperationcycles numeric(12,5),
    staff_id bigint,
    workstationtype_id bigint,
    division_id bigint,
    active boolean DEFAULT true
);


ALTER TABLE public.productioncounting_productionrecord OWNER TO postgres;

--
-- Name: productioncounting_productionrecordstatechange; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_productionrecordstatechange (
    id bigint NOT NULL,
    dateandtime timestamp without time zone,
    sourcestate character varying(255),
    targetstate character varying(255),
    status character varying(255),
    phase integer,
    worker character varying(255),
    productionrecord_id bigint,
    shift_id bigint
);


ALTER TABLE public.productioncounting_productionrecordstatechange OWNER TO postgres;

--
-- Name: productioncounting_recordoperationproductincomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_recordoperationproductincomponent (
    id bigint NOT NULL,
    productionrecord_id bigint,
    product_id bigint,
    plannedquantity numeric(10,5),
    usedquantity numeric(10,5),
    balance numeric(12,5)
);


ALTER TABLE public.productioncounting_recordoperationproductincomponent OWNER TO postgres;

--
-- Name: productioncounting_recordoperationproductoutcomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_recordoperationproductoutcomponent (
    id bigint NOT NULL,
    productionrecord_id bigint,
    product_id bigint,
    plannedquantity numeric(10,5),
    usedquantity numeric(10,5),
    balance numeric(12,5)
);


ALTER TABLE public.productioncounting_recordoperationproductoutcomponent OWNER TO postgres;

--
-- Name: productioncountingwithcosts_operationcostcomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncountingwithcosts_operationcostcomponent (
    id bigint NOT NULL,
    productionbalance_id bigint,
    technologyinstanceoperationcomponent_id bigint,
    plannedmachinecosts numeric(12,5),
    machinecosts numeric(12,5),
    machinecostsbalance numeric(12,5),
    plannedlaborcosts numeric(12,5),
    laborcosts numeric(12,5),
    laborcostsbalance numeric(12,5)
);


ALTER TABLE public.productioncountingwithcosts_operationcostcomponent OWNER TO postgres;

--
-- Name: productioncountingwithcosts_operationpieceworkcostcomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncountingwithcosts_operationpieceworkcostcomponent (
    id bigint NOT NULL,
    productionbalance_id bigint,
    technologyinstanceoperationcomponent_id bigint,
    plannedcyclescosts numeric(12,5),
    cyclescosts numeric(12,5),
    cyclescostsbalance numeric(12,5)
);


ALTER TABLE public.productioncountingwithcosts_operationpieceworkcostcomponent OWNER TO postgres;

--
-- Name: productioncountingwithcosts_technologyinstoperproductincomp; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncountingwithcosts_technologyinstoperproductincomp (
    id bigint NOT NULL,
    productionbalance_id bigint,
    product_id bigint,
    plannedcost numeric(12,5),
    registeredcost numeric(12,5),
    balance numeric(12,5)
);


ALTER TABLE public.productioncountingwithcosts_technologyinstoperproductincomp OWNER TO postgres;

--
-- Name: productionlines_productionline; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productionlines_productionline (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(2048),
    division_id bigint,
    place character varying(255),
    description character varying(2048),
    supportsalltechnologies boolean DEFAULT true,
    documentation character varying(255),
    supportsothertechnologiesworkstationtypes boolean DEFAULT true,
    quantityforotherworkstationtypes integer DEFAULT 1,
    active boolean DEFAULT true
);


ALTER TABLE public.productionlines_productionline OWNER TO postgres;

--
-- Name: productionlines_workstationtypecomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productionlines_workstationtypecomponent (
    id bigint NOT NULL,
    productionline_id bigint,
    workstationtype_id bigint,
    quantity integer DEFAULT 1
);


ALTER TABLE public.productionlines_workstationtypecomponent OWNER TO postgres;

--
-- Name: productionpershift_dailyprogress; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productionpershift_dailyprogress (
    id bigint NOT NULL,
    progressforday_id bigint,
    shift_id bigint,
    quantity numeric(12,5)
);


ALTER TABLE public.productionpershift_dailyprogress OWNER TO postgres;

--
-- Name: productionpershift_productionpershift; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productionpershift_productionpershift (
    id bigint NOT NULL,
    order_id bigint,
    plannedprogresscorrectiontype character varying(255),
    plannedprogresscorrectioncomment text
);


ALTER TABLE public.productionpershift_productionpershift OWNER TO postgres;

--
-- Name: productionpershift_progressforday; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productionpershift_progressforday (
    id bigint NOT NULL,
    technologyinstanceoperationcomponent_id bigint,
    day integer,
    corrected boolean DEFAULT false,
    dateofday date
);


ALTER TABLE public.productionpershift_progressforday OWNER TO postgres;

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
    technicalcode character varying(255),
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
    controlledquantity numeric(12,5),
    takenforcontrolquantity numeric(12,5),
    rejectedquantity numeric(12,5),
    accepteddefectsquantity numeric(12,5),
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
-- Name: simplematerialbalance_simplematerialbalancelocationscomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE simplematerialbalance_simplematerialbalancelocationscomponent (
    id bigint NOT NULL,
    simplematerialbalance_id bigint,
    location_id bigint
);


ALTER TABLE public.simplematerialbalance_simplematerialbalancelocationscomponent OWNER TO postgres;

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
-- Name: states_message; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE states_message (
    id bigint NOT NULL,
    type character varying(255),
    translationkey character varying(255),
    translationargs character varying(255),
    correspondfieldname character varying(255),
    autoclose boolean DEFAULT true,
    productionrecordstatechange_id bigint,
    orderstatechange_id bigint,
    assignmenttoshiftstatechange_id bigint,
    technologystatechange_id bigint
);


ALTER TABLE public.states_message OWNER TO postgres;

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
-- Name: technologies_operation; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE technologies_operation (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(1024),
    comment character varying(2048),
    workstationtype_id bigint,
    attachment character varying(255),
    areproductquantitiesdivisible boolean DEFAULT false,
    istjdivisible boolean DEFAULT false,
    tj integer DEFAULT 0,
    numberofoperations integer DEFAULT 1,
    machineutilization numeric(8,5) DEFAULT 1.0,
    countmachineunit character varying(255),
    machinehourlycost numeric(12,5) DEFAULT 0::numeric,
    hidedescriptioninworkplans boolean,
    pieceworkcost numeric(12,5) DEFAULT 0::numeric,
    hidedetailsinworkplans boolean,
    tpz integer DEFAULT 0,
    productioninonecycleunit character varying(255),
    imageurlinworkplan character varying(255),
    productioninonecycle numeric(12,5) DEFAULT 1::numeric,
    hidetechnologyandorderinworkplans boolean,
    laborhourlycost numeric(12,5) DEFAULT 0::numeric,
    laborutilization numeric(8,5) DEFAULT 1.0,
    dontprintinputproductsinworkplans boolean,
    dontprintoutputproductsinworkplans boolean,
    timenextoperation integer DEFAULT 0,
    countmachine numeric(8,5) DEFAULT 0::numeric,
    countrealized character varying(255) DEFAULT '01all'::character varying,
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
    quantity numeric(10,5),
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
    quantity numeric(10,5)
);


ALTER TABLE public.technologies_operationproductoutcomponent OWNER TO postgres;

--
-- Name: technologies_productcomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE technologies_productcomponent (
    id bigint NOT NULL,
    product_id bigint,
    operationin_id bigint,
    operationout_id bigint,
    active boolean DEFAULT true
);


ALTER TABLE public.technologies_productcomponent OWNER TO postgres;

--
-- Name: technologies_technology; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE technologies_technology (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(2048),
    product_id bigint,
    technologygroup_id bigint,
    master boolean DEFAULT true,
    description character varying(2048),
    state character varying(255) DEFAULT '01draft'::character varying,
    batchrequired boolean,
    qualitycontrolinstruction character varying(255),
    postfeaturerequired boolean,
    minimalquantity numeric(10,5),
    qualitycontroltype character varying(255),
    unitsamplingnr numeric(12,5),
    otherfeaturerequired boolean,
    shiftfeaturerequired boolean,
    active boolean DEFAULT true
);


ALTER TABLE public.technologies_technology OWNER TO postgres;

--
-- Name: technologies_technologygroup; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE technologies_technologygroup (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(2048),
    active boolean DEFAULT true
);


ALTER TABLE public.technologies_technologygroup OWNER TO postgres;

--
-- Name: technologies_technologyinstanceoperationcomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE technologies_technologyinstanceoperationcomponent (
    id bigint NOT NULL,
    technology_id bigint,
    operation_id bigint,
    technologyoperationcomponent_id bigint,
    parent_id bigint,
    entitytype character varying(255) DEFAULT 'operation'::character varying,
    priority integer,
    nodenumber character varying(255),
    areproductquantitiesdivisible boolean DEFAULT false,
    istjdivisible boolean DEFAULT false,
    hascorrections boolean,
    tpz integer DEFAULT 0,
    effectiveoperationrealizationtime integer,
    hidetechnologyandorderinworkplans boolean,
    productioninonecycle numeric(12,5) DEFAULT 1::numeric,
    operationoffset integer,
    machinehourlycost numeric(12,5) DEFAULT 0::numeric,
    countrealized character varying(255) DEFAULT '01all'::character varying,
    dontprintinputproductsinworkplans boolean,
    timenextoperation integer DEFAULT 0,
    tj integer DEFAULT 0,
    imageurlinworkplan character varying(255),
    laborhourlycost numeric(12,5) DEFAULT 0::numeric,
    effectivedateto timestamp without time zone,
    dontprintoutputproductsinworkplans boolean,
    countmachine numeric(12,5) DEFAULT 0::numeric,
    hidedescriptioninworkplans boolean,
    numberofoperations integer DEFAULT 1,
    hidedetailsinworkplans boolean,
    laborutilization numeric(8,5) DEFAULT 1.0,
    machineutilization numeric(8,5) DEFAULT 1.0,
    effectivedatefrom timestamp without time zone,
    countmachineunit character varying(255),
    quantityofworkstationtypes integer DEFAULT 1,
    order_id bigint,
    productioninonecycleunit character varying(255),
    pieceworkcost numeric(12,5) DEFAULT 0::numeric,
    createdate timestamp without time zone,
    updatedate timestamp without time zone,
    createuser character varying(255),
    updateuser character varying(255)
);


ALTER TABLE public.technologies_technologyinstanceoperationcomponent OWNER TO postgres;

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
    areproductquantitiesdivisible boolean DEFAULT false,
    istjdivisible boolean DEFAULT false,
    countmachine numeric(12,5) DEFAULT 0::numeric,
    countrealized character varying(255) DEFAULT '01all'::character varying,
    dontprintinputproductsinworkplans boolean,
    imageurlinworkplan character varying(255),
    hidedescriptioninworkplans boolean,
    productioninonecycleunit character varying(255),
    hidetechnologyandorderinworkplans boolean,
    laborutilization numeric(8,5) DEFAULT 1.0,
    dontprintoutputproductsinworkplans boolean,
    machineutilization numeric(8,5) DEFAULT 1.0,
    productioninonecycle numeric(12,5) DEFAULT 1::numeric,
    timenextoperation integer DEFAULT 0,
    countmachineunit character varying(255),
    qualitycontrolrequired boolean,
    pieceworkcost numeric(12,5) DEFAULT 0::numeric,
    tpz integer DEFAULT 0,
    numberofoperations integer DEFAULT 1,
    machinehourlycost numeric(12,5) DEFAULT 0::numeric,
    hidedetailsinworkplans boolean,
    laborhourlycost numeric(12,5) DEFAULT 0::numeric,
    tj integer DEFAULT 0
);


ALTER TABLE public.technologies_technologyoperationcomponent OWNER TO postgres;

--
-- Name: technologies_technologystatechange; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE technologies_technologystatechange (
    id bigint NOT NULL,
    dateandtime timestamp without time zone,
    sourcestate character varying(255),
    targetstate character varying(255),
    status character varying(255),
    phase integer,
    worker character varying(255),
    technology_id bigint,
    shift_id bigint
);


ALTER TABLE public.technologies_technologystatechange OWNER TO postgres;

--
-- Name: wagegroups_wagegroup; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE wagegroups_wagegroup (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(255),
    superiorwagegroup character varying(255),
    laborhourlycost numeric(12,5),
    laborhourlycostcurrency character varying(255)
);


ALTER TABLE public.wagegroups_wagegroup OWNER TO postgres;

--
-- Name: workplans_columnforinputproducts; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_columnforinputproducts (
    id bigint NOT NULL,
    identifier character varying(255),
    name character varying(1024),
    description character varying(1024),
    columnfiller character varying(255),
    alignment character varying(255) DEFAULT '01left'::character varying
);


ALTER TABLE public.workplans_columnforinputproducts OWNER TO postgres;

--
-- Name: workplans_columnfororders; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_columnfororders (
    id bigint NOT NULL,
    identifier character varying(255),
    name character varying(1024),
    description character varying(1024),
    columnfiller character varying(255),
    alignment character varying(255) DEFAULT '01left'::character varying
);


ALTER TABLE public.workplans_columnfororders OWNER TO postgres;

--
-- Name: workplans_columnforoutputproducts; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_columnforoutputproducts (
    id bigint NOT NULL,
    identifier character varying(255),
    name character varying(1024),
    description character varying(1024),
    columnfiller character varying(255),
    alignment character varying(255) DEFAULT '01left'::character varying
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
    technologyinstanceoperationcomponent_id bigint,
    columnforinputproducts_id bigint,
    succession integer
);


ALTER TABLE public.workplans_orderoperationinputcolumn OWNER TO postgres;

--
-- Name: workplans_orderoperationoutputcolumn; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_orderoperationoutputcolumn (
    id bigint NOT NULL,
    technologyinstanceoperationcomponent_id bigint,
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
-- Name: assignmenttoshift_assignmenttoshift_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY assignmenttoshift_assignmenttoshift
    ADD CONSTRAINT assignmenttoshift_assignmenttoshift_pkey PRIMARY KEY (id);


--
-- Name: assignmenttoshift_assignmenttoshiftreport_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY assignmenttoshift_assignmenttoshiftreport
    ADD CONSTRAINT assignmenttoshift_assignmenttoshiftreport_pkey PRIMARY KEY (id);


--
-- Name: assignmenttoshift_assignmenttoshiftstatechange_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY assignmenttoshift_assignmenttoshiftstatechange
    ADD CONSTRAINT assignmenttoshift_assignmenttoshiftstatechange_pkey PRIMARY KEY (id);


--
-- Name: assignmenttoshift_staffassignmenttoshift_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY assignmenttoshift_staffassignmenttoshift
    ADD CONSTRAINT assignmenttoshift_staffassignmenttoshift_pkey PRIMARY KEY (id);


--
-- Name: avglaborcostcalcfororder_assignmentworkertoshift_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY avglaborcostcalcfororder_assignmentworkertoshift
    ADD CONSTRAINT avglaborcostcalcfororder_assignmentworkertoshift_pkey PRIMARY KEY (id);


--
-- Name: avglaborcostcalcfororder_avglaborcostcalcfororder_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY avglaborcostcalcfororder_avglaborcostcalcfororder
    ADD CONSTRAINT avglaborcostcalcfororder_avglaborcostcalcfororder_pkey PRIMARY KEY (id);


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
-- Name: costnormsformaterials_technologyinstoperproductincomp_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY costnormsformaterials_technologyinstoperproductincomp
    ADD CONSTRAINT costnormsformaterials_technologyinstoperproductincomp_pkey PRIMARY KEY (id);


--
-- Name: costnormsforoperation_calculationoperationcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY costnormsforoperation_calculationoperationcomponent
    ADD CONSTRAINT costnormsforoperation_calculationoperationcomponent_pkey PRIMARY KEY (id);


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
    ADD CONSTRAINT jointable_materialrequirement_order_pkey PRIMARY KEY (order_id, materialrequirement_id);


--
-- Name: jointable_order_workplan_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY jointable_order_workplan
    ADD CONSTRAINT jointable_order_workplan_pkey PRIMARY KEY (order_id, workplan_id);


--
-- Name: jointable_productionline_technology_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY jointable_productionline_technology
    ADD CONSTRAINT jointable_productionline_technology_pkey PRIMARY KEY (productionline_id, technology_id);


--
-- Name: jointable_productionline_technologygroup_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY jointable_productionline_technologygroup
    ADD CONSTRAINT jointable_productionline_technologygroup_pkey PRIMARY KEY (productionline_id, technologygroup_id);


--
-- Name: linechangeovernorms_linechangeovernorms_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY linechangeovernorms_linechangeovernorms
    ADD CONSTRAINT linechangeovernorms_linechangeovernorms_pkey PRIMARY KEY (id);


--
-- Name: materialflow_location_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflow_location
    ADD CONSTRAINT materialflow_location_pkey PRIMARY KEY (id);


--
-- Name: materialflow_materialsinlocation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflow_materialsinlocation
    ADD CONSTRAINT materialflow_materialsinlocation_pkey PRIMARY KEY (id);


--
-- Name: materialflow_materialsinlocationcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflow_materialsinlocationcomponent
    ADD CONSTRAINT materialflow_materialsinlocationcomponent_pkey PRIMARY KEY (id);


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
-- Name: materialflowmultitransfers_productquantity_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflowmultitransfers_productquantity
    ADD CONSTRAINT materialflowmultitransfers_productquantity_pkey PRIMARY KEY (id);


--
-- Name: materialflowmultitransfers_transfertemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflowmultitransfers_transfertemplate
    ADD CONSTRAINT materialflowmultitransfers_transfertemplate_pkey PRIMARY KEY (id);


--
-- Name: materialflowresources_resource_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflowresources_resource
    ADD CONSTRAINT materialflowresources_resource_pkey PRIMARY KEY (id);


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
-- Name: orders_order_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY orders_order
    ADD CONSTRAINT orders_order_pkey PRIMARY KEY (id);


--
-- Name: orders_orderstatechange_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY orders_orderstatechange
    ADD CONSTRAINT orders_orderstatechange_pkey PRIMARY KEY (id);


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
-- Name: productioncounting_operationpieceworkcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncounting_operationpieceworkcomponent
    ADD CONSTRAINT productioncounting_operationpieceworkcomponent_pkey PRIMARY KEY (id);


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
-- Name: productioncounting_productionrecordstatechange_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncounting_productionrecordstatechange
    ADD CONSTRAINT productioncounting_productionrecordstatechange_pkey PRIMARY KEY (id);


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
-- Name: productioncountingwithcosts_operationpieceworkcostcomponen_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncountingwithcosts_operationpieceworkcostcomponent
    ADD CONSTRAINT productioncountingwithcosts_operationpieceworkcostcomponen_pkey PRIMARY KEY (id);


--
-- Name: productioncountingwithcosts_technologyinstoperproductincom_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncountingwithcosts_technologyinstoperproductincomp
    ADD CONSTRAINT productioncountingwithcosts_technologyinstoperproductincom_pkey PRIMARY KEY (id);


--
-- Name: productionlines_productionline_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productionlines_productionline
    ADD CONSTRAINT productionlines_productionline_pkey PRIMARY KEY (id);


--
-- Name: productionlines_workstationtypecomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productionlines_workstationtypecomponent
    ADD CONSTRAINT productionlines_workstationtypecomponent_pkey PRIMARY KEY (id);


--
-- Name: productionpershift_dailyprogress_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productionpershift_dailyprogress
    ADD CONSTRAINT productionpershift_dailyprogress_pkey PRIMARY KEY (id);


--
-- Name: productionpershift_productionpershift_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productionpershift_productionpershift
    ADD CONSTRAINT productionpershift_productionpershift_pkey PRIMARY KEY (id);


--
-- Name: productionpershift_progressforday_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productionpershift_progressforday
    ADD CONSTRAINT productionpershift_progressforday_pkey PRIMARY KEY (id);


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
-- Name: simplematerialbalance_simplematerialbalancelocationscompon_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY simplematerialbalance_simplematerialbalancelocationscomponent
    ADD CONSTRAINT simplematerialbalance_simplematerialbalancelocationscompon_pkey PRIMARY KEY (id);


--
-- Name: simplematerialbalance_simplematerialbalanceorderscomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY simplematerialbalance_simplematerialbalanceorderscomponent
    ADD CONSTRAINT simplematerialbalance_simplematerialbalanceorderscomponent_pkey PRIMARY KEY (id);


--
-- Name: states_message_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY states_message
    ADD CONSTRAINT states_message_pkey PRIMARY KEY (id);


--
-- Name: stoppage_stoppage_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY stoppage_stoppage
    ADD CONSTRAINT stoppage_stoppage_pkey PRIMARY KEY (id);


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
-- Name: technologies_productcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY technologies_productcomponent
    ADD CONSTRAINT technologies_productcomponent_pkey PRIMARY KEY (id);


--
-- Name: technologies_technology_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY technologies_technology
    ADD CONSTRAINT technologies_technology_pkey PRIMARY KEY (id);


--
-- Name: technologies_technologygroup_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY technologies_technologygroup
    ADD CONSTRAINT technologies_technologygroup_pkey PRIMARY KEY (id);


--
-- Name: technologies_technologyinstanceoperationcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY technologies_technologyinstanceoperationcomponent
    ADD CONSTRAINT technologies_technologyinstanceoperationcomponent_pkey PRIMARY KEY (id);


--
-- Name: technologies_technologyoperationcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY technologies_technologyoperationcomponent
    ADD CONSTRAINT technologies_technologyoperationcomponent_pkey PRIMARY KEY (id);


--
-- Name: technologies_technologystatechange_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY technologies_technologystatechange
    ADD CONSTRAINT technologies_technologystatechange_pkey PRIMARY KEY (id);


--
-- Name: wagegroups_wagegroup_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY wagegroups_wagegroup
    ADD CONSTRAINT wagegroups_wagegroup_pkey PRIMARY KEY (id);


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
-- Name: fk1de839ded7a28a88; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_product
    ADD CONSTRAINT fk1de839ded7a28a88 FOREIGN KEY (technologygroup_id) REFERENCES technologies_technologygroup(id);


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
-- Name: fk2f2b3d122331a16c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_operationpieceworkcomponent
    ADD CONSTRAINT fk2f2b3d122331a16c FOREIGN KEY (technologyinstanceoperationcomponent_id) REFERENCES technologies_technologyinstanceoperationcomponent(id);


--
-- Name: fk2f2b3d12bf1b2d48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_operationpieceworkcomponent
    ADD CONSTRAINT fk2f2b3d12bf1b2d48 FOREIGN KEY (productionbalance_id) REFERENCES productioncounting_productionbalance(id);


--
-- Name: fk31da647fb64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY stoppage_stoppage
    ADD CONSTRAINT fk31da647fb64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fk36a4b164b17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionrecordstatechange
    ADD CONSTRAINT fk36a4b164b17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- Name: fk36a4b164feff14c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionrecordstatechange
    ADD CONSTRAINT fk36a4b164feff14c FOREIGN KEY (productionrecord_id) REFERENCES productioncounting_productionrecord(id);


--
-- Name: fk3816ba98ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncountingwithcosts_technologyinstoperproductincomp
    ADD CONSTRAINT fk3816ba98ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fk3816ba98bf1b2d48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncountingwithcosts_technologyinstoperproductincomp
    ADD CONSTRAINT fk3816ba98bf1b2d48 FOREIGN KEY (productionbalance_id) REFERENCES productioncounting_productionbalance(id);


--
-- Name: fk3daecd745d8d5ea8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY orders_order
    ADD CONSTRAINT fk3daecd745d8d5ea8 FOREIGN KEY (company_id) REFERENCES basic_company(id);


--
-- Name: fk3daecd7463154a1c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY orders_order
    ADD CONSTRAINT fk3daecd7463154a1c FOREIGN KEY (productionline_id) REFERENCES productionlines_productionline(id);


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
-- Name: fk40a2a25b17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productionpershift_dailyprogress
    ADD CONSTRAINT fk40a2a25b17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- Name: fk40a2a25d83aacac; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productionpershift_dailyprogress
    ADD CONSTRAINT fk40a2a25d83aacac FOREIGN KEY (progressforday_id) REFERENCES productionpershift_progressforday(id);


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
-- Name: fk4a1a76df12260dea; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY assignmenttoshift_staffassignmenttoshift
    ADD CONSTRAINT fk4a1a76df12260dea FOREIGN KEY (worker_id) REFERENCES basic_staff(id);


--
-- Name: fk4a1a76df63154a1c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY assignmenttoshift_staffassignmenttoshift
    ADD CONSTRAINT fk4a1a76df63154a1c FOREIGN KEY (productionline_id) REFERENCES productionlines_productionline(id);


--
-- Name: fk4a1a76dfba9b2bb0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY assignmenttoshift_staffassignmenttoshift
    ADD CONSTRAINT fk4a1a76dfba9b2bb0 FOREIGN KEY (assignmenttoshift_id) REFERENCES assignmenttoshift_assignmenttoshift(id);


--
-- Name: fk510629c1ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technology
    ADD CONSTRAINT fk510629c1ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fk510629c1d7a28a88; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technology
    ADD CONSTRAINT fk510629c1d7a28a88 FOREIGN KEY (technologygroup_id) REFERENCES technologies_technologygroup(id);


--
-- Name: fk55f2fd8db17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY orders_orderstatechange
    ADD CONSTRAINT fk55f2fd8db17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- Name: fk55f2fd8db64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY orders_orderstatechange
    ADD CONSTRAINT fk55f2fd8db64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fk5902e76c154b936c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technologyinstanceoperationcomponent
    ADD CONSTRAINT fk5902e76c154b936c FOREIGN KEY (technologyoperationcomponent_id) REFERENCES technologies_technologyoperationcomponent(id);


--
-- Name: fk5902e76c31ff56b9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technologyinstanceoperationcomponent
    ADD CONSTRAINT fk5902e76c31ff56b9 FOREIGN KEY (parent_id) REFERENCES technologies_technologyinstanceoperationcomponent(id);


--
-- Name: fk5902e76cb1e1a8a8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technologyinstanceoperationcomponent
    ADD CONSTRAINT fk5902e76cb1e1a8a8 FOREIGN KEY (operation_id) REFERENCES technologies_operation(id);


--
-- Name: fk5902e76cb64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technologyinstanceoperationcomponent
    ADD CONSTRAINT fk5902e76cb64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fk5902e76ce3afcbac; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technologyinstanceoperationcomponent
    ADD CONSTRAINT fk5902e76ce3afcbac FOREIGN KEY (technology_id) REFERENCES technologies_technology(id);


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
-- Name: fk5d2719fd2331a16c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionrecord
    ADD CONSTRAINT fk5d2719fd2331a16c FOREIGN KEY (technologyinstanceoperationcomponent_id) REFERENCES technologies_technologyinstanceoperationcomponent(id);


--
-- Name: fk5d2719fd7728be4c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionrecord
    ADD CONSTRAINT fk5d2719fd7728be4c FOREIGN KEY (division_id) REFERENCES basic_division(id);


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
-- Name: fk5de2ef7f4e0851ac; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflowmultitransfers_productquantity
    ADD CONSTRAINT fk5de2ef7f4e0851ac FOREIGN KEY (transfer_id) REFERENCES materialflow_transfer(id);


--
-- Name: fk5de2ef7fad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflowmultitransfers_productquantity
    ADD CONSTRAINT fk5de2ef7fad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fk5e375575b17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY assignmenttoshift_assignmenttoshift
    ADD CONSTRAINT fk5e375575b17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- Name: fk686fa361b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogies_genealogy
    ADD CONSTRAINT fk686fa361b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fk68d413bc63154a1c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY jointable_productionline_technologygroup
    ADD CONSTRAINT fk68d413bc63154a1c FOREIGN KEY (productionline_id) REFERENCES productionlines_productionline(id);


--
-- Name: fk68d413bcd7a28a88; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY jointable_productionline_technologygroup
    ADD CONSTRAINT fk68d413bcd7a28a88 FOREIGN KEY (technologygroup_id) REFERENCES technologies_technologygroup(id);


--
-- Name: fk6bb812362331a16c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_orderoperationoutputcolumn
    ADD CONSTRAINT fk6bb812362331a16c FOREIGN KEY (technologyinstanceoperationcomponent_id) REFERENCES technologies_technologyinstanceoperationcomponent(id);


--
-- Name: fk6bb81236f3551292; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_orderoperationoutputcolumn
    ADD CONSTRAINT fk6bb81236f3551292 FOREIGN KEY (columnforoutputproducts_id) REFERENCES workplans_columnforoutputproducts(id);


--
-- Name: fk73ef37619f42928; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_materialsinlocationcomponent
    ADD CONSTRAINT fk73ef37619f42928 FOREIGN KEY (materialsinlocation_id) REFERENCES materialflow_materialsinlocation(id);


--
-- Name: fk73ef3761ba6c56c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_materialsinlocationcomponent
    ADD CONSTRAINT fk73ef3761ba6c56c FOREIGN KEY (location_id) REFERENCES materialflow_location(id);


--
-- Name: fk77e41fdc2331a16c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_operationtimecomponent
    ADD CONSTRAINT fk77e41fdc2331a16c FOREIGN KEY (technologyinstanceoperationcomponent_id) REFERENCES technologies_technologyinstanceoperationcomponent(id);


--
-- Name: fk77e41fdcbf1b2d48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_operationtimecomponent
    ADD CONSTRAINT fk77e41fdcbf1b2d48 FOREIGN KEY (productionbalance_id) REFERENCES productioncounting_productionbalance(id);


--
-- Name: fk77f71aecb17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY assignmenttoshift_assignmenttoshiftstatechange
    ADD CONSTRAINT fk77f71aecb17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- Name: fk77f71aecba9b2bb0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY assignmenttoshift_assignmenttoshiftstatechange
    ADD CONSTRAINT fk77f71aecba9b2bb0 FOREIGN KEY (assignmenttoshift_id) REFERENCES assignmenttoshift_assignmenttoshift(id);


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
-- Name: fk80af02ff63154a1c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY avglaborcostcalcfororder_avglaborcostcalcfororder
    ADD CONSTRAINT fk80af02ff63154a1c FOREIGN KEY (productionline_id) REFERENCES productionlines_productionline(id);


--
-- Name: fk80af02ffb64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY avglaborcostcalcfororder_avglaborcostcalcfororder
    ADD CONSTRAINT fk80af02ffb64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fk872515c36d58e323; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_productcomponent
    ADD CONSTRAINT fk872515c36d58e323 FOREIGN KEY (operationin_id) REFERENCES technologies_operation(id);


--
-- Name: fk872515c385d21d88; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_productcomponent
    ADD CONSTRAINT fk872515c385d21d88 FOREIGN KEY (operationout_id) REFERENCES technologies_operation(id);


--
-- Name: fk872515c3ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_productcomponent
    ADD CONSTRAINT fk872515c3ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fk89ba7c08a641f4dc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qualitycontrols_qualitycontrol
    ADD CONSTRAINT fk89ba7c08a641f4dc FOREIGN KEY (operation_id) REFERENCES technologies_technologyinstanceoperationcomponent(id);


--
-- Name: fk89ba7c08b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qualitycontrols_qualitycontrol
    ADD CONSTRAINT fk89ba7c08b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


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
-- Name: fk8c32fdf55c328802; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT fk8c32fdf55c328802 FOREIGN KEY (locationfrom_id) REFERENCES materialflow_location(id);


--
-- Name: fk8c32fdf580ca8251; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT fk8c32fdf580ca8251 FOREIGN KEY (locationto_id) REFERENCES materialflow_location(id);


--
-- Name: fk8c32fdf5ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT fk8c32fdf5ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fk8c32fdf5d8bb7bc1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT fk8c32fdf5d8bb7bc1 FOREIGN KEY (transformationsconsumption_id) REFERENCES materialflow_transformations(id);


--
-- Name: fk8c778d612331a16c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_orderoperationinputcolumn
    ADD CONSTRAINT fk8c778d612331a16c FOREIGN KEY (technologyinstanceoperationcomponent_id) REFERENCES technologies_technologyinstanceoperationcomponent(id);


--
-- Name: fk8c778d61b18b142; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_orderoperationinputcolumn
    ADD CONSTRAINT fk8c778d61b18b142 FOREIGN KEY (columnforinputproducts_id) REFERENCES workplans_columnforinputproducts(id);


--
-- Name: fk8e2b08bdad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflowresources_resource
    ADD CONSTRAINT fk8e2b08bdad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fk8e2b08bdba6c56c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflowresources_resource
    ADD CONSTRAINT fk8e2b08bdba6c56c FOREIGN KEY (location_id) REFERENCES materialflow_location(id);


--
-- Name: fk8f123e06ac0386c6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogies_otherfeature
    ADD CONSTRAINT fk8f123e06ac0386c6 FOREIGN KEY (genealogy_id) REFERENCES genealogies_genealogy(id);


--
-- Name: fk91a44c8363154a1c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY jointable_productionline_technology
    ADD CONSTRAINT fk91a44c8363154a1c FOREIGN KEY (productionline_id) REFERENCES productionlines_productionline(id);


--
-- Name: fk91a44c83e3afcbac; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY jointable_productionline_technology
    ADD CONSTRAINT fk91a44c83e3afcbac FOREIGN KEY (technology_id) REFERENCES technologies_technology(id);


--
-- Name: fk921291dfb64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productionpershift_productionpershift
    ADD CONSTRAINT fk921291dfb64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fk95347f3fb17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_shifttimetableexception
    ADD CONSTRAINT fk95347f3fb17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- Name: fk95403ebf63154a1c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY linechangeovernorms_linechangeovernorms
    ADD CONSTRAINT fk95403ebf63154a1c FOREIGN KEY (productionline_id) REFERENCES productionlines_productionline(id);


--
-- Name: fk95403ebf8a465d11; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY linechangeovernorms_linechangeovernorms
    ADD CONSTRAINT fk95403ebf8a465d11 FOREIGN KEY (totechnology_id) REFERENCES technologies_technology(id);


--
-- Name: fk95403ebf9be4f842; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY linechangeovernorms_linechangeovernorms
    ADD CONSTRAINT fk95403ebf9be4f842 FOREIGN KEY (fromtechnology_id) REFERENCES technologies_technology(id);


--
-- Name: fk95403ebfdbfcbb2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY linechangeovernorms_linechangeovernorms
    ADD CONSTRAINT fk95403ebfdbfcbb2 FOREIGN KEY (fromtechnologygroup_id) REFERENCES technologies_technologygroup(id);


--
-- Name: fk95403ebff9d81043; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY linechangeovernorms_linechangeovernorms
    ADD CONSTRAINT fk95403ebff9d81043 FOREIGN KEY (totechnologygroup_id) REFERENCES technologies_technologygroup(id);


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
-- Name: fka52db51ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costnormsformaterials_technologyinstoperproductincomp
    ADD CONSTRAINT fka52db51ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fka52db51b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costnormsformaterials_technologyinstoperproductincomp
    ADD CONSTRAINT fka52db51b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- Name: fka83409401e9fcb48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transformations
    ADD CONSTRAINT fka83409401e9fcb48 FOREIGN KEY (staff_id) REFERENCES basic_staff(id);


--
-- Name: fka83409405c328802; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transformations
    ADD CONSTRAINT fka83409405c328802 FOREIGN KEY (locationfrom_id) REFERENCES materialflow_location(id);


--
-- Name: fka834094080ca8251; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transformations
    ADD CONSTRAINT fka834094080ca8251 FOREIGN KEY (locationto_id) REFERENCES materialflow_location(id);


--
-- Name: fkad2352ca5c9bb48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY states_message
    ADD CONSTRAINT fkad2352ca5c9bb48 FOREIGN KEY (productionrecordstatechange_id) REFERENCES productioncounting_productionrecordstatechange(id);


--
-- Name: fkad2352caa1d7ed6c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY states_message
    ADD CONSTRAINT fkad2352caa1d7ed6c FOREIGN KEY (orderstatechange_id) REFERENCES orders_orderstatechange(id);


--
-- Name: fkad2352cad31ada64; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY states_message
    ADD CONSTRAINT fkad2352cad31ada64 FOREIGN KEY (assignmenttoshiftstatechange_id) REFERENCES assignmenttoshift_assignmenttoshiftstatechange(id);


--
-- Name: fkad2352cade1ba4e8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY states_message
    ADD CONSTRAINT fkad2352cade1ba4e8 FOREIGN KEY (technologystatechange_id) REFERENCES technologies_technologystatechange(id);


--
-- Name: fkaea67fc12331a16c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productionpershift_progressforday
    ADD CONSTRAINT fkaea67fc12331a16c FOREIGN KEY (technologyinstanceoperationcomponent_id) REFERENCES technologies_technologyinstanceoperationcomponent(id);


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
-- Name: fkb7f28af910813cc8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productionlines_workstationtypecomponent
    ADD CONSTRAINT fkb7f28af910813cc8 FOREIGN KEY (workstationtype_id) REFERENCES basic_workstationtype(id);


--
-- Name: fkb7f28af963154a1c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productionlines_workstationtypecomponent
    ADD CONSTRAINT fkb7f28af963154a1c FOREIGN KEY (productionline_id) REFERENCES productionlines_productionline(id);


--
-- Name: fkb84153a64525613e; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY simplematerialbalance_simplematerialbalancelocationscomponent
    ADD CONSTRAINT fkb84153a64525613e FOREIGN KEY (simplematerialbalance_id) REFERENCES simplematerialbalance_simplematerialbalance(id);


--
-- Name: fkb84153a6ba6c56c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY simplematerialbalance_simplematerialbalancelocationscomponent
    ADD CONSTRAINT fkb84153a6ba6c56c FOREIGN KEY (location_id) REFERENCES materialflow_location(id);


--
-- Name: fkbe18df20b17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technologystatechange
    ADD CONSTRAINT fkbe18df20b17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- Name: fkbe18df20e3afcbac; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technologystatechange
    ADD CONSTRAINT fkbe18df20e3afcbac FOREIGN KEY (technology_id) REFERENCES technologies_technology(id);


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
-- Name: fkc53531405c328802; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflowmultitransfers_transfertemplate
    ADD CONSTRAINT fkc53531405c328802 FOREIGN KEY (locationfrom_id) REFERENCES materialflow_location(id);


--
-- Name: fkc535314080ca8251; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflowmultitransfers_transfertemplate
    ADD CONSTRAINT fkc535314080ca8251 FOREIGN KEY (locationto_id) REFERENCES materialflow_location(id);


--
-- Name: fkc5353140ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflowmultitransfers_transfertemplate
    ADD CONSTRAINT fkc5353140ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


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
-- Name: fkc98deb09b17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY assignmenttoshift_assignmenttoshiftreport
    ADD CONSTRAINT fkc98deb09b17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- Name: fkc9d449ca1e9fcb48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_stockcorrection
    ADD CONSTRAINT fkc9d449ca1e9fcb48 FOREIGN KEY (staff_id) REFERENCES basic_staff(id);


--
-- Name: fkc9d449caad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_stockcorrection
    ADD CONSTRAINT fkc9d449caad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- Name: fkc9d449caba6c56c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_stockcorrection
    ADD CONSTRAINT fkc9d449caba6c56c FOREIGN KEY (location_id) REFERENCES materialflow_location(id);


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
-- Name: fkcc305bf42331a16c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncountingwithcosts_operationcostcomponent
    ADD CONSTRAINT fkcc305bf42331a16c FOREIGN KEY (technologyinstanceoperationcomponent_id) REFERENCES technologies_technologyinstanceoperationcomponent(id);


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
-- Name: fkcfbe273963154a1c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costcalculation_costcalculation
    ADD CONSTRAINT fkcfbe273963154a1c FOREIGN KEY (productionline_id) REFERENCES productionlines_productionline(id);


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
-- Name: fke3299f8f7ac4112d; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_staff
    ADD CONSTRAINT fke3299f8f7ac4112d FOREIGN KEY (workfor_id) REFERENCES basic_company(id);


--
-- Name: fke3299f8fb17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_staff
    ADD CONSTRAINT fke3299f8fb17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- Name: fke3299f8fbf62fac8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_staff
    ADD CONSTRAINT fke3299f8fbf62fac8 FOREIGN KEY (wagegroup_id) REFERENCES wagegroups_wagegroup(id);


--
-- Name: fkea89d32d2331a16c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncountingwithcosts_operationpieceworkcostcomponent
    ADD CONSTRAINT fkea89d32d2331a16c FOREIGN KEY (technologyinstanceoperationcomponent_id) REFERENCES technologies_technologyinstanceoperationcomponent(id);


--
-- Name: fkea89d32dbf1b2d48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncountingwithcosts_operationpieceworkcostcomponent
    ADD CONSTRAINT fkea89d32dbf1b2d48 FOREIGN KEY (productionbalance_id) REFERENCES productioncounting_productionbalance(id);


--
-- Name: fkecdc2ccc7728be4c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_workstationtype
    ADD CONSTRAINT fkecdc2ccc7728be4c FOREIGN KEY (division_id) REFERENCES basic_division(id);


--
-- Name: fkedd6c3e212260dea; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY avglaborcostcalcfororder_assignmentworkertoshift
    ADD CONSTRAINT fkedd6c3e212260dea FOREIGN KEY (worker_id) REFERENCES basic_staff(id);


--
-- Name: fkedd6c3e2881a80c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY avglaborcostcalcfororder_assignmentworkertoshift
    ADD CONSTRAINT fkedd6c3e2881a80c FOREIGN KEY (avglaborcostcalcfororder_id) REFERENCES avglaborcostcalcfororder_avglaborcostcalcfororder(id);


--
-- Name: fkedd6c3e2ba9b2bb0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY avglaborcostcalcfororder_assignmentworkertoshift
    ADD CONSTRAINT fkedd6c3e2ba9b2bb0 FOREIGN KEY (assignmenttoshift_id) REFERENCES assignmenttoshift_assignmenttoshift(id);


--
-- Name: fkf0b0619e64280dc0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_division
    ADD CONSTRAINT fkf0b0619e64280dc0 FOREIGN KEY (supervisor_id) REFERENCES basic_staff(id);


--
-- Name: fkf2fd76b063154a1c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionbalance
    ADD CONSTRAINT fkf2fd76b063154a1c FOREIGN KEY (productionline_id) REFERENCES productionlines_productionline(id);


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
-- Name: fkf39affe67728be4c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productionlines_productionline
    ADD CONSTRAINT fkf39affe67728be4c FOREIGN KEY (division_id) REFERENCES basic_division(id);


--
-- Name: fkf7f1a0d82b729dfb; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_parameter
    ADD CONSTRAINT fkf7f1a0d82b729dfb FOREIGN KEY (defaultproductionline_id) REFERENCES productionlines_productionline(id);


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

