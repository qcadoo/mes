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
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.imports.helpers.CellErrorsAccessor;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import com.qcadoo.model.api.BigDecimalUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BigDecimalCellParser implements CellParser {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_INVALID_NUMERIC_FORMAT = "qcadooView.validate.field.error.invalidNumericFormat";

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM = "qcadooView.validate.field.error.custom";

    private static final String L_POLISH_DECIMAL_PATTERN = "^-?\\d+(,\\d+)?$";

    private static final String L_ENGLISH_DECIMAL_PATTERN = "^-?\\d+(\\.\\d+)?$";

    private static final String L_CHINESE_DECIMAL_PATTERN = "^-?\\d+(\\.\\d+)?$";

    @Override
    public void parse(final String cellValue, final String dependentCellValue, final CellErrorsAccessor errorsAccessor, final Consumer<Object> valueConsumer) {
        Locale locale = LocaleContextHolder.getLocale();

        if (validateDecimalFormat(cellValue, locale, errorsAccessor)) {
            Either<Exception, Optional<BigDecimal>> either = BigDecimalUtils.tryParse(cellValue, locale);

            if (either.isLeft()) {
                errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
            } else if (either.getRight().isPresent()) {
                valueConsumer.accept(either.getRight().get());
            }
        }
    }

    private boolean validateDecimalFormat(final String cellValue, final Locale locale, final CellErrorsAccessor errorsAccessor) {
        String language = locale.getLanguage();

        Locale polish = new Locale("pl");
        Locale chinese = new Locale("cn");

        Pattern decimalPattern;

        if (polish.getLanguage().equals(language)) {
            decimalPattern = Pattern.compile(L_POLISH_DECIMAL_PATTERN);
        } else if (Locale.ENGLISH.getLanguage().equals(language)) {
            decimalPattern = Pattern.compile(L_ENGLISH_DECIMAL_PATTERN);
        } else if (chinese.getLanguage().equals(language)) {
            decimalPattern = Pattern.compile(L_CHINESE_DECIMAL_PATTERN);
        } else {
            throw new IllegalStateException("Encountered unsupported language: " + language);
        }

        Matcher decimalMatcher = decimalPattern.matcher(cellValue);

        if (!decimalMatcher.matches()) {
            errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_INVALID_NUMERIC_FORMAT);

            return false;
        }

        return true;
    }

}
