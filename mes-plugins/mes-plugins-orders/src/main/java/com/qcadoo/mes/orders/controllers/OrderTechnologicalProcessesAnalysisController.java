package com.qcadoo.mes.orders.controllers;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ColumnDTO;
import com.qcadoo.mes.orders.controllers.dataProvider.OrderTechnologicalProcessesAnalysisDataProvider;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.plugins.qcadooExport.api.ExportToCsv;

@Controller
@RequestMapping("/orderTechnologicalProcessesAnalysis")
public class OrderTechnologicalProcessesAnalysisController {

    @Autowired
    private OrderTechnologicalProcessesAnalysisDataProvider orderTechnologicalProcessesAnalysisDataProvider;

    @Autowired
    private ExportToCsv exportToCsv;

    @Autowired
    private FileService fileService;

    @Autowired
    private ParameterService parameterService;

    @ResponseBody
    @RequestMapping(value = "/columns", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ColumnDTO> getColumns(final Locale locale) {
        return orderTechnologicalProcessesAnalysisDataProvider.getColumns(locale);
    }

    @ResponseBody
    @RequestMapping(value = "/validate", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String validate(@RequestParam String dateFrom, @RequestParam String dateTo) {
        return orderTechnologicalProcessesAnalysisDataProvider.validate(dateFrom, dateTo);
    }

    @ResponseBody
    @RequestMapping(value = "/records", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getRecords(@RequestParam String dateFrom, @RequestParam String dateTo) {
        try {
            return orderTechnologicalProcessesAnalysisDataProvider.getRecords(dateFrom, dateTo, new JSONObject(), "",
                    false);
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/exportToCsv", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object exportToCsv(@RequestBody final JSONObject data, final Locale locale) {
        try {
            List<String> columns = Lists.newArrayList();
            List<String> columnNames = Lists.newArrayList();

            Entity parameter = parameterService.getParameter();

            if (parameter.getBooleanField(ParameterFields.EXPORT_TO_CSV_ONLY_VISIBLE_COLUMNS)) {
                JSONArray cols = data.getJSONArray("columns");

                for (int i = 0; i < cols.length(); i++) {
                    JSONObject col = cols.getJSONObject(i);

                    String id = col.getString("id");

                    if (!"_checkbox_selector".equals(id)) {
                        columns.add(id);
                        columnNames.add(col.getString("name"));
                    }
                }
            } else {
                List<ColumnDTO> cols = orderTechnologicalProcessesAnalysisDataProvider.getColumns(locale);

                for (ColumnDTO col : cols) {
                    columns.add(col.getId());
                    columnNames.add(col.getName());
                }
            }

            JSONObject sort = data.getJSONArray("sort").getJSONObject(0);
            JSONObject filters = data.getJSONObject("filters");

            List<Map<String, Object>> records = orderTechnologicalProcessesAnalysisDataProvider.getRecords(
                    data.getString("dateFrom"), data.getString("dateTo"), filters, sort.getString("columnId"),
                    sort.getBoolean("sortAsc"));

            List<Map<String, String>> rows = Lists.newArrayList();

            records.forEach(record -> {
                Map<String, String> row = Maps.newHashMap();

                record.forEach((k, v) -> row.put(k, (Objects.isNull(v) ? null : String.valueOf(v))));

                rows.add(row);
            });

            File file = exportToCsv.createExportFile(columns, columnNames, rows, "orderTechnologicalProcessesAnalysis");

            JSONObject json = new JSONObject();

            json.put("url", fileService.getUrl(file.getAbsolutePath()) + "?clean");

            return json;
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
