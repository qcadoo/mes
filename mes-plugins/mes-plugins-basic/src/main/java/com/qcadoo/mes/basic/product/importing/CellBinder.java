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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.function.Consumer;

abstract class CellBinder {
    private final String fieldName;
    private final CellParser cellParser;
    CellBinder(String fieldName, CellParser cellParser) {
        this.fieldName = fieldName;
        this.cellParser = cellParser;
    }

    public static CellBinder required(String fieldName) {
        return new RequiredCellBinder(fieldName);
    }

    public static CellBinder required(String fieldName, CellParser cellParser) {
        return new RequiredCellBinder(fieldName, cellParser);
    }

    private static String formatCell(final Cell cell) {
        Locale locale = LocaleContextHolder.getLocale();
        final DataFormatter dataFormatter = new DataFormatter(null == locale ? Locale.getDefault() : locale);
        return dataFormatter.formatCellValue(cell).trim();
    }

    public static CellBinder optional(String fieldName, CellParser cellParser) {
        return new OptionalCellBinder(fieldName, cellParser);
    }

    public static CellBinder optional(String fieldName) {
        return new OptionalCellBinder(fieldName);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    CellParser getCellParser() {
        return cellParser;
    }

    abstract void bind(Cell cell, Entity entity, BindingErrorsAccessor errorsAccessor);

    public String getFieldName() {
        return fieldName;
    }

    private static class DefaultCellParser implements CellParser {

        @Override
        public void parse(String cellValue, BindingErrorsAccessor errorsAccessor, Consumer<Object> valueConsumer) {
            valueConsumer.accept(cellValue);
        }
    }

    private static class OptionalCellBinder extends CellBinder {

        OptionalCellBinder(String fieldName, CellParser cellParser) {
            super(fieldName, cellParser);
        }

        OptionalCellBinder(String fieldName) {
            this(fieldName, new DefaultCellParser());
        }

        @Override
        public void bind(Cell cell, Entity entity, BindingErrorsAccessor errorsAccessor) {
            if (cell != null) {
                getCellParser().parse(formatCell(cell), errorsAccessor, o -> entity.setField(getFieldName(), o));
            }
        }
    }

    private static class RequiredCellBinder extends CellBinder {

        RequiredCellBinder(String fieldName) {
            this(fieldName, new DefaultCellParser());
        }

        RequiredCellBinder(String fieldName, CellParser cellParser) {
            super(fieldName, cellParser);
        }

        @Override
        public void bind(Cell cell, Entity entity, BindingErrorsAccessor errorsAccessor) {
            if (cell == null) {
                errorsAccessor.addError("qcadooView.validate.field.error.missing");
            } else {
                getCellParser().parse(formatCell(cell), errorsAccessor, o -> entity.setField(getFieldName(), o));
            }
        }
    }
}
