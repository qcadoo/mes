package com.qcadoo.mes.orders.hooks;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.OrderPackService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderPackFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderPackDetailsHooks {

    @Autowired
    private NumberService numberService;

    @Autowired
    private OrderPackService orderPackService;

    private static final String ACTIONS = "actions";

    public final void onBeforeRender(final ViewDefinitionState view) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OrderPackFields.ORDER);
        Entity order = orderLookup.getEntity();

        FieldComponent orderQuantity = (FieldComponent) view.getComponentByReference("orderQuantity");
        FieldComponent sumQuantityOrderPacksField = (FieldComponent) view.getComponentByReference("sumQuantityOrderPacks");
        FieldComponent orderQuantityUnit = (FieldComponent) view.getComponentByReference("orderQuantityUnit");
        FieldComponent sumQuantityOrderPacksUnit = (FieldComponent) view.getComponentByReference("sumQuantityOrderPacksUnit");
        FieldComponent quantityUnit = (FieldComponent) view.getComponentByReference("quantityUnit");
        if (order != null) {
            String orderState = order.getStringField(OrderFields.STATE);
            FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
            if (OrderState.COMPLETED.getStringValue().equals(orderState)
                    || OrderState.DECLINED.getStringValue().equals(orderState)
                    || OrderState.ABANDONED.getStringValue().equals(orderState)
                    || OrderState.PENDING.getStringValue().equals(orderState)) {
                form.setFormEnabled(false);
                WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
                Ribbon ribbon = window.getRibbon();
                RibbonActionItem actionsSave = ribbon.getGroupByName(ACTIONS).getItemByName("save");
                RibbonActionItem actionsSaveNew = ribbon.getGroupByName(ACTIONS).getItemByName("saveNew");
                RibbonActionItem actionsSaveBack = ribbon.getGroupByName(ACTIONS).getItemByName("saveBack");
                actionsSave.setEnabled(false);
                actionsSave.requestUpdate(true);
                actionsSaveNew.setEnabled(false);
                actionsSaveNew.requestUpdate(true);
                actionsSaveBack.setEnabled(false);
                actionsSaveBack.requestUpdate(true);
            }
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
