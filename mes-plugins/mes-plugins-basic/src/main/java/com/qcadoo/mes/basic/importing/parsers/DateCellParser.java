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
package com.qcadoo.mes.basic.importing.parsers;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.importing.helpers.CellErrorsAccessor;
import com.qcadoo.mes.basic.importing.helpers.CellParser;

@Component
public class DateCellParser implements CellParser {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_INVALID_DATE_FORMAT = "qcadooView.validate.field.error.invalidDateFormat";

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM = "qcadooView.validate.field.error.custom";

    private static final String L_DATE_PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";

    @Override
    public void parse(final String cellValue, final CellErrorsAccessor errorsAccessor, final Consumer<Object> valueConsumer) {
        if (validateDateFormat(cellValue, errorsAccessor)) {
            Either<? extends Exception, Optional<DateTime>> either = DateUtils.tryParse(cellValue);

            if (either.isLeft()) {
                errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
            } else {
                valueConsumer.accept(either.getRight().get().toDate());
            }
        }
    }

    private boolean validateDateFormat(final String cellValue, final CellErrorsAccessor errorsAccessor) {
        Pattern datePattern = Pattern.compile(L_DATE_PATTERN);

        Matcher dateMatcher = datePattern.matcher(cellValue);

        if (!dateMatcher.matches()) {
            errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_INVALID_DATE_FORMAT);

            return false;
        }

        return true;
    }

}
