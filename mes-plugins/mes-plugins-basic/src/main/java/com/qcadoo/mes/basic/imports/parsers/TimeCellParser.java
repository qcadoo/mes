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

import com.google.common.base.Optional;
import com.qcadoo.mes.basic.imports.helpers.CellErrorsAccessor;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TimeCellParser implements CellParser {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_WRONG_TYPE = "qcadooView.validate.field.error.wrongType";

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM = "qcadooView.validate.field.error.custom";

    private static final String L_TIME_PATTERN = "^\\d{1,2}:\\d{2}:\\d{2}$";

    @Override
    public void parse(final String cellValue, final String dependentCellValue, final CellErrorsAccessor errorsAccessor, final Consumer<Object> valueConsumer) {
        if (validateTimeFormat(cellValue, errorsAccessor)) {
            Optional<Integer> mayBeValue = parseTime(cellValue);

            if (mayBeValue.isPresent()) {
                Integer value = mayBeValue.get();

                valueConsumer.accept(value);
            } else {
                errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
            }
        }
    }

    private Optional<Integer> parseTime(final String cellValue) {
        String[] parts = cellValue.split(":");

        if (parts.length == 3) {
            return Optional.of((Integer.valueOf(parts[0]) * 3600) + (Integer.valueOf(parts[1]) * 60) + Integer.valueOf(parts[2]));
        }

        return Optional.absent();
    }

    private boolean validateTimeFormat(final String cellValue, final CellErrorsAccessor errorsAccessor) {
        Pattern timePattern = Pattern.compile(L_TIME_PATTERN);

        Matcher timeMatcher = timePattern.matcher(cellValue);

        if (!timeMatcher.matches()) {
            errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_WRONG_TYPE);

            return false;
        }

        return true;
    }

}
