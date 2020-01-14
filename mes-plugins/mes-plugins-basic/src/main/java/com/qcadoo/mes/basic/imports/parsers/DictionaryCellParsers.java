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

import com.qcadoo.mes.basic.imports.helpers.CellErrorsAccessor;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.constants.DictionaryFields;
import com.qcadoo.model.constants.DictionaryItemFields;
import com.qcadoo.model.constants.QcadooModelConstants;

import java.util.Objects;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static com.qcadoo.model.api.search.SearchRestrictions.and;
import static com.qcadoo.model.api.search.SearchRestrictions.belongsTo;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;

@Component
public class DictionaryCellParsers {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public CellParser productCategory() {
        return new DictionaryParser("categories");
    }

    public CellParser units() {
        return new DictionaryParser("units");
    }

    public CellParser typeOfPallet() {
        return new DictionaryParser("typeOfPallet");
    }

    private class DictionaryParser implements CellParser {

        private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_INVALID_DICTIONARY_ITEM = "qcadooView.validate.field.error.invalidDictionaryItem";

        private static final String L_BASIC_IMPORT_ERROR_FIELD_INACTIVE_DICTIONARY_ITEM = "basic.import.error.field.inactiveDictionaryItem";

        private final String dictionaryName;

        private DictionaryParser(final String dictionaryName) {
            this.dictionaryName = dictionaryName;
        }

        @Override
        public void parse(final String cellValue, final String dependentCellValue, final CellErrorsAccessor errorsAccessor, final Consumer<Object> valueConsumer) {
            Entity dictionaryItem = getDictionaryItemByName(cellValue);

            if (Objects.isNull(dictionaryItem)) {
                errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_INVALID_DICTIONARY_ITEM);
            } else if (!dictionaryItem.isActive()) {
                errorsAccessor.addError(L_BASIC_IMPORT_ERROR_FIELD_INACTIVE_DICTIONARY_ITEM);
            } else {
                valueConsumer.accept(cellValue);
            }
        }

        private Entity getDictionaryItemByName(final String name) {
            return getDictionaryItemDD().find().add(
                    and(eq(DictionaryItemFields.NAME, name), belongsTo(DictionaryItemFields.DICTIONARY, getDictionaryByName())))
                    .setMaxResults(1).uniqueResult();
        }

        private Entity getDictionaryByName() {
            return getDictionaryDD().find().add(eq(DictionaryFields.NAME, dictionaryName)).setMaxResults(1).uniqueResult();
        }

        private DataDefinition getDictionaryDD() {
            return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY);
        }

        private DataDefinition getDictionaryItemDD() {
            return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY_ITEM);
        }

    }

}
