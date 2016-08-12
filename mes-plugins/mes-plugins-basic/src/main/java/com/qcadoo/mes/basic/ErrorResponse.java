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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "exception" })
public class ErrorResponse extends IntegrationJsonMessage {

    private ResponseStatus status;

    private String message;

    private final Exception exception;

    public ErrorResponse() {
        this("An unexpected error was occured");
        this.status = ResponseStatus.ERROR;
    }

    public ErrorResponse(final String message) {
        super();
        this.message = message;
        exception = new BasicException(message);
    }

    public ErrorResponse(final Exception exception) {
        super();
        this.message = exception.getMessage();
        this.exception = exception;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setStatus(final ResponseStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public enum ResponseStatus {
        OK, ERROR;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(status).append(message).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || !(obj instanceof ErrorResponse)) {
            return false;
        }
        ErrorResponse other = (ErrorResponse) obj;
        return new EqualsBuilder().append(status, other.status).append(message, other.message).isEquals();
    }

    public final Exception getException() {
        return exception;
    }

}
