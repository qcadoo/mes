package com.qcadoo.mes.basic.controllers.dataProvider;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.AbstractDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.AdditionalCodeDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.PalletNumberDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ProductDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.DataResponse;
import com.qcadoo.model.api.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.Collections;
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

    private static final int MAX_RESULTS = 20;

    private String prepareProductsQuery() {
        return "SELECT product.id as id, product.number as code, product.number as number, product.name as name "
                + "FROM basic_product product WHERE product.active = true and product.number ilike :query ;";
    }

    private String prepareProductsQueryWithLimit(int limit) {
        return "SELECT product.id as id, product.number as code, product.number as number, product.name as name "
                + "FROM basic_product product WHERE product.active = true and product.number ilike :query LIMIT " + limit + ";";
    }

    private String prepareAdditionalCodeQuery(String productnumber) {
        String productNumberCondition = Strings.isNullOrEmpty(productnumber) ? "" : "and product.number = '" + productnumber + "'";

        return "SELECT additionalcode.id as id, additionalcode.code as code, product.number as productnumber "
                + "FROM basic_additionalcode additionalcode "
                + "JOIN basic_product product ON (additionalcode.product_id = product.id " + productNumberCondition + ")"
                + "WHERE additionalcode.code ilike :query;";
    }

    private String prepareAdditionalCodeQueryWithLimit(int limit) {
        return "SELECT additionalcode.id as id, additionalcode.code as code, product.number as productnumber "
                + "FROM basic_additionalcode additionalcode "
                + "JOIN basic_product product ON (additionalcode.product_id = product.id and (product.number = :productnumber OR COALESCE(:productnumber,'')='' ))"
                + "WHERE additionalcode.code ilike :query LIMIT " + limit + ";";
    }

    private String preparePalletNumbersQuery() {
        return "SELECT palletnumber.id as id, palletnumber.number as code, palletnumber.number as number "
                + "FROM basic_palletnumber palletnumber WHERE palletnumber.active = true and palletnumber.number ilike :query;";
    }

    private String preparePalletNumbersQueryWithLimit(int limit) {
        return "SELECT palletnumber.id as id, palletnumber.number as code, palletnumber.number as number "
                + "FROM basic_palletnumber palletnumber WHERE palletnumber.active = true and palletnumber.number ilike :query LIMIT "
                + limit + ";";
    }

    private int countQueryResults(String preparedQuery, String query, Map<String, Object> paramMap) {
        String countQuery = "SELECT count(*) as cnt FROM (" + preparedQuery.replace(";", "") + ") sq;";
        paramMap.put("query", "%" + query + "%");
        return jdbcTemplate.queryForObject(countQuery, paramMap, Integer.class);
    }

    public DataResponse getProductsResponseByQuery(String query) {
        return getDataResponse(query, prepareProductsQuery(), getProductsByQuery(query), new HashMap<String, Object>());
    }

    public DataResponse getAdditionalCodesResponseByQuery(String query, String productnumber) {
        return getDataResponse(query, prepareAdditionalCodeQuery(productnumber), getAdditionalCodesByQuery(query, productnumber),
                new HashMap<String, Object>());
    }

    public DataResponse getPalletNumbersResponseByQuery(String query) {
        return getDataResponse(query, preparePalletNumbersQuery(), getPalletNumbersByQuery(query), new HashMap<String, Object>());
    }

    public DataResponse getDataResponse(String query, String preparedQuery, List<AbstractDTO> entities,
            Map<String, Object> paramMap) {
        int numberOfResults = countQueryResults(preparedQuery, query, paramMap);
        if (numberOfResults > MAX_RESULTS) {
            return new DataResponse(Lists.newArrayList(), numberOfResults);
        }
        return new DataResponse(entities, numberOfResults);
    }

    public List<ProductDTO> getAllProducts(String sidx, String sord) {
        // TODO sort
        String _query = "SELECT product.id, product.number as code, product.number, product.name, product.ean, product.globaltypeofmaterial, product.category "
                + "FROM basic_product product WHERE product.active = true;";

        List<ProductDTO> products = jdbcTemplate.query(_query, new MapSqlParameterSource(Collections.EMPTY_MAP),
                new BeanPropertyRowMapper(ProductDTO.class));

        return products;
    }

    public List<AbstractDTO> getProductsByQuery(String query) {
        String _query = prepareProductsQueryWithLimit(MAX_RESULTS);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("query", "%" + query + "%");
        SqlParameterSource nParameters = new MapSqlParameterSource(parameters);

        List<AbstractDTO> products = jdbcTemplate.query(_query, nParameters, new BeanPropertyRowMapper(ProductDTO.class));

        return products;
    }

    public List<AdditionalCodeDTO> getAllAdditionalCodes(String sidx, String sord) {
        // TODO sort
        String _query = "SELECT additionalcode.id as id, additionalcode.code as code, product.number as productnumber "
                + "FROM basic_additionalcode additionalcode "
                + "JOIN basic_product product ON (additionalcode.product_id = product.id);";

        List<AdditionalCodeDTO> codes = jdbcTemplate.query(_query, new MapSqlParameterSource(Collections.EMPTY_MAP),
                new BeanPropertyRowMapper(AdditionalCodeDTO.class));

        return codes;
    }

    public List<AbstractDTO> getAdditionalCodesByQuery(String query, String productnumber) {
        String _query = prepareAdditionalCodeQueryWithLimit(MAX_RESULTS);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("query", "%" + query + "%");
        parameters.put("productnumber", productnumber);
        SqlParameterSource nParameters = new MapSqlParameterSource(parameters);

        List<AbstractDTO> codes = jdbcTemplate.query(_query, nParameters, new BeanPropertyRowMapper(AdditionalCodeDTO.class));

        return codes;
    }

    public List<PalletNumberDTO> getAllPalletNumbers(String sidx, String sord) {
        String _query = "SELECT palletnumber.id as id, palletnumber.number as code, palletnumber.number as number "
                + "FROM basic_palletnumber palletnumber WHERE palletnumber.active = true;";

        List<PalletNumberDTO> pallets = jdbcTemplate.query(_query, new MapSqlParameterSource(Collections.EMPTY_MAP),
                new BeanPropertyRowMapper(PalletNumberDTO.class));

        return pallets;
    }

    public List<AbstractDTO> getPalletNumbersByQuery(String query) {
        String _query = preparePalletNumbersQueryWithLimit(MAX_RESULTS);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("query", "%" + query + "%");
        SqlParameterSource nParameters = new MapSqlParameterSource(parameters);

        List<AbstractDTO> pallets = jdbcTemplate.query(_query, nParameters, new BeanPropertyRowMapper(PalletNumberDTO.class));

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
