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
package com.qcadoo.mes.basic.hooks;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;

import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import static com.qcadoo.mes.basic.constants.ProductFields.CONVERSION_ITEMS;
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;

@Service
public class ProductDetailsHooks {



    private static final String UNIT_FROM = "unitFrom";

    String[] innerComponents = { ProductFields.SIZE, "expiryDateValidity", "productForm",
            "showInProductData" };

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private UnitService unitService;

    public void generateProductNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT,
                QcadooViewConstants.L_FORM, ProductFields.NUMBER);
    }

    public void fillUnit(final ViewDefinitionState view) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        FieldComponent unitField = (FieldComponent) view.getComponentByReference(UNIT);

        if ((productForm.getEntityId() == null) && (unitField.getFieldValue() == null)) {
            unitField.setFieldValue(unitService.getDefaultUnitFromSystemParameters());
            unitField.requestComponentUpdateState();
        }
    }

    public void disableUnitFromWhenFormIsSaved(final ViewDefinitionState view) {
        final FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        final AwesomeDynamicListComponent conversionItemsAdl = (AwesomeDynamicListComponent) view
                .getComponentByReference(CONVERSION_ITEMS);

        conversionItemsAdl.setEnabled(productForm.getEntityId() != null);
        for (FormComponent formComponent : conversionItemsAdl.getFormComponents()) {
            formComponent.findFieldComponentByName(UNIT_FROM).setEnabled(formComponent.getEntityId() == null);
        }
        FieldComponent additionalUnit = (FieldComponent) view.getComponentByReference(ProductFields.ADDITIONAL_UNIT);
        additionalUnit.setEnabled(productForm.getEntityId() != null);
    }

    public void disableProductFormForExternalItems(final ViewDefinitionState state) {
        FormComponent productForm = (FormComponent) state.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent entityTypeField = (FieldComponent) state.getComponentByReference(ProductFields.ENTITY_TYPE);
        FieldComponent parentField = (FieldComponent) state.getComponentByReference(ProductFields.PARENT);

        LookupComponent assortmentLookup = (LookupComponent) state.getComponentByReference(ProductFields.ASSORTMENT);
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
            assortmentLookup.setEnabled(true);
        }
    }

    public void disableProductAdditionalFormForExternalItems(final ViewDefinitionState state) {
        FormComponent productForm = (FormComponent) state.getComponentByReference(QcadooViewConstants.L_FORM);
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
        }
    }

    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent operationGroupForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity operationGroup = operationGroupForm.getEntity();

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonGroup operationGroups = (RibbonGroup) window.getRibbon().getGroupByName("conversions");

        RibbonActionItem getDefaultConversions = (RibbonActionItem) operationGroups.getItemByName("getDefaultConversions");

        updateButtonState(getDefaultConversions, operationGroup.getId() != null);

    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    public void enableCharacteristicsTabForExternalItems(final ViewDefinitionState view) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long productId = productForm.getEntityId();

        if (productId == null) {
            return;
        }

        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(productId);

        if (product == null) {
            return;
        }

        String externalNumber = product.getStringField(ProductFields.EXTERNAL_NUMBER);

        if (!StringUtils.isEmpty(externalNumber)) {
            for (String componentName : innerComponents) {
                ComponentState characteristicsTab = (ComponentState) view.getComponentByReference(componentName);
                characteristicsTab.setEnabled(true);
            }
        }
    }

    public void setProductIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent product = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent productIdForMultiUpload = (FieldComponent) view.getComponentByReference("productIdForMultiUpload");
        FieldComponent productMultiUploadLocale = (FieldComponent) view.getComponentByReference("productMultiUploadLocale");

        if (product.getEntityId() != null) {
            productIdForMultiUpload.setFieldValue(product.getEntityId());
        } else {
            productIdForMultiUpload.setFieldValue("");
        }
        productIdForMultiUpload.requestComponentUpdateState();
        productMultiUploadLocale.setFieldValue(LocaleContextHolder.getLocale());
        productMultiUploadLocale.requestComponentUpdateState();
    }
}
