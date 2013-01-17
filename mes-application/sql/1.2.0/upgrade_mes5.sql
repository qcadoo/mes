-- Table: technologies_technology
-- changed: 16.01.2013

UPDATE technologies_technology SET master = false where state!='02accepted';
ALTER TABLE technologies_technology ALTER master SET DEFAULT false;
-- end

