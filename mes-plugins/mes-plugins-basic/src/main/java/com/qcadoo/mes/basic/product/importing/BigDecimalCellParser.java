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

import java.math.BigDecimal;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.i18n.LocaleContextHolder;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.model.api.BigDecimalUtils;

class BigDecimalCellParser implements CellParser {

    public static final String ERROR_CODE_INVALID_NUMERIC_FORMAT = "qcadooView.validate.field.error.invalidNumericFormat";

    private static final Pattern POLISH_DECIMAL_PATTERN = Pattern.compile("^-?\\d+(,\\d+)?$");

    private static final Pattern ENGLISH_AND_GERMAN_DECIMAL_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");

    private static final Locale POLISH_LOCALE = new Locale("pl");

    @Override
    public void parse(String cellValue, BindingErrorsAccessor errorsAccessor, Consumer<Object> valueConsumer) {
        Locale locale = LocaleContextHolder.getLocale();

        if (validateDecimalFormat(cellValue, locale, errorsAccessor)) {
            Either<Exception, Optional<BigDecimal>> either = BigDecimalUtils.tryParse(cellValue, locale);

            if (either.isLeft()) { // This is very unlikely to happen as decimal string was already checked
                errorsAccessor.addError(ERROR_CODE_INVALID_NUMERIC_FORMAT);
            } else if (either.getRight().isPresent()) {
                valueConsumer.accept(either.getRight().get());
            }
        }
    }

    private boolean validateDecimalFormat(String decimalString, Locale locale, BindingErrorsAccessor errorsAccessor) {
        final Pattern decimalPattern;
        String language = locale.getLanguage();

        if (POLISH_LOCALE.getLanguage().equals(language)) {
            decimalPattern = POLISH_DECIMAL_PATTERN;
        } else if (Locale.ENGLISH.getLanguage().equals(language) || Locale.GERMAN.getLanguage().equals(language)) {
            decimalPattern = ENGLISH_AND_GERMAN_DECIMAL_PATTERN;
        } else {
            throw new IllegalStateException("Encountered unsupported language: " + language);
        }

        Matcher matcher = decimalPattern.matcher(decimalString);

        if (!matcher.matches()) {
            errorsAccessor.addError(ERROR_CODE_INVALID_NUMERIC_FORMAT);

            return false;
        }

        return true;
    }

}
