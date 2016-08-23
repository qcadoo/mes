/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.basic;

import java.util.Map;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;

@SuppressWarnings("serial")
public class BasicException extends RuntimeException {

    public BasicException(final String message) {
        super(message);
    }

    public BasicException(final Throwable throwable) {
        super(throwable);
    }

    public BasicException(final String message, final Entity entity) {
        super(createMessageForValidationErrors(message, entity));
    }

    private static String createMessageForValidationErrors(final String message, final Entity entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(message).append("\n");

        for (ErrorMessage error : entity.getGlobalErrors()) {
            sb.append("- ").append(error.getMessage()).append("\n");
        }

        for (Map.Entry<String, ErrorMessage> error : entity.getErrors().entrySet()) {
            sb.append("- ").append(error.getKey()).append(" - ").append(error.getValue().getMessage()).append("\n");
        }

        return sb.toString();
    }
}
