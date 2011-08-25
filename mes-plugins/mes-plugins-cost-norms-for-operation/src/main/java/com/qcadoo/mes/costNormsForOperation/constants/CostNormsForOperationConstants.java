package com.qcadoo.mes.costNormsForOperation.constants;

import java.util.Arrays;

public interface CostNormsForOperationConstants {
	public static final String PLUGIN_IDENTIFIER = "costNorms";
	public static final Iterable<String> FIELDS = Arrays.asList("pieceworkCost", "numberOfOperations", "laborHourlyCost",
    "machineHourlyCost");
}
