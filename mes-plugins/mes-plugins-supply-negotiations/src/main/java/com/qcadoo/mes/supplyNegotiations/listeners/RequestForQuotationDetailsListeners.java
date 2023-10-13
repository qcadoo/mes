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
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.deliveries.constants.CompanyFieldsD;
import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.mes.supplyNegotiations.constants.*;
import com.qcadoo.mes.supplyNegotiations.hooks.RequestForQuotationDetailsHooks;
import com.qcadoo.mes.supplyNegotiations.print.RequestForQuotationReportPdf;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class RequestForQuotationDetailsListeners {

    private static final Integer L_REPORT_WIDTH_A4 = 515;

    private static final String L_FORM_ID = "form.id";

    private static final String L_FILTERS = "filters";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private RequestForQuotationReportPdf requestForQuotationReportPdf;

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    @Autowired
    private RequestForQuotationDetailsHooks requestForQuotationDetailsHooks;

    public void fillBufferForSupplier(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        requestForQuotationDetailsHooks.fillBufferForSupplier(view);
    }

    public void fillNegotiationDateField(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        requestForQuotationDetailsHooks.fillNegotiationDateField(view);
    }

    public final void printRequestForQuotationReport(final ViewDefinitionState viewDefinitionState, final ComponentState state,
                                                     final String[] args) {
        if (state instanceof FormComponent) {
            state.performEvent(viewDefinitionState, "save", args);

            if (!state.isHasError()) {
                viewDefinitionState
                        .redirectTo("/supplyNegotiations/requestsForQuotationReport." + args[0] + "?id=" + state.getFieldValue(),
                                true, false);
            }
        } else {
            state.addMessage("supplyNegotiations.requestsForQuatation.report.componentFormError", MessageType.FAILURE);
        }
    }

    public final void createOffer(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent requestForQuotationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long requestForQuotationId = requestForQuotationForm.getEntityId();

        if (Objects.isNull(requestForQuotationId)) {
            return;
        }

        Entity requestForQuotation = supplyNegotiationsService.getRequestForQuotation(requestForQuotationId);

        Entity offer = createOffer(requestForQuotation);

        if (Objects.isNull(offer)) {
            return;
        }

        Long offerId = offer.getId();

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put(L_FORM_ID, offerId);
        parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.offer");

        String url = "../page/supplyNegotiations/offerDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    private Entity createOffer(final Entity requestForQuotation) {
        String number = numberGeneratorService.generateNumber(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER, SupplyNegotiationsConstants.MODEL_OFFER);
        Entity supplier = requestForQuotation.getBelongsToField(RequestForQuotationFields.SUPPLIER);
        Date offeredDate = requestForQuotation.getDateField(RequestForQuotationFields.DESIRED_DATE);
        Integer workingDaysAfterOrder = requestForQuotation.getIntegerField(RequestForQuotationFields.WORKING_DAYS_AFTER_ORDER);
        Entity negotiation = requestForQuotation.getBelongsToField(RequestForQuotationFields.NEGOTIATION);
        List<Entity> requestForQuotationProducts = requestForQuotation.getHasManyField(RequestForQuotationFields.REQUEST_FOR_QUOTATION_PRODUCTS);

        Entity offer = supplyNegotiationsService.getOfferDD().create();

        offer.setField(OfferFields.NUMBER, number);
        offer.setField(OfferFields.SUPPLIER, supplier);
        offer.setField(OfferFields.OFFERED_DATE, offeredDate);
        offer.setField(OfferFields.WORKING_DAYS_AFTER_ORDER, workingDaysAfterOrder);
        offer.setField(OfferFields.OFFER_PRODUCTS, createOfferProducts(requestForQuotationProducts));
        offer.setField(OfferFields.REQUEST_FOR_QUOTATION, requestForQuotation);
        offer.setField(OfferFields.NEGOTIATION, negotiation);
        offer.setField(OfferFields.CURRENCY, getCurrency(supplier));

        offer = offer.getDataDefinition().save(offer);

        return offer;
    }

    private List<Entity> createOfferProducts(final List<Entity> requestForQuotationProducts) {
        List<Entity> offerProducts = Lists.newArrayList();

        for (Entity requestForQuotationProduct : requestForQuotationProducts) {
            offerProducts.add(createOfferProduct(requestForQuotationProduct));
        }

        return offerProducts;
    }

    private Entity createOfferProduct(final Entity requestForQuotationProduct) {
        Entity product = requestForQuotationProduct.getBelongsToField(RequestForQuotationProductFields.PRODUCT);
        BigDecimal quantity = requestForQuotationProduct.getDecimalField(RequestForQuotationProductFields.ORDERED_QUANTITY);

        Entity offerProduct = supplyNegotiationsService.getOfferProductDD().create();

        offerProduct.setField(OfferProductFields.PRODUCT, product);
        offerProduct.setField(OfferProductFields.QUANTITY, numberService.setScaleWithDefaultMathContext(quantity));
        offerProduct.setField(OfferProductFields.TOTAL_PRICE, numberService.setScaleWithDefaultMathContext(BigDecimal.ZERO));
        offerProduct.setField(OfferProductFields.PRICE_PER_UNIT, numberService.setScaleWithDefaultMathContext(BigDecimal.ZERO));

        return offerProduct;
    }

    private Entity getCurrency(final Entity supplier) {
        Entity currency = null;

        if (Objects.nonNull(supplier)) {
            currency = supplier.getBelongsToField(CompanyFieldsD.CURRENCY);
        }

        if (Objects.isNull(currency)) {
            currency = currencyService.getCurrentCurrency();
        }

        return currency;
    }

    public final void showOffersForGivenRequestForQuotation(final ViewDefinitionState view, final ComponentState state,
                                                            final String[] args) {
        FormComponent requestForQuotationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long requestForQuotationId = requestForQuotationForm.getEntityId();

        if (Objects.isNull(requestForQuotationId)) {
            return;
        }

        Entity requestForQuotation = requestForQuotationForm.getEntity();

        String requestForQuotationNumber = requestForQuotation.getStringField(RequestForQuotationFields.NUMBER);

        if (Objects.isNull(requestForQuotationNumber)) {
            return;
        }

        Map<String, String> filters = Maps.newHashMap();

        filters.put("requestForQuotationNumber", requestForQuotationNumber);

        Map<String, Object> gridOptions = Maps.newHashMap();

        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put(L_GRID_OPTIONS, gridOptions);
        parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.offer");

        String url = "../page/supplyNegotiations/offersList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void validateColumnsWidthForRequest(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent requestForQuotationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long requestForQuotationId = requestForQuotationForm.getEntityId();

        Entity request = supplyNegotiationsService.getRequestForQuotation(requestForQuotationId);

        List<String> columnNames = requestForQuotationReportPdf.getUsedColumnsInRequestReport(request);

        if (!pdfHelper.validateReportColumnWidths(L_REPORT_WIDTH_A4, parameterService.getReportColumnWidths(), columnNames)) {
            state.addMessage("deliveries.delivery.printOrderReport.columnsWidthIsGreaterThenMax", MessageType.INFO, false);
        }
    }

}
