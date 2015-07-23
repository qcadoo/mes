-- Added machine parts for event
-- last touched 23.07.2015 by kama

CREATE TABLE cmmsmachineparts_machinepartforevent
(
  id bigint NOT NULL,
  maintenanceevent_id bigint,
  machinepart_id bigint,
  warehouse_id bigint,
  plannedquantity numeric(14,5),
  availablequantity numeric(14,5),
  issuedquantity numeric(14,5),
  CONSTRAINT cmmsmachineparts_machinepartforevent_pkey PRIMARY KEY (id),
  CONSTRAINT machinepartforevent_warehouse_fkey FOREIGN KEY (warehouse_id)
      REFERENCES materialflow_location (id) DEFERRABLE,
  CONSTRAINT machinepartforevent_maintenanceevent_fkey FOREIGN KEY (maintenanceevent_id)
      REFERENCES cmmsmachineparts_maintenanceevent (id) DEFERRABLE,
  CONSTRAINT machinepartforevent_machinepart_fkey FOREIGN KEY (machinepart_id)
      REFERENCES basic_product (id) DEFERRABLE
);

-- end