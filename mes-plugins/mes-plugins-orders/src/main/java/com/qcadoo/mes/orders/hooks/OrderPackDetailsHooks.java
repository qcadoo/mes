package com.qcadoo.mes.orders.hooks;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.OrderPackService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderPackFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderPackDetailsHooks {

    @Autowired
    private NumberService numberService;

    @Autowired
    private OrderPackService orderPackService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OrderPackFields.ORDER);
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity order = orderLookup.getEntity();
        if (order != null) {
            FieldComponent orderQuantity = (FieldComponent) view.getComponentByReference("orderQuantity");
            orderQuantity.setFieldValue(numberService.format(order.getField(OrderFields.PLANNED_QUANTITY)));
            FieldComponent quantityField = (FieldComponent) view.getComponentByReference(OrderPackFields.QUANTITY);
            BigDecimal sumQuantityOrderPacks = orderPackService.getSumQuantityOrderPacksForOrderWithoutPack(order,
                    form.getEntityId());
            if (StringUtils.isNumeric((String) quantityField.getFieldValue())) {
                sumQuantityOrderPacks = sumQuantityOrderPacks.add(new BigDecimal((String) quantityField.getFieldValue()),
                        numberService.getMathContext());
            }
            FieldComponent sumQuantityOrderPacksField = (FieldComponent) view.getComponentByReference("sumQuantityOrderPacks");
            sumQuantityOrderPacksField.setFieldValue(numberService.format(sumQuantityOrderPacks));
            String unit = order.getBelongsToField(OrderFields.PRODUCT).getStringField(ProductFields.UNIT);
            FieldComponent orderQuantityUnit = (FieldComponent) view.getComponentByReference("orderQuantityUnit");
            orderQuantityUnit.setFieldValue(unit);
            orderQuantityUnit.requestComponentUpdateState();
            FieldComponent sumQuantityOrderPacksUnit = (FieldComponent) view.getComponentByReference("sumQuantityOrderPacksUnit");
            sumQuantityOrderPacksUnit.setFieldValue(unit);
            sumQuantityOrderPacksUnit.requestComponentUpdateState();
            FieldComponent quantityUnit = (FieldComponent) view.getComponentByReference("quantityUnit");
            quantityUnit.setFieldValue(unit);
            quantityUnit.requestComponentUpdateState();
        }

    }
}
