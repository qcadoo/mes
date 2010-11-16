/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.hooks.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.HookDefinition;
import com.qcadoo.mes.view.ViewValue;

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

    @Override
    public void callWithViewValue(final ViewValue<Long> value, final String triggerComponentName, final Locale locale) {
        call(new Object[] { value, triggerComponentName, locale }, new Class[] { ViewValue.class, String.class, Locale.class });
    }

}
