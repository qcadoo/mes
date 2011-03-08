/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.view.hooks.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import org.apache.commons.beanutils.MethodUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcadoo.mes.view.HookDefinition;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.ViewDefinitionState;

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
    public void callWithViewState(final ViewDefinitionState viewDefinitionState, final Locale locale) {
        call(new Object[] { viewDefinitionState, locale }, new Class[] { ViewDefinitionState.class, Locale.class });
    }

    @Override
    public void callWithJSONObject(final ViewDefinition viewDefinition, final JSONObject object, final Locale locale) {
        call(new Object[] { viewDefinition, object, locale },
                new Class[] { ViewDefinition.class, JSONObject.class, Locale.class });
    }

    public String getMethod() {
        return methodName;
    }

    public Object getObject() {
        return bean;
    }

}
