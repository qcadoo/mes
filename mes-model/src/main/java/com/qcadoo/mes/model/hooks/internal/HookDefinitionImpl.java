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

package com.qcadoo.mes.model.hooks.internal;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.HookDefinition;

public final class HookDefinitionImpl implements HookDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(HookDefinitionImpl.class);

    private final Object bean;

    private final String methodName;

    public HookDefinitionImpl(final Object bean, final String methodName) {
        this.bean = bean;
        this.methodName = methodName;
    }

    private Object call(final Object[] params, final Class<?>[] paramClasses) {
        try {
            return MethodUtils.invokeMethod(bean, methodName, params, paramClasses);
        } catch (NoSuchMethodException e) {
            LOG.warn("custom validation method is not exist", e);
        } catch (IllegalAccessException e) {
            LOG.warn("problem while calling custom validation method", e);
        } catch (InvocationTargetException e) {
            LOG.warn("problem while calling custom validation method", e);
        } catch (ClassCastException e) {
            LOG.warn("custom validation method has returned not boolean type", e);
        }
        return null;
    }

    @Override
    public boolean callWithObjectAndGetBoolean(final DataDefinition dataDefinition, final Object value) {
        Boolean retults = (Boolean) call(new Object[] { dataDefinition, value },
                new Class[] { DataDefinition.class, Object.class });
        if (retults == null) {
            return false;
        } else {
            return retults;
        }
    }

    @Override
    public boolean callWithEntityAndGetBoolean(final DataDefinition dataDefinition, final Entity entity) {
        Boolean retults = (Boolean) call(new Object[] { dataDefinition, entity }, new Class[] { DataDefinition.class,
                Entity.class });
        if (retults == null) {
            return false;
        } else {
            return retults;
        }
    }

    @Override
    public void callWithEntity(final DataDefinition dataDefinition, final Entity entity) {
        call(new Object[] { dataDefinition, entity }, new Class[] { DataDefinition.class, Entity.class });
    }

    public String getMethod() {
        return methodName;
    }

    public Object getObject() {
        return bean;
    }

}
