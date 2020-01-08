package com.qcadoo.mes.basic.hooks;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.NumberPatternElement;
import com.qcadoo.mes.basic.constants.NumberPatternElementFields;
import com.qcadoo.mes.basic.constants.NumberPatternFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NumberPatternElementHooks {

    @Autowired
    private TranslationService translationService;

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        Entity numberPattern = entity.getBelongsToField(NumberPatternElementFields.NUMBER_PATTERN);
        List<Entity> elements = numberPattern.getHasManyField(NumberPatternFields.NUMBER_PATTERN_ELEMENTS);
        StringBuilder stringBuilder = new StringBuilder();
        for (Entity element : elements) {
            if (NumberPatternElement.XX.getStringValue().equals(element.getStringField(NumberPatternElementFields.ELEMENT))) {
                stringBuilder.append(element.getStringField(NumberPatternElementFields.VALUE));
            } else {
                stringBuilder.append(translationService.translate(
                        "basic.numberPatternElement.element.value." + element.getStringField(NumberPatternElementFields.ELEMENT),
                        LocaleContextHolder.getLocale()));
            }
        }
        numberPattern.setField(NumberPatternFields.PATTERN, stringBuilder.toString());
        numberPattern.getDataDefinition().save(numberPattern);
    }

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity entity) {
        if (NumberPatternElement.XX.getStringValue().equals(entity.getStringField(NumberPatternElementFields.ELEMENT))
                && StringUtils.isBlank(entity.getStringField(NumberPatternElementFields.VALUE))) {
            entity.addError(dataDefinition.getField(NumberPatternElementFields.VALUE),
                    "basic.numberPatternElement.error.valueCannotBeEmpty");

            return false;
        }
        if (NumberPatternElement.N999.getStringValue().equals(entity.getStringField(NumberPatternElementFields.ELEMENT))
                || NumberPatternElement.N9999.getStringValue().equals(entity.getStringField(NumberPatternElementFields.ELEMENT))
                || NumberPatternElement.N99999.getStringValue()
                        .equals(entity.getStringField(NumberPatternElementFields.ELEMENT))) {
            Entity numberPattern = entity.getBelongsToField(NumberPatternElementFields.NUMBER_PATTERN);
            SearchCriteriaBuilder scb = numberPattern.getHasManyField(NumberPatternFields.NUMBER_PATTERN_ELEMENTS).find()
                    .add(SearchRestrictions.or(SearchRestrictions.or(
                            SearchRestrictions.eq(NumberPatternElementFields.ELEMENT, NumberPatternElement.N999.getStringValue()),
                            SearchRestrictions.eq(NumberPatternElementFields.ELEMENT,
                                    NumberPatternElement.N9999.getStringValue())),
                            SearchRestrictions.eq(NumberPatternElementFields.ELEMENT,
                                    NumberPatternElement.N99999.getStringValue())));
            if (entity.getId() != null) {
                scb.add(SearchRestrictions.idNe(entity.getId()));
            }
            if (scb.list().getTotalNumberOfEntities() > 0) {
                entity.addError(dataDefinition.getField(NumberPatternElementFields.ELEMENT),
                        "basic.numberPatternElement.error.thereCanBeOnlyOneSuchElement");
                return false;
            }
        } else if (!NumberPatternElement.XX.getStringValue().equals(entity.getStringField(NumberPatternElementFields.ELEMENT))) {
            Entity numberPattern = entity.getBelongsToField(NumberPatternElementFields.NUMBER_PATTERN);
            SearchCriteriaBuilder scb = numberPattern.getHasManyField(NumberPatternFields.NUMBER_PATTERN_ELEMENTS).find()
                    .add(SearchRestrictions.eq(NumberPatternElementFields.ELEMENT,
                            entity.getStringField(NumberPatternElementFields.ELEMENT)));
            if (entity.getId() != null) {
                scb.add(SearchRestrictions.idNe(entity.getId()));
            }
            if (scb.list().getTotalNumberOfEntities() > 0) {
                entity.addError(dataDefinition.getField(NumberPatternElementFields.ELEMENT),
                        "basic.numberPatternElement.error.thereCanBeOnlyOneSuchElement");
                return false;
            }
        }

        return true;
    }

}
