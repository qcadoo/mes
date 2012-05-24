package com.qcadoo.mes.lineChangeoverNorms.constants;

import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY_GROUP;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.PRODUCTION_LINE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY_GROUP;

import java.util.Arrays;
import java.util.List;

public class LineChangeoverNormsConstants {

    public static final String PLUGIN_IDENTIFIER = "lineChangeoverNorms";

    public static final String MODEL_LINE_CHANGEOVER_NORMS = "lineChangeoverNorms";

    public static final List<String> FIELDS_ENTITY = Arrays.asList(FROM_TECHNOLOGY, TO_TECHNOLOGY, FROM_TECHNOLOGY_GROUP,
            TO_TECHNOLOGY_GROUP, PRODUCTION_LINE);
}
