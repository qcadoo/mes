-- Table: basic_product
-- changed: 08.02.2012

ALTER TABLE basic_product ADD COLUMN description character varying(255);

-- end


-- Table: costnormsforproduct_orderoperationproductincomponent
-- changed: 09.02.2012

CREATE TABLE costnormsforproduct_orderoperationproductincomponent
(
  id bigint NOT NULL,
  order_id bigint,
  product_id bigint,
  costfornumber numeric(10,3) DEFAULT 1::numeric,
  nominalcost numeric(10,3) DEFAULT 0::numeric,
  lastpurchasecost numeric(10,3) DEFAULT 0::numeric,
  averagecost numeric(10,3) DEFAULT 0::numeric,
  costfororder numeric(10,3) DEFAULT 0::numeric,
  CONSTRAINT costnormsforproduct_orderoperationproductincomponent_pkey PRIMARY KEY (id ),
  CONSTRAINT costnormsforproduct_orderoperationproductincomponent_p_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT costnormsforproduct_orderoperationproductincomponent_o_fkey FOREIGN KEY (order_id)
      REFERENCES orders_order (id) DEFERRABLE
);

-- end
