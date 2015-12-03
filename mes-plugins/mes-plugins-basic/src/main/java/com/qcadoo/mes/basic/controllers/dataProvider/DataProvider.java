package com.qcadoo.mes.basic.controllers.dataProvider;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.AdditionalCodeDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.PalletNumberDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ProductDTO;
import com.qcadoo.model.api.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataProvider {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DictionaryService dictionaryService;

    public List<ProductDTO> getProductsByQuery(String query) {
        String _query = "SELECT product.id as id, product.number as code, product.number as number, product.name as name "
                + "FROM basic_product product WHERE product.active = true and product.number ilike :query LIMIT 15;";

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("query", "%"+query+"%");
        SqlParameterSource nParameters = new MapSqlParameterSource(parameters);

        List<ProductDTO> products = Lists.newArrayList();
        products = jdbcTemplate.query(_query, nParameters, new BeanPropertyRowMapper(ProductDTO.class));
        return products;
    }

    public List<AdditionalCodeDTO> getAdditionalCodesByQuery(String query) {
        String _query = "SELECT additionalcode.id as id, additionalcode.code as code "
                + "FROM basic_additionalcode additionalcode WHERE additionalcode.code ilike :query LIMIT 15;";

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("query", "%"+query+"%");
        SqlParameterSource nParameters = new MapSqlParameterSource(parameters);

        List<AdditionalCodeDTO> codes = Lists.newArrayList();
        codes = jdbcTemplate.query(_query, nParameters, new BeanPropertyRowMapper(AdditionalCodeDTO.class));
        return codes;
    }

    public List<PalletNumberDTO> getPalletNumbersByQuery(String query) {
        String _query = "SELECT palletnumber.id as id, palletnumber.number as code, palletnumber.number as number "
                + "FROM basic_palletnumber palletnumber WHERE palletnumber.active = true and palletnumber.number ilike :query LIMIT 15;";

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("query", "%"+query+"%");
        SqlParameterSource nParameters = new MapSqlParameterSource(parameters);

        List<PalletNumberDTO> pallets = Lists.newArrayList();
        pallets = jdbcTemplate.query(_query, nParameters, new BeanPropertyRowMapper(PalletNumberDTO.class));
        return pallets;
    }

    public List<Map<String, String>> getUnits() {
        return dictionaryService.getKeys("units").stream().map(unit -> {
            Map<String, String> type = new HashMap<>();
            type.put("value", unit);
            type.put("key", unit);

            return type;
        }).collect(Collectors.toList());
    }

    public List<Map<String, String>> getTypeOfPallets() {
        return dictionaryService.getKeys("typeOfPallet").stream().map(unit -> {
            Map<String, String> type = new HashMap<>();
            type.put("value", unit);
            type.put("key", unit);

            return type;
        }).collect(Collectors.toList());
    }
}
