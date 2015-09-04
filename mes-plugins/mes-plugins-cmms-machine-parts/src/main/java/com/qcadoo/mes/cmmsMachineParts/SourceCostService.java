package com.qcadoo.mes.cmmsMachineParts;

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.SourceCostFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service public class SourceCostService {

    @Autowired private DataDefinitionService dataDefinitionService;

    public DataDefinition getSourceCostDD() {
        return dataDefinitionService
                .get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_SOURCE_COST);
    }

    public Optional<Entity> findDefaultSourceCodeForFactory(final Entity factory){
        SearchCriteriaBuilder scb = getSourceCostDD().find();
        scb.add(SearchRestrictions.belongsTo(SourceCostFields.FACTORY, factory))
                .add(SearchRestrictions.eq(SourceCostFields.ACTIVE, true))
                .add(SearchRestrictions.eq(SourceCostFields.DEFAULT_COST, true));
        return Optional.ofNullable(scb.setMaxResults(1).uniqueResult());
    }

    public Optional<Entity> findDefaultSourceCode(){
        SearchCriteriaBuilder scb = getSourceCostDD().find();
        scb.add(SearchRestrictions.isNull(SourceCostFields.FACTORY))
                .add(SearchRestrictions.eq(SourceCostFields.ACTIVE, true))
                .add(SearchRestrictions.eq(SourceCostFields.DEFAULT_COST, true));
        return Optional.ofNullable(scb.setMaxResults(1).uniqueResult());
    }
}
