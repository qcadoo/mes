package com.qcadoo.mes.materialFlowResources;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.GridResponse;
import com.qcadoo.mes.basic.LookupUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "attribute")
public class AttributeLookupController {

    @Autowired
    private LookupUtils lookupUtils;

    @ResponseBody
    @RequestMapping(value = "{name}/records", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public GridResponse<AttributeDto> getRecords(@PathVariable String name, @RequestParam String sidx, @RequestParam String sord,
            @RequestParam(defaultValue = "1", required = false, value = "page") Integer page,
            @RequestParam(value = "rows") int perPage,
            @RequestParam(defaultValue = "0", required = false, value = "context") Long context, AttributeDto record) {

        String query = getQueryForRecords();

        return lookupUtils.getGridResponse(query, sidx, sord, page, perPage, record, getQueryParameters(name, record));
    }

    private Map<String, Object> getQueryParameters(String name, AttributeDto record) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("atrr", name);
        return parameters;
    }

    @ResponseBody
    @RequestMapping(value = "{name}/config", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getConfigView(@PathVariable String name, Locale locale) {
        return lookupUtils.getConfigMap(getGridFields());
    }

    @RequestMapping(value = "{name}/lookup", method = RequestMethod.GET)
    public ModelAndView getLookupView(@PathVariable String name, Map<String, String> arguments, Locale locale) {
        ModelAndView mav = lookupUtils.getModelAndView("attribute/" + name, "genericLookup", locale);

        return mav;
    }

    private List<String> getGridFields() {
        return Arrays.asList("value");
    }

    private String getQueryForRecords() {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT %s FROM ( SELECT av.id as id, av.value as value FROM basic_attributevalue av ");
        builder.append("LEFT JOIN basic_attribute a  ON a.id = av.attribute_id ");
        builder.append("WHERE a.number = :atrr %s) q");
        return builder.toString();
    }
}
