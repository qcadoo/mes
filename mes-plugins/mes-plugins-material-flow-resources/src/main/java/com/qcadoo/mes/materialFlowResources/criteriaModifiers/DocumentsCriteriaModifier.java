package com.qcadoo.mes.materialFlowResources.criteriaModifiers;


import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class DocumentsCriteriaModifier {

    private static final String L_ORDER = "order";

    public void hideDraftDocumentsWithOrder(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (!filterValue.has(L_ORDER)) {
            return;
        }

        scb.add(SearchRestrictions.not(SearchRestrictions.and(SearchRestrictions.isNotNull(L_ORDER),
                SearchRestrictions.eq(DocumentFields.STATE, DocumentState.DRAFT.getStringValue()))));
    }
}
