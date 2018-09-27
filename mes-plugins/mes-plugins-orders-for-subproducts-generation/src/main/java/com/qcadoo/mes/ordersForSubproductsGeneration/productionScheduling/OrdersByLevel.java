package com.qcadoo.mes.ordersForSubproductsGeneration.productionScheduling;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.qcadoo.model.api.Entity;

public class OrdersByLevel {

    public static final int ROOT_LEVEL = 0;

    private Integer level;

    private List<Entity> orders = Lists.newArrayList();

    public OrdersByLevel(final Integer level) {
        if (Objects.isNull(level)) {
            this.level = ROOT_LEVEL;
        } else {
            this.level = level;
        }
    }

    public List<Entity> pushOrder(final Entity order) {
        orders.add(order);

        return orders;
    }

    public Integer getLevel() {
        return level;
    }

    public List<Entity> getOrders() {
        return orders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof OrdersByLevel)) {
            return false;
        }

        OrdersByLevel that = (OrdersByLevel) o;

        return Objects.equals(level, that.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level);
    }

}
