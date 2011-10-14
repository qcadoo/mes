package com.qcadoo.mes.orders.states;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;

@Service
public class OrderStateListener {

    public ChangeOrderStateMessage onPending(final Entity newEntity) {
        return null;
    }

    public ChangeOrderStateMessage onAccepted(final Entity newEntity) {
        return null;
    }

    public ChangeOrderStateMessage onInProgress(final Entity newEntity) {
        return null;
    }

    public ChangeOrderStateMessage onCompleted(final Entity newEntity) {
        return null;
    }

    public ChangeOrderStateMessage onDeclined(final Entity newEntity) {
        return null;
    }

    public ChangeOrderStateMessage onInterrupted(final Entity newEntity) {
        return null;
    }

    public ChangeOrderStateMessage onAbandoned(final Entity newEntity) {
        return null;
    }

}
