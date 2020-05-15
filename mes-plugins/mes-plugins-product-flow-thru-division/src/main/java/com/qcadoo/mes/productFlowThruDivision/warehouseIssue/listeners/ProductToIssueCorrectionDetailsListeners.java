package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.listeners;

import com.google.common.collect.Lists;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.ProductToIssueCorrectionService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.ProductToIssueCorrectionFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductToIssueCorrectionDetailsListeners {

    @Autowired
    private ProductToIssueCorrectionService productToIssueCorrectionService;

    @Autowired
    private NumberService numberService;

    public void correct(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity helper = form.getPersistedEntityWithIncludedFormValues();
        Entity locationTo = helper.getBelongsToField("locationTo");
        if (locationTo == null) {
            FieldComponent locationToField = (FieldComponent) view.getComponentByReference("locationTo");
            locationToField.addMessage(
                    "productFlowThruDivision.productToIssueCorrectionHelperDetails.correction.error.locationToRequired",
                    ComponentState.MessageType.FAILURE);
            return;
        }
        List<Entity> corrections = helper.getHasManyField("corrections");
        List<Entity> savedCorrections = Lists.newArrayList();
        corrections.forEach(correction -> savedCorrections.add(correction.getDataDefinition().save(correction)));
        helper.setField("corrections", savedCorrections);
        form.setEntity(helper);
        if (savedCorrections.stream().anyMatch(correction -> !correction.isValid())) {
            AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference("corrections");
            for (FormComponent formComponent : adl.getFormComponents()) {
                Entity formEntity = formComponent.getEntity();
                formEntity.getErrors().forEach((field, errorMessage) -> {
                    FieldComponent fieldComponent = formComponent.findFieldComponentByName(field);
                    fieldComponent.addMessage(errorMessage);
                });
            }
            view.addMessage("productFlowThruDivision.productToIssueCorrectionHelperDetails.correction.error",
                    ComponentState.MessageType.FAILURE);
        } else {
            try {
                List<Entity> newWarehouseIssues = productToIssueCorrectionService
                        .correctProductsToIssue(helper, savedCorrections);
                helper.setField("corrections", savedCorrections);
                form.setEntity(helper);
                form.setFormEnabled(false);
                view.addMessage(
                        "productFlowThruDivision.productToIssueCorrectionHelperDetails.correction.success",
                        ComponentState.MessageType.SUCCESS,
                        newWarehouseIssues.stream().map(issue -> issue.getStringField(WarehouseIssueFields.NUMBER))
                                .collect(Collectors.joining(", ")));
                afterSuccessfulCreateCorrectionsHooks(view, savedCorrections);

            } catch (IllegalStateException e) {
                view.addMessage(
                        "productFlowThruDivision.productToIssueCorrectionHelperDetails.correction.error.newWarehouseIssue",
                        ComponentState.MessageType.FAILURE);
            }
        }
    }

    public void quantityChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference("corrections");
        List<FormComponent> formComponenets = adl.getFormComponents();
        for (FormComponent formComponent : formComponenets) {
            Entity formEntity = formComponent.getPersistedEntityWithIncludedFormValues();
            FieldComponent quantityField = formComponent.findFieldComponentByName("correctionQuantity");
            BigDecimal conversion = formEntity.getDecimalField(ProductToIssueCorrectionFields.CONVERSION);
            FieldComponent additionalQuantity = formComponent.findFieldComponentByName("correctionQuantityInAdditionalUnit");
            if (quantityField.getUuid().equals(state.getUuid())) {
                Either<Exception, com.google.common.base.Optional<BigDecimal>> maybeQuantity = BigDecimalUtils.tryParse(
                        quantityField.getFieldValue().toString(), LocaleContextHolder.getLocale());
                if (conversion != null && maybeQuantity.isRight() && maybeQuantity.getRight().isPresent()) {
                    BigDecimal quantity = maybeQuantity.getRight().get();
                    BigDecimal newAdditionalQuantity = quantity.multiply(conversion, numberService.getMathContext());
                    newAdditionalQuantity = newAdditionalQuantity.setScale(NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL,
                            RoundingMode.HALF_UP);
                    additionalQuantity.setFieldValue(numberService.formatWithMinimumFractionDigits(newAdditionalQuantity, 0));
                    additionalQuantity.requestComponentUpdateState();
                } else if (maybeQuantity.isLeft()) {
                    quantityField.setFieldValue(additionalQuantity.getFieldValue());
                    quantityField.addMessage("productFlowThruDivision.productsToIssueHelperDetails.error.invalidQuantity",
                            ComponentState.MessageType.FAILURE);
                } else {
                    additionalQuantity.setFieldValue(null);
                }
            }

        }
    }

    public void additionalQuantityChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference("corrections");
        List<FormComponent> formComponenets = adl.getFormComponents();
        for (FormComponent formComponent : formComponenets) {
            FieldComponent additionalQuantityField = formComponent.findFieldComponentByName("correctionQuantityInAdditionalUnit");
            Entity formEntity = formComponent.getPersistedEntityWithIncludedFormValues();
            BigDecimal conversion = formEntity.getDecimalField(ProductToIssueCorrectionFields.CONVERSION);
            FieldComponent quantity = formComponent.findFieldComponentByName("correctionQuantity");
            if (additionalQuantityField.getUuid().equals(state.getUuid())) {
                Either<Exception, com.google.common.base.Optional<BigDecimal>> maybeAdditionalQuantity = BigDecimalUtils
                        .tryParse(additionalQuantityField.getFieldValue().toString(), LocaleContextHolder.getLocale());
                if (conversion != null && maybeAdditionalQuantity.isRight() && maybeAdditionalQuantity.getRight().isPresent()) {
                    BigDecimal additionalQuantity = maybeAdditionalQuantity.getRight().get();
                    BigDecimal newQuantity = additionalQuantity.divide(conversion, numberService.getMathContext());
                    newQuantity = newQuantity
                            .setScale(NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL, RoundingMode.HALF_UP);
                    quantity.setFieldValue(numberService.formatWithMinimumFractionDigits(newQuantity, 0));
                    quantity.requestComponentUpdateState();
                } else if (maybeAdditionalQuantity.isLeft()) {
                    additionalQuantityField.setFieldValue(quantity.getFieldValue());
                    additionalQuantityField.addMessage(
                            "productFlowThruDivision.productsToIssueHelperDetails.error.invalidQuantity",
                            ComponentState.MessageType.FAILURE);
                } else {
                    quantity.setFieldValue(null);
                }
            }
        }
    }

    private void afterSuccessfulCreateCorrectionsHooks(final ViewDefinitionState view, List<Entity> savedCorrections) {
// used in AOP
    }
}
