package com.qcadoo.mes.basic.controllers.dataProvider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.SubassemblyFields;
import com.qcadoo.mes.basic.constants.TypeOfLoadUnitFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.*;
import com.qcadoo.mes.basic.controllers.dataProvider.requests.FaultTypeRequest;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.*;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.DictionaryService;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DataProvider {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public static final int MAX_RESULTS = 20;

    private String prepareProductsQuery() {
        return "SELECT product.id AS id, product.number AS code, product.number AS number, product.unit AS unit, product.name AS name "
                + "FROM basic_product product WHERE product.active = true AND product.number ilike :query ;";
    }

    private String prepareProductsQueryWithLimit(int limit) {
        return "SELECT product.id AS id, product.number AS code, product.number AS number, product.unit AS unit, product.name AS name "
                + "FROM basic_product product WHERE product.active = true AND product.number ilike :query LIMIT " + limit + ";";
    }

    private String preparePalletNumbersQuery() {
        return "SELECT palletnumber.id AS id, palletnumber.number AS code, palletnumber.number AS number "
                + "FROM basic_palletnumber palletnumber WHERE palletnumber.active = true AND palletnumber.number ilike :query;";
    }

    private String prepareAttributesQuery() {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT av.id as id, av.value as value FROM basic_attributevalue av ");
        builder.append("LEFT JOIN basic_attribute a  ON a.id = av.attribute_id ");
        builder.append("WHERE a.number = :attr AND av.value ilike :query ");
        return builder.toString();

    }

    private String prepareAttributesQueryLimit(int limit) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT av.id as id, av.value as value FROM basic_attributevalue av ");
        builder.append("LEFT JOIN basic_attribute a  ON a.id = av.attribute_id ");
        builder.append("WHERE a.number = :attr AND av.value ilike :query LIMIT " + limit + ";");
        return builder.toString();

    }

    private String preparePalletNumbersQueryWithLimit(int limit) {
        return "SELECT palletnumber.id AS id, palletnumber.number AS code, palletnumber.number AS number "
                + "FROM basic_palletnumber palletnumber WHERE palletnumber.active = true AND palletnumber.number ilike :query LIMIT "
                + limit + ";";
    }

    private int countQueryResults(final String preparedQuery, final String query, final Map<String, Object> paramMap) {
        String countQuery = "SELECT count(*) AS cnt FROM (" + preparedQuery.replace(";", "") + ") sq;";

        String ilikeQuery = buildConditionParameterForIlike(query);
        paramMap.put("query", ilikeQuery);

        return jdbcTemplate.queryForObject(countQuery, paramMap, Integer.class);
    }

    public DataResponse getProductsResponseByQuery(final String query) {
        return getDataResponse(query, prepareProductsQuery(), getProductsByQuery(query), Maps.newHashMap());
    }

    public DataResponse getPalletNumbersResponseByQuery(final String query) {
        return getDataResponse(query, preparePalletNumbersQuery(), getPalletNumbersByQuery(query), Maps.newHashMap());
    }

    public DataResponse getDataResponse(final String query, final String preparedQuery,
                                        final List<? extends AbstractDTO> entities,
                                        final Map<String, Object> paramMap) {
        return getDataResponse(query, preparedQuery, entities, paramMap, true);
    }

    public DataResponse getDataResponse(final String query, final String preparedQuery,
                                        final List<? extends AbstractDTO> entities, Map<String, Object> paramMap,
                                        boolean shouldCheckMaxResults) {
        int numberOfResults = countQueryResults(preparedQuery, query, paramMap);

        if (shouldCheckMaxResults && (numberOfResults > MAX_RESULTS)) {
            return new DataResponse(Lists.newArrayList(), numberOfResults);
        }

        return new DataResponse(entities, numberOfResults);
    }

    public DataResponse getAttributesByQuery(String attr, String query) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("attr", attr);
        return getDataResponse(query, prepareAttributesQuery(), getAttribiutesByQuery(attr, query), parameters);

    }

    private List<ProductDTO> getAllProducts() {
        String _query = "SELECT product.id, product.number AS code, product.number, product.name, product.unit, product.ean, product.globaltypeofmaterial, product.category "
                + "FROM basic_product product WHERE product.active = true ORDER BY product.number;";
        List<ProductDTO> products = jdbcTemplate.query(_query, new MapSqlParameterSource(Collections.EMPTY_MAP),
                new BeanPropertyRowMapper(ProductDTO.class));
        return products;
    }

    public DataResponse getProductsTypeahead(String query) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("SELECT product.id AS id, product.number AS code, product.number AS number, product.unit AS unit, product.name AS name ");
        queryBuilder
                .append("FROM basic_product product WHERE product.active = true AND product.number ilike :query ORDER BY product.number ASC LIMIT 10 ");

        Map<String, Object> parameters = Maps.newHashMap();

        String ilikeQuery = "%" + query + "%";
        parameters.put("query", ilikeQuery);

        List<ProductDTO> products = jdbcTemplate.query(queryBuilder.toString(), parameters, new BeanPropertyRowMapper(
                ProductDTO.class));
        return new DataResponse(products, products.size());
    }

    public ProductsGridResponse getProductsResponse(int limit, int offset, String sort, String order, String search) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT product.id, product.number AS code, product.number, product.name, product.unit, product.ean, product.globaltypeofmaterial, product.category ");
        query.append("FROM basic_product product WHERE product.active = true ");

        StringBuilder queryCount = new StringBuilder();
        queryCount.append("SELECT COUNT(*) ");
        queryCount.append("FROM basic_product product WHERE product.active = true ");

        appendProductsConditions(search, query);
        appendProductsConditions(search, queryCount);

        if (StringUtils.isNotEmpty(sort)) {
            query.append(" ORDER BY " + sort + " " + order);
        }
        query.append(String.format(" LIMIT %d OFFSET %d", limit, offset));

        Integer countRecords = jdbcTemplate.queryForObject(queryCount.toString(),
                new MapSqlParameterSource(Collections.EMPTY_MAP), Long.class).intValue();

        List<ProductDTO> products = jdbcTemplate.query(query.toString(), new MapSqlParameterSource(Collections.EMPTY_MAP),
                new BeanPropertyRowMapper(ProductDTO.class));

        return new ProductsGridResponse(countRecords, products);
    }

    private void appendProductsConditions(String search, StringBuilder query) {
        if (StringUtils.isNotEmpty(search)) {
            query.append(" AND (");
            query.append("UPPER(product.number) LIKE '%").append(search.toUpperCase()).append("%' OR ");
            query.append("UPPER(product.name) LIKE '%").append(search.toUpperCase()).append("%' OR ");
            query.append("UPPER(product.unit) LIKE '%").append(search.toUpperCase()).append("%'");
            query.append(") ");
        }
    }

    public List<AbstractDTO> getProductsByQuery(final String query) {
        String _query = prepareProductsQueryWithLimit(MAX_RESULTS);

        Map<String, Object> parameters = Maps.newHashMap();

        String ilikeQuery = buildConditionParameterForIlike(query);
        parameters.put("query", ilikeQuery);

        SqlParameterSource nParameters = new MapSqlParameterSource(parameters);

        List<AbstractDTO> products = jdbcTemplate.query(_query, nParameters, new BeanPropertyRowMapper(ProductDTO.class));

        return products;
    }

    public List<PalletNumberDTO> getAllPalletNumbers(final String sidx, final String sord) {
        String _query = "SELECT palletnumber.id AS id, palletnumber.number AS code, palletnumber.number AS number "
                + "FROM basic_palletnumber palletnumber WHERE palletnumber.active = true;";

        List<PalletNumberDTO> pallets = jdbcTemplate.query(_query, new MapSqlParameterSource(Collections.EMPTY_MAP),
                new BeanPropertyRowMapper(PalletNumberDTO.class));

        return pallets;
    }

    public List<AbstractDTO> getPalletNumbersByQuery(final String query) {
        String _query = preparePalletNumbersQueryWithLimit(MAX_RESULTS);

        Map<String, Object> parameters = Maps.newHashMap();

        String ilikeQuery = buildConditionParameterForIlike(query);
        parameters.put("query", ilikeQuery);

        SqlParameterSource nParameters = new MapSqlParameterSource(parameters);

        List<AbstractDTO> pallets = jdbcTemplate.query(_query, nParameters, new BeanPropertyRowMapper(PalletNumberDTO.class));

        return pallets;
    }

    public List<AbstractDTO> getAttribiutesByQuery(final String attr, final String query) {
        String _query = prepareAttributesQueryLimit(MAX_RESULTS);

        Map<String, Object> parameters = Maps.newHashMap();

        String ilikeQuery = buildConditionParameterForIlike(query);
        parameters.put("query", ilikeQuery);
        parameters.put("attr", attr);

        SqlParameterSource nParameters = new MapSqlParameterSource(parameters);

        List<AbstractDTO> attrs = jdbcTemplate.query(_query, nParameters, new BeanPropertyRowMapper(AttribiuteValueDTO.class));

        return attrs;
    }

    public List<Map<String, String>> getUnits() {
        return dictionaryService.getKeys("units").stream().map(unit -> {
            Map<String, String> type = Maps.newHashMap();

            type.put("value", unit);
            type.put("key", unit);

            return type;
        }).collect(Collectors.toList());
    }

    public List<Map<String, String>> getTypeOfLoadUnits() {
        return getTypeOfLoadUnitDD().find().list().getEntities().stream().map(e -> {
            Map<String, String> type = Maps.newHashMap();

            type.put("value", e.getStringField(TypeOfLoadUnitFields.NAME));
            type.put("key", e.getStringField(TypeOfLoadUnitFields.NAME));

            return type;
        }).collect(Collectors.toList());
    }

    private String buildConditionParameterForIlike(String query) {
        String ilikeQuery = "%" + query + "%";
        ilikeQuery = ilikeQuery.replace("*", "%");
        ilikeQuery = ilikeQuery.replace("%%", "%");
        return ilikeQuery;
    }

    public WorkstationTypesResponse getWorkstationTypes() {
        StringBuilder query = new StringBuilder();
        query.append("Select w.id as id, w.number as number, w.name as name From basic_workstationtype w WHERE ");
        query.append(" w.active = true ORDER BY w.number ");
        List<WorkstationTypeDto> workstationTypes = jdbcTemplate.query(query.toString(), Maps.newHashMap(),
                new BeanPropertyRowMapper(WorkstationTypeDto.class));
        return new WorkstationTypesResponse(workstationTypes);
    }

    public CountriesResponse getCountries(String query) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("Select c.id as id, c.code as code, c.country as country From basic_country c WHERE ");
        queryBuilder.append(" 1=1 ");
        Map<String, Object> parameters = Maps.newHashMap();

        queryBuilder.append(" AND c.country ilike :query ORDER BY country ASC LIMIT 10 ");

        String ilikeQuery = "%" + query + "%";
        parameters.put("query", ilikeQuery);
        List<CountryDto> countries = jdbcTemplate.query(queryBuilder.toString(), parameters, new BeanPropertyRowMapper(
                CountryDto.class));
        CountriesResponse response = new CountriesResponse();
        response.setCountries(countries);
        return response;
    }

    public CountriesGridResponse getCountriesByPage(int limit, int offset, String sort, String order, String search) {
        StringBuilder query = new StringBuilder();
        query.append("Select c.id as id, c.code as code, c.country as country From basic_country c WHERE ");
        query.append(" 1 = 1 ");

        StringBuilder queryCount = new StringBuilder();
        queryCount.append("SELECT COUNT(*) ");
        queryCount.append("From basic_country c WHERE 1 = 1 ");
        Map<String, Object> parameters = Maps.newHashMap();

        appendCountriesConditions(search, query, parameters);
        appendCountriesConditions(search, queryCount, parameters);

        if (StringUtils.isNotEmpty(sort)) {
            query.append(" ORDER BY " + sort + " " + order);
        }
        query.append(String.format(" LIMIT %d OFFSET %d", limit, offset));

        Integer countRecords = jdbcTemplate.queryForObject(queryCount.toString(), parameters, Long.class).intValue();

        List<CountryDto> countries = jdbcTemplate
                .query(query.toString(), parameters, new BeanPropertyRowMapper(CountryDto.class));

        return new CountriesGridResponse(countRecords, countries);
    }

    private void appendCountriesConditions(String search, StringBuilder query, Map<String, Object> parameters) {
        if (StringUtils.isNotEmpty(search)) {
            query.append(" AND (");
            query.append("UPPER(c.code) LIKE '%").append(search.toUpperCase()).append("%' OR ");
            query.append("UPPER(c.country) LIKE '%").append(search.toUpperCase()).append("%' ");
            query.append(") ");
        }
    }

    public FactoriesResponse getFactories() {
        StringBuilder query = new StringBuilder();
        query.append("Select w.id as id, w.number as number, w.name as name From basic_factory w WHERE ");
        query.append(" w.active = true ORDER BY w.number ");
        List<FactoryDto> factories = jdbcTemplate.query(query.toString(), Maps.newHashMap(), new BeanPropertyRowMapper(
                FactoryDto.class));
        return new FactoriesResponse(factories);
    }

    public SubassembliesResponse getSubassemblies(Long workstationId) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("workstationId", workstationId);
        StringBuilder query = new StringBuilder();
        query.append("Select w.id as id, w.number as number, w.name as name From basic_subassembly w WHERE ");
        query.append(" w.active = true AND workstation_id = :workstationId ORDER BY w.number ");
        List<SubassemblyDto> subassemblies = jdbcTemplate.query(query.toString(), parameters, new BeanPropertyRowMapper(
                SubassemblyDto.class));
        return new SubassembliesResponse(subassemblies);
    }

    public FaultTypeResponse getFaultTypes(FaultTypeRequest faultTypeRequest) {

        if (Objects.nonNull(faultTypeRequest.getSubassemblyId())) {
            Entity w = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SUBASSEMBLY).get(
                    faultTypeRequest.getSubassemblyId());
            Map<String, Object> parameters = Maps.newHashMap();

            parameters.put("subassemblyId", w.getId());
            parameters.put("workstationTypeId", w.getBelongsToField(SubassemblyFields.WORKSTATION_TYPE).getId());

            StringBuilder query = new StringBuilder();

            query.append("SELECT distinct ft.id as id, ft.name as name, ft.name as number ");
            query.append("FROM basic_faulttype ft ");
            query.append("LEFT JOIN jointable_faulttype_subassembly fs ON fs.faulttype_id = ft.id ");
            query.append("LEFT JOIN jointable_faulttype_workstationtype fwt ON fwt.faulttype_id = ft.id ");
            query.append("WHERE ft.active AND ( ft.isdefault OR fs.subassembly_id = :subassemblyId OR fwt.workstationtype_id = :workstationTypeId) ");
            query.append("ORDER BY ft.name ");
            List<FaultTypeDto> types = jdbcTemplate.query(query.toString(), parameters, new BeanPropertyRowMapper(
                    FaultTypeDto.class));

            return new FaultTypeResponse(types);
        } else if (Objects.nonNull(faultTypeRequest.getWorkstationId())) {
            Entity w = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_WORKSTATION).get(
                    faultTypeRequest.getWorkstationId());
            Map<String, Object> parameters = Maps.newHashMap();

            parameters.put("workstationId", faultTypeRequest.getWorkstationId());
            parameters.put("workstationTypeId", w.getBelongsToField(WorkstationFields.WORKSTATION_TYPE).getId());

            StringBuilder query = new StringBuilder();

            query.append("SELECT distinct ft.id as id, ft.name as name, ft.name as number ");
            query.append("FROM basic_faulttype ft ");
            query.append("LEFT JOIN jointable_faulttype_workstation fw ON fw.faulttype_id = ft.id ");
            query.append("LEFT JOIN jointable_faulttype_workstationtype fwt ON fwt.faulttype_id = ft.id ");
            query.append("WHERE ft.active AND ( ft.isdefault OR fw.workstation_id = :workstationId OR fwt.workstationtype_id = :workstationTypeId) ");
            query.append("ORDER BY ft.name ");

            List<FaultTypeDto> types = jdbcTemplate.query(query.toString(), parameters, new BeanPropertyRowMapper(
                    FaultTypeDto.class));

            return new FaultTypeResponse(types);
        } else {
            Map<String, Object> parameters = Maps.newHashMap();

            StringBuilder query = new StringBuilder();

            query.append("SELECT distinct ft.id as id, ft.name as name, ft.name as number ");
            query.append("FROM basic_faulttype ft ");
            query.append("LEFT JOIN jointable_faulttype_subassembly fs ON fs.faulttype_id = ft.id ");
            query.append("LEFT JOIN jointable_faulttype_workstationtype fwt ON fwt.faulttype_id = ft.id ");
            query.append("WHERE ft.active AND ( ft.isdefault OR ft.appliesTo in ('01workstationOrSubassembly','02workstationType','') OR ft.appliesTo is null) ");
            query.append("ORDER BY ft.name ");
            List<FaultTypeDto> types = jdbcTemplate.query(query.toString(), parameters, new BeanPropertyRowMapper(
                    FaultTypeDto.class));

            return new FaultTypeResponse(types);
        }

    }

    private DataDefinition getTypeOfLoadUnitDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_TYPE_OF_LOAD_UNIT);
    }
}
