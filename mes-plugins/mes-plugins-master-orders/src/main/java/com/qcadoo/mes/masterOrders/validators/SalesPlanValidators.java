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
package com.qcadoo.mes.masterOrders.validators;

import com.qcadoo.mes.masterOrders.constants.SalesPlanFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class SalesPlanValidators {

    public boolean onValidate(final DataDefinition salesPlanDD, final Entity salesPlan) {
        return checkIfDatesAreOk(salesPlanDD, salesPlan);
    }

    public boolean checkIfDatesAreOk(final DataDefinition salesPlanDD, final Entity salesPlan) {
        Date dateFrom = salesPlan.getDateField(SalesPlanFields.DATE_FROM);
        Date dateTo = salesPlan.getDateField(SalesPlanFields.DATE_TO);

        if ((dateFrom != null) && (dateTo != null) && dateTo.before(dateFrom)) {
            salesPlan.addError(salesPlanDD.getField(SalesPlanFields.DATE_TO), "masterOrders.masterOrder.dateTo.isBeforeDateFrom");

            return false;
        }

        return true;
    }

}
