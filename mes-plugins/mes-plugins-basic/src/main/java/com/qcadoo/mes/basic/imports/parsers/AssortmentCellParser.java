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
package com.qcadoo.mes.basic.imports.parsers;

import com.qcadoo.mes.basic.constants.AssortmentFields;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.imports.helpers.CellErrorsAccessor;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

import java.util.Objects;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AssortmentCellParser implements CellParser {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_LOOKUP_CODE_NOT_FOUND = "qcadooView.validate.field.error.lookupCodeNotFound";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public void parse(final String cellValue, final String dependentCellValue, final CellErrorsAccessor errorsAccessor, final Consumer<Object> valueConsumer) {
        Entity assortment = getAssortmentByName(cellValue);

        if (Objects.isNull(assortment)) {
            errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_LOOKUP_CODE_NOT_FOUND);
        } else {
            valueConsumer.accept(assortment);
        }
    }

    private Entity getAssortmentByName(final String name) {
        return getAssortmentDD().find().add(SearchRestrictions.eq(AssortmentFields.NAME, name)).setMaxResults(1)
                .uniqueResult();
    }

    private DataDefinition getAssortmentDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_ASSORTMENT);
    }

}
