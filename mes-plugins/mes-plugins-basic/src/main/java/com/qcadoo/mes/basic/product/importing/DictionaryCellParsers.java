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

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import static com.qcadoo.model.api.search.SearchRestrictions.*;

@Component
class DictionaryCellParsers {

    private final DataDefinitionService dataDefinitionService;

    @Autowired
    DictionaryCellParsers(DataDefinitionService dataDefinitionService) {
        this.dataDefinitionService = dataDefinitionService;
    }

    CellParser productCategory() {
        return new DictionaryParser("categories");
    }

    public CellParser units() {
        return new DictionaryParser("units");
    }

    private class DictionaryParser implements CellParser {

        private final String dictionaryName;

        private DictionaryParser(String dictionaryName) {
            this.dictionaryName = dictionaryName;
        }

        @Override
        public void parse(String cellValue, BindingErrorsAccessor errorsAccessor, Consumer<Object> valueConsumer) {
            Entity categoryItem = dataDefinitionService
                    .get("qcadooModel", "dictionaryItem")
                    .find()
                    .add(and(eq("name", cellValue), belongsTo("dictionary", getProductCategoryDictionary())))
                    .uniqueResult();
            if (null == categoryItem) {
                errorsAccessor.addError("qcadooView.validate.field.error.invalidDictionaryItem");
            } else if (!categoryItem.isActive()) {
                errorsAccessor.addError("basic.productsImport.error.field.inactiveDictionaryItem");
            } else {
                valueConsumer.accept(cellValue);
            }
        }

        private Entity getProductCategoryDictionary() {
            return dataDefinitionService
                    .get("qcadooModel", "dictionary")
                    .find()
                    .add(eq("name", dictionaryName))
                    .setMaxResults(1)
                    .uniqueResult();
        }

    }

}
