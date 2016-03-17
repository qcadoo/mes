-- added source cost report filter
-- last touched 17.03.2016 by pako

CREATE TABLE cmmsmachineparts_sourcecostreportfilter
(
  id bigint NOT NULL,
  fromdate date,
  todate date,
  sourcecost_id bigint,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT cmmsmachineparts_sourcecostreportfilter_pkey PRIMARY KEY (id),
  CONSTRAINT sourcecostreportfilter_sourcecost_fkey FOREIGN KEY (sourcecost_id)
      REFERENCES cmmsmachineparts_sourcecost (id) DEFERRABLE
)

-- end