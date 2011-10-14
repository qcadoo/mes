package com.qcadoo.mes.orders.states;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ShiftsServiceImpl;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState.MessageType;

@Service
public class OrderStateValidationService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ShiftsServiceImpl shiftsServiceImpl;

    public void saveLogging(final Entity order, final String previousState, final String currentState) {
        Entity logging = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_LOGGING).create();

        logging.setField("order", order);
        logging.setField("previousState", previousState);
        logging.setField("currentState", currentState);
        Date dateTime = new Date();
        Entity shift = shiftsServiceImpl.getShiftFromDate(dateTime);
        if (shift != null)
            logging.setField("shift", shift);
        else
            logging.setField("shift", null);
        logging.setField("worker", securityService.getCurrentUserName());
        logging.setField("dateAndTime", dateTime);

        logging.getDataDefinition().save(logging);
    }

    public ChangeOrderStateMessage validationPending(final Entity entity) {
        checkArgument(entity != null, "entity is null");
        List<String> references = Arrays.asList("product", "plannedQuantity");
        return checkValidation(references, entity);
    }

    public ChangeOrderStateMessage validationAccepted(final Entity entity) {
        checkArgument(entity != null, "entity is null");
        List<String> references = Arrays.asList("product", "plannedQuantity", "dateTo", "dateFrom", "technology");
        return checkValidation(references, entity);
    }

    public ChangeOrderStateMessage validationInProgress(final Entity entity) {
        checkArgument(entity != null, "entity is null");
        return validationAccepted(entity);
    }

    public ChangeOrderStateMessage validationCompleted(final Entity entity) {
        checkArgument(entity != null, "entity is null");
        List<String> references = Arrays.asList("product", "plannedQuantity", "dateTo", "dateFrom", "technology", "doneQuantity");
        return checkValidation(references, entity);
    }

    private ChangeOrderStateMessage checkValidation(final List<String> references, final Entity entity) {
        checkArgument(entity != null, "entity is null");
        ChangeOrderStateMessage error = null;
        for (String reference : references)
            if (entity.getField(reference) == null) {
                error = new ChangeOrderStateMessage("orders.order.orderStates.fieldRequired", reference, MessageType.FAILURE);
                return error;
            }
        return null;
    }
}
