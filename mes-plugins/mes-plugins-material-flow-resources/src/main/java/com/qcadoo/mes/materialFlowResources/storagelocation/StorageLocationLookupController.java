package com.qcadoo.mes.materialFlowResources.storagelocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.qcadoo.mes.basic.BasicLookupController;
import com.qcadoo.mes.materialFlowResources.StorageLocationDTO;

@Controller
@RequestMapping(value = "storageLocation")
public class StorageLocationLookupController extends BasicLookupController<StorageLocationDTO> {

    @Override
    protected String getQueryForRecords() {
        String query = "SELECT %s FROM ( SELECT sl.id, sl.number as number, p.name as product, loc.name as location\n" + 
 "FROM materialflowresources_storagelocation sl LEFT JOIN basic_product p on p.id = sl.product_id\n"
                +
                "JOIN materialflow_location loc on loc.id = sl.location_id " +
                "WHERE loc.id IN (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) FROM materialflowresources_document where id = :document)"+ 
 "AND (CASE WHEN :product != '' THEN p.name = :product OR sl.product_id IS NULL ELSE true END)) q";

        return query;
    }

    @Override
    protected Map<String, Object> getQueryParameters(StorageLocationDTO storageLocationDTO) {
        Map<String, Object> params = new HashMap<>();
        params.put("document", Integer.valueOf(storageLocationDTO.getLocation()));
        params.put("product", storageLocationDTO.getProduct());
        storageLocationDTO.setLocation(null);
        storageLocationDTO.setProduct(null);
        return params;
    }

    @Override
    protected List<String> getGridFields() {
        return Arrays.asList("number", "product", "location");
    }

    @Override
    protected String getRecordName() {
        return "storageLocation";
    }
}
