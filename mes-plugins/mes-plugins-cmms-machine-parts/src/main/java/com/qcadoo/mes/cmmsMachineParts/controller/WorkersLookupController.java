package com.qcadoo.mes.cmmsMachineParts.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.qcadoo.mes.basic.BasicLookupController;
import com.qcadoo.mes.cmmsMachineParts.dto.WorkerDTO;

@Controller
@RequestMapping(value = "responsibleWorker")
public class WorkersLookupController extends BasicLookupController<WorkerDTO> {

    @Override
    protected String getQueryForRecords(final Long context) {
        String query = "SELECT %s FROM ( SELECT id, name  || ' ' || surname || ' - ' || number AS code FROM basic_staff %s) q";

        return query;
    }

    @Override
    protected Map<String, Object> getQueryParameters(Long context, WorkerDTO workerDTO) {
        Map<String, Object> params = new HashMap<>();
        return params;
    }

    @Override
    protected List<String> getGridFields() {
        return Arrays.asList("code");
    }

    @Override
    protected String getRecordName() {
        return "responsibleWorker";
    }
}
