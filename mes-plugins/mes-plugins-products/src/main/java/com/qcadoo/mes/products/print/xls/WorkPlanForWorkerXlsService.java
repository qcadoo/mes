/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.products.print.xls;

import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.Entity;

@Service
public final class WorkPlanForWorkerXlsService extends XlsDocumentService {

    @Override
    protected void addHeader(final HSSFSheet sheet, final Locale locale) {
        HSSFRow header = sheet.createRow(0);
        header.createCell(0).setCellValue(getTranslationService().translate("products.product.number.label", locale));
        header.createCell(1).setCellValue(getTranslationService().translate("products.product.name.label", locale));
        header.createCell(2).setCellValue(
                getTranslationService().translate("products.technologyOperationComponent.quantity.label", locale));
        header.createCell(3).setCellValue(getTranslationService().translate("products.product.unit.label", locale));
    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity entity) {

    }

    @Override
    protected String getFileName() {
        return "WorkPlanForWorker";
    }

    @Override
    protected String getEntityName() {
        return "workPlan";
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("products.workPlan.report.title", locale);
    }

    @Override
    protected String getFileNameWithoutSuffix() {
        return "WorkPlan";
    }
}
