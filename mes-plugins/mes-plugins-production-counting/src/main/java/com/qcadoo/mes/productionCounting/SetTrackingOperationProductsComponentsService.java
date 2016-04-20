package com.qcadoo.mes.productionCounting;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantitySet;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SetTrackingOperationProductsComponentsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Entity fillTrackingOperationProductOutComponent(Entity productionTracking, Entity trackingOperationProductOutComponent, BigDecimal usedQuantity) {
        if (usedQuantity == null) {
            usedQuantity = BigDecimal.ZERO;
        }
        if (isSet(productionTracking, trackingOperationProductOutComponent)) {
            List<Entity> setTrackingOperationProductsInComponents = new ArrayList<>();

            Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
            SearchCriteriaBuilder findProductionCountingQuantity = getProductionCountingQuantityDD().find();
            List<Entity> entities = findProductionCountingQuantity.add(SearchRestrictions.and(
                    SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order),
                    SearchRestrictions.eq(ProductionCountingQuantityFields.SET, ProductionCountingQuantitySet.INTERMEDIATE.getStringValue()))).list().getEntities();

            for (Entity productionCountingQuantity : entities) {
                Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
                BigDecimal plannedQuantityFromProduct = productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);

                Entity setTrackingOperationProductInComponents = getSetTrackingOperationProductInComponentsDD().create();

                BigDecimal plannedQuantityForOrder = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
                BigDecimal quantityFromSets = plannedQuantityFromProduct.multiply(usedQuantity).divide(plannedQuantityForOrder, RoundingMode.HALF_UP);
                setTrackingOperationProductInComponents.setField("quantityFromSets", quantityFromSets);
                setTrackingOperationProductInComponents.setField("product", product);
                setTrackingOperationProductInComponents.setField("trackingOperationProductOutComponent", trackingOperationProductOutComponent);

                setTrackingOperationProductsInComponents.add(setTrackingOperationProductInComponents);
            }

            trackingOperationProductOutComponent.setField(TrackingOperationProductOutComponentFields.SET_TRACKING_OPERATION_PRODUCTS_IN_COMPONENTS, setTrackingOperationProductsInComponents);
        }
        return trackingOperationProductOutComponent;
    }

    public Entity recalculateTrackingOperationProductOutComponent(Entity productionTracking, Entity trackingOperationProductOutComponent, BigDecimal usedQuantity) {
        return fillTrackingOperationProductOutComponent(productionTracking, trackingOperationProductOutComponent, usedQuantity);
    }

    private DataDefinition getSetTrackingOperationProductInComponentsDD() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_SET_TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
    }

    private DataDefinition getProductionCountingQuantityDD() {
        return dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER, BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY);
    }

    public boolean isSet(Entity productionTracking, Entity trackingOperationProductOutComponent) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity product = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

        SearchCriteriaBuilder findProductionCountingQuantity = getProductionCountingQuantityDD().find();
        List<Entity> entities = findProductionCountingQuantity.add(SearchRestrictions.and(
                SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order),
                SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product))).list().getEntities();

        Optional<Entity> maybeProductionCountingQuantity = entities.stream().filter(entity -> {
            Entity entityTechnologyOperationComponent = entity.getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);

            return "1.".equals(entityTechnologyOperationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER));
        }).findFirst();

        if (maybeProductionCountingQuantity.isPresent()) {
            return ProductionCountingQuantitySet.SET.getStringValue().equals(maybeProductionCountingQuantity.get().getStringField(ProductionCountingQuantityFields.SET));

        } else {
            return false;
        }
    }
}
