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
package com.qcadoo.mes.basic.imports.parsers;

import com.qcadoo.mes.basic.imports.helpers.CellErrorsAccessor;
import com.qcadoo.mes.basic.imports.helpers.CellParser;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class BooleanCellParser implements CellParser {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM = "qcadooView.validate.field.error.custom";

    private static final String L_TRUE_BOOLEAN_PATTERN = "^(true|tak|t|1)$";

    private static final String L_FALSE_BOOLEAN_PATTERN = "^(false|nie|f|n|0)$";

    @Override
    public void parse(final String cellValue, final String dependentCellValue, final CellErrorsAccessor errorsAccessor, final Consumer<Object> valueConsumer) {
        if (validateBooleanFormat(cellValue, errorsAccessor)) {
            Optional<Boolean> mayBeValue = parse(cellValue);

            if (mayBeValue.isPresent()) {
                Boolean value = mayBeValue.get();

                valueConsumer.accept(value);
            } else {
                errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
            }
        }
    }

    private boolean validateBooleanFormat(final String cellValue, final CellErrorsAccessor errorsAccessor) {
        Pattern trueBooleanPattern = Pattern.compile(L_TRUE_BOOLEAN_PATTERN, Pattern.CASE_INSENSITIVE);
        Pattern falseBooleanPattern = Pattern.compile(L_FALSE_BOOLEAN_PATTERN, Pattern.CASE_INSENSITIVE);

        Matcher trueBooleanMatcher = trueBooleanPattern.matcher(cellValue);
        Matcher falseBooleanMatcher = falseBooleanPattern.matcher(cellValue);

        if (!trueBooleanMatcher.matches() && !falseBooleanMatcher.matches()) {
            errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);

            return false;
        }

        return true;
    }

    private Optional<Boolean> parse(final String cellValue) {
        Pattern trueBooleanPattern = Pattern.compile(L_TRUE_BOOLEAN_PATTERN, Pattern.CASE_INSENSITIVE);
        Pattern falseBooleanPattern = Pattern.compile(L_FALSE_BOOLEAN_PATTERN, Pattern.CASE_INSENSITIVE);

        Matcher trueBooleanMatcher = trueBooleanPattern.matcher(cellValue);
        Matcher falseBooleanMatcher = falseBooleanPattern.matcher(cellValue);

        if (trueBooleanMatcher.matches()) {
            return Optional.of(Boolean.TRUE);
        } else if (falseBooleanMatcher.matches()) {
            return Optional.of(Boolean.FALSE);
        } else {
            return Optional.empty();
        }
    }

}
