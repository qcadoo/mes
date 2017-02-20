package com.qcadoo.mes.orders.util;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.constants.UnitConversionItemFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class AdditionalUnitService {

    public double getConverter(final Entity order) {
        String additionalUnit = order.getBelongsToField(BasicConstants.MODEL_PRODUCT)
                .getStringField(ProductFields.ADDITIONAL_UNIT);
        double converter = 1;
        EntityList entityList = order.getBelongsToField(BasicConstants.MODEL_PRODUCT)
                .getHasManyField(ProductFields.CONVERSION_ITEMS);
        for (Entity entity : entityList) {
            String unitTo = entity.getStringField(UnitConversionItemFields.UNIT_TO);
            if (unitTo.equals(additionalUnit)) {
                converter = entity.getDecimalField(UnitConversionItemFields.QUANTITY_TO).doubleValue()
                        / entity.getDecimalField(UnitConversionItemFields.QUANTITY_FROM).doubleValue();
                break;
            }
        }
        return converter;
    }

    public void setQuantityFieldForAdditionalUnit(final ViewDefinitionState view, final Entity order) {
        FieldComponent quantityForAdditionalUnitField = (FieldComponent) view
                .getComponentByReference(OrderFields.PLANED_QUANTITY_FOR_ADDITIONAL_UNIT);
        double quantityForAdditionalUnit = order.getDecimalField(OrderFields.PLANNED_QUANTITY).doubleValue()
                * getConverter(order);
        quantityForAdditionalUnitField.setFieldValue(quantityForAdditionalUnit);
        quantityForAdditionalUnitField.requestComponentUpdateState();
    }

    public void setQuantityForUnit(final ViewDefinitionState view, final Entity order) {
        FieldComponent quantityForUnitField = (FieldComponent) view.getComponentByReference(OrderFields.PLANNED_QUANTITY);
        double unitQuantity = order.getDecimalField(OrderFields.PLANED_QUANTITY_FOR_ADDITIONAL_UNIT).doubleValue()
                / getConverter(order);
        quantityForUnitField.setFieldValue(unitQuantity);
        quantityForUnitField.requestComponentUpdateState();
    }

    public void setAdditionalUnitField(final ViewDefinitionState state) {
        Entity order = ((FormComponent) state.getComponentByReference("form")).getEntity();
        Entity product = order.getBelongsToField(BasicConstants.MODEL_PRODUCT);
        String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
        FieldComponent additionalUnitField = (FieldComponent) state.getComponentByReference(OrderFields.UNIT_FOR_ADDITIONAL_UNIT);
        if (additionalUnit == null) {
            additionalUnit = product.getStringField(ProductFields.UNIT);
        }
        additionalUnitField.setFieldValue(additionalUnit);
        additionalUnitField.requestComponentUpdateState();
    }

}