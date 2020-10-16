package com.qcadoo.mes.technologies.controller.dataProvider;

import com.google.common.collect.Maps;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.productionLines.controller.dataProvider.ProductionLineDto;
import com.qcadoo.mes.technologies.OperationComponentDataProvider;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DataProviderForTechnology {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private OperationComponentDataProvider operationComponentDataProvider;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public TechnologiesResponse getTechnologies(String query, Long productId, Boolean master, Boolean forEach) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("Select id as id, number as number, master as master From technologies_technology WHERE ");
        if (master) {
            queryBuilder.append(" master = true AND ");
        }
        if(forEach) {
            queryBuilder
                    .append(" typeOfProductionRecording = '03forEach' ");
        } else {
            queryBuilder
                    .append(" typeOfProductionRecording = '02cumulated' ");
        }
        queryBuilder
                .append(" AND product_id = :productId AND state = '02accepted' AND number ilike :query ORDER BY number ASC LIMIT 10 ");

        Map<String, Object> parameters = Maps.newHashMap();

        String ilikeQuery = "%" + query + "%";
        parameters.put("query", ilikeQuery);
        parameters.put("productId", productId);
        List<TechnologyDto> technologies = jdbcTemplate.query(queryBuilder.toString(), parameters, new BeanPropertyRowMapper(
                TechnologyDto.class));
        TechnologiesResponse technologiesResponse = new TechnologiesResponse();
        technologiesResponse.setTechnologies(technologies);
        return technologiesResponse;
    }

    public TechnologiesGridResponse getTechnologiesResponse(int limit, int offset, String sort, String order, String search,
            Long productId, Boolean forEach) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT tech.id, tech.number, tech.name ");
        query.append("FROM technologies_technology tech WHERE ");
        if(forEach) {
            query
                    .append(" tech.typeOfProductionRecording  = '03forEach' ");
        } else {
            query
                    .append(" tech.typeOfProductionRecording  = '02cumulated' ");
        }
        query.append(" AND tech.active = true AND tech.product_id = :productID AND tech.state = '02accepted' ");

        StringBuilder queryCount = new StringBuilder();
        queryCount.append("SELECT COUNT(*) ");
        queryCount.append("FROM technologies_technology tech WHERE ");
        if(forEach) {
            queryCount
                    .append(" tech.typeOfProductionRecording  = '03forEach' ");
        } else {
            queryCount
                    .append(" tech.typeOfProductionRecording  = '02cumulated' ");
        }
        queryCount
                .append(" AND tech.active = true AND tech.product_id = :productID AND tech.state = '02accepted' ");

        appendTechnologyConditions(search, query);
        appendTechnologyConditions(search, queryCount);

        if (StringUtils.isNotEmpty(sort)) {
            query.append(" ORDER BY " + sort + " " + order);
        }
        query.append(String.format(" LIMIT %d OFFSET %d", limit, offset));

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("productID", productId);

        Integer countRecords = jdbcTemplate.queryForObject(queryCount.toString(), parameters, Long.class).intValue();

        List<TechnologyDto> products = jdbcTemplate.query(query.toString(), parameters, new BeanPropertyRowMapper(
                TechnologyDto.class));

        return new TechnologiesGridResponse(countRecords, products);
    }

    private void appendTechnologyConditions(String search, StringBuilder query) {
        if (StringUtils.isNotEmpty(search)) {
            query.append(" AND (");
            query.append("UPPER(tech.number) LIKE '%").append(search.toUpperCase()).append("%' OR ");
            query.append("UPPER(tech.name) LIKE '%").append(search.toUpperCase()).append("%' ");
            query.append(") ");
        }
    }

    public List<MaterialDto> getTechnologyMaterials(Long technologyId) {
        List<Long> ids = operationComponentDataProvider.getComponentsForTechnology(technologyId);
        StringBuilder query = new StringBuilder();
        query.append("SELECT opic.id as productInId, opic.id as index, p.id as productId, p.number as product, p.number as productNumber,  ");
        query.append("p.name as productName, p.unit as unit, opic.quantity as quantityPerUnit ");
        query.append("FROM technologies_operationproductincomponent opic ");
        query.append("LEFT JOIN basic_product p ON opic.product_id = p.id ");
        query.append("WHERE opic.id IN (:ids) ");
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("ids", ids);
        return jdbcTemplate.query(query.toString(), parameters, new BeanPropertyRowMapper(MaterialDto.class));
    }

    public List<OperationMaterialDto> getTechnologyOperationMaterials(Long technologyId) {
        List<Long> ids = operationComponentDataProvider.getComponentsForTechnology(technologyId);
        ids.addAll(operationComponentDataProvider.getIntermediateInProductsForTechnology(technologyId));
        StringBuilder query = new StringBuilder();
        query.append("SELECT opic.id as productInId, opic.id as index, p.id as productId, p.number as product, p.number as productNumber,  ");
        query.append("p.name as productName, p.unit as unit, opic.quantity as quantityPerUnit, ");
        query.append("toc.id as tocId, toc.nodenumber as node, op.number operationNumber, op.id as operationId ");
        query.append("FROM technologies_operationproductincomponent opic ");
        query.append("LEFT JOIN basic_product p ON opic.product_id = p.id ");
        query.append("LEFT JOIN technologies_technologyoperationcomponent toc ON opic.operationcomponent_id = toc.id ");
        query.append("LEFT JOIN technologies_operation op ON toc.operation_id = op.id ");
        query.append("WHERE opic.id IN (:ids) ");
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("ids", ids);
        return jdbcTemplate.query(query.toString(), parameters, new BeanPropertyRowMapper(OperationMaterialDto.class));
    }

    public ProductionLineDto getTechnologyProductionLine(Long technologyId) {
        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);
        if (technology.getStringField("range").equals("01oneDivision")) {
            Entity productionLine = technology.getBelongsToField("productionLine");
            if (Objects.nonNull(productionLine)) {
                ProductionLineDto pl = new ProductionLineDto();
                pl.setId(productionLine.getId());
                pl.setName(productionLine.getStringField(ProductionLineFields.NAME));
                pl.setNumber(productionLine.getStringField(ProductionLineFields.NUMBER));
                return pl;
            }
        }
        return new ProductionLineDto();
    }

    public OperationsResponse getOperations() {
        StringBuilder query = new StringBuilder();
        query.append("SELECT id, number ");
        query.append("FROM technologies_operation ");
        query.append("WHERE active = true ORDER BY lower(number) ASC");
        return new OperationsResponse(jdbcTemplate.query(query.toString(), Maps.newHashMap(), new BeanPropertyRowMapper(OperationDto.class)));
    }
}
