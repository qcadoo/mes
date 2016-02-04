package com.qcadoo.mes.materialFlowResources.storagelocation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.qcadoo.mes.basic.BasicLookupController;
import com.qcadoo.mes.materialFlowResources.StorageLocationDTO;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping(value = "storageLocation")
public class StorageLocationLookupController extends BasicLookupController<StorageLocationDTO> {

    @Override
    protected String getQueryForRecords() {
        String query = "SELECT %s FROM ( SELECT id, number from materialflowresources_storagelocation) q ";

        return query;
    }

    @Override
    protected List<String> getGridFields() {
        return Arrays.asList("number");
    }

    @Override
    protected String getRecordName() {
        return "storageLocation";
    }
}
