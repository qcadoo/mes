package com.qcadoo.mes.masterOrders.hooks;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class OrderDetailsHooksMO {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void fillMasterOrderFields(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity order = orderForm.getEntity();

        if (order.getId() == null) {
            Entity masterOrder = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);

            if (masterOrder != null) {
                Long masterOrderId = masterOrder.getId();

                masterOrder = getMasterOrder(masterOrderId);

                fillMasterOrderFields(view, masterOrder);
            }
        }
    }

    private void fillMasterOrderFields(final ViewDefinitionState view, final Entity masterOrder) {
        FieldComponent numberField = (FieldComponent) view.getComponentByReference(OrderFields.NUMBER);
        LookupComponent companyLookup = (LookupComponent) view.getComponentByReference(OrderFields.COMPANY);
        FieldComponent deadlineField = (FieldComponent) view.getComponentByReference(OrderFields.DEADLINE);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCT);
        LookupComponent technologyPrototypeLookup = (LookupComponent) view
                .getComponentByReference(OrderFields.TECHNOLOGY_PROTOTYPE);

        if (masterOrder != null) {
            String masterOrderNumber = masterOrder.getStringField(MasterOrderFields.NUMBER);
            Entity masterOrderCompany = masterOrder.getBelongsToField(MasterOrderFields.COMPANY);
            Date masterOrderDeadline = masterOrder.getDateField(MasterOrderFields.DEADLINE);
            Entity masterOrderProduct = masterOrder.getBelongsToField(MasterOrderFields.PRODUCT);
            Entity masterOrderTechnology = masterOrder.getBelongsToField(MasterOrderFields.TECHNOLOGY);

            String number = (String) numberField.getFieldValue();

            String generatedNumber = numberGeneratorService.generateNumber(OrdersConstants.PLUGIN_IDENTIFIER,
                    OrdersConstants.MODEL_ORDER);

            if (StringUtils.isEmpty(number) || generatedNumber.equals(number)) {
                numberField.setFieldValue(masterOrderNumber);
            }

            if ((companyLookup.getEntity() == null) && (masterOrderCompany != null)) {
                companyLookup.setFieldValue(masterOrderCompany.getId());
            }

            if (StringUtils.isEmpty((String) deadlineField.getFieldValue()) && (masterOrderDeadline != null)) {
                deadlineField.setFieldValue(DateUtils.toDateString(masterOrderDeadline));
            }

            if ((productLookup.getEntity() == null) && (masterOrderProduct != null)) {
                productLookup.setFieldValue(masterOrderProduct.getId());
            }

            if ((technologyPrototypeLookup.getEntity() == null) && (masterOrderTechnology != null)) {
                technologyPrototypeLookup.setFieldValue(masterOrderTechnology.getId());
            }

            numberField.requestComponentUpdateState();
            companyLookup.requestComponentUpdateState();
            deadlineField.requestComponentUpdateState();
            productLookup.requestComponentUpdateState();
            technologyPrototypeLookup.requestComponentUpdateState();
        }
    }

    private Entity getMasterOrder(final Long masterOrderId) {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER).get(
                masterOrderId);
    }

}
