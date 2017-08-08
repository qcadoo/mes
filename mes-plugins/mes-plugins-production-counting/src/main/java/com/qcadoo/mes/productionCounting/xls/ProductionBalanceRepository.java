package com.qcadoo.mes.productionCounting.xls;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode;
import com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.xls.dto.LaborTimeDetails;
import com.qcadoo.mes.productionCounting.xls.dto.MaterialCost;
import com.qcadoo.mes.productionCounting.xls.dto.PieceworkDetails;
import com.qcadoo.mes.productionCounting.xls.dto.ProducedQuantities;
import com.qcadoo.mes.productionCounting.xls.dto.ProductionCost;
import com.qcadoo.model.api.Entity;

@Repository
class ProductionBalanceRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductionBalanceRepository.class);

    private static final String CUMULATED_PLANNED_QUANTITY_CLAUSE = "SUM(pcq.plannedquantity) ";

    private static final String CUMULATED_USED_QUANTITY_CLAUSE = "MIN(bpc.usedquantity) ";

    private static final String FOREACH_PLANNED_QUANTITY_CLAUSE = "MIN(pcq.plannedquantity) ";

    private static final String FOREACH_USED_QUANTITY_CLAUSE = "SUM(topic.usedquantity) ";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    List<MaterialCost> getMaterialCosts(Entity entity, List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();
        appendMaterialCostsSelectionClause(query);
        appendQuantityAndCosts(entity, query, CUMULATED_PLANNED_QUANTITY_CLAUSE, CUMULATED_USED_QUANTITY_CLAUSE);
        query.append("NULL AS operationNumber ");
        appendMaterialCostsFromClause(query);
        query.append(
                "JOIN basicproductioncounting_basicproductioncounting bpc ON bpc.product_id = p.id AND bpc.order_id = o.id ");
        appendMaterialCostsWhereClause(query);
        query.append("AND o.typeofproductionrecording = '02cumulated' ");
        query.append("GROUP BY o.number, p.number, p.name, p.unit, topic.wasteunit ");
        query.append("UNION ");
        appendMaterialCostsSelectionClause(query);
        appendQuantityAndCosts(entity, query, FOREACH_PLANNED_QUANTITY_CLAUSE, FOREACH_USED_QUANTITY_CLAUSE);
        query.append("op.number AS operationNumber ");
        appendMaterialCostsFromClause(query);
        query.append("JOIN technologies_operation op ON toc.operation_id = op.id ");
        appendMaterialCostsWhereClause(query);
        query.append("AND o.typeofproductionrecording = '03forEach' ");
        query.append("GROUP BY o.number, op.number, p.number, p.name, p.unit, topic.wasteunit ");
        query.append("ORDER BY orderNumber, operationNumber, productNumber ");

        LOGGER.info("---------" + query.toString());

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds), BeanPropertyRowMapper.newInstance(MaterialCost.class));
    }

    private void appendMaterialCostsWhereClause(StringBuilder query) {
        query.append("WHERE pcq.role = '01used' AND pcq.typeofmaterial = '01component' AND t.id IS NULL AND ");
        appendWhereClause(query);
    }

    private void appendMaterialCostsFromClause(StringBuilder query) {
        query.append("FROM orders_order o ");
        query.append("JOIN basicproductioncounting_productioncountingquantity pcq ON pcq.order_id = o.id ");
        query.append("JOIN technologies_technologyoperationcomponent toc ON pcq.technologyoperationcomponent_id = toc.id ");
        query.append("JOIN basic_product p ON pcq.product_id = p.id ");
        query.append("LEFT JOIN technologies_technology t ON t.product_id = p.id AND t.master = TRUE ");
        query.append(
                "LEFT JOIN productioncounting_productiontracking pt ON pt.order_id = o.id AND pt.technologyoperationcomponent_id = toc.id AND pt.state = '02accepted' ");
        query.append(
                "LEFT JOIN productioncounting_trackingoperationproductincomponent topic ON topic.productiontracking_id = pt.id AND topic.product_id = p.id ");
    }

    private void appendMaterialCostsSelectionClause(StringBuilder query) {
        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("p.number AS productNumber, ");
        query.append("p.name AS productName, ");
        query.append("p.unit AS productUnit, ");
        query.append("topic.wasteunit AS usedWasteUnit, ");
    }

    private void appendQuantityAndCosts(Entity entity, StringBuilder query, String plannedQuantityClause,
            String usedQuantityClause) {
        query.append("COALESCE(" + plannedQuantityClause + ",0) AS plannedQuantity, ");
        query.append("COALESCE(" + usedQuantityClause + ",0) AS usedQuantity, ");
        query.append("COALESCE(" + usedQuantityClause + "- " + plannedQuantityClause + ",0) AS quantitativeDeviation, ");
        if (SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT.getStringValue()
                .equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_MATERIAL_COSTS))) {
            appendGlobalDefinitionsCosts(query, entity.getStringField(ProductionBalanceFields.CALCULATE_MATERIAL_COSTS_MODE),
                    plannedQuantityClause, usedQuantityClause);
        } else if (SourceOfMaterialCosts.FROM_ORDERS_MATERIAL_COSTS.getStringValue()
                .equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_MATERIAL_COSTS))) {
            appendOrdersMaterialCosts(query, plannedQuantityClause, usedQuantityClause);
        }
        query.append("COALESCE(SUM(topic.wasteusedquantity), 0) AS usedWasteQuantity, ");
    }

    void appendOrdersMaterialCosts(StringBuilder query, String plannedQuantityClause, String usedQuantityClause) {
        // TODO KRNA add logic when KASI do sth with TKW
        query.append("COALESCE(" + plannedQuantityClause + "* NULL / " + usedQuantityClause + ",0) AS plannedCost, ");
        query.append("COALESCE(NULL, 0) AS realCost, ");
        query.append("COALESCE(NULL - " + plannedQuantityClause + "* NULL / " + usedQuantityClause + ",0) AS valueDeviation, ");
    }

    void appendGlobalDefinitionsCosts(StringBuilder query, String calculateMaterialCostsMode, String plannedQuantityClause,
            String usedQuantityClause) {
        switch (CalculateMaterialCostsMode.parseString(calculateMaterialCostsMode)) {
            case NOMINAL:
                insertComponentPrice(query, "MIN(p.nominalcost) ", plannedQuantityClause, usedQuantityClause);
                break;
            case AVERAGE:
                insertComponentPrice(query, "MIN(p.averagecost) ", plannedQuantityClause, usedQuantityClause);
                break;
            case LAST_PURCHASE:
                insertComponentPrice(query, "MIN(p.lastpurchasecost) ", plannedQuantityClause, usedQuantityClause);
                break;
            case AVERAGE_OFFER_COST:
                insertComponentPrice(query, "MIN(p.averageoffercost) ", plannedQuantityClause, usedQuantityClause);
                break;
            case LAST_OFFER_COST:
                insertComponentPrice(query, "MIN(p.lastoffercost) ", plannedQuantityClause, usedQuantityClause);
                break;
            default:
                throw new IllegalStateException("Unsupported calculateMaterialCostsMode: " + calculateMaterialCostsMode);
        }
    }

    void insertComponentPrice(StringBuilder query, String componentPriceClause, String plannedQuantityClause,
            String usedQuantityClause) {
        query.append("COALESCE(" + plannedQuantityClause + " * " + componentPriceClause + ",0) AS plannedCost, ");
        query.append("COALESCE(" + usedQuantityClause + " * " + componentPriceClause + ",0) AS realCost, ");
        query.append("COALESCE(" + usedQuantityClause + " * " + componentPriceClause + " - ");
        query.append(plannedQuantityClause + " * " + componentPriceClause + ",0) AS valueDeviation, ");
    }

    List<ProducedQuantities> getProducedQuantities(final List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();
        query.append("select o.number as orderNumber, product.number as productNumber, product.name as productName, o.plannedquantity AS plannedQuantity, ");
        query.append("COALESCE(SUM(topoc.usedquantity),0) AS producedQuantity,  ");
        query.append("COALESCE(SUM(topoc.wastesquantity),0) AS wastesQuantity, COALESCE(SUM(wasteTopoc.usedquantity), 0) AS producedWastes, ");
        query.append("COALESCE(SUM(topoc.usedquantity),0) - o.plannedQuantity AS deviation, product.unit AS productUnit ");
        query.append("from orders_order o ");
        query.append("join basic_product product ON o.product_id = product.id ");
        query.append("left join productioncounting_productiontracking pt ON pt.order_id = o.id ");
        query.append("join productioncounting_trackingoperationproductoutcomponent topoc ON topoc.productiontracking_id = pt.id AND topoc.product_id = product.id ");
        query.append("left join basicproductioncounting_productioncountingquantity pcq ON pcq.order_id = o.id AND pcq.typeofmaterial = '04waste' AND pcq.role = '02produced' ");
        query.append("left join productioncounting_trackingoperationproductoutcomponent wasteTopoc ON wasteTopoc.productiontracking_id = pt.id AND wasteTopoc.product_id = pcq.product_id ");
        query.append("where ");
        appendWhereClause(query);
        query.append("group by orderNumber, productNumber, productName, o.plannedQuantity, productUnit");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds), BeanPropertyRowMapper.newInstance(ProducedQuantities.class));
    }

    List<PieceworkDetails> getPieceworkDetails(List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("  o.number                        AS orderNumber, ");
        query.append("  op.number                       AS operationNumber, ");
        query.append("  SUM(pt.executedoperationcycles) AS totalexecutedoperationcycles ");
        query.append("FROM orders_order o ");
        query.append("  JOIN productioncounting_productiontracking pt ON o.id = pt.order_id ");
        query.append("  JOIN technologies_technologyoperationcomponent toc ON pt.technologyoperationcomponent_id = toc.id ");
        query.append("  JOIN technologies_operation op ON toc.operation_id = op.id ");
        query.append("WHERE ");
        appendWhereClause(query);
        query.append("  AND o.typeofproductionrecording = '03forEach' AND pt.state = '02accepted' ");
        query.append("GROUP BY orderNumber, operationNumber");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(PieceworkDetails.class));
    }

    List<LaborTimeDetails> getLaborTimeDetails(List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("  o.number           AS orderNumber, ");
        query.append("  op.number          AS operationNumber, ");
        query.append("  stf.number         AS staffNumber, ");
        query.append("  stf.name           AS staffName, ");
        query.append("  stf.surname        AS staffSurname, ");
        query.append("  SUM(swt.labortime) AS laborTime ");
        query.append("FROM orders_order o ");
        query.append("  JOIN productioncounting_productiontracking pt ON o.id = pt.order_id ");
        query.append("  JOIN productioncounting_staffworktime swt ON pt.id = swt.productionrecord_id ");
        query.append("  JOIN basic_staff stf ON swt.worker_id = stf.id ");
        query.append("  LEFT JOIN technologies_technologyoperationcomponent toc ON pt.technologyoperationcomponent_id = toc.id ");
        query.append("  LEFT JOIN technologies_operation op ON toc.operation_id = op.id ");
        query.append("WHERE ");
        appendWhereClause(query);
        query.append("  AND pt.state = '02accepted' ");
        query.append("GROUP BY orderNumber, operationNumber, staffNumber, staffName, staffSurname");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(LaborTimeDetails.class));
    }

    List<ProductionCost> getProductionCosts(List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("op.number AS operationNumber, ");
        query.append("0 AS plannedStaffTime, ");
        query.append("COALESCE(SUM(swt.labortime), 0) AS realStaffTime, ");
        query.append("0 AS plannedMachineTime, ");
        query.append("COALESCE(SUM(pt.machinetime), 0) AS realMachineTime, ");
        query.append("0 AS plannedStaffCosts, ");
        query.append("0 AS realStaffCosts, ");
        query.append("0 AS staffCostsDeviation, ");
        query.append("0 AS plannedMachineCosts, ");
        query.append("0 AS realMachineCosts, ");
        query.append("0 AS machineCostsDeviation, ");
        query.append("0 AS plannedPieceworkCosts, ");
        query.append("0 AS realPieceworkCosts, ");
        query.append("0 AS plannedCostsSum, ");
        query.append("0 AS realCostsSum, ");
        query.append("0 AS sumCostsDeviation ");
        query.append("FROM orders_order o ");
        query.append("JOIN productioncounting_productiontracking pt ON o.id = pt.order_id ");
        query.append("JOIN productioncounting_staffworktime swt ON pt.id = swt.productionrecord_id ");
        query.append("LEFT JOIN technologies_technologyoperationcomponent toc ON pt.technologyoperationcomponent_id = toc.id ");
        query.append("LEFT JOIN technologies_operation op ON toc.operation_id = op.id ");
        query.append("WHERE ");
        appendWhereClause(query);
        query.append("AND pt.state = '02accepted' ");
        query.append("GROUP BY orderNumber, operationNumber ");
        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(ProductionCost.class));
    }

    private void appendWhereClause(StringBuilder query) {
        query.append("o.id IN (:ordersIds) ");
    }
}
