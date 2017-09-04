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
import com.qcadoo.mes.productionCounting.xls.dto.OrderBalance;
import com.qcadoo.mes.productionCounting.xls.dto.PieceworkDetails;
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
        query.append("COALESCE(prodWaste.producedWastes, 0) AS producedWastes, ");
        appendProducedQuantity(query);
        query.append("- MIN(o.plannedQuantity) AS deviation, ");
        query.append("prod.unit AS productUnit ");
        query.append("FROM orders_order o ");
        query.append("JOIN basic_product prod ON o.product_id = prod.id ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON pt.order_id = o.id AND pt.state = '02accepted' ");
        query.append("LEFT JOIN productioncounting_trackingoperationproductoutcomponent topoc ON topoc.productiontracking_id = pt.id AND topoc.product_id = prod.id ");
        query.append("LEFT JOIN ");
        query.append("  (SELECT pcq.order_id as orderId, wastePt.order_id AS wastePtOrderId, COALESCE(SUM(wasteTopoc.usedquantity), 0) AS producedWastes ");
        query.append("  FROM basicproductioncounting_productioncountingquantity pcq ");
        query.append("  LEFT JOIN productioncounting_trackingoperationproductoutcomponent wasteTopoc ON  wasteTopoc.product_id = pcq.product_id ");
        query.append("  LEFT JOIN productioncounting_productiontracking wastePt ON wasteTopoc.productiontracking_id = wastePt.id AND wastePt.state = '02accepted' ");
        query.append("  WHERE pcq.typeofmaterial = '04waste' AND pcq.role = '02produced' ");
        query.append("  GROUP BY orderId, wastePtOrderId) prodWaste ON prodWaste.orderId = o.id AND prodWaste.wastePtOrderId = o.id ");
        appendWhereClause(query);
        query.append("GROUP BY orderNumber, productNumber, productName, productUnit, prodWaste.producedWastes");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(ProducedQuantity.class));
    }

    private void appendProducedQuantity(StringBuilder query) {
        query.append("COALESCE(SUM(topoc.usedquantity), 0) ");
    }

    private void appendWhereClause(StringBuilder query) {
        query.append("WHERE o.id IN (:ordersIds) ");
    }

    List<MaterialCost> getMaterialCosts(Entity entity, List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();
        appendCumulatedPlannedQuantities(query);
        appendMaterialCostsSelectionClause(query);
        appendQuantityAndCosts(entity, query);
        query.append("NULL AS operationNumber ");
        appendMaterialCostsFromClause(query);
        query.append("LEFT JOIN productioncounting_productiontracking pt ON pt.order_id = o.id AND pt.state = '02accepted' ");
        query.append("LEFT JOIN productioncounting_trackingoperationproductincomponent topic ON topic.productiontracking_id = pt.id AND topic.product_id = p.id ");
        query.append("GROUP BY o.id, o.number, p.number, p.name, p.unit, topic.wasteunit) ");
        query.append("UNION ");
        appendForEachPlannedQuantities(query);
        appendMaterialCostsSelectionClause(query);
        appendQuantityAndCosts(entity, query);
        query.append("op.number AS operationNumber ");
        appendMaterialCostsFromClause(query);
        query.append("JOIN technologies_operation op ON q.operation_id = op.id ");
        query.append("JOIN technologies_technologyoperationcomponent toc ON toc.operation_id = op.id AND o.technology_id = toc.technology_id ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON pt.order_id = o.id AND pt.technologyoperationcomponent_id = toc.id AND pt.state = '02accepted' ");
        query.append("LEFT JOIN productioncounting_trackingoperationproductincomponent topic ON topic.productiontracking_id = pt.id AND topic.product_id = p.id ");
        query.append("GROUP BY o.id, o.number, op.number, p.number, p.name, p.unit, topic.wasteunit) ");
        query.append("ORDER BY orderNumber, operationNumber, productNumber ");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(MaterialCost.class));
    }

    private void appendForEachPlannedQuantities(StringBuilder query) {
        query.append("(WITH planned_quantity (order_id, operation_id, product_id, quantity) AS (SELECT ");
        query.append("o.id AS orderId, ");
        query.append("toc.operation_id AS operationId, ");
        query.append("p.id AS productId, ");
        query.append("COALESCE(SUM(pcq.plannedquantity), 0) AS plannedQuantity ");
        query.append("FROM orders_order o ");
        query.append("JOIN basicproductioncounting_productioncountingquantity pcq ON pcq.order_id = o.id ");
        query.append("JOIN basic_product p ON pcq.product_id = p.id ");
        query.append("LEFT JOIN technologies_technology t ON t.product_id = p.id AND t.master = TRUE ");
        query.append("JOIN technologies_technologyoperationcomponent toc ON pcq.technologyoperationcomponent_id = toc.id ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '03forEach' ");
        query.append("AND pcq.role = '01used' AND pcq.typeofmaterial = '01component' AND t.id IS NULL ");
        query.append("GROUP BY o.id, toc.operation_id, p.id) ");
    }

    private void appendCumulatedPlannedQuantities(StringBuilder query) {
        query.append("(WITH planned_quantity (order_id, product_id, quantity) AS (SELECT ");
        query.append("o.id AS orderId, ");
        query.append("p.id AS productId, ");
        query.append("COALESCE(SUM(pcq.plannedquantity), 0) AS plannedQuantity ");
        query.append("FROM orders_order o ");
        query.append("JOIN basicproductioncounting_productioncountingquantity pcq ON pcq.order_id = o.id ");
        query.append("JOIN basic_product p ON pcq.product_id = p.id ");
        query.append("LEFT JOIN technologies_technology t ON t.product_id = p.id AND t.master = TRUE ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '02cumulated' ");
        query.append("AND pcq.role = '01used' AND pcq.typeofmaterial = '01component' AND t.id IS NULL ");
        query.append("GROUP BY o.id, p.id) ");
    }

    private void appendMaterialCostsSelectionClause(StringBuilder query) {
        query.append("SELECT ");
        query.append("o.id AS orderId, ");
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
        if (SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT.getStringValue().equals(
                entity.getStringField(ProductionBalanceFields.SOURCE_OF_MATERIAL_COSTS))) {
            String componentPriceClause = evaluateComponentPrice(entity
                    .getStringField(ProductionBalanceFields.CALCULATE_MATERIAL_COSTS_MODE));
            appendGlobalDefinitionsCosts(query, componentPriceClause);
        } else if (SourceOfMaterialCosts.FROM_ORDERS_MATERIAL_COSTS.getStringValue().equals(
                entity.getStringField(ProductionBalanceFields.SOURCE_OF_MATERIAL_COSTS))) {
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

    private void appendPlannedQuantity(StringBuilder query) {
        query.append("MIN(q.quantity) ");
    }

    private void appendUsedQuantity(StringBuilder query) {
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
        query.append("(WITH planned_time (order_id, staff_time, machine_time) AS (SELECT o.id AS orderId, ");
        appendPlannedStaffTime(entity, query);
        query.append("AS plannedStaffTime, ");
        appendPlannedMachineTime(entity, query);
        query.append("AS plannedMachineTime ");
        query.append("FROM orders_order o ");
        query.append("JOIN technologies_technology t ON o.technology_id = t.id ");
        query.append("JOIN technologies_technologyoperationcomponent toc ON toc.technology_id = t.id ");
        query.append("LEFT JOIN basicproductioncounting_productioncountingoperationrun pcor ON pcor.order_id = o.id AND pcor.technologyoperationcomponent_id = toc.id ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '02cumulated' ");
        query.append("GROUP BY o.id) ");
        query.append("SELECT ");
        query.append("o.id AS orderId, ");
        query.append("o.number AS orderNumber, ");
        query.append("NULL AS operationNumber, ");
        query.append("MIN(plt.staff_time) AS plannedStaffTime, ");
        appendRealStaffTime(query);
        query.append("AS realStaffTime, ");
        query.append("MIN(plt.machine_time) AS plannedMachineTime, ");
        appendRealMachineTime(query);
        query.append("AS realMachineTime, ");
        appendCumulatedPlannedStaffCosts(query);
        query.append("AS plannedStaffCosts, ");
        appendCumulatedRealStaffCosts(query);
        query.append("AS realStaffCosts, ");
        appendCumulatedRealStaffCosts(query);
        query.append("- ");
        appendCumulatedPlannedStaffCosts(query);
        query.append("AS staffCostsDeviation, ");
        appendCumulatedPlannedMachineCosts(query);
        query.append("AS plannedMachineCosts, ");
        appendCumulatedRealMachineCosts(query);
        query.append("AS realMachineCosts, ");
        appendCumulatedRealMachineCosts(query);
        query.append("- ");
        appendCumulatedPlannedMachineCosts(query);
        query.append("AS machineCostsDeviation, ");
        query.append("0 AS plannedPieceworkCosts, ");
        query.append("0 AS realPieceworkCosts, ");
        appendCumulatedPlannedStaffCosts(query);
        query.append("+ ");
        appendCumulatedPlannedMachineCosts(query);
        query.append("AS plannedCostsSum, ");
        appendCumulatedRealStaffCosts(query);
        query.append("+ ");
        appendCumulatedRealMachineCosts(query);
        query.append("AS realCostsSum, ");
        appendCumulatedRealStaffCosts(query);
        query.append("+ ");
        appendCumulatedRealMachineCosts(query);
        query.append("- ");
        appendCumulatedPlannedStaffCosts(query);
        query.append("- ");
        appendCumulatedPlannedMachineCosts(query);
        query.append("AS sumCostsDeviation ");
        query.append("FROM orders_order o ");
        query.append("JOIN planned_time plt ON plt.order_id = o.id ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON o.id = pt.order_id AND pt.state = '02accepted' ");
        query.append("CROSS JOIN basic_parameter bp ");
        query.append("GROUP BY orderId, orderNumber) ");
        query.append("UNION ");
        query.append("SELECT ");
        query.append("o.id AS orderId, ");
        query.append("o.number AS orderNumber, ");
        query.append("op.number AS operationNumber, ");
        appendPlannedStaffTime(entity, query);
        query.append("AS plannedStaffTime, ");
        appendRealStaffTime(query);
        query.append("AS realStaffTime, ");
        appendPlannedMachineTime(entity, query);
        query.append("AS plannedMachineTime, ");
        appendRealMachineTime(query);
        query.append("AS realMachineTime, ");
        appendForEachPlannedStaffCosts(entity, query);
        query.append("AS plannedStaffCosts, ");
        appendForEachRealStaffCosts(entity, query);
        query.append("AS realStaffCosts, ");
        appendForEachRealStaffCosts(entity, query);
        query.append("- ");
        appendForEachPlannedStaffCosts(entity, query);
        query.append("AS staffCostsDeviation, ");
        appendForEachPlannedMachineCosts(entity, query);
        query.append("AS plannedMachineCosts, ");
        appendForEachRealMachineCosts(entity, query);
        query.append("AS realMachineCosts, ");
        appendForEachRealMachineCosts(entity, query);
        query.append("- ");
        appendForEachPlannedMachineCosts(entity, query);
        query.append("AS machineCostsDeviation, ");
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
        query.append("LEFT JOIN productioncounting_productiontracking pt ON o.id = pt.order_id AND pt.state = '02accepted' ");
        query.append("LEFT JOIN technologies_technologyoperationcomponent toc ON pt.technologyoperationcomponent_id = toc.id ");
        query.append("LEFT JOIN basicproductioncounting_productioncountingoperationrun pcor ON pcor.order_id = o.id AND pcor.technologyoperationcomponent_id = toc.id ");
        query.append("LEFT JOIN technologies_operation op ON toc.operation_id = op.id ");
        query.append("CROSS JOIN basic_parameter bp ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '03forEach' ");
        query.append("GROUP BY orderId, orderNumber, operationNumber ");
        query.append("ORDER BY orderNumber, operationNumber ");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(ProductionCost.class));
    }

    private void appendRealMachineTime(StringBuilder query) {
        query.append("COALESCE(SUM(pt.machinetime), 0) ");
    }

    private void appendRealStaffTime(StringBuilder query) {
        query.append("COALESCE(SUM(pt.labortime), 0) ");
    }

    private void appendForEachRealMachineCosts(Entity entity, StringBuilder query) {
        appendRealMachineTime(query);
        query.append("/ 3600 * ");
        appendForEachMachineHourCost(entity, query);
    }

    private void appendForEachPlannedMachineCosts(Entity entity, StringBuilder query) {
        appendPlannedMachineTime(entity, query);
        query.append("/ 3600 * ");
        appendForEachMachineHourCost(entity, query);
    }

    private void appendForEachRealStaffCosts(Entity entity, StringBuilder query) {
        appendRealStaffTime(query);
        query.append("/ 3600 * ");
        appendForEachStaffHourCost(entity, query);
    }

    private void appendForEachPlannedStaffCosts(Entity entity, StringBuilder query) {
        appendPlannedStaffTime(entity, query);
        query.append("/ 3600 * ");
        appendForEachStaffHourCost(entity, query);
    }

    private void appendCumulatedRealMachineCosts(StringBuilder query) {
        appendRealMachineTime(query);
        query.append("/ 3600 * ");
        appendCumulatedMachineHourCost(query);
    }

    private void appendCumulatedPlannedMachineCosts(StringBuilder query) {
        query.append("MIN(plt.machine_time) / 3600 * ");
        appendCumulatedMachineHourCost(query);
    }

    private void appendCumulatedRealStaffCosts(StringBuilder query) {
        appendRealStaffTime(query);
        query.append("/ 3600 * ");
        appendCumulatedStaffHourCost(query);
    }

    private void appendCumulatedPlannedStaffCosts(StringBuilder query) {
        query.append("MIN(plt.staff_time) / 3600 * ");
        appendCumulatedStaffHourCost(query);
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
        query.append("COALESCE(MIN(bp.averagelaborhourlycostpb), 0) ");
    }

    private void appendCumulatedMachineHourCost(StringBuilder query) {
        query.append("COALESCE(MIN(bp.averagemachinehourlycostpb), 0) ");
    }

    private void appendForEachStaffHourCost(Entity entity, StringBuilder query) {
        if (SourceOfOperationCosts.TECHNOLOGY_OPERATION.getStringValue().equals(
                entity.getStringField(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS_PB))) {
            query.append("COALESCE(MIN(toc.laborhourlycost), 0) ");
        } else if (SourceOfOperationCosts.PARAMETERS.getStringValue().equals(
                entity.getStringField(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS_PB))) {
            query.append("COALESCE(MIN(bp.averagelaborhourlycostpb), 0) ");
        }
    }

    private void appendForEachMachineHourCost(Entity entity, StringBuilder query) {
        if (SourceOfOperationCosts.TECHNOLOGY_OPERATION.getStringValue().equals(
                entity.getStringField(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS_PB))) {
            query.append("COALESCE(MIN(toc.machinehourlycost), 0) ");
        } else if (SourceOfOperationCosts.PARAMETERS.getStringValue().equals(
                entity.getStringField(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS_PB))) {
            query.append("COALESCE(MIN(bp.averagemachinehourlycostpb), 0) ");
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
    
    List<OrderBalance> getOrdersBalance(Entity entity, List<Long> ordersIds, List<MaterialCost> materialCosts, List<ProductionCost> productionCosts) {
        StringBuilder query = new StringBuilder();
        appendWithQueries(materialCosts, productionCosts, query);
        appendOrdersBalanceSelectionClause(entity, query);
        query.append("MIN(gmc.cost) AS materialCosts, ");
        query.append("MIN(gpc.cost) AS productionCosts, ");
        query.append("MIN(gmc.cost) + MIN(gpc.cost) AS technicalProductionCosts, ");
        appendMaterialCostMarginValue(entity, query);
        query.append("AS materialCostMarginValue, ");
        appendProductionCostMarginValue(entity, query);
        query.append("AS productionCostMarginValue, ");
        appendTotalCosts(entity, query);
        query.append("AS totalCosts, ");
        appendRegistrationPrice(entity, query);
        query.append("AS registrationPrice, ");
        appendRegistrationPriceOverheadValue(entity, query);
        query.append("AS registrationPriceOverheadValue, ");
        appendRealProductionCosts(entity, query);
        query.append("AS realProductionCosts, ");
        appendProfitValue(entity, query);
        query.append("AS profitValue, ");
        appendRealProductionCosts(entity, query);
        query.append("+ ");
        appendProfitValue(entity, query);
        query.append("AS sellPrice ");
        query.append("FROM orders_order o ");
        query.append("JOIN basic_product prod ON o.product_id = prod.id ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON pt.order_id = o.id AND pt.state = '02accepted' ");
        query.append("LEFT JOIN productioncounting_trackingoperationproductoutcomponent topoc ON topoc.productiontracking_id = pt.id AND topoc.product_id = prod.id ");
        query.append("JOIN grouped_material_cost gmc ON gmc.order_id = o.id ");
        query.append("JOIN grouped_production_cost gpc ON gpc.order_id = o.id ");
        appendWhereClause(query);
        query.append("GROUP BY orderNumber, productNumber, productName ");
        query.append("ORDER BY orderNumber ");

        LOGGER.info("---------" + query.toString());

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(OrderBalance.class));
    }

    private void appendWithQueries(List<MaterialCost> materialCosts, List<ProductionCost> productionCosts, StringBuilder query) {
        query.append("WITH real_material_cost (order_id, cost) AS (VALUES ");
        for(int i = 0; i< materialCosts.size(); i++ ){
            MaterialCost materialCost = materialCosts.get(i);
            query.append("(" + materialCost.getOrderId() + ", " + materialCost.getRealCost() + ") ");
            if(i != materialCosts.size() - 1){
                query.append(", ");
            }
        }
        query.append("), ");
        query.append("grouped_material_cost AS (SELECT order_id, SUM(cost) AS cost FROM real_material_cost GROUP BY order_id), ");
        query.append("real_production_cost (order_id, cost) AS (VALUES ");
        for(int i = 0; i< productionCosts.size(); i++ ){
            ProductionCost productionCost = productionCosts.get(i);
            query.append("(" + productionCost.getOrderId() + ", " + productionCost.getRealCostsSum() + ") ");
            if(i != productionCosts.size() - 1){
                query.append(", ");
            }
        }
        query.append("), ");
        query.append("grouped_production_cost AS (SELECT order_id, SUM(cost) AS cost FROM real_production_cost GROUP BY order_id) ");
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

    private void appendProfitValue(Entity entity, StringBuilder query) {
        appendRealProductionCosts(entity, query);
        query.append(" / 100 * ");
        appendProfit(entity, query);
    }

    private void appendRealProductionCosts(Entity entity, StringBuilder query) {
        appendRegistrationPrice(entity, query);
        query.append("+ ");
        appendRegistrationPriceOverheadValue(entity, query);
    }

    private void appendRegistrationPriceOverheadValue(Entity entity, StringBuilder query) {
        appendRegistrationPrice(entity, query);
        query.append(" / 100 * ");
        appendRegistrationPriceOverhead(entity, query);
    }

    private void appendRegistrationPrice(Entity entity, StringBuilder query) {
        query.append("CASE WHEN ");
        appendProducedQuantity(query);
        query.append("<> 0 THEN ");
        appendTotalCosts(entity, query);
        query.append("/ ");
        appendProducedQuantity(query);
        query.append("ELSE 0 END ");
    }

    private void appendTotalCosts(Entity entity, StringBuilder query) {
        query.append("MIN(gmc.cost) + MIN(gpc.cost) + ");
        appendMaterialCostMarginValue(entity, query);
        query.append("+ ");
        appendProductionCostMarginValue(entity, query);
        query.append("+ ");
        appendAdditionalOverhead(entity, query);
        query.append("+ ");
        appendDirectAdditionalCost(query);
    }

    private void appendProductionCostMarginValue(Entity entity, StringBuilder query) {
        appendProductionCostMargin(entity, query);
        query.append("/ 100 * MIN(gpc.cost) ");
    }

    private void appendMaterialCostMarginValue(Entity entity, StringBuilder query) {
        appendMaterialCostMargin(entity, query);
        query.append("/ 100 * MIN(gmc.cost) ");
    }

}
