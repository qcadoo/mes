package com.qcadoo.mes.costNormsForMaterials.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class CostNormsForMaterialsInOrderDetailsListeners {

    private static final String PRODUCT_NUMBER = "productNumber";

    private static final String ORDER_ID = "orderId";

    private static final String DOCUMENT_ID = "documentId";

    private static final String DOCUMENT_TYPE = "documentType";

    public void showPositionsForProduct(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(PRODUCT_NUMBER) && filterValue.has(ORDER_ID)) {
            scb.add(SearchRestrictions.eq(PRODUCT_NUMBER, filterValue.getString(PRODUCT_NUMBER)));
            scb.add(SearchRestrictions.eq(ORDER_ID, filterValue.getInteger(ORDER_ID)));
            scb.add(SearchRestrictions.eq(DOCUMENT_TYPE, DocumentType.INTERNAL_OUTBOUND.getStringValue()));
        } else {
            scb.add(SearchRestrictions.eq(DOCUMENT_ID, 0L));
        }
    }

}
