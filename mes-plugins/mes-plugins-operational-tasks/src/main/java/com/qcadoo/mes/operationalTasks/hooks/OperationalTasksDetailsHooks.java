package com.qcadoo.mes.operationalTasks.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class OperationalTasksDetailsHooks {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void generateOperationalTasksNumber(final ViewDefinitionState state) {
        numberGeneratorService.generateAndInsertNumber(state, OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK, "form", "number");
    }
}
