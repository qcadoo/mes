package com.qcadoo.mes.orders.controllers;

import com.qcadoo.mes.orders.controllers.requests.OrderCreationRequest;
import com.qcadoo.mes.orders.controllers.requests.TechnologyCreationRequest;
import com.qcadoo.mes.orders.controllers.responses.OrderCreationResponse;

import com.qcadoo.mes.orders.controllers.responses.TechnologyCreationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OrdersApiController {

    @Autowired
    private OrderCreationService orderCreationService;

    @Autowired
    private TechnologyCreationService technologyCreationService;

    @ResponseBody
    @RequestMapping(value = "/order", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderCreationResponse saveOrder(@RequestBody OrderCreationRequest orderCreationRequest) {
        return orderCreationService.createOrder(orderCreationRequest);
    }

    @ResponseBody
    @RequestMapping(value = "/operationalTasks", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderCreationResponse saveOrderWithOperationalTasks(@RequestBody OrderCreationRequest orderCreationRequest) {
        return orderCreationService.createOrder(orderCreationRequest);
    }

    @ResponseBody
    @RequestMapping(value = "/createTechnology", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public TechnologyCreationResponse createTechnology(@RequestBody TechnologyCreationRequest technologyCreationRequest) {
        return technologyCreationService.createTechnology(technologyCreationRequest);
    }
}
