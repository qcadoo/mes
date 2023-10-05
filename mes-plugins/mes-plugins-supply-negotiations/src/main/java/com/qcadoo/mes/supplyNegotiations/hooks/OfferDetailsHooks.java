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
package com.qcadoo.mes.supplyNegotiations.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.CompanyFieldsD;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.mes.supplyNegotiations.constants.NegotiationFields;
import com.qcadoo.mes.supplyNegotiations.constants.OfferFields;
import com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields;
import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;
import com.qcadoo.mes.supplyNegotiations.states.constants.OfferStateChangeFields;
import com.qcadoo.mes.supplyNegotiations.states.constants.OfferStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;


@Service
public class OfferDetailsHooks {

    private static final String L_DELIVERY_DATE_BUFFER = "deliveryDateBuffer";

    private static final String L_REQUEST_FOR_QUOTATION_DATE = "requestForQuotationDate";

    private static final String L_NEGOTIATION_DATE = "negotiationDate";

    private static final String L_REQUEST_FOR_QUOTATIONS = "requestForQuotations";

    private static final String L_DELIVERIES = "deliveries";

    private static final String L_SHOW_REQUEST_FOR_QUOTATION = "showRequestForQuotation";

    private static final String L_CREATE_DELIVERY = "createDelivery";

    private static final String L_SHOW_SUPPLY_ITEMS_FOR_GIVEN_OFFER = "showSupplyItemsForGivenOffer";

    private static final String L_LOGGINGS_GRID = "loggingsGrid";

    private static final String L_OFFER_PRODUCTS_CUMULATED_TOTAL_PRICE_CURRENCY = "offerProductsCumulatedTotalPriceCurrency";

    private static final String L_TRANSPORT_COST_CURRENCY = "transportCostCurrency";

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private CurrencyService currencyService;

    public void onBeforeRender(final ViewDefinitionState view) {
        generateOfferNumber(view);
        fillCompanyFieldsForSupplier(view);
        fillOfferDateField(view);
        fillRequestForQuotationDateField(view);
        fillNegotiationDateField(view);
        updateRibbonState(view);
        changeFieldsEnabledDependOnState(view);
        fillCurrencyFields(view);
        filterStateChangeHistory(view);
    }

    private void generateOfferNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_OFFER, QcadooViewConstants.L_FORM, OfferFields.NUMBER);
    }

    public void fillCompanyFieldsForSupplier(final ViewDefinitionState view) {
        LookupComponent supplierLookup = (LookupComponent) view.getComponentByReference(OfferFields.SUPPLIER);
        FieldComponent deliveryDateBufferField = (FieldComponent) view.getComponentByReference(L_DELIVERY_DATE_BUFFER);

        Entity supplier = supplierLookup.getEntity();

        if (Objects.isNull(supplier)) {
            deliveryDateBufferField.setFieldValue(null);
        } else {
            deliveryDateBufferField.setFieldValue(supplier.getField(CompanyFieldsD.BUFFER));
        }

        deliveryDateBufferField.requestComponentUpdateState();
    }

    public void fillOfferDateField(final ViewDefinitionState view) {
        FormComponent offerForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent offerDateField = (FieldComponent) view.getComponentByReference(OfferFields.OFFER_DATE);

        String offerDate = (String) offerDateField.getFieldValue();

        if (Objects.isNull(offerForm.getEntityId()) && Objects.isNull(offerDate)) {
            offerDate = DateUtils.toDateTimeString(new Date());
        }

        offerDateField.setFieldValue(offerDate);
        offerDateField.requestComponentUpdateState();
    }

    public void fillRequestForQuotationDateField(final ViewDefinitionState view) {
        LookupComponent requestLookup = (LookupComponent) view.getComponentByReference(OfferFields.REQUEST_FOR_QUOTATION);
        FieldComponent requestForQuotationDateField = (FieldComponent) view.getComponentByReference(L_REQUEST_FOR_QUOTATION_DATE);

        Entity requestForQuotation = requestLookup.getEntity();

        if (Objects.isNull(requestForQuotation)) {
            requestForQuotationDateField.setFieldValue(null);
        } else {
            Date desiredDate = requestForQuotation.getDateField(RequestForQuotationFields.DESIRED_DATE);

            if (Objects.isNull(desiredDate)) {
                requestForQuotationDateField.setFieldValue(null);
            } else {
                requestForQuotationDateField.setFieldValue(DateUtils.toDateTimeString(desiredDate));
            }
        }

        requestForQuotationDateField.requestComponentUpdateState();
    }

    public void fillNegotiationDateField(final ViewDefinitionState view) {
        LookupComponent negotiationLookup = (LookupComponent) view.getComponentByReference(OfferFields.NEGOTIATION);
        FieldComponent negotiationDateField = (FieldComponent) view.getComponentByReference(L_NEGOTIATION_DATE);

        Entity negotiation = negotiationLookup.getEntity();

        if (Objects.isNull(negotiation)) {
            negotiationDateField.setFieldValue(null);
        } else {
            Date negotiationDate = negotiation.getDateField(NegotiationFields.FARTHEST_LIMIT_DATE);

            if (Objects.isNull(negotiationDate)) {
                negotiationDateField.setFieldValue(null);
            } else {
                negotiationDateField.setFieldValue(DateUtils.toDateTimeString(negotiationDate));
            }
        }

        negotiationDateField.requestComponentUpdateState();
    }

    private void updateRibbonState(final ViewDefinitionState view) {
        FormComponent offerForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent requestForQuotationLookup = (LookupComponent) view.getComponentByReference(OfferFields.REQUEST_FOR_QUOTATION);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonGroup requestForQuotationsRibbonGroup = window.getRibbon().getGroupByName(L_REQUEST_FOR_QUOTATIONS);
        RibbonGroup deliveriesRibbonGroup = window.getRibbon().getGroupByName(L_DELIVERIES);

        RibbonActionItem showRequestForQuotationRibbonActionItem = requestForQuotationsRibbonGroup
                .getItemByName(L_SHOW_REQUEST_FOR_QUOTATION);
        RibbonActionItem createDeliveryRibbonActionItem = deliveriesRibbonGroup.getItemByName(L_CREATE_DELIVERY);
        RibbonActionItem showSupplyItemsForGivenOfferRibbonActionItem = deliveriesRibbonGroup.getItemByName(L_SHOW_SUPPLY_ITEMS_FOR_GIVEN_OFFER);

        boolean isEnabled = Objects.nonNull(offerForm.getEntityId());
        boolean isRequestForQuotationFilled = Objects.nonNull(requestForQuotationLookup.getEntity());

        updateButtonState(showRequestForQuotationRibbonActionItem, isEnabled && isRequestForQuotationFilled);
        updateButtonState(createDeliveryRibbonActionItem, isEnabled);
        updateButtonState(showSupplyItemsForGivenOfferRibbonActionItem, isEnabled);
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    private void changeFieldsEnabledDependOnState(final ViewDefinitionState view) {
        FormComponent offerForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent stateField = (FieldComponent) view.getComponentByReference(OfferFields.STATE);

        String state = stateField.getFieldValue().toString();

        if (Objects.isNull(offerForm.getEntityId())) {
            changeFieldsEnabled(view, true, false);
        } else {
            if (OfferStateStringValues.ACCEPTED.equals(state) || OfferStateStringValues.DECLINED.equals(state)) {
                changeFieldsEnabled(view, false, false);
            } else {
                changeFieldsEnabled(view, true, true);
            }
        }
    }

    private void changeFieldsEnabled(final ViewDefinitionState view, final boolean enabledForm, final boolean enabledGrid) {
        FormComponent offerForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent offerProductsGrid = (GridComponent) view.getComponentByReference(OfferFields.OFFER_PRODUCTS);

        offerForm.setFormEnabled(enabledForm);
        offerProductsGrid.setEnabled(enabledGrid);
        offerProductsGrid.setEditable(enabledGrid);
    }

    private void fillCurrencyFields(final ViewDefinitionState view) {
        FormComponent offerForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity offer = offerForm.getEntity();

        deliveriesService.fillCurrencyFieldsForDelivery(view,
                Lists.newArrayList(L_OFFER_PRODUCTS_CUMULATED_TOTAL_PRICE_CURRENCY, L_TRANSPORT_COST_CURRENCY), offer);

        LookupComponent currencyLookup = (LookupComponent) view.getComponentByReference(DeliveryFields.CURRENCY);

        if (Objects.isNull(currencyLookup.getFieldValue()) && Objects.isNull(offerForm.getEntityId())) {
            Entity currencyEntity = currencyService.getCurrentCurrency();

            currencyLookup.setFieldValue(currencyEntity.getId());
            currencyLookup.requestComponentUpdateState();
        }
    }

    private void filterStateChangeHistory(final ViewDefinitionState view) {
        final GridComponent historyGrid = (GridComponent) view.getComponentByReference(L_LOGGINGS_GRID);

        final CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService.buildStatusRestriction(
                OfferStateChangeFields.STATUS, Lists.newArrayList(StateChangeStatus.SUCCESSFUL.getStringValue()));

        historyGrid.setCustomRestriction(onlySuccessfulRestriction);
    }

}
