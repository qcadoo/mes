package com.qcadoo.mes.orders.states;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;

@Service
public class OrderStateListener {

    public List<ChangeOrderStateMessage> onPending(final Entity newEntity) {
        return null;
    }

    public List<ChangeOrderStateMessage> onAccepted(final Entity newEntity) {
        return null;
    }

    public List<ChangeOrderStateMessage> onInProgress(final Entity newEntity) {
        return null;
    }

    public List<ChangeOrderStateMessage> onCompleted(final Entity newEntity) {
        return null;
    }

    public List<ChangeOrderStateMessage> onDeclined(final Entity newEntity) {
        return null;
    }

    public List<ChangeOrderStateMessage> onInterrupted(final Entity newEntity) {
        return null;
    }

    public List<ChangeOrderStateMessage> onAbandoned(final Entity newEntity) {
        return null;
    }

}
