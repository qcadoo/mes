package com.qcadoo.mes.cmmsMachineParts.criteriaModifiers;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;

@Service
public class OperationToolCriteriaModifiers {

    public static final String TOOL_CATEGORY = "TOOL_CATEGORY";

    public void filterByCategory(final SearchCriteriaBuilder searchCriteriaBuilder,
                                            final FilterValueHolder filterValueHolder) {
        if(filterValueHolder.has(TOOL_CATEGORY)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq("toolCategory", filterValueHolder.getString(TOOL_CATEGORY)));
        } else {
            searchCriteriaBuilder.add(SearchRestrictions.eq("id", -1L));
        }

    }

}
