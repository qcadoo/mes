-- table: qcadooview_view, qcadooview_item
-- last touched: 25.10.2015 by lupo

INSERT INTO qcadooview_view (pluginidentifier, name, view)
VALUES (
	'basic',
	'palletNumberHelpersList',
	'palletNumberHelpersList'
);

INSERT INTO qcadooview_item (pluginidentifier, name, active, category_id, view_id, succession, authrole)
VALUES ('basic', 'palletNumberHelpers', TRUE,
        (
            SELECT
                id
            FROM qcadooview_category
            WHERE
                name = 'basic'
            LIMIT 1
        ),
        (
            SELECT
                id
            FROM qcadooview_view
            WHERE
                name = 'palletNumberHelpersList'
            LIMIT 1
        ),
        (
            SELECT
                max(succession) + 1
            FROM qcadooview_item
            WHERE
                category_id = (
                    SELECT
                        id
                    FROM qcadooview_category
                    WHERE
                        name = 'basic'
                    LIMIT 1
                )
            LIMIT 1
        ),
        'ROLE_PALLET_NUMBERS'
    );


INSERT INTO qcadooview_view (pluginidentifier, name, view)
VALUES (
	'basic',
	'palletNumbersList',
	'palletNumbersList'
);

INSERT INTO qcadooview_item (pluginidentifier, name, active, category_id, view_id, succession, authrole)
VALUES ('basic', 'palletNumbers', TRUE,
        (
            SELECT
                id
            FROM qcadooview_category
            WHERE
                name = 'basic'
            LIMIT 1
        ),
        (
            SELECT
                id
            FROM qcadooview_view
            WHERE
                name = 'palletNumbersList'
            LIMIT 1
        ),
        (
            SELECT
                max(succession) + 1
            FROM qcadooview_item
            WHERE
                category_id = (
                    SELECT
                        id
                    FROM qcadooview_category
                    WHERE
                        name = 'basic'
                    LIMIT 1
                )
            LIMIT 1
        ),
        'ROLE_PALLET_NUMBERS'
    );

-- end


-- table: basic_palletnumber
-- last touched: 25.10.2015 by lupo

CREATE TABLE basic_palletnumber
(
  id bigint NOT NULL,
  "number" character varying(6),
  active boolean DEFAULT true,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT basic_palletnumber_pkey PRIMARY KEY (id)
);

-- end


-- table: basic_palletnumberhelper
-- last touched: 25.10.2015 by lupo

CREATE TABLE basic_palletnumberhelper
(
  id bigint NOT NULL,
  firstnumber character varying(6),
  quantity integer,
  active boolean DEFAULT true,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT basic_palletnumberhelper_pkey PRIMARY KEY (id)
);

--
-- end

-- add showmaterialcomponent parameter in operation component
-- last touched 28.10.2015 by pako

ALTER TABLE technologies_operationproductincomponent ADD COLUMN showmaterialcomponent boolean;

-- end
