-- Added new parameter rr
-- last touched 28.09.2016 by kasi

ALTER TABLE basic_parameter ADD COLUMN autorecalculateorder boolean;

-- end

-- added pps report
-- last touched 06.10.2016 by kama

CREATE TABLE productionpershift_ppsreport
(
  id bigint NOT NULL,
  "number" character varying(1024),
  name character varying(1024),
  datefrom date,
  dateto date,
  filename character varying(255),
  generated boolean DEFAULT false,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT productionpershift_ppsreport_pkey PRIMARY KEY (id)
);

-- end

-- last touched 21.10.2016 by kasi
ALTER TABLE orders_order ADD COLUMN finalproductiontracking boolean DEFAULT false;
-- end

-- sequences functions added
-- last touched 21.10.2016 by bafl&towo
CREATE OR REPLACE FUNCTION add_sequences() RETURNS VOID AS
$$
DECLARE
    row record;
BEGIN
    FOR row IN SELECT tablename FROM pg_tables p 
                INNER JOIN information_schema.columns c ON p.tablename = c.table_name 
                WHERE c.table_schema = 'public' AND p.schemaname = 'public' AND c.column_name = 'id' AND data_type = 'bigint'
    LOOP               
        IF NOT EXISTS (SELECT 0 FROM pg_class WHERE relname = substring('' || quote_ident(row.tablename) || '_id_seq' from 0 for 64) ) THEN
            EXECUTE 'CREATE SEQUENCE ' || quote_ident(row.tablename) || '_id_seq;';
            EXECUTE 'ALTER TABLE ' || quote_ident(row.tablename) || ' ALTER COLUMN id SET DEFAULT nextval(''' || quote_ident(row.tablename) || '_id_seq'');';
            EXECUTE 'ALTER SEQUENCE ' || quote_ident(row.tablename) || '_id_seq OWNED BY ' || quote_ident(row.tablename) || '.id';
            EXECUTE 'WITH mx AS (SELECT max(id)+1 AS mx FROM ' || quote_ident(row.tablename) || ') SELECT setval( ''' || quote_ident(row.tablename) || '_id_seq'' , mx.mx) FROM mx';
        END IF;
    END LOOP;
END;
$$
LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION update_sequences() RETURNS VOID AS
$$
DECLARE
    row record;
BEGIN
    FOR row IN SELECT tablename FROM pg_tables p 
                INNER JOIN information_schema.columns c ON p.tablename = c.table_name 
                WHERE c.table_schema = 'public' AND p.schemaname = 'public' AND c.column_name = 'id' AND data_type = 'bigint'
    LOOP 
        IF EXISTS (SELECT 0 FROM pg_class WHERE relname = '' || quote_ident(row.tablename) || '_id_seq' ) THEN      
            EXECUTE 'ALTER TABLE ' || quote_ident(row.tablename) || ' ALTER COLUMN id SET DEFAULT nextval(''' || quote_ident(row.tablename) || '_id_seq'');'; 
            EXECUTE 'SELECT setval(''' || quote_ident(row.tablename) || '_id_seq'', COALESCE((SELECT MAX(id)+1 FROM ' || quote_ident(row.tablename) || '), 1), false);';
        END IF;
    END LOOP;
END;
$$
LANGUAGE 'plpgsql';
-- end