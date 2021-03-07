package com.qcadoo.mes.orders.hooks;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.OrderPackService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderPackFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
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

        FieldComponent orderQuantity = (FieldComponent) view.getComponentByReference("orderQuantity");
        FieldComponent sumQuantityOrderPacksField = (FieldComponent) view.getComponentByReference("sumQuantityOrderPacks");
        FieldComponent orderQuantityUnit = (FieldComponent) view.getComponentByReference("orderQuantityUnit");
        FieldComponent sumQuantityOrderPacksUnit = (FieldComponent) view.getComponentByReference("sumQuantityOrderPacksUnit");
        FieldComponent quantityUnit = (FieldComponent) view.getComponentByReference("quantityUnit");
        if (order != null) {
            orderQuantity.setFieldValue(numberService.format(order.getField(OrderFields.PLANNED_QUANTITY)));
            FieldComponent quantityField = (FieldComponent) view.getComponentByReference(OrderPackFields.QUANTITY);
            BigDecimal sumQuantityOrderPacks = orderPackService.getSumQuantityOrderPacksForOrderWithoutPack(order,
                    form.getEntityId());
            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils
                    .tryParseAndIgnoreSeparator((String) quantityField.getFieldValue(), LocaleContextHolder.getLocale());
            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                sumQuantityOrderPacks = sumQuantityOrderPacks.add(eitherNumber.getRight().get(), numberService.getMathContext());
            }
            sumQuantityOrderPacksField.setFieldValue(numberService.format(sumQuantityOrderPacks));
            String unit = order.getBelongsToField(OrderFields.PRODUCT).getStringField(ProductFields.UNIT);
            orderQuantityUnit.setFieldValue(unit);
            sumQuantityOrderPacksUnit.setFieldValue(unit);
            quantityUnit.setFieldValue(unit);
        } else {
            orderQuantity.setFieldValue(null);
            sumQuantityOrderPacksField.setFieldValue(null);
            orderQuantityUnit.setFieldValue(null);
            sumQuantityOrderPacksUnit.setFieldValue(null);
            quantityUnit.setFieldValue(null);
        }
    }
}
