package com.qcadoo.mes.orders.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qcadoo.mes.orders.controllers.dao.OperationalTaskHolder;
import com.qcadoo.mes.orders.controllers.dao.OrderHolder;
import com.qcadoo.mes.orders.controllers.dataProvider.DashboardKanbanDataProvider;

@Controller
@RequestMapping("/dashboardKanban")
public class DashboardKanbanController {

    @Autowired
    private DashboardKanbanDataProvider dashboardKanbanDataProvider;

    @ResponseBody
    @RequestMapping(value = "/ordersPending", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OrderHolder> getOrdersPending() {
        return dashboardKanbanDataProvider.getOrdersPending();
    }

    @ResponseBody
    @RequestMapping(value = "/ordersInProgress", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OrderHolder> getOrdersInProgress() {
        return dashboardKanbanDataProvider.getOrdersInProgress();
    }

    @ResponseBody
    @RequestMapping(value = "/ordersCompleted", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OrderHolder> getOrdersCompleted() {
        return dashboardKanbanDataProvider.getOrdersCompleted();
    }

    @ResponseBody
    @RequestMapping(value = "/operationalTasksPending", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OperationalTaskHolder> getOperationalTasksPending() {
        return dashboardKanbanDataProvider.getOperationalTasksPending();
    }

    @ResponseBody
    @RequestMapping(value = "/operationalTasksInProgress", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OperationalTaskHolder> getOperationalTasksInProgress() {
        return dashboardKanbanDataProvider.getOperationalTasksInProgress();
    }

    @ResponseBody
    @RequestMapping(value = "/operationalTasksCompleted", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OperationalTaskHolder> getOperationalTasksCompleted() {
        return dashboardKanbanDataProvider.getOperationalTasksCompleted();
    }

}
