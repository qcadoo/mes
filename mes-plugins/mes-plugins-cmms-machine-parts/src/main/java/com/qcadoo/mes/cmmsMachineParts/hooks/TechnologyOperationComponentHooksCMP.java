package com.qcadoo.mes.cmmsMachineParts.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TechnologyOperationComponentHooksCMP {

    public static final String L_TECHNOLOGY_OPERATION_TOOL = "technologyOperationTool";
    @Autowired
    private DataDefinitionService dataDefinitionService;

    public static final String L_TOOLS = "tools";

    public void onCreate(final DataDefinition technologyOperationComponentDD, final Entity technologyOperationComponent) {
        Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

        if (Objects.nonNull(operation)) {
            List<Entity> technologyOperationTools = Lists.newArrayList();
            List<Entity> operationTools = operation.getHasManyField(L_TOOLS);
            for (Entity operationTool : operationTools) {

                Entity toolTechnologyOperationComponent = dataDefinitionService.get("cmmsMachineParts", L_TECHNOLOGY_OPERATION_TOOL).create();

                toolTechnologyOperationComponent.setField("toolCategory", operationTool.getField("toolCategory"));
                toolTechnologyOperationComponent.setField("tool", operationTool.getField("tool"));
                toolTechnologyOperationComponent.setField("description", operationTool.getField("description"));
                toolTechnologyOperationComponent.setField("quantity", operationTool.getField("quantity"));
                toolTechnologyOperationComponent.setField("unit", operationTool.getField("unit"));

                technologyOperationTools.add(toolTechnologyOperationComponent);

            }

            technologyOperationComponent.setField(L_TOOLS, technologyOperationTools);
        }
    }
}