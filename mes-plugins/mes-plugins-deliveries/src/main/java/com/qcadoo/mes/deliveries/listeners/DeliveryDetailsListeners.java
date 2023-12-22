/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.deliveries.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.CalculationQuantityService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.*;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.mes.deliveries.DeliveredProductMultiPositionService;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.*;
import com.qcadoo.mes.deliveries.print.DeliveryReportPdf;
import com.qcadoo.mes.deliveries.print.OrderReportPdf;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.*;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.field;

@Component
public class DeliveryDetailsListeners {

    private static final Logger LOG = LoggerFactory.getLogger(DeliveryDetailsListeners.class);

    private static final Integer L_REPORT_WIDTH_A4 = 515;

    private static final String L_FORM_ID = "form.id";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_DELIVERY_ID = "delivery.id";

    private static final String L_ORDERED_PRODUCTS_CUMULATED_TOTAL_PRICE = "orderedProductsCumulatedTotalPrice";

    private static final String L_PRODUCT = "product";

    private static final String L_DOT = ".";

    private static final String L_OFFER = "offer";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private FileService fileService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private DeliveryReportPdf deliveryReportPdf;

    @Autowired
    private OrderReportPdf orderReportPdf;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private DeliveredProductMultiPositionService deliveredProductMultiPositionService;

    @Autowired
    private CalculationQuantityService calculationQuantityService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public void fillPrices(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity parameter = parameterService.getParameter();

        String deliveryPriceFillBasedOn = parameter.getStringField(ParameterFieldsD.DELIVERY_PRICE_FILL_BASED_ON);
        boolean deliveryUseNominalCostWhenPriceNotSpecified = parameter.getBooleanField(ParameterFieldsD.DELIVERY_USE_NOMINAL_COST_WHEN_PRICE_NOT_SPECIFIED);

        if (DeliveryPriceFillBasedOn.LAST_PURCHASE_PRICE.getStringValue().equals(deliveryPriceFillBasedOn)) {
            fillBasedOnLastPurchasePrice(view, deliveryUseNominalCostWhenPriceNotSpecified);
        } else if (DeliveryPriceFillBasedOn.PRICES_FROM_LAST_DELIVERY_OFFER.getStringValue().equals(deliveryPriceFillBasedOn)) {
            fillBasedOnPricesFromLastDeliveryOffer(view, deliveryUseNominalCostWhenPriceNotSpecified);
        }
    }

    private void fillBasedOnLastPurchasePrice(final ViewDefinitionState view,
                                              final boolean deliveryUseNominalCostWhenPriceNotSpecified) {
        GridComponent orderedProductsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS);
        LookupComponent currencyLookup = (LookupComponent) view.getComponentByReference(DeliveryFields.CURRENCY);

        Entity currency = currencyLookup.getEntity();
        Entity plnCurrency = currencyService.getCurrencyByAlphabeticCode(CurrencyService.PLN);

        List<Entity> orderedProducts = deliveriesService.getSelectedOrderedProducts(orderedProductsGrid);

        List<Entity> productsToMessage = Lists.newArrayList();

        orderedProducts.forEach(orderedProduct -> {
            Entity product = orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);

            BigDecimal lastPurchaseCost = product.getDecimalField(ProductFieldsCNFP.LAST_PURCHASE_COST);
            BigDecimal nominalCost = product.getDecimalField(ProductFieldsCNFP.NOMINAL_COST);
            BigDecimal pricePerUnit;

            if (BigDecimalUtils.convertNullToZero(lastPurchaseCost).compareTo(BigDecimal.ZERO) == 0
                    && deliveryUseNominalCostWhenPriceNotSpecified) {
                Entity nominalCostCurrency = product.getBelongsToField(ProductFieldsCNFP.NOMINAL_COST_CURRENCY);

                pricePerUnit = getPricePerUnit(currency, plnCurrency, product, productsToMessage, nominalCostCurrency,
                        nominalCost);
            } else {
                if (Objects.isNull(lastPurchaseCost)) {
                    orderedProduct.setField(OrderedProductFields.TOTAL_PRICE, null);
                }

                Entity lastPurchaseCostCurrency = product.getBelongsToField(ProductFieldsCNFP.LAST_PURCHASE_COST_CURRENCY);

                pricePerUnit = getPricePerUnit(currency, plnCurrency, product, productsToMessage, lastPurchaseCostCurrency,
                        lastPurchaseCost);
            }

            orderedProduct.setField(OrderedProductFields.PRICE_PER_UNIT, pricePerUnit);

            orderedProduct.getDataDefinition().save(orderedProduct);
        });

        if (!productsToMessage.isEmpty()) {
            view.addMessage("deliveries.orderedProducts.differentCurrencies", MessageType.INFO, false, productsToMessage.stream()
                    .map(productToMessage -> productToMessage.getStringField(ProductFields.NUMBER)).collect(Collectors.joining(", ")));
        }

        orderedProductsGrid.reloadEntities();

        fillOrderedProductsCumulatedTotalPrice(view, orderedProducts);
    }

    private void fillBasedOnPricesFromLastDeliveryOffer(final ViewDefinitionState view,
                                                        final boolean deliveryUseNominalCostWhenPriceNotSpecified) {
        GridComponent orderedProductsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS);
        LookupComponent supplierLookup = (LookupComponent) view.getComponentByReference(DeliveryFields.SUPPLIER);
        LookupComponent currencyLookup = (LookupComponent) view.getComponentByReference(DeliveryFields.CURRENCY);

        Entity supplier = supplierLookup.getEntity();
        Entity currency = currencyLookup.getEntity();
        Entity plnCurrency = currencyService.getCurrencyByAlphabeticCode(CurrencyService.PLN);

        List<Entity> orderedProducts = deliveriesService.getSelectedOrderedProducts(orderedProductsGrid);

        List<Entity> productsToMessage = Lists.newArrayList();

        orderedProducts.forEach(orderedProduct -> {
            Entity product = orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);
            BigDecimal nominalCost = product.getDecimalField(ProductFieldsCNFP.NOMINAL_COST);

            if (Objects.isNull(supplier) || Objects.isNull(currency)) {
                if (deliveryUseNominalCostWhenPriceNotSpecified) {
                    Entity nominalCostCurrency = product.getBelongsToField(ProductFieldsCNFP.NOMINAL_COST_CURRENCY);

                    BigDecimal pricePerUnit = getPricePerUnit(currency, plnCurrency, product, productsToMessage,
                            nominalCostCurrency, nominalCost);

                    orderedProduct.setField(OrderedProductFields.PRICE_PER_UNIT, pricePerUnit);

                    orderedProduct.getDataDefinition().save(orderedProduct);
                }

                return;
            }

            Entity offerProduct = getLastOfferProduct(supplier, currency, product);

            if (Objects.nonNull(offerProduct)) {
                BigDecimal pricePerUnit = getPricePerUnit(currency, plnCurrency, product, productsToMessage, currency,
                        offerProduct.getDecimalField(OrderedProductFields.PRICE_PER_UNIT));

                orderedProduct.setField(OrderedProductFields.PRICE_PER_UNIT, pricePerUnit);
                orderedProduct.setField(L_OFFER, offerProduct.getBelongsToField(L_OFFER));

                orderedProduct.getDataDefinition().save(orderedProduct);
            } else if (deliveryUseNominalCostWhenPriceNotSpecified) {
                BigDecimal pricePerUnit = getPricePerUnit(currency, plnCurrency, product, productsToMessage, currency,
                        nominalCost);

                orderedProduct.setField(OrderedProductFields.PRICE_PER_UNIT, pricePerUnit);

                orderedProduct.getDataDefinition().save(orderedProduct);
            }
        });

        if (!productsToMessage.isEmpty()) {
            view.addMessage("deliveries.orderedProducts.differentCurrencies", MessageType.INFO, false, productsToMessage.stream()
                    .map(productToMessage -> productToMessage.getStringField(ProductFields.NUMBER)).collect(Collectors.joining(", ")));
        }

        orderedProductsGrid.reloadEntities();

        fillOrderedProductsCumulatedTotalPrice(view, orderedProducts);
    }

    private BigDecimal getPricePerUnit(final Entity deliveryCurrency, final Entity plnCurrency, final Entity product,
                                       final List<Entity> productsToMessage, final Entity productCurrency, final BigDecimal price) {
        BigDecimal pricePerUnit = null;

        if (Objects.isNull(deliveryCurrency) || Objects.isNull(productCurrency) || deliveryCurrency.getId().equals(productCurrency.getId())) {
            pricePerUnit = price;
        } else if (deliveryCurrency.getId().equals(plnCurrency.getId()) && !deliveryCurrency.getId().equals(productCurrency.getId())) {
            pricePerUnit = currencyService.getConvertedValue(price, productCurrency);
        } else if (!deliveryCurrency.getId().equals(plnCurrency.getId()) && !deliveryCurrency.getId().equals(productCurrency.getId())) {
            productsToMessage.add(product);
        }

        return pricePerUnit;
    }

    public Entity getLastOfferProduct(final Entity supplier, final Entity currency, final Entity product) {
        String query = String.format("SELECT offerProduct FROM #%s_%s AS offerProduct "
                        + " INNER JOIN offerProduct.%s AS offer "
                        + " WHERE offer.%s = :state AND offer.%s = :supplier"
                        + " AND offer.%s = :currency AND offerProduct.%s = :product "
                        + " ORDER BY offer.offerDate DESC",
                "supplyNegotiations", "offerProduct", "offer", "state", "supplier", "currency", "product");

        SearchQueryBuilder searchQueryBuilder = dataDefinitionService.get("supplyNegotiations", "offerProduct").find(query);

        searchQueryBuilder.setString("state", "02accepted");
        searchQueryBuilder.setEntity("supplier", supplier);
        searchQueryBuilder.setEntity("currency", currency);
        searchQueryBuilder.setEntity("product", product);

        return searchQueryBuilder.setMaxResults(1).uniqueResult();
    }

    private void fillOrderedProductsCumulatedTotalPrice(final ViewDefinitionState view, final List<Entity> orderedProducts) {
        BigDecimal totalPrice = orderedProducts.stream()
                .filter(orderedProduct -> Objects.nonNull(orderedProduct.getDecimalField(OrderedProductFields.TOTAL_PRICE)))
                .map(orderedProduct -> orderedProduct.getDecimalField(OrderedProductFields.TOTAL_PRICE))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        FieldComponent totalPriceComponent = (FieldComponent) view
                .getComponentByReference(L_ORDERED_PRODUCTS_CUMULATED_TOTAL_PRICE);

        totalPriceComponent.setFieldValue(numberService.formatWithMinimumFractionDigits(totalPrice, 0));
        totalPriceComponent.requestComponentUpdateState();
    }

    public void fillCompanyFieldsForSupplier(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent supplierLookup = (LookupComponent) view.getComponentByReference(DeliveryFields.SUPPLIER);
        FieldComponent deliveryDateBufferField = (FieldComponent) view
                .getComponentByReference(DeliveryFields.DELIVERY_DATE_BUFFER);
        FieldComponent paymentFormField = (FieldComponent) view.getComponentByReference(DeliveryFields.PAYMENT_FORM);
        FieldComponent currencyField = (FieldComponent) view.getComponentByReference(DeliveryFields.CURRENCY);
        FieldComponent contractorCategoryField = (FieldComponent) view
                .getComponentByReference(DeliveryFields.CONTRACTOR_CATEGORY);
        GridComponent orderedProductsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS);

        Entity supplier = supplierLookup.getEntity();

        if (Objects.isNull(supplier)) {
            deliveryDateBufferField.setFieldValue(null);
            paymentFormField.setFieldValue(null);
            contractorCategoryField.setFieldValue(null);
        } else {
            deliveryDateBufferField.setFieldValue(supplier.getIntegerField(CompanyFieldsD.BUFFER));
            paymentFormField.setFieldValue(supplier.getStringField(CompanyFieldsD.PAYMENT_FORM));
            contractorCategoryField.setFieldValue(supplier.getStringField(CompanyFields.CONTRACTOR_CATEGORY));

            Entity supplierCurrency = supplier.getBelongsToField(CompanyFieldsD.CURRENCY);

            if (Objects.nonNull(supplierCurrency)) {
                Long oldCurrency = (Long) currencyField.getFieldValue();

                if (Objects.nonNull(oldCurrency) && !oldCurrency.equals(supplierCurrency.getId())
                        && !orderedProductsGrid.getEntities().isEmpty()) {
                    view.addMessage("deliveries.delivery.currencyChange.orderedProductsPriceVerificationRequired",
                            MessageType.INFO, false);
                }

                currencyField.setFieldValue(supplierCurrency.getId());
                currencyField.requestComponentUpdateState();
            }
        }

        deliveryDateBufferField.requestComponentUpdateState();
        paymentFormField.requestComponentUpdateState();
        contractorCategoryField.requestComponentUpdateState();
    }

    public final void printDeliveryReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (state instanceof FormComponent) {
            view.redirectTo("/deliveries/deliveryReport." + args[0] + "?id=" + state.getFieldValue(), true, false);
        } else {
            state.addMessage("deliveries.delivery.report.componentFormError", MessageType.FAILURE);
        }
    }

    public final void printOrderReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (state instanceof FormComponent) {
            view.redirectTo("/deliveries/orderReport." + args[0] + "?id=" + state.getFieldValue(), true, false);
        } else {
            state.addMessage("deliveries.order.report.componentFormError", MessageType.FAILURE);
        }
    }

    public final void copyProductsWithoutQuantityAndPrice(final ViewDefinitionState view, final ComponentState state,
                                                          final String[] args) {
        copyOrderedProductToDelivered(view, false);

        state.performEvent(view, "reset");
    }

    public final void copyProductsWithQuantityAndPrice(final ViewDefinitionState view, final ComponentState state,
                                                       final String[] args) {
        copyOrderedProductToDelivered(view, true);

        state.performEvent(view, "reset");
    }

    public final void changeStorageLocations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent deliveredProductsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.DELIVERED_PRODUCTS);

        List<Entity> selectedProducts = deliveredProductsGrid.getSelectedEntities();
        Set<Long> selectedProductsIds = deliveredProductsGrid.getSelectedEntitiesIds();

        Entity delivery = deliveryForm.getPersistedEntityWithIncludedFormValues();
        List<Entity> deliveredProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);

        for (Entity selectedProduct : selectedProducts) {
            String palletNumber = selectedProduct.getStringField(DeliveredProductFields.PALLET_NUMBER);

            if (Objects.nonNull(palletNumber)) {
                List<Long> notSelectedMatchingProducts = deliveredProducts.stream()
                        .filter(deliveredProduct -> Objects
                                .nonNull(deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER))
                                && deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER)
                                .getStringField(PalletNumberFields.NUMBER).equals(palletNumber))
                        .map(Entity::getId).filter(deliveredProduct -> !selectedProductsIds.contains(deliveredProduct))
                        .collect(Collectors.toList());

                selectedProductsIds.addAll(notSelectedMatchingProducts);
            }
        }

        String url = "../page/deliveries/changeStorageLocationHelper.html?context={\"form.deliveredProductIds\":\""
                + selectedProductsIds.stream().map(Object::toString).collect(Collectors.joining(",")) + "\","
                + "\"form.delivery\":\"" + delivery.getId() + "\"}";
        view.openModal(url);
    }

    public final void assignStorageLocations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        deleteOldEntries();

        Entity delivery = deliveryForm.getPersistedEntityWithIncludedFormValues();
        Entity deliveredProductMultiEntity = createDeliveredProductMultiEntity(view, delivery);

        deliveredProductMultiEntity.getDataDefinition().save(deliveredProductMultiEntity);

        String url = "../page/deliveries/deliveredProductAddMulti.html?context={\"form.id\":\""
                + deliveredProductMultiEntity.getId() + "\"}";
        view.openModal(url);
    }

    private void deleteOldEntries() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);

        List<Entity> oldEntries = getDeliveredProductMultiDD().find().add(SearchRestrictions.lt(DeliveredProductMultiFields.UPDATE_DATE, cal.getTime()))
                .list().getEntities();

        oldEntries.forEach(oldEntry -> oldEntry.getDataDefinition().delete(oldEntry.getId()));
    }

    private Entity createDeliveredProductMultiEntity(final ViewDefinitionState view, final Entity delivery) {
        Entity deliveredProductMulti = getDeliveredProductMultiDD().create();

        deliveredProductMulti.setField(DeliveredProductMultiFields.DELIVERY, delivery);

        List<Entity> orderedProducts = getSelectedProducts(view);
        List<Entity> deliveredProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);
        List<Entity> deliveredProductMultiPositions = Lists.newArrayList();

        DataDefinition deliveredProductMultiPositionDD = getDeliveredProductMultiPositionDD();

        for (Entity orderedProduct : orderedProducts) {
            Entity deliveredProductMultiPosition = createDeliveredProductMultiPosition(orderedProduct,
                    deliveredProductMultiPositionDD, deliveredProducts);

            deliveredProductMultiPosition.setField(DeliveredProductMultiPositionFields.DELIVERED_PRODUCT_MULTI,
                    deliveredProductMulti);

            deliveredProductMultiPosition = deliveredProductMultiPositionDD.save(deliveredProductMultiPosition);

            deliveredProductMultiPositions.add(deliveredProductMultiPosition);
        }

        deliveredProductMulti.setField(DeliveredProductMultiFields.DELIVERED_PRODUCT_MULTI_POSITIONS,
                deliveredProductMultiPositions);

        return deliveredProductMulti.getDataDefinition().save(deliveredProductMulti);
    }

    private List<Entity> getSelectedProducts(final ViewDefinitionState view) {
        GridComponent orderedProductGrid = (GridComponent) view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS);

        List<Entity> result = Lists.newArrayList();

        Set<Long> ids = orderedProductGrid.getSelectedEntitiesIds();

        if (Objects.nonNull(ids) && !ids.isEmpty()) {
            final SearchCriteriaBuilder searchCriteria = deliveriesService.getOrderedProductDD().find();

            searchCriteria.add(SearchRestrictions.in("id", ids));

            result = searchCriteria.list().getEntities();

            if (!result.isEmpty()) {
                String numbersFilter = orderedProductGrid.getFilters().get(OrderedProductDtoFields.PRODUCT_NUMBER);

                if (StringUtils.isNotBlank(numbersFilter) && numbersFilter.startsWith("[") && numbersFilter.endsWith("]")) {
                    List<String> numbersOrder = Lists.newArrayList(numbersFilter.replace("[", "").replace("]", "").split(","));

                    result.sort((o1, o2) -> {
                        String number1 = o1.getBelongsToField(OrderedProductFields.PRODUCT).getStringField(ProductFields.NUMBER);
                        String number2 = o2.getBelongsToField(OrderedProductFields.PRODUCT).getStringField(ProductFields.NUMBER);

                        return Integer.compare(numbersOrder.indexOf(number1), numbersOrder.indexOf(number2));
                    });
                }
            }
        }

        return result;
    }

    private Entity createDeliveredProductMultiPosition(final Entity orderedProduct,
                                                       final DataDefinition deliveredProductMultiPositionDD, final List<Entity> deliveredProducts) {
        Entity deliveredProductMultiPosition = deliveredProductMultiPositionDD.create();

        Entity product = orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);
        String unit = product.getStringField(ProductFields.UNIT);
        String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);

        if (Objects.isNull(additionalUnit)) {
            additionalUnit = unit;
        }

        BigDecimal alreadyAssignedQuantity = deliveredProductMultiPositionService.countAlreadyAssignedQuantity(orderedProduct,
                orderedProduct.getBelongsToField(L_OFFER), deliveredProducts);
        BigDecimal quantity = orderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY)
                .subtract(alreadyAssignedQuantity);

        if (BigDecimal.ZERO.compareTo(quantity) >= 0) {
            quantity = BigDecimal.ZERO;
        }

        BigDecimal conversion = orderedProduct.getDecimalField(OrderedProductFields.CONVERSION);
        BigDecimal additionalQuantity = calculationQuantityService.calculateAdditionalQuantity(quantity, conversion,
                additionalUnit);

        deliveredProductMultiPosition.setField(DeliveredProductMultiPositionFields.PRODUCT, product);
        deliveredProductMultiPosition.setField(DeliveredProductMultiPositionFields.UNIT, unit);
        deliveredProductMultiPosition.setField(DeliveredProductMultiPositionFields.ADDITIONAL_UNIT, additionalUnit);
        deliveredProductMultiPosition.setField(DeliveredProductMultiPositionFields.QUANTITY, quantity);
        deliveredProductMultiPosition.setField(DeliveredProductMultiPositionFields.ADDITIONAL_QUANTITY, additionalQuantity);
        deliveredProductMultiPosition.setField(DeliveredProductMultiPositionFields.BATCH,
                orderedProduct.getBelongsToField(OrderedProductFields.BATCH));
        deliveredProductMultiPosition.setField(DeliveredProductMultiPositionFields.CONVERSION, conversion);
        deliveredProductMultiPosition.setField(DeliveredProductMultiPositionFields.PRICE_PER_UNIT,
                orderedProduct.getDecimalField(OrderedProductFields.PRICE_PER_UNIT));

        if (PluginUtils.isEnabled("supplyNegotiations")) {
            deliveredProductMultiPosition.setField(L_OFFER, orderedProduct.getBelongsToField(L_OFFER));
        }

        return deliveredProductMultiPosition;
    }

    private void copyOrderedProductToDelivered(final ViewDefinitionState view, boolean copyQuantityAndPrice) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long deliveryId = deliveryForm.getEntityId();

        if (Objects.isNull(deliveryId)) {
            return;
        }

        Entity delivery = deliveriesService.getDelivery(deliveryId);

        deliveryForm.setEntity(copyOrderedProductToDelivered(delivery, copyQuantityAndPrice));
    }

    private Entity copyOrderedProductToDelivered(final Entity delivery, final boolean copyQuantityAndPrice) {
        delivery.setField(DeliveryFields.DELIVERED_PRODUCTS, Lists.newArrayList());

        delivery.getDataDefinition().save(delivery);

        delivery.setField(DeliveryFields.DELIVERED_PRODUCTS, createDeliveredProducts(delivery,
                delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS), copyQuantityAndPrice));

        return delivery;
    }

    private List<Entity> createDeliveredProducts(final Entity delivery, final List<Entity> orderedProducts,
                                                 final boolean copyQuantityAndPrice) {
        List<Entity> deliveredProducts = Lists.newArrayList();

        for (Entity orderedProduct : orderedProducts) {
            deliveredProducts.add(createDeliveredProduct(delivery, orderedProduct, copyQuantityAndPrice));
        }

        return deliveredProducts;
    }

    private Entity createDeliveredProduct(final Entity delivery, final Entity orderedProduct,
                                          final boolean copyQuantityAndPrice) {
        Entity deliveredProduct = deliveriesService.getDeliveredProductDD().create();

        Entity location = delivery.getBelongsToField(DeliveryFields.LOCATION);
        Entity product = orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);
        BigDecimal conversion = orderedProduct.getDecimalField(OrderedProductFields.CONVERSION);

        deliveredProduct.setField(DeliveredProductFields.PRODUCT, product);
        deliveredProduct.setField(DeliveredProductFields.BATCH, orderedProduct.getBelongsToField(OrderedProductFields.BATCH));
        deliveredProduct.setField(DeliveredProductFields.CONVERSION, conversion);
        deliveredProduct.setField(DeliveredProductFields.IS_WASTE, false);
        deliveredProduct.setField(DeliveredProductFields.DELIVERY, delivery);

        if (copyQuantityAndPrice) {
            BigDecimal deliveredQuantity = orderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);

            deliveredProduct.setField(DeliveredProductFields.DELIVERED_QUANTITY,
                    numberService.setScaleWithDefaultMathContext(deliveredQuantity));
            deliveredProduct.setField(DeliveredProductFields.ADDITIONAL_QUANTITY,
                    numberService.setScaleWithDefaultMathContext(deliveredQuantity.multiply(conversion)));
            deliveredProduct.setField(DeliveredProductFields.PRICE_PER_UNIT, numberService
                    .setScaleWithDefaultMathContext(orderedProduct.getDecimalField(OrderedProductFields.PRICE_PER_UNIT)));
            deliveredProduct.setField(DeliveredProductFields.TOTAL_PRICE, numberService
                    .setScaleWithDefaultMathContext(orderedProduct.getDecimalField(OrderedProductFields.TOTAL_PRICE)));
        }

        if (Objects.nonNull(location)) {
            Optional<Entity> mayBeStorageLocation = materialFlowResourcesService.findStorageLocationForProduct(location, product);

            if (mayBeStorageLocation.isPresent()) {
                Entity storageLocation = mayBeStorageLocation.get();

                deliveredProduct.setField(DeliveredProductFields.STORAGE_LOCATION, storageLocation);
            }
        }

        if (PluginUtils.isEnabled("supplyNegotiations")) {
            Entity offer = orderedProduct.getBelongsToField(L_OFFER);

            deliveredProduct.setField(L_OFFER, offer);
        }

        deliveredProduct = deliveredProduct.getDataDefinition().save(deliveredProduct);

        return deliveredProduct;
    }

    public final void createRelatedDelivery(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long deliveryId = deliveryForm.getEntityId();

        if (Objects.isNull(deliveryId)) {
            return;
        }

        Entity delivery = deliveriesService.getDelivery(deliveryId);

        if (DeliveryStateStringValues.RECEIVED.equals(delivery.getStringField(DeliveryFields.STATE))) {
            Entity relatedDelivery = createRelatedDelivery(delivery);

            if (Objects.isNull(relatedDelivery)) {
                deliveryForm.addMessage("deliveries.delivery.relatedDelivery.thereAreNoLacksToCover", MessageType.INFO);

                return;
            }

            Long relatedDeliveryId = relatedDelivery.getId();

            if (Objects.isNull(relatedDeliveryId)) {
                deliveryForm.addMessage("deliveries.delivery.relatedDelivery.invalidDelivery", MessageType.FAILURE);

                return;
            }

            Map<String, Object> parameters = Maps.newHashMap();

            parameters.put(L_FORM_ID, relatedDeliveryId);
            parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.deliveries");

            String url = "../page/deliveries/deliveryDetails.html";
            view.redirectTo(url, false, true, parameters);
        }
    }

    private Entity createRelatedDelivery(final Entity delivery) {
        Entity relatedDelivery = null;

        List<Entity> orderedProducts = createOrderedProducts(delivery);

        if (!orderedProducts.isEmpty()) {
            relatedDelivery = deliveriesService.getDeliveryDD().create();

            relatedDelivery.setField(DeliveryFields.NUMBER, numberGeneratorService
                    .generateNumber(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY));
            relatedDelivery.setField(DeliveryFields.SUPPLIER, delivery.getBelongsToField(DeliveryFields.SUPPLIER));
            relatedDelivery.setField(DeliveryFields.DELIVERY_DATE, new Date());
            relatedDelivery.setField(DeliveryFields.RELATED_DELIVERY, delivery);
            relatedDelivery.setField(DeliveryFields.ORDERED_PRODUCTS, orderedProducts);
            relatedDelivery.setField(DeliveryFields.EXTERNAL_SYNCHRONIZED, true);
            relatedDelivery.setField(DeliveryFields.LOCATION, delivery.getBelongsToField(DeliveryFields.LOCATION));
            relatedDelivery.setField(DeliveryFields.CURRENCY, delivery.getBelongsToField(DeliveryFields.CURRENCY));

            relatedDelivery = relatedDelivery.getDataDefinition().save(relatedDelivery);
        }

        return relatedDelivery;
    }

    private List<Entity> createOrderedProducts(final Entity delivery) {
        List<Entity> newOrderedProducts = Lists.newArrayList();

        List<Entity> orderedProducts = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS);
        List<Entity> deliveredProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);

        for (Entity orderedProduct : orderedProducts) {
            Entity deliveredProduct = getDeliveredProduct(deliveredProducts, orderedProduct);

            if (Objects.isNull(deliveredProduct)) {
                BigDecimal orderedQuantity = orderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);

                newOrderedProducts.add(createOrderedProduct(orderedProduct, orderedQuantity, null));
            } else {
                BigDecimal orderedQuantity = getLackQuantity(orderedProduct, deliveredProduct);

                if (BigDecimal.ZERO.compareTo(orderedQuantity) < 0) {
                    newOrderedProducts.add(createOrderedProduct(orderedProduct, orderedQuantity, deliveredProduct));
                }
            }
        }

        return newOrderedProducts;
    }

    private Entity getDeliveredProduct(final List<Entity> deliveredProducts, final Entity orderedProduct) {
        for (Entity deliveredProduct : deliveredProducts) {
            if (checkIfProductsAreSame(orderedProduct, deliveredProduct)) {
                return deliveredProduct;
            }
        }

        return null;
    }

    private boolean checkIfProductsAreSame(final Entity orderedProduct, final Entity deliveredProduct) {
        return (orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT).getId()
                .equals(deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT).getId()));
    }

    private Entity createOrderedProduct(final Entity orderedProduct, final BigDecimal orderedQuantity,
                                        final Entity deliveredProduct) {
        Entity newOrderedProduct = deliveriesService.getOrderedProductDD().create();

        Entity product = orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);
        BigDecimal conversion = orderedProduct.getDecimalField(OrderedProductFields.CONVERSION);

        newOrderedProduct.setField(OrderedProductFields.PRODUCT, product);
        newOrderedProduct.setField(OrderedProductFields.ORDERED_QUANTITY,
                numberService.setScaleWithDefaultMathContext(orderedQuantity));
        newOrderedProduct.setField(OrderedProductFields.TOTAL_PRICE,
                orderedProduct.getDecimalField(OrderedProductFields.TOTAL_PRICE));
        newOrderedProduct.setField(OrderedProductFields.PRICE_PER_UNIT,
                orderedProduct.getDecimalField(OrderedProductFields.PRICE_PER_UNIT));
        newOrderedProduct.setField(OrderedProductFields.CONVERSION, conversion);
        newOrderedProduct.setField(OrderedProductFields.ADDITIONAL_QUANTITY,
                BigDecimalUtils.convertNullToZero(orderedQuantity).multiply(conversion, numberService.getMathContext()));

        newOrderedProduct.setField(L_OFFER, orderedProduct.getBelongsToField(L_OFFER));

        return newOrderedProduct;
    }


    public BigDecimal getConversion(final Entity product) {
        String unit = product.getStringField(ProductFields.UNIT);
        String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);

        if (Objects.isNull(additionalUnit)) {
            return BigDecimal.ONE;
        }

        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                searchCriteriaBuilder -> searchCriteriaBuilder
                        .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

        if (unitConversions.isDefinedFor(additionalUnit)) {
            return unitConversions.asUnitToConversionMap().get(additionalUnit);
        } else {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getLackQuantity(final Entity orderedProduct, final Entity deliveredProduct) {
        BigDecimal orderedQuantity = orderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);
        BigDecimal deliveredQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY);
        BigDecimal damagedQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.DAMAGED_QUANTITY);

        if (Objects.isNull(damagedQuantity)) {
            return orderedQuantity.subtract(deliveredQuantity, numberService.getMathContext());
        } else {
            return orderedQuantity.subtract(deliveredQuantity, numberService.getMathContext()).add(damagedQuantity,
                    numberService.getMathContext());
        }
    }

    public final void showRelatedDeliveries(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long deliveryId = deliveryForm.getEntityId();

        if (Objects.isNull(deliveryId)) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put(L_DELIVERY_ID, deliveryId);
        parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.deliveries");

        String url = "../page/deliveries/deliveriesList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showProduct(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent orderedProductGrid = (GridComponent) view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS);
        GridComponent deliveredProductsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.DELIVERED_PRODUCTS);

        List<Entity> selectedEntities = orderedProductGrid.getSelectedEntities();

        if (selectedEntities.isEmpty()) {
            selectedEntities = deliveredProductsGrid.getSelectedEntities();
        }

        Entity selectedEntity = selectedEntities.iterator().next();
        selectedEntity = selectedEntity.getDataDefinition().getMasterModelEntity(selectedEntity.getId());
        Entity product = selectedEntity.getBelongsToField(L_PRODUCT);

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put(L_FORM_ID, product.getId());
        parameters.put(L_WINDOW_ACTIVE_MENU, "basic.products");

        String url = "../page/basic/productDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void disableShowProductButton(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveriesService.disableShowProductButton(view);
    }

    public void validateColumnsWidthForOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long deliveryId = deliveryForm.getEntityId();

        Entity delivery = deliveriesService.getDelivery(deliveryId);

        List<String> columnNames = orderReportPdf.getUsedColumnsInOrderReport(delivery);

        if (!pdfHelper.validateReportColumnWidths(L_REPORT_WIDTH_A4, parameterService.getReportColumnWidths(), columnNames)) {
            state.addMessage("deliveries.delivery.printOrderReport.columnsWidthIsGreaterThenMax", MessageType.INFO, false);
        }
    }

    public void validateColumnsWidthForDelivery(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long deliveryId = deliveryForm.getEntityId();

        Entity delivery = deliveriesService.getDelivery(deliveryId);

        List<String> columnNames = deliveryReportPdf.getUsedColumnsInDeliveryReport(delivery);

        if (!pdfHelper.validateReportColumnWidths(L_REPORT_WIDTH_A4, parameterService.getReportColumnWidths(), columnNames)) {
            state.addMessage("deliveries.delivery.printOrderReport.columnsWidthIsGreaterThenMax", MessageType.INFO, false);
        }
    }

    public void downloadAttachment(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent attachmentsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.ATTACHMENTS);

        if (Objects.isNull(attachmentsGrid.getSelectedEntitiesIds()) || attachmentsGrid.getSelectedEntitiesIds().isEmpty()) {
            state.addMessage("deliveries.deliveryDetails.window.ribbon.atachments.nonSelectedAtachment",
                    ComponentState.MessageType.INFO);

            return;
        }

        List<File> attachments = Lists.newArrayList();

        for (Long deliveryAttachmentId : attachmentsGrid.getSelectedEntitiesIds()) {
            Entity deliveryAttachment = getDeliveryAttachmentDD().get(deliveryAttachmentId);

            File attachment = new File(deliveryAttachment.getStringField(DeliveryAttachmentFields.ATTACHMENT));

            attachments.add(attachment);
        }

        File zipFile = null;

        try {
            zipFile = fileService.compressToZipFile(attachments, false);
        } catch (IOException e) {
            LOG.error("Unable to compress documents to zip file.", e);

            return;
        }

        view.redirectTo(fileService.getUrl(zipFile.getAbsolutePath()) + "?clean", true, false);
    }

    public void downloadProductAttachment(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent orderedProductsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS);

        Set<Long> ids = orderedProductsGrid.getSelectedEntitiesIds();

        SearchCriteriaBuilder searchCriteria = deliveriesService.getOrderedProductDD().find()
                .createAlias(BasicConstants.MODEL_PRODUCT, BasicConstants.MODEL_PRODUCT, JoinType.INNER)
                .createAlias(BasicConstants.MODEL_PRODUCT + L_DOT + ProductFields.PRODUCT_ATTACHMENTS,
                        ProductFields.PRODUCT_ATTACHMENTS, JoinType.INNER)
                .setProjection(SearchProjections.list()
                        .add(alias(field(ProductFields.PRODUCT_ATTACHMENTS + L_DOT + ProductAttachmentFields.ATTACHMENT),
                                ProductAttachmentFields.ATTACHMENT)));

        if (ids.isEmpty()) {
            searchCriteria.createAlias(DeliveriesConstants.MODEL_DELIVERY, DeliveriesConstants.MODEL_DELIVERY, JoinType.INNER)
                    .add(SearchRestrictions.in(DeliveriesConstants.MODEL_DELIVERY + L_DOT + "id", deliveryForm.getEntityId()));
        } else {
            searchCriteria.add(SearchRestrictions.in("id", ids));
        }

        List<Entity> result = searchCriteria.list().getEntities();

        if (result.isEmpty()) {
            return;
        }

        List<File> attachments = result.stream()
                .map(productAttachment -> new File(productAttachment.getStringField(ProductAttachmentFields.ATTACHMENT)))
                .collect(Collectors.toList());

        File zipFile;

        try {
            zipFile = fileService.compressToZipFile(attachments, false);
        } catch (IOException e) {
            LOG.error("Unable to compress documents to zip file.", e);

            return;
        }

        view.redirectTo(fileService.getUrl(zipFile.getAbsolutePath()) + "?clean", true, false);
    }

    private DataDefinition getDeliveredProductMultiPositionDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERED_PRODUCT_MULTI_POSITION);
    }

    private DataDefinition getDeliveredProductMultiDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERED_PRODUCT_MULTI);
    }

    private DataDefinition getDeliveryAttachmentDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY_ATTACHMENT);
    }

}
