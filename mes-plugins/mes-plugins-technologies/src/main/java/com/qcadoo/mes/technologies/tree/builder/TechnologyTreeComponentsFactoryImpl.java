package com.qcadoo.mes.technologies.tree.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.tree.builder.api.InternalOperationProductComponent;
import com.qcadoo.mes.technologies.tree.builder.api.InternalTechnologyOperationComponent;
import com.qcadoo.mes.technologies.tree.builder.api.OperationProductComponent;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;

@Component
public class TechnologyTreeComponentsFactoryImpl implements TechnologyTreeComponentsFactory {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public InternalTechnologyOperationComponent buildToc() {
        return new TechnologyOperationCompImpl(getTocDataDef());
    }

    @Override
    public InternalOperationProductComponent buildOpc(final OperationProductComponent.OperationCompType type) {
        return new OperationProductComponentImpl(type, getOpcDataDef(type));
    }

    private DataDefinition getOpcDataDef(final OperationProductComponent.OperationCompType type) {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, type.getModelName());
    }

    private DataDefinition getTocDataDef() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
    }

}
