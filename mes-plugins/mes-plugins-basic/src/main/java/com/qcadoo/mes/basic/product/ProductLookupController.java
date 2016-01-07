package com.qcadoo.mes.basic.product;

import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.basic.BasicLookupController;
import com.qcadoo.mes.basic.controllers.dataProvider.DataProvider;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ProductDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "product")
public class ProductLookupController extends BasicLookupController {

    @Autowired
    private DataProvider dataProvider;

    @RequestMapping(value = "lookup", method = RequestMethod.GET)
    @Override
    public ModelAndView getLookupView(Map<String, String> arguments, Locale locale) {
        ModelAndView mav = getModelAndView("product", "genericLookup", locale);

        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "records", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public List<ProductDTO> getRecords(@RequestParam String sidx, @RequestParam String sord) {
        return dataProvider.getAllProducts(sidx, sord);
    }

    @ResponseBody
    @RequestMapping(value = "config", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Map<String, Object> getConfig(Locale locale) {
        Map<String, Object> config = getConfigMap(Arrays.asList("number", "name", "ean", "globaltypeofmaterial", "category"));

        Collection<Map<String, Object>> colModel = (Collection) config.get("colModel");
        Map<String, Object> globaltypeofmaterial = colModel.stream().filter(entry -> {
            return "globaltypeofmaterial".equals(entry.get("index"));
        }).findAny().get();
        globaltypeofmaterial.put("formatter", getGlobalTypeOfMaterialFormatter(locale));

        return config;
    }

    private String getGlobalTypeOfMaterialFormatter(Locale locale) {
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        for (String key : Arrays.asList("01component", "02intermediate", "03finalProduct", "04waste")) {
            values.put(key, translationService.translate("basic.product.globalTypeOfMaterial.value." + key, locale));
        }

        final List<String> items = new ArrayList<>();
        values.forEach((key, value) -> {
            items.add(String.format("map['%s'] = '%s';", key, value));
        });
        String valuesString = items.stream().collect(Collectors.joining());

        String formatter = "function(cellValue, options, rowObject) {"
                + "            var map = {};"
                + valuesString
                + "            var newValue = map[cellValue] || '';"
                + "            return newValue;"
                + "        }";

        return formatter;
    }
}
