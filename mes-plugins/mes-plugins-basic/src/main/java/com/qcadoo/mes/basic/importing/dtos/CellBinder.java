/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.basic.importing.dtos;

import java.util.Locale;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.springframework.context.i18n.LocaleContextHolder;

import com.qcadoo.mes.basic.importing.helpers.CellErrorsAccessor;
import com.qcadoo.mes.basic.importing.helpers.CellParser;
import com.qcadoo.mes.basic.importing.parsers.BooleanCellParser;
import com.qcadoo.model.api.Entity;

public abstract class CellBinder {

    private final String fieldName;

    private final CellParser cellParser;

    public CellBinder(final String fieldName, final CellParser cellParser) {
        this.fieldName = fieldName;
        this.cellParser = cellParser;
    }

    public String getFieldName() {
        return fieldName;
    }

    public CellParser getCellParser() {
        return cellParser;
    }

    public static CellBinder required(final String fieldName) {
        return new RequiredCellBinder(fieldName);
    }

    public static CellBinder required(final String fieldName, final CellParser cellParser) {
        return new RequiredCellBinder(fieldName, cellParser);
    }

    public static CellBinder optional(final String fieldName) {
        return new OptionalCellBinder(fieldName);
    }

    public static CellBinder optional(final String fieldName, final CellParser cellParser) {
        return new OptionalCellBinder(fieldName, cellParser);
    }

    public abstract void bind(final Cell cell, final Entity entity, final CellErrorsAccessor errorsAccessor);

    public abstract void bind(final String cellValue, final Entity entity, final CellErrorsAccessor errorsAccessor);

    private static String formatCell(final Cell cell) {
        Locale locale = LocaleContextHolder.getLocale();

        final DataFormatter dataFormatter = new DataFormatter(null == locale ? Locale.getDefault() : locale);

        return dataFormatter.formatCellValue(cell).trim();
    }

    private static String formatCell(final String cellValue) {
        return cellValue.trim();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    private static class DefaultCellParser implements CellParser {

        @Override
        public void parse(final String cellValue, final CellErrorsAccessor errorsAccessor, final Consumer<Object> valueConsumer) {
            valueConsumer.accept(cellValue);
        }
    }

    private static class RequiredCellBinder extends CellBinder {

        RequiredCellBinder(final String fieldName) {
            this(fieldName, new DefaultCellParser());
        }

        RequiredCellBinder(final String fieldName, final CellParser cellParser) {
            super(fieldName, cellParser);
        }

        @Override
        public void bind(Cell cell, Entity entity, CellErrorsAccessor errorsAccessor) {
            if (cell == null) {
                errorsAccessor.addError("qcadooView.validate.field.error.missing");
            } else {
                getCellParser().parse(formatCell(cell), errorsAccessor, o -> entity.setField(getFieldName(), o));
            }
        }

        @Override
        public void bind(final String cellValue, final Entity entity, final CellErrorsAccessor errorsAccessor) {
            if (StringUtils.isEmpty(cellValue)) {
                errorsAccessor.addError("qcadooView.validate.field.error.missing");
            } else {
                getCellParser().parse(formatCell(cellValue), errorsAccessor,
                        fieldValue -> entity.setField(getFieldName(), fieldValue));
            }
        }
    }

    private static class OptionalCellBinder extends CellBinder {

        OptionalCellBinder(final String fieldName) {
            this(fieldName, new DefaultCellParser());
        }

        OptionalCellBinder(final String fieldName, final CellParser cellParser) {
            super(fieldName, cellParser);
        }

        @Override
        public void bind(final Cell cell, final Entity entity, final CellErrorsAccessor errorsAccessor) {
            if (cell != null) {
                getCellParser().parse(formatCell(cell), errorsAccessor, o -> entity.setField(getFieldName(), o));
            }
        }

        @Override
        public void bind(final String cellValue, final Entity entity, final CellErrorsAccessor errorsAccessor) {
            if (StringUtils.isNotEmpty(cellValue)) {
                getCellParser().parse(formatCell(cellValue), errorsAccessor,
                        fieldValue -> entity.setField(getFieldName(), fieldValue));
            } else {
                if (getCellParser() instanceof BooleanCellParser) {
                    getCellParser().parse("false", errorsAccessor, fieldValue -> entity.setField(getFieldName(), fieldValue));
                }
            }
        }
    }

}
