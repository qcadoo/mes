package com.qcadoo.mes.masterOrders.listeners;

import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.ProductAttributeValueFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.ProductsByAttributeEntryHelperFields;
import com.qcadoo.mes.masterOrders.constants.ProductsByAttributeHelperFields;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.model.api.*;
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
import java.util.stream.Collectors;

@Service
public class ProductsByAttributeListeners {

    private static final String L_GENERATED = "generated";

    private static final String L_UNIT = "unit";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductStructureTreeService productStructureTreeService;

    public void onProductFamilyChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        setUnitField(view);
        clearProductsList(view);
        prepareProductsList(view);
        onFormChange(view);
    }

    private void setUnitField(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(ProductsByAttributeHelperFields.PRODUCT);
        FieldComponent unitField = (FieldComponent) view.getComponentByReference(L_UNIT);

        Entity product = productLookup.getEntity();

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

        List<Entity> productsByAttributeEntryHelpers = productsByAttributeHelper.getHasManyField(ProductsByAttributeHelperFields.PRODUCTS_BY_ATTRIBUTE_ENTRY_HELPERS);

        for (Entity productsByAttributeEntryHelper : productsByAttributeEntryHelpers) {
            productsByAttributeEntryHelper.getDataDefinition().delete(productsByAttributeEntryHelper.getId());
        }
    }

    private void prepareProductsList(final ViewDefinitionState view) {
        FormComponent productsByAttributeHelperForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(ProductsByAttributeHelperFields.PRODUCT);

        Entity product = productLookup.getEntity();

        if (Objects.nonNull(product)) {
            List<Entity> productAttributeValues = product.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES);

            for (Entity productAttributeValue : productAttributeValues) {
                Entity attribute = productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE);
                Entity attributeValue = productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE);
                String value = productAttributeValue.getStringField(ProductAttributeValueFields.VALUE);

                createProductsByAttributeHelper(productsByAttributeHelperForm.getEntityId(), product, attribute, attributeValue, value);
            }
        }
    }

    private void createProductsByAttributeHelper(final Long productsByAttributeHelperId, final Entity product, final Entity attribute, final Entity attributeValue, final String value) {
        Entity productsByAttributeEntryHelper = getProductsByAttributeEntryHelperDD().create();

        productsByAttributeEntryHelper.setField(ProductsByAttributeEntryHelperFields.PRODUCTS_BY_ATTRIBUTE_HELPER, productsByAttributeHelperId);
        productsByAttributeEntryHelper.setField(ProductsByAttributeEntryHelperFields.PRODUCT, product);
        productsByAttributeEntryHelper.setField(ProductsByAttributeEntryHelperFields.ATTRIBUTE, attribute);
        productsByAttributeEntryHelper.setField(ProductsByAttributeEntryHelperFields.ATTRIBUTE_VALUE, attributeValue);
        productsByAttributeEntryHelper.setField(ProductsByAttributeEntryHelperFields.VALUE, value);

        productsByAttributeEntryHelper.getDataDefinition().save(productsByAttributeEntryHelper);
    }

    public void onQuantityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        onFormChange(view);
    }

    public void onCommentsChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        onFormChange(view);
    }

    private void onFormChange(final ViewDefinitionState view) {
        FormComponent productsByAttributeHelperForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(ProductsByAttributeHelperFields.PRODUCT);
        FieldComponent orderedQuantityField = (FieldComponent) view.getComponentByReference(ProductsByAttributeHelperFields.ORDERED_QUANTITY);
        FieldComponent commentsField = (FieldComponent) view.getComponentByReference(ProductsByAttributeHelperFields.COMMENTS);

        Entity product = productLookup.getEntity();
        Object orderedQuantity = orderedQuantityField.getFieldValue();
        Object comments = commentsField.getFieldValue();

        Either<Exception, com.google.common.base.Optional<BigDecimal>> eitherOrderedQuantity = BigDecimalUtils.tryParseAndIgnoreSeparator(
                (String) orderedQuantity, LocaleContextHolder.getLocale());

        if (Objects.nonNull(product)) {
            Entity productsByAttributeHelper = getProductsByAttributeHelperDD().get(productsByAttributeHelperForm.getEntityId());

            productsByAttributeHelper.setField(ProductsByAttributeHelperFields.PRODUCT, product);
            if (eitherOrderedQuantity.isRight() && eitherOrderedQuantity.getRight().isPresent()) {
                productsByAttributeHelper.setField(ProductsByAttributeHelperFields.ORDERED_QUANTITY, eitherOrderedQuantity.getRight().get());
            } else {
                productsByAttributeHelper.setField(ProductsByAttributeHelperFields.ORDERED_QUANTITY, orderedQuantity);
            }
            productsByAttributeHelper.setField(ProductsByAttributeHelperFields.COMMENTS, comments);

            productsByAttributeHelper = productsByAttributeHelper.getDataDefinition().save(productsByAttributeHelper);

            productsByAttributeHelperForm.setEntity(productsByAttributeHelper);
        }
    }

    public void addPositionsToOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent productsByAttributeHelperForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(ProductsByAttributeHelperFields.PRODUCT);
        FieldComponent orderedQuantityField = (FieldComponent) view.getComponentByReference(ProductsByAttributeHelperFields.ORDERED_QUANTITY);
        FieldComponent commentsField = (FieldComponent) view.getComponentByReference(ProductsByAttributeHelperFields.COMMENTS);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference(L_GENERATED);

        if (Objects.isNull(productLookup.getEntity())) {
            productLookup.addMessage("qcadooView.validate.field.error.missing", ComponentState.MessageType.FAILURE);

            return;
        }

        BigDecimal orderedQuantity = getBigDecimal(orderedQuantityField);

        if (Objects.isNull(orderedQuantity)) {
            return;
        }

        String comments = (String) commentsField.getFieldValue();

        Entity productsByAttributeHelper = getProductsByAttributeHelperDD().get(productsByAttributeHelperForm.getEntityId());

        Entity masterOrder = productsByAttributeHelper.getBelongsToField(ProductsByAttributeHelperFields.MASTER_ORDER);
        Entity product = productsByAttributeHelper.getBelongsToField(ProductsByAttributeHelperFields.PRODUCT);
        List<Entity> productsByAttributeEntryHelpers = productsByAttributeHelper.getHasManyField(ProductsByAttributeHelperFields.PRODUCTS_BY_ATTRIBUTE_ENTRY_HELPERS);

        if (productsByAttributeEntryHelpers.isEmpty()) {
            view.addMessage("masterOrders.productsByAttribute.addPositionsToOrder.empty", ComponentState.MessageType.INFO);

            return;
        } else {
            if (productsByAttributeEntryHelpers.stream().allMatch(productsByAttributeEntryHelper -> StringUtils.isEmpty(productsByAttributeEntryHelper.getStringField(ProductsByAttributeEntryHelperFields.VALUE)))) {
                view.addMessage("masterOrders.productsByAttribute.addPositionsToOrder.info", ComponentState.MessageType.INFO);

                return;
            }
        }

        if (Objects.nonNull(masterOrder) && Objects.nonNull(product)) {
            List<Entity> children = product.getHasManyField(ProductFields.CHILDREN);

            Optional<Entity> mayBeProduct = findMatchingProduct(children, productsByAttributeEntryHelpers);

            if (mayBeProduct.isPresent()) {
                product = mayBeProduct.get();

                Entity masterOrderProduct = createMasterOrderPosition(masterOrder, product, orderedQuantity, comments);

                if (masterOrderProduct.isValid()) {
                    view.addMessage("masterOrders.productsByAttribute.addPositionsToOrder.success", ComponentState.MessageType.SUCCESS);

                    generatedCheckBox.setChecked(true);
                } else {
                    masterOrderProduct.getGlobalErrors().stream().filter(error ->
                            !error.getMessage().equals("qcadooView.validate.global.error.custom")).forEach(error ->
                            view.addMessage(error.getMessage(), ComponentState.MessageType.FAILURE, error.getVars())
                    );

                    masterOrderProduct.getErrors().values().forEach(error ->
                            view.addMessage(error.getMessage(), ComponentState.MessageType.FAILURE, error.getVars())
                    );

                    view.addMessage("masterOrders.productsByAttribute.addPositionsToOrder.error", ComponentState.MessageType.FAILURE);
                }
            } else {
                view.addMessage("masterOrders.productsByAttribute.addPositionsToOrder.failure", ComponentState.MessageType.FAILURE);
            }
        }
    }

    private BigDecimal getBigDecimal(final FieldComponent orderedQuantityField) {
        BigDecimal orderedQuantity;

        String orderedQuantityString = (String) orderedQuantityField.getFieldValue();

        Either<Exception, com.google.common.base.Optional<BigDecimal>> eitherOrderedQuantity = BigDecimalUtils.tryParseAndIgnoreSeparator(
                orderedQuantityString, LocaleContextHolder.getLocale());

        if (eitherOrderedQuantity.isRight()) {
            if (eitherOrderedQuantity.getRight().isPresent()) {
                orderedQuantity = eitherOrderedQuantity.getRight().get();

                int scale = 5;
                int valueScale = orderedQuantity.scale();

                if (valueScale > scale) {
                    orderedQuantityField.addMessage("qcadooView.validate.field.error.invalidScale.max", ComponentState.MessageType.FAILURE, String.valueOf(scale));

                    return null;
                }

                if (BigDecimal.ZERO.compareTo(orderedQuantity) > 0) {
                    orderedQuantityField.addMessage("qcadooView.validate.field.error.outOfRange.toSmall", ComponentState.MessageType.FAILURE);

                    return null;
                }
            } else {
                orderedQuantityField.addMessage("qcadooView.validate.field.error.missing", ComponentState.MessageType.FAILURE);

                return null;
            }
        } else {
            orderedQuantityField.addMessage("qcadooView.validate.field.error.invalidNumericFormat", ComponentState.MessageType.FAILURE);

            return null;
        }

        return orderedQuantity;
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

        List<Entity> filteredProductsByAttributeEntryHelpers = productsByAttributeEntryHelpers.stream().filter(productsByAttributeEntryHelper
                -> StringUtils.isNotEmpty(productsByAttributeEntryHelper.getStringField(ProductsByAttributeEntryHelperFields.VALUE))).collect(Collectors.toList());

        int isFound = 0;

        for (Entity child : children) {
            List<Entity> productAttributeValues = child.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES);

            int matchingAttributes = 0;

            for (Entity productsByAttributeEntryHelper : filteredProductsByAttributeEntryHelpers) {
                Entity attribute = productsByAttributeEntryHelper.getBelongsToField(ProductsByAttributeEntryHelperFields.ATTRIBUTE);
                String value = productsByAttributeEntryHelper.getStringField(ProductsByAttributeEntryHelperFields.VALUE);

                if (productAttributeValues.stream().anyMatch(productAttributeValue ->
                        productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getId().equals(attribute.getId())
                                && productAttributeValue.getStringField(ProductAttributeValueFields.VALUE).equals(value))) {
                    matchingAttributes++;
                }
            }

            if (matchingAttributes == filteredProductsByAttributeEntryHelpers.size()) {
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
