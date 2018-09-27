package com.qcadoo.mes.technologiesGenerator.criteriaModifier;

import com.qcadoo.model.api.search.JoinType;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologiesGenerator.constants.TechnologiesGeneratorConstants;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class TechnologiesForProductsCM {

    public static final String A_TECHNOLOGY = "technology";

    public static final String L_GENERATOR_CONTEXT = "generatorContext";

    public static String PARAMETER = "context";

    public void showForContext(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {

        Long context = null;
        if (!filterValue.has(PARAMETER)) {
            context = 0L;
        } else {
            context = filterValue.getLong(PARAMETER);
        }
        String techAlias = A_TECHNOLOGY;

        if (!scb.existsAliasForAssociation(A_TECHNOLOGY)){
            scb.createAlias(A_TECHNOLOGY, techAlias, JoinType.LEFT);
        } else {
            techAlias = scb.getAliasForAssociation(A_TECHNOLOGY);
        }

        scb.add(SearchRestrictions
                .belongsTo(L_GENERATOR_CONTEXT, TechnologiesGeneratorConstants.PLUGIN_IDENTIFIER, L_GENERATOR_CONTEXT, context));

        scb.add(SearchRestrictions.isNotNull(L_GENERATOR_CONTEXT));
        scb.add(SearchRestrictions.eq(techAlias+".active", true));
    }
}
