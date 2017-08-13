package com.qcadoo.mes.productionCounting.xls;

import java.util.Collections;
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
import com.qcadoo.mes.productionCounting.xls.dto.OrderBalance;
import com.qcadoo.mes.productionCounting.xls.dto.PieceworkDetails;
import com.qcadoo.mes.productionCounting.xls.dto.PlannedQuantity;
import com.qcadoo.mes.productionCounting.xls.dto.ProducedQuantity;
import com.qcadoo.mes.productionCounting.xls.dto.ProductionCost;
import com.qcadoo.model.api.Entity;

@Repository
class ProductionBalanceRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductionBalanceRepository.class);

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    List<ProducedQuantity> getProducedQuantities(final List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("prod.number AS productNumber, ");
        query.append("prod.name AS productName, ");
        query.append("MIN(o.plannedquantity) AS plannedQuantity, ");
        appendProducedQuantity(query);
        query.append("AS producedQuantity, ");
        query.append("COALESCE(SUM(topoc.wastesquantity), 0) AS wastesQuantity, ");
        query.append("COALESCE(SUM(wasteTopoc.usedquantity), 0) AS producedWastes, ");
        appendProducedQuantity(query);
        query.append("- MIN(o.plannedQuantity) AS deviation, ");
        query.append("prod.unit AS productUnit ");
        query.append("FROM orders_order o ");
        query.append("JOIN basic_product prod ON o.product_id = prod.id ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON pt.order_id = o.id AND pt.state = '02accepted' ");
        query.append(
                "LEFT JOIN productioncounting_trackingoperationproductoutcomponent topoc ON topoc.productiontracking_id = pt.id AND topoc.product_id = prod.id ");
        query.append(
                "LEFT JOIN basicproductioncounting_productioncountingquantity pcq ON pcq.order_id = o.id AND pcq.typeofmaterial = '04waste' AND pcq.role = '02produced' ");
        query.append(
                "LEFT JOIN productioncounting_trackingoperationproductoutcomponent wasteTopoc ON wasteTopoc.productiontracking_id = pt.id AND wasteTopoc.product_id = pcq.product_id ");
        appendWhereClause(query);
        query.append("GROUP BY orderNumber, productNumber, productName, productUnit");
        //FIXME KAMA kiedy będzie 2 odpady ilosc wyprodukowana wzrośnie 2 krotnie ?

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(ProducedQuantity.class));
    }

    private void appendProducedQuantity(StringBuilder query) {
        query.append("COALESCE(SUM(topoc.usedquantity), 0) ");
    }

    private void appendWhereClause(StringBuilder query) {
        query.append("WHERE o.id IN (:ordersIds) ");
    }

    private List<PlannedQuantity> getCumulatedPlannedQuantites(List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("o.id AS orderId, ");
        query.append("p.id AS productId, ");
        query.append("COALESCE(SUM(pcq.plannedquantity), 0) AS plannedQuantity, ");
        query.append("NULL AS operationId ");
        query.append("FROM orders_order o ");
        query.append("JOIN basicproductioncounting_productioncountingquantity pcq ON pcq.order_id = o.id ");
        query.append("JOIN basic_product p ON pcq.product_id = p.id ");
        query.append("LEFT JOIN technologies_technology t ON t.product_id = p.id AND t.master = TRUE ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '02cumulated' ");
        query.append("AND pcq.role = '01used' AND pcq.typeofmaterial = '01component' AND t.id IS NULL ");
        query.append("GROUP BY o.id, p.id ");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(PlannedQuantity.class));
    }

    private List<PlannedQuantity> getForEachPlannedQuantites(List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("o.id AS orderId, ");
        query.append("p.id AS productId, ");
        query.append("COALESCE(SUM(pcq.plannedquantity), 0) AS plannedQuantity, ");
        query.append("toc.operation_id AS operationId ");
        query.append("FROM orders_order o ");
        query.append("JOIN basicproductioncounting_productioncountingquantity pcq ON pcq.order_id = o.id ");
        query.append("JOIN basic_product p ON pcq.product_id = p.id ");
        query.append("LEFT JOIN technologies_technology t ON t.product_id = p.id AND t.master = TRUE ");
        query.append("JOIN technologies_technologyoperationcomponent toc ON pcq.technologyoperationcomponent_id = toc.id ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '03forEach' ");
        query.append("AND pcq.role = '01used' AND pcq.typeofmaterial = '01component' AND t.id IS NULL ");
        query.append("GROUP BY o.id, toc.operation_id, p.id ");
        
        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(PlannedQuantity.class));
    }

    List<MaterialCost> getMaterialCosts(Entity entity, List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();
        query.append("(WITH planned_quantity (order_id, product_id, quantity) AS (VALUES ");
        List<PlannedQuantity> plannedQuantities = getCumulatedPlannedQuantites(ordersIds);
        for(int i = 0; i< plannedQuantities.size(); i++ ){
            PlannedQuantity plannedQuantity = plannedQuantities.get(i);
            query.append("(" + plannedQuantity.getOrderId() + ", " + plannedQuantity.getProductId() + ", " + plannedQuantity.getPlannedQuantity() + ") ");
            if(i != plannedQuantities.size() - 1){
                query.append(", ");
            }
        }
        query.append(") ");
        appendMaterialCostsSelectionClause(query);
        appendQuantityAndCosts(entity, query);
        query.append("NULL AS operationNumber ");
        appendMaterialCostsFromClause(query);
        query.append(
                "LEFT JOIN productioncounting_productiontracking pt ON pt.order_id = o.id AND pt.state = '02accepted' ");
        query.append(
                "LEFT JOIN productioncounting_trackingoperationproductincomponent topic ON topic.productiontracking_id = pt.id AND topic.product_id = p.id ");
        query.append("GROUP BY o.number, p.number, p.name, p.unit, topic.wasteunit) ");
        query.append("UNION ");
        query.append("(WITH planned_quantity (order_id, operation_id, product_id, quantity) AS (VALUES ");
        plannedQuantities = getForEachPlannedQuantites(ordersIds);
        for(int i = 0; i< plannedQuantities.size(); i++ ){
            PlannedQuantity plannedQuantity = plannedQuantities.get(i);
            query.append("(" + plannedQuantity.getOrderId() + ", " + plannedQuantity.getOperationId() + ", " + plannedQuantity.getProductId() + ", " + plannedQuantity.getPlannedQuantity() + ") ");
            if(i != plannedQuantities.size() - 1){
                query.append(", ");
            }
        }
        query.append(") ");
        appendMaterialCostsSelectionClause(query);
        appendQuantityAndCosts(entity, query);
        query.append("op.number AS operationNumber ");
        appendMaterialCostsFromClause(query);
        query.append("JOIN technologies_operation op ON q.operation_id = op.id ");
        query.append("JOIN technologies_technologyoperationcomponent toc ON toc.operation_id = op.id AND o.technology_id = toc.technology_id ");
        query.append(
                "LEFT JOIN productioncounting_productiontracking pt ON pt.order_id = o.id AND pt.technologyoperationcomponent_id = toc.id AND pt.state = '02accepted' ");
        query.append(
                "LEFT JOIN productioncounting_trackingoperationproductincomponent topic ON topic.productiontracking_id = pt.id AND topic.product_id = p.id ");
        query.append("GROUP BY o.number, op.number, p.number, p.name, p.unit, topic.wasteunit) ");
        query.append("ORDER BY orderNumber, operationNumber, productNumber ");

        return jdbcTemplate.query(query.toString(), Collections.emptyMap(),
                BeanPropertyRowMapper.newInstance(MaterialCost.class));
    }

    private void appendMaterialCostsSelectionClause(StringBuilder query) {
        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("p.number AS productNumber, ");
        query.append("p.name AS productName, ");
        query.append("p.unit AS productUnit, ");
        query.append("topic.wasteunit AS usedWasteUnit, ");
    }

    private void appendMaterialCostsFromClause(StringBuilder query) {
        query.append("FROM orders_order o ");
        query.append("JOIN planned_quantity q ON q.order_id = o.id ");
        query.append("JOIN basic_product p ON q.product_id = p.id ");
    }

    private void appendQuantityAndCosts(Entity entity, StringBuilder query) {
        appendPlannedQuantity(query);
        query.append("AS plannedQuantity, ");
        appendUsedQuantity(query);
        query.append("AS usedQuantity, ");
        appendUsedQuantity(query);
        query.append("- ");
        appendPlannedQuantity(query);
        query.append("AS quantitativeDeviation, ");
        if (SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT.getStringValue()
                .equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_MATERIAL_COSTS))) {
            String componentPriceClause = evaluateComponentPrice(entity.getStringField(ProductionBalanceFields.CALCULATE_MATERIAL_COSTS_MODE));
            appendGlobalDefinitionsCosts(query, componentPriceClause);
        } else if (SourceOfMaterialCosts.FROM_ORDERS_MATERIAL_COSTS.getStringValue()
                .equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_MATERIAL_COSTS))) {
            appendOrdersMaterialCosts(query);
        }
        query.append("COALESCE(SUM(topic.wasteusedquantity), 0) AS usedWasteQuantity, ");
    }

    private String evaluateComponentPrice(String calculateMaterialCostsMode) {
        switch (CalculateMaterialCostsMode.parseString(calculateMaterialCostsMode)) {
            case NOMINAL:
                return "COALESCE(MIN(p.nominalcost), 0) ";
            case AVERAGE:
                return "COALESCE(MIN(p.averagecost), 0) ";
            case LAST_PURCHASE:
                return "COALESCE(MIN(p.lastpurchasecost), 0) ";
            case AVERAGE_OFFER_COST:
                return "COALESCE(MIN(p.averageoffercost), 0) ";
            case LAST_OFFER_COST:
                return "COALESCE(MIN(p.lastoffercost), 0) ";
            default:
                throw new IllegalStateException("Unsupported calculateMaterialCostsMode: " + calculateMaterialCostsMode);
        }
    }

    private void appendGlobalDefinitionsCosts(StringBuilder query, String componentPriceClause) {
        appendPlannedQuantity(query);
        query.append(" * " + componentPriceClause + "AS plannedCost, ");
        appendUsedQuantity(query);
        query.append(" * " + componentPriceClause + "AS realCost, ");
        appendUsedQuantity(query);
        query.append(" * " + componentPriceClause + " - ");
        appendPlannedQuantity(query);
        query.append(" * " + componentPriceClause + "AS valueDeviation, ");
    }

    private void appendOrdersMaterialCosts(StringBuilder query) {
        // TODO KRNA add logic when KASI do sth with TKW
        appendPlannedQuantity(query);
        query.append("* 0 / ");
        appendUsedQuantity(query);
        query.append("AS plannedCost, ");
        query.append("0 AS realCost, ");
        query.append("0 - ");
        appendPlannedQuantity(query);
        query.append("* 0 / ");
        appendUsedQuantity(query);
        query.append("AS valueDeviation, ");
    }

    private void appendPlannedQuantity(StringBuilder query){
        query.append("MIN(q.quantity) ");
    }

    private void appendUsedQuantity(StringBuilder query){
        query.append("COALESCE(SUM(topic.usedquantity), 0) ");
    }

    List<PieceworkDetails> getPieceworkDetails(List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("op.number AS operationNumber, ");
        query.append("COALESCE(SUM(pt.executedoperationcycles), 0) AS totalexecutedoperationcycles ");
        query.append("FROM orders_order o ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON o.id = pt.order_id AND pt.state = '02accepted' ");
        query.append("LEFT JOIN technologies_technologyoperationcomponent toc ON pt.technologyoperationcomponent_id = toc.id ");
        query.append("LEFT JOIN technologies_operation op ON toc.operation_id = op.id ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '03forEach' ");
        query.append("GROUP BY orderNumber, operationNumber ");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(PieceworkDetails.class));
    }

    List<LaborTimeDetails> getLaborTimeDetails(List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("op.number AS operationNumber, ");
        query.append("stf.number AS staffNumber, ");
        query.append("stf.name AS staffName, ");
        query.append("stf.surname AS staffSurname, ");
        query.append("COALESCE(SUM(swt.labortime), 0) AS laborTime ");
        query.append("FROM orders_order o ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON o.id = pt.order_id AND pt.state = '02accepted' ");
        query.append("LEFT JOIN productioncounting_staffworktime swt ON pt.id = swt.productionrecord_id ");
        query.append("LEFT JOIN basic_staff stf ON swt.worker_id = stf.id ");
        query.append("LEFT JOIN technologies_technologyoperationcomponent toc ON pt.technologyoperationcomponent_id = toc.id ");
        query.append("LEFT JOIN technologies_operation op ON toc.operation_id = op.id ");
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
        query.append(
                "LEFT JOIN basicproductioncounting_productioncountingoperationrun pcor ON pcor.order_id = o.id AND pcor.technologyoperationcomponent_id = toc.id ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON o.id = pt.order_id AND pt.state = '02accepted' ");
        query.append("CROSS JOIN basic_parameter bp ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '02cumulated' ");
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
        query.append(
                "COALESCE(SUM(pt.executedoperationcycles) / MIN(toc.numberofoperations) * MIN(toc.pieceworkcost), 0) AS realPieceworkCosts, ");
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
        query.append("LEFT JOIN productioncounting_productiontracking pt ON o.id = pt.order_id AND pt.state = '02accepted' ");
        query.append("LEFT JOIN technologies_technologyoperationcomponent toc ON pt.technologyoperationcomponent_id = toc.id ");
        query.append(
                "LEFT JOIN basicproductioncounting_productioncountingoperationrun pcor ON pcor.order_id = o.id AND pcor.technologyoperationcomponent_id = toc.id ");
        query.append("LEFT JOIN technologies_operation op ON toc.operation_id = op.id ");
        query.append("CROSS JOIN basic_parameter bp ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '03forEach' ");
        query.append("GROUP BY orderNumber, operationNumber ");
        query.append("ORDER BY orderNumber, operationNumber ");

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
        if (SourceOfOperationCosts.TECHNOLOGY_OPERATION.getStringValue()
                .equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS_PB))) {
            query.append("MIN(toc.laborhourlycost) ");
        } else if (SourceOfOperationCosts.PARAMETERS.getStringValue()
                .equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS_PB))) {
            query.append("MIN(bp.averagelaborhourlycostpb) ");
        }
    }

    private void appendForEachMachineHourCost(Entity entity, StringBuilder query) {
        if (SourceOfOperationCosts.TECHNOLOGY_OPERATION.getStringValue()
                .equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS_PB))) {
            query.append("MIN(toc.machinehourlycost) ");
        } else if (SourceOfOperationCosts.PARAMETERS.getStringValue()
                .equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS_PB))) {
            query.append("MIN(bp.averagemachinehourlycostpb) ");
        }
    }

    private void appendTPZandAdditionalTime(Entity entity, StringBuilder query) {
        if (entity.getBooleanField(ProductionBalanceFields.INCLUDE_TPZ)) {
            query.append("+ toc.tpz ");
        }
        if (entity.getBooleanField(ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME)) {
            query.append("+ toc.timenextoperation ");
        }
    }

    List<OrderBalance> getOrdersBalance(Entity entity, List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();
        appendOrdersBalanceSelectionClause(entity, query);
        appendMaterialCostsForOrdersBalance(entity, query);
        query.append("AS materialCosts, ");

        appendCumulatedRealStaffCosts(query);
        query.append("+ ");
        appendCumulatedRealMachineCosts(query);
        query.append("AS productionCosts, ");
        appendCumulatedTechnicalProductionCosts(entity, query);
        query.append("AS technicalProductionCosts, ");
        appendCumulatedMaterialCostMarginValue(entity, query);
        query.append("AS materialCostMarginValue, ");
        appendCumulatedProductionCostMarginValue(entity, query);
        query.append("AS productionCostMarginValue, ");
        appendCumulatedTotalCosts(entity, query);
        query.append("AS totalCosts, ");
        appendCumulatedRegistrationPrice(entity, query);
        query.append("AS registrationPrice, ");
        appendCumulatedRegistrationPriceOverheadValue(entity, query);
        query.append("AS registrationPriceOverheadValue, ");
        appendCumulatedRealProductionCosts(entity, query);
        query.append("AS realProductionCosts, ");
        appendCumulatedProfitValue(entity, query);
        query.append("AS profitValue, ");
        appendCumulatedRealProductionCosts(entity, query);
        query.append("+ ");
        appendCumulatedProfitValue(entity, query);
        query.append("AS sellPrice ");
        query.append("FROM orders_order o ");
        query.append("JOIN basic_product prod ON o.product_id = prod.id ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON pt.order_id = o.id AND pt.state = '02accepted' ");
        query.append(
                "LEFT JOIN productioncounting_trackingoperationproductoutcomponent topoc ON topoc.productiontracking_id = pt.id AND topoc.product_id = prod.id ");
        query.append("JOIN basicproductioncounting_productioncountingquantity pcq ON pcq.order_id = o.id ");
        query.append("JOIN basic_product p ON pcq.product_id = p.id ");
        query.append(
                "LEFT JOIN productioncounting_trackingoperationproductincomponent topic ON topic.productiontracking_id = pt.id AND topic.product_id = pcq.product_id ");

        query.append("JOIN technologies_technology t ON o.technology_id = t.id ");
        query.append("JOIN technologies_technologyoperationcomponent toc ON toc.technology_id = t.id ");
        query.append("CROSS JOIN basic_parameter bp ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '02cumulated' ");
        query.append("GROUP BY orderNumber, productNumber, productName ");
        query.append("UNION ");
        appendOrdersBalanceSelectionClause(entity, query);
        appendMaterialCostsForOrdersBalance(entity, query);
        query.append("AS materialCosts, ");

        appendForEachRealStaffCosts(entity, query);
        query.append("+ ");
        appendForEachRealMachineCosts(entity, query);
        query.append("AS productionCosts, ");
        appendForEachTechnicalProductionCosts(entity, query);
        query.append("AS technicalProductionCosts, ");
        appendForEachMaterialCostMarginValue(entity, query);
        query.append("AS materialCostMarginValue, ");
        appendForEachProductionCostMarginValue(entity, query);
        query.append("AS productionCostMarginValue, ");
        appendForEachTotalCosts(entity, query);
        query.append("AS totalCosts, ");
        appendForEachRegistrationPrice(entity, query);
        query.append("AS registrationPrice, ");
        appendForEachRegistrationPriceOverheadValue(entity, query);
        query.append("AS registrationPriceOverheadValue, ");
        appendForEachRealProductionCosts(entity, query);
        query.append("AS realProductionCosts, ");
        appendForEachProfitValue(entity, query);
        query.append("AS profitValue, ");
        appendForEachRealProductionCosts(entity, query);
        query.append("+ ");
        appendForEachProfitValue(entity, query);
        query.append("AS sellPrice ");
        query.append("FROM orders_order o ");
        query.append("JOIN basic_product prod ON o.product_id = prod.id ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON pt.order_id = o.id AND pt.state = '02accepted' ");
        query.append(
                "LEFT JOIN productioncounting_trackingoperationproductoutcomponent topoc ON topoc.productiontracking_id = pt.id AND topoc.product_id = prod.id ");
        query.append("JOIN basicproductioncounting_productioncountingquantity pcq ON pcq.order_id = o.id ");
        query.append("JOIN basic_product p ON pcq.product_id = p.id ");
        query.append(
                "LEFT JOIN productioncounting_trackingoperationproductincomponent topic ON topic.productiontracking_id = pt.id AND topic.product_id = pcq.product_id ");

        query.append("LEFT JOIN technologies_technologyoperationcomponent toc ON pt.technologyoperationcomponent_id = toc.id ");
        query.append("CROSS JOIN basic_parameter bp ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '03forEach' ");
        query.append("GROUP BY orderNumber, productNumber, productName ");
        query.append("ORDER BY orderNumber ");

        LOGGER.info("---------" + query.toString());

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(OrderBalance.class));
    }

    private void appendOrdersBalanceSelectionClause(Entity entity, StringBuilder query) {
        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("prod.number AS productNumber, ");
        query.append("prod.name AS productName, ");
        appendProducedQuantity(query);
        query.append("AS producedQuantity, ");
        appendMaterialCostMargin(entity, query);
        query.append("AS materialCostMargin, ");
        appendProductionCostMargin(entity, query);
        query.append("AS productionCostMargin, ");
        appendAdditionalOverhead(entity, query);
        query.append("AS additionalOverhead, ");
        appendDirectAdditionalCost(query);
        query.append("AS directAdditionalCost, ");
        appendRegistrationPriceOverhead(entity, query);
        query.append("AS registrationPriceOverhead, ");
        appendProfit(entity, query);
        query.append("AS profit, ");
    }

    private void appendMaterialCostMargin(Entity entity, StringBuilder query) {
        query.append("COALESCE(" + entity.getDecimalField(ProductionBalanceFields.MATERIAL_COST_MARGIN) + ", 0) ");
    }

    private void appendProductionCostMargin(Entity entity, StringBuilder query) {
        query.append("COALESCE(" + entity.getDecimalField(ProductionBalanceFields.PRODUCTION_COST_MARGIN) + ", 0) ");
    }

    private void appendAdditionalOverhead(Entity entity, StringBuilder query) {
        query.append("COALESCE(" + entity.getDecimalField(ProductionBalanceFields.ADDITIONAL_OVERHEAD) + ", 0) ");
    }

    private void appendDirectAdditionalCost(StringBuilder query) {
        query.append("COALESCE(MIN(o.directadditionalcost), 0) ");
    }

    private void appendRegistrationPriceOverhead(Entity entity, StringBuilder query) {
        query.append("COALESCE(" + entity.getDecimalField(ProductionBalanceFields.REGISTRATION_PRICE_OVERHEAD) + ", 0) ");
    }

    private void appendProfit(Entity entity, StringBuilder query) {
        query.append("COALESCE(" + entity.getDecimalField(ProductionBalanceFields.PROFIT) + ", 0) ");
    }

    private void appendMaterialCostsForOrdersBalance(Entity entity, StringBuilder query) {
        if (SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT.getStringValue()
                .equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_MATERIAL_COSTS))) {
            String componentPriceClause = evaluateComponentPrice(
                    entity.getStringField(ProductionBalanceFields.CALCULATE_MATERIAL_COSTS_MODE));
            appendUsedQuantity(query);
            query.append(" * " + componentPriceClause);
        } else if (SourceOfMaterialCosts.FROM_ORDERS_MATERIAL_COSTS.getStringValue()
                .equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_MATERIAL_COSTS))) {
            // TODO KRNA add logic when KASI do sth with TKW
            query.append("0 ");
        }
    }

    private void appendCumulatedProfitValue(Entity entity, StringBuilder query) {
        appendCumulatedRealProductionCosts(entity, query);
        query.append(" / 100 * ");
        appendProfit(entity, query);
    }

    private void appendForEachProfitValue(Entity entity, StringBuilder query) {
        appendForEachRealProductionCosts(entity, query);
        query.append(" / 100 * ");
        appendProfit(entity, query);
    }

    private void appendCumulatedRealProductionCosts(Entity entity, StringBuilder query) {
        appendCumulatedRegistrationPrice(entity, query);
        query.append("+ ");
        appendCumulatedRegistrationPriceOverheadValue(entity, query);
    }

    private void appendForEachRealProductionCosts(Entity entity, StringBuilder query) {
        appendForEachRegistrationPrice(entity, query);
        query.append("+ ");
        appendForEachRegistrationPriceOverheadValue(entity, query);
    }

    private void appendCumulatedRegistrationPriceOverheadValue(Entity entity, StringBuilder query) {
        appendCumulatedRegistrationPrice(entity, query);
        query.append(" / 100 * ");
        appendRegistrationPriceOverhead(entity, query);
    }

    private void appendForEachRegistrationPriceOverheadValue(Entity entity, StringBuilder query) {
        appendForEachRegistrationPrice(entity, query);
        query.append(" / 100 * ");
        appendRegistrationPriceOverhead(entity, query);
    }

    private void appendForEachRegistrationPrice(Entity entity, StringBuilder query) {
        query.append("CASE WHEN ");
        appendProducedQuantity(query);
        query.append("<> 0 THEN ");
        appendForEachTotalCosts(entity, query);
        query.append("/ ");
        appendProducedQuantity(query);
        query.append("ELSE 0 END ");
    }

    private void appendCumulatedRegistrationPrice(Entity entity, StringBuilder query) {
        query.append("CASE WHEN ");
        appendProducedQuantity(query);
        query.append("<> 0 THEN ");
        appendCumulatedTotalCosts(entity, query);
        query.append("/ ");
        appendProducedQuantity(query);
        query.append("ELSE 0 END ");
    }

    private void appendForEachTotalCosts(Entity entity, StringBuilder query) {
        appendForEachTechnicalProductionCosts(entity, query);
        query.append("+ ");
        appendForEachMaterialCostMarginValue(entity, query);
        query.append("+ ");
        appendForEachProductionCostMarginValue(entity, query);
        query.append("+ ");
        appendAdditionalOverhead(entity, query);
        query.append("+ ");
        appendDirectAdditionalCost(query);
    }

    private void appendCumulatedTotalCosts(Entity entity, StringBuilder query) {
        appendCumulatedTechnicalProductionCosts(entity, query);
        query.append("+ ");
        appendCumulatedMaterialCostMarginValue(entity, query);
        query.append("+ ");
        appendCumulatedProductionCostMarginValue(entity, query);
        query.append("+ ");
        appendAdditionalOverhead(entity, query);
        query.append("+ ");
        appendDirectAdditionalCost(query);
    }

    private void appendCumulatedTechnicalProductionCosts(Entity entity, StringBuilder query) {
        appendMaterialCostsForOrdersBalance(entity, query);
        query.append("+ ");
        appendCumulatedRealStaffCosts(query);
        query.append("+ ");
        appendCumulatedRealMachineCosts(query);
    }

    private void appendCumulatedMaterialCostMarginValue(Entity entity, StringBuilder query) {
        appendMaterialCostMargin(entity, query);
        query.append("/ 100 * ");
        appendMaterialCostsForOrdersBalance(entity, query);
    }

    private void appendCumulatedProductionCostMarginValue(Entity entity, StringBuilder query) {
        appendProductionCostMargin(entity, query);
        query.append("/ 100 * ( ");
        appendCumulatedRealStaffCosts(query);
        query.append("+ ");
        appendCumulatedRealMachineCosts(query);
        query.append(") ");
    }

    private void appendForEachProductionCostMarginValue(Entity entity, StringBuilder query) {
        appendProductionCostMargin(entity, query);
        query.append("/ 100 * ( ");
        appendForEachRealStaffCosts(entity, query);
        query.append("+ ");
        appendForEachRealMachineCosts(entity, query);
        query.append(") ");
    }

    private void appendForEachMaterialCostMarginValue(Entity entity, StringBuilder query) {
        appendMaterialCostMargin(entity, query);
        query.append("/ 100 * ");
        appendMaterialCostsForOrdersBalance(entity, query);
    }

    private void appendForEachTechnicalProductionCosts(Entity entity, StringBuilder query) {
        appendMaterialCostsForOrdersBalance(entity, query);
        query.append("+ ");
        appendForEachRealStaffCosts(entity, query);
        query.append("+ ");
        appendForEachRealMachineCosts(entity, query);
    }

}
