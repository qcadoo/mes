/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
package com.qcadoo.mes.workPlans.print.xls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.workPlans.print.WorkPlanReportDataService;
import com.qcadoo.mes.workPlans.util.EntityOperationNumberComparator;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.xls.XlsDocumentService;
import com.qcadoo.report.api.xls.XlsUtil;

@Service
public final class WorkPlanForProductXlsService extends XlsDocumentService {

    @Autowired
    private WorkPlanReportDataService workPlanReportDataService;

    @Override
    protected void addHeader(final HSSFSheet sheet, final Locale locale) {
        HSSFRow header = sheet.createRow(0);
        HSSFCell cell0 = header.createCell(0);
        cell0.setCellValue(getTranslationService().translate("workPlans.workPlan.report.operationTable.product.column", locale));
        cell0.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell1 = header.createCell(1);
        cell1.setCellValue(getTranslationService().translate("technologies.operation.number.label", locale));
        cell1.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell2 = header.createCell(2);
        cell2.setCellValue(getTranslationService().translate("technologies.operation.name.label", locale));
        cell2.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell3 = header.createCell(3);
        cell3.setCellValue(getTranslationService().translate("workPlans.workPlan.report.operationTable.productsOut.column",
                locale));
        cell3.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell4 = header.createCell(4);
        cell4.setCellValue(getTranslationService()
                .translate("workPlans.workPlan.report.operationTable.productsIn.column", locale));
        cell4.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));

    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity entity) {
        int rowNum = 1;
        List<Entity> orders = entity.getHasManyField("orders");
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            Entity technology = (Entity) order.getField("technology");
            if (technology != null) {
                List<Entity> operationComponents = new ArrayList<Entity>();
                workPlanReportDataService.addOperationsFromSubtechnologiesToList(technology.getTreeField("operationComponents"),
                        operationComponents);
                Collections.sort(operationComponents, new EntityOperationNumberComparator());

                for (Entity operationComponent : operationComponents) {
                    if ("operation".equals(operationComponent.getField("entityType"))) {
                        Entity operation = (Entity) operationComponent.getField("operation");
                        Entity product = (Entity) order.getField("product");
                        HSSFRow row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue((String) product.getField("name"));
                        row.createCell(1).setCellValue(operation.getField("number").toString());
                        row.createCell(2).setCellValue(operation.getField("name").toString());
                        List<Entity> operationProductInComponents = operationComponent
                                .getHasManyField("operationProductInComponents");
                        List<Entity> operationProductOutComponents = operationComponent
                                .getHasManyField("operationProductOutComponents");
                        StringBuilder productsOut = new StringBuilder();
                        StringBuilder productsIn = new StringBuilder();
                        for (Entity operationProductComponent : operationProductInComponents) {
                            Entity productIn = (Entity) operationProductComponent.getField("product");
                            productsIn.append(productIn.getField("number").toString() + " ");
                            productsIn.append(productIn.getField("name").toString() + ", ");
                        }
                        for (Entity operationProductComponent : operationProductOutComponents) {
                            Entity productOut = (Entity) operationProductComponent.getField("product");
                            productsOut.append(productOut.getField("number").toString() + " ");
                            productsOut.append(productOut.getField("name").toString() + ", ");
                        }
                        row.createCell(3).setCellValue(productsOut.toString());
                        row.createCell(4).setCellValue(productsIn.toString());
                    }
                }
            }
        }
        sheet.autoSizeColumn((short) 0);
        sheet.autoSizeColumn((short) 1);
        sheet.autoSizeColumn((short) 2);
        sheet.autoSizeColumn((short) 3);
        sheet.autoSizeColumn((short) 4);
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("workPlans.workPlan.report.title", locale);
    }

    @Override
    protected String getSuffix() {
        return "_for_product";
    }
}
