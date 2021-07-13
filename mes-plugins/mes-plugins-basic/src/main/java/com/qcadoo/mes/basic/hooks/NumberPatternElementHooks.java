package com.qcadoo.mes.basic.hooks;

import com.qcadoo.mes.basic.constants.NumberPatternElement;
import com.qcadoo.mes.basic.constants.NumberPatternElementFields;
import com.qcadoo.mes.basic.constants.NumberPatternFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class NumberPatternElementHooks {

    public void onSave(final DataDefinition dataDefinition, final Entity numberPatternElement) {
        String sequenceCycle = numberPatternElement.getStringField(NumberPatternElementFields.SEQUENCE_CYCLE);
        if (StringUtils.isNoneEmpty(sequenceCycle)) {
            Entity numberPattern = numberPatternElement.getBelongsToField(NumberPatternElementFields.NUMBER_PATTERN);
            List<Entity> elements = numberPattern.getHasManyField(NumberPatternFields.NUMBER_PATTERN_ELEMENTS);
            if (sequenceCycle.equals("01monthly")) {
                boolean hasMMElement = false;
                for (Entity element : elements) {
                    if (element.getStringField(NumberPatternElementFields.ELEMENT).equals("02mm")) {
                        hasMMElement = true;
                    }
                }
                if (!hasMMElement) {
                    numberPatternElement.addGlobalMessage("basic.numberPatternElement.sequenceCycle.monthlyInfo");
                }
            }

            if (sequenceCycle.equals("02annual")) {
                boolean hasRRElement = false;
                for (Entity element : elements) {
                    if (element.getStringField(NumberPatternElementFields.ELEMENT).equals("03rr")
                            || element.getStringField(NumberPatternElementFields.ELEMENT).equals("04rrrr")) {
                        hasRRElement = true;
                    }
                }
                if (!hasRRElement) {
                    numberPatternElement.addGlobalMessage("basic.numberPatternElement.sequenceCycle.annualInfo");
                }
            }
        }
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
                || NumberPatternElement.N99999.getStringValue().equals(entity.getStringField(NumberPatternElementFields.ELEMENT))) {
            Entity numberPattern = entity.getBelongsToField(NumberPatternElementFields.NUMBER_PATTERN);
            SearchCriteriaBuilder scb = numberPattern
                    .getHasManyField(NumberPatternFields.NUMBER_PATTERN_ELEMENTS)
                    .find()
                    .add(SearchRestrictions.or(
                            SearchRestrictions.or(
                                    SearchRestrictions.eq(NumberPatternElementFields.ELEMENT,
                                            NumberPatternElement.N999.getStringValue()),
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
            SearchCriteriaBuilder scb = numberPattern
                    .getHasManyField(NumberPatternFields.NUMBER_PATTERN_ELEMENTS)
                    .find()
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
