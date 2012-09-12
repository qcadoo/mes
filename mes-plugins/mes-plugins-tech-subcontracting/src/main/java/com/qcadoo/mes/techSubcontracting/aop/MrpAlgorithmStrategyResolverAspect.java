package com.qcadoo.mes.techSubcontracting.aop;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.qcadoo.mes.technologies.MrpAlgorithmStrategy;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginUtils;

@Aspect
public abstract class MrpAlgorithmStrategyResolverAspect {

    protected abstract MrpAlgorithmStrategy getAlgorithmService();

    @Pointcut("execution(private java.util.Map<com.qcadoo.model.api.Entity, java.math.BigDecimal> com.qcadoo.mes.technologies.ProductQuantitiesServiceImpl.getProducts(..)) "
            + "&& args(productComponentQuantities, nonComponents, algorithm, type)")
    public void getProductsMethodExecution(final Map<Entity, BigDecimal> productComponentQuantities,
            final Set<Entity> nonComponents, final MrpAlgorithm algorithm, final String type) {
    }

    @Around("getProductsMethodExecution(productComponentQuantities, nonComponents, algorithm, type)")
    public Map<Entity, BigDecimal> aroundGetProductsMethodExecution(final ProceedingJoinPoint pjp,
            final Map<Entity, BigDecimal> productComponentQuantities, final Set<Entity> nonComponents,
            final MrpAlgorithm algorithm, final String type) throws Throwable {
        Map<Entity, BigDecimal> productList = new HashMap<Entity, BigDecimal>();
        if (PluginUtils.isEnabled("techSubcontracting") && getAlgorithmService().isApplicableFor(algorithm)) {
            productList = getAlgorithmService().perform(productComponentQuantities, nonComponents, algorithm, type);
        } else {
            productList = (Map<Entity, BigDecimal>) pjp.proceed();
        }
        return productList;
    }
}
