package com.qcadoo.mes.productionCounting.hooks.helpers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjection;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;

public abstract class AbstractPlannedQuantitiesCounter {

    @Autowired
    private NumberService numberService;

    private final String opcProductFieldName;

    private final String tocOperationProductComponentsFieldName;

    private final String pcqOperationProductComponentFieldName;

    private final String topcProductionTrackingFieldName;

    private final ProductionCountingQuantityRole role;

    private final String topcProductFieldName;

    protected AbstractPlannedQuantitiesCounter(final ProductionCountingQuantityRole role) {
        this.role = role;
        if (role == ProductionCountingQuantityRole.USED) {
            this.opcProductFieldName = OperationProductInComponentFields.PRODUCT;
            this.tocOperationProductComponentsFieldName = TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS;
            this.pcqOperationProductComponentFieldName = ProductionCountingQuantityFields.OPERATION_PRODUCT_IN_COMPONENT;
            this.topcProductionTrackingFieldName = TrackingOperationProductInComponentFields.PRODUCTION_TRACKING;
            this.topcProductFieldName = TrackingOperationProductInComponentFields.PRODUCT;
        } else if (role == ProductionCountingQuantityRole.PRODUCED) {
            this.opcProductFieldName = OperationProductOutComponentFields.PRODUCT;
            this.tocOperationProductComponentsFieldName = TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS;
            this.pcqOperationProductComponentFieldName = ProductionCountingQuantityFields.OPERATION_PRODUCT_OUT_COMPONENT;
            this.topcProductionTrackingFieldName = TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING;
            this.topcProductFieldName = TrackingOperationProductOutComponentFields.PRODUCT;
        } else {
            throw new IllegalArgumentException(String.format("Unsupported type of production counting quantity: %s", role));
        }
    }

    protected BigDecimal getPlannedQuantity(final Entity trackingOperationProductInComponent) {
        Entity productionTracking = trackingOperationProductInComponent.getBelongsToField(topcProductionTrackingFieldName);
        Entity product = trackingOperationProductInComponent.getBelongsToField(topcProductFieldName);
        return getPlannedQuantity(productionTracking, product);
    }

    private BigDecimal getPlannedQuantity(final Entity productionTracking, final Entity product) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity technologyOperationComponent = productionTracking
                .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        SearchCriteriaBuilder scb = prepareCriteria(product, order, technologyOperationComponent);
        List<Entity> partialResults = scb.list().getEntities();

        return sumOfPlannedQuantities(partialResults);
    }

    private SearchCriteriaBuilder prepareCriteria(final Entity product, final Entity order,
            final Entity technologyOperationComponent) {
        SearchCriteriaBuilder scb = order.getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES).find();

        if (technologyOperationComponent == null) {
            scb.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product)).list().getEntities();
        } else {
            Entity operationProductComponent = findOperationProductComponent(technologyOperationComponent, product);
            scb.add(SearchRestrictions.belongsTo(pcqOperationProductComponentFieldName, operationProductComponent));
            scb.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product));
        }

        scb.add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE, role.getStringValue()));
        scb.add(SearchRestrictions.isNotNull(ProductionCountingQuantityFields.PLANNED_QUANTITY));

        SearchProjection sumOfPlannedQntty = SearchProjections.alias(
                SearchProjections.field(ProductionCountingQuantityFields.PLANNED_QUANTITY),
                ProductionCountingQuantityFields.PLANNED_QUANTITY);
        scb.setProjection(sumOfPlannedQntty);
        return scb;
    }

    private BigDecimal sumOfPlannedQuantities(final List<Entity> partialResults) {
        BigDecimal plannedQuantity = BigDecimal.ZERO;
        for (Entity productionCountingQuantity : partialResults) {
            BigDecimal productionCountingQuantityPlannedQuantity = productionCountingQuantity
                    .getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);
            plannedQuantity = plannedQuantity.add(productionCountingQuantityPlannedQuantity, numberService.getMathContext());
        }
        return numberService.setScale(plannedQuantity);
    }

    private Entity findOperationProductComponent(final Entity technologyOperationComponent, final Entity product) {
        return technologyOperationComponent.getHasManyField(tocOperationProductComponentsFieldName).find()
                .add(SearchRestrictions.belongsTo(opcProductFieldName, product)).setMaxResults(1).uniqueResult();
    }

}
