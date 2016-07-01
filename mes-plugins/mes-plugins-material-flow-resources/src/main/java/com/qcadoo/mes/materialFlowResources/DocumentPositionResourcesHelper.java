package com.qcadoo.mes.materialFlowResources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentPositionResourcesHelper {

    @Autowired
    private WarehouseMethodOfDisposalService warehouseMethodOfDisposalService;

    public String getResourceQuery(final Long document, boolean query, boolean addMethodOfDisposal, boolean useAdditionalCode) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select number, batch from materialflowresources_resource");
        appendWhereClause(queryBuilder, query, useAdditionalCode);
        if (addMethodOfDisposal) {
            // queryBuilder.append(" AND ");
            // queryBuilder.append(warehouseMethodOfDisposalService.getSqlConditionForResourceLookup(document));
            // appendWhereClause(queryBuilder, query, useAdditionalCode);
            // queryBuilder.append(")");
            queryBuilder.append(warehouseMethodOfDisposalService.getSqlOrderByForResource(document));
        }

        return queryBuilder.toString();
    }

    public String getMethodOfDisposalQuery(final Long document, boolean query, boolean useAdditionalCode) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(warehouseMethodOfDisposalService.getSqlConditionForResource(document));
        appendWhereClause(queryBuilder, query, useAdditionalCode);
        return queryBuilder.toString();
    }

    private void appendWhereClause(StringBuilder queryBuilder, boolean query, boolean useAdditionalCode) {
        queryBuilder.append(" WHERE conversion = :conversion");
        queryBuilder.append(" AND product_id = (SELECT id FROM basic_product WHERE number = :product)");
        queryBuilder.append(
                " AND location_id in (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) as location from materialflowresources_document WHERE id = :context)");
        if (query) {
            queryBuilder.append(" AND number ilike :query ");
        }
        if (useAdditionalCode) {
            queryBuilder.append(" AND additionalcode_id = (SELECT id FROM basic_additionalcode WHERE code = :add_code) ");
        }
    }
}
