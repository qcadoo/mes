/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.orders.constants;

public final class OrdersConstants {

    private OrdersConstants() {

    }

    public static final String PLUGIN_IDENTIFIER = "orders";

    // MODEL

    public static final String MODEL_ORDER = "order";

    public static final String MODEL_ORDER_DTO = "orderDto";

    public static final String MODEL_ORDER_PLANNING_LIST_DTO = "orderPlanningListDto";

    public static final String MODEL_ORDER_STATE_CHANGE = "orderStateChange";

    public static final String MODEL_REASON_TYPE_CORRECTION_DATE_TO = "reasonTypeCorrectionDateTo";

    public static final String MODEL_REASON_TYPE_CORRECTION_DATE_FROM = "reasonTypeCorrectionDateFrom";

    public static final String MODEL_REASON_TYPE_DEVIATION_EFFECTIVE_START = "reasonTypeDeviationEffectiveStart";

    public static final String MODEL_REASON_TYPE_DEVIATION_EFFECTIVE_END = "reasonTypeDeviationEffectiveEnd";

    public static final String MODEL_TYPE_OF_CORRECTION_CAUSES = "typeOfCorrectionCauses";

    public static final String MODEL_SCHEDULE = "schedule";

    public static final String MODEL_PRODUCTION_LINE_SCHEDULE = "productionLineSchedule";

    public static final String MODEL_SCHEDULE_STATE_CHANGE = "scheduleStateChange";

    public static final String MODEL_PRODUCTION_LINE_SCHEDULE_STATE_CHANGE = "productionLineScheduleStateChange";

    public static final String MODEL_SCHEDULE_POSITION = "schedulePosition";

    public static final String MODEL_PRODUCTION_LINE_SCHEDULE_POSITION = "productionLineSchedulePosition";

    public static final String MODEL_OPERATIONAL_TASK = "operationalTask";

    public static final String MODEL_OPERATIONAL_TASK_STATE_CHANGE = "operationalTaskStateChange";

    public static final String MODEL_OPERATIONAL_TASK_DTO = "operationalTaskDto";

    public static final String MODEL_OPERATIONAL_TASK_WITH_COLOR_DTO = "operationalTaskWithColorDto";

    public static final String MODEL_ORDER_PACK = "orderPack";

    public static final String MODEL_ORDER_PACK_STATE_CHANGE = "orderPackStateChange";

    public static final String MODEL_ORDER_TECHNOLOGICAL_PROCESS = "orderTechnologicalProcess";

    public static final String MODEL_ORDER_TECHNOLOGICAL_PROCESS_DTO = "orderTechnologicalProcessDto";

    public static final String MODEL_ORDER_TECHNOLOGICAL_PROCESS_PART = "orderTechnologicalProcessPart";

    public static final String MODEL_ORDER_ATTACHMENT = "orderAttachment";

    public static final String MODEL_WORKSTATION_CHANGEOVER_FOR_OPERATIONAL_TASK = "workstationChangeoverForOperationalTask";

    public static final String MODEL_WORKSTATION_CHANGEOVER_FOR_SCHEDULE_POSITION = "workstationChangeoverForSchedulePosition";

    public static final String MODEL_WORKSTATION_CHANGEOVER_FOR_OPERATIONAL_TASK_DTO = "workstationChangeoverForOperationalTaskDto";

    public static final String MODEL_SPLIT_ORDER_HELPER = "splitOrderHelper";

    public static final String MODEL_SPLIT_ORDER_PARENT = "splitOrderParent";

    public static final String MODEL_SPLIT_ORDER_CHILD = "splitOrderChild";

    // VIEW

    public static final String VIEW_ORDER_TECHNOLOGICAL_PROCESSES_ANALYSIS = "orderTechnologicalProcessesAnalysis";

    public static String orderDetailsUrl(final Long id) {
        return "#page/" + PLUGIN_IDENTIFIER + "/" + MODEL_ORDER + "Details.html?context=%7B%22form.id%22%3A%22" + id
                + "%22%2C%22form.undefined%22%3Anull%7D";
    }

}
