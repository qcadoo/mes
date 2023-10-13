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
package com.qcadoo.mes.supplyNegotiations.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.CompanyFieldsD;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.mes.supplyNegotiations.constants.OfferFields;
import com.qcadoo.mes.supplyNegotiations.constants.OfferProductFields;
import com.qcadoo.mes.supplyNegotiations.constants.OrderedProductFieldsSN;
import com.qcadoo.mes.supplyNegotiations.hooks.OfferDetailsHooks;
import com.qcadoo.mes.supplyNegotiations.print.OfferReportPdf;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class OfferDetailsListeners {

    private static final Integer L_REPORT_WIDTH_A4 = 515;

    private static final String L_FORM_ID = "form.id";

    private static final String L_FILTERS = "filters";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_DELIVERY_DATE_BUFFER = "deliveryDateBuffer";

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
    private PdfHelper pdfHelper;

    @Autowired
    private OfferReportPdf offerReportPdf;

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private OfferDetailsHooks offerDetailsHooks;

    public void fillCompanyFieldsForSupplier(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent supplierLookup = (LookupComponent) view.getComponentByReference(OfferFields.SUPPLIER);
        FieldComponent deliveryDateBufferField = (FieldComponent) view.getComponentByReference(L_DELIVERY_DATE_BUFFER);
        FieldComponent currencyField = (FieldComponent) view.getComponentByReference(OfferFields.CURRENCY);
        GridComponent offerProductsGrid = (GridComponent) view.getComponentByReference(OfferFields.OFFER_PRODUCTS);

        Entity supplier = supplierLookup.getEntity();

        if (Objects.isNull(supplier)) {
            deliveryDateBufferField.setFieldValue(null);
        } else {
            deliveryDateBufferField.setFieldValue(supplier.getField(CompanyFieldsD.BUFFER));

            Entity supplierCurrency = supplier.getBelongsToField(CompanyFieldsD.CURRENCY);

            if (Objects.nonNull(supplierCurrency)) {
                Long oldCurrency = (Long) currencyField.getFieldValue();

                if (Objects.nonNull(oldCurrency) && !oldCurrency.equals(supplierCurrency.getId())
                        && !offerProductsGrid.getEntities().isEmpty()) {
                    view.addMessage("supplyNegotiations.offer.currencyChange.offerProductsPriceVerificationRequired",
                            MessageType.INFO, false);
                }

                currencyField.setFieldValue(supplierCurrency.getId());
                currencyField.requestComponentUpdateState();
            }
        }

        deliveryDateBufferField.requestComponentUpdateState();
    }

    public void fillRequestForQuotationDateField(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        offerDetailsHooks.fillRequestForQuotationDateField(view);
    }

    public void fillNegotiationDateField(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        offerDetailsHooks.fillNegotiationDateField(view);
    }

    public final void printOfferReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (state instanceof FormComponent) {
            state.performEvent(view, "save", args);

            if (!state.isHasError()) {
                view.redirectTo("/supplyNegotiations/offerReport." + args[0] + "?id=" + state.getFieldValue(), true, false);
            }
        } else {
            state.addMessage("supplyNegotiations.offer.report.componentFormError", MessageType.FAILURE);
        }
    }

    public void showRequestForQuotation(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent offerForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long offerId = offerForm.getEntityId();

        if (Objects.isNull(offerId)) {
            return;
        }

        Entity offer = offerForm.getEntity();

        Entity requestForQuotation = offer.getBelongsToField(OfferFields.REQUEST_FOR_QUOTATION);

        if (Objects.isNull(requestForQuotation)) {
            return;
        }

        Long requestForQuotationId = requestForQuotation.getId();

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put(L_FORM_ID, requestForQuotationId);
        parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.requestsForQuotation");

        String url = "../page/supplyNegotiations/requestForQuotationDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void createDelivery(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent offerForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long offerId = offerForm.getEntityId();

        if (Objects.isNull(offerId)) {
            return;
        }

        Entity delivery = createDelivery(supplyNegotiationsService.getOffer(offerId));

        Long deliveryId = delivery.getId();

        if (Objects.isNull(deliveryId)) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put(L_FORM_ID, deliveryId);
        parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.deliveries");

        String url = "../page/deliveries/deliveryDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showSupplyItemsForGivenOffer(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent offerForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long offerId = offerForm.getEntityId();

        if (Objects.isNull(offerId)) {
            return;
        }

        Entity offer = offerForm.getEntity();

        String offerNumber = offer.getStringField(OfferFields.NUMBER);

        if (Objects.isNull(offerNumber)) {
            return;
        }

        Map<String, String> filters = Maps.newHashMap();

        filters.put("offerNumber", offerNumber);

        Map<String, Object> gridOptions = Maps.newHashMap();

        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put(L_GRID_OPTIONS, gridOptions);
        parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.supplyItems");

        String url = "../page/deliveries/supplyItems.html";
        view.redirectTo(url, false, true, parameters);
    }

    private Entity createDelivery(final Entity offer) {
        String number = numberGeneratorService.generateNumber(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY);
        Entity supplier = offer.getBelongsToField(OfferFields.SUPPLIER);
        Entity currency = offer.getBelongsToField(OfferFields.CURRENCY);
        Object deliveryDate = offer.getField(OfferFields.OFFERED_DATE);
        List<Entity> offerProducts = offer.getHasManyField(OfferFields.OFFER_PRODUCTS);

        Entity delivery = deliveriesService.getDeliveryDD().create();

        delivery.setField(DeliveryFields.NUMBER, number);
        delivery.setField(DeliveryFields.SUPPLIER, supplier);
        delivery.setField(DeliveryFields.DELIVERY_DATE, deliveryDate);
        delivery.setField(DeliveryFields.EXTERNAL_SYNCHRONIZED, true);
        delivery.setField(DeliveryFields.ORDERED_PRODUCTS, createOrderedProducts(offerProducts));
        delivery.setField(DeliveryFields.CURRENCY, currency);

        delivery = delivery.getDataDefinition().save(delivery);

        return delivery;
    }

    private List<Entity> createOrderedProducts(final List<Entity> offerProducts) {
        List<Entity> orderedProducts = Lists.newArrayList();

        for (Entity offerProduct : offerProducts) {
            orderedProducts.add(createOrderedProduct(offerProduct));
        }

        return orderedProducts;
    }

    private Entity createOrderedProduct(final Entity offerProduct) {
        Entity product = offerProduct.getBelongsToField(OfferProductFields.PRODUCT);
        Entity offer = offerProduct.getBelongsToField(OfferProductFields.OFFER);
        BigDecimal orderedQuantity = offerProduct.getDecimalField(OfferProductFields.QUANTITY);
        BigDecimal pricePerUnit = offerProduct.getDecimalField(OfferProductFields.PRICE_PER_UNIT);
        BigDecimal totalPrice = offerProduct.getDecimalField(OfferProductFields.TOTAL_PRICE);
        BigDecimal conversion = getConversion(product);
        BigDecimal additionalQuantity = orderedQuantity.multiply(conversion, numberService.getMathContext());

        Entity orderedProduct = deliveriesService.getOrderedProductDD().create();

        orderedProduct.setField(OrderedProductFields.PRODUCT, product);
        orderedProduct.setField(OrderedProductFieldsSN.OFFER, offer);
        orderedProduct.setField(OrderedProductFields.ORDERED_QUANTITY, numberService.setScaleWithDefaultMathContext(orderedQuantity));
        orderedProduct.setField(OrderedProductFields.PRICE_PER_UNIT, numberService.setScaleWithDefaultMathContext(pricePerUnit));
        orderedProduct.setField(OrderedProductFields.TOTAL_PRICE, numberService.setScaleWithDefaultMathContext(totalPrice));
        orderedProduct.setField(OrderedProductFields.CONVERSION, conversion);
        orderedProduct.setField(OrderedProductFields.ADDITIONAL_QUANTITY, additionalQuantity);

        return orderedProduct;
    }

    private BigDecimal getConversion(final Entity product) {
        String unit = product.getStringField(ProductFields.UNIT);
        String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);

        if (Objects.isNull(additionalUnit)) {
            return BigDecimal.ONE;
        }

        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions.belongsTo(
                        UnitConversionItemFieldsB.PRODUCT, product)));

        if (unitConversions.isDefinedFor(additionalUnit)) {
            return unitConversions.asUnitToConversionMap().get(additionalUnit);
        } else {
            return BigDecimal.ZERO;
        }
    }

    public void validateColumnsWidthForOffer(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent requestForQuotationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long offerId = requestForQuotationForm.getEntity().getId();

        Entity offer = supplyNegotiationsService.getOffer(offerId);

        List<String> columnNames = offerReportPdf.getUsedColumnsInOfferReport(offer);

        if (!pdfHelper.validateReportColumnWidths(L_REPORT_WIDTH_A4, parameterService.getReportColumnWidths(), columnNames)) {
            state.addMessage("deliveries.delivery.printOrderReport.columnsWidthIsGreaterThenMax", MessageType.INFO, false);
        }
    }

}
