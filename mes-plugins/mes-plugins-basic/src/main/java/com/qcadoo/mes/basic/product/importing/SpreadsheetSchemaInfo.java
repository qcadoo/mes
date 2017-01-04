/*
 * **************************************************************************
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
 * **************************************************************************
 */
package com.qcadoo.mes.basic.product.importing;

import com.qcadoo.mes.basic.constants.ProductFields;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperties;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpreadsheetSchemaInfo {
    static final int START_ROW_INDEX = 1;
    static final int COLUMN_NUMBER = 13;
    private static final String SCHEMA_VERSION = "1.0.0";
    private static final String SCHEMA_VERSION_PROPERTY_NAME = "SchemaVersion";
    private static Map<String, Integer> nameToIndexMap = new HashMap<>(SpreadsheetSchemaInfo.COLUMN_NUMBER);

    static {
        int position = 0;
        nameToIndexMap.put(ProductFields.NUMBER, position++);
        nameToIndexMap.put(ProductFields.NAME, position++);
        nameToIndexMap.put(ProductFields.GLOBAL_TYPE_OF_MATERIAL, position++);
        nameToIndexMap.put(ProductFields.UNIT, position++);
        nameToIndexMap.put(ProductFields.EAN, position++);
        nameToIndexMap.put(ProductFields.CATEGORY, position++);
        nameToIndexMap.put(ProductFields.DESCRIPTION, position++);
        nameToIndexMap.put(ProductFields.PRODUCER, position++);
        nameToIndexMap.put(ProductFields.ASSORTMENT, position++);
        nameToIndexMap.put(ProductFields.PARENT, position++);
        nameToIndexMap.put("nominalCost", position++);
        nameToIndexMap.put("lastPurchaseCost", position++);
        nameToIndexMap.put("averageCost", position);
    }

    private SpreadsheetSchemaInfo() {
        // empty by design
    }

    public static int getIndexUsingFieldName(String fieldName) {
        return nameToIndexMap.get(fieldName);
    }

    static void assureSpreadsheetMatchesCurrentSchemaVersion(XSSFWorkbook workbook) throws ImportException {
        POIXMLProperties properties = workbook.getProperties();
        POIXMLProperties.CustomProperties customProperties = properties.getCustomProperties();
        CTProperties underlyingProperties = customProperties.getUnderlyingProperties();
        List<CTProperty> propertyList = underlyingProperties.getPropertyList();
        for (CTProperty ctProperty : propertyList) {
            if (SCHEMA_VERSION_PROPERTY_NAME.equals(ctProperty.getName()) && SCHEMA_VERSION.equals(ctProperty.getLpwstr())) {
                return;
            }
        }
        throw new ImportException("SchemaVersion metadata is either missing or has incorrect value");
    }
}
