package com.qcadoo.mes.productionCounting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingQuantityFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingQuantitySetComponentFields;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.SetTechnologyInComponentsFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class SetTechnologyInComponentsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Entity fillTrackingOperationProductOutComponent(final Entity trackingOperationProductInComponent,
            final Entity productionTracking, BigDecimal usedQuantity) {
        if (usedQuantity == null) {
            usedQuantity = BigDecimal.ZERO;
        }

        List<Entity> setTechnologyInComponents = Lists.newArrayList();

        DataDefinition setTechnologyInComponentsDD = getSetTechnologyInComponentsDD();

        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        EntityList productionCountingQuantities = order.getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES);

        for (Entity productionCountingQuantity : productionCountingQuantities) {
            EntityList productionCountingQuantitySetComponents = productionCountingQuantity
                    .getHasManyField(ProductionCountingQuantityFieldsPC.PRODUCTION_COUNTING_QUANTITY_SET_COMPONENTS);
            BigDecimal plannedQuantity = productionCountingQuantity
                    .getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);

            for (Entity productionCountingQuantitySetComponent : productionCountingQuantitySetComponents) {
                Entity setTechnologyInComponent = setTechnologyInComponentsDD.create();

                BigDecimal quantityFromSets = productionCountingQuantitySetComponent
                        .getDecimalField(ProductionCountingQuantitySetComponentFields.QUANTITY_FROM_SETS);

                quantityFromSets = quantityFromSets.multiply(usedQuantity).divide(plannedQuantity, 5, RoundingMode.HALF_EVEN);

                setTechnologyInComponent.setField(SetTechnologyInComponentsFields.QUANTITY_FROM_SETS, quantityFromSets);
                setTechnologyInComponent.setField(SetTechnologyInComponentsFields.PRODUCT, productionCountingQuantitySetComponent
                        .getBelongsToField(ProductionCountingQuantitySetComponentFields.PRODUCT));
                setTechnologyInComponent.setField(SetTechnologyInComponentsFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENT,
                        trackingOperationProductInComponent);

                setTechnologyInComponents.add(setTechnologyInComponent);
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

    public boolean isSet(final Entity componentEntity) {
        Entity product = componentEntity.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

        return isProductASet(product);
    }

    public boolean isProductASet(final Long productId) {
        return getSetProductTechnology(productId).isPresent();
    }

    public boolean isProductASet(final Entity product) {
        return getSetProductTechnology(product).isPresent();
    }

    public Optional<Entity> getSetProductTechnology(final Entity product) {
        Long productId = product.getId();

        return getSetProductTechnology(productId);
    }

    private Optional<Entity> getSetProductTechnology(final Long productId) {
        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);
        Entity masterTechnology = technologyDD.find().add(SearchRestrictions.eq(TechnologyFields.PRODUCT + ".id", productId))
                .add(SearchRestrictions.eq(TechnologyFields.MASTER, true)).uniqueResult();

        if (masterTechnology != null) {
            EntityTree operationComponents = masterTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

            boolean isSet = operationComponents.getRoot()
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS).get(0)
                    .getBooleanField(OperationProductOutComponentFields.SET);

            if (isSet) {
                return Optional.of(masterTechnology);
            }
        }

        return Optional.empty();
    }

}
