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

import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.ProxyEntity;

@Service
public final class WorkPlanForMachineXlsService extends XlsDocumentService {

    @Override
    protected void addHeader(final HSSFSheet sheet, final Locale locale) {
        HSSFRow header = sheet.createRow(0);
        header.createCell(0).setCellValue(getTranslationService().translate("products.operation.number.label", locale));
        header.createCell(1).setCellValue(getTranslationService().translate("products.operation.name.label", locale));
        header.createCell(2).setCellValue(
                getTranslationService().translate("products.workPlan.report.operationTable.productsOut.column", locale));
    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity entity) {
        int rowNum = 1;
        List<Entity> orders = entity.getHasManyField("orders");
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            Entity technology = (Entity) order.getField("technology");
            if (technology != null) {
                List<Entity> operationComponents = technology.getHasManyField("operationComponents");
                for (Entity operationComponent : operationComponents) {
                    Entity operation = (Entity) operationComponent.getField("operation");
                    HSSFRow row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(operation.getField("number").toString());
                    row.createCell(1).setCellValue(operation.getField("name").toString());
                    List<Entity> operationProductComponents = operationComponent.getHasManyField("operationProductComponents");
                    StringBuilder products = new StringBuilder();
                    for (Entity operationProductComponent : operationProductComponents) {
                        if (!(Boolean) operationProductComponent.getField("inParameter")) {
                            ProxyEntity product = (ProxyEntity) operationProductComponent.getField("product");
                            products.append(product.getField("number").toString() + " ");
                            products.append(product.getField("name").toString() + ", ");
                        }
                    }
                    row.createCell(2).setCellValue(products.toString());
                }
            }
        }
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("products.workPlan.report.title", locale);
    }

    @Override
    protected String getSuffix() {
        return "ForMachine";
    }
}
