/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.model.validators;

/**
 * Object holds validation error message.
 */
public final class ErrorMessage {

    private final String message;

    private final String[] vars;

    /**
     * Create new validation error message.
     * 
     * @param message
     *            message
     * @param vars
     *            message's vars
     */
    public ErrorMessage(final String message, final String... vars) {
        this.message = message;
        this.vars = vars;
    }

    /**
     * Return validation error message.
     * 
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Return validation error message's vars.
     * 
     * @return message's vars
     */
    public String[] getVars() {
        return vars;
    }

}
