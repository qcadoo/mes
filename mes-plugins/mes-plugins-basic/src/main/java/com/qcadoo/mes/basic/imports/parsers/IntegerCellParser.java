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
import com.qcadoo.model.api.IntegerUtils;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class IntegerCellParser implements CellParser {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_INVALID_NUMERIC_FORMAT = "qcadooView.validate.field.error.invalidNumericFormat";

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM = "qcadooView.validate.field.error.custom";

    private static final String L_INTEGER_PATTERN = "^-?\\d+$";

    @Override
    public void parse(final String cellValue, final String dependentCellValue, final CellErrorsAccessor errorsAccessor, final Consumer<Object> valueConsumer) {
        if (validateIntegerFormat(cellValue, errorsAccessor)) {
            Optional<Integer> mayBeValue = Optional.ofNullable(IntegerUtils.parse(cellValue));

            if (mayBeValue.isPresent()) {
                Integer value = mayBeValue.get();

                valueConsumer.accept(value);
            } else {
                errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
            }
        }
    }

    private boolean validateIntegerFormat(final String cellValue, final CellErrorsAccessor errorsAccessor) {
        Pattern integerPattern = Pattern.compile(L_INTEGER_PATTERN);

        Matcher integerMatcher = integerPattern.matcher(cellValue);

        if (!integerMatcher.matches()) {
            errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_INVALID_NUMERIC_FORMAT);

            return false;
        }

        return true;
    }

}
