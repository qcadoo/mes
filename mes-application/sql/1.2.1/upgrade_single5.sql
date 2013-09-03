-- Table: deliveries_columnfordeliveries
-- changed: 02.09.2013

INSERT INTO deliveries_columnfordeliveries(
            id, identifier, name, description, columnfiller, alignment, parameter_id, 
            succession)
    VALUES (
   			nextval('hibernate_sequence') + 1000,
    		'succession',
    		'deliveries.columnForDeliveries.name.value.succession', 
    		'deliveries.columnForDeliveries.description.value.succession', 
    		'com.qcadoo.mes.deliveries.columnExtension.DeliveriesColumnFiller', 
			'01left', 
    		(SELECT id FROM basic_parameter LIMIT 1), 
            (SELECT succession FROM deliveries_columnfordeliveries ORDER BY succession DESC LIMIT 1) + 1);
            
-- end 