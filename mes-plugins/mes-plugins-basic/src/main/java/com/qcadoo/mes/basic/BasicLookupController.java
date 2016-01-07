package com.qcadoo.mes.basic;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.security.api.SecurityService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

public abstract class BasicLookupController {

    @Autowired
    protected TranslationService translationService;

    @Autowired
    protected SecurityService securityService;

    @Value("${useCompressedStaticResources}")
    protected boolean useCompressedStaticResources;

    protected ModelAndView getModelAndView(final String recordName, final String view, final Locale locale) {
        ModelAndView mav = new ModelAndView();

        mav.addObject("userLogin", securityService.getCurrentUserName());
        mav.addObject("translationsMap", translationService.getMessagesGroup("commons", locale));
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

        Map<String, Map<String, Object>> colModel = new HashMap<>();
        colModel.put("ID", modelId);

        columns.forEach(column -> {
            Map<String, Object> model = new HashMap<>();
            model.put("name", column);
            model.put("index", column);
            model.put("editable", false);
            model.put("editoptions", Collections.singletonMap("readonly", "readonly"));

            colModel.put(column, model);
        });

        config.put("colModel", colModel.values());
        config.put("colNames", colModel.keySet());

        return config;
    }

    public abstract ModelAndView getLookupView(@RequestParam final Map<String, String> arguments, final Locale locale);

    public abstract List getRecords(@RequestParam String sidx, @RequestParam String sord);

    public abstract Map<String, Object> getConfig(Locale locale);

}
