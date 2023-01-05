package com.qcadoo.mes.basic.criteriaModifiers;

import com.qcadoo.mes.basic.constants.AttributeValueFields;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;

@Service
public class AttributeValueCriteriaModifiers {

    public static final String L_ATTRIBUTE_ID = "attributeId";

    public void filter(final SearchCriteriaBuilder scb, final FilterValueHolder filter) {
        if (filter.has(L_ATTRIBUTE_ID)) {
            scb.createAlias(AttributeValueFields.ATTRIBUTE, AttributeValueFields.ATTRIBUTE, JoinType.LEFT);

            scb.add(SearchRestrictions.eq(AttributeValueFields.ATTRIBUTE + ".id", filter.getLong(L_ATTRIBUTE_ID)));
        } else {
            scb.add(SearchRestrictions.idEq(-1L));
        }
    }

}
