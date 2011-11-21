--
-- PostgreSQL database dump
--

-- Started on 2011-11-16 12:18:56 CET

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 449 (class 2612 OID 16386)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

CREATE PROCEDURAL LANGUAGE plpgsql;


ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO postgres;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1639 (class 1259 OID 329426)
-- Dependencies: 1969 1970 3
-- Name: basic_company; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_company (
    id bigint NOT NULL,
    companyfullname character varying(255) DEFAULT 'Company'::character varying,
    tax character varying(30),
    street character varying(255),
    house character varying(30),
    flat character varying(30),
    zipcode character varying(30),
    city character varying(255),
    state character varying(30),
    country character varying(30),
    email character varying(30),
    addresswww character varying(30),
    phone character varying(25),
    active boolean DEFAULT true
);


ALTER TABLE public.basic_company OWNER TO postgres;

--
-- TOC entry 1640 (class 1259 OID 329436)
-- Dependencies: 3
-- Name: basic_contractor; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_contractor (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(255),
    externalnumber character varying(255)
);


ALTER TABLE public.basic_contractor OWNER TO postgres;

--
-- TOC entry 1641 (class 1259 OID 329444)
-- Dependencies: 3
-- Name: basic_currency; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_currency (
    id bigint NOT NULL,
    currency character varying(255),
    alphabeticcode character varying(3),
    isocode integer,
    minorunit integer,
    isactive boolean
);


ALTER TABLE public.basic_currency OWNER TO postgres;

--
-- TOC entry 1642 (class 1259 OID 329449)
-- Dependencies: 3
-- Name: basic_machine; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_machine (
    id bigint NOT NULL,
    name character varying(2048),
    number character varying(255),
    description character varying(2048)
);


ALTER TABLE public.basic_machine OWNER TO postgres;

--
-- TOC entry 1643 (class 1259 OID 329457)
-- Dependencies: 1971 3
-- Name: basic_parameter; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_parameter (
    id bigint NOT NULL,
    currency_id bigint,
    allowtoclose boolean,
    autogeneratequalitycontrol boolean,
    justone boolean,
    batchfordoneorder character varying(255) DEFAULT '01none'::character varying,
    autocloseorder boolean,
    registerquantityoutproduct boolean,
    checkdoneorderforquality boolean,
    registerproductiontime boolean,
    registerquantityinproduct boolean
);


ALTER TABLE public.basic_parameter OWNER TO postgres;

--
-- TOC entry 1644 (class 1259 OID 329463)
-- Dependencies: 1972 1973 1974 1975 1976 3
-- Name: basic_product; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_product (
    id bigint NOT NULL,
    number character varying(40),
    name character varying(255),
    typeofmaterial character varying(255),
    ean character varying(255),
    category character varying(255),
    unit character varying(255),
    externalnumber character varying(255),
    lastusedbatch character varying(255),
    genealogybatchreq boolean,
    batch character varying(255),
    averagecost numeric(10,3) DEFAULT 0::numeric,
    lastpurchasecost numeric(10,3) DEFAULT 0::numeric,
    nominalcost numeric(10,3) DEFAULT 0::numeric,
    costfornumber numeric(10,3) DEFAULT 1::numeric,
    active boolean DEFAULT true,
    createdate timestamp without time zone,
    updatedate timestamp without time zone,
    createuser character varying(255),
    updateuser character varying(255)
);


ALTER TABLE public.basic_product OWNER TO postgres;

--
-- TOC entry 1645 (class 1259 OID 329476)
-- Dependencies: 1977 1978 1979 1980 1981 1982 1983 3
-- Name: basic_shift; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_shift (
    id bigint NOT NULL,
    name character varying(255),
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
-- TOC entry 1646 (class 1259 OID 329491)
-- Dependencies: 1984 3
-- Name: basic_shifttimetableexception; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_shifttimetableexception (
    id bigint NOT NULL,
    name character varying(255),
    fromdate timestamp without time zone,
    todate timestamp without time zone,
    type character varying(255) DEFAULT '01freeTime'::character varying,
    shift_id bigint
);


ALTER TABLE public.basic_shifttimetableexception OWNER TO postgres;

--
-- TOC entry 1647 (class 1259 OID 329500)
-- Dependencies: 3
-- Name: basic_staff; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_staff (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(255),
    surname character varying(255),
    post character varying(255)
);


ALTER TABLE public.basic_staff OWNER TO postgres;

--
-- TOC entry 1648 (class 1259 OID 329508)
-- Dependencies: 3
-- Name: basic_substitute; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE basic_substitute (
    id bigint NOT NULL,
    number character varying(40),
    name character varying(255),
    product_id bigint,
    priority integer
);


ALTER TABLE public.basic_substitute OWNER TO postgres;

--
-- TOC entry 1649 (class 1259 OID 329513)
-- Dependencies: 3
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
-- TOC entry 1638 (class 1259 OID 329421)
-- Dependencies: 3
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
-- TOC entry 1650 (class 1259 OID 329518)
-- Dependencies: 1985 1986 1987 1988 1989 1990 1991 3
-- Name: costcalculation_costcalculation; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE costcalculation_costcalculation (
    id bigint NOT NULL,
    number character varying(40),
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
    costperunit numeric(10,3),
    description character varying(255),
    includetpz boolean DEFAULT true,
    calculatematerialcostsmode character varying(255) DEFAULT 'nominal'::character varying,
    calculateoperationcostsmode character varying(255) DEFAULT 'hourly'::character varying,
    dateofcalculation timestamp without time zone,
    generated boolean,
    filename character varying(255)
);


ALTER TABLE public.costcalculation_costcalculation OWNER TO postgres;

--
-- TOC entry 1651 (class 1259 OID 329533)
-- Dependencies: 1992 1993 1994 1995 1996 1997 1998 1999 2000 2001 2002 2003 3
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
    costcalculation_id bigint
);


ALTER TABLE public.costnormsforoperation_calculationoperationcomponent OWNER TO postgres;

--
-- TOC entry 1654 (class 1259 OID 329566)
-- Dependencies: 3
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
-- TOC entry 1655 (class 1259 OID 329574)
-- Dependencies: 3
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
-- TOC entry 1656 (class 1259 OID 329582)
-- Dependencies: 3
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
-- TOC entry 1657 (class 1259 OID 329590)
-- Dependencies: 3
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
-- TOC entry 1658 (class 1259 OID 329598)
-- Dependencies: 3
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
-- TOC entry 1652 (class 1259 OID 329553)
-- Dependencies: 3
-- Name: genealogiesforcomponents_genealogyproductincomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE genealogiesforcomponents_genealogyproductincomponent (
    id bigint NOT NULL,
    genealogy_id bigint,
    productincomponent_id bigint
);


ALTER TABLE public.genealogiesforcomponents_genealogyproductincomponent OWNER TO postgres;

--
-- TOC entry 1653 (class 1259 OID 329558)
-- Dependencies: 3
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
-- TOC entry 1637 (class 1259 OID 329419)
-- Dependencies: 3
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
-- TOC entry 1659 (class 1259 OID 329603)
-- Dependencies: 3
-- Name: jointable_materialrequirement_order; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE jointable_materialrequirement_order (
    order_id bigint NOT NULL,
    materialrequirement_id bigint NOT NULL
);


ALTER TABLE public.jointable_materialrequirement_order OWNER TO postgres;

--
-- TOC entry 1660 (class 1259 OID 329608)
-- Dependencies: 3
-- Name: materialflow_materialsinstockareas; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflow_materialsinstockareas (
    id bigint NOT NULL,
    name character varying(255),
    materialflowfordate timestamp without time zone,
    "time" timestamp without time zone,
    worker character varying(255),
    generated boolean,
    filename character varying(255)
);


ALTER TABLE public.materialflow_materialsinstockareas OWNER TO postgres;

--
-- TOC entry 1661 (class 1259 OID 329616)
-- Dependencies: 3
-- Name: materialflow_materialsinstockareascomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflow_materialsinstockareascomponent (
    id bigint NOT NULL,
    materialsinstockareas_id bigint,
    stockareas_id bigint
);


ALTER TABLE public.materialflow_materialsinstockareascomponent OWNER TO postgres;

--
-- TOC entry 1662 (class 1259 OID 329621)
-- Dependencies: 3
-- Name: materialflow_stockareas; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflow_stockareas (
    id bigint NOT NULL,
    number character varying(40),
    name character varying(255)
);


ALTER TABLE public.materialflow_stockareas OWNER TO postgres;

--
-- TOC entry 1663 (class 1259 OID 329626)
-- Dependencies: 3
-- Name: materialflow_stockcorrection; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflow_stockcorrection (
    id bigint NOT NULL,
    number character varying(40),
    stockcorrectiondate timestamp without time zone,
    stockareas_id bigint,
    product_id bigint,
    found numeric(10,3),
    staff_id bigint
);


ALTER TABLE public.materialflow_stockcorrection OWNER TO postgres;

--
-- TOC entry 1664 (class 1259 OID 329631)
-- Dependencies: 3
-- Name: materialflow_transfer; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflow_transfer (
    id bigint NOT NULL,
    number character varying(40),
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
-- TOC entry 1665 (class 1259 OID 329636)
-- Dependencies: 3
-- Name: materialflow_transformations; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialflow_transformations (
    id bigint NOT NULL,
    number character varying(40),
    name character varying(255),
    "time" timestamp without time zone,
    stockareasfrom_id bigint,
    stockareasto_id bigint,
    staff_id bigint
);


ALTER TABLE public.materialflow_transformations OWNER TO postgres;

--
-- TOC entry 1666 (class 1259 OID 329641)
-- Dependencies: 2004 3
-- Name: materialrequirements_materialrequirement; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE materialrequirements_materialrequirement (
    id bigint NOT NULL,
    name character varying(255),
    date timestamp without time zone,
    worker character varying(255),
    onlycomponents boolean DEFAULT true,
    generated boolean,
    filename character varying(255)
);


ALTER TABLE public.materialrequirements_materialrequirement OWNER TO postgres;

--
-- TOC entry 1667 (class 1259 OID 329650)
-- Dependencies: 3
-- Name: ordergroups_ordergroup; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE ordergroups_ordergroup (
    id bigint NOT NULL,
    number character varying(40),
    name character varying(255),
    datefrom date,
    dateto date
);


ALTER TABLE public.ordergroups_ordergroup OWNER TO postgres;

--
-- TOC entry 1668 (class 1259 OID 329655)
-- Dependencies: 2005 3
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
-- TOC entry 1669 (class 1259 OID 329664)
-- Dependencies: 2006 2007 2008 3
-- Name: orders_order; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE orders_order (
    id bigint NOT NULL,
    number character varying(40),
    name character varying(255),
    datefrom timestamp without time zone,
    dateto timestamp without time zone,
    state character varying(255),
    contractor_id bigint,
    product_id bigint,
    technology_id bigint,
    plannedquantity numeric(8,3),
    donequantity numeric(8,3),
    effectivedatefrom timestamp without time zone,
    effectivedateto timestamp without time zone,
    externalnumber character varying(255),
    externalsynchronized boolean DEFAULT true,
    registerquantityoutproduct boolean,
    calculate boolean,
    allowtoclose boolean,
    alert character varying(255),
    typeofproductionrecording character varying(255) DEFAULT '02cumulated'::character varying,
    ordergroup_id bigint,
    registerquantityinproduct boolean,
    realizationtime integer,
    registerproductiontime boolean,
    ordergroupname character varying(255),
    justone boolean,
    autocloseorder boolean,
    active boolean DEFAULT true
);


ALTER TABLE public.orders_order OWNER TO postgres;

--
-- TOC entry 1670 (class 1259 OID 329675)
-- Dependencies: 3
-- Name: productioncounting_productionbalance; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_productionbalance (
    id bigint NOT NULL,
    generated boolean,
    order_id bigint,
    product_id bigint,
    name character varying(255),
    date timestamp without time zone,
    worker character varying(255),
    recordsnumber integer,
    description character varying(255),
    filename character varying(255)
);


ALTER TABLE public.productioncounting_productionbalance OWNER TO postgres;

--
-- TOC entry 1671 (class 1259 OID 329683)
-- Dependencies: 3
-- Name: productioncounting_productioncounting; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_productioncounting (
    id bigint NOT NULL,
    generated boolean,
    order_id bigint,
    product_id bigint,
    name character varying(255),
    date timestamp without time zone,
    worker character varying(255),
    description character varying(255),
    filename character varying(255)
);


ALTER TABLE public.productioncounting_productioncounting OWNER TO postgres;

--
-- TOC entry 1672 (class 1259 OID 329691)
-- Dependencies: 2009 3
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
    worker character varying(255),
    creationtime timestamp without time zone,
    machinetime integer,
    machinetimebalance integer,
    labortime integer,
    labortimebalance integer,
    plannedtime integer,
    plannedmachinetime integer,
    plannedlabortime integer
);


ALTER TABLE public.productioncounting_productionrecord OWNER TO postgres;

--
-- TOC entry 1673 (class 1259 OID 329700)
-- Dependencies: 3
-- Name: productioncounting_recordoperationproductincomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_recordoperationproductincomponent (
    id bigint NOT NULL,
    productionrecord_id bigint,
    product_id bigint,
    usedquantity numeric(8,3),
    plannedquantity numeric(8,3),
    balance numeric(10,3)
);


ALTER TABLE public.productioncounting_recordoperationproductincomponent OWNER TO postgres;

--
-- TOC entry 1674 (class 1259 OID 329705)
-- Dependencies: 3
-- Name: productioncounting_recordoperationproductoutcomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productioncounting_recordoperationproductoutcomponent (
    id bigint NOT NULL,
    productionrecord_id bigint,
    product_id bigint,
    usedquantity numeric(8,3),
    plannedquantity numeric(8,3),
    balance numeric(10,3)
);


ALTER TABLE public.productioncounting_recordoperationproductoutcomponent OWNER TO postgres;

--
-- TOC entry 1675 (class 1259 OID 329710)
-- Dependencies: 2010 2011 2012 2013 2014 2015 2016 2017 2018 2019 2020 2021 2022 3
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
    machinehourlycost numeric(10,3) DEFAULT 0::numeric,
    laborhourlycost numeric(10,3) DEFAULT 0::numeric,
    pieceworkcost numeric(10,3) DEFAULT 0::numeric,
    numberofoperations integer DEFAULT 1
);


ALTER TABLE public.productionscheduling_orderoperationcomponent OWNER TO postgres;

--
-- TOC entry 1676 (class 1259 OID 329731)
-- Dependencies: 3
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
-- TOC entry 1677 (class 1259 OID 329739)
-- Dependencies: 3
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
-- TOC entry 1636 (class 1259 OID 329411)
-- Dependencies: 3
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
-- TOC entry 1678 (class 1259 OID 329747)
-- Dependencies: 3
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
-- TOC entry 1679 (class 1259 OID 329755)
-- Dependencies: 2023 3
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
-- TOC entry 1680 (class 1259 OID 329764)
-- Dependencies: 3
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
-- TOC entry 1681 (class 1259 OID 329772)
-- Dependencies: 2024 3
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
-- TOC entry 1682 (class 1259 OID 329781)
-- Dependencies: 3
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
-- TOC entry 1683 (class 1259 OID 329789)
-- Dependencies: 3
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
-- TOC entry 1684 (class 1259 OID 329797)
-- Dependencies: 3
-- Name: stoppage_stoppage; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE stoppage_stoppage (
    id bigint NOT NULL,
    order_id bigint,
    duration integer,
    reason text
);


ALTER TABLE public.stoppage_stoppage OWNER TO postgres;

--
-- TOC entry 1685 (class 1259 OID 329805)
-- Dependencies: 2025 2026 2027 2028 2029 2030 2031 2032 2033 2034 2035 2036 3
-- Name: technologies_operation; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE technologies_operation (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(2048),
    comment character varying(2048),
    staff_id bigint,
    machine_id bigint,
    attachment character varying(255),
    laborutilization numeric(6,3) DEFAULT 1.0,
    numberofoperations integer DEFAULT 1,
    timenextoperation integer DEFAULT 0,
    pieceworkcost numeric(10,3) DEFAULT 0::numeric,
    countmachineoperation numeric(6,3) DEFAULT 0::numeric,
    machinehourlycost numeric(10,3) DEFAULT 0::numeric,
    tj integer DEFAULT 0,
    laborhourlycost numeric(10,3) DEFAULT 0::numeric,
    countrealizedoperation character varying(255) DEFAULT '01all'::character varying,
    productioninonecycle numeric(10,3) DEFAULT 1::numeric,
    tpz integer DEFAULT 0,
    machineutilization numeric(6,3) DEFAULT 1.0
);


ALTER TABLE public.technologies_operation OWNER TO postgres;

--
-- TOC entry 1686 (class 1259 OID 329825)
-- Dependencies: 3
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
-- TOC entry 1687 (class 1259 OID 329830)
-- Dependencies: 3
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
-- TOC entry 1688 (class 1259 OID 329835)
-- Dependencies: 2037 2038 2039 3
-- Name: technologies_technology; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE technologies_technology (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(2048),
    product_id bigint,
    master boolean DEFAULT true,
    description character varying(2048),
    componentquantityalgorithm character varying(255) DEFAULT '01perProductOut'::character varying,
    unitsamplingnr numeric(10,3),
    shiftfeaturerequired boolean,
    minimalquantity numeric(8,3),
    postfeaturerequired boolean,
    qualitycontrolinstruction character varying(255),
    qualitycontroltype character varying(255),
    batchrequired boolean,
    otherfeaturerequired boolean,
    active boolean DEFAULT true
);


ALTER TABLE public.technologies_technology OWNER TO postgres;

--
-- TOC entry 1689 (class 1259 OID 329846)
-- Dependencies: 2040 2041 2042 2043 2044 2045 2046 2047 2048 2049 2050 2051 3
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
    tpz integer DEFAULT 0,
    countmachine numeric(10,3) DEFAULT 0::numeric,
    productioninonecycle numeric(10,3) DEFAULT 1::numeric,
    tj integer DEFAULT 0,
    machineutilization numeric(6,3) DEFAULT 1.0,
    laborutilization numeric(6,3) DEFAULT 1.0,
    machinehourlycost numeric(10,3) DEFAULT 0::numeric,
    countrealized character varying(255) DEFAULT '01all'::character varying,
    timenextoperation integer DEFAULT 0,
    numberofoperations integer DEFAULT 1,
    laborhourlycost numeric(10,3) DEFAULT 0::numeric,
    pieceworkcost numeric(10,3) DEFAULT 0::numeric
);


ALTER TABLE public.technologies_technologyoperationcomponent OWNER TO postgres;

--
-- TOC entry 1690 (class 1259 OID 329866)
-- Dependencies: 3
-- Name: workplans_workplan; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_workplan (
    id bigint NOT NULL,
    name character varying(255),
    date timestamp without time zone,
    worker character varying(255),
    generated boolean,
    filename character varying(255)
);


ALTER TABLE public.workplans_workplan OWNER TO postgres;

--
-- TOC entry 1691 (class 1259 OID 329874)
-- Dependencies: 3
-- Name: workplans_workplancomponent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE workplans_workplancomponent (
    id bigint NOT NULL,
    workplan_id bigint,
    order_id bigint
);


ALTER TABLE public.workplans_workplancomponent OWNER TO postgres;

--
-- TOC entry 2057 (class 2606 OID 329435)
-- Dependencies: 1639 1639
-- Name: basic_company_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_company
    ADD CONSTRAINT basic_company_pkey PRIMARY KEY (id);


--
-- TOC entry 2059 (class 2606 OID 329443)
-- Dependencies: 1640 1640
-- Name: basic_contractor_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_contractor
    ADD CONSTRAINT basic_contractor_pkey PRIMARY KEY (id);


--
-- TOC entry 2061 (class 2606 OID 329448)
-- Dependencies: 1641 1641
-- Name: basic_currency_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_currency
    ADD CONSTRAINT basic_currency_pkey PRIMARY KEY (id);


--
-- TOC entry 2063 (class 2606 OID 329456)
-- Dependencies: 1642 1642
-- Name: basic_machine_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_machine
    ADD CONSTRAINT basic_machine_pkey PRIMARY KEY (id);


--
-- TOC entry 2065 (class 2606 OID 329462)
-- Dependencies: 1643 1643
-- Name: basic_parameter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_parameter
    ADD CONSTRAINT basic_parameter_pkey PRIMARY KEY (id);


--
-- TOC entry 2067 (class 2606 OID 329475)
-- Dependencies: 1644 1644
-- Name: basic_product_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_product
    ADD CONSTRAINT basic_product_pkey PRIMARY KEY (id);


--
-- TOC entry 2069 (class 2606 OID 329490)
-- Dependencies: 1645 1645
-- Name: basic_shift_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_shift
    ADD CONSTRAINT basic_shift_pkey PRIMARY KEY (id);


--
-- TOC entry 2071 (class 2606 OID 329499)
-- Dependencies: 1646 1646
-- Name: basic_shifttimetableexception_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_shifttimetableexception
    ADD CONSTRAINT basic_shifttimetableexception_pkey PRIMARY KEY (id);


--
-- TOC entry 2073 (class 2606 OID 329507)
-- Dependencies: 1647 1647
-- Name: basic_staff_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_staff
    ADD CONSTRAINT basic_staff_pkey PRIMARY KEY (id);


--
-- TOC entry 2075 (class 2606 OID 329512)
-- Dependencies: 1648 1648
-- Name: basic_substitute_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_substitute
    ADD CONSTRAINT basic_substitute_pkey PRIMARY KEY (id);


--
-- TOC entry 2077 (class 2606 OID 329517)
-- Dependencies: 1649 1649
-- Name: basic_substitutecomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basic_substitutecomponent
    ADD CONSTRAINT basic_substitutecomponent_pkey PRIMARY KEY (id);


--
-- TOC entry 2055 (class 2606 OID 329425)
-- Dependencies: 1638 1638
-- Name: basicproductioncounting_basicproductioncounting_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY basicproductioncounting_basicproductioncounting
    ADD CONSTRAINT basicproductioncounting_basicproductioncounting_pkey PRIMARY KEY (id);


--
-- TOC entry 2079 (class 2606 OID 329532)
-- Dependencies: 1650 1650
-- Name: costcalculation_costcalculation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY costcalculation_costcalculation
    ADD CONSTRAINT costcalculation_costcalculation_pkey PRIMARY KEY (id);


--
-- TOC entry 2081 (class 2606 OID 329552)
-- Dependencies: 1651 1651
-- Name: costnormsforoperation_calculationoperationcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY costnormsforoperation_calculationoperationcomponent
    ADD CONSTRAINT costnormsforoperation_calculationoperationcomponent_pkey PRIMARY KEY (id);


--
-- TOC entry 2087 (class 2606 OID 329573)
-- Dependencies: 1654 1654
-- Name: genealogies_currentattribute_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY genealogies_currentattribute
    ADD CONSTRAINT genealogies_currentattribute_pkey PRIMARY KEY (id);


--
-- TOC entry 2089 (class 2606 OID 329581)
-- Dependencies: 1655 1655
-- Name: genealogies_genealogy_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY genealogies_genealogy
    ADD CONSTRAINT genealogies_genealogy_pkey PRIMARY KEY (id);


--
-- TOC entry 2091 (class 2606 OID 329589)
-- Dependencies: 1656 1656
-- Name: genealogies_otherfeature_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY genealogies_otherfeature
    ADD CONSTRAINT genealogies_otherfeature_pkey PRIMARY KEY (id);


--
-- TOC entry 2093 (class 2606 OID 329597)
-- Dependencies: 1657 1657
-- Name: genealogies_postfeature_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY genealogies_postfeature
    ADD CONSTRAINT genealogies_postfeature_pkey PRIMARY KEY (id);


--
-- TOC entry 2095 (class 2606 OID 329602)
-- Dependencies: 1658 1658
-- Name: genealogies_shiftfeature_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY genealogies_shiftfeature
    ADD CONSTRAINT genealogies_shiftfeature_pkey PRIMARY KEY (id);


--
-- TOC entry 2083 (class 2606 OID 329557)
-- Dependencies: 1652 1652
-- Name: genealogiesforcomponents_genealogyproductincomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY genealogiesforcomponents_genealogyproductincomponent
    ADD CONSTRAINT genealogiesforcomponents_genealogyproductincomponent_pkey PRIMARY KEY (id);


--
-- TOC entry 2085 (class 2606 OID 329565)
-- Dependencies: 1653 1653
-- Name: genealogiesforcomponents_productinbatch_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY genealogiesforcomponents_productinbatch
    ADD CONSTRAINT genealogiesforcomponents_productinbatch_pkey PRIMARY KEY (id);


--
-- TOC entry 2097 (class 2606 OID 329607)
-- Dependencies: 1659 1659 1659
-- Name: jointable_materialrequirement_order_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY jointable_materialrequirement_order
    ADD CONSTRAINT jointable_materialrequirement_order_pkey PRIMARY KEY (materialrequirement_id, order_id);


--
-- TOC entry 2099 (class 2606 OID 329615)
-- Dependencies: 1660 1660
-- Name: materialflow_materialsinstockareas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflow_materialsinstockareas
    ADD CONSTRAINT materialflow_materialsinstockareas_pkey PRIMARY KEY (id);


--
-- TOC entry 2101 (class 2606 OID 329620)
-- Dependencies: 1661 1661
-- Name: materialflow_materialsinstockareascomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflow_materialsinstockareascomponent
    ADD CONSTRAINT materialflow_materialsinstockareascomponent_pkey PRIMARY KEY (id);


--
-- TOC entry 2103 (class 2606 OID 329625)
-- Dependencies: 1662 1662
-- Name: materialflow_stockareas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflow_stockareas
    ADD CONSTRAINT materialflow_stockareas_pkey PRIMARY KEY (id);


--
-- TOC entry 2105 (class 2606 OID 329630)
-- Dependencies: 1663 1663
-- Name: materialflow_stockcorrection_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflow_stockcorrection
    ADD CONSTRAINT materialflow_stockcorrection_pkey PRIMARY KEY (id);


--
-- TOC entry 2107 (class 2606 OID 329635)
-- Dependencies: 1664 1664
-- Name: materialflow_transfer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT materialflow_transfer_pkey PRIMARY KEY (id);


--
-- TOC entry 2109 (class 2606 OID 329640)
-- Dependencies: 1665 1665
-- Name: materialflow_transformations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialflow_transformations
    ADD CONSTRAINT materialflow_transformations_pkey PRIMARY KEY (id);


--
-- TOC entry 2111 (class 2606 OID 329649)
-- Dependencies: 1666 1666
-- Name: materialrequirements_materialrequirement_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY materialrequirements_materialrequirement
    ADD CONSTRAINT materialrequirements_materialrequirement_pkey PRIMARY KEY (id);


--
-- TOC entry 2113 (class 2606 OID 329654)
-- Dependencies: 1667 1667
-- Name: ordergroups_ordergroup_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ordergroups_ordergroup
    ADD CONSTRAINT ordergroups_ordergroup_pkey PRIMARY KEY (id);


--
-- TOC entry 2115 (class 2606 OID 329663)
-- Dependencies: 1668 1668
-- Name: orders_logging_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY orders_logging
    ADD CONSTRAINT orders_logging_pkey PRIMARY KEY (id);


--
-- TOC entry 2117 (class 2606 OID 329674)
-- Dependencies: 1669 1669
-- Name: orders_order_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY orders_order
    ADD CONSTRAINT orders_order_pkey PRIMARY KEY (id);


--
-- TOC entry 2119 (class 2606 OID 329682)
-- Dependencies: 1670 1670
-- Name: productioncounting_productionbalance_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncounting_productionbalance
    ADD CONSTRAINT productioncounting_productionbalance_pkey PRIMARY KEY (id);


--
-- TOC entry 2121 (class 2606 OID 329690)
-- Dependencies: 1671 1671
-- Name: productioncounting_productioncounting_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncounting_productioncounting
    ADD CONSTRAINT productioncounting_productioncounting_pkey PRIMARY KEY (id);


--
-- TOC entry 2123 (class 2606 OID 329699)
-- Dependencies: 1672 1672
-- Name: productioncounting_productionrecord_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncounting_productionrecord
    ADD CONSTRAINT productioncounting_productionrecord_pkey PRIMARY KEY (id);


--
-- TOC entry 2125 (class 2606 OID 329704)
-- Dependencies: 1673 1673
-- Name: productioncounting_recordoperationproductincomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncounting_recordoperationproductincomponent
    ADD CONSTRAINT productioncounting_recordoperationproductincomponent_pkey PRIMARY KEY (id);


--
-- TOC entry 2127 (class 2606 OID 329709)
-- Dependencies: 1674 1674
-- Name: productioncounting_recordoperationproductoutcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productioncounting_recordoperationproductoutcomponent
    ADD CONSTRAINT productioncounting_recordoperationproductoutcomponent_pkey PRIMARY KEY (id);


--
-- TOC entry 2129 (class 2606 OID 329730)
-- Dependencies: 1675 1675
-- Name: productionscheduling_orderoperationcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productionscheduling_orderoperationcomponent
    ADD CONSTRAINT productionscheduling_orderoperationcomponent_pkey PRIMARY KEY (id);


--
-- TOC entry 2131 (class 2606 OID 329738)
-- Dependencies: 1676 1676
-- Name: qcadoomodel_dictionary_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qcadoomodel_dictionary
    ADD CONSTRAINT qcadoomodel_dictionary_pkey PRIMARY KEY (id);


--
-- TOC entry 2133 (class 2606 OID 329746)
-- Dependencies: 1677 1677
-- Name: qcadoomodel_dictionaryitem_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qcadoomodel_dictionaryitem
    ADD CONSTRAINT qcadoomodel_dictionaryitem_pkey PRIMARY KEY (id);


--
-- TOC entry 2053 (class 2606 OID 329418)
-- Dependencies: 1636 1636
-- Name: qcadooplugin_plugin_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qcadooplugin_plugin
    ADD CONSTRAINT qcadooplugin_plugin_pkey PRIMARY KEY (id);


--
-- TOC entry 2135 (class 2606 OID 329754)
-- Dependencies: 1678 1678
-- Name: qcadoosecurity_persistenttoken_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qcadoosecurity_persistenttoken
    ADD CONSTRAINT qcadoosecurity_persistenttoken_pkey PRIMARY KEY (id);


--
-- TOC entry 2137 (class 2606 OID 329763)
-- Dependencies: 1679 1679
-- Name: qcadoosecurity_user_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qcadoosecurity_user
    ADD CONSTRAINT qcadoosecurity_user_pkey PRIMARY KEY (id);


--
-- TOC entry 2139 (class 2606 OID 329771)
-- Dependencies: 1680 1680
-- Name: qcadooview_category_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qcadooview_category
    ADD CONSTRAINT qcadooview_category_pkey PRIMARY KEY (id);


--
-- TOC entry 2141 (class 2606 OID 329780)
-- Dependencies: 1681 1681
-- Name: qcadooview_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qcadooview_item
    ADD CONSTRAINT qcadooview_item_pkey PRIMARY KEY (id);


--
-- TOC entry 2143 (class 2606 OID 329788)
-- Dependencies: 1682 1682
-- Name: qcadooview_view_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qcadooview_view
    ADD CONSTRAINT qcadooview_view_pkey PRIMARY KEY (id);


--
-- TOC entry 2145 (class 2606 OID 329796)
-- Dependencies: 1683 1683
-- Name: qualitycontrols_qualitycontrol_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qualitycontrols_qualitycontrol
    ADD CONSTRAINT qualitycontrols_qualitycontrol_pkey PRIMARY KEY (id);


--
-- TOC entry 2147 (class 2606 OID 329804)
-- Dependencies: 1684 1684
-- Name: stoppage_stoppage_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY stoppage_stoppage
    ADD CONSTRAINT stoppage_stoppage_pkey PRIMARY KEY (id);


--
-- TOC entry 2149 (class 2606 OID 329824)
-- Dependencies: 1685 1685
-- Name: technologies_operation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY technologies_operation
    ADD CONSTRAINT technologies_operation_pkey PRIMARY KEY (id);


--
-- TOC entry 2151 (class 2606 OID 329829)
-- Dependencies: 1686 1686
-- Name: technologies_operationproductincomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY technologies_operationproductincomponent
    ADD CONSTRAINT technologies_operationproductincomponent_pkey PRIMARY KEY (id);


--
-- TOC entry 2153 (class 2606 OID 329834)
-- Dependencies: 1687 1687
-- Name: technologies_operationproductoutcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY technologies_operationproductoutcomponent
    ADD CONSTRAINT technologies_operationproductoutcomponent_pkey PRIMARY KEY (id);


--
-- TOC entry 2155 (class 2606 OID 329845)
-- Dependencies: 1688 1688
-- Name: technologies_technology_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY technologies_technology
    ADD CONSTRAINT technologies_technology_pkey PRIMARY KEY (id);


--
-- TOC entry 2157 (class 2606 OID 329865)
-- Dependencies: 1689 1689
-- Name: technologies_technologyoperationcomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY technologies_technologyoperationcomponent
    ADD CONSTRAINT technologies_technologyoperationcomponent_pkey PRIMARY KEY (id);


--
-- TOC entry 2159 (class 2606 OID 329873)
-- Dependencies: 1690 1690
-- Name: workplans_workplan_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY workplans_workplan
    ADD CONSTRAINT workplans_workplan_pkey PRIMARY KEY (id);


--
-- TOC entry 2161 (class 2606 OID 329878)
-- Dependencies: 1691 1691
-- Name: workplans_workplancomponent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY workplans_workplancomponent
    ADD CONSTRAINT workplans_workplancomponent_pkey PRIMARY KEY (id);


--
-- TOC entry 2238 (class 2606 OID 330259)
-- Dependencies: 1689 1688 2154
-- Name: fk2337e2f7574cfe41; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technologyoperationcomponent
    ADD CONSTRAINT fk2337e2f7574cfe41 FOREIGN KEY (referencetechnology_id) REFERENCES technologies_technology(id);


--
-- TOC entry 2239 (class 2606 OID 330264)
-- Dependencies: 1689 1685 2148
-- Name: fk2337e2f7b1e1a8a8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technologyoperationcomponent
    ADD CONSTRAINT fk2337e2f7b1e1a8a8 FOREIGN KEY (operation_id) REFERENCES technologies_operation(id);


--
-- TOC entry 2240 (class 2606 OID 330269)
-- Dependencies: 1689 1689 2156
-- Name: fk2337e2f7b4851f44; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technologyoperationcomponent
    ADD CONSTRAINT fk2337e2f7b4851f44 FOREIGN KEY (parent_id) REFERENCES technologies_technologyoperationcomponent(id);


--
-- TOC entry 2237 (class 2606 OID 330254)
-- Dependencies: 1688 1689 2154
-- Name: fk2337e2f7e3afcbac; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technologyoperationcomponent
    ADD CONSTRAINT fk2337e2f7e3afcbac FOREIGN KEY (technology_id) REFERENCES technologies_technology(id);


--
-- TOC entry 2179 (class 2606 OID 329964)
-- Dependencies: 2082 1652 1653
-- Name: fk29b24045f52c9469; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogiesforcomponents_productinbatch
    ADD CONSTRAINT fk29b24045f52c9469 FOREIGN KEY (productincomponent_id) REFERENCES genealogiesforcomponents_genealogyproductincomponent(id);


--
-- TOC entry 2183 (class 2606 OID 329984)
-- Dependencies: 1657 1655 2088
-- Name: fk2c8b41f6ac0386c6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogies_postfeature
    ADD CONSTRAINT fk2c8b41f6ac0386c6 FOREIGN KEY (genealogy_id) REFERENCES genealogies_genealogy(id);


--
-- TOC entry 2229 (class 2606 OID 330214)
-- Dependencies: 2116 1669 1684
-- Name: fk31da647fb64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY stoppage_stoppage
    ADD CONSTRAINT fk31da647fb64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- TOC entry 2206 (class 2606 OID 330099)
-- Dependencies: 1669 2066 1644
-- Name: fk3daecd74ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY orders_order
    ADD CONSTRAINT fk3daecd74ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- TOC entry 2207 (class 2606 OID 330104)
-- Dependencies: 1640 1669 2058
-- Name: fk3daecd74aea6e4cc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY orders_order
    ADD CONSTRAINT fk3daecd74aea6e4cc FOREIGN KEY (contractor_id) REFERENCES basic_contractor(id);


--
-- TOC entry 2204 (class 2606 OID 330089)
-- Dependencies: 1667 2112 1669
-- Name: fk3daecd74c7f35254; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY orders_order
    ADD CONSTRAINT fk3daecd74c7f35254 FOREIGN KEY (ordergroup_id) REFERENCES ordergroups_ordergroup(id);


--
-- TOC entry 2205 (class 2606 OID 330094)
-- Dependencies: 1669 1688 2154
-- Name: fk3daecd74e3afcbac; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY orders_order
    ADD CONSTRAINT fk3daecd74e3afcbac FOREIGN KEY (technology_id) REFERENCES technologies_technology(id);


--
-- TOC entry 2241 (class 2606 OID 330274)
-- Dependencies: 1691 2158 1690
-- Name: fk3de2c75d1533ae2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_workplancomponent
    ADD CONSTRAINT fk3de2c75d1533ae2 FOREIGN KEY (workplan_id) REFERENCES workplans_workplan(id);


--
-- TOC entry 2242 (class 2606 OID 330279)
-- Dependencies: 1691 2116 1669
-- Name: fk3de2c75db64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY workplans_workplancomponent
    ADD CONSTRAINT fk3de2c75db64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- TOC entry 2231 (class 2606 OID 330224)
-- Dependencies: 2062 1642 1685
-- Name: fk47ea46121cd7d268; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_operation
    ADD CONSTRAINT fk47ea46121cd7d268 FOREIGN KEY (machine_id) REFERENCES basic_machine(id);


--
-- TOC entry 2230 (class 2606 OID 330219)
-- Dependencies: 1685 2072 1647
-- Name: fk47ea46121e9fcb48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_operation
    ADD CONSTRAINT fk47ea46121e9fcb48 FOREIGN KEY (staff_id) REFERENCES basic_staff(id);


--
-- TOC entry 2180 (class 2606 OID 329969)
-- Dependencies: 1645 1654 2068
-- Name: fk4964c163b17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogies_currentattribute
    ADD CONSTRAINT fk4964c163b17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- TOC entry 2236 (class 2606 OID 330249)
-- Dependencies: 1688 2066 1644
-- Name: fk510629c1ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_technology
    ADD CONSTRAINT fk510629c1ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- TOC entry 2163 (class 2606 OID 329884)
-- Dependencies: 1644 1638 2066
-- Name: fk5ac920f5ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basicproductioncounting_basicproductioncounting
    ADD CONSTRAINT fk5ac920f5ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- TOC entry 2162 (class 2606 OID 329879)
-- Dependencies: 1669 2116 1638
-- Name: fk5ac920f5b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basicproductioncounting_basicproductioncounting
    ADD CONSTRAINT fk5ac920f5b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- TOC entry 2218 (class 2606 OID 330159)
-- Dependencies: 1674 1644 2066
-- Name: fk5bdc58bbad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_recordoperationproductoutcomponent
    ADD CONSTRAINT fk5bdc58bbad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- TOC entry 2217 (class 2606 OID 330154)
-- Dependencies: 2122 1674 1672
-- Name: fk5bdc58bbfeff14c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_recordoperationproductoutcomponent
    ADD CONSTRAINT fk5bdc58bbfeff14c FOREIGN KEY (productionrecord_id) REFERENCES productioncounting_productionrecord(id);


--
-- TOC entry 2214 (class 2606 OID 330139)
-- Dependencies: 1672 1675 2128
-- Name: fk5d2719fd7f0f0b28; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionrecord
    ADD CONSTRAINT fk5d2719fd7f0f0b28 FOREIGN KEY (orderoperationcomponent_id) REFERENCES productionscheduling_orderoperationcomponent(id);


--
-- TOC entry 2212 (class 2606 OID 330129)
-- Dependencies: 1645 2068 1672
-- Name: fk5d2719fdb17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionrecord
    ADD CONSTRAINT fk5d2719fdb17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- TOC entry 2213 (class 2606 OID 330134)
-- Dependencies: 1672 1669 2116
-- Name: fk5d2719fdb64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionrecord
    ADD CONSTRAINT fk5d2719fdb64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- TOC entry 2221 (class 2606 OID 330174)
-- Dependencies: 2156 1675 1689
-- Name: fk5f6c432154b936c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productionscheduling_orderoperationcomponent
    ADD CONSTRAINT fk5f6c432154b936c FOREIGN KEY (technologyoperationcomponent_id) REFERENCES technologies_technologyoperationcomponent(id);


--
-- TOC entry 2223 (class 2606 OID 330184)
-- Dependencies: 2128 1675 1675
-- Name: fk5f6c43248833c02; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productionscheduling_orderoperationcomponent
    ADD CONSTRAINT fk5f6c43248833c02 FOREIGN KEY (parent_id) REFERENCES productionscheduling_orderoperationcomponent(id);


--
-- TOC entry 2222 (class 2606 OID 330179)
-- Dependencies: 2148 1685 1675
-- Name: fk5f6c432b1e1a8a8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productionscheduling_orderoperationcomponent
    ADD CONSTRAINT fk5f6c432b1e1a8a8 FOREIGN KEY (operation_id) REFERENCES technologies_operation(id);


--
-- TOC entry 2220 (class 2606 OID 330169)
-- Dependencies: 1675 1669 2116
-- Name: fk5f6c432b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productionscheduling_orderoperationcomponent
    ADD CONSTRAINT fk5f6c432b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- TOC entry 2219 (class 2606 OID 330164)
-- Dependencies: 2154 1675 1688
-- Name: fk5f6c432e3afcbac; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productionscheduling_orderoperationcomponent
    ADD CONSTRAINT fk5f6c432e3afcbac FOREIGN KEY (technology_id) REFERENCES technologies_technology(id);


--
-- TOC entry 2181 (class 2606 OID 329974)
-- Dependencies: 2116 1669 1655
-- Name: fk686fa361b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogies_genealogy
    ADD CONSTRAINT fk686fa361b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- TOC entry 2189 (class 2606 OID 330014)
-- Dependencies: 2102 1662 1661
-- Name: fk6c887326308f12ec; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_materialsinstockareascomponent
    ADD CONSTRAINT fk6c887326308f12ec FOREIGN KEY (stockareas_id) REFERENCES materialflow_stockareas(id);


--
-- TOC entry 2188 (class 2606 OID 330009)
-- Dependencies: 2098 1661 1660
-- Name: fk6c887326d11277a8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_materialsinstockareascomponent
    ADD CONSTRAINT fk6c887326d11277a8 FOREIGN KEY (materialsinstockareas_id) REFERENCES materialflow_materialsinstockareas(id);


--
-- TOC entry 2186 (class 2606 OID 329999)
-- Dependencies: 1659 1666 2110
-- Name: fk78f5fa302f1f24c8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY jointable_materialrequirement_order
    ADD CONSTRAINT fk78f5fa302f1f24c8 FOREIGN KEY (materialrequirement_id) REFERENCES materialrequirements_materialrequirement(id);


--
-- TOC entry 2187 (class 2606 OID 330004)
-- Dependencies: 1659 1669 2116
-- Name: fk78f5fa30b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY jointable_materialrequirement_order
    ADD CONSTRAINT fk78f5fa30b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- TOC entry 2234 (class 2606 OID 330239)
-- Dependencies: 1689 2156 1687
-- Name: fk79b7ec6ca29938f8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_operationproductoutcomponent
    ADD CONSTRAINT fk79b7ec6ca29938f8 FOREIGN KEY (operationcomponent_id) REFERENCES technologies_technologyoperationcomponent(id);


--
-- TOC entry 2235 (class 2606 OID 330244)
-- Dependencies: 1687 2066 1644
-- Name: fk79b7ec6cad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_operationproductoutcomponent
    ADD CONSTRAINT fk79b7ec6cad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- TOC entry 2227 (class 2606 OID 330204)
-- Dependencies: 1669 1683 2116
-- Name: fk89ba7c08b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qualitycontrols_qualitycontrol
    ADD CONSTRAINT fk89ba7c08b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- TOC entry 2228 (class 2606 OID 330209)
-- Dependencies: 1683 2128 1675
-- Name: fk89ba7c08bcc5da25; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qualitycontrols_qualitycontrol
    ADD CONSTRAINT fk89ba7c08bcc5da25 FOREIGN KEY (operation_id) REFERENCES productionscheduling_orderoperationcomponent(id);


--
-- TOC entry 2194 (class 2606 OID 330039)
-- Dependencies: 1647 2072 1664
-- Name: fk8c32fdf51e9fcb48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT fk8c32fdf51e9fcb48 FOREIGN KEY (staff_id) REFERENCES basic_staff(id);


--
-- TOC entry 2193 (class 2606 OID 330034)
-- Dependencies: 1665 1664 2108
-- Name: fk8c32fdf5403d0e8f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT fk8c32fdf5403d0e8f FOREIGN KEY (transformationsproduction_id) REFERENCES materialflow_transformations(id);


--
-- TOC entry 2196 (class 2606 OID 330049)
-- Dependencies: 1644 2066 1664
-- Name: fk8c32fdf5ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT fk8c32fdf5ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- TOC entry 2195 (class 2606 OID 330044)
-- Dependencies: 1664 1662 2102
-- Name: fk8c32fdf5b0a8fa91; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT fk8c32fdf5b0a8fa91 FOREIGN KEY (stockareasto_id) REFERENCES materialflow_stockareas(id);


--
-- TOC entry 2198 (class 2606 OID 330059)
-- Dependencies: 1664 1662 2102
-- Name: fk8c32fdf5b2277b02; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT fk8c32fdf5b2277b02 FOREIGN KEY (stockareasfrom_id) REFERENCES materialflow_stockareas(id);


--
-- TOC entry 2197 (class 2606 OID 330054)
-- Dependencies: 2108 1665 1664
-- Name: fk8c32fdf5d8bb7bc1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transfer
    ADD CONSTRAINT fk8c32fdf5d8bb7bc1 FOREIGN KEY (transformationsconsumption_id) REFERENCES materialflow_transformations(id);


--
-- TOC entry 2182 (class 2606 OID 329979)
-- Dependencies: 1656 2088 1655
-- Name: fk8f123e06ac0386c6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogies_otherfeature
    ADD CONSTRAINT fk8f123e06ac0386c6 FOREIGN KEY (genealogy_id) REFERENCES genealogies_genealogy(id);


--
-- TOC entry 2165 (class 2606 OID 329894)
-- Dependencies: 1646 2068 1645
-- Name: fk95347f3fb17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_shifttimetableexception
    ADD CONSTRAINT fk95347f3fb17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- TOC entry 2224 (class 2606 OID 330189)
-- Dependencies: 2130 1676 1677
-- Name: fk9b37ca9470d83278; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qcadoomodel_dictionaryitem
    ADD CONSTRAINT fk9b37ca9470d83278 FOREIGN KEY (dictionary_id) REFERENCES qcadoomodel_dictionary(id);


--
-- TOC entry 2216 (class 2606 OID 330149)
-- Dependencies: 1673 2066 1644
-- Name: fka223986cad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_recordoperationproductincomponent
    ADD CONSTRAINT fka223986cad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- TOC entry 2215 (class 2606 OID 330144)
-- Dependencies: 1673 2122 1672
-- Name: fka223986cfeff14c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_recordoperationproductincomponent
    ADD CONSTRAINT fka223986cfeff14c FOREIGN KEY (productionrecord_id) REFERENCES productioncounting_productionrecord(id);


--
-- TOC entry 2199 (class 2606 OID 330064)
-- Dependencies: 1647 2072 1665
-- Name: fka83409401e9fcb48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transformations
    ADD CONSTRAINT fka83409401e9fcb48 FOREIGN KEY (staff_id) REFERENCES basic_staff(id);


--
-- TOC entry 2200 (class 2606 OID 330069)
-- Dependencies: 1665 2102 1662
-- Name: fka8340940b0a8fa91; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transformations
    ADD CONSTRAINT fka8340940b0a8fa91 FOREIGN KEY (stockareasto_id) REFERENCES materialflow_stockareas(id);


--
-- TOC entry 2201 (class 2606 OID 330074)
-- Dependencies: 1665 1662 2102
-- Name: fka8340940b2277b02; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_transformations
    ADD CONSTRAINT fka8340940b2277b02 FOREIGN KEY (stockareasfrom_id) REFERENCES materialflow_stockareas(id);


--
-- TOC entry 2232 (class 2606 OID 330229)
-- Dependencies: 2156 1686 1689
-- Name: fkb39e4a9ba29938f8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_operationproductincomponent
    ADD CONSTRAINT fkb39e4a9ba29938f8 FOREIGN KEY (operationcomponent_id) REFERENCES technologies_technologyoperationcomponent(id);


--
-- TOC entry 2233 (class 2606 OID 330234)
-- Dependencies: 1644 1686 2066
-- Name: fkb39e4a9bad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY technologies_operationproductincomponent
    ADD CONSTRAINT fkb39e4a9bad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- TOC entry 2185 (class 2606 OID 329994)
-- Dependencies: 1658 2068 1645
-- Name: fkb70ac547cbfd0b9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogies_shiftfeature
    ADD CONSTRAINT fkb70ac547cbfd0b9 FOREIGN KEY (value_id) REFERENCES basic_shift(id);


--
-- TOC entry 2184 (class 2606 OID 329989)
-- Dependencies: 2088 1658 1655
-- Name: fkb70ac54ac0386c6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogies_shiftfeature
    ADD CONSTRAINT fkb70ac54ac0386c6 FOREIGN KEY (genealogy_id) REFERENCES genealogies_genealogy(id);


--
-- TOC entry 2166 (class 2606 OID 329899)
-- Dependencies: 2066 1648 1644
-- Name: fkbebf5d4bad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_substitute
    ADD CONSTRAINT fkbebf5d4bad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- TOC entry 2174 (class 2606 OID 329939)
-- Dependencies: 2156 1689 1651
-- Name: fkbf24a028154b936c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costnormsforoperation_calculationoperationcomponent
    ADD CONSTRAINT fkbf24a028154b936c FOREIGN KEY (technologyoperationcomponent_id) REFERENCES technologies_technologyoperationcomponent(id);


--
-- TOC entry 2173 (class 2606 OID 329934)
-- Dependencies: 2078 1651 1650
-- Name: fkbf24a0282ee8598c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costnormsforoperation_calculationoperationcomponent
    ADD CONSTRAINT fkbf24a0282ee8598c FOREIGN KEY (costcalculation_id) REFERENCES costcalculation_costcalculation(id);


--
-- TOC entry 2175 (class 2606 OID 329944)
-- Dependencies: 1685 2148 1651
-- Name: fkbf24a028b1e1a8a8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costnormsforoperation_calculationoperationcomponent
    ADD CONSTRAINT fkbf24a028b1e1a8a8 FOREIGN KEY (operation_id) REFERENCES technologies_operation(id);


--
-- TOC entry 2176 (class 2606 OID 329949)
-- Dependencies: 2080 1651 1651
-- Name: fkbf24a028eeb36669; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costnormsforoperation_calculationoperationcomponent
    ADD CONSTRAINT fkbf24a028eeb36669 FOREIGN KEY (parent_id) REFERENCES costnormsforoperation_calculationoperationcomponent(id);


--
-- TOC entry 2211 (class 2606 OID 330124)
-- Dependencies: 2066 1671 1644
-- Name: fkc93f6b1fad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productioncounting
    ADD CONSTRAINT fkc93f6b1fad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- TOC entry 2210 (class 2606 OID 330119)
-- Dependencies: 1669 1671 2116
-- Name: fkc93f6b1fb64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productioncounting
    ADD CONSTRAINT fkc93f6b1fb64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- TOC entry 2190 (class 2606 OID 330019)
-- Dependencies: 1647 1663 2072
-- Name: fkc9d449ca1e9fcb48; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_stockcorrection
    ADD CONSTRAINT fkc9d449ca1e9fcb48 FOREIGN KEY (staff_id) REFERENCES basic_staff(id);


--
-- TOC entry 2192 (class 2606 OID 330029)
-- Dependencies: 1663 1662 2102
-- Name: fkc9d449ca308f12ec; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_stockcorrection
    ADD CONSTRAINT fkc9d449ca308f12ec FOREIGN KEY (stockareas_id) REFERENCES materialflow_stockareas(id);


--
-- TOC entry 2191 (class 2606 OID 330024)
-- Dependencies: 1663 1644 2066
-- Name: fkc9d449caad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY materialflow_stockcorrection
    ADD CONSTRAINT fkc9d449caad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- TOC entry 2167 (class 2606 OID 329904)
-- Dependencies: 1648 1649 2074
-- Name: fkcbbea3f2717076ac; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_substitutecomponent
    ADD CONSTRAINT fkcbbea3f2717076ac FOREIGN KEY (substitute_id) REFERENCES basic_substitute(id);


--
-- TOC entry 2168 (class 2606 OID 329909)
-- Dependencies: 1649 2066 1644
-- Name: fkcbbea3f2ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_substitutecomponent
    ADD CONSTRAINT fkcbbea3f2ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- TOC entry 2171 (class 2606 OID 329924)
-- Dependencies: 1644 1650 2066
-- Name: fkcfbe2739ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costcalculation_costcalculation
    ADD CONSTRAINT fkcfbe2739ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- TOC entry 2170 (class 2606 OID 329919)
-- Dependencies: 1669 2116 1650
-- Name: fkcfbe2739b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costcalculation_costcalculation
    ADD CONSTRAINT fkcfbe2739b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- TOC entry 2172 (class 2606 OID 329929)
-- Dependencies: 1688 1650 2154
-- Name: fkcfbe2739be57e70b; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costcalculation_costcalculation
    ADD CONSTRAINT fkcfbe2739be57e70b FOREIGN KEY (defaulttechnology_id) REFERENCES technologies_technology(id);


--
-- TOC entry 2169 (class 2606 OID 329914)
-- Dependencies: 1688 2154 1650
-- Name: fkcfbe2739e3afcbac; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY costcalculation_costcalculation
    ADD CONSTRAINT fkcfbe2739e3afcbac FOREIGN KEY (technology_id) REFERENCES technologies_technology(id);


--
-- TOC entry 2177 (class 2606 OID 329954)
-- Dependencies: 2150 1652 1686
-- Name: fkd14459ab80ff2f6f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogiesforcomponents_genealogyproductincomponent
    ADD CONSTRAINT fkd14459ab80ff2f6f FOREIGN KEY (productincomponent_id) REFERENCES technologies_operationproductincomponent(id);


--
-- TOC entry 2178 (class 2606 OID 329959)
-- Dependencies: 1655 1652 2088
-- Name: fkd14459abac0386c6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY genealogiesforcomponents_genealogyproductincomponent
    ADD CONSTRAINT fkd14459abac0386c6 FOREIGN KEY (genealogy_id) REFERENCES genealogies_genealogy(id);


--
-- TOC entry 2202 (class 2606 OID 330079)
-- Dependencies: 1645 1668 2068
-- Name: fke98b2005b17cd008; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY orders_logging
    ADD CONSTRAINT fke98b2005b17cd008 FOREIGN KEY (shift_id) REFERENCES basic_shift(id);


--
-- TOC entry 2203 (class 2606 OID 330084)
-- Dependencies: 1669 2116 1668
-- Name: fke98b2005b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY orders_logging
    ADD CONSTRAINT fke98b2005b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- TOC entry 2209 (class 2606 OID 330114)
-- Dependencies: 1644 1670 2066
-- Name: fkf2fd76b0ad773168; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionbalance
    ADD CONSTRAINT fkf2fd76b0ad773168 FOREIGN KEY (product_id) REFERENCES basic_product(id);


--
-- TOC entry 2208 (class 2606 OID 330109)
-- Dependencies: 2116 1670 1669
-- Name: fkf2fd76b0b64bada8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY productioncounting_productionbalance
    ADD CONSTRAINT fkf2fd76b0b64bada8 FOREIGN KEY (order_id) REFERENCES orders_order(id);


--
-- TOC entry 2164 (class 2606 OID 329889)
-- Dependencies: 2060 1641 1643
-- Name: fkf7f1a0d8db69d3cc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY basic_parameter
    ADD CONSTRAINT fkf7f1a0d8db69d3cc FOREIGN KEY (currency_id) REFERENCES basic_currency(id);


--
-- TOC entry 2225 (class 2606 OID 330194)
-- Dependencies: 1681 2142 1682
-- Name: fkf855759847760b8c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qcadooview_item
    ADD CONSTRAINT fkf855759847760b8c FOREIGN KEY (view_id) REFERENCES qcadooview_view(id);


--
-- TOC entry 2226 (class 2606 OID 330199)
-- Dependencies: 1680 1681 2138
-- Name: fkf85575986065f7ec; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qcadooview_item
    ADD CONSTRAINT fkf85575986065f7ec FOREIGN KEY (category_id) REFERENCES qcadooview_category(id);


--
-- TOC entry 2247 (class 0 OID 0)
-- Dependencies: 3
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2011-11-16 12:18:57 CET

--
-- PostgreSQL database dump complete
--

