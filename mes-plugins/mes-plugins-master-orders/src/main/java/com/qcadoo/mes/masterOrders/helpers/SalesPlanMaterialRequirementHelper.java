package com.qcadoo.mes.masterOrders.helpers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.CompanyProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.SalesPlanFields;
import com.qcadoo.mes.masterOrders.constants.SalesPlanMaterialRequirementFields;
import com.qcadoo.mes.masterOrders.constants.SalesPlanMaterialRequirementProductFields;
import com.qcadoo.mes.masterOrders.constants.SalesPlanProductFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.ProductQuantitiesWithComponentsService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.ProductBySizeGroupFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.plugin.api.PluginManager;

@Service
public class SalesPlanMaterialRequirementHelper {

    private static final String ORDERS_FOR_SUBPRODUCTS_GENERATION = "ordersForSubproductsGeneration";

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private ProductQuantitiesWithComponentsService productQuantitiesWithComponentsService;

    @Autowired
    private ProductStructureTreeService productStructureTreeService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private TechnologyService technologyService;

    public List<Entity> generateSalesPlanMaterialRequirementProducts(final Entity salesPlanMaterialRequirement) {
        List<Entity> salesPlanMaterialRequirementProducts = Lists.newArrayList();

        Entity salesPlan = salesPlanMaterialRequirement.getBelongsToField(SalesPlanMaterialRequirementFields.SALES_PLAN);
        boolean includeComponents = salesPlanMaterialRequirement.getBooleanField(SalesPlanMaterialRequirementFields.INCLUDE_COMPONENTS);

        createSalesPlanMaterialRequirementProducts(salesPlanMaterialRequirementProducts, salesPlan, includeComponents);

        updateSalesPlanMaterialRequirementProducts(salesPlanMaterialRequirementProducts);

        return salesPlanMaterialRequirementProducts;
    }

    private void createSalesPlanMaterialRequirementProducts(final List<Entity> salesPlanMaterialRequirementProducts,
            final Entity salesPlan, final boolean includeComponents) {
        List<Entity> salesPlanProducts = salesPlan.getHasManyField(SalesPlanFields.PRODUCTS);

        for (Entity salesPlanProduct : salesPlanProducts) {
            Entity salesPlanProductTechnology = salesPlanProduct.getBelongsToField(SalesPlanProductFields.TECHNOLOGY);
            Entity salesPlanProductProduct = salesPlanProduct.getBelongsToField(SalesPlanProductFields.PRODUCT);
            BigDecimal plannedQuantity = salesPlanProduct.getDecimalField(SalesPlanProductFields.PLANNED_QUANTITY);

            Entity technology = getTechnology(salesPlanProductTechnology, salesPlanProductProduct);

            if (Objects.nonNull(technology)) {
                Map<OperationProductComponentHolder, BigDecimal> neededQuantities;

                if (pluginManager.isPluginEnabled(ORDERS_FOR_SUBPRODUCTS_GENERATION) && includeComponents) {
                    neededQuantities = productQuantitiesWithComponentsService
                            .getNeededProductQuantities(technology, salesPlanProductProduct, plannedQuantity);
                } else {
                    neededQuantities = productQuantitiesService
                            .getNeededProductQuantities(technology, salesPlanProductProduct, plannedQuantity);
                }

                for (Map.Entry<OperationProductComponentHolder, BigDecimal> neededProductQuantity : neededQuantities.entrySet()) {
                    Long productId = neededProductQuantity.getKey().getProductId();
                    Long operationProductComponentId = neededProductQuantity.getKey().getOperationProductComponentId();
                    Entity product = neededProductQuantity.getKey().getProduct();
                    BigDecimal neededQuantity = neededProductQuantity.getValue();

                    if (Objects.isNull(productId)) {
                        List<Entity> productBySizeGroups = technologyService.getProductBySizeGroups(operationProductComponentId);

                        for (Entity productBySizeGroup : productBySizeGroups) {
                            createSalesPlanMaterialRequirementProductFromProductBySizeGroup(salesPlanMaterialRequirementProducts,
                                    productBySizeGroup, plannedQuantity);
                        }
                    } else {
                        createSalesPlanMaterialRequirementProductFromProduct(salesPlanMaterialRequirementProducts, product,
                                neededQuantity);
                    }
                }
            }
        }
    }

    private Entity getTechnology(final Entity technology, final Entity product) {
        if (Objects.isNull(technology)) {
            String entityType = product.getStringField(ProductFields.ENTITY_TYPE);

            if (ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue().equals(entityType)) {
                Entity parent = product.getBelongsToField(ProductFields.PARENT);

                if (Objects.nonNull(parent)) {
                    return productStructureTreeService.findTechnologyForProduct(parent);
                } else {
                    return productStructureTreeService.findTechnologyForProduct(product);
                }
            } else {
                return productStructureTreeService.findTechnologyForProduct(product);
            }
        }

        return technology;
    }

    private Entity createSalesPlanMaterialRequirementProductFromProduct(final List<Entity> salesPlanMaterialRequirementProducts,
            final Entity product, final BigDecimal neededQuantity) {
        Optional<Entity> mayBeSalesPlanMaterialRequirementProduct = salesPlanMaterialRequirementProducts.stream()
                .filter(salesPlanMaterialRequirementProduct -> filterByProduct(salesPlanMaterialRequirementProduct, product,
                        SalesPlanMaterialRequirementProductFields.QUANTITY))
                .findFirst();

        Entity salesPlanMaterialRequirementProduct;

        if (mayBeSalesPlanMaterialRequirementProduct.isPresent()) {
            salesPlanMaterialRequirementProduct = mayBeSalesPlanMaterialRequirementProduct.get();

            BigDecimal quantity = salesPlanMaterialRequirementProduct
                    .getDecimalField(SalesPlanMaterialRequirementProductFields.QUANTITY);

            quantity = quantity.add(neededQuantity, numberService.getMathContext());

            salesPlanMaterialRequirementProduct.setField(SalesPlanMaterialRequirementProductFields.QUANTITY, quantity);
        } else {
            salesPlanMaterialRequirementProduct = getSalesPlanMaterialRequirementProductDD().create();

            salesPlanMaterialRequirementProduct.setField(SalesPlanMaterialRequirementProductFields.PRODUCT, product);
            salesPlanMaterialRequirementProduct.setField(SalesPlanMaterialRequirementProductFields.QUANTITY, neededQuantity);
            salesPlanMaterialRequirementProduct.setField(SalesPlanMaterialRequirementProductFields.IS_DELIVERY_CREATED, false);

            salesPlanMaterialRequirementProducts.add(salesPlanMaterialRequirementProduct);
        }

        return salesPlanMaterialRequirementProduct;
    }

    private Entity createSalesPlanMaterialRequirementProductFromProductBySizeGroup(
            final List<Entity> salesPlanMaterialRequirementProducts, final Entity productBySizeGroup,
            final BigDecimal planedQuantity) {
        Entity product = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.PRODUCT);
        Entity sizeGroup = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.SIZE_GROUP);

        Optional<Entity> mayBeSalesPlanMaterialRequirementProduct = salesPlanMaterialRequirementProducts.stream()
                .filter(salesPlanMaterialRequirementProduct -> filterByProduct(salesPlanMaterialRequirementProduct, product,
                        SalesPlanMaterialRequirementProductFields.SIZE_GROUP)
                        && filterBySizeGroup(salesPlanMaterialRequirementProduct, sizeGroup))
                .findFirst();

        Entity salesPlanMaterialRequirementProduct;

        if (mayBeSalesPlanMaterialRequirementProduct.isPresent()) {
            salesPlanMaterialRequirementProduct = mayBeSalesPlanMaterialRequirementProduct.get();

            BigDecimal sumForSizes = salesPlanMaterialRequirementProduct
                    .getDecimalField(SalesPlanMaterialRequirementProductFields.SUM_FOR_SIZES);

            BigDecimal neededQuantity = numberService.setScaleWithDefaultMathContext(planedQuantity.multiply(
                    productBySizeGroup.getDecimalField(ProductBySizeGroupFields.QUANTITY), numberService.getMathContext()));

            sumForSizes = sumForSizes.add(neededQuantity, numberService.getMathContext());

            salesPlanMaterialRequirementProduct.setField(SalesPlanMaterialRequirementProductFields.SUM_FOR_SIZES, sumForSizes);
        } else {
            BigDecimal neededQuantity = numberService.setScaleWithDefaultMathContext(planedQuantity.multiply(
                    productBySizeGroup.getDecimalField(ProductBySizeGroupFields.QUANTITY), numberService.getMathContext()));
            salesPlanMaterialRequirementProduct = getSalesPlanMaterialRequirementProductDD().create();

            salesPlanMaterialRequirementProduct.setField(SalesPlanMaterialRequirementProductFields.PRODUCT, product);
            salesPlanMaterialRequirementProduct.setField(SalesPlanMaterialRequirementProductFields.SIZE_GROUP, sizeGroup);
            salesPlanMaterialRequirementProduct.setField(SalesPlanMaterialRequirementProductFields.SUM_FOR_SIZES, neededQuantity);
            salesPlanMaterialRequirementProduct.setField(SalesPlanMaterialRequirementProductFields.IS_DELIVERY_CREATED, false);

            salesPlanMaterialRequirementProducts.add(salesPlanMaterialRequirementProduct);
        }

        return salesPlanMaterialRequirementProduct;
    }

    private boolean filterByProduct(final Entity salesPlanMaterialRequirementProduct, final Entity product,
            final String fieldName) {
        Entity salesPlanMaterialRequirementProductProduct = salesPlanMaterialRequirementProduct
                .getBelongsToField(SalesPlanMaterialRequirementProductFields.PRODUCT);

        return Objects.nonNull(salesPlanMaterialRequirementProductProduct)
                && Objects.nonNull(salesPlanMaterialRequirementProduct.getField(fieldName))
                && salesPlanMaterialRequirementProductProduct.getId().equals(product.getId());
    }

    private boolean filterBySizeGroup(final Entity salesPlanMaterialRequirementProduct, final Entity sizeGroup) {
        Entity salesPlanMaterialRequirementProductSizeGroup = salesPlanMaterialRequirementProduct
                .getBelongsToField(SalesPlanMaterialRequirementProductFields.SIZE_GROUP);

        return Objects.nonNull(salesPlanMaterialRequirementProductSizeGroup)
                && salesPlanMaterialRequirementProductSizeGroup.getId().equals(sizeGroup.getId());
    }

    private void updateSalesPlanMaterialRequirementProducts(final List<Entity> salesPlanMaterialRequirementProducts) {
        List<Entity> products = getSalesPlanMaterialRequirementProducts(salesPlanMaterialRequirementProducts);

        Set<Long> parentIds = getParentIds(products);
        Set<Long> productIds = getProductIds(products);

        Map<Long, Map<Long, BigDecimal>> resourceStocks = getResourceStocks(products);
        List<Entity> companyProducts = deliveriesService.getCompanyProducts(productIds);
        List<Entity> companyProductsFamilies = deliveriesService.getCompanyProducts(parentIds);
        Map<Long, BigDecimal> neededQuantitiesFromOrders = getNeededQuantitiesFromOrders(productIds);

        for (Entity salesPlanMaterialRequirementProduct : salesPlanMaterialRequirementProducts) {
            Entity product = salesPlanMaterialRequirementProduct
                    .getBelongsToField(SalesPlanMaterialRequirementProductFields.PRODUCT);

            Long productId = product.getId();
            Entity parent = product.getBelongsToField(ProductFields.PARENT);

            BigDecimal currentStock = BigDecimalUtils.convertNullToZero(getCurrentStock(resourceStocks, productId));
            BigDecimal neededQuantity = BigDecimalUtils.convertNullToZero(neededQuantitiesFromOrders.get(productId));

            if (neededQuantity.compareTo(BigDecimal.ZERO) < 0) {
                neededQuantity = BigDecimal.ZERO;
            }

            Optional<Entity> mayBeCompanyProduct = deliveriesService.getCompanyProduct(companyProducts, productId);

            Entity supplier = null;
            BigDecimal minimumOrderQuantity = null;

            if (mayBeCompanyProduct.isPresent()) {
                Entity companyProduct = mayBeCompanyProduct.get();

                supplier = companyProduct.getBelongsToField(CompanyProductFields.COMPANY);
                minimumOrderQuantity = companyProduct.getDecimalField(CompanyProductFields.MINIMUM_ORDER_QUANTITY);
            } else {
                if (Objects.nonNull(parent)) {
                    Optional<Entity> mayBeCompanyProductsFamily = deliveriesService.getCompanyProduct(companyProductsFamilies,
                            parent.getId());

                    if (mayBeCompanyProductsFamily.isPresent()) {
                        Entity companyProductsFamily = mayBeCompanyProductsFamily.get();

                        supplier = companyProductsFamily.getBelongsToField(CompanyProductFields.COMPANY);
                        minimumOrderQuantity = companyProductsFamily
                                .getDecimalField(CompanyProductFields.MINIMUM_ORDER_QUANTITY);
                    }
                }
            }

            salesPlanMaterialRequirementProduct.setField(SalesPlanMaterialRequirementProductFields.CURRENT_STOCK, currentStock);
            salesPlanMaterialRequirementProduct.setField(SalesPlanMaterialRequirementProductFields.NEEDED_QUANTITY,
                    neededQuantity);
            salesPlanMaterialRequirementProduct.setField(SalesPlanMaterialRequirementProductFields.SUPPLIER, supplier);
            salesPlanMaterialRequirementProduct.setField(SalesPlanMaterialRequirementProductFields.MINIMUM_ORDER_QUANTITY,
                    minimumOrderQuantity);
        }
    }

    public List<Entity> getSalesPlanMaterialRequirementProducts(final List<Entity> salesPlanMaterialRequirementProducts) {
        return salesPlanMaterialRequirementProducts.stream()
                .map(salesPlanMaterialRequirementProduct -> salesPlanMaterialRequirementProduct
                        .getBelongsToField(SalesPlanMaterialRequirementProductFields.PRODUCT))
                .collect(Collectors.toList());
    }

    public Set<Long> getParentIds(final List<Entity> products) {
        return products.stream().filter(product -> Objects.nonNull(product.getBelongsToField(ProductFields.PARENT)))
                .map(product -> product.getBelongsToField(ProductFields.PARENT).getId()).collect(Collectors.toSet());
    }

    public Set<Long> getProductIds(final List<Entity> products) {
        return products.stream().map(Entity::getId).collect(Collectors.toSet());
    }

    private Map<Long, Map<Long, BigDecimal>> getResourceStocks(final List<Entity> products) {
        Map<Long, Map<Long, BigDecimal>> resourceStocks = Maps.newHashMap();

        if (!products.isEmpty()) {
            List<Entity> locations = materialFlowResourcesService.getWarehouseLocationsFromDB();

            resourceStocks = materialFlowResourcesService.getQuantitiesForProductsAndLocations(products, locations);
        }

        return resourceStocks;
    }

    private BigDecimal getCurrentStock(final Map<Long, Map<Long, BigDecimal>> resourceStocks, final Long productId) {
        BigDecimal currentStock = BigDecimal.ZERO;

        for (Map.Entry<Long, Map<Long, BigDecimal>> resourceStock : resourceStocks.entrySet()) {
            currentStock = currentStock.add(BigDecimalUtils.convertNullToZero(resourceStock.getValue().get(productId)),
                    numberService.getMathContext());
        }

        return currentStock;
    }

    private Map<Long, BigDecimal> getNeededQuantitiesFromOrders(final Set<Long> productIds) {
        Map<Long, BigDecimal> neededQuantitiesFromOrders = Maps.newHashMap();

        if (!productIds.isEmpty()) {
            StringBuilder queryBuilder = new StringBuilder();

            queryBuilder.append("SELECT ");
            queryBuilder.append("productid,  SUM(COALESCE(plannedquantity, 0) - COALESCE(usedquantity, 0)) AS neededQuantity ");
            queryBuilder.append("FROM basicproductioncounting_productioncountingquantitydto ");
            queryBuilder.append("WHERE productid IN (:productIds) ");
            queryBuilder.append("AND orderid IN ( ");
            queryBuilder.append(
                    "SELECT id FROM orders_order WHERE state NOT IN ('01pending', '04completed', '05declined', '07abandoned') ");
            queryBuilder.append(") ");
            queryBuilder.append("GROUP BY productid");

            Map<String, Object> params = Maps.newHashMap();

            params.put("productIds", productIds);

            try {
                List<Map<String, Object>> values = jdbcTemplate.queryForList(queryBuilder.toString(), params);

                for (Map<String, Object> value : values) {
                    Long productId = Long.valueOf(value.get("productId").toString());
                    BigDecimal neededQuantity = BigDecimalUtils.convertNullToZero(value.get("neededQuantity"));

                    neededQuantitiesFromOrders.put(productId, neededQuantity);
                }
            } catch (EmptyResultDataAccessException e) {
                return neededQuantitiesFromOrders;
            }
        }

        return neededQuantitiesFromOrders;
    }

    private DataDefinition getSalesPlanMaterialRequirementProductDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_SALES_PLAN_MATERIAL_REQUIREMENT_PRODUCT);
    }

}
