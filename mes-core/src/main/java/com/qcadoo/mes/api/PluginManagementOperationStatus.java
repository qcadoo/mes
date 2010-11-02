package com.qcadoo.mes.api;

public class PluginManagementOperationStatus {

    private boolean restartRequired = false;

    private boolean error;

    private String message;

    public PluginManagementOperationStatus(boolean error, String message) {
        super();
        this.error = error;
        this.message = message;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRestartRequired() {
        return restartRequired;
    }

    public void setRestartRequired(boolean restartRequired) {
        this.restartRequired = restartRequired;
    }

}
