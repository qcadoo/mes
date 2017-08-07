package com.qcadoo.mes.productionCounting.xls;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.qcadoo.mes.productionCounting.xls.dto.PieceworkDetails;
import com.qcadoo.mes.productionCounting.xls.dto.ProducedQuantities;
import com.qcadoo.mes.productionCounting.xls.dto.ProductionCost;

@Repository
class ProductionBalanceRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    List<ProductionCost> getProductionCosts(List<Long> ordersIds) {

        StringBuilder query = new StringBuilder("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("NULL AS operationNumber, ");
        query.append("p.number AS productNumber, ");
        query.append("p.name AS productName ");
        query.append("FROM orders_order o ");
        query.append("JOIN basicproductioncounting_productioncountingquantity pcq ON pcq.order_id = o.id ");
        query.append("JOIN basic_product p ON pcq.product_id = p.id ");
        query.append("WHERE pcq.role = '01used' AND pcq.typeofmaterial = '01component' AND pcq.isnoncomponent = true ");
        query.append("AND pcq.typeofproductionrecording = '02cumulated' ");
        appendWhereClause(query);
        query.append("GROUP BY o.number ");
        query.append("UNION ");
        query.append("SELECT ");
        query.append("o.number AS orderNumber, ");
        query.append("to.number AS operationNumber, ");
        query.append("p.number AS productNumber, ");
        query.append("p.name AS productName ");
        query.append("FROM orders_order o ");
        query.append("JOIN basicproductioncounting_productioncountingquantity pcq ON pcq.order_id = o.id ");
        query.append("JOIN technologies_technologyoperationcomponent toc ON pcq.technologyoperationcomponent_id = toc.id ");
        query.append("JOIN technologies_operation to ON toc.operation_id = to.id ");
        query.append("JOIN basic_product p ON pcq.product_id = p.id ");
        query.append("WHERE pcq.role = '01used' AND pcq.typeofmaterial = '01component' AND pcq.isnoncomponent = true ");
        query.append("AND pcq.typeofproductionrecording = '03forEach' ");
        appendWhereClause(query);
        query.append("ORDER BY orderNumber ");

        Map<String, Object> params = Maps.newHashMap();
        params.put("ordersIds", ordersIds);

        return jdbcTemplate.query(query.toString(), Collections.emptyMap(),
                BeanPropertyRowMapper.newInstance(ProductionCost.class));
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

        Map<String, Object> params = Maps.newHashMap();
        params.put("ordersIds", ordersIds);

        return jdbcTemplate.query(query.toString(), params, BeanPropertyRowMapper.newInstance(ProducedQuantities.class));
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

    private void appendWhereClause(StringBuilder query) {
        query.append("o.id IN (:ordersIds) ");
    }
}
