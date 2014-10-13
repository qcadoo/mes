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
package com.qcadoo.mes.samples.aop;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.samples.constants.SamplesConstants;
import com.qcadoo.mes.samples.util.SamplesValidationHelper;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(SamplesConstants.PLUGIN_IDENTIFIER)
public class ValidationAfterSaveAspect {

    @Autowired
    private SamplesValidationHelper samplesValidationHelper;

    @Pointcut("call(* com.qcadoo.model.api.DataDefinition.save(com.qcadoo.model.api.Entity)) && args(entity)")
    public void dataDefinitionSaveCall(final Entity entity) {
    }

    @After("dataDefinitionSaveCall(entity) && cflow(within(com.qcadoo.mes.samples.api.SamplesLoader+)) && execution(* *(..))")
    public void afterDataDefinitionSaveCall(final Entity entity) {
        samplesValidationHelper.validateEntity(entity);
    }

}
