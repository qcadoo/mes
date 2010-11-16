/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.internal;

import com.qcadoo.mes.api.PluginManagementOperationStatus;


public final class PluginManagementOperationStatusImpl implements PluginManagementOperationStatus {

    private boolean restartRequired = false;

    private boolean error;

    private String message;

    public PluginManagementOperationStatusImpl(final boolean error, final String message) {
        super();
        this.error = error;
        this.message = message;
    }

    @Override
    public boolean isError() {
        return error;
    }

    public void setError(final boolean error) {
        this.error = error;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public boolean isRestartRequired() {
        return restartRequired;
    }

    public void setRestartRequired(final boolean restartRequired) {
        this.restartRequired = restartRequired;
    }

}
