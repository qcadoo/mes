/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ShiftsServiceImpl;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OrderRealizationTimeServiceImpl implements OrderRealizationTimeService {

    private static final String OPERATION_NODE_ENTITY_TYPE = "operation";

    @Autowired
    private ShiftsServiceImpl shiftsService;

    @Override
    public void changeDateFrom(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent dateTo = (FieldComponent) state;
        FieldComponent dateFrom = (FieldComponent) viewDefinitionState.getComponentByReference("dateFrom");
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");
        if (StringUtils.hasText((String) dateTo.getFieldValue()) && !StringUtils.hasText((String) dateFrom.getFieldValue())) {
            Date date = shiftsService.findDateFromForOrder(getDateFromField(dateTo.getFieldValue()),
                    Integer.valueOf((String) realizationTime.getFieldValue()));
            if (date != null) {
                dateFrom.setFieldValue(setDateToField(date));
            }
        }
    }

    @Override
    public void changeDateTo(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent dateFrom = (FieldComponent) state;
        FieldComponent dateTo = (FieldComponent) viewDefinitionState.getComponentByReference("dateTo");
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");
        if (!StringUtils.hasText((String) dateTo.getFieldValue()) && StringUtils.hasText((String) dateFrom.getFieldValue())) {
            Date date = shiftsService.findDateToForOrder(getDateFromField(dateFrom.getFieldValue()),
                    Integer.valueOf((String) realizationTime.getFieldValue()));
            if (date != null) {
                dateTo.setFieldValue(setDateToField(date));
            }
        }
    }

    @Override
    public Object setDateToField(final Date date) {
        return new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT, Locale.getDefault()).format(date);
    }

    @Override
    public Date getDateFromField(final Object value) {
        try {
            return new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT, Locale.getDefault()).parse((String) value);
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public int estimateRealizationTimeForOperation(final EntityTreeNode operationComponent, final BigDecimal plannedQuantity) {
        return estimateRealizationTimeForOperation(operationComponent, plannedQuantity, true);
    }

    @Override
    @Transactional
    public int estimateRealizationTimeForOperation(final EntityTreeNode operationComponent, final BigDecimal plannedQuantity,
            final Boolean includeTpz) {
        if (operationComponent.getField("entityType") != null
                && !OPERATION_NODE_ENTITY_TYPE.equals(operationComponent.getField("entityType"))) {
            return estimateRealizationTimeForOperation(
                    operationComponent.getBelongsToField("referenceTechnology").getTreeField("operationComponents").getRoot(),
                    plannedQuantity);
        } else {
            int operationTime = 0;
            int pathTime = 0;

            for (EntityTreeNode child : operationComponent.getChildren()) {
                int tmpPathTime = estimateRealizationTimeForOperation(child, plannedQuantity);
                if (tmpPathTime > pathTime) {
                    pathTime = tmpPathTime;
                }
            }

            BigDecimal productionInOneCycle = (BigDecimal) operationComponent.getField("productionInOneCycle");
            BigDecimal roundUp = plannedQuantity.divide(productionInOneCycle, BigDecimal.ROUND_UP);

            if ("01all".equals(operationComponent.getField("countRealized"))
                    || operationComponent.getBelongsToField("parent") == null) {
                operationTime = (roundUp.multiply(BigDecimal.valueOf(getIntegerValue(operationComponent.getField("tj")))))
                        .intValue();
            } else {
                operationTime = ((operationComponent.getField("countMachine") == null ? BigDecimal.ZERO
                        : (BigDecimal) operationComponent.getField("countMachine")).multiply(BigDecimal
                        .valueOf(getIntegerValue(operationComponent.getField("tj"))))).intValue();
            }
            if (includeTpz) {
                operationTime += getIntegerValue(operationComponent.getField("tpz"));
            }
            if ("orderOperationComponent".equals(operationComponent.getDataDefinition().getName())) {
                operationComponent.setField("effectiveOperationRealizationTime", operationTime);
                operationComponent.setField("operationOffSet", pathTime);
                operationComponent.getDataDefinition().save(operationComponent);
            }

            pathTime += operationTime + getIntegerValue(operationComponent.getField("timeNextOperation"));
            return pathTime;
        }
    }

    private Integer getIntegerValue(final Object value) {
        return value == null ? Integer.valueOf(0) : (Integer) value;
    }

    @Override
    public BigDecimal getBigDecimalFromField(final Object value, final Locale locale) {
        try {
            DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(locale);
            format.setParseBigDecimal(true);
            return new BigDecimal(format.parse(value.toString()).doubleValue());
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
