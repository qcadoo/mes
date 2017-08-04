package com.qcadoo.mes.productionCounting.xls;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

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

        return jdbcTemplate.query(query.toString(), Collections.emptyMap(), BeanPropertyRowMapper.newInstance(ProductionCost.class));
    }
}
