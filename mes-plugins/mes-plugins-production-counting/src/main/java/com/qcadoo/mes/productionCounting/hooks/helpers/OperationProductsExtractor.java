package com.qcadoo.mes.productionCounting.hooks.helpers;

import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.model.api.Entity;

@Service
public class OperationProductsExtractor {

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private TrackingOperationComponentBuilder trackingOperationComponentBuilder;

    /**
     * This method takes production tracking entity and returns all matching products wrapped in tracking operation components.
     * Results will be grouped by their model name, so you can easily distinct inputs products from output ones.
     * 
     * @param productionTracking
     *            production tracking for which you want to extract products.
     * 
     * @return object representing tracking operation components grouped by their model name.
     */
    public TrackingOperationProducts getProductsByModelName(final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        Iterable<Entity> allProducts = getProductsFromOrderOperation(productionTracking, order);

        return new TrackingOperationProducts(Multimaps.index(allProducts, EXTRACT_MODEL_NAME));
    }

    private Iterable<Entity> getProductsFromOrderOperation(final Entity productionTracking, final Entity order) {
        Entity technologyOperationComponent = productionTracking
                .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        return getOperationProductComponents(order, technologyOperationComponent);
    }

    private List<Entity> getOperationProductComponents(final Entity order, final Entity technologyOperationComponent) {
        List<Entity> trackingOperationProductComponents = Lists.newArrayList();

        OperationProductComponentWithQuantityContainer productComponentQuantities = productQuantitiesService
                .getProductComponentQuantities(asList(order));

        for (Entry<OperationProductComponentHolder, BigDecimal> productComponentQuantity : productComponentQuantities.asMap()
                .entrySet()) {
            OperationProductComponentHolder operationProductComponentHolder = productComponentQuantity.getKey();

            // we want to collect only that entities which is related to given technology operation component
            if (technologyOperationComponent != null) {
                Entity operationComponent = operationProductComponentHolder.getTechnologyOperationComponent();

                if (!technologyOperationComponent.getId().equals(operationComponent.getId())) {
                    continue;
                }
            }

            Entity trackingOperationProductComponent = trackingOperationComponentBuilder
                    .fromOperationProductComponentHolder(operationProductComponentHolder);

            trackingOperationProductComponents.add(trackingOperationProductComponent);
        }

        return trackingOperationProductComponents;
    }

    public static class TrackingOperationProducts {

        private final Multimap<String, Entity> operationProductsByModelName;

        protected TrackingOperationProducts(final Multimap<String, Entity> operationProductsByModelName) {
            this.operationProductsByModelName = operationProductsByModelName;
        }

        public List<Entity> getInputComponents() {
            return copyOf(ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT);
        }

        public List<Entity> getOutputComponents() {
            return copyOf(ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT);
        }

        private List<Entity> copyOf(final String key) {
            return Lists.newArrayList(operationProductsByModelName.get(key));
        }
    }

    private static final Function<Entity, String> EXTRACT_MODEL_NAME = new Function<Entity, String>() {

        @Override
        public String apply(final Entity from) {
            return from.getDataDefinition().getName();
        }
    };

}
