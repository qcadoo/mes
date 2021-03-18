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
package com.qcadoo.mes.orders.hooks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.util.ColorService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.constants.DictionaryItemFields;

@Service
public class DictionaryItemHooksO {

    @Autowired
    private ColorService colorService;

    public boolean validatesWith(final DataDefinition dictionaryItemDD, final Entity dictionaryItem) {
        return checkIfDescriptionIsHexColor(dictionaryItemDD, dictionaryItem);
    }

    private boolean checkIfDescriptionIsHexColor(final DataDefinition dictionaryItemDD, final Entity dictionaryItem) {
        Entity dictionary = dictionaryItem.getBelongsToField(DictionaryItemFields.DICTIONARY);

        if (colorService.checkIfDictionaryIsColorDictionary(dictionary)) {
            String description = dictionaryItem.getStringField(DictionaryItemFields.DESCRIPTION);

            if (StringUtils.isNotEmpty(description) && description.contains("#")
                    && !colorService.checkIfIsHexColor(description)) {
                dictionaryItem.addError(dictionaryItemDD.getField(DictionaryItemFields.DESCRIPTION),
                        "qcadooModel.dictionaryItem.description.error.hexColorIsIncorrect");

                return false;
            }
        }

        return true;
    }

}
