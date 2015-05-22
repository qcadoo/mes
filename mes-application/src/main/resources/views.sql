--
-- ***************************************************************************
-- Copyright (c) 2010 Qcadoo Limited
-- Project: Qcadoo MES
-- Version: 1.3
--
-- This file is part of Qcadoo.
--
-- Qcadoo is free software; you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as published
-- by the Free Software Foundation; either version 3 of the License,
-- or (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty
-- of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
-- See the GNU Affero General Public License for more details.
--
-- You should have received a copy of the GNU Affero General Public License
-- along with this program; if not, write to the Free Software
-- Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
-- ***************************************************************************
--

-- This script is invoked when application starts with hbm2ddlAuto=create.

--  Qcadoo-model & Hibernate will automatically generate regular db table due to existence of warehouseStock.xml model,
-- we need to first drop this table, before create table view.
DROP TABLE IF EXISTS materialflowresources_warehousestock;

CREATE OR REPLACE FUNCTION createWarehouseStockView () RETURNS boolean AS 'BEGIN IF EXISTS (SELECT * FROM information_schema.columns WHERE table_name=''basic_parameter'' AND column_name=''tenantid'') THEN CREATE OR REPLACE VIEW materialflowresources_warehousestock AS SELECT row_number() OVER () AS id, location_id, product_id, SUM(quantity) AS quantity, tenantid FROM materialflowresources_resource GROUP BY tenantid, location_id, product_id ; ELSE CREATE OR REPLACE VIEW materialflowresources_warehousestock AS SELECT row_number() OVER () AS id, location_id, product_id, SUM(quantity) AS quantity FROM materialflowresources_resource GROUP BY location_id, product_id ; END IF; RETURN TRUE; END;' LANGUAGE 'plpgsql';

SELECT createWarehouseStockView();

DROP FUNCTION createWarehouseStockView ();

-- Create sequences for existing tables
CREATE OR REPLACE FUNCTION update_sequence () RETURNS VOID AS 'DECLARE row record; BEGIN FOR row IN SELECT tablename FROM pg_tables p INNER JOIN information_schema.columns c on p.tablename = c.table_name WHERE c.table_schema = ''public'' and p.schemaname = ''public''  and c.column_name = ''id'' and data_type = ''bigint'' LOOP EXECUTE ''ALTER TABLE '' || quote_ident(row.tablename) || '' OWNER TO postgres;''; EXECUTE ''CREATE SEQUENCE '' || quote_ident(row.tablename) || ''_id_seq;''; EXECUTE ''ALTER TABLE '' || quote_ident(row.tablename) || '' ALTER COLUMN id SET DEFAULT nextval('''''' || quote_ident(row.tablename) || ''_id_seq'''');''; EXECUTE ''ALTER SEQUENCE '' || quote_ident(row.tablename) || ''_id_seq OWNED BY '' || quote_ident(row.tablename) || ''.id''; EXECUTE ''WITH mx AS (SELECT max(id) AS mx FROM '' || quote_ident(row.tablename) || '') SELECT setval( '''''' || quote_ident(row.tablename) || ''_id_seq'''' , mx.mx) FROM mx''; END LOOP; END;' LANGUAGE 'plpgsql';

SELECT * FROM update_sequence();

DROP FUNCTION update_sequence ();
-- end