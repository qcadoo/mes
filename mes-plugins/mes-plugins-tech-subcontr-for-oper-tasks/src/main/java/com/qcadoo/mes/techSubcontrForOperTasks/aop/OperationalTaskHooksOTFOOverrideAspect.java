/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.techSubcontrForOperTasks.aop;

import com.qcadoo.mes.techSubcontracting.constants.TechSubcontractingConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Aspect
@Configurable
@RunIfEnabled(TechSubcontractingConstants.PLUGIN_IDENTIFIER)
public class OperationalTaskHooksOTFOOverrideAspect {

    @Autowired
    private OperationalTaskHooksOTFOOverrideUtil operationalTaskHooksOTFOOverrideUtil;

    @Pointcut("execution(public void com.qcadoo.mes.orders.hooks.OperationalTaskHooks.onSave(..)) "
            + "&& args(operationalTaskDD, operationalTask)")
    public void onSaveExecution(final DataDefinition operationalTaskDD, final Entity operationalTask) {
    }

    @After("onSaveExecution(operationalTaskDD, operationalTask)")
    public void afterOnSaveExecution(final DataDefinition operationalTaskDD, final Entity operationalTask) throws Throwable {
        operationalTaskHooksOTFOOverrideUtil.onSaveForSubcontracted(operationalTaskDD, operationalTask);
    }

}
