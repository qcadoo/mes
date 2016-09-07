package com.qcadoo.mes.materialFlowResources.batch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.BasicLookupController;
import com.qcadoo.mes.basic.GridResponse;
import com.qcadoo.mes.basic.LookupUtils;
import com.qcadoo.mes.materialFlowResources.DocumentPositionService;
import com.qcadoo.mes.materialFlowResources.ResourceDTO;
import com.qcadoo.mes.materialFlowResources.WarehouseMethodOfDisposalService;

@Controller
@RequestMapping(value = "resource")
public class ResourceLookupController extends BasicLookupController<ResourceDTO> {

    @Autowired
    private DocumentPositionService documentPositionService;

    @Autowired
    private WarehouseMethodOfDisposalService warehouseMethodOfDisposalService;

    @Autowired
    private LookupUtils lookupUtils;

    @Autowired
    private TranslationService translationService;

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

        boolean properFilter = prepareWasteFilter(record);
        if ("wasteString".equals(sidx)) {
            sidx = "waste";
        }
        String query = getQuery(context, useAdditionalCode,
                documentPositionService.addMethodOfDisposalCondition(context, parameters, false, useAdditionalCode),
                !properFilter);

        GridResponse<ResourceDTO> response = lookupUtils.getGridResponse(query, sidx, sord, page, perPage, record, parameters);

        if (response.getRows().isEmpty() && useAdditionalCode) {
            parameters = geParameters(context, record, false, additionalCode);
            query = getQuery(context, false,
                    documentPositionService.addMethodOfDisposalCondition(context, parameters, false, false), !properFilter);
            response = lookupUtils.getGridResponse(query, sidx, sord, page, perPage, record, parameters);
        }
        setTranslatedWasteFlag(response);
        return response;
    }

    protected String getQuery(final Long context, boolean useAdditionalCode, boolean addMethodOfDisposal,
            boolean wasteFilterIsWrong) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(
                "select %s from (select r.*, sl.number as storageLocation, pn.number as palletNumber, ac.code as additionalCode, bp.unit as unit ");
        queryBuilder.append("FROM materialflowresources_resource r ");
        queryBuilder.append("LEFT JOIN materialflowresources_storagelocation sl on sl.id = storageLocation_id ");
        queryBuilder.append("LEFT JOIN basic_additionalcode ac on ac.id = additionalcode_id ");
        queryBuilder.append("LEFT JOIN basic_product bp on bp.number = :product ");
        queryBuilder.append("LEFT JOIN basic_palletnumber pn on pn.id = palletnumber_id WHERE r.product_id = bp.id ");
        queryBuilder.append(
                " AND r.location_id in (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) as location from materialflowresources_document WHERE id = :context)");
        queryBuilder.append(" AND r.conversion = :conversion ");
        if (wasteFilterIsWrong) {
            queryBuilder.append(" AND waste IS NULL ");
        }
        if (useAdditionalCode) {
            // queryBuilder.append(" AND additionalcode_id = (SELECT id FROM basic_additionalcode WHERE code = :add_code) ");
        }
        if (addMethodOfDisposal) {
            // queryBuilder.append(" AND ");
            // queryBuilder.append(warehouseMethodOfDisposalService.getSqlConditionForResourceLookup(context));
            // queryBuilder.append(" WHERE product_id = (SELECT id FROM basic_product WHERE number = :product)");
            // queryBuilder
            // .append(" and location_id in (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) as location from
            // materialflowresources_document WHERE id = :context)");
            // queryBuilder.append(" AND conversion = :conversion");
            // if (useAdditionalCode) {
            // queryBuilder.append(" AND additionalcode_id = (SELECT id FROM basic_additionalcode WHERE code = :add_code) ");
            // }
            // queryBuilder.append(" )");
            queryBuilder.append(warehouseMethodOfDisposalService.getSqlOrderByForResource(context));
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
        return Arrays.asList(new String[] { "number", "quantity", "unit", "quantityInAdditionalUnit", "givenUnit",
                "expirationDate", "storageLocation", "batch", "palletNumber", "additionalCode", "wasteString" });
    }

    @Override
    protected String getRecordName() {
        return "resource";
    }

    @Override
    protected String getQueryForRecords(Long context) {
        return null;
    }

    private void setTranslatedWasteFlag(GridResponse<ResourceDTO> responce) {
        String yes = translationService.translate("documentGrid.gridColumn.wasteString.value.yes",
                LocaleContextHolder.getLocale());
        String no = translationService.translate("documentGrid.gridColumn.wasteString.value.no", LocaleContextHolder.getLocale());
        responce.getRows().forEach(resDTO -> resDTO.setWasteString(resDTO.isWaste() ? yes : no));
    }

    private boolean prepareWasteFilter(ResourceDTO record) {
        String yes = translationService.translate("documentGrid.gridColumn.wasteString.value.yes",
                LocaleContextHolder.getLocale()).toLowerCase();
        String no = translationService.translate("documentGrid.gridColumn.wasteString.value.no", LocaleContextHolder.getLocale())
                .toLowerCase();
        String filter = record.getWasteString();
        record.setWasteString(null);
        if (filter != null) {
            filter = filter.toLowerCase();
        }
        if (yes.equals(filter)) {
            record.setWaste(true);
        } else if (no.equals(filter)) {
            record.setWaste(false);
        } else if (!StringUtils.isEmpty(filter)) {
            return false;
        } else {
            record.setWaste(null);
        }
        return true;
    }
}
