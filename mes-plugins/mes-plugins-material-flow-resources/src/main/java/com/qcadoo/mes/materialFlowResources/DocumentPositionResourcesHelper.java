package com.qcadoo.mes.materialFlowResources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentPositionResourcesHelper {

    @Autowired
    private WarehouseMethodOfDisposalService warehouseMethodOfDisposalService;

    public  String getResourceQuery(final Long document, boolean query, boolean useAdditionalCode){
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select number, batch from materialflowresources_resource WHERE product_id = ");
        queryBuilder.append("(SELECT id FROM basic_product WHERE number = :product) and ");
        queryBuilder.append(warehouseMethodOfDisposalService.getSqlConditionForResourceLookup(document));
        queryBuilder.append(" WHERE product_id = ");
        if(query){
            queryBuilder.append("(SELECT id FROM basic_product WHERE number = :product)) " + "AND number ilike :query ");
        } else{
            queryBuilder.append("(SELECT id FROM basic_product WHERE number = :product))");
        }
        queryBuilder
                .append("AND location_id in (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) as location from materialflowresources_document WHERE id = :context)");
        queryBuilder.append("AND conversion = :conversion ");
        if(useAdditionalCode){
            queryBuilder.append("AND additionalcode_id = (SELECT id FROM basic_additionalcode WHERE code = :additionalCode) ");
        }
        return queryBuilder.toString();
    }
}
