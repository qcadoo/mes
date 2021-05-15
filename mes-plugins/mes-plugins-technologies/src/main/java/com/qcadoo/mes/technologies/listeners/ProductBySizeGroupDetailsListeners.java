package com.qcadoo.mes.technologies.listeners;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.ProductBySizeGroupFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductBySizeGroupDetailsListeners {

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private UnitService unitService;

    public void onProductChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OperationProductInComponentFields.PRODUCT);
        Entity product = productLookup.getEntity();
        if (Objects.nonNull(product)) {

            FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
            Entity productBySizeGroup = form.getPersistedEntityWithIncludedFormValues();
            Entity opic = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.OPERATION_PRODUCT_IN_COMPONENT);
            FieldComponent givenUnitField = (FieldComponent) view.getComponentByReference(ProductBySizeGroupFields.GIVEN_UNIT);
            FieldComponent unitField = (FieldComponent) view.getComponentByReference(ProductBySizeGroupFields.UNIT);
            unitField.setFieldValue(product.getStringField(ProductFields.UNIT));

            if (!opic.getBooleanField(OperationProductInComponentFields.VARIOUS_QUANTITIES_IN_PRODUCTS_BY_SIZE)) {

                String opicUnit = opic.getStringField(OperationProductInComponentFields.GIVEN_UNIT);
                String productBySizeGroupUnit = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.PRODUCT).getStringField(ProductFields.UNIT);
                FieldComponent givenQuantityField = (FieldComponent) view.getComponentByReference(ProductBySizeGroupFields.GIVEN_QUANTITY);

                if (opicUnit.equals(productBySizeGroupUnit)) {

                    givenUnitField.setFieldValue(productBySizeGroupUnit);
                    givenQuantityField.setFieldValue(
                            BigDecimalUtils.toString(opic.getDecimalField(OperationProductInComponentFields.GIVEN_QUANTITY), 5));
                    calculateQuantity(view, state, args);
                } else {
                    PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(productBySizeGroupUnit,
                            searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions
                                    .belongsTo(UnitConversionItemFieldsB.PRODUCT, productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.PRODUCT))));

                    if (unitConversions.isDefinedFor(opicUnit)) {
                        givenUnitField.setFieldValue(opicUnit);
                        givenQuantityField.setFieldValue(opic.getDecimalField(OperationProductInComponentFields.GIVEN_QUANTITY));
                        calculateQuantity(view, state, args);

                    } else {
                        productBySizeGroup.addError(
                                productBySizeGroup.getDataDefinition().getField(ProductBySizeGroupFields.GIVEN_QUANTITY),
                                "technologies.operationProductInComponent.validate.error.missingUnitConversion");

                        productBySizeGroup.setField(ProductBySizeGroupFields.QUANTITY, null);
                        form.setEntity(productBySizeGroup);
                    }

                }
            } else {
                calculateQuantity(view, state, args);
            }


        }
    }

    public void onGivenQuantityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        calculateQuantity(view, state, args);
    }

    public void onGivenUnitChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        calculateQuantity(view, state, args);
    }

    private void calculateQuantity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity productBySizeGroup = form.getPersistedEntityWithIncludedFormValues();

        calculateQuantity(view, productBySizeGroup);

        form.setEntity(productBySizeGroup);
    }

    private void calculateQuantity(final ViewDefinitionState view, final Entity productBySizeGroup) {
        Entity product = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.PRODUCT);
        String givenUnit = productBySizeGroup.getStringField(ProductBySizeGroupFields.GIVEN_UNIT);

        FieldComponent unitField = (FieldComponent) view.getComponentByReference(ProductBySizeGroupFields.UNIT);
        FieldComponent givenQuantityField = (FieldComponent) view
                .getComponentByReference(ProductBySizeGroupFields.GIVEN_QUANTITY);

        Either<Exception, Optional<BigDecimal>> maybeQuantity = BigDecimalUtils.tryParse(
                (String) givenQuantityField.getFieldValue(), view.getLocale());

        if (maybeQuantity.isRight()) {
            if (maybeQuantity.getRight().isPresent()) {
                BigDecimal givenQuantity = maybeQuantity.getRight().get();

                if (Objects.nonNull(product)) {
                    String baseUnit = product.getStringField(ProductFields.UNIT);

                    if (baseUnit.equals(givenUnit)) {
                        productBySizeGroup.setField(ProductBySizeGroupFields.QUANTITY, givenQuantity);
                    } else {
                        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(givenUnit,
                                searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions.belongsTo(
                                        UnitConversionItemFieldsB.PRODUCT, product)));

                        if (unitConversions.isDefinedFor(baseUnit)) {
                            BigDecimal convertedQuantity = unitConversions.convertTo(givenQuantity, baseUnit);

                            productBySizeGroup.setField(ProductBySizeGroupFields.QUANTITY, convertedQuantity);
                        } else {
                            productBySizeGroup.addError(
                                    productBySizeGroup.getDataDefinition().getField(ProductBySizeGroupFields.GIVEN_QUANTITY),
                                    "technologies.operationProductInComponent.validate.error.missingUnitConversion");

                            productBySizeGroup.setField(ProductBySizeGroupFields.QUANTITY, null);
                        }
                    }
                } else {
                    productBySizeGroup.setField(ProductBySizeGroupFields.QUANTITY, givenQuantity);
                    productBySizeGroup.setField(ProductBySizeGroupFields.UNIT, givenUnit);

                    unitField.setFieldValue(givenUnit);
                    unitField.requestComponentUpdateState();
                }
            } else {
                productBySizeGroup.setField(ProductBySizeGroupFields.QUANTITY, null);
            }
        } else {
            productBySizeGroup.setField(ProductBySizeGroupFields.QUANTITY, null);
        }
    }
}
