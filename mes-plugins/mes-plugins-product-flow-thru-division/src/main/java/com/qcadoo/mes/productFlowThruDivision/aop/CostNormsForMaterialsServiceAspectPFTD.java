package com.qcadoo.mes.productFlowThruDivision.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.states.ProductionTrackingListenerServicePFTD;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER)
public class CostNormsForMaterialsServiceAspectPFTD {

    @Autowired
    private ProductionTrackingListenerServicePFTD productionTrackingListenerServicePFTD;

    @AfterReturning(pointcut = "execution(private com.qcadoo.model.api.Entity com.qcadoo.mes.costNormsForMaterials.CostNormsForMaterialsService.updateCostsInOrder(com.qcadoo.model.api.Entity))", returning = "order")
    public void afterUpdateCostsInOrder(final JoinPoint jp, final Entity order) throws Throwable {
        productionTrackingListenerServicePFTD.updateCostsForOrder(order);
    }

}
