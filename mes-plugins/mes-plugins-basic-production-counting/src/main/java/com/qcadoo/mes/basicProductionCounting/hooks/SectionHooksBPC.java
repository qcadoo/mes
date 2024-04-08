package com.qcadoo.mes.basicProductionCounting.hooks;

import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.SectionFieldsBPC;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SectionHooksBPC {

    public boolean validatesWith(final DataDefinition sectionDD, final Entity section) {
        return checkSectionUnit(sectionDD, section);
    }

    private boolean checkSectionUnit(final DataDefinition sectionDD, final Entity section) {
        List<Entity> sections = section.getBelongsToField(SectionFieldsBPC.PRODUCTION_COUNTING_QUANTITY).getHasManyField(ProductionCountingQuantityFields.SECTIONS);
        if (!sections.isEmpty()) {
            String unit = section.getStringField(SectionFieldsBPC.UNIT);
            Entity firstSection = sections.get(0);

            if (!unit.equals(firstSection.getStringField(SectionFieldsBPC.UNIT))
                    && !(firstSection.getId().equals(section.getId()) && sections.size() == 1)) {
                section.addError(sectionDD.getField(SectionFieldsBPC.UNIT), "technologies.section.unit.error.differentUnits");

                return false;
            }
        }
        return true;
    }

}
