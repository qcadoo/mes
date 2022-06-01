package com.qcadoo.mes.orders;

import java.util.Date;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UserFieldsB;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessFields;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessWasteFields;
import com.qcadoo.mes.technologies.constants.TechnologicalProcessFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OrderTechnologicalProcessWasteService {

    private static final String L_TECHNOLOGICAL_PROCESS_NAME = "technologicalProcessName";

    private static final String L_WASTE_QUANTITY_UNIT = "wasteQuantityUnit";

    @Autowired
    private UserService userService;

    @Autowired
    private OrderTechnologicalProcessService orderTechnologicalProcessService;

    public void setFormEnabled(final FormComponent orderTechnologicalProcessWasteForm,
            final Entity orderTechnologicalProcessWaste, final Entity orderTechnologicalProcess) {
        boolean isEnabled = true;

        if (Objects.nonNull(orderTechnologicalProcess)) {
            Entity order = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER);

            isEnabled = !orderTechnologicalProcessService.checkOrderState(order);
        }

        orderTechnologicalProcessWasteForm.setFormEnabled(isEnabled);
    }

    public void fillUnit(final ViewDefinitionState view, final Entity orderTechnologicalProcess) {
        FieldComponent quantityUnit = (FieldComponent) view.getComponentByReference(L_WASTE_QUANTITY_UNIT);

        String unit = null;

        if (Objects.nonNull(orderTechnologicalProcess)) {
            Entity product = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessWasteFields.PRODUCT);

            if (Objects.nonNull(product)) {
                unit = product.getStringField(ProductFields.UNIT);
            }
        }

        quantityUnit.setFieldValue(unit);
        quantityUnit.requestComponentUpdateState();
    }

    public void fillOrderPack(final ViewDefinitionState view, final Entity orderTechnologicalProcess) {
        LookupComponent orderPackLookup = (LookupComponent) view
                .getComponentByReference(OrderTechnologicalProcessWasteFields.ORDER_PACK);

        Long orderPackId = null;

        if (Objects.nonNull(orderTechnologicalProcess)) {
            Entity orderPack = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER_PACK);

            if (Objects.nonNull(orderPack)) {
                orderPackId = orderPack.getId();
            }
        }

        orderPackLookup.setFieldValue(orderPackId);
        orderPackLookup.requestComponentUpdateState();
    }

    public void fillTechnologicalProcessName(final ViewDefinitionState view, final Entity orderTechnologicalProcess) {
        FieldComponent technologicalProcessNameField = (FieldComponent) view
                .getComponentByReference(L_TECHNOLOGICAL_PROCESS_NAME);

        String technologicalProcessName = null;

        if (Objects.nonNull(orderTechnologicalProcess)) {
            Entity technologicalProcess = orderTechnologicalProcess
                    .getBelongsToField(OrderTechnologicalProcessWasteFields.TECHNOLOGICAL_PROCESS);

            if (Objects.nonNull(technologicalProcess)) {
                technologicalProcessName = technologicalProcess.getStringField(TechnologicalProcessFields.NAME);
            }
        }

        technologicalProcessNameField.setFieldValue(technologicalProcessName);
        technologicalProcessNameField.requestComponentUpdateState();
    }

    public void fillDateAndWorker(final ViewDefinitionState view) {
        FieldComponent dateField = (FieldComponent) view.getComponentByReference(OrderTechnologicalProcessWasteFields.DATE);
        LookupComponent workerLookup = (LookupComponent) view
                .getComponentByReference(OrderTechnologicalProcessWasteFields.WORKER);

        String date = (String) dateField.getFieldValue();
        Entity worker = workerLookup.getEntity();

        if (view.isViewAfterRedirect() && Objects.isNull(date) && Objects.isNull(worker)) {
            Entity currentUser = userService.getCurrentUserEntity();

            date = DateUtils.toDateTimeString(new Date());
            worker = currentUser.getBelongsToField(UserFieldsB.STAFF);

            dateField.setFieldValue(date);

            if (Objects.nonNull(worker)) {
                workerLookup.setFieldValue(worker.getId());
            }

            dateField.requestComponentUpdateState();
            workerLookup.requestComponentUpdateState();
        }
    }

}
