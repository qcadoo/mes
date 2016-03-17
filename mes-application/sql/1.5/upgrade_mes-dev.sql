-- #GOODFOOD-1554 i GOODFOOD-1489
ALTER TABLE cmmsmachineparts_maintenanceevent ADD entityVersion BIGINT DEFAULT 0;
ALTER TABLE cmmsmachineparts_plannedevent ADD entityVersion BIGINT DEFAULT 0; 
-- end;