package com.qcadoo.mes.productionCounting.hooks.helpers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjection;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;

public abstract class AbstractPlannedQuantitiesCounter {

    @Autowired
    private NumberService numberService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private final String topcProductionTrackingFieldName;

    private final ProductionCountingQuantityRole role;

    private final String topcProductFieldName;

    protected AbstractPlannedQuantitiesCounter(final ProductionCountingQuantityRole role) {
        this.role = role;
        if (role == ProductionCountingQuantityRole.USED) {
            this.topcProductionTrackingFieldName = TrackingOperationProductInComponentFields.PRODUCTION_TRACKING;
            this.topcProductFieldName = TrackingOperationProductInComponentFields.PRODUCT;
        } else if (role == ProductionCountingQuantityRole.PRODUCED) {
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
        SearchCriteriaBuilder scb = criteriaBuilderFor(order);

        TypeOfProductionRecording recordingType = TypeOfProductionRecording.parseString(order
                .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING));

        if (recordingType == TypeOfProductionRecording.FOR_EACH) {
            // since belongsTo restriction produces .isNull(fieldName) for null entity argument we do not have to deal with any
            // null checks
            scb.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT,
                    technologyOperationComponent));
        }

        scb.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product));
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

    private SearchCriteriaBuilder criteriaBuilderFor(final Entity order) {
        DataDefinition dd = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY);
        SearchCriteriaBuilder scb = dd.find();
        scb.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order));
        return scb;
    }

}
