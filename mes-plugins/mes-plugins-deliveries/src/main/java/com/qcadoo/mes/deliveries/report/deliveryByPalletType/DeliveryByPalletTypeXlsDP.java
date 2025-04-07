package com.qcadoo.mes.deliveries.report.deliveryByPalletType;

import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
class DeliveryByPalletTypeXlsDP {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public Map<DeliveryByPalletTypeKey, DeliveryByPalletTypeValue> findEntries(final Map<String, Object> filters) {
        Map<String, Object> _filters = (Map<String, Object>) filters.get("params");
        Long from = (Long) _filters.get("from");
        Long to = (Long) _filters.get("to");
        Map<String, Object> params = Maps.newHashMap();
        params.put("fromDate", new Date(from));
        params.put("toDate", new DateTime(to).plusDays(1).toDate());
        String query = buildQuery();
        List<DeliveryByPalletTypeEntry> entries = jdbcTemplate.query(query, params, new BeanPropertyRowMapper(
                DeliveryByPalletTypeEntry.class));
        Map<DeliveryByPalletTypeKey, DeliveryByPalletTypeValue> deliveryByPallet = Maps.newLinkedHashMap();

        for (DeliveryByPalletTypeEntry entry : entries) {
            DeliveryByPalletTypeKey key = new DeliveryByPalletTypeKey(entry);
            if (deliveryByPallet.containsKey(key)) {
                DeliveryByPalletTypeValue value = deliveryByPallet.get(key);
                value.addQuantityForPallet(entry.getTypeOfLoadUnit(), entry.getNumberOfPallets());
            } else {
                DeliveryByPalletTypeValue value = new DeliveryByPalletTypeValue();
                value.addQuantityForPallet(entry.getTypeOfLoadUnit(), entry.getNumberOfPallets());
                deliveryByPallet.put(key, value);
            }
        }
        return deliveryByPallet;
    }

    private String buildQuery() {
        StringBuilder query = new StringBuilder();
        query.append("SELECT delivery.id, delivery.number, typeofloadunit.name AS typeofloadunit, deliverystatechange.dateandtime as date, ");
        query.append("count(DISTINCT deliveredproduct.palletnumber_id) as numberofpallets FROM deliveries_delivery delivery ");
        query.append("LEFT JOIN deliveries_deliveredproduct deliveredproduct ON deliveredproduct.delivery_id = delivery.id ");
        query.append("LEFT JOIN basic_typeofloadunit typeofloadunit ON typeofloadunit.id = deliveredproduct.typeofloadunit_id ");
        query.append("LEFT JOIN ");
        query.append("  (SELECT dp.palletnumber_id, count(dp.palletnumber_id) > 0 AS anyWaste FROM deliveries_deliveredproduct dp ");
        query.append("  JOIN deliveries_delivery delivery ON delivery.id = dp.delivery_id ");
        query.append("  LEFT JOIN deliveries_deliverystatechange deliverystatechange ON deliverystatechange.delivery_id = dp.delivery_id AND deliverystatechange.status = '03successful' AND deliverystatechange.targetstate = '06received' ");
        query.append("  WHERE delivery.state = '06received' AND deliverystatechange.dateandtime >= :fromDate AND deliverystatechange.dateandtime <= :toDate AND dp.iswaste = true ");
        query.append("  GROUP BY dp.palletnumber_id ) AS otherdp ");
        query.append("ON otherdp.palletnumber_id = deliveredproduct.palletnumber_id ");
        query.append("LEFT JOIN deliveries_deliverystatechange deliverystatechange ON deliverystatechange.delivery_id = delivery.id AND deliverystatechange.status = '03successful' AND deliverystatechange.targetstate = '06received' ");
        query.append("WHERE delivery.state = '06received' AND deliverystatechange.dateandtime >= :fromDate AND deliverystatechange.dateandtime <= :toDate AND otherdp.anyWaste is null ");
        query.append("GROUP BY delivery.id, delivery.number, deliverystatechange.dateandtime, typeofloadunit.name ");
        query.append("ORDER BY deliverystatechange.dateandtime ASC, delivery.number");
        return query.toString();
    }
}
