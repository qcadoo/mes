package com.qcadoo.mes.materialFlowResources;

import static com.qcadoo.mes.materialFlow.constants.LocationFields.TYPE;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferType.CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransferType.PRODUCTION;
import static com.qcadoo.mes.materialFlow.constants.TransferType.TRANSPORT;
import static com.qcadoo.mes.materialFlowResources.constants.LocationTypeMFR.WAREHOUSE;
import static com.qcadoo.mes.materialFlowResources.constants.ResourceFields.LOCATION;
import static com.qcadoo.mes.materialFlowResources.constants.ResourceFields.PRODUCT;
import static com.qcadoo.mes.materialFlowResources.constants.ResourceFields.QUANTITY;
import static com.qcadoo.mes.materialFlowResources.constants.ResourceFields.TIME;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class MaterialFlowResourcesServiceImpl implements MaterialFlowResourcesService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Override
    public boolean areResourcesSufficient(final Entity location, final Entity product, final BigDecimal quantity) {
        List<Entity> resources = getResourcesForLocationAndProduct(location, product);

        String type = location.getStringField(TYPE);

        if (isTypeWarehouse(type)) {
            if (resources == null) {
                return false;
            } else {
                BigDecimal resourcesQuantity = BigDecimal.ZERO;

                for (Entity resource : resources) {
                    resourcesQuantity = resourcesQuantity.add(resource.getDecimalField(QUANTITY), numberService.getMathContext());
                }

                return (resourcesQuantity.compareTo(quantity) >= 0);
            }
        } else {
            return true;
        }
    }

    @Transactional
    @Override
    public void manageResources(final Entity transfer) {
        if (transfer == null) {
            return;
        }

        String type = transfer.getStringField(TYPE);
        Entity locationFrom = transfer.getBelongsToField(LOCATION_FROM);
        Entity locationTo = transfer.getBelongsToField(LOCATION_TO);
        Entity product = transfer.getBelongsToField(PRODUCT);
        BigDecimal quantity = transfer.getDecimalField(QUANTITY);
        Date time = (Date) transfer.getField(TIME);

        if (PRODUCTION.getStringValue().equals(type)) {
            String locationToType = locationTo.getStringField(TYPE);

            if (isTypeWarehouse(locationToType)) {
                addResource(locationTo, product, quantity, time);
            }
        } else if (CONSUMPTION.getStringValue().equals(type)) {
            String locationFromType = locationFrom.getStringField(TYPE);

            if (isTypeWarehouse(locationFromType)) {
                updateResource(locationFrom, product, quantity);
            }
        } else if (TRANSPORT.getStringValue().equals(type)) {
            if (locationFrom != null) {
                String locationFromType = locationFrom.getStringField(TYPE);

                if (isTypeWarehouse(locationFromType)) {
                    updateResource(locationFrom, product, quantity);
                }
            }
            if (locationTo != null) {
                String locationToType = locationTo.getStringField(TYPE);

                if (isTypeWarehouse(locationToType)) {
                    addResource(locationTo, product, quantity, time);
                }
            }
        }
    }

    private void addResource(final Entity location, final Entity product, final BigDecimal quantity, final Date time) {
        Entity resource = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE).create();

        resource.setField(LOCATION, location);
        resource.setField(PRODUCT, product);
        resource.setField(QUANTITY, quantity);
        resource.setField(TIME, time);

        resource.getDataDefinition().save(resource);
    }

    private void updateResource(final Entity location, final Entity product, BigDecimal quantity) {
        List<Entity> resources = getResourcesForLocationAndProduct(location, product);

        if (resources != null) {
            for (Entity resource : resources) {
                BigDecimal resourceQuantity = resource.getDecimalField(QUANTITY);

                if (quantity.compareTo(resourceQuantity) >= 0) {
                    quantity = quantity.subtract(resourceQuantity, numberService.getMathContext());

                    resource.getDataDefinition().delete(resource.getId());

                    if (BigDecimal.ZERO.equals(quantity)) {
                        return;
                    }
                } else {
                    resourceQuantity = resourceQuantity.subtract(quantity, numberService.getMathContext());

                    resource.setField(QUANTITY, resourceQuantity);

                    resource.getDataDefinition().save(resource);

                    return;
                }
            }
        }
    }

    private List<Entity> getResourcesForLocationAndProduct(final Entity location, final Entity product) {
        List<Entity> resources = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE).find()
                .add(SearchRestrictions.belongsTo(LOCATION, location)).add(SearchRestrictions.belongsTo(PRODUCT, product))
                .orderAscBy(TIME).list().getEntities();

        return resources;
    }

    @Override
    public Map<Entity, BigDecimal> groupResourcesByProduct(final Entity location) {
        Map<Entity, BigDecimal> productsAndQuantities = new HashMap<Entity, BigDecimal>();

        List<Entity> resources = getResourcesForLocation(location);

        if (resources != null) {
            for (Entity resource : resources) {
                Entity product = resource.getBelongsToField(PRODUCT);
                BigDecimal quantity = resource.getDecimalField(QUANTITY);

                if (productsAndQuantities.containsKey(product)) {
                    productsAndQuantities.put(product,
                            productsAndQuantities.get(product).add(quantity, numberService.getMathContext()));
                } else {
                    productsAndQuantities.put(product, quantity);
                }
            }
        }

        return productsAndQuantities;
    }

    private List<Entity> getResourcesForLocation(final Entity location) {
        List<Entity> resources = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE).find()
                .add(SearchRestrictions.belongsTo(LOCATION, location)).list().getEntities();

        return resources;
    }

    private boolean isTypeWarehouse(final String type) {
        return ((type != null) && WAREHOUSE.getStringValue().equals(type));
    }
}
