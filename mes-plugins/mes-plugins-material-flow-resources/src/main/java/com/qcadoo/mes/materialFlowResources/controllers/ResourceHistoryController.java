package com.qcadoo.mes.materialFlowResources.controllers;

import com.qcadoo.mes.basic.controllers.dataProvider.dto.ColumnDTO;
import com.qcadoo.mes.basic.services.ExportToCsvServiceB;
import com.qcadoo.mes.materialFlowResources.controllers.dataProvider.ResourceHistoryDataProvider;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/resourceHistory")
public class ResourceHistoryController {

    @Autowired
    private ResourceHistoryDataProvider resourceHistoryDataProvider;

    @Autowired
    private ExportToCsvServiceB exportToCsvServiceB;

    @ResponseBody
    @RequestMapping(value = "/columns", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ColumnDTO> getColumns(final Locale locale) {
        return resourceHistoryDataProvider.getColumns(locale);
    }

    @ResponseBody
    @RequestMapping(value = "/validate", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String validate(@RequestParam String number) throws ParseException {
        return resourceHistoryDataProvider.validate(number);
    }

    @ResponseBody
    @RequestMapping(value = "/records", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getRecords(@RequestParam String number) {
        try {
            return resourceHistoryDataProvider.getRecords(number, null, new JSONObject(), "",
                    false);
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/exportToCsv", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object exportToCsv(@RequestBody final JSONObject data, final Locale locale) {
        return exportToCsvServiceB.prepareJsonForCsv(resourceHistoryDataProvider, "resourceHistory", data, locale);
    }

}
