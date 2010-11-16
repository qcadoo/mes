/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.api;

import com.qcadoo.mes.beans.users.UsersUser;

/**
 * Service for getting current user.
 */
public interface SecurityService {

    /**
     * Return the current logged user.
     * 
     * @throws NullPointerException
     *             if current user is not found
     * @return the current user
     */
    UsersUser getCurrentUser();

}
