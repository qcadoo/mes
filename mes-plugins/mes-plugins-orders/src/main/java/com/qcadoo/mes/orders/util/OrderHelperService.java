package com.qcadoo.mes.orders.util;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;

@Service
public class OrderHelperService {

    public List<String> getOrdersWithoutTechnology(List<Entity> orders) {
        List<String> ordersWithoutTechnology = Lists.newArrayList();
        for (Entity order : orders) {
            if (order.getBelongsToField("technology") == null) {
                String number = order.getStringField(OrderFields.NUMBER);
                String name = order.getStringField(OrderFields.NAME);
                StringBuilder numberAndName = new StringBuilder();
                numberAndName.append(number).append(": ").append(name);
                ordersWithoutTechnology.add(numberAndName.toString());
            }
        }
        return ordersWithoutTechnology;
    }

}
