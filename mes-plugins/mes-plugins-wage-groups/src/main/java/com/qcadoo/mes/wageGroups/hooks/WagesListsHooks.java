package com.qcadoo.mes.wageGroups.hooks;

import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_COMPANY;
import static com.qcadoo.mes.basic.constants.BasicConstants.PLUGIN_IDENTIFIER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class WagesListsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void addDiscriminatorRestrictionToGrid(final ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        grid.setCustomRestriction(new CustomRestriction() {

            @Override
            public void addRestriction(final SearchCriteriaBuilder searchBuilder) {
                Entity ownerCompany = dataDefinitionService.get(PLUGIN_IDENTIFIER, MODEL_COMPANY).find()
                        .add(SearchRestrictions.eq("owner", true)).uniqueResult();
                searchBuilder.add(SearchRestrictions.or(SearchRestrictions.belongsTo("workFor", ownerCompany),
                        SearchRestrictions.isNotNull("laborHourlyCost")));
            }

        });
    }
}
