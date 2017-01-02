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

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.model.api.BigDecimalUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.function.Consumer;

public class BigDecimalCellParser implements CellParser {

    @Override
    public void parse(String cellValue, BindingErrorsAccessor errorsAccessor, Consumer<Object> valueConsumer) {
        Locale locale = LocaleContextHolder.getLocale();
        // TODO add validation here to be more restrictive rather than incorrectly parse big decimal
        Either<Exception, Optional<BigDecimal>> either = BigDecimalUtils.tryParse(cellValue, locale);
        if (either.isLeft()) {
            errorsAccessor.addError("invalid");
        } else if (either.getRight().isPresent()) {
            valueConsumer.accept(either.getRight().get());
        }
    }
}
