/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.negotForOrderSuppliesWithTechSubcontr.aop;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.negotForOrderSupplies.NegotForOrderSuppliesService;
import com.qcadoo.mes.orderSupplies.OrderSuppliesService;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductLoggingEventType;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductLoggingFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductLoggingState;
import com.qcadoo.mes.orderSupplies.constants.MaterialRequirementCoverageFields;
import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.mes.supplyNegotiations.constants.NegotiationFields;
import com.qcadoo.mes.supplyNegotiations.constants.NegotiationProductFields;
import com.qcadoo.mes.supplyNegotiations.states.constants.NegotiationStateStringValues;
import com.qcadoo.mes.techSubcontrForNegot.constants.NegotiationProductFieldsTSFN;
import com.qcadoo.mes.techSubcontrForOrderSupplies.constants.CoverageProductFieldsTSFOS;
import com.qcadoo.mes.techSubcontrForOrderSupplies.constants.CoverageProductLoggingFieldsTSFOS;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class NegotForOrderSuppliesServiceImpNFOSWTSOverrideUtil {

    @Autowired
    private NegotForOrderSuppliesService negotForOrderSuppliesService;

    @Autowired
    private OrderSuppliesService orderSuppliesService;

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    @Autowired
    private NumberService numberService;

    public List<Entity> createNegotiationProducts(final List<Entity> coverageProducts) {
        List<Entity> negotiationProducts = Lists.newArrayList();

        for (Entity coverageProduct : coverageProducts) {
            BigDecimal neededQuantity = negotForOrderSuppliesService.getNeededQuantity(coverageProduct);

            if (BigDecimal.ZERO.compareTo(neededQuantity) < 0) {
                if (coverageProduct.getBooleanField(CoverageProductFieldsTSFOS.IS_SUBCONTRACTED)) {
                    negotiationProducts.addAll(getNegotiationProducts(coverageProduct));
                } else {
                    negotiationProducts.add(negotForOrderSuppliesService.createNegotiationProduct(coverageProduct));
                }
            }
        }

        return negotiationProducts;
    }

    private List<Entity> getNegotiationProducts(final Entity coverageProduct) {
        List<Entity> negotiationProducts = Lists.newArrayList();

        Entity materialRequirementCoverage = coverageProduct
                .getBelongsToField(CoverageProductFields.MATERIAL_REQUIREMENT_COVERAGE);
        Date coverageToDate = materialRequirementCoverage.getDateField(MaterialRequirementCoverageFields.COVERAGE_TO_DATE);

        Entity firstCoverageProductLogging = getCoverageProductLoggingFirstCoverage(coverageProduct);
        Entity coverageProductLoggingFarthestCoverage = getCoverageProductLoggingFarthestCoverage(coverageProduct);

        Date farthestCoverageDate = null;

        if (coverageProductLoggingFarthestCoverage == null) {
            farthestCoverageDate = firstCoverageProductLogging.getDateField(CoverageProductLoggingFields.DATE);
        } else {
            farthestCoverageDate = coverageProductLoggingFarthestCoverage.getDateField(CoverageProductLoggingFields.DATE);
        }

        List<Entity> coverageProductLoggingDemands = getCoverageProductLoggingDemands(coverageProduct, farthestCoverageDate);

        List<Entity> coverageProductLoggingCoverages = getCoverageProductLoggingCovereges(coverageProduct, farthestCoverageDate);

        Map<Long, BigDecimal> coverageProductLoggingWithQuantities = getCoverageProductLoggingWithQuantities(coverageProductLoggingDemands);

        BigDecimal coverageQuantity = getCoverageQuantity(coverageProductLoggingCoverages);

        Map<Long, BigDecimal> filteredCoverageProductLoggingWithQuantities = filterCoverageProductLoggingWithQuantities(
                coverageProductLoggingWithQuantities, coverageQuantity);

        negotiationProducts = filterNegotiationProducts(
                getNegotiationProductsGroupedByOperation(filteredCoverageProductLoggingWithQuantities), coverageToDate);

        return negotiationProducts;
    }

    private Entity getCoverageProductLoggingFirstCoverage(final Entity coverageProduct) {
        return coverageProduct
                .getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS)
                .find()
                .add(SearchRestrictions.eq(CoverageProductLoggingFields.EVENT_TYPE,
                        CoverageProductLoggingEventType.WAREHOUSE_STATE.getStringValue()))
                .addOrder(SearchOrders.asc(CoverageProductLoggingFields.DATE)).setMaxResults(1).uniqueResult();
    }

    private Entity getCoverageProductLoggingFarthestCoverage(final Entity coverageProduct) {
        return coverageProduct
                .getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS)
                .find()
                .add(SearchRestrictions.eq(CoverageProductLoggingFields.STATE,
                        CoverageProductLoggingState.COVERED.getStringValue()))
                .addOrder(SearchOrders.desc(CoverageProductLoggingFields.DATE)).setMaxResults(1).uniqueResult();
    }

    private List<Entity> getCoverageProductLoggingDemands(final Entity coverageProduct, final Date farthestCoverageDate) {
        return coverageProduct
                .getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS)
                .find()
                .add(SearchRestrictions.or(SearchRestrictions.eq(CoverageProductLoggingFields.EVENT_TYPE,
                        CoverageProductLoggingEventType.ORDER_INPUT.getStringValue()),
                        SearchRestrictions.eq(CoverageProductLoggingFields.EVENT_TYPE,
                                CoverageProductLoggingEventType.OPERATION_INPUT.getStringValue())))
                .add(SearchRestrictions.gt(CoverageProductLoggingFields.DATE, farthestCoverageDate))
                .addOrder(SearchOrders.asc(CoverageProductLoggingFields.DATE)).list().getEntities();
    }

    private List<Entity> getCoverageProductLoggingCovereges(final Entity coverageProduct, final Date farthestCoverageDate) {
        return coverageProduct
                .getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS)
                .find()
                .add(SearchRestrictions.eq(CoverageProductLoggingFields.EVENT_TYPE,
                        CoverageProductLoggingEventType.DELIVERY.getStringValue()))
                .add(SearchRestrictions.gt(CoverageProductLoggingFields.DATE, farthestCoverageDate))
                .addOrder(SearchOrders.asc(CoverageProductLoggingFields.DATE)).list().getEntities();
    }

    private Map<Long, BigDecimal> getCoverageProductLoggingWithQuantities(final List<Entity> coverageProductLoggingDemands) {
        Map<Long, BigDecimal> coverageProductLoggingWithQuantities = Maps.newLinkedHashMap();

        for (Entity coverageProductLogging : coverageProductLoggingDemands) {
            if (coverageProductLoggingWithQuantities.isEmpty()) {
                BigDecimal quantity = coverageProductLogging
                        .getDecimalField(CoverageProductLoggingFields.RESERVE_MISSING_QUANTITY);

                coverageProductLoggingWithQuantities.put(coverageProductLogging.getId(),
                        quantity.abs(numberService.getMathContext()));
            } else {
                BigDecimal quantity = coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES);

                coverageProductLoggingWithQuantities.put(coverageProductLogging.getId(), quantity);
            }
        }

        return coverageProductLoggingWithQuantities;
    }

    private BigDecimal getCoverageQuantity(final List<Entity> coverageProductLoggingCoverages) {
        BigDecimal coverageQuantity = BigDecimal.ZERO;

        for (Entity coverageProductLogging : coverageProductLoggingCoverages) {
            BigDecimal quantity = coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES);

            if (quantity != null) {
                coverageQuantity = coverageQuantity.add(quantity, numberService.getMathContext());
            }
        }

        return coverageQuantity;
    }

    private Map<Long, BigDecimal> filterCoverageProductLoggingWithQuantities(
            final Map<Long, BigDecimal> coverageProductLoggingWithQuantities, final BigDecimal coverageQuantity) {
        Map<Long, BigDecimal> filteredCoverageProductLoggingWithQuantities = Maps
                .newLinkedHashMap(coverageProductLoggingWithQuantities);

        BigDecimal coverage = coverageQuantity;

        for (Entry<Long, BigDecimal> coverageProductLoggingWithQuantity : coverageProductLoggingWithQuantities.entrySet()) {
            Long coverageProductLoggingId = coverageProductLoggingWithQuantity.getKey();
            BigDecimal quantity = coverageProductLoggingWithQuantity.getValue();

            if (coverage.compareTo(quantity) >= 0) {
                coverage = coverage.subtract(quantity, numberService.getMathContext());

                filteredCoverageProductLoggingWithQuantities.remove(coverageProductLoggingId);

                if (BigDecimal.ZERO.compareTo(coverage) == 0) {
                    break;
                }
            } else {
                quantity = quantity.subtract(coverage, numberService.getMathContext());

                filteredCoverageProductLoggingWithQuantities.put(coverageProductLoggingId, quantity);

                break;
            }
        }

        return filteredCoverageProductLoggingWithQuantities;
    }

    private List<Entity> getNegotiationProductsGroupedByOperation(final Map<Long, BigDecimal> coverageProductLoggingWithQuantities) {
        List<Entity> negotiationProductsGroupedByOperation = Lists.newArrayList();

        for (Entry<Long, BigDecimal> coverageProductLoggingWithQuantity : coverageProductLoggingWithQuantities.entrySet()) {
            Long coverageProductLoggingId = coverageProductLoggingWithQuantity.getKey();
            BigDecimal quantity = coverageProductLoggingWithQuantity.getValue();

            Entity coverageProductLogging = orderSuppliesService.getCoverageProductLogging(coverageProductLoggingId);

            if (coverageProductLogging != null) {
                Entity coverageProduct = coverageProductLogging.getBelongsToField(CoverageProductLoggingFields.COVERAGE_PRODUCT);
                Entity product = coverageProduct.getBelongsToField(CoverageProductFields.PRODUCT);

                Date date = coverageProductLogging.getDateField(CoverageProductLoggingFields.DATE);
                Entity subcontractedOperation = coverageProductLogging
                        .getBelongsToField(CoverageProductLoggingFieldsTSFOS.SUBCONTRACTED_OPERATION);

                fillNegotiationProducts(negotiationProductsGroupedByOperation, product, quantity, date, subcontractedOperation);
            }
        }

        return negotiationProductsGroupedByOperation;
    }

    private void fillNegotiationProducts(final List<Entity> negotiationProducts, final Entity product,
            final BigDecimal neededQuantity, final Date dueDate, final Entity operation) {
        if (checkIfNegotiationProductsContainsOperation(negotiationProducts, operation)) {
            for (Entity negotiationProduct : negotiationProducts) {
                Entity addedOperation = negotiationProduct.getBelongsToField(NegotiationProductFieldsTSFN.OPERATION);

                if (checkIfOperationsAreSame(operation, addedOperation)) {
                    Date addedDueDate = negotiationProduct.getDateField(NegotiationProductFields.DUE_DATE);
                    BigDecimal addedNeededQuantity = negotiationProduct.getDecimalField(NegotiationProductFields.NEEDED_QUANTITY);

                    if (addedDueDate.after(dueDate)) {
                        negotiationProduct.setField(NegotiationProductFields.DUE_DATE, dueDate);
                    }

                    addedNeededQuantity = addedNeededQuantity.add(neededQuantity, numberService.getMathContext());

                    negotiationProduct.setField(NegotiationProductFields.NEEDED_QUANTITY, addedNeededQuantity);
                }
            }
        } else {
            Entity negotiationProduct = createNegotiationProduct(product, neededQuantity, dueDate, operation);

            negotiationProducts.add(negotiationProduct);
        }
    }

    private boolean checkIfNegotiationProductsContainsOperation(final List<Entity> negotiationProducts, final Entity operation) {
        for (Entity negotiationProduct : negotiationProducts) {
            Entity addedOperation = negotiationProduct.getBelongsToField(NegotiationProductFieldsTSFN.OPERATION);

            if (checkIfOperationsAreSame(operation, addedOperation)) {
                return true;
            }
        }

        return false;
    }

    private boolean checkIfOperationsAreSame(final Entity operation, final Entity addedOperation) {
        return (checkIfOperationsAreNull(operation, addedOperation) || checkIfOperationsAreEqual(operation, addedOperation));
    }

    private boolean checkIfOperationsAreNull(final Entity operation, final Entity addedOperation) {
        return ((addedOperation == null) && (operation == null));
    }

    private boolean checkIfOperationsAreEqual(final Entity operation, final Entity addedOperation) {
        return ((addedOperation != null) && (operation != null) && addedOperation.equals(operation));
    }

    private List<Entity> filterNegotiationProducts(final List<Entity> negotiationProducts, final Date coverageToDate) {
        List<Entity> filteredNegotiationProducts = Lists.newLinkedList();

        for (Entity negotiationProduct : negotiationProducts) {
            BigDecimal neededQuantity = negotiationProduct.getDecimalField(NegotiationProductFields.NEEDED_QUANTITY);

            Entity product = negotiationProduct.getBelongsToField(NegotiationProductFields.PRODUCT);
            Entity operation = negotiationProduct.getBelongsToField(NegotiationProductFieldsTSFN.OPERATION);

            BigDecimal negotiatedQuantity = getNegotiatedQuantity(product, coverageToDate, operation);

            neededQuantity = neededQuantity.subtract(negotiatedQuantity, numberService.getMathContext());

            if (BigDecimal.ZERO.compareTo(neededQuantity) < 0) {
                negotiationProduct.setField(NegotiationProductFields.NEEDED_QUANTITY, neededQuantity);

                filteredNegotiationProducts.add(negotiationProduct);
            }
        }

        return filteredNegotiationProducts;
    }

    public Entity createNegotiationProduct(final Entity product, final BigDecimal neededQuantity, final Date dueDate,
            final Entity operation) {
        Entity negotiationProduct = supplyNegotiationsService.getNegotiationProductDD().create();

        negotiationProduct.setField(NegotiationProductFields.PRODUCT, product);
        negotiationProduct.setField(NegotiationProductFields.NEEDED_QUANTITY, numberService.setScaleWithDefaultMathContext(neededQuantity));
        negotiationProduct.setField(NegotiationProductFields.DUE_DATE, dueDate);
        negotiationProduct.setField(NegotiationProductFieldsTSFN.OPERATION, operation);

        return negotiationProduct;
    }

    private BigDecimal getNegotiatedQuantity(final Entity product, final Date coverageToDate, final Entity operation) {
        BigDecimal negotiatedQuantity = BigDecimal.ZERO;

        List<Entity> negotiationProducts = supplyNegotiationsService
                .getNegotiationProductDD()
                .find()
                .createAlias(NegotiationProductFields.NEGOTIATION, NegotiationProductFields.NEGOTIATION)
                .add(SearchRestrictions.belongsTo(NegotiationProductFields.PRODUCT, product))
                .add(SearchRestrictions.lt(NegotiationProductFields.DUE_DATE, coverageToDate))
                .add(SearchRestrictions.or(SearchRestrictions.eq(NegotiationProductFields.NEGOTIATION + "."
                        + NegotiationFields.STATE, NegotiationStateStringValues.DRAFT), SearchRestrictions.eq(
                        NegotiationProductFields.NEGOTIATION + "." + NegotiationFields.STATE,
                        NegotiationStateStringValues.GENERATED_REQUESTS)))
                .add(SearchRestrictions.belongsTo(NegotiationProductFieldsTSFN.OPERATION, operation)).list().getEntities();

        for (Entity negotiationProduct : negotiationProducts) {
            BigDecimal neededQuantity = negotiationProduct.getDecimalField(NegotiationProductFields.NEEDED_QUANTITY);

            if (neededQuantity != null) {
                negotiatedQuantity = negotiatedQuantity.add(neededQuantity, numberService.getMathContext());
            }
        }

        return negotiatedQuantity;
    }

}
