package com.qcadoo.mes.productFlowThruDivision.aop;

import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.service.ProductionCountingDocumentService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Aspect
@Configurable
@RunIfEnabled(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER)
public class CostNormsForMaterialsServiceAspectPFTD {

    @Autowired
    private ProductionCountingDocumentService productionCountingDocumentService;

    @AfterReturning(pointcut = "execution(public com.qcadoo.model.api.Entity com.qcadoo.mes.costNormsForMaterials.CostNormsForMaterialsService.updateCostsInOrder(com.qcadoo.model.api.Entity))", returning = "order")
    public void afterUpdateCostsInOrder(final JoinPoint jp, final Entity order) throws Throwable {
        productionCountingDocumentService.updateCostsForOrder(order);
    }

}
