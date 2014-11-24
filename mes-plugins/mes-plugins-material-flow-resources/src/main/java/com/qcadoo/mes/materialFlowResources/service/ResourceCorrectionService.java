package com.qcadoo.mes.materialFlowResources.service;

import java.math.BigDecimal;

public interface ResourceCorrectionService {

    boolean createCorrectionForResource(final Long resourceId, final BigDecimal newQuantity);
}
