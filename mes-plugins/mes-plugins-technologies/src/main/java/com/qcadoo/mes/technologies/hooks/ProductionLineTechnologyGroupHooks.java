package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
public class ProductionLineTechnologyGroupHooks {

    private static final String L_PRODUCTION_LINE = "productionLine";

    private static final String L_TECHNOLOGY_GROUP = "technologyGroup";

    public boolean validatesWith(final DataDefinition productionLineTechnologyGroupDD, final Entity productionLineTechnologyGroup) {
        boolean isValid = true;
        isValid = isValid && checkIfIsUnique(productionLineTechnologyGroupDD, productionLineTechnologyGroup);
        return isValid;
    }

    private boolean checkIfIsUnique(DataDefinition productionLineTechnologyGroupDD, Entity productionLineTechnologyGroup) {
        SearchCriteriaBuilder scb = productionLineTechnologyGroupDD.find().add(
                SearchRestrictions.belongsTo(L_PRODUCTION_LINE,
                        productionLineTechnologyGroup.getBelongsToField(L_PRODUCTION_LINE)));

        if (Objects.nonNull(productionLineTechnologyGroup.getBelongsToField(L_TECHNOLOGY_GROUP))) {
            scb.add(SearchRestrictions.belongsTo(L_TECHNOLOGY_GROUP,
                    productionLineTechnologyGroup.getBelongsToField(L_TECHNOLOGY_GROUP)));
        } else {
            scb.add(SearchRestrictions.isNull(L_TECHNOLOGY_GROUP));
        }

        if (Objects.nonNull(productionLineTechnologyGroup.getId())) {
            scb.add(SearchRestrictions.idNe(productionLineTechnologyGroup.getId()));
        }

        boolean empty = scb.list().getEntities().isEmpty();

        if(!empty) {
            productionLineTechnologyGroup.addGlobalError("technologies.productionLineTechnologyGroup.error.notUnique");
        }

        return empty;
    }
}
