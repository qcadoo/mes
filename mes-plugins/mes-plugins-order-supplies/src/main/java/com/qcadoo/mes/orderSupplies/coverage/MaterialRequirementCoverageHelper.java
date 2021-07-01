package com.qcadoo.mes.orderSupplies.coverage;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orderSupplies.OrderSuppliesService;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductLoggingFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MaterialRequirementCoverageHelper {

    @Autowired
    private OrderSuppliesService orderSuppliesService;

    @Autowired
    private NumberService numberService;


    public Entity createCoverageProductLoggingForOrder(final Entity registerEntry, final Date actualDate) {
        Entity coverageProductLogging = orderSuppliesService.getCoverageProductLoggingDD().create();

        coverageProductLogging.setField(CoverageProductLoggingFields.DATE,
                getCoverageProductLoggingDateForOrder(registerEntry, actualDate));
        coverageProductLogging.setField(CoverageProductLoggingFields.ORDER, Long.valueOf(registerEntry.getIntegerField("orderId")));
        if(Objects.nonNull(registerEntry.getIntegerField("operationId"))) {
            coverageProductLogging.setField(CoverageProductLoggingFields.OPERATION, Long.valueOf(registerEntry.getIntegerField("operationId")));
        }
        coverageProductLogging.setField(CoverageProductLoggingFields.CHANGES,
                numberService.setScaleWithDefaultMathContext(registerEntry.getDecimalField("quantity")));
        coverageProductLogging.setField(CoverageProductLoggingFields.EVENT_TYPE, registerEntry.getStringField("eventType"));

        return coverageProductLogging;
    }



    public Entity createProductLoggingForOrderProduced(Entity registerEntry, Date actualDate, Date coverageToDate) {
        Entity coverageProductLogging = orderSuppliesService.getCoverageProductLoggingDD().create();

        coverageProductLogging
                .setField(
                        CoverageProductLoggingFields.DATE,
                        getCoverageProductLoggingDateForOrderProduced(registerEntry.getDateField("finishDate"),
                                actualDate));
        coverageProductLogging.setField(CoverageProductLoggingFields.ORDER, Long.valueOf(registerEntry.getIntegerField("orderId")));
        if(Objects.nonNull(registerEntry.getIntegerField("operationId"))) {
            coverageProductLogging.setField(CoverageProductLoggingFields.OPERATION, Long.valueOf(registerEntry.getIntegerField("operationId")));
        }        coverageProductLogging.setField(CoverageProductLoggingFields.CHANGES,
                numberService.setScaleWithDefaultMathContext(registerEntry.getDecimalField("quantity")));
        coverageProductLogging.setField(CoverageProductLoggingFields.EVENT_TYPE, registerEntry.getStringField("eventType"));

        return coverageProductLogging;
    }

    public void fillCoverageProductForOrder(final Map<Long, Entity> productAndCoverageProducts, final Integer productId,
            final String productType, final Entity coverageProductLogging) {
        if (coverageProductLogging != null) {
            if (productAndCoverageProducts.containsKey(Long.valueOf(productId))) {
                updateCoverageProductForOrder(productAndCoverageProducts, productId, productType, coverageProductLogging);
            } else {
                addCoverageProductForOrder(productAndCoverageProducts, productId, productType, coverageProductLogging);
            }
        }
    }


    public void fillCoverageProductForOrderProduced(Map<Long, Entity> productAndCoverageProducts, Long productId, Entity coverageProductLogging) {
        if (coverageProductLogging != null) {
            if (productAndCoverageProducts.containsKey(productId)) {
                updateCoverageProductForOrderProduced(productAndCoverageProducts, productId, coverageProductLogging);
            }
        }
    }

    private void addCoverageProductForOrder(final Map<Long, Entity> productAndCoverageProducts, final Integer productId,
            final String productType, final Entity coverageProductLogging) {
        Entity coverageProduct = orderSuppliesService.getCoverageProductDD().create();

        coverageProduct.setField(CoverageProductFields.PRODUCT, Long.valueOf(productId));
        coverageProduct.setField(CoverageProductFields.PRODUCT_TYPE, productType);
        coverageProduct.setField(CoverageProductFields.DEMAND_QUANTITY, numberService
                .setScaleWithDefaultMathContext(coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES)));
        coverageProduct.setField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS, Lists.newArrayList(coverageProductLogging));

        productAndCoverageProducts.put(Long.valueOf(productId), coverageProduct);
    }

    private void updateCoverageProductForOrder(final Map<Long, Entity> productAndCoverageProducts, final Integer productId,
            final String productType, final Entity coverageProductLogging) {
        Entity addedCoverageProduct = productAndCoverageProducts.get(Long.valueOf(productId));

        BigDecimal demandQuantity = BigDecimalUtils
                .convertNullToZero(addedCoverageProduct.getDecimalField(CoverageProductFields.DEMAND_QUANTITY));

        demandQuantity = demandQuantity.add(coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES),
                numberService.getMathContext());

        List<Entity> coverageProductLoggings = Lists
                .newArrayList(addedCoverageProduct.getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS));
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

}
