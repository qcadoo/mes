package com.qcadoo.mes.basic.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.basic.GridResponse;
import com.qcadoo.mes.basic.LookupUtils;


public abstract class BasicLookupController<R> {

    @Autowired
    private LookupUtils lookupUtils;

    @ResponseBody
    @RequestMapping(value = "records", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)    
    public GridResponse<R> getRecords(@RequestParam String sidx, @RequestParam String sord,
									  @RequestParam(defaultValue = "1", required = false, value = "page") Integer page,
									  @RequestParam(value = "rows") int perPage,
									  @RequestParam(defaultValue = "0", required = false, value = "context") Long context,
									  R record) {

        String query = getQueryForRecords(context);

        return lookupUtils.getGridResponse(query, sidx, sord, page, perPage, record, getQueryParameters(context, record));
    }

    protected Map<String, Object> getQueryParameters(Long context, R record) {
        return new HashMap<>();
    }

    @ResponseBody
    @RequestMapping(value = "config", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getConfigView(Locale locale) {
        return getConfigMap(getGridFields());
    }

    @RequestMapping(value = "lookup", method = RequestMethod.GET)
    public ModelAndView getLookupView(Map<String, String> arguments, Locale locale) {
        return lookupUtils.getModelAndView(getRecordName(), "genericLookup", locale);
    }

    protected Map<String, Object> getConfigMap(List<String> columns){
        return lookupUtils.getConfigMap(columns);
    }
    protected abstract List<String> getGridFields();

    protected abstract String getRecordName();

    protected abstract String getQueryForRecords(final Long context);
}
