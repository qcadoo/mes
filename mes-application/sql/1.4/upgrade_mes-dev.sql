


--QCADOOCLS-4341 order list optimizations
DROP TABLE IF EXISTS orders_orderlistdto;
create sequence orders_orderlistdto_id_seq;
CREATE OR REPLACE VIEW orders_orderlistdto AS SELECT o.id, o.active, o.number, o.name,  o.datefrom,  o.dateto, o.startdate,  o.finishdate, o.state, o.externalnumber, o.externalsynchronized, o.issubcontracted,  o.plannedquantity, o.workplandelivered,  o.deadline,  product.number AS productnumber,  tech.number AS technologynumber,  product.unit,  master.number AS masterordernumber, division.name AS divisionname,  company.name AS companyname, masterdefinition.number AS masterorderdefinitionnumber FROM orders_order o JOIN basic_product product ON o.product_id = product.id LEFT JOIN technologies_technology tech ON o.technology_id = tech.id LEFT JOIN basic_company company ON o.company_id = company.id LEFT JOIN masterorders_masterorder master ON o.masterorder_id = master.id LEFT JOIN masterorders_masterorderdefinition masterdefinition ON master.masterorderdefinition_id = masterdefinition.id LEFT JOIN basic_division division ON tech.division_id = division.id;
-- end