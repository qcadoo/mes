package com.qcadoo.mes.core.data.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.definition.CallbackDefinition;

@Service
public final class CallbackFactory {

    @Autowired
    private ApplicationContext applicationContext;

    public CallbackDefinition getCallback(final String beanName, final String methodName) {
        return new CallbackDefinition(applicationContext.getBean(beanName), methodName);
    }

}
