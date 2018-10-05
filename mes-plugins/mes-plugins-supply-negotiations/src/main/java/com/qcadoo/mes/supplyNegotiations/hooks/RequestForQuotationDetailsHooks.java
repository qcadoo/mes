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

import static com.qcadoo.mes.deliveries.constants.CompanyFieldsD.BUFFER;
import static com.qcadoo.mes.states.constants.StateChangeStatus.SUCCESSFUL;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationFields.FARTHEST_LIMIT_DATE;
import static com.qcadoo.mes.supplyNegotiations.constants.OfferFields.NEGOTIATION;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.DESIRED_DATE;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.NUMBER;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.REQUEST_FOR_QUOTATION_PRODUCTS;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.STATE;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.SUPPLIER;
import static com.qcadoo.mes.supplyNegotiations.states.constants.RequestForQuotationStateStringValues.ACCEPTED;
import static com.qcadoo.mes.supplyNegotiations.states.constants.RequestForQuotationStateStringValues.DECLINED;
import static com.qcadoo.mes.supplyNegotiations.states.constants.RequestForQuotationStateStringValues.DRAFT;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;
import com.qcadoo.mes.supplyNegotiations.states.constants.RequestForQuotationStateChangeFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class RequestForQuotationDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_DELIVERY_DATE_BUFFER = "deliveryDateBuffer";

    private static final String L_NEGOTIATION_DATE = "negotiationDate";

    private static final String L_WINDOW = "window";

    private static final String L_OFFERS = "offers";

    private static final String L_CREATE_OFFER = "createOffer";

    private static final String L_SHOW_OFFERS_FOR_GIVEN_REQUEST_FOR_QUOTATION = "showOffersForGivenRequestForQuotation";

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    public void generateRequestForQuotationNumber(final ViewDefinitionState state) {
        numberGeneratorService.generateAndInsertNumber(state, SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_REQUEST_FOR_QUOTATION, L_FORM, NUMBER);
    }

    public void fillBufferForSupplier(final ViewDefinitionState view) {
        LookupComponent supplierLookup = (LookupComponent) view.getComponentByReference(SUPPLIER);
        FieldComponent deliveryDateBufferField = (FieldComponent) view.getComponentByReference(L_DELIVERY_DATE_BUFFER);

        Entity supplier = supplierLookup.getEntity();

        if (supplier == null) {
            deliveryDateBufferField.setFieldValue(null);
        } else {
            deliveryDateBufferField.setFieldValue(supplier.getField(BUFFER));
        }

        deliveryDateBufferField.requestComponentUpdateState();
    }

    public void changeFieldsEnabledDependOnState(final ViewDefinitionState view) {
        FormComponent requestForQuotationForm = (FormComponent) view.getComponentByReference(L_FORM);

        FieldComponent stateField = (FieldComponent) view.getComponentByReference(STATE);
        String state = stateField.getFieldValue().toString();

        if (requestForQuotationForm.getEntityId() == null) {
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
        FormComponent requestForQuotationForm = (FormComponent) view.getComponentByReference(L_FORM);

        GridComponent requestForQuotationProducts = (GridComponent) view.getComponentByReference(REQUEST_FOR_QUOTATION_PRODUCTS);

        requestForQuotationForm.setFormEnabled(enabledForm);
        requestForQuotationProducts.setEnabled(enabledGrid);
        requestForQuotationProducts.setEditable(enabledGrid);
    }

    public void fillDesiredDateFieldRequired(final ViewDefinitionState view) {
        FieldComponent stateField = (FieldComponent) view.getComponentByReference(STATE);
        String state = stateField.getFieldValue().toString();

        FieldComponent desiredDateField = (FieldComponent) view.getComponentByReference(DESIRED_DATE);

        if (DRAFT.equals(state)) {
            desiredDateField.setRequired(true);
        } else {
            desiredDateField.setRequired(false);
        }
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
        FormComponent requestForQuotationForm = (FormComponent) view.getComponentByReference(L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);

        RibbonGroup offers = (RibbonGroup) window.getRibbon().getGroupByName(L_OFFERS);

        RibbonActionItem createOffer = (RibbonActionItem) offers.getItemByName(L_CREATE_OFFER);
        RibbonActionItem showOffersForGivenRequestForQuotation = (RibbonActionItem) offers
                .getItemByName(L_SHOW_OFFERS_FOR_GIVEN_REQUEST_FOR_QUOTATION);

        boolean isEnabled = (requestForQuotationForm.getEntityId() != null);

        updateButtonState(createOffer, isEnabled);
        updateButtonState(showOffersForGivenRequestForQuotation, isEnabled);
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    public void filterStateChangeHistory(final ViewDefinitionState view) {
        final GridComponent historyGrid = (GridComponent) view.getComponentByReference("loggingsGrid");
        final CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService.buildStatusRestriction(
                RequestForQuotationStateChangeFields.STATUS, Lists.newArrayList(SUCCESSFUL.getStringValue()));
        historyGrid.setCustomRestriction(onlySuccessfulRestriction);
    }

}
