package com.qcadoo.mes.techSubcontracting.aop;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;

import com.qcadoo.mes.technologies.MrpAlgorithmStrategy;

@Aspect
@Configurable
public final class MrpAlgorithmStrategyTSResolverAspect extends MrpAlgorithmStrategyResolverAspect {

    @Autowired
    @Qualifier("mrpAlgorithmStrategyTS")
    private MrpAlgorithmStrategy mrpAlgorithmStrategy;

    protected MrpAlgorithmStrategy getAlgorithmService() {
        return mrpAlgorithmStrategy;
    }
}
