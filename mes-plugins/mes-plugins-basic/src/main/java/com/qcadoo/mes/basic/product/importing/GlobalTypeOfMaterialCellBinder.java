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
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class GlobalTypeOfMaterialCellBinder implements CellBinder {

    private final TranslationService translationService;

    @Autowired
    public GlobalTypeOfMaterialCellBinder(TranslationService translationService) {
        this.translationService = translationService;
    }

    @Override
    public void bind(Cell cell, Entity entity, BindingErrorsAccessor errorsAccessor) {
        if (null != cell) {
            String value = formatCell(cell);
            if (!StringUtils.isBlank(value)) {
                Optional<GlobalTypeOfMaterial> match = Arrays.stream(GlobalTypeOfMaterial.values())
                        .filter(gtom ->
                                translationService.translate(
                                        "basic.product.globalTypeOfMaterial.value." + gtom.getStringValue(),
                                        LocaleContextHolder.getLocale()).equals(value))
                        .findAny();
                if (match.isPresent()) {
                    entity.setField(getFieldName(), match.get().getStringValue());
                } else {
                    errorsAccessor.addError("invalid");
                }
            }
        }
    }

    @Override
    public String getFieldName() {
        return ProductFields.GLOBAL_TYPE_OF_MATERIAL;
    }
}
