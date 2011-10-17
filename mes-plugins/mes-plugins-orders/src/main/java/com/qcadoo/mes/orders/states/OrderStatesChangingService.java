package com.qcadoo.mes.orders.states;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.model.api.Entity;

@Service
public class OrderStatesChangingService {

    @Autowired
    TranslationService translationService;

    @Autowired
    OrderStateValidationService orderStateValidationService;

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

    List<ChangeOrderStateMessage> performAccepted(final Entity newEntity, final Entity oldEntity) {
        List<ChangeOrderStateMessage> errorMessages = orderStateValidationService.validationAccepted(newEntity);
        if (errorMessages.size() > 0) {
            return errorMessages;
        }
        for (OrderStateListener listener : listeners) {
            errorMessages = listener.onAccepted(newEntity);
            if (errorMessages != null && errorMessages.size() > 0) {
                return errorMessages;
            }
        }
        return null;
    }

    List<ChangeOrderStateMessage> performInProgress(final Entity newEntity, final Entity oldEntity) {
        List<ChangeOrderStateMessage> errorMessages = orderStateValidationService.validationInProgress(newEntity);
        if (errorMessages.size() > 0) {
            return errorMessages;
        }
        for (OrderStateListener listener : listeners) {
            errorMessages = listener.onInProgress(newEntity);
            if (errorMessages != null && errorMessages.size() > 0) {
                return errorMessages;
            }
        }
        return null;
    }

    List<ChangeOrderStateMessage> performCompleted(final Entity newEntity, final Entity oldEntity) {
        List<ChangeOrderStateMessage> errorMessages = orderStateValidationService.validationCompleted(newEntity);
        if (errorMessages.size() > 0) {
            return errorMessages;
        }
        for (OrderStateListener listener : listeners) {
            errorMessages = listener.onCompleted(newEntity);
            if (errorMessages != null && errorMessages.size() > 0) {
                return errorMessages;
            }
        }
        return null;
    }

    List<ChangeOrderStateMessage> performInterrupted(final Entity newEntity, final Entity oldEntity) {
        List<ChangeOrderStateMessage> errorMessages = orderStateValidationService.validationInProgress(newEntity);
        if (errorMessages.size() > 0) {
            return errorMessages;
        }
        for (OrderStateListener listener : listeners) {
            errorMessages = listener.onInterrupted(newEntity);
            if (errorMessages != null && errorMessages.size() > 0) {
                return errorMessages;
            }
        }
        return null;
    }

    List<ChangeOrderStateMessage> performDeclined(final Entity newEntity, final Entity oldEntity) {
        for (OrderStateListener listener : listeners) {
            List<ChangeOrderStateMessage> errorMessages = listener.onDeclined(newEntity);
            if (errorMessages != null && errorMessages.size() > 0) {
                return errorMessages;
            }
        }
        return null;
    }

    List<ChangeOrderStateMessage> performAbandoned(final Entity newEntity, final Entity oldEntity) {
        for (OrderStateListener listener : listeners) {
            List<ChangeOrderStateMessage> errorMessages = listener.onAbandoned(newEntity);
            if (errorMessages != null && errorMessages.size() > 0) {
                return errorMessages;
            }
        }
        return null;
    }

}
