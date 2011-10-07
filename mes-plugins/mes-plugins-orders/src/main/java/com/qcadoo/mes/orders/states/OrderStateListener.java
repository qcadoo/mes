package com.qcadoo.mes.orders.states;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;

@Service
public class OrderStateListener {

    public ChangeOrderStateError onPending(final Entity newEntity) {
        return null;
    }

    public ChangeOrderStateError onAccepted(final Entity newEntity) {
        return null;
    }

    public ChangeOrderStateError onInProgress(final Entity newEntity) {
        return null;
    }

    public ChangeOrderStateError onCompleted(final Entity newEntity) {
        return null;
    }

    public ChangeOrderStateError onDeclined(final Entity newEntity) {
        return null;
    }

    public ChangeOrderStateError onInterrupted(final Entity newEntity) {
        return null;
    }

    public ChangeOrderStateError onAbandoned(final Entity newEntity) {
        return null;
    }

}
