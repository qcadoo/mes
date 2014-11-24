/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.workPlans.pdf.document.component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.GroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.order.component.OrderTable;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class OrderSection {

    private OrderTable orderTable;

    @Autowired
    public OrderSection(OrderTable orderTable) {
        this.orderTable = orderTable;
    }

    public void print(Entity workPlan, GroupingContainer groupingContainer, Document document, Locale locale) throws DocumentException {
        if(printingOrdersEnabled(workPlan))
            orderTable.print(groupingContainer, document, locale);
    }

    private boolean printingOrdersEnabled(Entity workPlan) {
        return !workPlan.getBooleanField(WorkPlanFields.DONT_PRINT_ORDERS_IN_WORK_PLANS);
    }
}
