package com.qcadoo.mes.orders.states;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.model.api.Entity;

@Service
class OrderStatesService {

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
        if (oldEntity == null && !newEntity.getField("state").equals("pending")) {
            throw new IllegalStateException();
        }
        if (oldEntity != null && newEntity.getField("state").equals("open") && !oldEntity.getField("state").equals("pending")) {
            throw new IllegalStateException();
        }
        ChangeOrderStateError error = globalValidation(newEntity);
        if (error != null) {
            return error;
        }
        String newState = newEntity.getStringField("state");
        if (newState.equals(OrderStates.PENDING)) {
            return performPending(newEntity, oldEntity);
        } else if (newState.equals(OrderStates.ACCEPTED)) {
            return performAccepted(newEntity, oldEntity);
        } else if (newState.equals(OrderStates.IN_PROGRESS)) {
            return performInProgress(newEntity, oldEntity);
        } else if (newState.equals(OrderStates.COMPLETED)) {
            return performCompleted(newEntity, oldEntity);
        } else if (newState.equals(OrderStates.INTERRUPTED)) {
            return performInterrupted(newEntity, oldEntity);
        } else if (newState.equals(OrderStates.DECLINED)) {
            return performDeclined(newEntity, oldEntity);
        } else if (newState.equals(OrderStates.ABANDONED)) {
            return performAbandoned(newEntity, oldEntity);
        }

        return null;
    }

    ChangeOrderStateError globalValidation(final Entity newEntity) {
        ChangeOrderStateError error = null;
        for (String reference : Arrays.asList("number", "name")) {
            if (newEntity.getField(reference) == null) {
                error.setMessage("orders.order.orderStates.fieldRequired");
                error.setReferenceToField(reference);
                return error;
            }
        }
        return null;
    }

    ChangeOrderStateError performPending(final Entity newEntity, final Entity oldEntity) {
        ChangeOrderStateError errorMessage = orderStateChangingService.validationPending(newEntity);
        if (errorMessage != null) {
            return errorMessage;
        }
        for (OrderStateListener listener : listeners) {
            errorMessage = listener.onPending(newEntity);
            if (errorMessage != null) {
                return errorMessage;
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
        ChangeOrderStateError errorMessage = orderStateChangingService.validationCompleted(newEntity);
        if (errorMessage != null) {
            return errorMessage;
        }
        for (OrderStateListener listener : listeners) {
            errorMessage = listener.onInterrupted(newEntity);
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
