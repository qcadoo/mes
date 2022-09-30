package com.qcadoo.mes.basic.services;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.basic.controllers.dataProvider.dto.ColumnDTO;

public interface AnalysisDataProvider {

    List<ColumnDTO> getColumns(final Locale locale);

    List<Map<String, Object>> getRecords(final String dateFrom, final String dateTo, final JSONObject filters,
                                         final String sortColumn, final boolean sortAsc) throws JSONException;

}
