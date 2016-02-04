package com.qcadoo.mes.basic.product;

import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.qcadoo.mes.basic.BasicLookupController;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ProductDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.i18n.LocaleContextHolder;

@Controller
@RequestMapping(value = "product")
public class ProductLookupController extends BasicLookupController<ProductDTO> {

    @Override
    protected Map<String, Object> getConfigMap(List<String> columns) {
        Map<String, Object> config = super.getConfigMap(columns);

        Collection<Map<String, Object>> colModel = (Collection) config.get("colModel");
        Map<String, Object> globaltypeofmaterial = colModel.stream().filter(entry -> {
            return "globaltypeofmaterial".equals(entry.get("index"));
        }).findAny().get();
        globaltypeofmaterial.put("formatter", getGlobalTypeOfMaterialFormatter(LocaleContextHolder.getLocale()));

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

    @Override
    protected String getQueryForRecords() {
        String query = "SELECT %s FROM (SELECT product.id, product.number as code, product.number, product.name, product.ean, product.globaltypeofmaterial, product.category "
                + "FROM basic_product product WHERE product.active = true ) q ";

        return query;
    }

    @Override
    protected List<String> getGridFields() {
        return Arrays.asList("number", "name", "ean", "globaltypeofmaterial", "category");
    }

    @Override
    protected String getRecordName() {
        return "product";
    }
}
