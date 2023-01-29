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
package com.qcadoo.mes.deliveries.hooks;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.advancedGenealogy.constants.ParameterFieldsAG;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.NumberPatternFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.constants.ParameterFieldsD;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import static com.qcadoo.mes.deliveries.constants.DefaultAddressType.OTHER;
import static com.qcadoo.mes.deliveries.constants.ParameterFieldsD.DEFAULT_ADDRESS;
import static com.qcadoo.mes.deliveries.constants.ParameterFieldsD.OTHER_ADDRESS;

@Service
public class SupplyParameterHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    public void setFieldsVisibleAndRequired(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        setFieldsVisibleAndRequired(view);
    }


    public void onBeforeRender(final ViewDefinitionState view) {
        setFieldsVisibleAndRequired(view);
        setBatchNumberPatternEnabled(view);
    }

    public final void setBatchNumberPatternEnabled(final ViewDefinitionState view) {
        CheckBoxComponent batchEvidence = (CheckBoxComponent) view
                .getComponentByReference(ParameterFieldsD.PRODUCT_DELIVERY_BATCH_EVIDENCE);
        FieldComponent batchNumberPattern = (FieldComponent) view.getComponentByReference(ParameterFieldsD.PRODUCT_DELIVERY_BATCH_NUMBER_PATTERN);

        if (batchEvidence.isChecked()) {
            batchNumberPattern.setEnabled(true);
        } else {
            batchNumberPattern.setEnabled(false);
            batchNumberPattern.setFieldValue(null);
        }
    }

    public void setFieldsVisibleAndRequired(final ViewDefinitionState view) {
        FieldComponent defaultAddress = (FieldComponent) view.getComponentByReference(DEFAULT_ADDRESS);

        boolean selectForAddress = OTHER.getStringValue().equals(defaultAddress.getFieldValue());

        changeFieldsState(view, OTHER_ADDRESS, selectForAddress);
    }

    private void changeFieldsState(final ViewDefinitionState view, final String fieldName, final boolean selectForAddress) {
        FieldComponent field = (FieldComponent) view.getComponentByReference(fieldName);
        field.setVisible(selectForAddress);
        field.setRequired(selectForAddress);

        if (!selectForAddress) {
            field.setFieldValue(null);
        }

        field.requestComponentUpdateState();
    }

    public void onCreate(final DataDefinition dataDefinition, final Entity parameter) {

        parameter.setField(ParameterFieldsD.DELIVERED_BIGGER_THAN_ORDERED, true);
    }

    public final boolean checkIfNumberPatternIsSelected(final DataDefinition parameterDD, final Entity parameter) {
        if (parameter.getBooleanField(ParameterFieldsD.PRODUCT_DELIVERY_BATCH_EVIDENCE)
                && parameter.getBelongsToField(ParameterFieldsD.PRODUCT_DELIVERY_BATCH_NUMBER_PATTERN) == null) {
            parameter.addError(parameterDD.getField(ParameterFieldsD.PRODUCT_DELIVERY_BATCH_NUMBER_PATTERN),
                    "basic.parameter.message.numberPatternIsNotSelected");
            return false;
        }

        return true;
    }


    public void setUsedInForNumberPattern(final DataDefinition parameterDD, final Entity parameter) {
        Entity numberPattern = parameter.getBelongsToField(ParameterFieldsD.PRODUCT_DELIVERY_BATCH_NUMBER_PATTERN);
        DataDefinition numberPatternDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                BasicConstants.MODEL_NUMBER_PATTERN);
        String usedInValue = translationService.translate("basic.parameter.numberPattern.usedInDeliveryProductBatch.value",
                LocaleContextHolder.getLocale());

        Entity dbNumberPattern = null;
        boolean usedInOtherPlaces = false;
        if (parameter.getId() != null) {
            dbNumberPattern = parameterDD.get(parameter.getId()).getBelongsToField(ParameterFieldsD.PRODUCT_DELIVERY_BATCH_NUMBER_PATTERN);
            usedInOtherPlaces = isUsedInOtherPlaces(dbNumberPattern);
        }
        if (numberPattern != null) {
            numberPattern.setField(NumberPatternFields.USED, true);
            if (dbNumberPattern != null && !dbNumberPattern.getId().equals(numberPattern.getId())) {
                numberPattern.setField(NumberPatternFields.USED_IN, usedInValue);
                numberPatternDD.save(numberPattern);
                if (!usedInOtherPlaces) {
                    dbNumberPattern.setField(NumberPatternFields.USED_IN, null);
                    numberPatternDD.save(dbNumberPattern);
                }
            } else if (dbNumberPattern == null) {
                numberPattern.setField(NumberPatternFields.USED_IN, usedInValue);
                numberPatternDD.save(numberPattern);
            }
        } else if (dbNumberPattern != null && !usedInOtherPlaces) {
            dbNumberPattern.setField(NumberPatternFields.USED_IN, null);
            numberPatternDD.save(dbNumberPattern);
        }
    }

    private boolean isUsedInOtherPlaces(Entity dbNumberPattern) {
        if (dbNumberPattern != null) {
            DataDefinition productDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                    BasicConstants.MODEL_PRODUCT);
            int countProductsWithNumberPattern = productDD.find().add(SearchRestrictions.belongsTo(ProductFields.BATCH_NUMBER_PATTERN, dbNumberPattern)).list().getTotalNumberOfEntities();
            return countProductsWithNumberPattern > 0;
        }
        return false;
    }
}
