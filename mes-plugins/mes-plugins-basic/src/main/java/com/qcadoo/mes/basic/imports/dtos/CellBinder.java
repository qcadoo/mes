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
package com.qcadoo.mes.basic.imports.dtos;

import com.qcadoo.mes.basic.imports.helpers.CellErrorsAccessor;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import com.qcadoo.mes.basic.imports.parsers.BooleanCellParser;
import com.qcadoo.model.api.Entity;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.springframework.context.i18n.LocaleContextHolder;

public abstract class CellBinder {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING = "qcadooView.validate.field.error.missing";

    private final String fieldName;

    private final String dependentFieldName;

    private final CellParser cellParser;

    public CellBinder(final String fieldName, final CellParser cellParser) {
        this.fieldName = fieldName;
        this.cellParser = cellParser;
        this.dependentFieldName = null;
    }

    public CellBinder(String fieldName, String dependentFieldName, CellParser cellParser) {
        this.fieldName = fieldName;
        this.dependentFieldName = dependentFieldName;
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

    public static CellBinder optional(final String fieldName, final String dependentFieldName, final CellParser cellParser) {
        return new OptionalCellBinder(fieldName, dependentFieldName, cellParser);
    }

    public String getDependentFieldName() {
        return dependentFieldName;
    }

    public abstract void bind(final Cell cell, final Entity entity, final CellErrorsAccessor errorsAccessor);

    public abstract void bind(final String cellValue, final Entity entity, final CellErrorsAccessor errorsAccessor);

    public abstract void bind(final Cell cellValue, final Cell dependentCellValue, final Entity entity, final CellErrorsAccessor errorsAccessor);

    private static String formatCell(final Cell cell) {
        Locale locale = LocaleContextHolder.getLocale();

        final DataFormatter dataFormatter = new DataFormatter(Objects.isNull(locale) ? Locale.getDefault() : locale);

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
        public void parse(final String cellValue, final String dependentCellValue, final CellErrorsAccessor errorsAccessor,
                final Consumer<Object> valueConsumer) {
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
            if (Objects.nonNull(cell)) {
                getCellParser().parse(formatCell(cell), null, errorsAccessor, o -> entity.setField(getFieldName(), o));
            }
        }

        @Override
        public void bind(String cellValue, Entity entity,
                CellErrorsAccessor errorsAccessor) {
            if (StringUtils.isEmpty(cellValue)) {
                errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
            } else {
                getCellParser()
                        .parse(formatCell(cellValue), null, errorsAccessor, fieldValue -> entity.setField(getFieldName(), fieldValue));
            }
        }

        @Override public void bind(Cell cellValue, Cell dependentCellValue, Entity entity, CellErrorsAccessor errorsAccessor) {
            if (Objects.nonNull(cellValue)) {
                getCellParser().parse(formatCell(cellValue), formatCell(dependentCellValue), errorsAccessor, o -> entity.setField(getFieldName(), o));
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

        OptionalCellBinder(final String fieldName, final String dependentFieldName, final CellParser cellParser) {
            super(fieldName, dependentFieldName, cellParser);
        }

        @Override
        public void bind(final Cell cell, final Entity entity, final CellErrorsAccessor errorsAccessor) {
            if (Objects.nonNull(cell)) {
                getCellParser().parse(formatCell(cell), null, errorsAccessor, o -> entity.setField(getFieldName(), o));
            }
        }

        @Override
        public void bind(String cellValue, Entity entity,
                CellErrorsAccessor errorsAccessor) {
            if (StringUtils.isNotEmpty(cellValue)) {
                getCellParser().parse(formatCell(cellValue), null,  errorsAccessor,
                        fieldValue -> entity.setField(getFieldName(), fieldValue));
            } else {
                if (getCellParser() instanceof BooleanCellParser) {
                    getCellParser().parse("false", "false", errorsAccessor, fieldValue -> entity.setField(getFieldName(), fieldValue));
                }
            }
        }

        @Override
        public void bind(Cell cellValue, Cell dependentCellValue, Entity entity, CellErrorsAccessor errorsAccessor) {
            if (Objects.nonNull(cellValue)) {
                getCellParser().parse(formatCell(cellValue), formatCell(dependentCellValue), errorsAccessor, o -> entity.setField(getFieldName(), o));
            }
        }
    }

}
