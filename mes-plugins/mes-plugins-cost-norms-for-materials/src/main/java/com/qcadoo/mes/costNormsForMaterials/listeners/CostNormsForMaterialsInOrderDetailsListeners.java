package com.qcadoo.mes.costNormsForMaterials.listeners;

import com.qcadoo.model.api.search.SearchCriterion;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.qcadoo.model.api.search.SearchRestrictions.in;
import static com.qcadoo.model.api.search.SearchRestrictions.not;

@Service
public class CostNormsForMaterialsInOrderDetailsListeners {

    private static final String PRODUCT_NUMBER = "productNumber";

    private static final String ORDER_ID = "orderId";

    private static final String DOCUMENT_ID = "documentId";

    private static final String DOCUMENT_TYPE = "documentType";

    public static final String SUB_ORDER_IDS = "subOrderIds";

    public static final String SUB_ORDER_ID = "subOrderId";

    public void showPositionsForProduct(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {

        if (filterValue.has(SUB_ORDER_IDS)) {
            List<Long> subOrderIds = filterValue.getListOfLongs(SUB_ORDER_IDS);
            if(Objects.nonNull(subOrderIds) && !subOrderIds.isEmpty()) {
                scb.add(SearchRestrictions.eq(PRODUCT_NUMBER, filterValue.getString(PRODUCT_NUMBER)));

                SearchCriterion release = SearchRestrictions.and(SearchRestrictions.eq(DOCUMENT_TYPE, DocumentType.RELEASE.getStringValue()), in(SUB_ORDER_ID, subOrderIds.stream().map(Long::intValue).collect(Collectors.toList())));
                SearchCriterion intOut = SearchRestrictions.and(SearchRestrictions.eq(ORDER_ID, filterValue.getInteger(ORDER_ID)), SearchRestrictions.eq(DOCUMENT_TYPE, DocumentType.INTERNAL_OUTBOUND.getStringValue()));

                scb.add(SearchRestrictions.or(release, intOut));
            } else {
                appendBaseCriteria(scb, filterValue);
            }
        } else {
            appendBaseCriteria(scb, filterValue);
        }

    }

    private void appendBaseCriteria(SearchCriteriaBuilder scb, FilterValueHolder filterValue) {
        if(filterValue.has(PRODUCT_NUMBER) && filterValue.has(ORDER_ID)) {
            scb.add(SearchRestrictions.eq(PRODUCT_NUMBER, filterValue.getString(PRODUCT_NUMBER)));
            scb.add(SearchRestrictions.eq(ORDER_ID, filterValue.getInteger(ORDER_ID)));
            scb.add(SearchRestrictions.eq(DOCUMENT_TYPE, DocumentType.INTERNAL_OUTBOUND.getStringValue()));
        }else {
            scb.add(SearchRestrictions.eq(DOCUMENT_ID, 0));
        }
    }

}
