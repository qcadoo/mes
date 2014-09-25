package com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.dataProvider;

import static com.qcadoo.model.api.search.SearchRestrictions.in;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.qcadoo.commons.functional.FluentOptional;
import com.qcadoo.commons.functional.Optionals;
import com.qcadoo.mes.costNormsForMaterials.constants.TechnologyInstOperProductInCompFields;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.OrderMaterialsCostDataGenerator;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.domain.ProductWithCosts;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.tree.dataProvider.TechnologyRawInputProductComponentsCriteria;
import com.qcadoo.mes.technologies.tree.dataProvider.TechnologyRawInputProductComponentsDataProvider;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchProjection;
import com.qcadoo.model.api.utils.EntityUtils;

@Service
final class OrderMaterialsCostsDataGeneratorImpl implements OrderMaterialsCostDataGenerator {

    private static final SearchProjection PRODUCT_WITH_COSTS_PROJECTION = ProductWithCostsBuilder
            .buildProjectionForProduct(TechnologyRawInputProductComponentsCriteria.PRODUCT_ALIAS);

    @Autowired
    private OrderMaterialCostsEntityBuilder orderMaterialCostsEntityBuilder;

    @Autowired
    private TechnologyRawInputProductComponentsDataProvider technologyRawInputProductComponentsDataProvider;

    @Autowired
    private OrderMaterialCostsDataProvider orderMaterialCostsDataProvider;

    @Override
    public List<Entity> generateUpdatedMaterialsListFor(final Entity order) {
        for (Long technologyId : extractTechnologyIdFrom(order).asSet()) {
            List<ProductWithCosts> allTechnologyRawProductsWithCosts = findRawInputProductsFor(technologyId);
            final Set<Long> technologyRawProductIds = FluentIterable.from(allTechnologyRawProductsWithCosts)
                    .transform(ProductWithCosts.EXTRACT_ID).toSet();
            List<Entity> existingOrderMaterialCosts = findExistingOrderMaterialCosts(order, technologyRawProductIds);
            List<Entity> missingOrderMaterialCosts = createMissingOrderMaterialCostsEntities(order,
                    allTechnologyRawProductsWithCosts, existingOrderMaterialCosts);
            return ImmutableList.<Entity> builder().addAll(existingOrderMaterialCosts).addAll(missingOrderMaterialCosts).build();
        }
        return ImmutableList.of();
    }

    private ImmutableList<Entity> createMissingOrderMaterialCostsEntities(final Entity order,
            final List<ProductWithCosts> allTechnologyRawProductsWithCosts, final List<Entity> existingOrderMaterialCosts) {
        final Set<Long> existingMaterialCostIds = FluentIterable.from(existingOrderMaterialCosts)
                .transform(EntityUtils.getBelongsToFieldExtractor(TechnologyInstOperProductInCompFields.PRODUCT))
                .transform(EntityUtils.getIdExtractor()).toSet();
        return FluentIterable.from(allTechnologyRawProductsWithCosts).filter(new Predicate<ProductWithCosts>() {

            @Override
            public boolean apply(final ProductWithCosts productWithCosts) {
                return !existingMaterialCostIds.contains(productWithCosts.getProductId());
            }
        }).transform(new Function<ProductWithCosts, Entity>() {

            @Override
            public Entity apply(final ProductWithCosts productWithCosts) {
                return orderMaterialCostsEntityBuilder.create(order, productWithCosts);
            }
        }).toList();
    }

    private List<Entity> findExistingOrderMaterialCosts(final Entity order, final Collection<Long> productIds) {
        if (order.getId() == null) {
            return Collections.emptyList();
        }
        OrderMaterialCostsCriteria criteria = OrderMaterialCostsCriteria.forOrder(order.getId());
        criteria.setProductCriteria(in("id", productIds));
        return orderMaterialCostsDataProvider.findAll(criteria);
    }

    private List<ProductWithCosts> findRawInputProductsFor(final Long technologyId) {
        TechnologyRawInputProductComponentsCriteria criteria = TechnologyRawInputProductComponentsCriteria
                .forTechnology(technologyId);
        criteria.setSearchProjection(PRODUCT_WITH_COSTS_PROJECTION);
        return asProductsWithCosts(technologyRawInputProductComponentsDataProvider.findAll(criteria));
    }

    private List<ProductWithCosts> asProductsWithCosts(final List<Entity> projectionResults) {
        return FluentIterable.from(projectionResults).transform(ProductWithCostsBuilder.BUILD_FROM_PROJECTION).toList();
    }

    private Optional<Long> extractTechnologyIdFrom(final Entity order) {
        return FluentOptional.fromNullable(order)
                .flatMap(Optionals.lift(EntityUtils.getBelongsToFieldExtractor(OrderFields.TECHNOLOGY)))
                .flatMap(EntityUtils.getSafeIdExtractor()).toOpt();
    }

}
