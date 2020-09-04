package com.qcadoo.mes.basic.controllers.dataProvider;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class DailyProductionChartDataProvider {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private static final String QUERY_DATE_PART = "AND date_trunc('day', o.startdate) <= current_date AND current_date <= date_trunc('day', o.finishdate) ";

    public List<Long> getData() {
        List<Long> data = Lists.newArrayList();
        String pendingQuery = "SELECT coalesce(sum(o.plannedquantity), 0) FROM orders_order o "
                + "WHERE o.state NOT IN ('05declined', '07abandoned', '04completed') " + QUERY_DATE_PART
                + "AND coalesce(o.donequantity, 0) = 0";
        String inProgressQuery = "SELECT coalesce(sum(o.plannedquantity), 0) FROM orders_order o "
                + "WHERE o.state NOT IN ('05declined', '07abandoned','04completed') " + QUERY_DATE_PART
                + "AND (o.plannedquantity > 0 AND o.donequantity * 100 / o.plannedquantity > 0 AND o.donequantity * 100 / o.plannedquantity < 100)";
        String doneQuery = "SELECT coalesce(sum(o.donequantity), 0) FROM orders_order o "
                + "WHERE o.state not in ('05declined', '07abandoned') " + QUERY_DATE_PART
                + "AND ((o.plannedquantity > 0 AND o.donequantity * 100 / o.plannedquantity >= 100) OR o.state = '04completed')";

        data.add(jdbcTemplate.queryForObject(pendingQuery, Collections.emptyMap(), Long.class));
        data.add(jdbcTemplate.queryForObject(inProgressQuery, Collections.emptyMap(), Long.class));
        data.add(jdbcTemplate.queryForObject(doneQuery, Collections.emptyMap(), Long.class));
        return data;
    }

}
