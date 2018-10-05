package com.qcadoo.mes.technologiesGenerator.dataProvider;

import com.qcadoo.mes.technologiesGenerator.constants.GeneratorTreeNodeFields;
import com.qcadoo.mes.technologiesGenerator.constants.TechnologiesGeneratorConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TechnologyStructureNodeDataProvider {

    private static final String CUSTOMIZED_COMPONENT = "customizedComponent";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Optional<List<Entity>> getCastumizedNodesForContext(final Entity context){

        List<Entity> nodes = getTechnologyStructureNodeDD().find()
                .add(SearchRestrictions.belongsTo(GeneratorTreeNodeFields.GENERATOR_CONTEXT, context))
                .add(SearchRestrictions.eq(GeneratorTreeNodeFields.ENTITY_TYPE, CUSTOMIZED_COMPONENT))
                .list().getEntities();

        return Optional.ofNullable(nodes);
    }

    public DataDefinition getTechnologyStructureNodeDD() {
        return dataDefinitionService.get(TechnologiesGeneratorConstants.PLUGIN_IDENTIFIER,
                TechnologiesGeneratorConstants.MODEL_GENERATOR_TREE_NODE);
    }
}
