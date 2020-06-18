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

    public List<Long> getData() {
        List<Long> data = Lists.newArrayList();
        String pendingQuery = "SELECT sum(o.plannedquantity) FROM orders_order o "
                + "WHERE o.state not in ('05declined', '07abandoned', '04completed') AND o.startdate <= current_date "
                + "AND current_date <= o.finishdate AND coalesce(o.donequantity, 0) = 0";
        String inProgressQuery = "SELECT sum(o.plannedquantity) FROM orders_order o "
                + "WHERE o.state not in ('05declined', '07abandoned','04completed') AND o.startdate <= current_date "
                + "AND current_date <= o.finishdate AND o.donequantity * 100 / o.plannedquantity > 0 "
                + "AND o.donequantity * 100 / o.plannedquantity < 100";
        String doneQuery = "SELECT sum(coalesce(o.donequantity, 0)) FROM orders_order o "
                + "WHERE o.state not in ('05declined', '07abandoned') AND o.startdate <= current_date "
                + "AND current_date <= o.finishdate AND (o.donequantity * 100 / o.plannedquantity >= 100 "
                + "OR o.state = '04completed')";

        data.add(jdbcTemplate.queryForObject(pendingQuery, Collections.emptyMap(), Long.class));
        data.add(jdbcTemplate.queryForObject(inProgressQuery, Collections.emptyMap(), Long.class));
        data.add(jdbcTemplate.queryForObject(doneQuery, Collections.emptyMap(), Long.class));
        return data;
    }

}
