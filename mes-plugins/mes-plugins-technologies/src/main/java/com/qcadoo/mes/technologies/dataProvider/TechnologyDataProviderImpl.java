package com.qcadoo.mes.technologies.dataProvider;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
final class TechnologyDataProviderImpl implements TechnologyDataProvider {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Optional<Entity> tryFind(final Long id) {
        return Optional.ofNullable(id).map(i -> getDataDefinition().get(i));
    }

    private DataDefinition getDataDefinition() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }

}
