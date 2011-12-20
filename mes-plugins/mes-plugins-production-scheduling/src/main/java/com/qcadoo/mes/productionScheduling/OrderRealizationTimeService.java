/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;

import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public interface OrderRealizationTimeService {

    int MAX_REALIZATION_TIME = 99999 * 60 * 60;

    void changeDateFrom(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args);

    void changeDateTo(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args);

    BigDecimal getBigDecimalFromField(final Object value, final Locale locale);

    int estimateRealizationTimeForOperation(final EntityTreeNode operationComponent, final BigDecimal plannedQuantity);

    int estimateRealizationTimeForOperation(final EntityTreeNode operationComponent, final BigDecimal plannedQuantity,
            final Boolean includeTpz);

    Date getDateFromField(final Object value);

    Object setDateToField(final Date date);

}