package com.qcadoo.mes.productionCounting.xls;

import com.qcadoo.mes.costCalculation.constants.MaterialCostsUsed;
import com.qcadoo.mes.costCalculation.constants.SourceOfOperationCosts;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.xls.dto.*;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class ProductionBalanceRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    List<ProducedQuantity> getProducedQuantities(final List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("o.additionalFinalProducts AS additionalFinalProducts, ");
        query.append("prod.number AS productNumber, ");
        query.append("prod.name AS productName, ");
        query.append("MIN(o.plannedquantity) AS plannedQuantity, ");
        appendProducedQuantity(query);
        query.append("AS producedQuantity, ");
        query.append("COALESCE(SUM(topoc.wastesquantity), 0) AS wastesQuantity, ");
        query.append("COALESCE(prodWaste.producedWastes, 0) AS producedWastes, ");
        appendProducedQuantity(query);
        query.append("- MIN(o.plannedQuantity) AS deviation, ");
        query.append("prod.unit AS productUnit, ");
        query.append("(SELECT COALESCE(SUM(stopoc.usedquantity), 0) FROM productioncounting_productiontracking spt " +
                "LEFT JOIN productioncounting_trackingoperationproductoutcomponent stopoc ON stopoc.productiontracking_id = spt.id " +
                "WHERE  spt.order_id = o.id AND spt.state = '02accepted' and  stopoc.typeofmaterial = '05additionalFinalProduct') as additionalFinalProductsQuantity ");
        query.append("FROM orders_order o ");
        query.append("JOIN basic_product prod ON o.product_id = prod.id ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON pt.order_id = o.id AND pt.state = '02accepted' ");
        query.append(
                "LEFT JOIN productioncounting_trackingoperationproductoutcomponent topoc ON topoc.productiontracking_id = pt.id AND topoc.product_id = prod.id ");
        query.append("LEFT JOIN ");
        query.append(
                "(SELECT pcq.order_id as orderId, wastePt.order_id AS wastePtOrderId, COALESCE(SUM(wasteTopoc.usedquantity), 0) AS producedWastes ");
        query.append("FROM basicproductioncounting_productioncountingquantity pcq ");
        query.append(
                "LEFT JOIN productioncounting_trackingoperationproductoutcomponent wasteTopoc ON  wasteTopoc.product_id = pcq.product_id ");
        query.append(
                "LEFT JOIN productioncounting_productiontracking wastePt ON wasteTopoc.productiontracking_id = wastePt.id AND wastePt.state = '02accepted' ");
        query.append("WHERE pcq.typeofmaterial = '04waste' AND pcq.role = '02produced' ");
        query.append(
                "GROUP BY orderId, wastePtOrderId) prodWaste ON prodWaste.orderId = o.id AND prodWaste.wastePtOrderId = o.id ");
        appendWhereClause(query);
        query.append("GROUP BY o.id, orderNumber, productNumber, productName, productUnit, prodWaste.producedWastes ");
        query.append("ORDER BY orderNumber ");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(ProducedQuantity.class));
    }


    public List<OrderProduct> getOrderProducts(final List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT \n" +
                "o.number AS orderNumber,\n" +
                "p.number AS productNumber,\n" +
                "p.name AS productName,\n" +
                "pcq.typeofmaterial AS productType, \n" +
                "pcq.plannedquantity AS plannedQuantity, \n" +
                "COALESCE(pcq.producedquantity, 0) AS producedQuantity,\n" +
                "COALESCE(pcq.producedquantity, 0) - pcq.plannedquantity as deviation,\n" +
                "p.unit productUnit\n" +
                "FROM basicproductioncounting_productioncountingquantity pcq\n" +
                "LEFT JOIN basic_product p ON pcq.product_id = p.id\n" +
                "LEFT JOIN orders_order o ON pcq.order_id = o.id\n" +
                "WHERE o.id IN (:ordersIds)  AND pcq.typeofmaterial::text = ANY (ARRAY['05additionalFinalProduct'::character varying::text, '03finalProduct'::character varying::text, '04waste'::character varying::text]) AND pcq.role::text = '02produced'::text \n ");
        query.append("ORDER BY orderNumber ");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(OrderProduct.class));
    }

    private void appendProducedQuantity(final StringBuilder query) {
        query.append("COALESCE(SUM(topoc.usedquantity), 0) ");
    }

    private void appendWhereClause(final StringBuilder query) {
        query.append("WHERE o.id IN (:ordersIds) ");
    }

    List<MaterialCost> getMaterialCosts(final Entity entity, final List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();

        appendCumulatedPlannedQuantities(query);
        appendMaterialCostsSelectionClause(query, entity);
        query.append("q.replacementTo AS replacementTo, ");
        query.append("NULL AS operationNumber ");
        appendMaterialCostsFromClause(query, entity);
        query.append("LEFT JOIN productioncounting_productiontracking pt ON pt.order_id = o.id AND pt.state = '02accepted' ");
        query.append(
                "LEFT JOIN productioncounting_trackingoperationproductincomponent topic ON topic.productiontracking_id = pt.id AND topic.product_id = p.id ");
        query.append("GROUP BY o.id, o.number, p.number, p.name, p.unit, topic.wasteunit, q.replacementTo) ");

        query.append("UNION ");
        appendForEachPlannedQuantities(query);
        appendMaterialCostsSelectionClause(query, entity);
        query.append("NULL AS replacementTo, ");
        query.append("q.operations AS operationNumber ");
        appendMaterialCostsFromClause(query, entity);
        query.append("LEFT JOIN productioncounting_productiontracking pt ON pt.order_id = o.id AND pt.state = '02accepted' ");
        query.append(
                "LEFT JOIN productioncounting_trackingoperationproductincomponent topic ON topic.productiontracking_id = pt.id AND topic.product_id = p.id ");
        query.append("GROUP BY o.id, o.number, p.number, p.name, p.unit, topic.wasteunit, q.replacementTo, q.operations) ");
        query.append("ORDER BY orderNumber, operationNumber, productNumber ");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(MaterialCost.class));
    }

    private void appendForEachPlannedQuantities(final StringBuilder query) {
        query.append(
                "(WITH planned_quantity (order_id, operations, product_id, plannedQuantity, usedQuantity, childsQuantity, replacementTo) AS (SELECT ");
        query.append("o.id AS orderId, ");
        query.append("STRING_AGG (op.number, ', ') operations, ");
        query.append("p.id AS productId, ");
        query.append("COALESCE(SUM(pcq.plannedquantity), 0) AS plannedQuantity, ");
        query.append("COALESCE(SUM(pcq.usedquantity), 0) AS usedQuantity, ");
        query.append("0 AS childsQuantity, ");
        query.append("replacementto.number AS replacementTo ");
        query.append("FROM orders_order o ");
        query.append("JOIN basicproductioncounting_productioncountingquantity pcq ON pcq.order_id = o.id ");
        query.append("LEFT JOIN basic_product replacementto ON replacementto.id = pcq.replacementto_id ");
        query.append("JOIN basic_product p ON pcq.product_id = p.id ");
        query.append("LEFT JOIN technologies_technology t ON t.product_id = p.id AND t.master = TRUE ");
        query.append("JOIN technologies_technologyoperationcomponent toc ON pcq.technologyoperationcomponent_id = toc.id ");
        query.append("JOIN technologies_operation op ON op.id = toc.operation_id ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '03forEach' ");
        query.append("AND pcq.role = '01used' AND pcq.typeofmaterial = '01component' AND (t.id IS NULL ");
        query.append("OR t.id IS NOT NULL) ");
        query.append("GROUP BY o.id, p.id, replacementto.number) ");
    }

    private void appendCumulatedPlannedQuantities(final StringBuilder query) {
        query.append(
                "(WITH planned_quantity (order_id, product_id, plannedQuantity, usedQuantity, childsQuantity, replacementTo) AS (SELECT ");
        query.append("o.id AS orderId, ");
        query.append("p.id AS productId, ");
        query.append("COALESCE(SUM(pcq.plannedquantity), 0) AS plannedQuantity, ");
        query.append("COALESCE(SUM(pcq.usedquantity), 0) AS usedQuantity, ");
        query.append("0 AS childsQuantity, ");
        query.append("replacementto.number AS replacementTo ");
        query.append("FROM orders_order o ");
        query.append("JOIN basicproductioncounting_productioncountingquantity pcq ON pcq.order_id = o.id ");
        query.append("LEFT JOIN basic_product replacementto ON replacementto.id = pcq.replacementto_id ");
        query.append("JOIN basic_product p ON pcq.product_id = p.id ");
        query.append("LEFT JOIN technologies_technology t ON t.product_id = p.id AND t.master = TRUE ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '02cumulated' ");
        query.append("AND pcq.role = '01used' AND pcq.typeofmaterial = '01component' AND t.id IS NULL ");
        query.append("GROUP BY o.id, replacementto.number, p.id ");
        query.append("UNION ");
        query.append("SELECT ");
        query.append("o.id AS orderId, ");
        query.append("p.id AS productId, ");
        query.append("COALESCE(SUM(pcq.plannedquantity), 0) AS plannedQuantity, ");
        query.append("COALESCE(SUM(pcq.usedquantity), 0) AS usedQuantity, ");
        query.append("COALESCE(SUM(och.plannedquantity), 0) AS childsQuantity, ");
        query.append("replacementto.number AS replacementTo ");
        query.append("FROM orders_order o ");
        query.append("JOIN basicproductioncounting_productioncountingquantity pcq ON pcq.order_id = o.id ");
        query.append("LEFT JOIN basic_product replacementto ON replacementto.id = pcq.replacementto_id ");
        query.append("JOIN basic_product p ON pcq.product_id = p.id ");
        query.append("LEFT JOIN technologies_technology t ON t.product_id = p.id AND t.master = TRUE ");
        query.append("LEFT JOIN orders_order och ON och.product_id = p.id AND och.parent_id = o.id ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '02cumulated' ");
        query.append("AND pcq.role = '01used' AND pcq.typeofmaterial = '01component' AND t.id IS NOT NULL ");
        query.append(
                "GROUP BY o.id, replacementto.number, p.id HAVING COALESCE(SUM(pcq.plannedquantity), 0) - COALESCE(SUM(och.plannedquantity), 0) > 0) ");
    }

    private void appendMaterialCostsSelectionClause(final StringBuilder query, final Entity entity) {
        query.append("SELECT ");
        query.append("o.id AS orderId, ");
        query.append("o.number AS orderNumber, ");
        query.append("p.number AS productNumber, ");
        query.append("p.name AS productName, ");
        query.append("p.unit AS productUnit, ");
        query.append("topic.wasteunit AS usedWasteUnit, ");
        appendPlannedQuantity(query);
        query.append("AS plannedQuantity, ");
        appendUsedQuantity(query);
        query.append("AS usedQuantity, ");
        appendUsedQuantity(query);
        query.append("- ");
        appendPlannedQuantity(query);
        query.append("AS quantitativeDeviation, ");
        appendPlannedCost(query, entity);
        query.append("AS plannedCost, ");
        appendRealCost(query, entity);
        query.append("AS realCost, ");
        appendRealCost(query, entity);
        query.append("- ");
        appendPlannedCost(query, entity);
        query.append("AS valueDeviation, ");
        appendCostCurrency(query, entity);
        query.append("AS costCurrencyId, ");
        query.append("COALESCE(SUM(topic.wasteusedquantity), 0) AS usedWasteQuantity, ");
    }

    private void appendMaterialCostsFromClause(final StringBuilder query, final Entity entity) {
        query.append("FROM orders_order o ");
        query.append("JOIN planned_quantity q ON q.order_id = o.id ");
        query.append("JOIN basic_product p ON q.product_id = p.id ");

        if (MaterialCostsUsed.COST_FOR_ORDER.getStringValue()
                .equals(entity.getStringField(ProductionBalanceFields.MATERIAL_COSTS_USED))) {
            query.append("LEFT JOIN costnormsformaterials_technologyinstoperproductincomp tiopic ");
            query.append("ON tiopic.product_id = p.id AND tiopic.order_id = o.id ");
        }
    }

    private void appendPlannedCost(final StringBuilder query, final Entity entity) {
        if (MaterialCostsUsed.COST_FOR_ORDER.getStringValue()
                .equals(entity.getStringField(ProductionBalanceFields.MATERIAL_COSTS_USED))) {
            query.append("CASE WHEN ");
            appendUsedQuantity(query);
            query.append("<> 0 THEN ");
            appendPlannedQuantity(query);
            query.append("* COALESCE(MIN(tiopic.costfororder), 0) / ");
            appendUsedQuantity(query);
            query.append("ELSE 0 END ");
        } else {
            String componentPriceClause = evaluateComponentPrice(
                    entity.getStringField(ProductionBalanceFields.MATERIAL_COSTS_USED));
            appendPlannedQuantity(query);
            query.append("* ").append(componentPriceClause);
        }
    }

    private void appendRealCost(final StringBuilder query, final Entity entity) {
        if (MaterialCostsUsed.COST_FOR_ORDER.getStringValue()
                .equals(entity.getStringField(ProductionBalanceFields.MATERIAL_COSTS_USED))) {
            query.append("COALESCE(MIN(tiopic.costfororder), 0) ");
        } else {
            String componentPriceClause = evaluateComponentPrice(
                    entity.getStringField(ProductionBalanceFields.MATERIAL_COSTS_USED));
            appendUsedQuantity(query);
            query.append("* ").append(componentPriceClause);
        }
    }

    private String evaluateComponentPrice(final String materialCostsUsed) {
        switch (MaterialCostsUsed.parseString(materialCostsUsed)) {
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
                throw new IllegalStateException("Unsupported materialCostsUsed: " + materialCostsUsed);
        }
    }

    private void appendCostCurrency(final StringBuilder query, final Entity entity) {
        switch (MaterialCostsUsed.parseString(entity.getStringField(ProductionBalanceFields.MATERIAL_COSTS_USED))) {
            case NOMINAL:
                query.append("MIN(p.nominalcostcurrency_id) ");
                break;

            case LAST_PURCHASE:
                query.append("MIN(p.lastpurchasecostcurrency_id) ");
                break;

            default:
                query.append("NULL ");
        }
    }

    private void appendPlannedQuantity(final StringBuilder query) {
        query.append("MIN(q.plannedQuantity - q.childsQuantity) ");
    }

    private void appendUsedQuantity(final StringBuilder query) {
        query.append("MIN(q.usedQuantity - q.childsQuantity) ");
    }

    List<PieceworkDetails> getPieceworkDetails(final List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("NULL AS operationNumber, ");
        query.append("stf.name || ' ' || stf.surname AS worker, ");
        appendProducedQuantity(query);
        query.append("AS producedQuantity, ");
        query.append("pr.name AS pieceRate, ");
        appendActualPieceRate(query);
        query.append("AS rate, ");
        appendProducedQuantity(query);
        query.append("* ");
        appendActualPieceRate(query);
        query.append("AS cost ");
        query.append("FROM productioncounting_productiontracking pt ");
        query.append("JOIN orders_order o ON pt.order_id = o.id ");
        query.append("JOIN technologies_technology t ON o.technology_id = t.id ");
        query.append("JOIN basic_piecerate pr ON pr.id = t.piecerate_id ");
        query.append("LEFT JOIN basic_staff stf ON pt.staff_id = stf.id ");
        query.append("LEFT JOIN productioncounting_trackingoperationproductoutcomponent topoc ON topoc.productiontracking_id = pt.id AND topoc.typeofmaterial::text = '03finalProduct'::text ");
        appendWhereClause(query);
        query.append("AND pt.state = '02accepted' AND o.typeofproductionrecording = '02cumulated' ");
        query.append("AND t.pieceworkproduction = TRUE ");
        query.append("GROUP BY orderNumber, o.startdate, operationNumber, worker, pr.id, pieceRate ");
        query.append("UNION ");
        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("op.number AS operationNumber, ");
        query.append("stf.name || ' ' || stf.surname AS worker, ");
        appendProducedQuantity(query);
        query.append("AS producedQuantity, ");
        query.append("pr.name AS pieceRate, ");
        appendActualPieceRate(query);
        query.append("AS rate, ");
        appendProducedQuantity(query);
        query.append("* ");
        appendActualPieceRate(query);
        query.append("AS cost ");
        query.append("FROM productioncounting_productiontracking pt ");
        query.append("JOIN orders_order o ON pt.order_id = o.id ");
        query.append("JOIN technologies_technologyoperationcomponent toc ON pt.technologyoperationcomponent_id = toc.id ");
        query.append("JOIN technologies_operation op ON toc.operation_id = op.id ");
        query.append("JOIN basic_piecerate pr ON pr.id = toc.piecerate_id ");
        query.append("LEFT JOIN basic_staff stf ON pt.staff_id = stf.id ");
        query.append("LEFT JOIN productioncounting_trackingoperationproductoutcomponent topoc ON topoc.productiontracking_id = pt.id AND (topoc.typeofmaterial::text = '02intermediate'::text OR topoc.typeofmaterial::text = '03finalProduct'::text) ");
        appendWhereClause(query);
        query.append("AND pt.state = '02accepted' AND o.typeofproductionrecording = '03forEach' ");
        query.append("AND toc.pieceworkproduction = TRUE ");
        query.append("GROUP BY orderNumber, o.startdate, operationNumber, worker, pr.id, pieceRate ");
        query.append("ORDER BY orderNumber, operationNumber, worker ");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(PieceworkDetails.class));
    }

    private void appendActualPieceRate(final StringBuilder query) {
        query.append("COALESCE((SELECT pri.actualrate FROM basic_piecerateitem pri WHERE pri.piecerate_id = pr.id ");
        query.append("AND pri.datefrom <= o.startdate ORDER BY pri.datefrom DESC LIMIT 1), 0) ");
    }

    List<LaborTime> getLaborTime(final List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("op.number AS operationNumber, ");
        query.append("stf.number AS staffNumber, ");
        query.append("stf.name AS staffName, ");
        query.append("stf.surname AS staffSurname, ");
        query.append("COALESCE(stf.laborhourlycost, 0) AS staffLaborHourlyCost, ");
        query.append("wg.name AS wageGroupName, ");
        query.append("COALESCE(SUM(swt.labortime), 0) AS laborTime ");
        query.append("FROM orders_order o ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON o.id = pt.order_id AND pt.state = '02accepted' ");
        query.append("LEFT JOIN productioncounting_staffworktime swt ON pt.id = swt.productionrecord_id ");
        query.append("LEFT JOIN basic_staff stf ON swt.worker_id = stf.id ");
        query.append("LEFT JOIN technologies_technologyoperationcomponent toc ON pt.technologyoperationcomponent_id = toc.id ");
        query.append("LEFT JOIN technologies_operation op ON toc.operation_id = op.id ");
        query.append("LEFT JOIN wagegroups_wagegroup wg ON stf.wagegroup_id = wg.id ");
        appendWhereClause(query);
        query.append(
                "GROUP BY orderNumber, operationNumber, staffNumber, staffName, staffSurname, staffLaborHourlyCost, wageGroupName ");
        query.append("ORDER BY orderNumber, operationNumber, staffNumber ");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(LaborTime.class));
    }

    List<LaborTimeDetails> getLaborTimeDetails(final Entity entity, final List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();

        query.append("(WITH planned_time (order_id, staff_time, machine_time) AS (SELECT o.id AS orderId, ");
        appendPlannedStaffTime(entity, query);
        query.append("AS plannedStaffTime, ");
        appendPlannedMachineTime(entity, query);
        query.append("AS plannedMachineTime ");
        query.append("FROM orders_order o ");
        query.append("JOIN technologies_technology t ON o.technology_id = t.id ");
        query.append("JOIN technologies_technologyoperationcomponent toc ON toc.technology_id = t.id ");
        query.append(
                "LEFT JOIN basicproductioncounting_productioncountingoperationrun pcor ON pcor.order_id = o.id AND pcor.technologyoperationcomponent_id = toc.id ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '02cumulated' ");
        query.append("GROUP BY o.id) ");
        query.append("SELECT ");
        query.append("d.number AS divisionNumber, ");
        query.append("pl.number AS productionLineNumber, ");
        query.append("o.number AS orderNumber, ");
        query.append("o.state AS orderState, ");
        query.append("o.datefrom AS plannedDateFrom, ");
        query.append("o.effectivedatefrom AS effectiveDateFrom, ");
        query.append("o.dateTo AS plannedDateTo, ");
        query.append("o.effectivedateto AS effectiveDateTo, ");
        query.append("p.number AS productNumber, ");
        query.append("o.name AS orderName, ");
        query.append("o.plannedquantity AS plannedQuantity, ");
        query.append("COALESCE(o.amountofproductproduced, 0::numeric) AS amountOfProductProduced, ");
        query.append("stf.number AS staffNumber, ");
        query.append("stf.name AS staffName, ");
        query.append("stf.surname AS staffSurname, ");
        query.append("NULL AS operationNumber, ");
        query.append("pt.timerangefrom AS timeRangeFrom, ");
        query.append("pt.timerangeto AS timeRangeTo, ");
        query.append("sh.name AS shiftName, ");
        query.append("pt.createdate AS createDate, ");
        query.append("COALESCE(swt.labortime, 0) AS laborTime, ");
        query.append("plt.staff_time AS plannedLaborTime, ");
        query.append("COALESCE(swt.labortime, 0) - plt.staff_time AS laborTimeDeviation, ");
        query.append("COALESCE(pt.machinetime, 0) AS machineTime, ");
        query.append("plt.machine_time AS plannedMachineTime, ");
        query.append("COALESCE(pt.machinetime, 0) - plt.machine_time AS machineTimeDeviation ");
        query.append("FROM orders_order o ");
        query.append("JOIN planned_time plt ON plt.order_id = o.id ");
        query.append("JOIN basic_product p ON p.id = o.product_id ");
        query.append("JOIN technologies_technology t ON t.id = o.technology_id ");
        query.append("LEFT JOIN basic_division d ON d.id = t.division_id ");
        query.append("LEFT JOIN productionlines_productionline pl ON pl.id = o.productionline_id ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON o.id = pt.order_id AND pt.state = '02accepted' ");
        query.append("LEFT JOIN productioncounting_staffworktime swt ON pt.id = swt.productionrecord_id ");
        query.append("LEFT JOIN basic_staff stf ON swt.worker_id = stf.id ");
        query.append("LEFT JOIN basic_shift sh ON sh.id = pt.shift_id) ");
        query.append("UNION ");
        query.append(
                "(WITH planned_time (order_id, toc_id, staff_time, machine_time) AS (SELECT o.id AS orderId, toc.id AS tocId, ");
        appendPlannedStaffTime(entity, query);
        query.append("AS plannedStaffTime, ");
        appendPlannedMachineTime(entity, query);
        query.append("AS plannedMachineTime ");
        query.append("FROM orders_order o ");
        query.append("JOIN technologies_technology t ON o.technology_id = t.id ");
        query.append("JOIN technologies_technologyoperationcomponent toc ON toc.technology_id = t.id ");
        query.append(
                "LEFT JOIN basicproductioncounting_productioncountingoperationrun pcor ON pcor.order_id = o.id AND pcor.technologyoperationcomponent_id = toc.id ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '03forEach' ");
        query.append("GROUP BY o.id, toc.id) ");
        query.append("SELECT ");
        query.append("d.number AS divisionNumber, ");
        query.append("pl.number AS productionLineNumber, ");
        query.append("o.number AS orderNumber, ");
        query.append("o.state AS orderState, ");
        query.append("o.datefrom AS plannedDateFrom, ");
        query.append("o.effectivedatefrom AS effectiveDateFrom, ");
        query.append("o.dateTo AS plannedDateTo, ");
        query.append("o.effectivedateto AS effectiveDateTo, ");
        query.append("p.number AS productNumber, ");
        query.append("o.name AS orderName, ");
        query.append("o.plannedquantity AS plannedQuantity, ");
        query.append("COALESCE(o.amountofproductproduced, 0::numeric) AS amountOfProductProduced, ");
        query.append("stf.number AS staffNumber, ");
        query.append("stf.name AS staffName, ");
        query.append("stf.surname AS staffSurname, ");
        query.append("op.number AS operationNumber, ");
        query.append("pt.timerangefrom AS timeRangeFrom, ");
        query.append("pt.timerangeto AS timeRangeTo, ");
        query.append("sh.name AS shiftName, ");
        query.append("pt.createdate AS createDate, ");
        query.append("COALESCE(swt.labortime, 0) AS laborTime, ");
        query.append("plt.staff_time AS plannedLaborTime, ");
        query.append("COALESCE(swt.labortime, 0) - plt.staff_time AS laborTimeDeviation, ");
        query.append("COALESCE(pt.machinetime, 0) AS machineTime, ");
        query.append("plt.machine_time AS plannedMachineTime, ");
        query.append("COALESCE(pt.machinetime, 0) - plt.machine_time AS machineTimeDeviation ");
        query.append("FROM orders_order o ");
        query.append("JOIN basic_product p ON p.id = o.product_id ");
        query.append("JOIN technologies_technology t ON t.id = o.technology_id ");
        query.append("LEFT JOIN basic_division d ON d.id = t.division_id ");
        query.append("LEFT JOIN productionlines_productionline pl ON pl.id = o.productionline_id ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON o.id = pt.order_id AND pt.state = '02accepted' ");
        query.append("LEFT JOIN planned_time plt ON plt.order_id = o.id AND plt.toc_id = pt.technologyoperationcomponent_id ");
        query.append("LEFT JOIN productioncounting_staffworktime swt ON pt.id = swt.productionrecord_id ");
        query.append("LEFT JOIN basic_staff stf ON swt.worker_id = stf.id ");
        query.append("LEFT JOIN basic_shift sh ON sh.id = pt.shift_id ");
        query.append("LEFT JOIN technologies_technologyoperationcomponent toc ON pt.technologyoperationcomponent_id = toc.id ");
        query.append("LEFT JOIN technologies_operation op ON toc.operation_id = op.id ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '03forEach') ");
        query.append("ORDER BY orderNumber, operationNumber, staffNumber ");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(LaborTimeDetails.class));
    }

    List<ProductionCost> getProductionCosts(final Entity entity, final List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();

        query.append("(WITH planned_time (order_id, staff_time, machine_time) AS (SELECT o.id AS orderId, ");
        appendPlannedStaffTime(entity, query);
        query.append("AS plannedStaffTime, ");
        appendPlannedMachineTime(entity, query);
        query.append("AS plannedMachineTime ");
        query.append("FROM orders_order o ");
        query.append("JOIN technologies_technology t ON o.technology_id = t.id ");
        query.append("JOIN technologies_technologyoperationcomponent toc ON toc.technology_id = t.id ");
        query.append(
                "LEFT JOIN basicproductioncounting_productioncountingoperationrun pcor ON pcor.order_id = o.id AND pcor.technologyoperationcomponent_id = toc.id ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '02cumulated' ");
        query.append("GROUP BY o.id), ");
        query.append("quantities (order_id, plannedQuantity, producedQuantity) AS (SELECT ");
        query.append("o.id AS orderId, ");
        query.append("COALESCE(SUM(pcq.plannedquantity), 0) AS plannedQuantity, ");
        query.append("COALESCE(SUM(pcq.producedquantity), 0) AS producedQuantity ");
        query.append("FROM orders_order o ");
        query.append("JOIN basicproductioncounting_productioncountingquantity pcq ON pcq.order_id = o.id ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '02cumulated' ");
        query.append("AND pcq.role = '02produced' AND pcq.typeofmaterial = '03finalProduct' ");
        query.append("GROUP BY o.id) ");
        appendRealStaffCosts(entity, query, "'02cumulated'");
        query.append("SELECT ");
        query.append("o.id AS orderId, ");
        query.append("o.number AS orderNumber, ");
        query.append("NULL AS operationNumber, ");
        query.append("MIN(plt.staff_time) AS plannedStaffTime, ");
        appendRealStaffTime(entity, query);
        query.append("AS realStaffTime, ");
        query.append("MIN(plt.machine_time) AS plannedMachineTime, ");
        appendRealMachineTime(query);
        query.append("AS realMachineTime, ");
        appendCumulatedPlannedStaffCosts(query);
        query.append("AS plannedStaffCosts, ");
        appendCumulatedRealStaffCosts(entity, query);
        query.append("AS realStaffCosts, ");
        appendCumulatedRealStaffCosts(entity, query);
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
        query.append("t.pieceworkproduction, ");
        query.append("MIN(q.plannedQuantity) * ");
        appendActualPieceRate(query);
        query.append("AS plannedPieceworkCosts, ");
        query.append("MIN(q.producedQuantity) * ");
        appendActualPieceRate(query);
        query.append("AS realPieceworkCosts, ");
        appendCumulatedPlannedStaffCosts(query);
        query.append("+ ");
        appendCumulatedPlannedMachineCosts(query);
        query.append("AS plannedCostsSum, ");
        appendCumulatedRealStaffCosts(entity, query);
        query.append("+ ");
        appendCumulatedRealMachineCosts(query);
        query.append("AS realCostsSum, ");
        appendCumulatedRealStaffCosts(entity, query);
        query.append("+ ");
        appendCumulatedRealMachineCosts(query);
        query.append("- ");
        appendCumulatedPlannedStaffCosts(query);
        query.append("- ");
        appendCumulatedPlannedMachineCosts(query);
        query.append("AS sumCostsDeviation ");
        query.append("FROM orders_order o ");
        query.append("JOIN planned_time plt ON plt.order_id = o.id ");
        query.append("JOIN quantities q ON q.order_id = o.id ");
        query.append("JOIN technologies_technology t ON o.technology_id = t.id ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON o.id = pt.order_id AND pt.state = '02accepted' ");
        query.append("LEFT JOIN basic_piecerate pr ON pr.id = t.piecerate_id ");
        appendRealStaffCostsJoin(entity, query);
        query.append("CROSS JOIN basic_parameter bp ");
        query.append("GROUP BY orderId, orderNumber, t.pieceworkproduction, pr.id) ");
        query.append("UNION ALL ");
        query.append(
                "(WITH planned_time (order_id, toc_id, staff_time, machine_time) AS (SELECT o.id AS orderId, toc.id AS tocId, ");
        appendPlannedStaffTime(entity, query);
        query.append("AS plannedStaffTime, ");
        appendPlannedMachineTime(entity, query);
        query.append("AS plannedMachineTime ");
        query.append("FROM orders_order o ");
        query.append("JOIN technologies_technology t ON o.technology_id = t.id ");
        query.append("JOIN technologies_technologyoperationcomponent toc ON toc.technology_id = t.id ");
        query.append(
                "LEFT JOIN basicproductioncounting_productioncountingoperationrun pcor ON pcor.order_id = o.id AND pcor.technologyoperationcomponent_id = toc.id ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '03forEach' ");
        query.append("GROUP BY o.id, toc.id), ");
        query.append("quantities (order_id, toc_id, plannedQuantity, producedQuantity) AS (SELECT ");
        query.append("o.id AS orderId, ");
        query.append("toc.id AS tocId, ");
        query.append("COALESCE(SUM(pcq.plannedquantity), 0) AS plannedQuantity, ");
        query.append("COALESCE(SUM(pcq.producedquantity), 0) AS producedQuantity ");
        query.append("FROM orders_order o ");
        query.append("JOIN basicproductioncounting_productioncountingquantity pcq ON pcq.order_id = o.id ");
        query.append("JOIN technologies_technologyoperationcomponent toc ON pcq.technologyoperationcomponent_id = toc.id ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '03forEach' ");
        query.append("AND pcq.role = '02produced' AND (pcq.typeofmaterial = '03finalProduct' OR pcq.typeofmaterial = '02intermediate') ");
        query.append("GROUP BY o.id, toc.id) ");
        appendRealStaffCosts(entity, query, "'03forEach'");
        query.append("SELECT ");
        query.append("o.id AS orderId, ");
        query.append("o.number AS orderNumber, ");
        query.append("op.number AS operationNumber, ");
        query.append("MIN(plt.staff_time) AS plannedStaffTime, ");
        appendRealStaffTime(entity, query);
        query.append("AS realStaffTime, ");
        query.append("MIN(plt.machine_time) AS plannedMachineTime, ");
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
        query.append("COALESCE(toc.pieceworkproduction, FALSE) AS pieceworkProduction, ");
        query.append("MIN(q.plannedQuantity) * ");
        appendActualPieceRate(query);
        query.append("AS plannedPieceworkCosts, ");
        query.append("MIN(q.producedQuantity) * ");
        appendActualPieceRate(query);
        query.append("AS realPieceworkCosts, ");
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
        query.append("LEFT JOIN planned_time plt ON plt.order_id = o.id AND plt.toc_id = toc.id ");
        query.append("LEFT JOIN quantities q ON q.order_id = o.id AND q.toc_id = toc.id ");
        query.append("LEFT JOIN technologies_operation op ON toc.operation_id = op.id ");
        query.append("LEFT JOIN basic_piecerate pr ON pr.id = toc.piecerate_id ");
        appendRealStaffCostsJoin(entity, query);
        query.append("CROSS JOIN basic_parameter bp ");
        appendWhereClause(query);
        query.append("AND o.typeofproductionrecording = '03forEach' ");
        query.append("GROUP BY orderId, orderNumber, toc.id, operationNumber, toc.pieceworkproduction, pr.id) ");
        query.append("ORDER BY orderNumber, operationNumber ");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(ProductionCost.class));
    }

    private void appendRealStaffCosts(final Entity entity, final StringBuilder query, final String typeOfProductionRecording) {
        if (includeWageGroups(entity)) {
            query.append(", real_staff_cost (order_id, productiontracking_id, labor_time, staff_cost) AS ");
            query.append("(SELECT o.id AS orderId, pt.id AS productionTrackingId, MIN(swt.labortime) AS laborTime, ");
            query.append("COALESCE(MIN(swt.labortime), 0) / 3600 * COALESCE(MIN(s.laborhourlycost), 0) AS staffCost ");
            query.append("FROM orders_order o ");
            query.append("JOIN productioncounting_productiontracking pt ON pt.order_id = o.id ");
            query.append("JOIN productioncounting_staffworktime swt ON swt.productionrecord_id = pt.id ");
            query.append("JOIN basic_staff s ON swt.worker_id = s.id ");
            appendWhereClause(query);
            query.append("AND pt.state = '02accepted' AND o.typeofproductionrecording = ").append(typeOfProductionRecording);
            query.append("GROUP BY o.id, swt.id, pt.id) ");
        }
    }

    private void appendRealStaffCostsJoin(final Entity entity, final StringBuilder query) {
        if (includeWageGroups(entity)) {
            query.append("LEFT JOIN real_staff_cost rsc ON rsc.order_id = o.id AND rsc.productiontracking_id = pt.id ");
        }
    }

    private void appendRealStaffCostsFromWageGroups(final StringBuilder query) {
        query.append("COALESCE(SUM(rsc.staff_cost), 0) ");
    }

    private boolean includeWageGroups(final Entity entity) {
        return entity.getBooleanField(ProductionBalanceFields.INCLUDE_WAGE_GROUPS);
    }

    private void appendRealMachineTime(final StringBuilder query) {
        query.append("COALESCE(SUM(pt.machinetime), 0) ");
    }

    private void appendRealStaffTime(final Entity entity, final StringBuilder query) {
        if (includeWageGroups(entity)) {
            query.append("COALESCE(SUM(rsc.labor_time), 0) ");
        } else {
            query.append("COALESCE(SUM(pt.labortime), 0) ");
        }
    }

    private void appendForEachRealMachineCosts(final Entity entity, final StringBuilder query) {
        appendRealMachineTime(query);
        query.append("::numeric/ 3600 * ");
        appendForEachMachineHourCost(entity, query);
    }

    private void appendForEachPlannedMachineCosts(final Entity entity, final StringBuilder query) {
        query.append("COALESCE(MIN(plt.machine_time), 0) / 3600 * ");
        appendForEachMachineHourCost(entity, query);
    }

    private void appendForEachRealStaffCosts(final Entity entity, final StringBuilder query) {
        if (includeWageGroups(entity)) {
            appendRealStaffCostsFromWageGroups(query);
        } else {
            appendRealStaffTime(entity, query);
            query.append("::numeric/ 3600 * ");
            appendForEachStaffHourCost(entity, query);
        }
    }

    private void appendForEachPlannedStaffCosts(final Entity entity, final StringBuilder query) {
        query.append("COALESCE(MIN(plt.staff_time), 0) / 3600 * ");
        appendForEachStaffHourCost(entity, query);
    }

    private void appendCumulatedRealMachineCosts(final StringBuilder query) {
        appendRealMachineTime(query);
        query.append("::numeric/ 3600 * ");
        appendCumulatedMachineHourCost(query);
    }

    private void appendCumulatedPlannedMachineCosts(final StringBuilder query) {
        query.append("MIN(plt.machine_time) / 3600 * ");
        appendCumulatedMachineHourCost(query);
    }

    private void appendCumulatedRealStaffCosts(final Entity entity, final StringBuilder query) {
        if (includeWageGroups(entity)) {
            appendRealStaffCostsFromWageGroups(query);
        } else {
            appendRealStaffTime(entity, query);
            query.append("::numeric/ 3600 * ");
            appendCumulatedStaffHourCost(query);
        }
    }

    private void appendCumulatedPlannedStaffCosts(final StringBuilder query) {
        query.append("MIN(plt.staff_time) / 3600 * ");
        appendCumulatedStaffHourCost(query);
    }

    private void appendPlannedMachineTime(final Entity entity, final StringBuilder query) {
        query.append("COALESCE(SUM((toc.tj * pcor.runs ");
        appendTPZandAdditionalTime(entity, query);
        query.append(") * toc.machineutilization), 0) ");
    }

    private void appendPlannedStaffTime(final Entity entity, final StringBuilder query) {
        query.append("COALESCE(SUM((toc.tj * pcor.runs ");
        appendTPZandAdditionalTime(entity, query);
        query.append(") * toc.laborutilization), 0) ");
    }

    private void appendCumulatedStaffHourCost(final StringBuilder query) {
        query.append("COALESCE(MIN(bp.averagelaborhourlycostpb), 0) ");
    }

    private void appendCumulatedMachineHourCost(final StringBuilder query) {
        query.append("COALESCE(MIN(bp.averagemachinehourlycostpb), 0) ");
    }

    private void appendForEachStaffHourCost(final Entity entity, final StringBuilder query) {
        if (SourceOfOperationCosts.TECHNOLOGY_OPERATION.getStringValue()
                .equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS))) {
            query.append("COALESCE(MIN(toc.laborhourlycost), 0) ");
        } else if (SourceOfOperationCosts.PARAMETERS.getStringValue()
                .equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS))) {
            query.append("COALESCE(MIN(bp.averagelaborhourlycostpb), 0) ");
        }
    }

    private void appendForEachMachineHourCost(final Entity entity, final StringBuilder query) {
        if (SourceOfOperationCosts.TECHNOLOGY_OPERATION.getStringValue()
                .equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS))) {
            query.append("COALESCE(MIN(toc.machinehourlycost), 0) ");
        } else if (SourceOfOperationCosts.PARAMETERS.getStringValue()
                .equals(entity.getStringField(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS))) {
            query.append("COALESCE(MIN(bp.averagemachinehourlycostpb), 0) ");
        }
    }

    private void appendTPZandAdditionalTime(final Entity entity, final StringBuilder query) {
        if (entity.getBooleanField(ProductionBalanceFields.INCLUDE_TPZ)) {
            query.append("+ toc.tpz ");
        }
        if (entity.getBooleanField(ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME)) {
            query.append("+ toc.timenextoperation ");
        }
    }

    List<OrderBalance> getOrdersBalance(final Entity entity, List<Long> ordersIds, final List<MaterialCost> materialCosts,
                                        final List<ProductionCost> productionCosts) {
        StringBuilder query = new StringBuilder();

        appendOrdersBalanceWithQueries(materialCosts, productionCosts, query);
        appendOrdersBalanceSelectionClause(entity, query);
        query.append("MIN(COALESCE(gpmc.cost, 0)) AS plannedMaterialCosts, ");
        query.append("MIN(COALESCE(gmc.cost, 0)) AS materialCosts, ");
        query.append("MIN(COALESCE(gmc.cost, 0)) - MIN(COALESCE(gpmc.cost, 0)) AS materialCostsDeviation, ");
        query.append("MIN(gppc.cost) AS plannedProductionCosts, ");
        query.append("MIN(gpc.cost) AS productionCosts, ");
        query.append("MIN(gpc.cost) - MIN(gppc.cost) AS productionCostsDeviation, ");
        query.append("MIN(COALESCE(gmc.cost, 0)) + MIN(gpc.cost) AS technicalProductionCosts, ");
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
        appendTechnicalProductionCostOverheadValue(entity, query);
        query.append("AS technicalProductionCostOverheadValue, ");
        appendTotalManufacturingCost(entity, query);
        query.append("AS totalManufacturingCost, ");
        appendProfitValue(entity, query);
        query.append("AS profitValue, ");
        appendTotalManufacturingCost(entity, query);
        query.append("+ ");
        appendProfitValue(entity, query);
        query.append("AS sellPrice ");
        query.append("FROM orders_order o ");
        query.append("JOIN basic_product prod ON o.product_id = prod.id ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON pt.order_id = o.id AND pt.state = '02accepted' ");
        query.append(
                "LEFT JOIN productioncounting_trackingoperationproductoutcomponent topoc ON topoc.productiontracking_id = pt.id AND topoc.product_id = prod.id ");
        query.append("LEFT JOIN grouped_material_cost gmc ON gmc.order_id = o.id ");
        query.append("LEFT JOIN grouped_planned_material_cost gpmc ON gpmc.order_id = o.id ");
        query.append("JOIN grouped_production_cost gpc ON gpc.order_id = o.id ");
        query.append("JOIN grouped_planned_production_cost gppc ON gppc.order_id = o.id ");
        query.append("LEFT JOIN grouped_additional_cost gac ON gac.order_id = o.id ");
        query.append("LEFT JOIN grouped_external_services_cost gesc ON gesc.order_id = o.id ");
        appendWhereClause(query);
        query.append("GROUP BY orderId, rootId, orderNumber, productNumber, productName, productUnit ");
        query.append("ORDER BY orderNumber ");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(OrderBalance.class));
    }

    private void appendOrdersBalanceWithQueries(final List<MaterialCost> materialCosts, final List<ProductionCost> productionCosts,
                                                final StringBuilder query) {
        query.append("WITH real_material_cost (order_id, cost) AS (VALUES ");

        if (materialCosts.isEmpty()) {
            query.append("(NULL::numeric, NULL::numeric) ");
        } else {
            for (int i = 0; i < materialCosts.size(); i++) {
                MaterialCost materialCost = materialCosts.get(i);

                query.append("(").append(materialCost.getOrderId()).append(", ").append(materialCost.getRealCost()).append(") ");

                if (i != materialCosts.size() - 1) {
                    query.append(", ");
                }
            }
        }

        query.append("), ");
        query.append("planned_material_cost (order_id, cost) AS (VALUES ");

        if (materialCosts.isEmpty()) {
            query.append("(NULL::numeric, NULL::numeric) ");
        } else {
            for (int i = 0; i < materialCosts.size(); i++) {
                MaterialCost materialCost = materialCosts.get(i);

                query.append("(").append(materialCost.getOrderId()).append(", ").append(materialCost.getPlannedCost()).append(") ");

                if (i != materialCosts.size() - 1) {
                    query.append(", ");
                }
            }
        }

        query.append("), ");
        query.append("grouped_material_cost AS (SELECT order_id, SUM(cost) AS cost FROM real_material_cost GROUP BY order_id), ");
        query.append("grouped_planned_material_cost AS (SELECT order_id, SUM(cost) AS cost FROM planned_material_cost GROUP BY order_id), ");
        query.append("real_production_cost (order_id, cost) AS (VALUES ");

        for (int i = 0; i < productionCosts.size(); i++) {
            ProductionCost productionCost = productionCosts.get(i);

            query.append("(").append(productionCost.getOrderId()).append(", ").append(productionCost.getRealCostsSum()).append(") ");

            if (i != productionCosts.size() - 1) {
                query.append(", ");
            }
        }

        query.append("), ");
        query.append("planned_production_cost (order_id, cost) AS (VALUES ");

        for (int i = 0; i < productionCosts.size(); i++) {
            ProductionCost productionCost = productionCosts.get(i);

            query.append("(").append(productionCost.getOrderId()).append(", ").append(productionCost.getPlannedCostsSum()).append(") ");

            if (i != productionCosts.size() - 1) {
                query.append(", ");
            }
        }

        query.append("), ");
        query.append(
                "grouped_production_cost AS (SELECT order_id, SUM(cost) AS cost FROM real_production_cost GROUP BY order_id), ");
        query.append(
                "grouped_planned_production_cost AS (SELECT order_id, SUM(cost) AS cost FROM planned_production_cost GROUP BY order_id), ");
        query.append(
                "grouped_additional_cost AS (SELECT order_id, SUM(actualCost) AS cost FROM costcalculation_orderadditionaldirectcost GROUP BY order_id), ");
        query.append(
                "grouped_external_services_cost AS (SELECT order_id, SUM(totalCost) AS cost FROM techsubcontracting_orderexternalservicecost GROUP BY order_id) ");
    }

    private void appendOrdersBalanceSelectionClause(final Entity entity, final StringBuilder query) {
        query.append("SELECT ");
        query.append("o.id AS orderId, ");
        query.append("o.root_id AS rootId, ");
        query.append("o.number AS orderNumber, ");
        query.append("o.additionalFinalProducts AS additionalFinalProducts, ");
        query.append("prod.number AS productNumber, ");
        query.append("prod.name AS productName, ");
        query.append("prod.unit AS productUnit, ");
        query.append("MIN(o.plannedquantity) AS plannedQuantity, ");
        appendProducedQuantity(query);
        query.append("AS producedQuantity, ");
        appendProducedQuantity(query);
        query.append("- MIN(o.plannedQuantity) AS deviation, ");
        appendMaterialCostMargin(entity, query);
        query.append("AS materialCostMargin, ");
        appendProductionCostMargin(entity, query);
        query.append("AS productionCostMargin, ");
        appendAdditionalOverhead(entity, query);
        query.append("AS additionalOverhead, ");
        appendDirectAdditionalCost(query);
        query.append("AS directAdditionalCost, ");
        appendExternalServicesCost(query);
        query.append("AS externalServicesCost, ");
        appendRegistrationPriceOverhead(entity, query);
        query.append("AS registrationPriceOverhead, ");
        appendTechnicalProductionCostOverhead(entity, query);
        query.append("AS technicalProductionCostOverhead, ");
        appendProfit(entity, query);
        query.append("AS profit, ");
    }

    private void appendMaterialCostMargin(final Entity entity, final StringBuilder query) {
        query.append("COALESCE(" + entity.getDecimalField(ProductionBalanceFields.MATERIAL_COST_MARGIN) + ", 0) ");
    }

    private void appendProductionCostMargin(final Entity entity, final StringBuilder query) {
        query.append("COALESCE(" + entity.getDecimalField(ProductionBalanceFields.PRODUCTION_COST_MARGIN) + ", 0) ");
    }

    private void appendAdditionalOverhead(final Entity entity, final StringBuilder query) {
        query.append("COALESCE(" + entity.getDecimalField(ProductionBalanceFields.ADDITIONAL_OVERHEAD) + ", 0) ");
    }

    private void appendDirectAdditionalCost(final StringBuilder query) {
        query.append("COALESCE(MIN(gac.cost), 0) ");
    }

    private void appendExternalServicesCost(final StringBuilder query) {
        query.append("COALESCE(MIN(gesc.cost), 0) ");
    }

    private void appendRegistrationPriceOverhead(final Entity entity, final StringBuilder query) {
        query.append("COALESCE(" + entity.getDecimalField(ProductionBalanceFields.REGISTRATION_PRICE_OVERHEAD) + ", 0) ");
    }

    private void appendTechnicalProductionCostOverhead(final Entity entity, final StringBuilder query) {
        query.append("COALESCE(" + entity.getDecimalField(ProductionBalanceFields.TECHNICAL_PRODUCTION_COST_OVERHEAD) + ", 0) ");
    }

    private void appendProfit(final Entity entity, final StringBuilder query) {
        query.append("COALESCE(" + entity.getDecimalField(ProductionBalanceFields.PROFIT) + ", 0) ");
    }

    private void appendProfitValue(final Entity entity, final StringBuilder query) {
        appendTotalManufacturingCost(entity, query);
        query.append(" / 100 * ");
        appendProfit(entity, query);
    }

    private void appendTotalManufacturingCost(final Entity entity, final StringBuilder query) {
        query.append("( ");
        appendRealProductionCosts(entity, query);
        query.append("+ ");
        appendTechnicalProductionCostOverheadValue(entity, query);
        query.append(") ");
    }

    private void appendTechnicalProductionCostOverheadValue(final Entity entity, final StringBuilder query) {
        appendRealProductionCosts(entity, query);
        query.append(" / 100 * ");
        appendTechnicalProductionCostOverhead(entity, query);
    }

    private void appendRealProductionCosts(final Entity entity, final StringBuilder query) {
        query.append("( ");
        appendRegistrationPrice(entity, query);
        query.append("+ ");
        appendRegistrationPriceOverheadValue(entity, query);
        query.append(") ");
    }

    private void appendRegistrationPriceOverheadValue(final Entity entity, final StringBuilder query) {
        appendRegistrationPrice(entity, query);
        query.append(" / 100 * ");
        appendRegistrationPriceOverhead(entity, query);
    }

    private void appendRegistrationPrice(final Entity entity, final StringBuilder query) {
        query.append("CASE WHEN ");
        appendProducedQuantity(query);
        query.append("<> 0 THEN (");
        appendTotalCosts(entity, query);
        query.append(")/ ");
        appendProducedQuantity(query);
        query.append("ELSE 0 END ");
    }

    private void appendTotalCosts(final Entity entity, final StringBuilder query) {
        query.append("MIN(COALESCE(gmc.cost, 0)) + MIN(gpc.cost) + ");
        appendMaterialCostMarginValue(entity, query);
        query.append("+ ");
        appendProductionCostMarginValue(entity, query);
        query.append("+ ");
        appendAdditionalOverhead(entity, query);
        query.append("+ ");
        appendDirectAdditionalCost(query);
        query.append("+ ");
        appendExternalServicesCost(query);
    }

    private void appendProductionCostMarginValue(final Entity entity, final StringBuilder query) {
        appendProductionCostMargin(entity, query);
        query.append("/ 100 * MIN(gpc.cost) ");
    }

    private void appendMaterialCostMarginValue(final Entity entity, final StringBuilder query) {
        appendMaterialCostMargin(entity, query);
        query.append("/ 100 * MIN(COALESCE(gmc.cost, 0)) ");
    }

    List<OrderBalance> getComponentsBalance(final Entity entity, final List<Long> ordersIds, final List<OrderBalance> ordersBalance) {
        StringBuilder query = new StringBuilder();

        appendComponentsBalanceWithQueries(ordersBalance, query);
        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("prod.id AS productId, ");
        query.append("prod.number AS productNumber, ");
        query.append("prod.name AS productName, ");
        query.append("o.additionalFinalProducts AS additionalFinalProducts, ");
        appendProducedQuantity(query);
        query.append("AS producedQuantity, ");
        appendMaterialCostMargin(entity, query);
        query.append("AS materialCostMargin, ");
        appendProductionCostMargin(entity, query);
        query.append("AS productionCostMargin, ");
        appendRegistrationPriceOverhead(entity, query);
        query.append("AS registrationPriceOverhead, ");
        appendTechnicalProductionCostOverhead(entity, query);
        query.append("AS technicalProductionCostOverhead, ");
        appendProfit(entity, query);
        query.append("AS profit, ");
        query.append("MIN(obr.additional_overhead) AS additionalOverhead, ");
        query.append("MIN(obr.direct_additional_cost) AS directAdditionalCost, ");
        query.append("MIN(obr.external_services_cost) AS externalServicesCost, ");
        query.append("MIN(obr.material_costs) AS materialCosts, ");
        query.append("MIN(obr.production_costs) AS productionCosts, ");
        query.append("MIN(obr.technical_production_costs) AS technicalProductionCosts, ");
        query.append("MIN(obr.material_cost_margin_value) AS materialCostMarginValue, ");
        query.append("MIN(obr.production_cost_margin_value) AS productionCostMarginValue, ");
        query.append("MIN(obr.total_costs) AS totalCosts, ");
        appendComponentsBalanceRegistrationPrice(query);
        query.append("AS registrationPrice, ");
        appendComponentsBalanceRegistrationPriceOverheadValue(entity, query);
        query.append("AS registrationPriceOverheadValue, ");
        appendComponentsBalanceRealProductionCosts(entity, query);
        query.append("AS realProductionCosts, ");
        appendComponentsBalanceTechnicalProductionCostOverheadValue(entity, query);
        query.append("AS technicalProductionCostOverheadValue, ");
        appendComponentsBalanceTotalManufacturingCost(entity, query);
        query.append("AS totalManufacturingCost, ");
        appendComponentsBalanceProfitValue(entity, query);
        query.append("AS profitValue, ");
        appendComponentsBalanceTotalManufacturingCost(entity, query);
        query.append("+ ");
        appendComponentsBalanceProfitValue(entity, query);
        query.append("AS sellPrice ");
        query.append("FROM orders_order o ");
        query.append("JOIN basic_product prod ON o.product_id = prod.id ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON pt.order_id = o.id AND pt.state = '02accepted' ");
        query.append(
                "LEFT JOIN productioncounting_trackingoperationproductoutcomponent topoc ON topoc.productiontracking_id = pt.id AND topoc.product_id = prod.id ");
        query.append("JOIN order_balance_rec obr ON obr.order_id = o.id ");
        appendWhereClause(query);
        query.append("AND o.root_id IS NULL ");
        query.append("GROUP BY orderNumber, productId, productNumber, productName, additionalFinalProducts ");
        query.append("ORDER BY orderNumber ");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(OrderBalance.class));
    }

    private void appendComponentsBalanceProfitValue(final Entity entity, final StringBuilder query) {
        appendComponentsBalanceTotalManufacturingCost(entity, query);
        query.append(" / 100 * ");
        appendProfit(entity, query);
    }

    private void appendComponentsBalanceTotalManufacturingCost(final Entity entity, final StringBuilder query) {
        query.append("( ");
        appendComponentsBalanceRealProductionCosts(entity, query);
        query.append("+ ");
        appendComponentsBalanceTechnicalProductionCostOverheadValue(entity, query);
        query.append(") ");
    }

    private void appendComponentsBalanceTechnicalProductionCostOverheadValue(final Entity entity, final StringBuilder query) {
        appendComponentsBalanceRealProductionCosts(entity, query);
        query.append(" / 100 * ");
        appendTechnicalProductionCostOverhead(entity, query);
    }

    private void appendComponentsBalanceRealProductionCosts(final Entity entity, final StringBuilder query) {
        query.append("( ");
        appendComponentsBalanceRegistrationPrice(query);
        query.append("+ ");
        appendComponentsBalanceRegistrationPriceOverheadValue(entity, query);
        query.append(") ");
    }

    private void appendComponentsBalanceRegistrationPriceOverheadValue(final Entity entity, final StringBuilder query) {
        appendComponentsBalanceRegistrationPrice(query);
        query.append(" / 100 * ");
        appendRegistrationPriceOverhead(entity, query);
    }

    private void appendComponentsBalanceRegistrationPrice(final StringBuilder query) {
        query.append("CASE WHEN ");
        appendProducedQuantity(query);
        query.append("<> 0 THEN MIN(obr.total_costs) ");
        query.append("/ ");
        appendProducedQuantity(query);
        query.append("ELSE 0 END ");
    }

    private void appendComponentsBalanceWithQueries(final List<OrderBalance> ordersBalance, final StringBuilder query) {
        query.append("WITH order_balance (order_id, root_id, material_costs, ");
        query.append("production_costs, technical_production_costs, material_cost_margin_value, ");
        query.append("production_cost_margin_value, additional_overhead, direct_additional_cost, external_services_cost, total_costs ");
        query.append(") AS (VALUES ");

        for (int i = 0; i < ordersBalance.size(); i++) {
            OrderBalance orderBalance = ordersBalance.get(i);

            query.append("(");
            query.append(orderBalance.getOrderId()).append(", ");
            query.append(orderBalance.getRootId()).append("::INTEGER, ");
            query.append(orderBalance.getMaterialCosts()).append(", ");
            query.append(orderBalance.getProductionCosts()).append(", ");
            query.append(orderBalance.getTechnicalProductionCosts()).append(", ");
            query.append(orderBalance.getMaterialCostMarginValue()).append(", ");
            query.append(orderBalance.getProductionCostMarginValue()).append(", ");
            query.append(orderBalance.getAdditionalOverhead()).append(", ");
            query.append(orderBalance.getDirectAdditionalCost()).append(", ");
            query.append(orderBalance.getExternalServicesCost()).append(", ");
            query.append(orderBalance.getTotalCosts());
            query.append(") ");

            if (i != ordersBalance.size() - 1) {
                query.append(", ");
            }
        }

        query.append("), ");
        query.append("order_balance_rec AS (WITH RECURSIVE order_balance_rec AS ");
        query.append("(SELECT order_id, order_id AS root_id, material_costs, ");
        query.append("production_costs, technical_production_costs, material_cost_margin_value, ");
        query.append("production_cost_margin_value, additional_overhead, direct_additional_cost, external_services_cost, total_costs ");
        query.append("FROM order_balance WHERE root_id IS NULL ");
        query.append("UNION ALL ");
        query.append("SELECT obr.order_id, ob.order_id, ob.material_costs, ");
        query.append("ob.production_costs, ob.technical_production_costs, ob.material_cost_margin_value, ");
        query.append("ob.production_cost_margin_value, ob.additional_overhead, ob.direct_additional_cost, ob.external_services_cost, ob.total_costs ");
        query.append("FROM order_balance_rec obr JOIN order_balance ob USING(root_id)) ");
        query.append("SELECT order_id, SUM(material_costs) AS material_costs, SUM(production_costs) AS production_costs, ");
        query.append(
                "SUM(technical_production_costs) AS technical_production_costs, SUM(material_cost_margin_value) AS material_cost_margin_value, ");
        query.append(
                "SUM(production_cost_margin_value) AS production_cost_margin_value, SUM(additional_overhead) AS additional_overhead, ");
        query.append("SUM(direct_additional_cost) AS direct_additional_cost, SUM(external_services_cost) AS external_services_cost, ");
        query.append("SUM(total_costs) AS total_costs ");
        query.append("FROM order_balance_rec GROUP BY order_id) ");
    }

    List<OrderBalance> getProductsBalance(final Entity entity, final List<Long> ordersIds, final List<OrderBalance> componentsBalance) {
        StringBuilder query = new StringBuilder();

        appendProductsBalanceWithQueries(componentsBalance, query);
        query.append("SELECT ");
        query.append("prod.number AS productNumber, ");
        query.append("prod.name AS productName, ");
        appendMaterialCostMargin(entity, query);
        query.append("AS materialCostMargin, ");
        appendProductionCostMargin(entity, query);
        query.append("AS productionCostMargin, ");
        appendRegistrationPriceOverhead(entity, query);
        query.append("AS registrationPriceOverhead, ");
        appendTechnicalProductionCostOverhead(entity, query);
        query.append("AS technicalProductionCostOverhead, ");
        appendProfit(entity, query);
        query.append("AS profit, ");
        query.append("MIN(gcb.produced_quantity) AS producedQuantity, ");
        query.append("MIN(gcb.additional_overhead) AS additionalOverhead, ");
        query.append("MIN(gcb.direct_additional_cost) AS directAdditionalCost, ");
        query.append("MIN(gcb.external_services_cost) AS externalServicesCost, ");
        query.append("MIN(gcb.material_costs) AS materialCosts, ");
        query.append("MIN(gcb.production_costs) AS productionCosts, ");
        query.append("MIN(gcb.technical_production_costs) AS technicalProductionCosts, ");
        query.append("MIN(gcb.material_cost_margin_value) AS materialCostMarginValue, ");
        query.append("MIN(gcb.production_cost_margin_value) AS productionCostMarginValue, ");
        query.append("MIN(gcb.total_costs) AS totalCosts, ");
        appendProductsBalanceRegistrationPrice(query);
        query.append("AS registrationPrice, ");
        appendProductsBalanceRegistrationPriceOverheadValue(entity, query);
        query.append("AS registrationPriceOverheadValue, ");
        appendProductsBalanceRealProductionCosts(entity, query);
        query.append("AS realProductionCosts, ");
        appendProductsBalanceTechnicalProductionCostOverheadValue(entity, query);
        query.append("AS technicalProductionCostOverheadValue, ");
        appendProductsBalanceTotalManufacturingCost(entity, query);
        query.append("AS totalManufacturingCost, ");
        appendProductsBalanceProfitValue(entity, query);
        query.append("AS profitValue, ");
        appendProductsBalanceTotalManufacturingCost(entity, query);
        query.append("+ ");
        appendProductsBalanceProfitValue(entity, query);
        query.append("AS sellPrice ");
        query.append("FROM orders_order o ");
        query.append("JOIN basic_product prod ON o.product_id = prod.id ");
        query.append("JOIN grouped_component_balance gcb ON gcb.product_id = prod.id ");
        appendWhereClause(query);
        query.append("AND o.root_id IS NULL ");
        query.append("GROUP BY productNumber, productName ");
        query.append("ORDER BY productNumber ");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(OrderBalance.class));
    }

    private void appendProductsBalanceProfitValue(final Entity entity, final StringBuilder query) {
        appendProductsBalanceTotalManufacturingCost(entity, query);
        query.append(" / 100 * ");
        appendProfit(entity, query);
    }

    private void appendProductsBalanceTotalManufacturingCost(final Entity entity, final StringBuilder query) {
        query.append("( ");
        appendProductsBalanceRealProductionCosts(entity, query);
        query.append("+ ");
        appendProductsBalanceTechnicalProductionCostOverheadValue(entity, query);
        query.append(") ");
    }

    private void appendProductsBalanceTechnicalProductionCostOverheadValue(final Entity entity, final StringBuilder query) {
        appendProductsBalanceRealProductionCosts(entity, query);
        query.append(" / 100 * ");
        appendTechnicalProductionCostOverhead(entity, query);
    }

    private void appendProductsBalanceRealProductionCosts(final Entity entity, final StringBuilder query) {
        query.append("( ");
        appendProductsBalanceRegistrationPrice(query);
        query.append("+ ");
        appendProductsBalanceRegistrationPriceOverheadValue(entity, query);
        query.append(") ");
    }

    private void appendProductsBalanceRegistrationPriceOverheadValue(final Entity entity, final StringBuilder query) {
        appendProductsBalanceRegistrationPrice(query);
        query.append(" / 100 * ");
        appendRegistrationPriceOverhead(entity, query);
    }

    private void appendProductsBalanceRegistrationPrice(final StringBuilder query) {
        query.append("CASE WHEN MIN(gcb.produced_quantity) <> 0 THEN MIN(gcb.total_costs) ");
        query.append("/ MIN(gcb.produced_quantity) ELSE 0 END ");
    }

    private void appendProductsBalanceWithQueries(final List<OrderBalance> componentsBalance, final StringBuilder query) {
        query.append("WITH component_balance (product_id, produced_quantity, material_costs, ");
        query.append("production_costs, technical_production_costs, material_cost_margin_value, ");
        query.append("production_cost_margin_value, additional_overhead, direct_additional_cost, external_services_cost, total_costs ");
        query.append(") AS (VALUES ");

        if (componentsBalance.isEmpty()) {
            query.append("(NULL::integer, NULL::numeric, NULL::numeric, NULL::numeric, NULL::numeric, NULL::numeric, ");
            query.append("NULL::numeric, NULL::numeric, NULL::numeric, NULL::numeric, NULL::numeric) ");
        } else {
            for (int i = 0; i < componentsBalance.size(); i++) {
                OrderBalance orderBalance = componentsBalance.get(i);

                query.append("(");
                query.append(orderBalance.getProductId()).append(", ");
                query.append(orderBalance.getProducedQuantity()).append(", ");
                query.append(orderBalance.getMaterialCosts()).append(", ");
                query.append(orderBalance.getProductionCosts()).append(", ");
                query.append(orderBalance.getTechnicalProductionCosts()).append(", ");
                query.append(orderBalance.getMaterialCostMarginValue()).append(", ");
                query.append(orderBalance.getProductionCostMarginValue()).append(", ");
                query.append(orderBalance.getAdditionalOverhead()).append(", ");
                query.append(orderBalance.getDirectAdditionalCost()).append(", ");
                query.append(orderBalance.getExternalServicesCost()).append(", ");
                query.append(orderBalance.getTotalCosts());
                query.append(") ");

                if (i != componentsBalance.size() - 1) {
                    query.append(", ");
                }
            }
        }

        query.append("), ");
        query.append("grouped_component_balance AS (SELECT product_id, SUM(produced_quantity) AS produced_quantity, ");
        query.append("SUM(material_costs) AS material_costs, SUM(production_costs) AS production_costs, ");
        query.append(
                "SUM(technical_production_costs) AS technical_production_costs, SUM(material_cost_margin_value) AS material_cost_margin_value, ");
        query.append(
                "SUM(production_cost_margin_value) AS production_cost_margin_value, SUM(additional_overhead) AS additional_overhead, ");
        query.append("SUM(direct_additional_cost) AS direct_additional_cost, SUM(external_services_cost) AS external_services_cost, ");
        query.append("SUM(total_costs) AS total_costs ");
        query.append("FROM component_balance GROUP BY product_id) ");
    }

    List<Stoppage> getStoppages(final List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("pt.number AS productionTrackingNumber, ");
        query.append("pt.state AS productionTrackingState, ");
        query.append("s.duration, ");
        query.append("s.datefrom AS dateFrom, ");
        query.append("s.dateto AS dateTo, ");
        query.append("sr.name AS reason, ");
        query.append("s.description, ");
        query.append("COALESCE(ptd.number, od.number) AS division, ");
        query.append("pl.number AS productionLine, ");
        query.append("w.number AS workstation, ");
        query.append("stf.name || ' ' || stf.surname AS worker ");
        query.append("FROM stoppage_stoppage s ");
        query.append("JOIN orders_order o ON o.id = s.order_id ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON pt.id = s.productiontracking_id ");
        query.append("JOIN stoppage_stoppagereason sr ON sr.id = s.reason_id ");
        query.append("LEFT JOIN basic_division ptd ON ptd.id = pt.division_id ");
        query.append("LEFT JOIN basic_division od ON od.id = o.division_id ");
        query.append("LEFT JOIN productionlines_productionline pl ON pl.id = o.productionline_id ");
        query.append("LEFT JOIN basic_workstation w ON w.id = pt.workstation_id ");
        query.append("LEFT JOIN basic_staff stf ON pt.staff_id = stf.id ");
        appendWhereClause(query);
        query.append("ORDER BY orderNumber, productionTrackingNumber, dateFrom ");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(Stoppage.class));
    }

    public List<AdditionalCost> getAdditionalCosts(final List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("adc.number, ");
        query.append("adc.name, ");
        query.append("COALESCE(oadc.actualcost, 0) AS actualCost ");
        query.append("FROM costcalculation_orderadditionaldirectcost oadc ");
        query.append("JOIN orders_order o ON o.id = oadc.order_id ");
        query.append("JOIN costcalculation_additionaldirectcost adc ON adc.id = oadc.additionaldirectcost_id ");
        appendWhereClause(query);
        query.append("ORDER BY orderNumber, number ");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(AdditionalCost.class));
    }

    public List<ExternalServiceCost> getExternalServiceCosts(final List<Long> ordersIds) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("op.number AS operationNumber, ");
        query.append("p.number AS productNumber, ");
        query.append("COALESCE(oesc.unitCost, 0) AS unitCost, ");
        query.append("COALESCE(oesc.quantity, 0) AS quantity, ");
        query.append("p.unit AS productUnit, ");
        query.append("COALESCE(oesc.totalCost, 0) AS totalCost ");
        query.append("FROM techsubcontracting_orderexternalservicecost oesc ");
        query.append("JOIN orders_order o ON o.id = oesc.order_id ");
        query.append("LEFT JOIN technologies_technologyoperationcomponent toc ON toc.id = oesc.technologyoperationcomponent_id ");
        query.append("LEFT JOIN technologies_operation op ON op.id = toc.operation_id ");
        query.append("LEFT JOIN basic_product p ON p.id = oesc.product_id ");
        appendWhereClause(query);
        query.append("ORDER BY orderNumber, operationNumber, productNumber");

        return jdbcTemplate.query(query.toString(), new MapSqlParameterSource("ordersIds", ordersIds),
                BeanPropertyRowMapper.newInstance(ExternalServiceCost.class));
    }

}
