/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.basic.hooks;

import static com.qcadoo.mes.basic.constants.ProductFields.CONVERSION_ITEMS;
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class ProductDetailsHooks {

    private static final String L_FORM = "form";

    private static final String UNIT_FROM = "unitFrom";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private UnitService unitService;

    public void generateProductNumber(final ViewDefinitionState state) {
        numberGeneratorService.generateAndInsertNumber(state, BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT,
                L_FORM, "number");
    }

    public void fillUnit(final ViewDefinitionState view) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(L_FORM);

        FieldComponent unitField = (FieldComponent) view.getComponentByReference(UNIT);

        if ((productForm.getEntityId() == null) && (unitField.getFieldValue() == null)) {
            unitField.setFieldValue(unitService.getDefaultUnitFromSystemParameters());
            unitField.requestComponentUpdateState();
        }
    }

    public void disableUnitFromWhenFormIsSaved(final ViewDefinitionState view) {
        final FormComponent productForm = (FormComponent) view.getComponentByReference(L_FORM);
        final AwesomeDynamicListComponent conversionItemsAdl = (AwesomeDynamicListComponent) view
                .getComponentByReference(CONVERSION_ITEMS);

        conversionItemsAdl.setEnabled(productForm.getEntityId() != null);
        for (FormComponent formComponent : conversionItemsAdl.getFormComponents()) {
            formComponent.findFieldComponentByName(UNIT_FROM).setEnabled(formComponent.getEntityId() == null);
        }
    }

    public void disableProductFormForExternalItems(final ViewDefinitionState state) {
        FormComponent productForm = (FormComponent) state.getComponentByReference(L_FORM);
        FieldComponent entityTypeField = (FieldComponent) state.getComponentByReference(ProductFields.ENTITY_TYPE);
        FieldComponent parentField = (FieldComponent) state.getComponentByReference(ProductFields.PARENT);

        Long productId = productForm.getEntityId();

        if (productId == null) {
            productForm.setFormEnabled(true);

            return;
        }

        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(productId);

        if (product == null) {
            return;
        }

        String externalNumber = product.getStringField(ProductFields.EXTERNAL_NUMBER);

        if (StringUtils.isEmpty(externalNumber)) {
            productForm.setFormEnabled(true);
        } else {
            productForm.setFormEnabled(false);
            entityTypeField.setEnabled(true);
            parentField.setEnabled(true);
        }
    }

    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent operationGroupForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity operationGroup = operationGroupForm.getEntity();

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");

        RibbonGroup operationGroups = (RibbonGroup) window.getRibbon().getGroupByName("conversions");

        RibbonActionItem getDefaultConversions = (RibbonActionItem) operationGroups.getItemByName("getDefaultConversions");

        updateButtonState(getDefaultConversions, operationGroup.getId() != null);

    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

}
