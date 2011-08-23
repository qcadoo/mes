package com.qcadoo.mes.costNormsForOperation;

import java.math.BigDecimal;
import java.util.HashMap;

import com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants;
import com.qcadoo.model.api.Entity;

public interface OperationsCostCalculationService {

    public HashMap<String, BigDecimal> calculateOperationsCost(final Entity source,
            final OperationsCostCalculationConstants mode, final boolean includeTPZs, final BigDecimal quantity);
}
