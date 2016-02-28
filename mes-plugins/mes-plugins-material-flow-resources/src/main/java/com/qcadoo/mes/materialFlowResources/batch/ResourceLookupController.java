package com.qcadoo.mes.materialFlowResources.batch;

import com.qcadoo.mes.basic.BasicLookupController;
import com.qcadoo.mes.materialFlowResources.ResourceDTO;
import com.qcadoo.mes.materialFlowResources.WarehouseMethodOfDisposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "resource")
public class ResourceLookupController extends BasicLookupController<ResourceDTO> {

    @Autowired
    private WarehouseMethodOfDisposalService warehouseMethodOfDisposalService;

    @Override
    protected String getQueryForRecords(final Long context) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select %s from (select r.*, sl.number as storageLocation, pn.number as palletNumber ");
        queryBuilder.append("FROM materialflowresources_resource r ");
        queryBuilder.append("LEFT JOIN materialflowresources_storagelocation sl on sl.id = storageLocation_id ");
        queryBuilder.append("LEFT JOIN basic_palletnumber pn on pn.id = palletnumber_id WHERE r.product_id = ");
        queryBuilder.append("(SELECT id FROM basic_product WHERE number = :product) and ");
        queryBuilder.append(warehouseMethodOfDisposalService.getSqlConditionForResourceLookup(context));
        queryBuilder.append(" WHERE product_id = ");
        queryBuilder.append("(SELECT id FROM basic_product WHERE number = :product))) as resources");
        return queryBuilder.toString();
    }

    @Override
    protected Map<String, Object> getQueryParameters(ResourceDTO resourceDTO) {
        Map<String, Object> params = new HashMap<>();
        params.put("product", resourceDTO.getProduct());
        resourceDTO.setProduct(null);
        return params;
    }

    @Override
    protected List<String> getGridFields() {
        return Arrays.asList(new String[] { "number", "batch", "quantity", "givenUnit", "expirationDate", "storageLocation",
                "palletNumber" });
    }

    @Override
    protected String getRecordName() {
        return "resource";
    }

}
