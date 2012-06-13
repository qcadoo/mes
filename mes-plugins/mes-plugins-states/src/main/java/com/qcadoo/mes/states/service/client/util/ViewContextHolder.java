package com.qcadoo.mes.states.service.client.util;

import com.google.common.base.Preconditions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public final class ViewContextHolder {

    private final ComponentState invoker;

    private final ComponentState messagesConsumer;

    private final ViewDefinitionState viewDefinitionState;

    public ViewContextHolder(final ViewDefinitionState view, final ComponentState invoker) {
        this(view, invoker, invoker);
    }

    public ViewContextHolder(final ViewDefinitionState viewDefinitionState, final ComponentState invoker,
            final ComponentState messagesConsumer) {
        Preconditions.checkNotNull(viewDefinitionState);
        Preconditions.checkNotNull(invoker);

        this.viewDefinitionState = viewDefinitionState;
        this.invoker = invoker;
        if (messagesConsumer == null) {
            this.messagesConsumer = invoker;
        } else {
            this.messagesConsumer = messagesConsumer;
        }
    }

    public ComponentState getInvoker() {
        return invoker;
    }

    public ComponentState getMessagesConsumer() {
        return messagesConsumer;
    }

    public ViewDefinitionState getViewDefinitionState() {
        return viewDefinitionState;
    }

}
