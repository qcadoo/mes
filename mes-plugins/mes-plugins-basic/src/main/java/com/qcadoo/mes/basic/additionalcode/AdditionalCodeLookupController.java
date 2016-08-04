package com.qcadoo.mes.basic.additionalcode;

import com.qcadoo.mes.basic.BasicLookupController;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.AdditionalCodeDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "additionalCode")
public class AdditionalCodeLookupController extends BasicLookupController<AdditionalCodeDTO> {

    @Override
    protected String getQueryForRecords(final Long context) {
        String query = "SELECT %s FROM (SELECT additionalcode.id as id, additionalcode.code as code, product.number as productnumber "
                + "FROM basic_additionalcode additionalcode "
                + "JOIN basic_product product ON (additionalcode.product_id = product.id) WHERE (product.number = :productnumber OR COALESCE(:productnumber,'')='' ) %s) q";

        return query;
    }

    @Override
    protected List<String> getGridFields() {
        return Arrays.asList("code", "productnumber");
    }

    @Override
    protected String getRecordName() {
        return "additionalCode";
    }

    @Override
    protected Map<String, Object> getQueryParameters(Long context, AdditionalCodeDTO additionalCodeDTO) {
        Map<String, Object> params = new HashMap<>();

        params.put("productnumber", additionalCodeDTO.getProductnumber());
        additionalCodeDTO.setProductnumber(null);

        return params;
    }
}
