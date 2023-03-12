package com.qcadoo.mes.technologies.controller.dataProvider;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.WorkstationDto;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.WorkstationTypeDto;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.WorkstationTypesGridResponse;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.WorkstationTypesResponse;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.WorkstationsGridResponse;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.WorkstationsResponse;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.productionLines.controller.dataProvider.ProductionLineDto;
import com.qcadoo.mes.technologies.OperationComponentDataProvider;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        if (forEach) {
            queryBuilder.append(" typeOfProductionRecording = '03forEach' ");
        } else {
            queryBuilder.append(" typeOfProductionRecording = '02cumulated' ");
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
        if (forEach) {
            query.append(" tech.typeOfProductionRecording  = '03forEach' ");
        } else {
            query.append(" tech.typeOfProductionRecording  = '02cumulated' ");
        }
        query.append(" AND tech.active = true AND tech.product_id = :productID AND tech.state = '02accepted' ");

        StringBuilder queryCount = new StringBuilder();
        queryCount.append("SELECT COUNT(*) ");
        queryCount.append("FROM technologies_technology tech WHERE ");
        if (forEach) {
            queryCount.append(" tech.typeOfProductionRecording  = '03forEach' ");
        } else {
            queryCount.append(" tech.typeOfProductionRecording  = '02cumulated' ");
        }
        queryCount.append(" AND tech.active = true AND tech.product_id = :productID AND tech.state = '02accepted' ");

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
        List<OperationMaterialDto> operationMaterials = jdbcTemplate.query(query.toString(), parameters,
                new BeanPropertyRowMapper(OperationMaterialDto.class));

        Map<Long, List<WorkstionTechnologyOperationComponentDto>> workstationByTechnologyOperationComponents = getWorkstionsByTechnologyOperationComponents(operationMaterials
                .stream().map(om -> om.getTocId()).collect(Collectors.toList()));
        for (OperationMaterialDto operationMaterial : operationMaterials) {

            if (workstationByTechnologyOperationComponents.containsKey(operationMaterial.getTocId())) {
                List<WorkstionTechnologyOperationComponentDto> workstations = workstationByTechnologyOperationComponents
                        .get(operationMaterial.getTocId());
                if(workstations.size() == 1) {
                    WorkstionTechnologyOperationComponentDto workstationTechnologyOperationComponent = workstations.get(0);
                    operationMaterial.setWorkstationId(workstationTechnologyOperationComponent.getWorkstationId());
                    operationMaterial.setWorkstationNumber(workstationTechnologyOperationComponent.getWorkstationNumber());
                }
            }
        }
        return operationMaterials;
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
        query.append("SELECT o.id, o.number, p.number as productNumber, p.unit as productUnit ");
        query.append("FROM technologies_operation o ");
        query.append("LEFT JOIN basic_product p ON p.id = o.product_id ");
        query.append("WHERE o.active = true AND (o.createOperationOutput = true OR o.product_id is not null) ");
        query.append("ORDER BY lower(o.number) ASC ");
        return new OperationsResponse(jdbcTemplate.query(query.toString(), Maps.newHashMap(), new BeanPropertyRowMapper(
                OperationDto.class)));
    }

    public List<Long> getWorkstationsIds(Long technologyOperationId) {
        Entity technologyOperation = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(technologyOperationId);
        List<Entity> workstationEntities = technologyOperation.getHasManyField(TechnologyOperationComponentFields.WORKSTATIONS);
        return workstationEntities.stream().map(w -> w.getId()).collect(Collectors.toList());
    }


    public List<Long> getWorkstationsIdsForOperation(Long operationId) {
        Entity operation = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION).get(operationId);
        List<Entity> workstationEntities = operation.getHasManyField(OperationFields.WORKSTATIONS);
        return workstationEntities.stream().map(w -> w.getId()).collect(Collectors.toList());
    }

    public WorkstationsResponse getWorkstations(String query, Long tocId) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("Select id as id, number as number, name as name From basic_workstation WHERE ");
        queryBuilder.append(" active = true ");
        Map<String, Object> parameters = Maps.newHashMap();

        if (Objects.nonNull(tocId)) {
            List<Long> ids = getWorkstationsIds(tocId);
            if (ids.isEmpty()) {
                queryBuilder.append(" AND id = -1 ");
            } else {
                queryBuilder.append(" AND id in (:ids) ");
                parameters.put("ids", ids);
            }
        }
        queryBuilder.append(" AND number ilike :query ORDER BY number ASC LIMIT 10 ");

        String ilikeQuery = "%" + query + "%";
        parameters.put("query", ilikeQuery);
        List<WorkstationDto> workstations = jdbcTemplate.query(queryBuilder.toString(), parameters, new BeanPropertyRowMapper(
                WorkstationDto.class));
        WorkstationsResponse workstationsResponse = new WorkstationsResponse();
        workstationsResponse.setWorkstations(workstations);
        return workstationsResponse;
    }

    public WorkstationsGridResponse getWorkstations(int limit, int offset, String sort, String order, String search, Long tocId, Long operation) {
        StringBuilder query = new StringBuilder();
        query.append("Select w.id as id, w.number as number, w.name as name From basic_workstation w WHERE ");
        query.append(" w.active = true ");

        StringBuilder queryCount = new StringBuilder();
        queryCount.append("SELECT COUNT(*) ");
        queryCount.append("From basic_workstation w WHERE w.active = true  ");
        Map<String, Object> parameters = Maps.newHashMap();

        appendWorkstationConditions(tocId, operation, search, query, parameters);
        appendWorkstationConditions(tocId, operation,search, queryCount, parameters);

        if (StringUtils.isNotEmpty(sort)) {
            query.append(" ORDER BY " + sort + " " + order);
        }
        query.append(String.format(" LIMIT %d OFFSET %d", limit, offset));

        Integer countRecords = jdbcTemplate.queryForObject(queryCount.toString(), parameters, Long.class).intValue();

        List<WorkstationDto> workstations = jdbcTemplate.query(query.toString(), parameters, new BeanPropertyRowMapper(
                WorkstationDto.class));

        return new WorkstationsGridResponse(countRecords, workstations);
    }

    private void appendWorkstationConditions(Long tocId, Long operation, String search, StringBuilder query, Map<String, Object> parameters) {
        if (StringUtils.isNotEmpty(search)) {
            query.append(" AND (");
            query.append("UPPER(w.number) LIKE '%").append(search.toUpperCase()).append("%' OR ");
            query.append("UPPER(w.name) LIKE '%").append(search.toUpperCase()).append("%' ");
            query.append(") ");
        }
        if (Objects.nonNull(tocId)) {
            List<Long> ids = getWorkstationsIds(tocId);
            if (ids.isEmpty()) {
                query.append(" AND w.id = -1 ");
            } else {
                query.append(" AND w.id in (:ids) ");
                parameters.put("ids", ids);
            }
        }

        if (Objects.nonNull(operation)) {
            List<Long> ids = getWorkstationsIdsForOperation(operation);
            if (ids.isEmpty()) {
                query.append(" AND w.id = -1 ");
            } else {
                query.append(" AND w.id in (:ids) ");
                parameters.put("ids", ids);
            }
        }
    }

    private Map<Long, List<WorkstionTechnologyOperationComponentDto>> getWorkstionsByTechnologyOperationComponents(List<Long> ids) {
        StringBuilder query = new StringBuilder();
        query.append("select toc.id AS technologyOperationComponentId, w.name as workstationName, w.number as workstationNumber, ");
        query.append("w.id AS workstationId from jointable_technologyoperationcomponent_workstation jtw ");
        query.append("left join technologies_technologyoperationcomponent toc ON toc.id = jtw.technologyoperationcomponent_id ");
        query.append("left join basic_workstation w ON w.id = jtw.workstation_id ");
        query.append("where toc.id in (:ids)");
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("ids", ids);
        List<WorkstionTechnologyOperationComponentDto> workstionsTechnologyOperationComponents = jdbcTemplate.query(
                query.toString(), parameters, new BeanPropertyRowMapper(WorkstionTechnologyOperationComponentDto.class));
        return workstionsTechnologyOperationComponents.stream().collect(
                Collectors.groupingBy(WorkstionTechnologyOperationComponentDto::getTechnologyOperationComponentId));
    }

    public WorkstationTypesResponse getWorkstationTypes(String query) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("Select id as id, number as number, name as name From basic_workstationtype WHERE ");
        queryBuilder.append(" active = true ");
        Map<String, Object> parameters = Maps.newHashMap();

        queryBuilder.append(" AND number ilike :query ORDER BY number ASC LIMIT 10 ");

        String ilikeQuery = "%" + query + "%";
        parameters.put("query", ilikeQuery);

        List<WorkstationTypeDto> workstationTypes = jdbcTemplate.query(queryBuilder.toString(), parameters, new BeanPropertyRowMapper(
                WorkstationTypeDto.class));
        return new WorkstationTypesResponse(workstationTypes);
    }

    public WorkstationTypesGridResponse getWorkstationTypes(int limit, int offset, String sort, String order, String search) {
        StringBuilder query = new StringBuilder();
        query.append("Select w.id as id, w.number as number, w.name as name From basic_workstationtype w WHERE ");
        query.append(" w.active = true ");

        StringBuilder queryCount = new StringBuilder();
        queryCount.append("SELECT COUNT(*) ");
        queryCount.append("From basic_workstationtype w WHERE w.active = true  ");
        Map<String, Object> parameters = Maps.newHashMap();

        appendWorkstationTypesConditions(search, query, parameters);
        appendWorkstationTypesConditions(search, queryCount, parameters);

        if (StringUtils.isNotEmpty(sort)) {
            query.append(" ORDER BY " + sort + " " + order);
        }
        query.append(String.format(" LIMIT %d OFFSET %d", limit, offset));

        Integer countRecords = jdbcTemplate.queryForObject(queryCount.toString(), parameters, Long.class).intValue();

        List<WorkstationTypeDto> workstations = jdbcTemplate.query(query.toString(), parameters, new BeanPropertyRowMapper(
                WorkstationTypeDto.class));

        return new WorkstationTypesGridResponse(countRecords, workstations);
    }

    private void appendWorkstationTypesConditions(String search, StringBuilder query, Map<String, Object> parameters) {
        if (StringUtils.isNotEmpty(search)) {
            query.append(" AND (");
            query.append("UPPER(w.number) LIKE '%").append(search.toUpperCase()).append("%' OR ");
            query.append("UPPER(w.name) LIKE '%").append(search.toUpperCase()).append("%' ");
            query.append(") ");
        }
    }

}
