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
        String query = "SELECT count(*) FROM orders_order o WHERE o.state not in ('05declined', '07abandoned') "
                + "AND o.startdate <= current_date AND current_date <= o.finishdate ";

        data.add(jdbcTemplate.queryForObject(query + "AND o.donequantity = 0", Collections.emptyMap(), Long.class));
        data.add(jdbcTemplate.queryForObject(
                query + "AND o.donequantity * 100 / o.plannedquantity > 0 AND o.donequantity * 100 / o.plannedquantity < 100",
                Collections.emptyMap(), Long.class));
        data.add(jdbcTemplate.queryForObject(query + "AND o.donequantity * 100 / o.plannedquantity >= 100",
                Collections.emptyMap(), Long.class));
        return data;
    }

}
