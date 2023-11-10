package com.qcadoo.mes.materialFlowResources.storagelocation;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import com.qcadoo.mes.basic.controllers.BasicLookupController;
import com.qcadoo.mes.materialFlowResources.StorageLocationDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "storageLocation")
public class StorageLocationLookupController extends BasicLookupController<StorageLocationDTO> {

    @Override
    protected String getQueryForRecords(final Long context) {
        String query = "SELECT %s FROM (SELECT sl.id, sl.number AS number, l.name AS location "
                + "FROM materialflowresources_storagelocation sl "
                + "JOIN materialflow_location l ON l.id = sl.location_id "
                + "WHERE l.id IN "
                + "(SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) FROM materialflowresources_document WHERE id = :document) "
                + "AND sl.active = true %s) q";

        return query;
    }

    @Override
    protected Map<String, Object> getQueryParameters(final Long context, final StorageLocationDTO storageLocationDTO) {
        Map<String, Object> params = Maps.newHashMap();

        params.put("document", Integer.valueOf(storageLocationDTO.getLocation()));
        params.put("product", storageLocationDTO.getProduct());

        storageLocationDTO.setLocation(null);
        storageLocationDTO.setProduct(null);

        return params;
    }

    @Override
    protected List<String> getGridFields() {
        return Lists.newArrayList("number", "location");
    }

    @Override
    protected String getRecordName() {
        return "storageLocation";
    }

}
