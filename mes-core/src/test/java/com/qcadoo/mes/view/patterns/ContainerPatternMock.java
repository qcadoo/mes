/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.view.patterns;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ContainerState;
import com.qcadoo.mes.view.states.ContainerStateMock;

public class ContainerPatternMock extends AbstractContainerPattern {

    private final ContainerState containerState;

    public ContainerPatternMock(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
        this.containerState = new ContainerStateMock();
    }

    public ContainerPatternMock(final ComponentDefinition componentDefinition, final ContainerState containerState) {
        super(componentDefinition);
        this.containerState = containerState;
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return containerState;
    }

    @Override
    public String getJspFilePath() {
        return JSP_PATH;
    }

    @Override
    public String getJsFilePath() {
        return JS_PATH;
    }

    @Override
    public String getJsObjectName() {
        return JS_OBJECT;
    }
}
