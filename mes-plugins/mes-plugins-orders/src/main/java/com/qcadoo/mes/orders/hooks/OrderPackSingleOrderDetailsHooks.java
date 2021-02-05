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
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class OrderPackSingleOrderDetailsHooks {

    @Autowired
    private NumberService numberService;

    @Autowired
    private OrderPackService orderPackService;

    public final void onBeforeRender(final ViewDefinitionState view) throws JSONException {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OrderPackFields.ORDER);
        if (Objects.isNull(form.getEntityId())) {
            JSONObject context = view.getJsonContext();
            Long orderId = context.getLong("window.mainTab.orderPack.order");
            orderLookup.setFieldValue(orderId);
            orderLookup.requestComponentUpdateState();
        }

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
