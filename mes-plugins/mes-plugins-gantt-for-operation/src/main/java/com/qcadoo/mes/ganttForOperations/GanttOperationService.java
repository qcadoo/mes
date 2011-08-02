package com.qcadoo.mes.ganttForOperations;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class GanttOperationService {

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    private Long orderId;

    public void refereshGanttChart(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.getComponentByReference("gantt").performEvent(viewDefinitionState, "refresh");
    }

    public void disableFormWhenNoOrderSelected(final ViewDefinitionState viewDefinitionState) {
        if (viewDefinitionState.getComponentByReference("gantt").getFieldValue() == null) {
            viewDefinitionState.getComponentByReference("dateFrom").setEnabled(false);
            viewDefinitionState.getComponentByReference("dateTo").setEnabled(false);
        } else {
            viewDefinitionState.getComponentByReference("dateFrom").setEnabled(true);
            viewDefinitionState.getComponentByReference("dateTo").setEnabled(true);
        }
    }

    public void showOperationsGantt(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        orderId = (Long) triggerState.getFieldValue();

        scheduleOrder(orderId);

        if (orderId != null) {
            String url = "../page/ganttForOperations/ganttForOperations.html?context={\"gantt.orderId\":\"" + orderId + "\"}";

            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    private void scheduleOrder(final Long orderId) {
        Entity order = dataDefinitionService.get("orders", "order").get(orderId);

        if (order == null) {
            return;
        }

        DataDefinition dataDefinition = dataDefinitionService.get("productionScheduling", "orderOperationComponent");

        List<Entity> operations = dataDefinition.find().add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

        Date orderStartDate = null;

        if (order.getField("effectiveDateFrom") != null) {
            orderStartDate = (Date) order.getField("effectiveDateFrom");
        } else if (order.getField("dateFrom") != null) {
            orderStartDate = (Date) order.getField("dateFrom");
        } else {
            return;
        }

        for (Entity operation : operations) {
            Integer offset = (Integer) operation.getField("operationOffSet");
            Integer duration = (Integer) operation.getField("effectiveOperationRealizationTime");

            operation.setField("effectiveDateFrom", null);
            operation.setField("effectiveDateTo", null);

            if (offset == null || duration == null || duration.equals(0)) {
                continue;
            }

            if (offset == 0) {
                offset = 1;
            }

            Date dateFrom = shiftsService.findDateToForOrder(orderStartDate, offset);

            if (dateFrom == null) {
                continue;
            }

            Date dateTo = shiftsService.findDateToForOrder(orderStartDate, offset + duration);

            if (dateTo == null) {
                continue;
            }

            operation.setField("effectiveDateFrom", dateFrom);
            operation.setField("effectiveDateTo", dateTo);
        }

        for (Entity operation : operations) {
            dataDefinition.save(operation);
        }
    }

    public void checkDoneCalculate(final ViewDefinitionState viewDefinitionState) {

        ComponentState window = (ComponentState) viewDefinitionState.getComponentByReference("form");
        System.out.println("***ala" + orderId);
        System.out.println("***ala22" + window);
        Entity order = dataDefinitionService.get("orders", "order").get(orderId);
        String realizationTime = order.getStringField("realizationTime");
        if ("".equals(realizationTime) || realizationTime == null) {
            window.addMessage(
                    translationService.translate("orders.order.report.realizationTime", viewDefinitionState.getLocale()),
                    MessageType.INFO, false);
        }

    }

}
