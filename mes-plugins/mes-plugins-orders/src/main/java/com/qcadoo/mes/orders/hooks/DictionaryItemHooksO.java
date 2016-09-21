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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.constants.DictionaryFields;
import com.qcadoo.model.constants.DictionaryItemFields;
import com.qcadoo.plugins.dictionaries.DictionariesService;

@Service
public class DictionaryItemHooksO {

    private static final String L_COLOR = "color";

    private static final String L_HEX_COLOR_PATTERN = "^#(?:[0-9a-fA-F]{3}){1,2}$";

    @Autowired
    private DictionariesService dictionariesService;

    public boolean validatesWith(final DataDefinition dictionaryItemDD, final Entity dictionaryItem) {
        boolean isValid = true;

        isValid = isValid && checkIfDescriptionIsHexColor(dictionaryItemDD, dictionaryItem);

        return isValid;
    }

    private boolean checkIfDescriptionIsHexColor(final DataDefinition dictionaryItemDD, final Entity dictionaryItem) {
        Entity dictionary = dictionaryItem.getBelongsToField(DictionaryItemFields.DICTIONARY);

        if (checkIfDictionaryIsColorDictionary(dictionary)) {
            String description = dictionaryItem.getStringField(DictionaryItemFields.DESCRIPTION);

            if (StringUtils.isNotEmpty(description)) {
                if (description.contains("#") && !checkIfIsHexColor(description)) {
                    dictionaryItem.addError(dictionaryItemDD.getField(DictionaryItemFields.DESCRIPTION), "qcadooModel.dictionaryItem.description.error.hexColorIsIncorrect");

                    return false;
                }
            }
        }

        return true;
    }

    private boolean checkIfDictionaryIsColorDictionary(final Entity dictionary) {
        return ((dictionary != null) && dictionary.getId().equals(getColorDictionary().getId()));
    }

    private boolean checkIfIsHexColor(final String description) {
         Pattern pattern = Pattern.compile(L_HEX_COLOR_PATTERN);
         Matcher matcher = pattern.matcher(description);

        return matcher.matches();
    }

    private Entity getColorDictionary() {
        return dictionariesService.getDictionaryDD().find().add(SearchRestrictions.eq(DictionaryFields.NAME, L_COLOR))
                .setMaxResults(1).uniqueResult();
    }

}
