package com.qcadoo.mes.materialFlowResources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentPositionResourcesHelper {

    @Autowired
    private WarehouseMethodOfDisposalService warehouseMethodOfDisposalService;

    public String getResourceQuery(final Long document, boolean query, boolean useAdditionalCode, boolean useBatch) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select r.number, batch.id as batchId, batch.number as batch, r.availablequantity from materialflowresources_resource r LEFT JOIN advancedgenealogy_batch batch ON batch.id = r.batch_id ");
        appendWhereClause(queryBuilder, query, useAdditionalCode, useBatch);
        queryBuilder.append(warehouseMethodOfDisposalService.getSqlOrderByForResource(document));
        return queryBuilder.toString();
    }

    public String getMethodOfDisposalQuery(final Long document, boolean query, boolean useAdditionalCode) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(warehouseMethodOfDisposalService.getSqlConditionForResource(document));
        appendWhereClause(queryBuilder, query, useAdditionalCode, false);
        return queryBuilder.toString();
    }

    private void appendWhereClause(StringBuilder queryBuilder, boolean query, boolean useAdditionalCode, boolean useBatch) {
        queryBuilder.append(" WHERE r.conversion = :conversion ");
        queryBuilder.append(" AND r.product_id = (SELECT id FROM basic_product WHERE number = :product)");
        queryBuilder.append(
                " AND r.location_id in (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) as location from materialflowresources_document WHERE id = :context)");
        if (query) {
            queryBuilder.append(" AND r.number ilike :query ");
        }
        if (useAdditionalCode) {
            queryBuilder.append(" AND r.additionalcode_id = (SELECT id FROM basic_additionalcode WHERE code = :add_code) ");
        }

        if (useBatch) {
            queryBuilder.append(" AND batch.id = :batch ");
        }
    }
}
