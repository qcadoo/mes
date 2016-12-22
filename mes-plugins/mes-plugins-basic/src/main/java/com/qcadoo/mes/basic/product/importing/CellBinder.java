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

import com.qcadoo.model.api.Entity;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

class CellBinder {

    private final String fieldName;
    private final boolean required;


    CellBinder(String fieldName, boolean required) {
        this.fieldName = fieldName;
        this.required = required;
    }

    void bind(Cell cell, Entity entity) {
        // TODO wymagany: numer, nazwa i jednostka. Jesli dataDefinition to zapewnia to usunac required
        if (required && cell == null) {
            // VALUE MISSING ERROR
        } else if (cell != null) {
            entity.setField(fieldName, ConvertersHolder.convert(cell));
        }
    }

    private static class ConvertersHolder {

//        private static final Map<Integer, Function<Cell, String>> converters;


//        static {
//            converters = new HashMap<>();
//            converters.put(CellType.STRING.code, Cell::getStringCellValue);
//            converters.put(CellType.NUMERIC.code, c -> String.valueOf(c.getNumericCellValue()));
//        }

        static String convert(Cell cell) {
            Locale locale = LocaleContextHolder.getLocale();
            final DataFormatter dataFormatter = new DataFormatter(null == locale ? Locale.getDefault() : locale);
            return dataFormatter.formatCellValue(cell);
//                return converters.get(cell.getCellType()).apply(cell);
        }
    }

}
