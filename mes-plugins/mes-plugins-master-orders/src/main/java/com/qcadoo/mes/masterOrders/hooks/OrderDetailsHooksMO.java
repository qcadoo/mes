package com.qcadoo.mes.masterOrders.hooks;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OrderDetailsHooksMO {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void fillMasterOrderFields(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity order = orderForm.getEntity();

        if (order.getId() == null) {
            Long masterOrderId = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER).getId();

            Entity masterOrder = getMasterOrder(masterOrderId);

            fillMasterOrderFields(view, masterOrder);
        }
    }

    private void fillMasterOrderFields(final ViewDefinitionState view, final Entity masterOrder) {
        FieldComponent numberField = (FieldComponent) view.getComponentByReference(OrderFields.NUMBER);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCT);
        LookupComponent companyLookup = (LookupComponent) view.getComponentByReference(OrderFields.COMPANY);
        FieldComponent deadlineField = (FieldComponent) view.getComponentByReference(OrderFields.DEADLINE);

        if (masterOrder != null) {
            String number = masterOrder.getStringField(MasterOrderFields.NUMBER);
            Entity product = masterOrder.getBelongsToField(MasterOrderFields.PRODUCT);
            Entity company = masterOrder.getBelongsToField(MasterOrderFields.COMPANY);
            Date deadline = masterOrder.getDateField(MasterOrderFields.DEADLINE);

            numberField.setFieldValue(number);

            if (product != null) {
                productLookup.setFieldValue(product.getId());
            }

            if (company != null) {
                companyLookup.setFieldValue(company.getId());
            }

            if (deadline != null) {
                deadlineField.setFieldValue(DateUtils.toDateString(deadline));
            }

            numberField.requestComponentUpdateState();
            productLookup.requestComponentUpdateState();
            companyLookup.requestComponentUpdateState();
            deadlineField.requestComponentUpdateState();
        }
    }

    private Entity getMasterOrder(final Long masterOrderId) {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER).get(
                masterOrderId);
    }

}
