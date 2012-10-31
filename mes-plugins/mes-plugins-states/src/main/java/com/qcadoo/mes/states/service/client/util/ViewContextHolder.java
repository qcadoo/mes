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
package com.qcadoo.mes.states.service.client.util;

import com.google.common.base.Preconditions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public final class ViewContextHolder {

    private final ComponentState invoker;

    private final ComponentState messagesConsumer;

    private final ViewDefinitionState viewDefinitionState;

    public ViewContextHolder(final ViewDefinitionState view, final ComponentState invoker) {
        this(view, invoker, invoker);
    }

    public ViewContextHolder(final ViewDefinitionState viewDefinitionState, final ComponentState invoker,
            final ComponentState messagesConsumer) {
        Preconditions.checkNotNull(viewDefinitionState);
        Preconditions.checkNotNull(invoker);

        this.viewDefinitionState = viewDefinitionState;
        this.invoker = invoker;
        if (messagesConsumer == null) {
            this.messagesConsumer = invoker;
        } else {
            this.messagesConsumer = messagesConsumer;
        }
    }

    public ComponentState getInvoker() {
        return invoker;
    }

    public ComponentState getMessagesConsumer() {
        return messagesConsumer;
    }

    public ViewDefinitionState getViewDefinitionState() {
        return viewDefinitionState;
    }

}
