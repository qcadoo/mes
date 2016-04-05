package com.qcadoo.mes.productionCounting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.GlobalTypeOfMaterial;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class SetTechnologyInComponentsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Entity fillTrackingOperationProductOutComponent(Entity trackingOperationProductInComponent, BigDecimal usedQuantity) {
        List<Entity> setTechnologyInComponents = new ArrayList<>();
        DataDefinition setTechnologyInComponentsDD = getSetTechnologyInComponentsDD();

        Entity product = trackingOperationProductInComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

        Entity productionTracking = trackingOperationProductInComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);

        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);
        Entity technology = technologyDD.find()
                .add(SearchRestrictions.eq(TechnologyFields.STATE, TechnologyState.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.eq(TechnologyFields.MASTER, true))
                .add(SearchRestrictions.eq(OperationProductOutComponentFields.PRODUCT + ".id", product.getId())).uniqueResult();

        EntityTree operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        List<EntityTreeNode> children = operationComponents.getRoot().getChildren();

        for (Entity technologyOperationComponent : children) {
            Entity operationProductOutComponent = technologyOperationComponent.getHasManyField(
                    TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS).get(0);

            Entity productFromComponent = operationProductOutComponent
                    .getBelongsToField(OperationProductOutComponentFields.PRODUCT);
            GlobalTypeOfMaterial productGlobalTypeOfMaterial = GlobalTypeOfMaterial.parseString(productFromComponent
                    .getStringField(ProductFields.GLOBAL_TYPE_OF_MATERIAL));

            if (productGlobalTypeOfMaterial == GlobalTypeOfMaterial.INTERMEDIATE) {
                Entity setTrackingOperationProductInComponents = setTechnologyInComponentsDD.create();

                BigDecimal plannedQuantityFromProduct = operationProductOutComponent
                        .getDecimalField(OperationProductOutComponentFields.QUANTITY);

                BigDecimal plannedQuantityForOrder = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
                BigDecimal quantityFromSets = plannedQuantityFromProduct.multiply(usedQuantity).divide(plannedQuantityForOrder,
                        RoundingMode.HALF_UP);
                setTrackingOperationProductInComponents.setField("quantityFromSets", quantityFromSets);
                setTrackingOperationProductInComponents.setField("product", productFromComponent);
                setTrackingOperationProductInComponents.setField("trackingOperationProductInComponent",
                        trackingOperationProductInComponent);

                setTrackingOperationProductInComponents = setTrackingOperationProductInComponents.getDataDefinition().save(
                        setTrackingOperationProductInComponents);

                setTechnologyInComponents.add(setTrackingOperationProductInComponents);
            }
        }

        trackingOperationProductInComponent.setField(TrackingOperationProductInComponentFields.SET_TECHNOLOGY_IN_COMPONENTS,
                setTechnologyInComponents);
        return trackingOperationProductInComponent;
    }

    private DataDefinition getSetTechnologyInComponentsDD() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_SET_TECHNOLOGY_IN_COMPONENTS);
    }

    public boolean isSet(Entity componentEntity) {
        boolean isSet = false;
        Entity product = componentEntity.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

        DataDefinition operationProductOutComponentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT);
        SearchResult productOutComponents = operationProductOutComponentDD.find()
                .add(SearchRestrictions.eq(OperationProductOutComponentFields.PRODUCT + ".id", product.getId()))
                .add(SearchRestrictions.eq(OperationProductOutComponentFields.SET, true)).list();

        if (productOutComponents.getTotalNumberOfEntities() > 0) {
            isSet = productOutComponents.getEntities().stream()
                    .map(poc -> poc.getBelongsToField(OperationProductOutComponentFields.OPERATION_COMPONENT))
                    .map(oc -> oc.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY))
                    .anyMatch(t -> TechnologyState.ACCEPTED.getStringValue().equals(t.getStringField(TechnologyFields.STATE)));
        }

        return isSet;
    }
}
