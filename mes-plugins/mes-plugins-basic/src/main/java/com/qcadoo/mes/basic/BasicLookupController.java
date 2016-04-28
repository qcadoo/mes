package com.qcadoo.mes.basic;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.security.api.SecurityService;

public abstract class BasicLookupController<R> {

    @Autowired
    private LookupUtils lookupUtils;

    @Autowired
    protected TranslationService translationService;

    @Autowired
    protected SecurityService securityService;

    @Value("${useCompressedStaticResources}")
    protected boolean useCompressedStaticResources;

    protected ModelAndView getModelAndView(final String recordName, final String view, final Locale locale) {
        ModelAndView mav = new ModelAndView();

        mav.addObject("userLogin", securityService.getCurrentUserName());
        mav.addObject("translationsMap", translationService.getMessagesGroup("documentGrid", locale));
        mav.addObject("recordName", recordName);

        mav.setViewName("basic/" + view);
        mav.addObject("useCompressedStaticResources", useCompressedStaticResources);
        return mav;
    }

    protected Map<String, Object> getConfigMap(List<String> columns) {
        Map<String, Object> config = new HashMap<>();

        Map<String, Object> modelId = new HashMap<>();
        modelId.put("name", "id");
        modelId.put("index", "id");
        modelId.put("key", true);
        modelId.put("hidden", true);

        Map<String, Map<String, Object>> colModel = new LinkedHashMap<>();
        colModel.put("ID", modelId);

        columns.forEach(column -> {
            Map<String, Object> model = new HashMap<>();
            model.put("name", column);
            model.put("index", column);
            model.put("editable", false);
            
            Map<String, Object> editoptions = new HashMap<>();
            editoptions.put("readonly", "readonly");
            model.put("editoptions", editoptions);
            
            Map<String, Object> searchoptions = new HashMap<>();
            model.put("searchoptions", searchoptions);

            colModel.put(column, model);
        });

        config.put("colModel", colModel.values());
        config.put("colNames", colModel.keySet());

        return config;
    }

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
        ModelAndView mav = getModelAndView(getRecordName(), "genericLookup", locale);

        return mav;
    }

    protected abstract List<String> getGridFields();

    protected abstract String getRecordName();

    protected abstract String getQueryForRecords(final Long context);
}
