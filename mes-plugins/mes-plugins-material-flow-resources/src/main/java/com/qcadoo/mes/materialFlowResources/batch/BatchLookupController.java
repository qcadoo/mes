package com.qcadoo.mes.materialFlowResources.batch;

import com.qcadoo.mes.basic.controllers.BasicLookupController;
import com.qcadoo.mes.materialFlowResources.BatchDTO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "batch")
public class BatchLookupController extends BasicLookupController<BatchDTO> {

    @Override
    protected String getQueryForRecords(final Long context) {
        String query = "SELECT %s FROM ( SELECT _batch.id, _batch.number as number, p.number as product, _company.name as supplier \n" +
                "FROM advancedgenealogy_batch _batch " +
                "LEFT JOIN basic_product p on p.id = _batch.product_id "+
                "LEFT JOIN basic_company _company on _company.id = _batch.supplier_id " +
                "WHERE p.number = :product AND _batch.active=true %s) q";

        return query;
    }

    @Override
    protected Map<String, Object> getQueryParameters(Long context, BatchDTO batchDTO) {
        Map<String, Object> params = new HashMap<>();
        params.put("product", batchDTO.getProduct());
        batchDTO.setProduct(null);
        return params;
    }

    @Override
    protected List<String> getGridFields() {
        return Arrays.asList("number", "supplier");
    }

    @Override
    protected String getRecordName() {
        return "batch";
    }
}
