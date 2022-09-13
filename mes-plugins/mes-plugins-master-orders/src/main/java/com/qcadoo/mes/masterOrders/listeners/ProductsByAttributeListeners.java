package com.qcadoo.mes.masterOrders.listeners;

import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.ProductAttributeValueFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.ProductsByAttributeEntryHelperFields;
import com.qcadoo.mes.masterOrders.constants.ProductsByAttributeHelperFields;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProductsByAttributeListeners {

    private static final String L_GENERATED = "generated";

    private static final String L_UNIT = "unit";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductStructureTreeService productStructureTreeService;

    public void changeProductFamily(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(ProductsByAttributeHelperFields.PRODUCT);

        Entity product = productLookup.getEntity();

        if (Objects.nonNull(product)) {
            setUnitField(view, product);
            clearProductsList(view);
            prepareProductsList(view, product);
        } else {
            setUnitField(view, null);
            clearProductsList(view);
        }
    }

    private void setUnitField(final ViewDefinitionState view, final Entity product) {
        FieldComponent unitField = (FieldComponent) view.getComponentByReference(L_UNIT);

        String unit = null;

        if (Objects.nonNull(product)) {
            unit = product.getStringField(ProductFields.UNIT);
        }

        unitField.setFieldValue(unit);
        unitField.requestComponentUpdateState();
    }

    private void clearProductsList(final ViewDefinitionState view) {
        FormComponent productsByAttributeHelperForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity productsByAttributeHelper = getProductsByAttributeHelperDD().get(productsByAttributeHelperForm.getEntityId());

        for (Entity productsByAttributeEntryHelper : productsByAttributeHelper.getHasManyField(ProductsByAttributeHelperFields.PRODUCTS_BY_ATTRIBUTE_ENTRY_HELPERS)) {
            productsByAttributeEntryHelper.getDataDefinition().delete(productsByAttributeEntryHelper.getId());
        }
    }

    private void prepareProductsList(final ViewDefinitionState view, final Entity product) {
        FormComponent productsByAttributeHelperForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent orderedQuantityField = (FieldComponent) view.getComponentByReference(ProductsByAttributeHelperFields.ORDERED_QUANTITY);
        FieldComponent commentsField = (FieldComponent) view.getComponentByReference(ProductsByAttributeHelperFields.COMMENTS);

        List<Entity> productAttributeValues = product.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES);

        for (Entity productAttributeValue : productAttributeValues) {
            Entity attribute = productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE);
            Entity attributeValue = productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE);
            String value = productAttributeValue.getStringField(ProductAttributeValueFields.VALUE);

            Entity productsByAttributeEntryHelper = getProductsByAttributeEntryHelperDD().create();

            productsByAttributeEntryHelper.setField(ProductsByAttributeEntryHelperFields.PRODUCTS_BY_ATTRIBUTE_HELPER, productsByAttributeHelperForm.getEntityId());
            productsByAttributeEntryHelper.setField(ProductsByAttributeEntryHelperFields.PRODUCT, product);
            productsByAttributeEntryHelper.setField(ProductsByAttributeEntryHelperFields.ATTRIBUTE, attribute);
            productsByAttributeEntryHelper.setField(ProductsByAttributeEntryHelperFields.ATTRIBUTE_VALUE, attributeValue);
            productsByAttributeEntryHelper.setField(ProductsByAttributeEntryHelperFields.VALUE, value);

            productsByAttributeEntryHelper.getDataDefinition().save(productsByAttributeEntryHelper);
        }

        Entity productsByAttributeHelper = getProductsByAttributeHelperDD().get(productsByAttributeHelperForm.getEntityId());

        productsByAttributeHelper.setField(ProductsByAttributeHelperFields.PRODUCT, product);

        productsByAttributeHelper = productsByAttributeHelper.getDataDefinition().save(productsByAttributeHelper);

        productsByAttributeHelperForm.setEntity(productsByAttributeHelper);
    }

    public void addPositionsToOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent productsByAttributeHelperForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent orderedQuantityField = (FieldComponent) view.getComponentByReference(ProductsByAttributeHelperFields.ORDERED_QUANTITY);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference(L_GENERATED);

        Entity productsByAttributeHelper = productsByAttributeHelperForm.getPersistedEntityWithIncludedFormValues();

        BigDecimal orderedQuantity;

        String orderedQuantityString = (String) orderedQuantityField.getFieldValue();

        if (StringUtils.isEmpty(orderedQuantityString)) {
            orderedQuantityField.addMessage("qcadooView.validate.field.error.missing", ComponentState.MessageType.FAILURE);

            return;
        } else {
            Either<Exception, com.google.common.base.Optional<BigDecimal>> tryParseOrderedQuantity = BigDecimalUtils.tryParseAndIgnoreSeparator(
                    orderedQuantityString, LocaleContextHolder.getLocale());

            if (tryParseOrderedQuantity.isLeft()) {
                orderedQuantityField.addMessage("qcadooView.validate.field.error.invalidNumericFormat", ComponentState.MessageType.FAILURE);

                return;
            } else {
                orderedQuantity = tryParseOrderedQuantity.getRight().get();
            }
        }

        String comments = productsByAttributeHelper.getStringField(ProductsByAttributeHelperFields.COMMENTS);

        productsByAttributeHelper = getProductsByAttributeHelperDD().get(productsByAttributeHelperForm.getEntityId());

        Entity masterOrder = productsByAttributeHelper.getBelongsToField(ProductsByAttributeHelperFields.MASTER_ORDER);
        Entity product = productsByAttributeHelper.getBelongsToField(ProductsByAttributeHelperFields.PRODUCT);
        List<Entity> productsByAttributeEntryHelpers = productsByAttributeHelper.getHasManyField(ProductsByAttributeHelperFields.PRODUCTS_BY_ATTRIBUTE_ENTRY_HELPERS);

        List<Entity> children = product.getHasManyField(ProductFields.CHILDREN);

        Optional<Entity> mayBeProduct = findMatchingProduct(children, productsByAttributeEntryHelpers);

        if (mayBeProduct.isPresent()) {

            product = mayBeProduct.get();

            Entity masterOrderProduct = createMasterOrderPosition(masterOrder, product, orderedQuantity, comments);

            if (masterOrderProduct.isValid()) {
                view.addMessage("masterOrders.productsByAttribute.addPositionsToOrder.success", ComponentState.MessageType.SUCCESS);

                generatedCheckBox.setChecked(true);
            } else {
                view.addMessage("masterOrders.productsByAttribute.addPositionsToOrder.error", ComponentState.MessageType.FAILURE);
            }
        } else {
            view.addMessage("masterOrders.productsByAttribute.addPositionsToOrder.failure", ComponentState.MessageType.FAILURE);
        }
    }

    private Entity createMasterOrderPosition(final Entity masterOrder, final Entity product, final BigDecimal masterOrderQuantity, final String comments) {
        Entity masterOrderProduct = getMasterOrderProductDD().create();

        masterOrderProduct.setField(MasterOrderProductFields.MASTER_ORDER, masterOrder);
        masterOrderProduct.setField(MasterOrderProductFields.PRODUCT, product);
        masterOrderProduct.setField(MasterOrderProductFields.TECHNOLOGY, getTechnology(product));
        masterOrderProduct.setField(MasterOrderProductFields.MASTER_ORDER_QUANTITY, masterOrderQuantity);
        masterOrderProduct.setField(MasterOrderProductFields.COMMENTS, comments);

        return masterOrderProduct.getDataDefinition().save(masterOrderProduct);
    }

    private Entity getTechnology(final Entity product) {
        Entity technology = productStructureTreeService.findTechnologyForProduct(product);

        if (Objects.isNull(technology)) {
            Entity parent = product.getBelongsToField(ProductFields.PARENT);

            technology = productStructureTreeService.findTechnologyForProduct(parent);
        }

        return technology;
    }

    private Optional<Entity> findMatchingProduct(final List<Entity> children, final List<Entity> productsByAttributeEntryHelpers) {
        Entity product = null;

        int isFound = 0;

        for (Entity child : children) {
            List<Entity> productAttributeValues = child.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES);

            int matchingAttributes = 0;

            for (Entity productsByAttributeEntryHelper : productsByAttributeEntryHelpers) {
                Entity attribute = productsByAttributeEntryHelper.getBelongsToField(ProductsByAttributeEntryHelperFields.ATTRIBUTE);
                String value = productsByAttributeEntryHelper.getStringField(ProductsByAttributeEntryHelperFields.VALUE);

                if (productAttributeValues.stream().anyMatch(productAttributeValue ->
                        productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getId().equals(attribute.getId())
                                && productAttributeValue.getStringField(ProductAttributeValueFields.VALUE).equals(value))) {
                    matchingAttributes++;
                }
            }

            if (matchingAttributes == productsByAttributeEntryHelpers.size()) {
                isFound++;

                product = child;
            }
        }

        if (isFound == 1) {
            return Optional.of(product);
        } else {
            return Optional.empty();
        }
    }

    private DataDefinition getProductsByAttributeHelperDD() {
        return dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_PRODUCTS_BY_ATTRIBUTE_HELPER);
    }

    private DataDefinition getProductsByAttributeEntryHelperDD() {
        return dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_PRODUCTS_BY_ATTRIBUTE_ENTRY_HELPER);
    }

    private DataDefinition getMasterOrderProductDD() {
        return dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER_PRODUCT);
    }

}
