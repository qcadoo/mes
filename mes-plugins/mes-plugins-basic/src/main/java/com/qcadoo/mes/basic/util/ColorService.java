package com.qcadoo.mes.basic.util;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.constants.DictionaryFields;
import com.qcadoo.model.constants.DictionaryItemFields;
import com.qcadoo.plugins.dictionaries.DictionariesService;

@Service
public class ColorService {

    private static final String L_COLOR = "color";

    private static final String L_HEX_COLOR_PATTERN = "^#(?:[0-9a-fA-F]{3}){1,2}$";

    @Autowired
    private DictionariesService dictionariesService;

    public boolean checkIfIsHexColor(final String description) {
        Pattern pattern = Pattern.compile(L_HEX_COLOR_PATTERN);
        Matcher matcher = pattern.matcher(description);

        return matcher.matches();
    }

    private Entity getColorDictionary() {
        return dictionariesService.getDictionaryDD().find().add(SearchRestrictions.eq(DictionaryFields.NAME, L_COLOR))
                .setMaxResults(1).uniqueResult();
    }

    public boolean checkIfDictionaryIsColorDictionary(final Entity dictionary) {
        return ((dictionary != null) && dictionary.getId().equals(getColorDictionary().getId()));
    }

    private Optional<Entity> getColorDictionaryItem(final String name) {
        return Optional.ofNullable(dictionariesService.getDictionaryItemDD().find()
                .add(SearchRestrictions.belongsTo(DictionaryItemFields.DICTIONARY, getColorDictionary()))
                .add(SearchRestrictions.eq(DictionaryItemFields.NAME, name)).setMaxResults(1).uniqueResult());
    }

    public Color getBackgroundColor(String color) {
        Color backgroundColor = Color.WHITE;

        Optional<Entity> mayBeColorDictionaryItem = getColorDictionaryItem(color);

        if (mayBeColorDictionaryItem.isPresent()) {
            Entity colorDictionaryItem = mayBeColorDictionaryItem.get();

            String description = colorDictionaryItem.getStringField(DictionaryItemFields.DESCRIPTION);

            if (StringUtils.isNotEmpty(description)) {
                if (description.contains("#") && checkIfIsHexColor(description)) {
                    backgroundColor = Color.decode(description);
                } else {
                    try {
                        Field field = Color.class.getField(description);

                        backgroundColor = (Color) field.get(null);
                    } catch (Exception e) {
                        backgroundColor = Color.WHITE;
                    }
                }
            }
        }

        return backgroundColor;
    }

}
