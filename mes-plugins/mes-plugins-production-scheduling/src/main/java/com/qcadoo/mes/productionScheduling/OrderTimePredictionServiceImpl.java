/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.productionScheduling;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.timeNormsForOperations.constants.TechOperCompTimeCalculationsFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.Entity;

@Service
public class OrderTimePredictionServiceImpl implements OrderTimePredictionService {

    public Date getDateFromOrdersFromOperation(final List<Entity> operations) {
        Date beforeOperation = null;

        for (Entity operation : operations) {
            Date operationDateFrom = operation.getBelongsToField(
                    TechnologyOperationComponentFieldsTNFO.TECH_OPER_COMP_TIME_CALCULATION).getDateField(
                    TechOperCompTimeCalculationsFields.EFFECTIVE_DATE_FROM);

            if (operationDateFrom != null) {
                if (beforeOperation == null) {
                    beforeOperation = operationDateFrom;
                }

                if (operationDateFrom.compareTo(beforeOperation) == -1) {
                    beforeOperation = operationDateFrom;
                }
            }
        }

        return beforeOperation;
    }

    public Date getDateToOrdersFromOperation(final List<Entity> operations) {
        Date laterOperation = null;

        for (Entity operation : operations) {
            Date operationDateTo = operation.getBelongsToField(
                    TechnologyOperationComponentFieldsTNFO.TECH_OPER_COMP_TIME_CALCULATION).getDateField(
                    TechOperCompTimeCalculationsFields.EFFECTIVE_DATE_TO);

            if (operationDateTo != null) {
                if (laterOperation == null) {
                    laterOperation = operationDateTo;
                }

                if (operationDateTo.compareTo(laterOperation) == 1) {
                    laterOperation = operationDateTo;
                }
            }
        }

        return laterOperation;
    }

}
