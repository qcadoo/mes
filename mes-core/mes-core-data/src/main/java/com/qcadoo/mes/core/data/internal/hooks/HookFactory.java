package com.qcadoo.mes.core.data.internal.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.internal.model.HookDefinitionImpl;
import com.qcadoo.mes.core.data.model.HookDefinition;

@Service
public final class HookFactory {

    @Autowired
    private ApplicationContext applicationContext;

    public HookDefinition getHook(final String fullyQualifiedClassName, final String methodName) {
        Class<?> beanClass;
        try {
            beanClass = HookFactory.class.getClassLoader().loadClass(fullyQualifiedClassName);
            Object bean = applicationContext.getBean(beanClass);
            if (bean != null) {
                return new HookDefinitionImpl(bean, methodName);
            } else {
                throw new IllegalStateException("Cannot find bean for hook: " + fullyQualifiedClassName);
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find mapping class for hook: " + fullyQualifiedClassName, e);
        }
    }

}
