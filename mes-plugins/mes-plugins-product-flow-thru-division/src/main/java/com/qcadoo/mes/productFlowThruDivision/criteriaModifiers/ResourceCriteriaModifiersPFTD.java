package com.qcadoo.mes.productFlowThruDivision.criteriaModifiers;

import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ResourceCriteriaModifiersPFTD {

    public static final String L_LOCATION_ID = "locationId";
    public static final String L_PRODUCT_ID = "productId";
    public static final String L_RESOURCES_IDS = "resourcesIds";

    public void filter(final SearchCriteriaBuilder scb, final FilterValueHolder filter) {
        if (filter.has(L_LOCATION_ID)) {
            scb.createAlias(ResourceFields.LOCATION, ResourceFields.LOCATION, JoinType.LEFT);
            scb.createAlias(ResourceFields.PRODUCT, ResourceFields.PRODUCT, JoinType.LEFT);

            scb.add(SearchRestrictions.eq(ResourceFields.LOCATION + ".id", filter.getLong(L_LOCATION_ID)));
            scb.add(SearchRestrictions.eq(ResourceFields.PRODUCT + ".id", filter.getLong(L_PRODUCT_ID)));
            scb.add(SearchRestrictions.gt(ResourceFields.AVAILABLE_QUANTITY, BigDecimal.ZERO));
            filter.getListOfLongs(L_RESOURCES_IDS).forEach(rId -> {
              scb.add(SearchRestrictions.idNe(rId));
            });

        } else {
            scb.add(SearchRestrictions.idEq(-1L));
        }
    }

}
