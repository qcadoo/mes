package com.qcadoo.mes.orders.util;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AdditionalUnitService {

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private NumberService numberService;

    public BigDecimal getQuantityAfterConversion(final Entity order, String givenUnit, BigDecimal quantity, String baseUnit) {
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);
        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(baseUnit,
                searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions.belongsTo(
                        UnitConversionItemFieldsB.PRODUCT, product)));
        if (quantity == null) {
            return BigDecimal.ZERO;
        }
        if (!baseUnit.equals(givenUnit)) {
            if (unitConversions.isDefinedFor(givenUnit)) {
                return unitConversions.convertTo(quantity, givenUnit);
            }
        }
        return quantity;

    }

    public void setQuantityFieldForAdditionalUnit(final ViewDefinitionState view, final Entity order) {
        FieldComponent quantityForAdditionalUnitField = (FieldComponent) view
                .getComponentByReference(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT);
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);
        if (!isValidDecimalField(view, Lists.newArrayList(OrderFields.PLANNED_QUANTITY))) {
            return;
        }
        BigDecimal quantityForAdditionalUnit = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
        if (product != null) {
            quantityForAdditionalUnit = getQuantityAfterConversion(order, getAdditionalUnit(product),
                    order.getDecimalField(OrderFields.PLANNED_QUANTITY), product.getStringField(ProductFields.UNIT));
        }
        if (quantityForAdditionalUnit.compareTo(BigDecimal.ZERO) == 0) {
            FieldComponent quantityForUnitField = (FieldComponent) view.getComponentByReference(OrderFields.PLANNED_QUANTITY);
            quantityForUnitField.setFieldValue(numberService.formatWithMinimumFractionDigits(quantityForAdditionalUnit, 0));
            quantityForUnitField.requestComponentUpdateState();
        }
        quantityForAdditionalUnitField.setFieldValue(numberService.formatWithMinimumFractionDigits(quantityForAdditionalUnit, 0));
        quantityForAdditionalUnitField.requestComponentUpdateState();
    }

    public void setQuantityForUnit(final ViewDefinitionState view, final Entity order) {
        if (!isValidDecimalField(view, Lists.newArrayList(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT))) {
            return;
        }
        FieldComponent quantityForUnitField = (FieldComponent) view.getComponentByReference(OrderFields.PLANNED_QUANTITY);
        Entity product = order.getBelongsToField(BasicConstants.MODEL_PRODUCT);
        BigDecimal unitQuantity = getQuantityAfterConversion(order, product.getStringField(ProductFields.UNIT),
                order.getDecimalField(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT), getAdditionalUnit(product));
        quantityForUnitField.setFieldValue(numberService.format(unitQuantity));
        quantityForUnitField.requestComponentUpdateState();
    }

    public void setAdditionalUnitField(final ViewDefinitionState state) {
        Entity order = ((FormComponent) state.getComponentByReference(QcadooViewConstants.L_FORM)).getEntity();
        Entity product = order.getBelongsToField(BasicConstants.MODEL_PRODUCT);
        FieldComponent additionalUnitField = (FieldComponent) state.getComponentByReference(OrderFields.UNIT_FOR_ADDITIONAL_UNIT);
        String additionalUnit = getAdditionalUnit(product);
        additionalUnitField.setFieldValue(additionalUnit);
        additionalUnitField.requestComponentUpdateState();
    }

    public String getAdditionalUnit(final Entity product) {
        String additionalUnit = "-";
        if (product != null) {
            additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
            if (additionalUnit == null) {
                additionalUnit = product.getStringField(ProductFields.UNIT);
            }
        }
        return additionalUnit;
    }

    public boolean isValidDecimalField(final ViewDefinitionState view, final List<String> fileds) {
        boolean isValid = true;

        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity entity = orderForm.getEntity();

        for (String field : fileds) {
            try {
                entity.getDecimalField(field);
            } catch (IllegalArgumentException e) {
                FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(field);
                fieldComponent.addMessage("qcadooView.validate.field.error.invalidNumericFormat", ComponentState.MessageType.FAILURE);

                isValid = false;
            }
        }

        return isValid;
    }

    public boolean isValidDecimalFieldWithoutMsg(final ViewDefinitionState view, final List<String> fields) {
        boolean isValid = true;

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity entity = form.getEntity();

        for (String field : fields) {
            try {
                entity.getDecimalField(field);
            } catch (IllegalArgumentException e) {
                isValid = false;
            }
        }

        return isValid;
    }

}
