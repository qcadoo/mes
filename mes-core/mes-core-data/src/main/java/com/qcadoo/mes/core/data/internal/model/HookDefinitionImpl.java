package com.qcadoo.mes.core.data.internal.model;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.HookDefinition;
import com.qcadoo.mes.core.data.model.DataDefinition;

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

    /* (non-Javadoc)
     * @see com.qcadoo.mes.core.data.model.HookDefinition#callWithObjectAndGetBoolean(com.qcadoo.mes.core.data.model.ModelDefinition, java.lang.Object)
     */
    @Override
    public boolean callWithObjectAndGetBoolean(final DataDefinition dataDefinition, final Object value) {
        Boolean retults = (Boolean) call(dataDefinition, value);
        if (retults == null) {
            return false;
        } else {
            return retults;
        }
    }

    /* (non-Javadoc)
     * @see com.qcadoo.mes.core.data.model.HookDefinition#callWithEntityAndGetBoolean(com.qcadoo.mes.core.data.model.ModelDefinition, com.qcadoo.mes.core.data.beans.Entity)
     */
    @Override
    public boolean callWithEntityAndGetBoolean(final DataDefinition dataDefinition, final Entity entity) {
        Boolean retults = (Boolean) call(dataDefinition, entity);
        if (retults == null) {
            return false;
        } else {
            return retults;
        }
    }

    /* (non-Javadoc)
     * @see com.qcadoo.mes.core.data.model.HookDefinition#callWithEntity(com.qcadoo.mes.core.data.model.ModelDefinition, com.qcadoo.mes.core.data.beans.Entity)
     */
    @Override
    public void callWithEntity(final DataDefinition dataDefinition, final Entity entity) {
        call(dataDefinition, entity);
    }

}
