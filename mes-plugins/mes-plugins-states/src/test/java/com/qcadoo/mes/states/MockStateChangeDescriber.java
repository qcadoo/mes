/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.states;

import static org.mockito.Mockito.mock;

import org.mockito.Mockito;

import com.qcadoo.model.api.DataDefinition;

public final class MockStateChangeDescriber extends AbstractStateChangeDescriber {

    private final DataDefinition stateChangeDataDefinition;

    private final DataDefinition ownerDataDefinition;

    public MockStateChangeDescriber() {
        this(mock(DataDefinition.class), mock(DataDefinition.class));
    }

    public MockStateChangeDescriber(final DataDefinition stateChangeDD) {
        this(stateChangeDD, mock(DataDefinition.class));
    }

    public MockStateChangeDescriber(final DataDefinition stateChangeDD, final DataDefinition ownerDD) {
        this.stateChangeDataDefinition = stateChangeDD;
        this.ownerDataDefinition = ownerDD;
    }

    @Override
    public DataDefinition getDataDefinition() {
        return stateChangeDataDefinition;
    }

    @Override
    public StateEnum parseStateEnum(final String stringValue) {
        return Mockito.mock(StateEnum.class);
    }

    @Override
    public void checkFields() {
    }

    @Override
    public DataDefinition getOwnerDataDefinition() {
        return ownerDataDefinition;
    }

    @Override
    public String getOwnerFieldName() {
        return "owner";
    }

}
