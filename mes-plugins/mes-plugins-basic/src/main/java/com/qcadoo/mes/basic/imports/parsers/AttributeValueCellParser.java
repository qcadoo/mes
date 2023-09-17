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

import com.qcadoo.mes.basic.constants.AttributeDataType;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.AttributeValueFields;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.imports.helpers.CellErrorsAccessor;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Consumer;

@Component
public class AttributeValueCellParser implements CellParser {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_LOOKUP_CODE_NOT_FOUND = "qcadooView.validate.field.error.lookupCodeNotFound";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public void parse(final String cellValue, final String dependentCellValue, final CellErrorsAccessor errorsAccessor, final Consumer<Object> valueConsumer) {
        Entity attribute = getAttributeByNumber(dependentCellValue);

        if (Objects.isNull(attribute)) {
            errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_LOOKUP_CODE_NOT_FOUND);

            return;
        }

        Entity attributeValue = getAttributeValueByValue(attribute, cellValue);

        if (Objects.isNull(attributeValue)) {
            errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_LOOKUP_CODE_NOT_FOUND);
        } else {
            valueConsumer.accept(attributeValue);
        }
    }

    private Entity getAttributeByNumber(final String number) {
        return getAttributeDD().find().add(SearchRestrictions.eq(AttributeFields.NUMBER, number))
                .add(SearchRestrictions.eq(AttributeFields.DATA_TYPE, AttributeDataType.CALCULATED.getStringValue()))
                .add(SearchRestrictions.eq(AttributeFields.FOR_PRODUCT, true))
                .setMaxResults(1).uniqueResult();
    }

    private DataDefinition getAttributeDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_ATTRIBUTE);
    }

    private Entity getAttributeValueByValue(final Entity attribute, final String value) {
        return getAttributeValueDD().find().add(SearchRestrictions.belongsTo(AttributeValueFields.ATTRIBUTE, attribute))
                .add(SearchRestrictions.eq(AttributeValueFields.VALUE, value))
                .setMaxResults(1).uniqueResult();
    }

    private DataDefinition getAttributeValueDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_ATTRIBUTE_VALUE);
    }

}
