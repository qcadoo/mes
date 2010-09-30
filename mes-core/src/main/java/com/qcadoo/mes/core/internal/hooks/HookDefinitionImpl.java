package com.qcadoo.mes.core.internal.hooks;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.view.ViewValue;
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

    private Object call(final Object... params) {
        try {
            return MethodUtils.invokeMethod(bean, methodName, params);
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
        Boolean retults = (Boolean) call(dataDefinition, value);
        if (retults == null) {
            return false;
        } else {
            return retults;
        }
    }

    @Override
    public boolean callWithEntityAndGetBoolean(final DataDefinition dataDefinition, final Entity entity) {
        Boolean retults = (Boolean) call(dataDefinition, entity);
        if (retults == null) {
            return false;
        } else {
            return retults;
        }
    }

    @Override
    public void callWithEntity(final DataDefinition dataDefinition, final Entity entity) {
        call(dataDefinition, entity);
    }

    @Override
    public void callWithViewValue(final ViewValue<Object> value, final String triggerComponentName) {
        call(value, triggerComponentName);
    }

}
