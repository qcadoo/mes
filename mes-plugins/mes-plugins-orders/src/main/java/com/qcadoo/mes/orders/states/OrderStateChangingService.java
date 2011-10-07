package com.qcadoo.mes.orders.states;

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

@Service
public class OrderStateChangingService {

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
            throw new IllegalStateException();
        logging.setField("worker", securityService.getCurrentUserName());
        logging.setField("dateAndTime", dateTime);

        logging.getDataDefinition().save(logging);
    }

    public boolean validationPending(final Entity entity) {
        List<String> references = Arrays.asList("number", "name", "product", "plannedQuantity");
        return checkValidation(references, entity);
    }

    public boolean validationAccepted(final Entity entity) {
        List<String> references = Arrays.asList("number", "name", "product", "plannedQuantity", "dateTo", "dateFrom",
                "defaultTechnology", "technology");
        return checkValidation(references, entity);
    }

    public boolean validationInProgress(final Entity entity) {
        return validationAccepted(entity);
    }

    public boolean validationCompleted(final Entity entity) {
        List<String> references = Arrays.asList("number", "name", "product", "plannedQuantity", "dateTo", "dateFrom",
                "defaultTechnology", "technology", "doneQuantity");
        return checkValidation(references, entity);
    }

    public boolean checkValidation(final List<String> references, final Entity entity) {
        for (String reference : references)
            if (entity.getField(reference) == null) {
                entity.addError(entity.getDataDefinition().getField("order"), "orders.order.orderStates.fieldRequired");
                return false;
            }
        return true;
    }

}
