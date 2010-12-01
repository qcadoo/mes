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

package com.qcadoo.mes.view.containers;

/**
 * View value of Form component.
 * 
 * @see com.qcadoo.mes.view.containers.FormComponent
 * @see com.qcadoo.mes.view.ViewValue
 */
public final class FormValue {

    private Long id;

    private String header;

    private String headerEntityIdentifier;

    private boolean valid = true;

    public FormValue() {
    }

    public FormValue(final Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(final String header) {
        this.header = header;
    }

    public void setHeaderEntityIdentifier(final String headerEntityIdentifier) {
        this.headerEntityIdentifier = headerEntityIdentifier;
    }

    public String getHeaderEntityIdentifier() {
        return headerEntityIdentifier;
    }

    public void setValid(final boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public String toString() {
        if (id == null) {
            return ""; // FIXME masz toString cannot return null
        }
        return id.toString();
    }

}
