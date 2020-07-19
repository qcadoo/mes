package com.qcadoo.mes.orders.controllers.responses;

import com.qcadoo.mes.orders.controllers.dto.OrderHolder;

public class OrderResponse {

    private OrderHolder order;

    private String message = "";

    public OrderResponse(OrderHolder order) {
        this.order = order;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OrderHolder getOrder() {
        return order;
    }

    public void setOrder(OrderHolder order) {
        this.order = order;
    }
}
