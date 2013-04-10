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
package com.qcadoo.mes.basicProductionCounting.hooks;

import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.PRODUCED_QUANTITY;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.USED_QUANTITY;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class BasicProductionCountingHooks {

    public boolean checkValueOfQuantity(final DataDefinition basicProductionCountingDD, final Entity basciProductionCounting) {
        BigDecimal usedQuantity = (BigDecimal) basciProductionCounting.getField(USED_QUANTITY);
        BigDecimal producedQuantity = (BigDecimal) basciProductionCounting.getField(PRODUCED_QUANTITY);

        if (usedQuantity == null && producedQuantity == null) {
            return true;
        }

        if (usedQuantity != null && usedQuantity.compareTo(BigDecimal.ZERO) == -1) {
            basciProductionCounting.addError(basicProductionCountingDD.getField(USED_QUANTITY),
                    "basic.production.counting.value.lower.zero");
        }

        if (producedQuantity != null && producedQuantity.compareTo(BigDecimal.ZERO) == -1) {
            basciProductionCounting.addError(basicProductionCountingDD.getField(PRODUCED_QUANTITY),
                    "basic.production.counting.value.lower.zero");
        }

        if (!basciProductionCounting.getGlobalErrors().isEmpty() || !basciProductionCounting.getErrors().isEmpty()) {
            return false;
        }

        return true;
    }

}
