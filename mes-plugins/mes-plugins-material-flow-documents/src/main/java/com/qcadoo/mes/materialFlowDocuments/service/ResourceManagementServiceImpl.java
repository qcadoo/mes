package com.qcadoo.mes.materialFlowDocuments.service;

import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.LOCATION;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlowResources.constants.ResourceFields.PRODUCT;
import static com.qcadoo.mes.materialFlowResources.constants.ResourceFields.QUANTITY;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowDocuments.constants.DocumentFields;
import com.qcadoo.mes.materialFlowDocuments.constants.LocationFieldsMFD;
import com.qcadoo.mes.materialFlowDocuments.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ResourceManagementServiceImpl implements ResourceManagementService {

    public enum WarehouseAlgorithm {
        FIFO("01fifo"), LIFO("02lifo"), FEFO("03fefo"), LEFO("04lefo");

        private final String value;

        private WarehouseAlgorithm(final String value) {
            this.value = value;
        }

        public String getStringValue() {
            return this.value;
        }

        public static WarehouseAlgorithm parseString(final String type) {
            if (FIFO.getStringValue().equalsIgnoreCase(type)) {
                return FIFO;
            } else if (LIFO.getStringValue().equalsIgnoreCase(type)) {
                return LIFO;
            } else if (FEFO.getStringValue().equalsIgnoreCase(type)) {
                return FEFO;
            } else if (LEFO.getStringValue().equalsIgnoreCase(type)) {
                return LEFO;
            }
            {
                throw new IllegalArgumentException("Couldn't parse WarehouseAlgorithm from string '" + type + "'");
            }
        }
    };

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Override
    @Transactional
    public void createResourcesForReceiptDocuments(final Entity document) {
        Entity warehouse = document.getBelongsToField(DocumentFields.LOCATION_TO);
        for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
            createResource(warehouse, position);
        }
    }

    private Entity createResource(final Entity warehouse, final Entity position) {

        DataDefinition resourceDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);

        Entity resource = resourceDD.create();
        resource.setField(ResourceFields.TIME, position.getBelongsToField(PositionFields.DOCUMENT).getField(DocumentFields.TIME));
        resource.setField(ResourceFields.LOCATION, warehouse);
        resource.setField(ResourceFields.PRODUCT, position.getBelongsToField(PositionFields.PRODUCT));
        resource.setField(ResourceFields.QUANTITY, position.getField(PositionFields.QUANTITY));
        resource.setField(ResourceFields.PRICE, position.getField(PositionFields.PRICE));
        resource.setField(ResourceFields.BATCH, position.getField(PositionFields.BATCH));
        resource.setField(ResourceFields.EXPIRATION_DATE, position.getField(PositionFields.EXPIRATION_DATE));
        resource.setField(ResourceFields.PRODUCTION_DATE, position.getField(PositionFields.PRODUCTION_DATE));

        return resourceDD.save(resource);
    }

    private Entity createResource(final Entity warehouse, final Entity resource, final BigDecimal quantity, Date date) {

        DataDefinition resourceDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);

        Entity newResource = resourceDD.create();
        newResource.setField(ResourceFields.TIME, date);
        newResource.setField(ResourceFields.LOCATION, warehouse);
        newResource.setField(ResourceFields.PRODUCT, resource.getBelongsToField(PositionFields.PRODUCT));
        newResource.setField(ResourceFields.QUANTITY, quantity);
        newResource.setField(ResourceFields.PRICE, resource.getField(PositionFields.PRICE));
        newResource.setField(ResourceFields.BATCH, resource.getField(PositionFields.BATCH));
        newResource.setField(ResourceFields.EXPIRATION_DATE, resource.getField(PositionFields.EXPIRATION_DATE));
        newResource.setField(ResourceFields.PRODUCTION_DATE, resource.getField(PositionFields.PRODUCTION_DATE));

        return resourceDD.save(newResource);
    }

    @Override
    @Transactional
    public void updateResourcesForReleaseDocuments(final Entity document) {
        Entity warehouse = document.getBelongsToField(DocumentFields.LOCATION_FROM);
        WarehouseAlgorithm warehouseAlgorithm = WarehouseAlgorithm.parseString(warehouse
                .getStringField(LocationFieldsMFD.ALGORITHM));
        for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
            Entity product = position.getBelongsToField(PositionFields.PRODUCT);
            BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);
            updateResources(warehouse, product, quantity, warehouseAlgorithm);
        }
    }

    private void updateResources(Entity warehouse, Entity product, BigDecimal quantity, WarehouseAlgorithm warehouseAlgorithm) {
        List<Entity> resources = getResourcesForWarehouseProductAndAlgorithm(warehouse, product, warehouseAlgorithm);

        for (Entity resource : resources) {
            BigDecimal resourceQuantity = resource.getDecimalField(ResourceFields.QUANTITY);

            if (quantity.compareTo(resourceQuantity) >= 0) {
                quantity = quantity.subtract(resourceQuantity, numberService.getMathContext());

                resource.getDataDefinition().delete(resource.getId());

                if (BigDecimal.ZERO.compareTo(quantity) == 0) {
                    return;
                }
            } else {
                resourceQuantity = resourceQuantity.subtract(quantity, numberService.getMathContext());

                resource.setField(ResourceFields.QUANTITY, numberService.setScale(resourceQuantity));

                resource.getDataDefinition().save(resource);

                return;
            }
        }

    }

    @Override
    @Transactional
    public void moveResourcesForTransferDocument(Entity document) {
        Entity warehouseFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);
        Entity warehouseTo = document.getBelongsToField(DocumentFields.LOCATION_TO);
        WarehouseAlgorithm warehouseAlgorithm = WarehouseAlgorithm.parseString(warehouseFrom
                .getStringField(LocationFieldsMFD.ALGORITHM));
        for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
            moveResources(warehouseFrom, warehouseTo, position, warehouseAlgorithm);
        }

    }

    private void moveResources(Entity warehouseFrom, Entity warehouseTo, Entity position, WarehouseAlgorithm warehouseAlgorithm) {
        Entity product = position.getBelongsToField(PositionFields.PRODUCT);
        List<Entity> resources = getResourcesForWarehouseProductAndAlgorithm(warehouseFrom, product, warehouseAlgorithm);

        BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);
        for (Entity resource : resources) {
            BigDecimal resourceQuantity = resource.getDecimalField(QUANTITY);

            if (quantity.compareTo(resourceQuantity) >= 0) {
                quantity = quantity.subtract(resourceQuantity, numberService.getMathContext());

                resource.getDataDefinition().delete(resource.getId());

                createResource(warehouseTo, resource, resourceQuantity, position.getBelongsToField(PositionFields.DOCUMENT)
                        .getDateField(DocumentFields.TIME));

                if (BigDecimal.ZERO.compareTo(quantity) == 0) {
                    return;
                }
            } else {
                resourceQuantity = resourceQuantity.subtract(quantity, numberService.getMathContext());

                resource.setField(QUANTITY, numberService.setScale(resourceQuantity));

                resource.getDataDefinition().save(resource);

                createResource(warehouseTo, resource, quantity,
                        position.getBelongsToField(PositionFields.DOCUMENT).getDateField(DocumentFields.TIME));

                return;
            }
        }
    }

    private List<Entity> getResourcesForWarehouseProductAndAlgorithm(Entity warehouse, Entity product,
            WarehouseAlgorithm warehouseAlgorithm) {
        List<Entity> resources = Lists.newArrayList();
        if (WarehouseAlgorithm.FIFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationAndProductFIFO(warehouse, product);
        } else if (WarehouseAlgorithm.LIFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationAndProductLIFO(warehouse, product);
        } else if (WarehouseAlgorithm.FEFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationAndProductFEFO(warehouse, product);
        } else if (WarehouseAlgorithm.LEFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationAndProductLEFO(warehouse, product);
        }
        return resources;
    }

    private List<Entity> getResourcesForLocationAndProductFIFO(final Entity warehouse, final Entity product) {
        List<Entity> resources = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE).find()
                .add(SearchRestrictions.belongsTo(LOCATION, warehouse)).add(SearchRestrictions.belongsTo(PRODUCT, product))
                .addOrder(SearchOrders.asc(TIME)).list().getEntities();

        return resources;
    }

    private List<Entity> getResourcesForLocationAndProductLIFO(final Entity warehouse, final Entity product) {
        List<Entity> resources = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE).find()
                .add(SearchRestrictions.belongsTo(LOCATION, warehouse)).add(SearchRestrictions.belongsTo(PRODUCT, product))
                .addOrder(SearchOrders.desc(TIME)).list().getEntities();

        return resources;
    }

    private List<Entity> getResourcesForLocationAndProductFEFO(final Entity warehouse, final Entity product) {
        List<Entity> resources = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE).find()
                .add(SearchRestrictions.belongsTo(LOCATION, warehouse)).add(SearchRestrictions.belongsTo(PRODUCT, product))
                .addOrder(SearchOrders.asc(ResourceFields.EXPIRATION_DATE)).list().getEntities();

        return resources;
    }

    private List<Entity> getResourcesForLocationAndProductLEFO(final Entity warehouse, final Entity product) {
        List<Entity> resources = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE).find()
                .add(SearchRestrictions.belongsTo(LOCATION, warehouse)).add(SearchRestrictions.belongsTo(PRODUCT, product))
                .addOrder(SearchOrders.desc(ResourceFields.EXPIRATION_DATE)).list().getEntities();

        return resources;
    }

}
