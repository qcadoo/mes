package com.qcadoo.mes.materialFlow;

import static com.qcadoo.mes.materialFlow.constants.ResourceFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.QUANTITY;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STOCK_AREAS_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STOCK_AREAS_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;
import static com.qcadoo.mes.materialFlow.constants.TransferType.CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransferType.PRODUCTION;
import static com.qcadoo.mes.materialFlow.constants.TransferType.TRANSPORT;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlow.constants.ResourceFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class MaterialFlowResourceServiceImpl implements MaterialFlowResourceService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    public boolean areResourcesSufficient(final Entity stockAreas, final Entity product, final BigDecimal quantity) {
        List<Entity> resources = getResources(stockAreas, product);

        if (resources == null) {
            return false;
        } else {
            BigDecimal resourcesQuantity = BigDecimal.ZERO;

            for (Entity resource : resources) {
                resourcesQuantity = resourcesQuantity.add(resource.getDecimalField(QUANTITY), numberService.getMathContext());
            }

            return (resourcesQuantity.compareTo(quantity) >= 0);
        }
    }

    @Transactional
    public void manageResources(final Entity transfer) {
        if (transfer == null) {
            return;
        }

        String type = transfer.getStringField(TYPE);
        Entity stockAreasFrom = transfer.getBelongsToField(STOCK_AREAS_FROM);
        Entity stockAreasTo = transfer.getBelongsToField(STOCK_AREAS_TO);
        Entity product = transfer.getBelongsToField(PRODUCT);
        BigDecimal quantity = transfer.getDecimalField(QUANTITY);
        Date time = (Date) transfer.getField(TIME);

        if (PRODUCTION.getStringValue().equals(type)) {
            addResource(stockAreasTo, product, quantity, time);
        } else if (CONSUMPTION.getStringValue().equals(type)) {
            updateResource(stockAreasFrom, product, quantity);
        } else if (TRANSPORT.getStringValue().equals(type)) {
            updateResource(stockAreasFrom, product, quantity);
            addResource(stockAreasTo, product, quantity, time);
        }
    }

    private void addResource(final Entity stockAreas, final Entity product, final BigDecimal quantity, final Date time) {
        Entity resource = dataDefinitionService
                .get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_RESOURCE).create();

        resource.setField(ResourceFields.STOCK_AREAS, stockAreas);
        resource.setField(ResourceFields.PRODUCT, product);
        resource.setField(ResourceFields.QUANTITY, quantity);
        resource.setField(ResourceFields.TIME, time);

        resource.getDataDefinition().save(resource);
    }

    private void updateResource(final Entity stockAreas, final Entity product, BigDecimal quantity) {
        List<Entity> resources = getResources(stockAreas, product);

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

    private List<Entity> getResources(final Entity stockAreas, final Entity product) {
        List<Entity> resources = dataDefinitionService
                .get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_RESOURCE).find()
                .add(SearchRestrictions.belongsTo(ResourceFields.STOCK_AREAS, stockAreas))
                .add(SearchRestrictions.belongsTo(PRODUCT, product)).orderAscBy(TIME).list().getEntities();

        return resources;
    }

}
