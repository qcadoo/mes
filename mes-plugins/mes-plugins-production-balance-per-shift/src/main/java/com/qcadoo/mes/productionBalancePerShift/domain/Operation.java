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
package com.qcadoo.mes.productionBalancePerShift.domain;

public class Operation {

    private final Long id;

    private final String number;

    private final String nodeNumber;

    public Operation(final Long id, final String number, final String nodeNumber) {
        // Preconditions.checkArgument(id != null, "operation id must be not null");
        // Preconditions.checkArgument(number != null, "operation number must be not null");
        this.id = id;
        this.number = number;
        this.nodeNumber = nodeNumber;
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public String getNodeNumber() {
        return nodeNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Operation operation = (Operation) o;

        if (id != null ? !id.equals(operation.id) : operation.id != null)
            return false;
        if (number != null ? !number.equals(operation.number) : operation.number != null)
            return false;
        return nodeNumber != null ? nodeNumber.equals(operation.nodeNumber) : operation.nodeNumber == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (nodeNumber != null ? nodeNumber.hashCode() : 0);
        return result;
    }
}
