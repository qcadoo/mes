package com.qcadoo.mes.productionCounting.xls;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.qcadoo.mes.productionCounting.xls.dto.ProducedQuantities;
import com.qcadoo.mes.productionCounting.xls.dto.ProductionCost;

@Repository
class ProductionBalanceRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    List<ProductionCost> getCumulatedProductionCosts() {

        StringBuilder query = new StringBuilder("SELECT ");
        query.append("o.number AS orderNumber ");
        query.append("FROM orders_order o ");
        query.append("JOIN basicproductioncounting_productioncountingquantity pcq ON pcq.order_id = o.id ");
        query.append("WHERE pcq.role = '01used' AND pcq.typeofmaterial = '01component' ");
        query.append("ORDER BY o.number ");

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

    private void appendWhereClause(StringBuilder query) {
        query.append("o.id IN (:ordersIds) ");
    }
}
