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
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.CompanyFieldsD;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;
import com.qcadoo.mes.supplyNegotiations.states.constants.OfferStateChangeFields;
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

import static com.qcadoo.mes.states.constants.StateChangeStatus.SUCCESSFUL;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationFields.FARTHEST_LIMIT_DATE;
import static com.qcadoo.mes.supplyNegotiations.constants.OfferFields.*;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.DESIRED_DATE;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.SUPPLIER;
import static com.qcadoo.mes.supplyNegotiations.states.constants.OfferStateStringValues.ACCEPTED;
import static com.qcadoo.mes.supplyNegotiations.states.constants.OfferStateStringValues.DECLINED;

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

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    @Autowired
    private DeliveriesService deliveriesService;

    public void generateOfferNumber(final ViewDefinitionState state) {
        numberGeneratorService.generateAndInsertNumber(state, SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_OFFER, QcadooViewConstants.L_FORM, NUMBER);
    }

    public void fillBufferForSupplier(final ViewDefinitionState view) {
        LookupComponent supplierLookup = (LookupComponent) view.getComponentByReference(SUPPLIER);
        FieldComponent deliveryDateBufferField = (FieldComponent) view.getComponentByReference(L_DELIVERY_DATE_BUFFER);

        Entity supplier = supplierLookup.getEntity();

        if (supplier == null) {
            deliveryDateBufferField.setFieldValue(null);
        } else {
            deliveryDateBufferField.setFieldValue(supplier.getField(CompanyFieldsD.BUFFER));
        }

        deliveryDateBufferField.requestComponentUpdateState();
    }

    public void changeFieldsEnabledDependOnState(final ViewDefinitionState view) {
        FormComponent offerForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        FieldComponent stateField = (FieldComponent) view.getComponentByReference(STATE);
        String state = stateField.getFieldValue().toString();

        if (offerForm.getEntityId() == null) {
            changeFieldsEnabled(view, true, false);
        } else {
            if (ACCEPTED.equals(state) || DECLINED.equals(state)) {
                changeFieldsEnabled(view, false, false);
            } else {
                changeFieldsEnabled(view, true, true);
            }
        }
    }

    private void changeFieldsEnabled(final ViewDefinitionState view, final boolean enabledForm, final boolean enabledGrid) {
        FormComponent offerForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        GridComponent offerProducts = (GridComponent) view.getComponentByReference(OFFER_PRODUCTS);

        offerForm.setFormEnabled(enabledForm);
        offerProducts.setEnabled(enabledGrid);
        offerProducts.setEditable(enabledGrid);
    }

    public void fillRequestforQuotationDateField(final ViewDefinitionState view) {
        LookupComponent requestLookup = (LookupComponent) view.getComponentByReference(REQUEST_FOR_QUOTATION);
        FieldComponent requestForQuotationDateField = (FieldComponent) view.getComponentByReference(L_REQUEST_FOR_QUOTATION_DATE);

        Entity requestForQuotation = requestLookup.getEntity();

        if (requestForQuotation == null) {
            requestForQuotationDateField.setFieldValue(null);
        } else {
            Date desiredDate = (Date) requestForQuotation.getField(DESIRED_DATE);

            if (desiredDate == null) {
                requestForQuotationDateField.setFieldValue(null);
            } else {
                requestForQuotationDateField.setFieldValue(DateUtils.toDateTimeString(desiredDate));
            }
        }

        requestForQuotationDateField.requestComponentUpdateState();
    }

    public void fillCurrencyFields(final ViewDefinitionState viewDefinitionState) {
        deliveriesService.fillCurrencyFields(viewDefinitionState,
                Lists.newArrayList("offerProductsCumulatedTotalPriceCurrency", "transportCostCurrency"));
    }

    public void fillNegotiationDateField(final ViewDefinitionState view) {
        LookupComponent negotiationLookup = (LookupComponent) view.getComponentByReference(NEGOTIATION);
        FieldComponent negotiationDateField = (FieldComponent) view.getComponentByReference(L_NEGOTIATION_DATE);

        Entity negotiation = negotiationLookup.getEntity();

        if (negotiation == null) {
            negotiationDateField.setFieldValue(null);
        } else {
            Date negotiationDate = (Date) negotiation.getField(FARTHEST_LIMIT_DATE);

            if (negotiationDate == null) {
                negotiationDateField.setFieldValue(null);
            } else {
                negotiationDateField.setFieldValue(DateUtils.toDateTimeString(negotiationDate));
            }
        }

        negotiationDateField.requestComponentUpdateState();
    }

    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent offerForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        LookupComponent requestForQuotaitonLookup = (LookupComponent) view.getComponentByReference(REQUEST_FOR_QUOTATION);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonGroup requestForQuotations = (RibbonGroup) window.getRibbon().getGroupByName(L_REQUEST_FOR_QUOTATIONS);
        RibbonGroup deliveries = (RibbonGroup) window.getRibbon().getGroupByName(L_DELIVERIES);

        RibbonActionItem showRequestForQuotation = (RibbonActionItem) requestForQuotations
                .getItemByName(L_SHOW_REQUEST_FOR_QUOTATION);

        RibbonActionItem createDelivery = (RibbonActionItem) deliveries.getItemByName(L_CREATE_DELIVERY);
        RibbonActionItem showSupplyItemsForGivenOffer = (RibbonActionItem) deliveries
                .getItemByName(L_SHOW_SUPPLY_ITEMS_FOR_GIVEN_OFFER);

        boolean isEnabled = (offerForm.getEntityId() != null);
        boolean isRequestForQuotationFilled = (requestForQuotaitonLookup.getEntity() != null);

        updateButtonState(showRequestForQuotation, isEnabled && isRequestForQuotationFilled);
        updateButtonState(createDelivery, isEnabled);
        updateButtonState(showSupplyItemsForGivenOffer, isEnabled);
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    public void filterStateChangeHistory(final ViewDefinitionState view) {
        final GridComponent historyGrid = (GridComponent) view.getComponentByReference("loggingsGrid");
        final CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService.buildStatusRestriction(
                OfferStateChangeFields.STATUS, Lists.newArrayList(SUCCESSFUL.getStringValue()));
        historyGrid.setCustomRestriction(onlySuccessfulRestriction);
    }

}
