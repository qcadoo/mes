/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.basic.hooks;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.criteriaModifiers.ModelCriteriaModifiers;
import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.qcadoo.mes.basic.constants.ProductFields.CONVERSION_ITEMS;
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;

@Service
public class ProductDetailsHooks {

    private static final String L_CONVERSIONS = "conversions";

    private static final String L_GET_DEFAULT_CONVERSIONS = "getDefaultConversions";

    private static final String L_PRODUCT_FAMILY = "productFamily";

    private static final String L_PRODUCT_FAMILY_SIZES = "productFamilySizes";

    private static final String UNIT_FROM = "unitFrom";

    private static final String L_PRODUCT_ID_FOR_MULTI_UPLOAD = "productIdForMultiUpload";

    private static final String L_PRODUCT_MULTI_UPLOAD_LOCALE = "productMultiUploadLocale";

    private static final String[] innerComponents = {ProductFields.SIZE, ProductFields.EXPIRY_DATE_VALIDITY, ProductFields.EXPIRY_DATE_VALIDITY_UNIT,
            ProductFields.PRODUCT_FORM, ProductFields.SHOW_IN_PRODUCT_DATA};

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private UnitService unitService;

    @Autowired
    private SecurityService securityService;

    public void onBeforeRender(final ViewDefinitionState view) {
        fillUnit(view);
        disableProductFormForExternalItems(view);
        disableUnitFromWhenFormIsSaved(view);
        updateRibbonState(view);
        updateProductFamilySizesRibbonState(view);
        disableEntityTypeWhenProductFamilyHasChildren(view);
        setProductIdForMultiUploadField(view);
        enableCharacteristicsTabForExternalItems(view);
        setCriteriaModifierParameters(view);
    }

    public void fillUnit(final ViewDefinitionState view) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        FieldComponent unitField = (FieldComponent) view.getComponentByReference(UNIT);

        if (Objects.isNull(productForm.getEntityId()) && Objects.isNull(unitField.getFieldValue())) {
            unitField.setFieldValue(unitService.getDefaultUnitFromSystemParameters());
            unitField.requestComponentUpdateState();
        }
    }

    public void disableUnitFromWhenFormIsSaved(final ViewDefinitionState view) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        AwesomeDynamicListComponent conversionItemsAdl = (AwesomeDynamicListComponent) view
                .getComponentByReference(CONVERSION_ITEMS);

        conversionItemsAdl.setEnabled(Objects.nonNull(productForm.getEntityId()));

        for (FormComponent formComponent : conversionItemsAdl.getFormComponents()) {
            formComponent.findFieldComponentByName(UNIT_FROM).setEnabled(Objects.isNull(formComponent.getEntityId()));
        }

        FieldComponent additionalUnit = (FieldComponent) view.getComponentByReference(ProductFields.ADDITIONAL_UNIT);
        additionalUnit.setEnabled(Objects.nonNull(productForm.getEntityId()));
    }

    public void disableProductFormForExternalItems(final ViewDefinitionState view) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent entityTypeField = (FieldComponent) view.getComponentByReference(ProductFields.ENTITY_TYPE);
        FieldComponent parentField = (FieldComponent) view.getComponentByReference(ProductFields.PARENT);
        FieldComponent categoryField = (FieldComponent) view.getComponentByReference(ProductFields.CATEGORY);
        FieldComponent batchEvidenceField = (FieldComponent) view.getComponentByReference(ProductFields.BATCH_EVIDENCE);
        LookupComponent supplierLookup = (LookupComponent) view.getComponentByReference(ProductFields.SUPPLIER);
        LookupComponent assortmentLookup = (LookupComponent) view.getComponentByReference(ProductFields.ASSORTMENT);
        LookupComponent modelLookup = (LookupComponent) view.getComponentByReference(ProductFields.MODEL);

        Long productId = productForm.getEntityId();

        if (Objects.isNull(productId)) {
            productForm.setFormEnabled(true);

            return;
        }

        Entity product = getProductDD().get(productId);

        if (Objects.isNull(product)) {
            return;
        }

        String externalNumber = product.getStringField(ProductFields.EXTERNAL_NUMBER);

        if (StringUtils.isEmpty(externalNumber)) {
            productForm.setFormEnabled(true);
        } else {
            productForm.setFormEnabled(false);
            entityTypeField.setEnabled(true);
            parentField.setEnabled(true);
            categoryField.setEnabled(true);
            supplierLookup.setEnabled(true);
            assortmentLookup.setEnabled(true);
            modelLookup.setEnabled(true);
            batchEvidenceField.setEnabled(true);
        }
    }

    public void disableProductAdditionalFormForExternalItems(final ViewDefinitionState state) {
        FormComponent productForm = (FormComponent) state.getComponentByReference(QcadooViewConstants.L_FORM);
        Long productId = productForm.getEntityId();

        if (Objects.isNull(productId)) {
            productForm.setFormEnabled(true);

            return;
        }

        Entity product = getProductDD().get(productId);

        if (Objects.isNull(product)) {
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

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup operationGroups = window.getRibbon().getGroupByName(L_CONVERSIONS);
        RibbonActionItem getDefaultConversions = operationGroups.getItemByName(L_GET_DEFAULT_CONVERSIONS);

        Entity operationGroup = operationGroupForm.getEntity();

        updateButtonState(getDefaultConversions, Objects.nonNull(operationGroup.getId()));
    }

    public void updateProductFamilySizesRibbonState(final ViewDefinitionState view) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup productFamily = window.getRibbon().getGroupByName(L_PRODUCT_FAMILY);
        RibbonActionItem productFamilySizes = productFamily.getItemByName(L_PRODUCT_FAMILY_SIZES);

        Entity product = productForm.getEntity();

        updateButtonState(productFamilySizes,
                ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(product.getField(ProductFields.ENTITY_TYPE)));
    }

    public void disableEntityTypeWhenProductFamilyHasChildren(final ViewDefinitionState view) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent entityTypeField = (FieldComponent) view.getComponentByReference(ProductFields.ENTITY_TYPE);
        FieldComponent parentField = (FieldComponent) view.getComponentByReference(ProductFields.PARENT);

        Entity product = productForm.getPersistedEntityWithIncludedFormValues();

        entityTypeField.setEnabled(product.getHasManyField(ProductFields.CHILDREN).isEmpty());

        if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(product.getField(ProductFields.ENTITY_TYPE))) {
            parentField.setFieldValue(null);
            parentField.setEnabled(false);
        } else {
            parentField.setEnabled(true);
        }
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    public void enableCharacteristicsTabForExternalItems(final ViewDefinitionState view) {
        ComponentState showInProductData = view.getComponentByReference(ProductFields.SHOW_IN_PRODUCT_DATA);
        showInProductData.setVisible(securityService.hasCurrentUserRole("ROLE_TECHNOLOGIES"));
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long productId = productForm.getEntityId();

        if (Objects.isNull(productId)) {
            return;
        }

        Entity product = getProductDD().get(productId);

        if (Objects.isNull(product)) {
            return;
        }

        String externalNumber = product.getStringField(ProductFields.EXTERNAL_NUMBER);

        if (!StringUtils.isEmpty(externalNumber)) {
            for (String componentName : innerComponents) {
                ComponentState characteristicsTab = view.getComponentByReference(componentName);
                characteristicsTab.setEnabled(true);
            }
        }
    }

    public void setProductIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent product = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent productIdForMultiUpload = (FieldComponent) view.getComponentByReference(L_PRODUCT_ID_FOR_MULTI_UPLOAD);
        FieldComponent productMultiUploadLocale = (FieldComponent) view.getComponentByReference(L_PRODUCT_MULTI_UPLOAD_LOCALE);

        if (Objects.nonNull(product.getEntityId())) {
            productIdForMultiUpload.setFieldValue(product.getEntityId());
        } else {
            productIdForMultiUpload.setFieldValue("");
        }

        productIdForMultiUpload.requestComponentUpdateState();
        productMultiUploadLocale.setFieldValue(LocaleContextHolder.getLocale());
        productMultiUploadLocale.requestComponentUpdateState();
    }

    private void setCriteriaModifierParameters(final ViewDefinitionState view) {
        LookupComponent assortmentLookup = (LookupComponent) view.getComponentByReference(ProductFields.ASSORTMENT);
        LookupComponent modelLookup = (LookupComponent) view.getComponentByReference(ProductFields.MODEL);

        Entity assortment = assortmentLookup.getEntity();

        FilterValueHolder filterValueHolder = modelLookup.getFilterValue();

        if (Objects.isNull(assortment)) {
            if (filterValueHolder.has(ModelCriteriaModifiers.L_ASSORTMENT_ID)) {
                filterValueHolder.remove(ModelCriteriaModifiers.L_ASSORTMENT_ID);
            }
        } else {
            filterValueHolder.put(ModelCriteriaModifiers.L_ASSORTMENT_ID, assortment.getId());
        }

        modelLookup.setFilterValue(filterValueHolder);
    }

    private DataDefinition getProductDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

}
