package com.qcadoo.mes.basic.controllers.dataProvider;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.AdditionalCodeDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.PalletNumberDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ProductDTO;
import com.qcadoo.model.api.DictionaryService;
import java.util.Collections;
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

    public List<ProductDTO> getAllProducts(String sidx, String sord) {
        // TODO sort
        String _query = "SELECT product.id, product.number as code, product.number, product.name, product.ean, product.globaltypeofmaterial, product.category "
                + "FROM basic_product product WHERE product.active = true;";

        List<ProductDTO> products = jdbcTemplate.query(_query, new MapSqlParameterSource(Collections.EMPTY_MAP), new BeanPropertyRowMapper(ProductDTO.class));

        return products;
    }

    public List<ProductDTO> getProductsByQuery(String query) {
        String _query = "SELECT product.id as id, product.number as code, product.number as number, product.name as name "
                + "FROM basic_product product WHERE product.active = true and product.number ilike :query LIMIT 15;";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("query", "%" + query + "%");
        SqlParameterSource nParameters = new MapSqlParameterSource(parameters);

        List<ProductDTO> products = jdbcTemplate.query(_query, nParameters, new BeanPropertyRowMapper(ProductDTO.class));

        return products;
    }

    public List<AdditionalCodeDTO> getAllAdditionalCodes(String sidx, String sord) {
        // TODO sort
        String _query = "SELECT additionalcode.id as id, additionalcode.code as code, product.number as productnumber "
                + "FROM basic_additionalcode additionalcode "
                + "JOIN basic_product product ON (additionalcode.product_id = product.id);";

        List<AdditionalCodeDTO> codes = jdbcTemplate.query(_query, new MapSqlParameterSource(Collections.EMPTY_MAP), new BeanPropertyRowMapper(AdditionalCodeDTO.class));

        return codes;
    }

    public List<AdditionalCodeDTO> getAdditionalCodesByQuery(String query) {
        String _query = "SELECT additionalcode.id as id, additionalcode.code as code, product.number as productnumber "
                + "FROM basic_additionalcode additionalcode "
                + "JOIN basic_product product ON (additionalcode.product_id = product.id)"
                + "WHERE additionalcode.code ilike :query LIMIT 15;";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("query", "%" + query + "%");
        SqlParameterSource nParameters = new MapSqlParameterSource(parameters);

        List<AdditionalCodeDTO> codes = jdbcTemplate.query(_query, nParameters, new BeanPropertyRowMapper(AdditionalCodeDTO.class));

        return codes;
    }

    public List<PalletNumberDTO> getAllPalletNumbers(String sidx, String sord) {
        String _query = "SELECT palletnumber.id as id, palletnumber.number as code, palletnumber.number as number "
                + "FROM basic_palletnumber palletnumber WHERE palletnumber.active = true;";

        List<PalletNumberDTO> pallets = jdbcTemplate.query(_query, new MapSqlParameterSource(Collections.EMPTY_MAP), new BeanPropertyRowMapper(PalletNumberDTO.class));

        return pallets;
    }

    public List<PalletNumberDTO> getPalletNumbersByQuery(String query) {
        String _query = "SELECT palletnumber.id as id, palletnumber.number as code, palletnumber.number as number "
                + "FROM basic_palletnumber palletnumber WHERE palletnumber.active = true and palletnumber.number ilike :query LIMIT 15;";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("query", "%" + query + "%");
        SqlParameterSource nParameters = new MapSqlParameterSource(parameters);

        List<PalletNumberDTO> pallets = jdbcTemplate.query(_query, nParameters, new BeanPropertyRowMapper(PalletNumberDTO.class));

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
