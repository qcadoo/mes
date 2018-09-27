/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.supplyNegotiations.aop;

import java.util.List;
import java.util.Map;

import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;
import com.qcadoo.plugin.api.RunIfEnabled;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.model.api.Entity;

@Aspect
@Configurable
@RunIfEnabled(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER)
public class OrderReportPdfSNOverrideAspect {

    @Autowired
    private OrderReportPdfSNOverrideUtil orderReportPdfSNOverrideUtil;

    @Pointcut("execution(private java.util.Map<String, Object> com.qcadoo.mes.deliveries.print.OrderReportPdf.createSecondColumn(..)) "
            + " && args(delivery)")
    public void createSecondColumnExecution(final Entity delivery) {
    }

    @SuppressWarnings("unchecked")
    @Around("createSecondColumnExecution(delivery)")
    public Map<String, Object> afterCreateSecondColumnExecution(final ProceedingJoinPoint pjp, final Entity delivery)
            throws Throwable {
        final Map<String, Object> result = (Map<String, Object>) pjp.proceed();

        if (orderReportPdfSNOverrideUtil.shouldOverride()) {
            return orderReportPdfSNOverrideUtil.addFieldToSecondColumn(delivery, result);
        } else {
            return result;
        }
    }

    @Pointcut("execution(private java.util.List<com.qcadoo.model.api.Entity> com.qcadoo.mes.deliveries.print.OrderReportPdf.getOrderReportColumns(..)) "
            + " && args(columnsForOrders, orderedProducts, orderedProductsColumnValues)")
    public void getOrderReportColumnsExecution(final List<Entity> columnsForOrders, final List<Entity> orderedProducts,
            final Map<Entity, Map<String, String>> orderedProductsColumnValues) {
    }

    @SuppressWarnings("unchecked")
    @Around("getOrderReportColumnsExecution(columnsForOrders, orderedProducts, orderedProductsColumnValues)")
    public List<Entity> afterGetOrderReportColumnsExecution(final ProceedingJoinPoint pjp, final List<Entity> columnsForOrders,
            final List<Entity> orderedProducts, final Map<Entity, Map<String, String>> orderedProductsColumnValues)
            throws Throwable {
        final List<Entity> columnForOrders = (List<Entity>) pjp.proceed();

        if (orderReportPdfSNOverrideUtil.shouldOverride()) {
            return orderReportPdfSNOverrideUtil.getOrderReportColumnsExecution(columnForOrders, orderedProducts);
        } else {
            return columnForOrders;
        }
    }

}
