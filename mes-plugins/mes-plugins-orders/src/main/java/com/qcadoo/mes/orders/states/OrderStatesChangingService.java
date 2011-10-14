package com.qcadoo.mes.orders.states;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState.MessageType;

@Service
public class OrderStatesChangingService {

    @Autowired
    OrderStateValidationService orderStateChangingService;

    private List<OrderStateListener> listeners = new LinkedList<OrderStateListener>();

    public void addOrderStateListener(OrderStateListener listener) {
        listeners.add(listener);
    }

    public void removeOrderStateListener(OrderStateListener listener) {
        listeners.remove(listener);
    }

    List<ChangeOrderStateMessage> performChangeState(final Entity newEntity, final Entity oldEntity) {
        if (oldEntity == null && !newEntity.getStringField("state").equals("01pending")) {
            throw new IllegalStateException();
        }
        if (oldEntity != null && newEntity.getStringField("state").equals("02accepted")
                && !oldEntity.getStringField("state").equals("01pending")) {
            throw new IllegalStateException();
        }
        List<ChangeOrderStateMessage> errors = globalValidation(newEntity);
        if (errors != null) {
            return errors;
        }
        String newState = newEntity.getStringField("state");
        if (newState.equals(OrderStates.ACCEPTED.getStringValue())) {
            return performAccepted(newEntity, oldEntity);
        } else if (newState.equals(OrderStates.IN_PROGRESS.getStringValue())) {
            return performInProgress(newEntity, oldEntity);
        } else if (newState.equals(OrderStates.COMPLETED.getStringValue())) {
            return performCompleted(newEntity, oldEntity);
        } else if (newState.equals(OrderStates.INTERRUPTED.getStringValue())) {
            return performInterrupted(newEntity, oldEntity);
        } else if (newState.equals(OrderStates.DECLINED.getStringValue())) {
            return performDeclined(newEntity, oldEntity);
        } else if (newState.equals(OrderStates.ABANDONED.getStringValue())) {
            return performAbandoned(newEntity, oldEntity);
        }

        return null;
    }

    List<ChangeOrderStateMessage> globalValidation(final Entity newEntity) {
        List<ChangeOrderStateMessage> errors = new ArrayList<ChangeOrderStateMessage>();
        for (String reference : Arrays.asList("number", "name")) {
            if (newEntity.getStringField(reference) == null) {
                errors.add(new ChangeOrderStateMessage("orders.order.orderStates.fieldRequired", reference, MessageType.FAILURE));
            }
        }
        return errors;
    }

    List<ChangeOrderStateMessage> performAccepted(final Entity newEntity, final Entity oldEntity) {
        List<ChangeOrderStateMessage> errorMessages = orderStateChangingService.validationAccepted(newEntity);
        if (errorMessages != null) {
            return errorMessages;
        }
        for (OrderStateListener listener : listeners) {
            errorMessages = listener.onAccepted(newEntity);
            if (errorMessages != null) {
                return errorMessages;
            }
        }
        return null;
    }

    List<ChangeOrderStateMessage> performInProgress(final Entity newEntity, final Entity oldEntity) {
        List<ChangeOrderStateMessage> errorMessages = orderStateChangingService.validationInProgress(newEntity);
        if (errorMessages != null) {
            return errorMessages;
        }
        for (OrderStateListener listener : listeners) {
            errorMessages = listener.onInProgress(newEntity);
            if (errorMessages != null) {
                return errorMessages;
            }
        }
        return null;
    }

    List<ChangeOrderStateMessage> performCompleted(final Entity newEntity, final Entity oldEntity) {
        List<ChangeOrderStateMessage> errorMessages = orderStateChangingService.validationCompleted(newEntity);
        if (errorMessages != null) {
            return errorMessages;
        }
        for (OrderStateListener listener : listeners) {
            errorMessages = listener.onCompleted(newEntity);
            if (errorMessages != null) {
                return errorMessages;
            }
        }
        return null;
    }

    List<ChangeOrderStateMessage> performInterrupted(final Entity newEntity, final Entity oldEntity) {
        for (OrderStateListener listener : listeners) {
            List<ChangeOrderStateMessage> errorMessages = listener.onInterrupted(newEntity);
            if (errorMessages != null) {
                return errorMessages;
            }
        }
        return null;
    }

    List<ChangeOrderStateMessage> performDeclined(final Entity newEntity, final Entity oldEntity) {
        for (OrderStateListener listener : listeners) {
            List<ChangeOrderStateMessage> errorMessages = listener.onDeclined(newEntity);
            if (errorMessages != null) {
                return errorMessages;
            }
        }
        return null;
    }

    List<ChangeOrderStateMessage> performAbandoned(final Entity newEntity, final Entity oldEntity) {
        for (OrderStateListener listener : listeners) {
            List<ChangeOrderStateMessage> errorMessages = listener.onDeclined(newEntity);
            if (errorMessages != null) {
                return errorMessages;
            }
        }
        return null;
    }

}
