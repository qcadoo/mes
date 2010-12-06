package com.qcadoo.mes.view.internal;

public final class ComponentCustomEvent {

    private final String event;

    private final Object object;

    private final String method;

    public ComponentCustomEvent(final String event, final Object object, final String method) {
        this.event = event;
        this.object = object;
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public Object getObject() {
        return object;
    }

    public String getEvent() {
        return event;
    }

}
