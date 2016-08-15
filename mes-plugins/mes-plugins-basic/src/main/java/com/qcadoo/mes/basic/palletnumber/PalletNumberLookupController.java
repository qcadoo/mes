package com.qcadoo.mes.basic.palletnumber;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.qcadoo.mes.basic.BasicLookupController;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.PalletNumberDTO;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping(value = "palletNumber")
public class PalletNumberLookupController extends BasicLookupController<PalletNumberDTO> {

    @Override
    protected String getQueryForRecords(final Long context) {
        String query = "SELECT %s FROM (SELECT palletnumber.id as id, palletnumber.number as code, palletnumber.number as number "
                + "FROM basic_palletnumber palletnumber WHERE palletnumber.active = true %s) q";

        return query;
    }

    @Override
    protected List<String> getGridFields() {
        return Arrays.asList("number");
    }

    @Override
    protected String getRecordName() {
        return "palletNumber";
    }

}
