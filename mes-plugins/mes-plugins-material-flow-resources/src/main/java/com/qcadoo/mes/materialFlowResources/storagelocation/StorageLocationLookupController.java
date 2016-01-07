package com.qcadoo.mes.materialFlowResources.storagelocation;

import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.basic.BasicLookupController;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.AdditionalCodeDTO;
import com.qcadoo.mes.materialFlowResources.StorageLocationDTO;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "storageLocation")
public class StorageLocationLookupController extends BasicLookupController {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @RequestMapping(value = "lookup", method = RequestMethod.GET)
    @Override
    public ModelAndView getLookupView(Map<String, String> arguments, Locale locale) {
        ModelAndView mav = getModelAndView("storageLocation", "genericLookup", locale);

        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "records", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public List<AdditionalCodeDTO> getRecords(@RequestParam String sidx, @RequestParam String sord) {
        String query = "SELECT id, number from materialflowresources_storagelocation;";
        return jdbcTemplate.query(query, Collections.EMPTY_MAP, new BeanPropertyRowMapper(StorageLocationDTO.class));
    }

    @ResponseBody
    @RequestMapping(value = "config", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Map<String, Object> getConfig(Locale locale) {
        return getConfigMap(Arrays.asList("number"));
    }

}
