package com.qcadoo.mes.core.data.internal.hooks;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.internal.model.HookDefinitionImpl;
import com.qcadoo.mes.core.data.model.HookDefinition;

@Service
public final class HookFactory {

    private static HookFactory INSTANCE;

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        INSTANCE = this;
    }

    public static final HookFactory getInstance() {
        return INSTANCE;
    }

    public HookDefinition getHook(final String beanName, final String methodName) {
        return new HookDefinitionImpl(applicationContext.getBean(beanName), methodName);
    }

}
