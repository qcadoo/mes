/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.api;

/**
 * Status of the service operation.
 * 
 * @see com.qcadoo.mes.api.PluginManagementService
 */
public interface PluginManagementOperationStatus {

    /**
     * Return true if the plugin service operation has finished with error.
     * 
     * @return true if error exists
     */
    boolean isError();

    /**
     * Return error message for the plugin service operation.
     * 
     * @return error message
     */
    String getMessage();

    /**
     * Return true if the plugin service operation requires server's restart.
     * 
     * @return true if restart is required
     */
    boolean isRestartRequired();

}