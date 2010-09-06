package com.qcadoo.mes.core.data.definition;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcadoo.mes.core.data.beans.Entity;

public class CallbackDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(CallbackDefinition.class);

    private final Object bean;

    private final String methodName;

    public CallbackDefinition(final Object bean, final String methodName) {
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

    public boolean callWithObjectAndGetBoolean(final Object value) {
        Boolean retults = (Boolean) call(value);
        if (retults == null) {
            return false;
        } else {
            return retults;
        }
    }

    public boolean callWithEntityAndGetBoolean(final Entity entity) {
        Boolean retults = (Boolean) call(entity);
        if (retults == null) {
            return false;
        } else {
            return retults;
        }
    }

    public void callWithEntity(final Entity entity) {
        call(entity);
    }

}
