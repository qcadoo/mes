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
import com.qcadoo.mes.deliveries.constants.CompanyFieldsD;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.mes.supplyNegotiations.constants.NegotiationFields;
import com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields;
import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;
import com.qcadoo.mes.supplyNegotiations.states.constants.RequestForQuotationStateChangeFields;
import com.qcadoo.mes.supplyNegotiations.states.constants.RequestForQuotationStateStringValues;
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

import static com.qcadoo.mes.states.constants.StateChangeStatus.SUCCESSFUL;


@Service
public class RequestForQuotationDetailsHooks {

    private static final String L_DELIVERY_DATE_BUFFER = "deliveryDateBuffer";

    private static final String L_NEGOTIATION_DATE = "negotiationDate";

    private static final String L_OFFERS = "offers";

    private static final String L_CREATE_OFFER = "createOffer";

    private static final String L_SHOW_OFFERS_FOR_GIVEN_REQUEST_FOR_QUOTATION = "showOffersForGivenRequestForQuotation";

    private static final String L_LOGGINGS_GRID = "loggingsGrid";

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    public void onBeforeRender(final ViewDefinitionState view) {
        generateRequestForQuotationNumber(view);
        fillBufferForSupplier(view);
        fillNegotiationDateField(view);
        updateRibbonState(view);
        changeFieldsEnabledDependOnState(view);
        filterStateChangeHistory(view);
    }

    private void generateRequestForQuotationNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_REQUEST_FOR_QUOTATION, QcadooViewConstants.L_FORM, RequestForQuotationFields.NUMBER);
    }

    public void fillBufferForSupplier(final ViewDefinitionState view) {
        LookupComponent supplierLookup = (LookupComponent) view.getComponentByReference(RequestForQuotationFields.SUPPLIER);
        FieldComponent deliveryDateBufferField = (FieldComponent) view.getComponentByReference(L_DELIVERY_DATE_BUFFER);

        Entity supplier = supplierLookup.getEntity();

        if (Objects.isNull(supplier)) {
            deliveryDateBufferField.setFieldValue(null);
        } else {
            deliveryDateBufferField.setFieldValue(supplier.getField(CompanyFieldsD.BUFFER));
        }

        deliveryDateBufferField.requestComponentUpdateState();
    }

    public void fillNegotiationDateField(final ViewDefinitionState view) {
        LookupComponent negotiationLookup = (LookupComponent) view.getComponentByReference(RequestForQuotationFields.NEGOTIATION);
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

    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent requestForQuotationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonGroup offersRibbonGroup = window.getRibbon().getGroupByName(L_OFFERS);

        RibbonActionItem createOfferRibbonActionItem = offersRibbonGroup.getItemByName(L_CREATE_OFFER);
        RibbonActionItem showOffersForGivenRequestForQuotationRibbonActionItem = offersRibbonGroup
                .getItemByName(L_SHOW_OFFERS_FOR_GIVEN_REQUEST_FOR_QUOTATION);

        boolean isEnabled = Objects.nonNull(requestForQuotationForm.getEntityId());

        updateButtonState(createOfferRibbonActionItem, isEnabled);
        updateButtonState(showOffersForGivenRequestForQuotationRibbonActionItem, isEnabled);
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    public void changeFieldsEnabledDependOnState(final ViewDefinitionState view) {
        FormComponent requestForQuotationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent stateField = (FieldComponent) view.getComponentByReference(RequestForQuotationFields.STATE);

        String state = stateField.getFieldValue().toString();

        if (Objects.isNull(requestForQuotationForm.getEntityId())) {
            changeFieldsEnabled(view, true, false);
        } else {
            if (RequestForQuotationStateStringValues.ACCEPTED.equals(state) || RequestForQuotationStateStringValues.DECLINED.equals(state)) {
                changeFieldsEnabled(view, false, false);
            } else {
                changeFieldsEnabled(view, true, true);
            }
        }
    }

    private void changeFieldsEnabled(final ViewDefinitionState view, final boolean enabledForm, final boolean enabledGrid) {
        FormComponent requestForQuotationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent requestForQuotationProducts = (GridComponent) view.getComponentByReference(RequestForQuotationFields.REQUEST_FOR_QUOTATION_PRODUCTS);

        requestForQuotationForm.setFormEnabled(enabledForm);
        requestForQuotationProducts.setEnabled(enabledGrid);
        requestForQuotationProducts.setEditable(enabledGrid);
    }

    private void fillDesiredDateFieldRequired(final ViewDefinitionState view) {
        FieldComponent stateField = (FieldComponent) view.getComponentByReference(RequestForQuotationFields.STATE);
        FieldComponent desiredDateField = (FieldComponent) view.getComponentByReference(RequestForQuotationFields.DESIRED_DATE);

        String state = stateField.getFieldValue().toString();

        desiredDateField.setRequired(RequestForQuotationStateStringValues.DRAFT.equals(state));
    }

    public void filterStateChangeHistory(final ViewDefinitionState view) {
        final GridComponent historyGrid = (GridComponent) view.getComponentByReference(L_LOGGINGS_GRID);

        final CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService.buildStatusRestriction(
                RequestForQuotationStateChangeFields.STATUS, Lists.newArrayList(SUCCESSFUL.getStringValue()));

        historyGrid.setCustomRestriction(onlySuccessfulRestriction);
    }

}
