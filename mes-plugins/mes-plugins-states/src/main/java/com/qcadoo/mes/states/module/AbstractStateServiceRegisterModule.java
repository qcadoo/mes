/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.states.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.mes.states.service.StateChangeServiceResolver;
import com.qcadoo.plugin.api.Module;

@Service
public abstract class AbstractStateServiceRegisterModule extends Module {

    @Autowired
    private StateChangeServiceResolver stateChangeServiceResolver;

    protected abstract StateChangeService getStateChangeService();

    @Override
    public void enable() {
        register();
    }

    @Override
    public void multiTenantEnable() {
        register();
    }

    private void register() {
        stateChangeServiceResolver.register(getStateChangeService().getChangeEntityDescriber().getOwnerDataDefinition(),
                getStateChangeService());
    }

    @Override
    public void disable() {
        stateChangeServiceResolver.unregister(getStateChangeService());
    }

}
