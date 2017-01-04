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

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.GlobalTypeOfMaterial;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

@Component
class GlobalTypeOfMaterialCellParser implements CellParser {

    private final TranslationService translationService;

    @Autowired
    GlobalTypeOfMaterialCellParser(TranslationService translationService) {
        this.translationService = translationService;
    }

    @Override
    public void parse(String cellValue, BindingErrorsAccessor errorsAccessor, Consumer<Object> valueConsumer) {
        if (!StringUtils.isBlank(cellValue)) {
            Optional<GlobalTypeOfMaterial> match = Arrays.stream(GlobalTypeOfMaterial.values())
                    .filter(gtom ->
                            translationService.translate(
                                    "basic.product.globalTypeOfMaterial.value." + gtom.getStringValue(),
                                    LocaleContextHolder.getLocale()).equals(cellValue))
                    .findAny();
            if (match.isPresent()) {
                valueConsumer.accept(match.get().getStringValue());
            } else {
                errorsAccessor.addError("qcadooView.validate.field.error.invalidDictionaryItem");
            }
        }
    }
}
