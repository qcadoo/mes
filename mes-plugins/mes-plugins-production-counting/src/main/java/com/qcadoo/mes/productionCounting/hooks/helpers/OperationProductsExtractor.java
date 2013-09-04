package com.qcadoo.mes.productionCounting.hooks.helpers;

import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.model.api.Entity;

@Service
public class OperationProductsExtractor {

    private static final String L_OPERATION_COMPONENT = "operationComponent";

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private ProductionCountingQuantityFetcher productionCountingQuantityFetcher;

    @Autowired
    private TrackingOperationComponentBuilder trackingOperationComponentBuilder;

    /**
     * This method takes production tracking entity and returns all matching products wrapped in tracking operation components.
     * Results will be grouped by their model name, so you can easily distinct inputs products from output ones.
     * 
     * @param productionTracking
     *            production tracking for which you want to extract products.
     * @return object representing tracking operation components grouped by their model name.
     */
    public TrackingOperationProducts getProductsByModelName(final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Iterable<Entity> allProducts = getProductsFromOrderOpsAndProductionCountings(productionTracking, order);
        return new TrackingOperationProducts(Multimaps.index(allProducts, EXTRACT_MODEL_NAME));
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

    private Iterable<Entity> getProductsFromOrderOpsAndProductionCountings(final Entity productionTracking, final Entity order) {
        Collection<Entity> productsFromPcQuantities = getProductsFromProductionCountingQuantities(productionTracking, order);
        Collection<Entity> productsFromOrderOperations = getProductsFromOrderOperation(productionTracking, order);

        Set<Entity> products = Sets.newHashSet();
        products.addAll(productsFromOrderOperations);
        products.addAll(productsFromPcQuantities);
        return products;
    }

    private List<Entity> getProductsFromProductionCountingQuantities(final Entity productionTracking, final Entity order) {
        if (hasValueChanged(productionTracking, order, ProductionTrackingFields.ORDER)) {
            return Lists.newArrayList();
        }

        Entity toc = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
        List<Entity> quantities = productionCountingQuantityFetcher.getQuantities(order, toc);
        return trackingOperationComponentBuilder.build(quantities);
    }

    private List<Entity> getProductsFromOrderOperation(final Entity productionTracking, final Entity order) {
        Entity technologyOperationComponent = productionTracking
                .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        List<Entity> productComponents = Lists.newArrayList();
        if (shouldCopy(productionTracking, order, technologyOperationComponent)) {
            productComponents.addAll(getOperationProductComponents(order, technologyOperationComponent));
        }
        return productComponents;
    }

    private List<Entity> getOperationProductComponents(final Entity order, final Entity technologyOperationComponent) {
        List<Entity> trackingOperationProductComponents = Lists.newArrayList();

        OperationProductComponentWithQuantityContainer productComponentQuantities = productQuantitiesService
                .getProductComponentQuantities(asList(order));

        for (Entry<OperationProductComponentHolder, BigDecimal> productComponentQuantity : productComponentQuantities.asMap()
                .entrySet()) {
            Entity operationProductComponent = productComponentQuantity.getKey().getEntity();

            // we want to collect only that entities which is related to given technology operation component
            if (technologyOperationComponent != null) {
                Entity operationComponent = operationProductComponent.getBelongsToField(L_OPERATION_COMPONENT);

                if (!technologyOperationComponent.getId().equals(operationComponent.getId())) {
                    continue;
                }
            }

            Entity trackingOpComp = trackingOperationComponentBuilder.fromOperationProductComponent(operationProductComponent);
            trackingOperationProductComponents.add(trackingOpComp);
        }

        return trackingOperationProductComponents;
    }

    private boolean shouldCopy(final Entity productionTracking, final Entity order, final Entity technologyOperationComponent) {
        return (hasValueChanged(productionTracking, order, ProductionTrackingFields.ORDER)
                || (technologyOperationComponent != null && hasValueChanged(productionTracking, technologyOperationComponent,
                        ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT)) || !hasTrackingOperationProductComponents(productionTracking));
    }

    private boolean hasTrackingOperationProductComponents(final Entity productionTracking) {
        return ((productionTracking.getField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS) != null) && (productionTracking
                .getField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS) != null));
    }

    private boolean hasValueChanged(final Entity productionTracking, final Entity value, final String field) {
        Entity existingProductionTracking = getExistingProductionTracking(productionTracking);
        if (existingProductionTracking == null) {
            return false;
        }
        Entity existingProductionTrackingValue = existingProductionTracking.getBelongsToField(field);
        if (existingProductionTrackingValue == null) {
            return true;
        }
        return !existingProductionTrackingValue.equals(value);
    }

    private Entity getExistingProductionTracking(final Entity productionTracking) {
        if (productionTracking.getId() == null) {
            return null;
        }
        return productionTracking.getDataDefinition().get(productionTracking.getId());
    }

}
