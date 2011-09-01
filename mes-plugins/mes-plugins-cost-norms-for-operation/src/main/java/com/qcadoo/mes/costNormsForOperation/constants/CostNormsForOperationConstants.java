package com.qcadoo.mes.costNormsForOperation.constants;

import java.util.Arrays;
import java.util.List;

public interface CostNormsForOperationConstants {

    public static final String PLUGIN_IDENTIFIER = "costNorms";

    public static final List<String> FIELDS = Arrays.asList("pieceworkCost", "numberOfOperations", "laborHourlyCost",
            "machineHourlyCost");
}
