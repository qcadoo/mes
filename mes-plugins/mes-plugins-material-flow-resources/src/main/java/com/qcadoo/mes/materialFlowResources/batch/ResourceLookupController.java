package com.qcadoo.mes.materialFlowResources.batch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.qcadoo.mes.basic.BasicLookupController;
import com.qcadoo.mes.materialFlowResources.ResourceDTO;

@Controller
@RequestMapping(value = "resource")
public class ResourceLookupController extends BasicLookupController<ResourceDTO> {

    @Override
    protected String getQueryForRecords() {
        return "select %s from (select r.*, sl.number as storageLocation, pn.number as palletNumber "
                + "FROM materialflowresources_resource r "
                + "LEFT JOIN materialflowresources_storagelocation sl on sl.id = storageLocation_id "
                + "LEFT JOIN basic_palletnumber pn on pn.id = palletnumber_id WHERE r.product_id =\n"
                + "(SELECT id FROM basic_product WHERE number = :product) and expirationdate = \n"
                + "(select min(expirationdate) from materialflowresources_resource WHERE product_id =\n"
                + "(SELECT id FROM basic_product WHERE number = :product))) as resources";
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
