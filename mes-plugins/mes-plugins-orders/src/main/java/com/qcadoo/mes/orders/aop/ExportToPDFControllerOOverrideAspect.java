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
package com.qcadoo.mes.orders.aop;

import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(OrdersConstants.PLUGIN_IDENTIFIER)
public class ExportToPDFControllerOOverrideAspect {

    @Autowired
    private ExportToPDFControllerOOverrideUtil exportToPDFControllerOOverrideUtil;

    @Pointcut("execution(private void com.qcadoo.plugins.qcadooExport.internal.ExportToPDFController.addPdfTableCells(..)) "
            + "&& args(pdfTable, rows, columns, viewName)")
    public void addPdfTableCells(final PdfPTable pdfTable, final List<Map<String, String>> rows, final List<String> columns, final String viewName) {

    }

    @Around("addPdfTableCells(pdfTable, rows, columns, viewName)")
    public void aroundAddPdfTableCells(final ProceedingJoinPoint pjp, final PdfPTable pdfTable, final List<Map<String, String>> rows, final List<String> columns, final String viewName)
            throws Throwable {
        if (exportToPDFControllerOOverrideUtil.shouldOverride(viewName)) {
            exportToPDFControllerOOverrideUtil.addPdfTableCells(pdfTable, rows, columns, viewName);
        } else {
            pjp.proceed();
        }
    }

}
