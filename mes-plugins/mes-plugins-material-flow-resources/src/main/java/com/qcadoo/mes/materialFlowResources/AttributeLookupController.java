package com.qcadoo.mes.materialFlowResources;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.GridResponse;
import com.qcadoo.mes.basic.LookupUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

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
            @RequestParam(defaultValue = "0", required = false, value = "context") Long context, AttributeDto record,
            HttpServletRequest httpServletRequest) {
        String query = getQueryForRecords();
        String requestURI = httpServletRequest.getRequestURI();
        URI uri = URI.create(requestURI);
        Path path = Paths.get(uri.getPath());
        String secondToLast = path.getName(path.getNameCount() - 2).toString();
        return lookupUtils.getGridResponse(query, sidx, sord, page, perPage, record, getQueryParameters(secondToLast, record));
    }

    private Map<String, Object> getQueryParameters(String name, AttributeDto record) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("atrr", name);
        return parameters;
    }

    @ResponseBody
    @RequestMapping(value = "{name}/config", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getConfigView(@PathVariable String name, Locale locale)
            throws UnsupportedEncodingException {
        return lookupUtils.getConfigMap(getGridFields());
    }

    @RequestMapping(value = "{name}/lookup", method = RequestMethod.GET)
    public ModelAndView getLookupView(@PathVariable String name, Map<String, String> arguments, Locale locale) {
        ModelAndView mav = lookupUtils.getModelAndView("attribute/" + name, "genericLookup", locale);

        return mav;
    }

    private List<String> getGridFields() {
        return Arrays.asList("value", "description");
    }

    private String getQueryForRecords() {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT %s FROM ( SELECT av.id as id, av.value as value, av.description as description FROM basic_attributevalue av ");
        builder.append("LEFT JOIN basic_attribute a  ON a.id = av.attribute_id ");
        builder.append("WHERE a.number = :atrr %s) q");
        return builder.toString();
    }
}
