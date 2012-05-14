package com.qcadoo.mes.samples.aop;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.samples.util.SamplesValidationHelper;
import com.qcadoo.model.api.Entity;

@Aspect
@Configurable
public class ValidationAfterSaveAspect {

    @Autowired
    private SamplesValidationHelper samplesValidationHelper;

    @Pointcut("call(* com.qcadoo.model.api.DataDefinition.save(com.qcadoo.model.api.Entity)) && args(entity)")
    public void dataDefinitionSaveCall(final Entity entity) {
    }

    @After("dataDefinitionSaveCall(entity) && cflow(within(com.qcadoo.mes.samples.api.SamplesLoader+))")
    public void afterDataDefinitionSaveCall(final Entity entity) {
        samplesValidationHelper.validateEntity(entity);
    }

}
