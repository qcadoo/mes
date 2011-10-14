package com.qcadoo.mes.orders.states;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.model.api.Entity;

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

    ChangeOrderStateMessage performChangeState(final Entity newEntity, final Entity oldEntity) {
        if (oldEntity == null && !newEntity.getStringField("state").equals("pending")) {
            throw new IllegalStateException();
        }
        if (oldEntity != null && newEntity.getStringField("state").equals("open")
                && !oldEntity.getStringField("state").equals("pending")) {
            throw new IllegalStateException();
        }
        ChangeOrderStateMessage error = globalValidation(newEntity);
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

    ChangeOrderStateMessage globalValidation(final Entity newEntity) {
        ChangeOrderStateMessage error = null;
        for (String reference : Arrays.asList("number", "name")) {
            if (newEntity.getStringField(reference) == null) {
                error = new ChangeOrderStateMessage();
                error.setMessage("orders.order.orderStates.fieldRequired");
                error.setReferenceToField(reference);
                return error;
            }
        }
        return null;
    }

    ChangeOrderStateMessage performAccepted(final Entity newEntity, final Entity oldEntity) {
        ChangeOrderStateMessage errorMessage = orderStateChangingService.validationAccepted(newEntity);
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

    ChangeOrderStateMessage performInProgress(final Entity newEntity, final Entity oldEntity) {
        ChangeOrderStateMessage errorMessage = orderStateChangingService.validationInProgress(newEntity);
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

    ChangeOrderStateMessage performCompleted(final Entity newEntity, final Entity oldEntity) {
        ChangeOrderStateMessage errorMessage = orderStateChangingService.validationCompleted(newEntity);
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

    ChangeOrderStateMessage performInterrupted(final Entity newEntity, final Entity oldEntity) {
        for (OrderStateListener listener : listeners) {
            ChangeOrderStateMessage errorMessage = listener.onInterrupted(newEntity);
            if (errorMessage != null) {
                return errorMessage;
            }
        }
        return null;
    }

    ChangeOrderStateMessage performDeclined(final Entity newEntity, final Entity oldEntity) {
        for (OrderStateListener listener : listeners) {
            ChangeOrderStateMessage errorMessage = listener.onDeclined(newEntity);
            if (errorMessage != null) {
                return errorMessage;
            }
        }
        return null;
    }

    ChangeOrderStateMessage performAbandoned(final Entity newEntity, final Entity oldEntity) {
        for (OrderStateListener listener : listeners) {
            ChangeOrderStateMessage errorMessage = listener.onDeclined(newEntity);
            if (errorMessage != null) {
                return errorMessage;
            }
        }
        return null;
    }

}
