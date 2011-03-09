/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.model.internal.types;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.localization.TranslationService;
import com.qcadoo.model.api.types.EnumeratedType;

public final class EnumType implements EnumeratedType {

    private final List<String> keys;

    private final TranslationService translationService;

    private final String translationPath;

    public EnumType(final TranslationService translationService, final String translationPath, final String... keys) {
        this.translationService = translationService;
        this.translationPath = translationPath;
        this.keys = Arrays.asList(keys);
    }

    @Override
    public Map<String, String> values(final Locale locale) {
        Map<String, String> values = new HashMap<String, String>();

        for (String key : keys) {
            values.put(key, translationService.translate(translationPath + ".value." + key, locale));
        }

        return values;
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object toObject(final FieldDefinition fieldDefinition, final Object value, final Entity entity) {
        String stringValue = String.valueOf(value);
        if (!keys.contains(stringValue)) {
            entity.addError(fieldDefinition, "core.validate.field.error.invalidDictionaryItem", String.valueOf(keys));
            return null;
        }
        return stringValue;
    }

    @Override
    public String toString(final Object value, final Locale locale) {
        return String.valueOf(value);
    }

    @Override
    public Object fromString(final String value, final Locale locale) {
        return value;
    }

}
