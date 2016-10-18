package com.qcadoo.mes.productionPerShift.domain;

import com.qcadoo.view.api.ComponentState;

public class PpsMessage {

    ComponentState.MessageType type;

    private String message;

    private String[] vars;

    private boolean autoClose;

    public PpsMessage(ComponentState.MessageType type, String message, boolean autoClose) {
        this.type = type;
        this.message = message;
        this.autoClose = autoClose;
    }

    public PpsMessage(ComponentState.MessageType type, String message, boolean autoClose, String[] vars) {
        this.type = type;
        this.message = message;
        this.vars = vars;
        this.autoClose = autoClose;
    }

    public ComponentState.MessageType getType() {
        return type;
    }

    public void setType(ComponentState.MessageType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String[] getVars() {
        return vars;
    }

    public void setVars(String[] vars) {
        this.vars = vars;
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    public void setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
    }
}
