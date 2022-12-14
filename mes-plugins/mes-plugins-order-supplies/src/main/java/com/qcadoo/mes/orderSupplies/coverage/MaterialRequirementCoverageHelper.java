package com.qcadoo.mes.orderSupplies.coverage;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orderSupplies.OrderSuppliesService;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductLoggingFields;
import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MaterialRequirementCoverageHelper {

    @Autowired
    private OrderSuppliesService orderSuppliesService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public List<Long> getOrderProductsIds(final Entity order) {
        return getOrdersProductsIds(Lists.newArrayList(order));
    }

    public List<Long> getOrdersProductsIds(final List<Entity> orders) {

        List<Number> orderIds = orders.stream().map(Entity::getId).collect(Collectors.toList());


        String sqlIn = "SELECT distinct registry.productId AS productId FROM #orderSupplies_productionCountingQuantityInput AS registry "
                + "WHERE registry.orderId IN :ids AND eventType IN ('04orderInput','03operationInput') ";

        List<Entity> regsIn = getCoverageRegisterDD().find(sqlIn)
                .setParameterList("ids", orderIds.stream().map(x -> x.intValue()).collect(Collectors.toList())).list()
                .getEntities();

        String sqlOut = "SELECT distinct registry.productId AS productId FROM #orderSupplies_productionCountingQuantityOutput AS registry "
                + "WHERE registry.orderId IN :ids ";

        List<Entity> regOut = getCoverageRegisterDD().find(sqlOut)
                .setParameterList("ids", orderIds.stream().map(x -> x.intValue()).collect(Collectors.toList())).list()
                .getEntities();

        List<Long> ids = regsIn.stream().map(p -> ((Number) p.getField("productId")).longValue()).collect(Collectors.toList());
        ids.addAll(regOut.stream().map(p -> ((Number) p.getField("productId")).longValue()).collect(Collectors.toList()));

        return ids;
    }

    public List<Entity> findComponentEntries(final Entity order) {
        String query = "select registry from #orderSupplies_productionCountingQuantityInput as registry, \n"
                + "                #technologies_operationProductInComponent as operationProductInComponent \n"
                + "               where  registry.orderId = :orderId AND eventType in ('04orderInput','03operationInput') \n"
                + "                and productType = '02intermediate' \n"
                + "                and operationProductInComponent.operationComponent.id = registry.technologyOperationComponentId \n"
                + "                and (operationProductInComponent.product.id = registry.productId or operationProductInComponent.product = \n"
                + "                (select productFamily from #technologies_productToProductGroupTechnology as productToProductGroupTechnology \n"
                + "                where productToProductGroupTechnology.finalProduct.id = registry.orderProductId and \n"
                + "                productToProductGroupTechnology.orderProduct.id = registry.productId)) order by operationProductInComponent.priority ";
        return getCoverageRegisterDD().find(query).setParameter("orderId", order.getId().intValue()).list().getEntities();
    }

    public Entity createCoverageProductLoggingForOrder(final Entity registerEntry, final Date actualDate) {
        Entity coverageProductLogging = orderSuppliesService.getCoverageProductLoggingDD().create();

        coverageProductLogging.setField(CoverageProductLoggingFields.DATE,
                getCoverageProductLoggingDateForOrder(registerEntry, actualDate));
        coverageProductLogging.setField(CoverageProductLoggingFields.ORDER,
                Long.valueOf(registerEntry.getIntegerField("orderId")));
        if (Objects.nonNull(registerEntry.getIntegerField("operationId"))) {
            coverageProductLogging.setField(CoverageProductLoggingFields.OPERATION,
                    Long.valueOf(registerEntry.getIntegerField("operationId")));
        }
        coverageProductLogging.setField(CoverageProductLoggingFields.CHANGES,
                numberService.setScaleWithDefaultMathContext(registerEntry.getDecimalField("quantity")));
        coverageProductLogging.setField(CoverageProductLoggingFields.EVENT_TYPE, registerEntry.getStringField("eventType"));

        return coverageProductLogging;
    }

    public Entity createProductLoggingForOrderProduced(Entity registerEntry, Date actualDate, Date coverageToDate) {
        Entity coverageProductLogging = orderSuppliesService.getCoverageProductLoggingDD().create();

        coverageProductLogging.setField(CoverageProductLoggingFields.DATE,
                getCoverageProductLoggingDateForOrderProduced(registerEntry.getDateField("finishDate"), actualDate));
        coverageProductLogging.setField(CoverageProductLoggingFields.ORDER,
                Long.valueOf(registerEntry.getIntegerField("orderId")));
        if (Objects.nonNull(registerEntry.getIntegerField("operationId"))) {
            coverageProductLogging.setField(CoverageProductLoggingFields.OPERATION,
                    Long.valueOf(registerEntry.getIntegerField("operationId")));
        }
        coverageProductLogging.setField(CoverageProductLoggingFields.CHANGES,
                numberService.setScaleWithDefaultMathContext(registerEntry.getDecimalField("quantity")));
        coverageProductLogging.setField(CoverageProductLoggingFields.EVENT_TYPE, registerEntry.getStringField("eventType"));

        return coverageProductLogging;
    }

    public void fillCoverageProductForOrder(final Map<Long, Entity> productAndCoverageProducts, final Integer productId,
                                            final String productType, BigDecimal price, final Entity coverageProductLogging) {
        if (coverageProductLogging != null) {
            if (productAndCoverageProducts.containsKey(Long.valueOf(productId))) {
                updateCoverageProductForOrder(productAndCoverageProducts, productId, productType, coverageProductLogging);
            } else {
                addCoverageProductForOrder(productAndCoverageProducts, productId, productType, price, coverageProductLogging);
            }
        }
    }

    public void fillCoverageProductForOrderProduced(Map<Long, Entity> productAndCoverageProducts, Long productId,
            Entity coverageProductLogging) {
        if (coverageProductLogging != null) {
            if (productAndCoverageProducts.containsKey(productId)) {
                updateCoverageProductForOrderProduced(productAndCoverageProducts, productId, coverageProductLogging);
            }
        }
    }

    private void addCoverageProductForOrder(final Map<Long, Entity> productAndCoverageProducts, final Integer productId,
                                            final String productType, BigDecimal price, final Entity coverageProductLogging) {
        Entity coverageProduct = orderSuppliesService.getCoverageProductDD().create();

        coverageProduct.setField(CoverageProductFields.PRODUCT, Long.valueOf(productId));
        coverageProduct.setField(CoverageProductFields.PRICE, price);
        coverageProduct.setField(CoverageProductFields.PRODUCT_TYPE, productType);
        coverageProduct.setField(CoverageProductFields.DEMAND_QUANTITY, numberService
                .setScaleWithDefaultMathContext(coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES)));
        coverageProduct.setField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS, Lists.newArrayList(coverageProductLogging));

        productAndCoverageProducts.put(Long.valueOf(productId), coverageProduct);
    }

    private void updateCoverageProductForOrder(final Map<Long, Entity> productAndCoverageProducts, final Integer productId,
            final String productType, final Entity coverageProductLogging) {
        Entity addedCoverageProduct = productAndCoverageProducts.get(Long.valueOf(productId));

        BigDecimal demandQuantity = BigDecimalUtils.convertNullToZero(addedCoverageProduct
                .getDecimalField(CoverageProductFields.DEMAND_QUANTITY));

        demandQuantity = demandQuantity.add(coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES),
                numberService.getMathContext());

        List<Entity> coverageProductLoggings = Lists.newArrayList(addedCoverageProduct
                .getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS));
        coverageProductLoggings.add(coverageProductLogging);

        addedCoverageProduct.setField(CoverageProductFields.DEMAND_QUANTITY,
                numberService.setScaleWithDefaultMathContext(demandQuantity));
        addedCoverageProduct.setField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS, coverageProductLoggings);

        productAndCoverageProducts.put(Long.valueOf(productId), addedCoverageProduct);
    }

    private void updateCoverageProductForOrderProduced(Map<Long, Entity> productAndCoverageProducts, Long productId,
            Entity coverageProductLogging) {
        Entity addedCoverageProduct = productAndCoverageProducts.get(productId);

        BigDecimal demandQuantity = BigDecimalUtils.convertNullToZero(addedCoverageProduct
                .getDecimalField(CoverageProductFields.PRODUCE_QUANTITY));

        demandQuantity = demandQuantity.add(coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES),
                numberService.getMathContext());

        List<Entity> coverageProductLoggings = Lists.newArrayList(addedCoverageProduct
                .getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS));
        coverageProductLoggings.add(coverageProductLogging);

        addedCoverageProduct.setField(CoverageProductFields.PRODUCE_QUANTITY,
                numberService.setScaleWithDefaultMathContext(demandQuantity));
        addedCoverageProduct.setField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS, coverageProductLoggings);

        productAndCoverageProducts.put(productId, addedCoverageProduct);
    }

    private Date getCoverageProductLoggingDateForOrder(final Entity registerEntry, final Date actualDate) {
        Date startDate = registerEntry.getDateField("startDate");
        Date coverageDate;

        if (startDate.before(actualDate)) {
            coverageDate = new DateTime(actualDate).plusSeconds(3).toDate();
        } else {
            coverageDate = startDate;
        }

        return coverageDate;
    }

    private Date getCoverageProductLoggingDateForOrderProduced(final Date finishDate, final Date actualDate) {
        Date coverageDate = null;

        if (finishDate.before(actualDate)) {
            coverageDate = new DateTime(actualDate).plusSeconds(1).toDate();
        } else {
            coverageDate = finishDate;
        }

        return coverageDate;
    }

    private DataDefinition getCoverageRegisterDD() {
        return dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_PRODUCT);

    }

}
