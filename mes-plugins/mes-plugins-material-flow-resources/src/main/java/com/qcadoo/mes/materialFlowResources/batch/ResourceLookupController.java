package com.qcadoo.mes.materialFlowResources.batch;

import com.qcadoo.mes.basic.BasicLookupController;
import com.qcadoo.mes.basic.GridResponse;
import com.qcadoo.mes.basic.LookupUtils;
import com.qcadoo.mes.materialFlowResources.DocumentPositionService;
import com.qcadoo.mes.materialFlowResources.ResourceDTO;
import com.qcadoo.mes.materialFlowResources.WarehouseMethodOfDisposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "resource")
public class ResourceLookupController extends BasicLookupController<ResourceDTO> {

    @Autowired
    private DocumentPositionService documentPositionService;

    @Autowired
    private WarehouseMethodOfDisposalService warehouseMethodOfDisposalService;

    @Autowired
    private LookupUtils lookupUtils;

    @Override
    @ResponseBody
    @RequestMapping(value = "records", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public GridResponse<ResourceDTO> getRecords(@RequestParam String sidx, @RequestParam String sord,
            @RequestParam(defaultValue = "1", required = false, value = "page") Integer page,
            @RequestParam(value = "rows") int perPage,
            @RequestParam(defaultValue = "0", required = false, value = "context") Long context, ResourceDTO record) {

        return getResponse(sidx, sord, page, perPage, record, context);

    }

    private GridResponse<ResourceDTO> getResponse(String sidx, String sord, Integer page, int perPage, ResourceDTO record,
            Long context) {
        String additionalCode = record.getAc();
        boolean useAdditionalCode = org.apache.commons.lang3.StringUtils.isNotEmpty(additionalCode);
        Map<String, Object> parameters = geParameters(context, record, useAdditionalCode, additionalCode);

        String query = getQuery(context, useAdditionalCode,
                documentPositionService.addMethodOfDisposalCondition(context, parameters, false, useAdditionalCode));
        GridResponse<ResourceDTO> response = lookupUtils.getGridResponse(query, sidx, sord, page, perPage, record, parameters);

        if (response.getRows().isEmpty() && useAdditionalCode) {
            parameters = geParameters(context, record, false, additionalCode);
            query = getQuery(context, false,
                    documentPositionService.addMethodOfDisposalCondition(context, parameters, false, false));
            response = lookupUtils.getGridResponse(query, sidx, sord, page, perPage, record, parameters);
        }
        return response;
    }

    protected String getQuery(final Long context, boolean useAdditionalCode, boolean addMethodOfDisposal) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("select %s from (select r.*, sl.number as storageLocation, pn.number as palletNumber, ac.code as additionalCode ");
        queryBuilder.append("FROM materialflowresources_resource r ");
        queryBuilder.append("LEFT JOIN materialflowresources_storagelocation sl on sl.id = storageLocation_id ");
        queryBuilder.append("LEFT JOIN basic_additionalcode ac on ac.id = additionalcode_id ");
        queryBuilder.append("LEFT JOIN basic_palletnumber pn on pn.id = palletnumber_id WHERE r.product_id = ");
        queryBuilder.append("(SELECT id FROM basic_product WHERE number = :product)");
        queryBuilder
                .append(" AND r.location_id in (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) as location from materialflowresources_document WHERE id = :context)");
        queryBuilder.append(" AND r.conversion = :conversion ");
        if (useAdditionalCode) {
            queryBuilder.append(" AND additionalcode_id = (SELECT id FROM basic_additionalcode WHERE code = :add_code) ");
        }
        if (addMethodOfDisposal) {
            queryBuilder.append(" AND ");
            queryBuilder.append(warehouseMethodOfDisposalService.getSqlConditionForResourceLookup(context));
            queryBuilder.append(" WHERE product_id = (SELECT id FROM basic_product WHERE number = :product)");
            queryBuilder
                    .append(" and location_id in (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) as location from materialflowresources_document WHERE id = :context)");
            queryBuilder.append(" AND conversion = :conversion");
            if (useAdditionalCode) {
                queryBuilder.append(" AND additionalcode_id = (SELECT id FROM basic_additionalcode WHERE code = :add_code) ");
            }
            queryBuilder.append(" )");
        }
        queryBuilder.append(") as resources");
        return queryBuilder.toString();
    }

    protected Map<String, Object> geParameters(Long context, ResourceDTO resourceDTO, boolean useAdditionalCode,
            String additionalCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("product", resourceDTO.getProduct());
        params.put("conversion", resourceDTO.getConversion());
        params.put("context", context);

        if (useAdditionalCode) {
            params.put("add_code", additionalCode);
        }

        resourceDTO.setProduct(null);
        resourceDTO.setConversion(null);
        resourceDTO.setAc(null);

        return params;
    }

    @Override
    protected List<String> getGridFields() {
        return Arrays.asList(new String[] { "number", "batch", "quantity", "givenUnit", "expirationDate", "storageLocation",
                "palletNumber", "additionalCode" });
    }

    @Override
    protected String getRecordName() {
        return "resource";
    }

    @Override
    protected String getQueryForRecords(Long context) {
        return null;
    }

}
