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

import static com.qcadoo.mes.supplyNegotiations.constants.OfferFields.*;
import static com.qcadoo.mes.supplyNegotiations.constants.OfferProductFields.*;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.DESIRED_DATE;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.REQUEST_FOR_QUOTATION_PRODUCTS;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationProductFields.ORDERED_QUANTITY;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;
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

@Component
public class RequestForQuotationDetailsListeners {

    private static final Integer REPORT_WIDTH_A4 = 515;

    private static final String L_FORM = "form";

    private static final String L_FILTERS = "filters";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    @Autowired
    private RequestForQuotationDetailsHooks requestForQuotationDetailsHooks;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private RequestForQuotationReportPdf requestForQuotationReportPdf;

    @Autowired
    private ParameterService parameterService;

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
        FormComponent requestForQuotationForm = (FormComponent) view.getComponentByReference(L_FORM);
        Long requestForQuotationId = requestForQuotationForm.getEntityId();

        if (requestForQuotationId == null) {
            return;
        }

        Entity offer = createOffer(supplyNegotiationsService.getRequestForQuotation(requestForQuotationId));

        if (offer == null) {
            return;
        }

        Long offerId = offer.getId();

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", offerId);

        parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.offer");

        String url = "../page/supplyNegotiations/offerDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showOffersForGivenRequestForQuotation(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent requestForQuotationForm = (FormComponent) view.getComponentByReference(L_FORM);
        Long requestForQuotationId = requestForQuotationForm.getEntityId();

        if (requestForQuotationId == null) {
            return;
        }

        Entity requestForQuotation = requestForQuotationForm.getEntity();

        String requestForQuotationNumber = requestForQuotation.getStringField(NUMBER);

        if (requestForQuotationNumber == null) {
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

    private Entity createOffer(final Entity requestForQuotation) {
        Entity offer = supplyNegotiationsService.getOfferDD().create();

        offer.setField(NUMBER, numberGeneratorService.generateNumber(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_OFFER));
        offer.setField(SUPPLIER, requestForQuotation.getBelongsToField(SUPPLIER));
        offer.setField(OFFERED_DATE, requestForQuotation.getField(DESIRED_DATE));
        offer.setField(WORKING_DAYS_AFTER_ORDER, requestForQuotation.getField(WORKING_DAYS_AFTER_ORDER));
        offer.setField(OFFER_PRODUCTS, createOfferProducts(requestForQuotation.getHasManyField(REQUEST_FOR_QUOTATION_PRODUCTS)));
        offer.setField(REQUEST_FOR_QUOTATION, requestForQuotation);
        offer.setField(NEGOTIATION, requestForQuotation.getBelongsToField(NEGOTIATION));

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
        Entity offerProduct = supplyNegotiationsService.getOfferProductDD().create();

        offerProduct.setField(PRODUCT, requestForQuotationProduct.getBelongsToField(PRODUCT));
        offerProduct.setField(QUANTITY, numberService.setScaleWithDefaultMathContext(requestForQuotationProduct.getDecimalField(ORDERED_QUANTITY)));
        offerProduct.setField(TOTAL_PRICE, numberService.setScaleWithDefaultMathContext(BigDecimal.ZERO));
        offerProduct.setField(PRICE_PER_UNIT, numberService.setScaleWithDefaultMathContext(BigDecimal.ZERO));

        return offerProduct;
    }

    public void validateColumnsWidthForRequest(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Long requestForQuotationId = ((FormComponent) view.getComponentByReference("form")).getEntity().getId();
        Entity request = supplyNegotiationsService.getRequestForQuotation(requestForQuotationId);
        List<String> columnNames = requestForQuotationReportPdf.getUsedColumnsInRequestReport(request);
        if (!pdfHelper.validateReportColumnWidths(REPORT_WIDTH_A4, parameterService.getReportColumnWidths(), columnNames)) {
            state.addMessage("deliveries.delivery.printOrderReport.columnsWidthIsGreaterThenMax", MessageType.INFO, false);
        }
    }

}
