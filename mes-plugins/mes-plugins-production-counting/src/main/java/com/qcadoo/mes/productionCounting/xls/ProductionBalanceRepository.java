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
import com.qcadoo.mes.costCalculation.constants.SourceOfOperationCosts;
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

    private void appendOrdersMaterialCosts(StringBuilder query, String plannedQuantityClause, String usedQuantityClause) {
        // TODO KRNA add logic when KASI do sth with TKW
        query.append("COALESCE(" + plannedQuantityClause + "* NULL / " + usedQuantityClause + ",0) AS plannedCost, ");
        query.append("COALESCE(NULL, 0) AS realCost, ");
        query.append("COALESCE(NULL - " + plannedQuantityClause + "* NULL / " + usedQuantityClause + ",0) AS valueDeviation, ");
    }

    private void appendGlobalDefinitionsCosts(StringBuilder query, String calculateMaterialCostsMode, String plannedQuantityClause,
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

    private void insertComponentPrice(StringBuilder query, String componentPriceClause, String plannedQuantityClause,
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
        query.append("o.number                        AS orderNumber, ");
        query.append("op.number                       AS operationNumber, ");
        query.append("COALESCE(SUM(pt.executedoperationcycles), 0) AS totalexecutedoperationcycles ");
        query.append("FROM orders_order o ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON o.id = pt.order_id ");
        query.append("LEFT JOIN technologies_technologyoperationcomponent toc ON pt.technologyoperationcomponent_id = toc.id ");
        query.append("LEFT JOIN technologies_operation op ON toc.operation_id = op.id ");
        query.append("WHERE o.typeofproductionrecording = '03forEach' AND pt.state = '02accepted' AND ");
        appendWhereClause(query);
        query.append("GROUP BY orderNumber, operationNumber ");

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
        query.append("  COALESCE(SUM(swt.labortime), 0) AS laborTime ");
        query.append("FROM orders_order o ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON o.id = pt.order_id ");
        query.append("LEFT JOIN productioncounting_staffworktime swt ON pt.id = swt.productionrecord_id ");
        query.append("LEFT JOIN basic_staff stf ON swt.worker_id = stf.id ");
        query.append("LEFT JOIN technologies_technologyoperationcomponent toc ON pt.technologyoperationcomponent_id = toc.id ");
        query.append("LEFT JOIN technologies_operation op ON toc.operation_id = op.id ");
        query.append("WHERE pt.state = '02accepted' AND ");
        appendWhereClause(query);
        query.append("GROUP BY orderNumber, operationNumber, staffNumber, staffName, staffSurname ");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(LaborTimeDetails.class));
    }

    List<ProductionCost> getProductionCosts(Entity entity, List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("NULL AS operationNumber, ");
        appendPlannedStaffTime(entity, query);
        query.append("AS plannedStaffTime, ");
        query.append("COALESCE(SUM(pt.labortime), 0) AS realStaffTime, ");
        appendPlannedMachineTime(entity, query);
        query.append("AS plannedMachineTime, ");
        query.append("COALESCE(SUM(pt.machinetime), 0) AS realMachineTime, ");
        appendCumulatedPlannedStaffCosts(entity, query);
        query.append("AS plannedStaffCosts, ");
        appendCumulatedRealStaffCosts(query);
        query.append("AS realStaffCosts, ");
        query.append("COALESCE( ");
        appendCumulatedRealStaffCosts(query);
        query.append("- ");
        appendCumulatedPlannedStaffCosts(entity, query);
        query.append(", 0) AS staffCostsDeviation, ");
        appendCumulatedPlannedMachineCosts(entity, query);
        query.append("AS plannedMachineCosts, ");
        appendCumulatedRealMachineCosts(query);
        query.append("AS realMachineCosts, ");
        query.append("COALESCE( ");
        appendCumulatedRealMachineCosts(query);
        query.append("- ");
        appendCumulatedPlannedMachineCosts(entity, query);
        query.append(", 0) AS machineCostsDeviation, ");
        query.append("0 AS plannedPieceworkCosts, ");
        query.append("0 AS realPieceworkCosts, ");
        appendCumulatedPlannedStaffCosts(entity, query);
        query.append("+ ");
        appendCumulatedPlannedMachineCosts(entity, query);
        query.append("AS plannedCostsSum, ");
        appendCumulatedRealStaffCosts(query);
        query.append("+ ");
        appendCumulatedRealMachineCosts(query);
        query.append("AS realCostsSum, ");
        appendCumulatedRealStaffCosts(query);
        query.append("+ ");
        appendCumulatedRealMachineCosts(query);
        query.append("- ");
        appendCumulatedPlannedStaffCosts(entity, query);
        query.append("- ");
        appendCumulatedPlannedMachineCosts(entity, query);
        query.append("AS sumCostsDeviation ");
        query.append("FROM orders_order o ");
        query.append("JOIN technologies_technology t ON o.technology_id = t.id ");
        query.append("JOIN technologies_technologyoperationcomponent toc ON toc.technology_id = t.id ");
        query.append("LEFT JOIN basicproductioncounting_productioncountingoperationrun pcor ON pcor.order_id = o.id AND pcor.technologyoperationcomponent_id = toc.id ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON o.id = pt.order_id ");
        query.append("CROSS JOIN basic_parameter bp ");
        query.append("WHERE ");
        appendWhereClause(query);
        query.append("AND pt.state = '02accepted' AND o.typeofproductionrecording = '02cumulated' ");
        query.append("GROUP BY orderNumber ");
        query.append("UNION ");
        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("op.number AS operationNumber, ");
        appendPlannedStaffTime(entity, query);
        query.append("AS plannedStaffTime, ");
        query.append("COALESCE(SUM(pt.labortime), 0) AS realStaffTime, ");
        appendPlannedMachineTime(entity, query);
        query.append("AS plannedMachineTime, ");
        query.append("COALESCE(SUM(pt.machinetime), 0) AS realMachineTime, ");
        appendForEachPlannedStaffCosts(entity, query);
        query.append("AS plannedStaffCosts, ");
        appendForEachRealStaffCosts(entity, query);
        query.append("AS realStaffCosts, ");
        query.append("COALESCE( ");
        appendForEachRealStaffCosts(entity, query);
        query.append("- ");
        appendForEachPlannedStaffCosts(entity, query);
        query.append(", 0) AS staffCostsDeviation, ");
        appendForEachPlannedMachineCosts(entity, query);
        query.append("AS plannedMachineCosts, ");
        appendForEachRealMachineCosts(entity, query);
        query.append("AS realMachineCosts, ");
        query.append("COALESCE( ");
        appendForEachRealMachineCosts(entity, query);
        query.append("- ");
        appendForEachPlannedMachineCosts(entity, query);
        query.append(", 0) AS machineCostsDeviation, ");
        query.append("COALESCE(MIN(pcor.runs / toc.numberofoperations * toc.pieceworkcost), 0) AS plannedPieceworkCosts, ");
        query.append("COALESCE(SUM(pt.executedoperationcycles) / MIN(toc.numberofoperations) * MIN(toc.pieceworkcost), 0) AS realPieceworkCosts, ");
        appendForEachPlannedStaffCosts(entity, query);
        query.append("+ ");
        appendForEachPlannedMachineCosts(entity, query);
        query.append("AS plannedCostsSum, ");
        appendForEachRealStaffCosts(entity, query);
        query.append("+ ");
        appendForEachRealMachineCosts(entity, query);
        query.append("AS realCostsSum, ");
        appendForEachRealStaffCosts(entity, query);
        query.append("+ ");
        appendForEachRealMachineCosts(entity, query);
        query.append("- ");
        appendForEachPlannedStaffCosts(entity, query);
        query.append("- ");
        appendForEachPlannedMachineCosts(entity, query);
        query.append("AS sumCostsDeviation ");
        query.append("FROM orders_order o ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON o.id = pt.order_id ");
        query.append("LEFT JOIN technologies_technologyoperationcomponent toc ON pt.technologyoperationcomponent_id = toc.id ");
        query.append("LEFT JOIN basicproductioncounting_productioncountingoperationrun pcor ON pcor.order_id = o.id AND pcor.technologyoperationcomponent_id = toc.id ");
        query.append("LEFT JOIN technologies_operation op ON toc.operation_id = op.id ");
        query.append("CROSS JOIN basic_parameter bp ");
        query.append("WHERE ");
        appendWhereClause(query);
        query.append("AND pt.state = '02accepted' AND o.typeofproductionrecording = '03forEach' ");
        query.append("GROUP BY orderNumber, operationNumber ");

        LOGGER.info("---------" + query.toString());

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(ProductionCost.class));
    }

    private void appendForEachRealMachineCosts(Entity entity, StringBuilder query) {
        query.append("COALESCE(SUM(pt.machinetime) / 3600 * ");
        appendForEachMachineHourCost(entity, query);
        query.append(", 0) ");
    }

    private void appendForEachPlannedMachineCosts(Entity entity, StringBuilder query) {
        query.append("COALESCE( ");
        appendPlannedMachineTime(entity, query);
        query.append("/ 3600 * ");
        appendForEachMachineHourCost(entity, query);
        query.append(", 0) ");
    }

    private void appendForEachRealStaffCosts(Entity entity, StringBuilder query) {
        query.append("COALESCE(SUM(pt.labortime) / 3600 * ");
        appendForEachStaffHourCost(entity, query);
        query.append(", 0) ");
    }

    private void appendForEachPlannedStaffCosts(Entity entity, StringBuilder query) {
        query.append("COALESCE( ");
        appendPlannedStaffTime(entity, query);
        query.append("/ 3600 * ");
        appendForEachStaffHourCost(entity, query);
        query.append(", 0) ");
    }

    private void appendCumulatedRealMachineCosts(StringBuilder query) {
        query.append("COALESCE(SUM(pt.machinetime) / 3600 * ");
        appendCumulatedMachineHourCost(query);
        query.append(", 0) ");
    }

    private void appendCumulatedPlannedMachineCosts(Entity entity, StringBuilder query) {
        query.append("COALESCE( ");
        appendPlannedMachineTime(entity, query);
        query.append("/ 3600 * ");
        appendCumulatedMachineHourCost(query);
        query.append(", 0) ");
    }

    private void appendCumulatedRealStaffCosts(StringBuilder query) {
        query.append("COALESCE(SUM(pt.labortime) / 3600 * ");
        appendCumulatedStaffHourCost(query);
        query.append(", 0) ");
    }

    private void appendCumulatedPlannedStaffCosts(Entity entity, StringBuilder query) {
        query.append("COALESCE( ");
        appendPlannedStaffTime(entity, query);
        query.append("/ 3600 * ");
        appendCumulatedStaffHourCost(query);
        query.append(", 0) ");
    }

    private void appendPlannedMachineTime(Entity entity, StringBuilder query) {
        query.append("COALESCE(SUM(toc.tj * pcor.runs * toc.machineutilization ");
        appendTPZandAdditionalTime(entity, query);
        query.append("), 0) ");
    }

    private void appendPlannedStaffTime(Entity entity, StringBuilder query) {
        query.append("COALESCE(SUM(toc.tj * pcor.runs * toc.laborutilization ");
        appendTPZandAdditionalTime(entity, query);
        query.append("), 0) ");
    }

    private void appendCumulatedStaffHourCost(StringBuilder query) {
        query.append("MIN(bp.averagelaborhourlycostpb) ");
    }

    private void appendCumulatedMachineHourCost(StringBuilder query) {
        query.append("MIN(bp.averagemachinehourlycostpb) ");
    }

    private void appendForEachStaffHourCost(Entity entity, StringBuilder query) {
        if(SourceOfOperationCosts.TECHNOLOGY_OPERATION.getStringValue().equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS_PB))){
            query.append("MIN(toc.laborhourlycost) ");
        } else if(SourceOfOperationCosts.PARAMETERS.getStringValue().equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS_PB))){
            query.append("MIN(bp.averagelaborhourlycostpb) ");
        }
    }

    private void appendForEachMachineHourCost(Entity entity, StringBuilder query) {
        if(SourceOfOperationCosts.TECHNOLOGY_OPERATION.getStringValue().equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS_PB))){
            query.append("MIN(toc.machinehourlycost) ");
        } else if(SourceOfOperationCosts.PARAMETERS.getStringValue().equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS_PB))){
            query.append("MIN(bp.averagemachinehourlycostpb) ");
        }
    }

    private void appendTPZandAdditionalTime(Entity entity, StringBuilder query) {
        if(entity.getBooleanField(ProductionBalanceFields.INCLUDE_TPZ)){
            query.append("+ toc.tpz ");
        }
        if(entity.getBooleanField(ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME)){
            query.append("+ toc.timenextoperation ");
        }
    }

    private void appendWhereClause(StringBuilder query) {
        query.append("o.id IN (:ordersIds) ");
    }
}
