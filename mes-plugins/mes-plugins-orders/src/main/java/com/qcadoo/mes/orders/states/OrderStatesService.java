package com.qcadoo.mes.orders.states;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.model.api.Entity;

@Service
public class OrderStatesService {

    @Autowired
    OrderStateChangingService orderStateChangingService;

    private List<OrderStateListener> listeners = new LinkedList<OrderStateListener>();

    public void addOrderStateListener(OrderStateListener listener) {
        listeners.add(listener);
    }

    public void removeOrderStateListener(OrderStateListener listener) {
        listeners.remove(listener);
    }

    ChangeOrderStateError performChangeState(final Entity newEntity, final Entity oldEntity) {
        if (oldEntity == null && !newEntity.getStringField("state").equals("pending")) {
            throw new IllegalStateException();
        }
        if (oldEntity != null && newEntity.getStringField("state").equals("open")
                && !oldEntity.getStringField("state").equals("pending")) {
            throw new IllegalStateException();
        }
        ChangeOrderStateError error = globalValidation(newEntity);
        if (error != null) {
            return error;
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

    ChangeOrderStateError globalValidation(final Entity newEntity) {
        ChangeOrderStateError error = null;
        for (String reference : Arrays.asList("number", "name")) {
            if (newEntity.getStringField(reference) == null) {
                error = new ChangeOrderStateError();
                error.setMessage("orders.order.orderStates.fieldRequired");
                error.setReferenceToField(reference);
                return error;
            }
        }
        return null;
    }

    ChangeOrderStateError performAccepted(final Entity newEntity, final Entity oldEntity) {
        ChangeOrderStateError errorMessage = orderStateChangingService.validationAccepted(newEntity);
        if (errorMessage != null) {
            return errorMessage;
        }
        for (OrderStateListener listener : listeners) {
            errorMessage = listener.onAccepted(newEntity);
            if (errorMessage != null) {
                return errorMessage;
            }
        }
        return null;
    }

    ChangeOrderStateError performInProgress(final Entity newEntity, final Entity oldEntity) {
        ChangeOrderStateError errorMessage = orderStateChangingService.validationInProgress(newEntity);
        if (errorMessage != null) {
            return errorMessage;
        }
        for (OrderStateListener listener : listeners) {
            errorMessage = listener.onInProgress(newEntity);
            if (errorMessage != null) {
                return errorMessage;
            }
        }
        return null;
    }

    ChangeOrderStateError performCompleted(final Entity newEntity, final Entity oldEntity) {
        ChangeOrderStateError errorMessage = orderStateChangingService.validationCompleted(newEntity);
        if (errorMessage != null) {
            return errorMessage;
        }
        for (OrderStateListener listener : listeners) {
            errorMessage = listener.onCompleted(newEntity);
            if (errorMessage != null) {
                return errorMessage;
            }
        }
        return null;
    }

    ChangeOrderStateError performInterrupted(final Entity newEntity, final Entity oldEntity) {
        for (OrderStateListener listener : listeners) {
            ChangeOrderStateError errorMessage = listener.onInterrupted(newEntity);
            if (errorMessage != null) {
                return errorMessage;
            }
        }
        return null;
    }

    ChangeOrderStateError performDeclined(final Entity newEntity, final Entity oldEntity) {
        for (OrderStateListener listener : listeners) {
            ChangeOrderStateError errorMessage = listener.onDeclined(newEntity);
            if (errorMessage != null) {
                return errorMessage;
            }
        }
        return null;
    }

    ChangeOrderStateError performAbandoned(final Entity newEntity, final Entity oldEntity) {
        for (OrderStateListener listener : listeners) {
            ChangeOrderStateError errorMessage = listener.onDeclined(newEntity);
            if (errorMessage != null) {
                return errorMessage;
            }
        }
        return null;
    }

}
