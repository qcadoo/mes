package com.qcadoo.mes.materialFlowResources.hooks;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.CalculationQuantityService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ResourceDetailsHooks {

    public static final String L_FORM = "form";

    @Autowired
    private NumberService numberService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private CalculationQuantityService calculationQuantityService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity resource = form.getPersistedEntityWithIncludedFormValues();
        LookupComponent storageLocationLookup = (LookupComponent) view.getComponentByReference(ResourceFields.STORAGE_LOCATION);
        FilterValueHolder filter = storageLocationLookup.getFilterValue();
        Entity product = resource.getBelongsToField(ResourceFields.PRODUCT);
        Entity warehouse = resource.getBelongsToField(ResourceFields.LOCATION);
        filter.put("product", product.getId());
        filter.put("location", warehouse.getId());
        storageLocationLookup.setFilterValue(filter);

        fillUnitField(view, resource);
        togglePriceFields(view, resource);
    }

    public void onConversionChange(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        FieldComponent conversionField = (FieldComponent) viewDefinitionState.getComponentByReference(ResourceFields.CONVERSION);
        FieldComponent quantityInAdditionalUnitField = (FieldComponent) viewDefinitionState
                .getComponentByReference(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT);
        FieldComponent additionalUnitField = (FieldComponent) viewDefinitionState
                .getComponentByReference(ResourceFields.GIVEN_UNIT);

        Either<Exception, Optional<BigDecimal>> maybeConversion = BigDecimalUtils.tryParseAndIgnoreSeparator(
                (String) conversionField.getFieldValue(), viewDefinitionState.getLocale());
        if (maybeConversion.isRight() && maybeConversion.getRight().isPresent()) {
            FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);
            Entity resource = form.getPersistedEntityWithIncludedFormValues();

            BigDecimal newAdditionalQuantity = calculationQuantityService.calculateAdditionalQuantity(
                    resource.getDecimalField(ResourceFields.QUANTITY), maybeConversion.getRight().get(),
                    (String) additionalUnitField.getFieldValue());

            String quantityInAdditionalUnitFormatted = numberService.format(newAdditionalQuantity);
            quantityInAdditionalUnitField.setFieldValue(quantityInAdditionalUnitFormatted);

        } else {
            quantityInAdditionalUnitField.setFieldValue(null);
        }
    }

    public void onQuantityChange(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        FieldComponent quantityField = (FieldComponent) viewDefinitionState.getComponentByReference(ResourceFields.QUANTITY);
        FieldComponent quantityInAdditionalUnitField = (FieldComponent) viewDefinitionState
                .getComponentByReference(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT);
        FieldComponent additionalUnitField = (FieldComponent) viewDefinitionState
                .getComponentByReference(ResourceFields.GIVEN_UNIT);

        Either<Exception, Optional<BigDecimal>> maybeQuantity = BigDecimalUtils.tryParseAndIgnoreSeparator(
                (String) quantityField.getFieldValue(), viewDefinitionState.getLocale());
        if (maybeQuantity.isRight() && maybeQuantity.getRight().isPresent()) {
            FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);
            Entity resource = form.getEntity();

            BigDecimal newAdditionalQuantity = calculationQuantityService.calculateAdditionalQuantity(maybeQuantity.getRight()
                    .get(), resource.getDecimalField(ResourceFields.CONVERSION), (String) additionalUnitField.getFieldValue());

            String quantityInAdditionalUnitFormatted = numberService.format(newAdditionalQuantity);
            quantityInAdditionalUnitField.setFieldValue(quantityInAdditionalUnitFormatted);

        } else {
            quantityInAdditionalUnitField.setFieldValue(null);
        }
    }

    public void onQuantityInAdditionalUnitChange(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent quantityField = (FieldComponent) viewDefinitionState.getComponentByReference(ResourceFields.QUANTITY);
        FieldComponent quantityInAdditionalUnitField = (FieldComponent) viewDefinitionState
                .getComponentByReference(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT);

        Either<Exception, Optional<BigDecimal>> maybeQuantityInAdditionalUnit = BigDecimalUtils.tryParseAndIgnoreSeparator(
                (String) quantityInAdditionalUnitField.getFieldValue(), viewDefinitionState.getLocale());
        if (maybeQuantityInAdditionalUnit.isRight() && maybeQuantityInAdditionalUnit.getRight().isPresent()) {
            FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);
            Entity resource = form.getEntity();
            BigDecimal conversion = resource.getDecimalField(ResourceFields.CONVERSION);
            Entity product = resource.getBelongsToField(ResourceFields.PRODUCT);

            BigDecimal quantity = calculationQuantityService.calculateQuantity(maybeQuantityInAdditionalUnit.getRight().get(),
                    conversion, product.getStringField(ProductFields.UNIT));

            String quantityFormatted = numberService.format(numberService.setScaleWithDefaultMathContext(quantity));
            quantityField.setFieldValue(quantityFormatted);

        } else {
            quantityField.setFieldValue(null);
        }
    }

    private void fillUnitField(ViewDefinitionState view, Entity resource) {
        FieldComponent givenUnitField = (FieldComponent) view.getComponentByReference(ResourceFields.GIVEN_UNIT);
        givenUnitField.setFieldValue(resource.getStringField(ResourceFields.GIVEN_UNIT));
        givenUnitField.requestComponentUpdateState();
    }

    private void togglePriceFields(ViewDefinitionState view, Entity resource) {
        boolean hasCurrentUserRole = securityService.hasCurrentUserRole("ROLE_RESOURCE_PRICE");
        FieldComponent priceField = (FieldComponent) view.getComponentByReference("price");
        priceField.setVisible(hasCurrentUserRole);
        FieldComponent priceCurrencyField = (FieldComponent) view.getComponentByReference("priceCurrency");
        priceCurrencyField.setVisible(hasCurrentUserRole);
    }
}
