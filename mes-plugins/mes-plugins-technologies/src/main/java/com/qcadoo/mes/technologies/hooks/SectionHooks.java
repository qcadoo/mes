package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.SectionFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class SectionHooks {

    public boolean validatesWith(final DataDefinition sectionDD, final Entity section) {
        return checkSectionUnit(sectionDD, section);
    }

    private boolean checkSectionUnit(final DataDefinition sectionDD, final Entity section) {
        List<Entity> sections = section.getBelongsToField(SectionFields.OPERATION_PRODUCT_IN_COMPONENT).getHasManyField(OperationProductInComponentFields.SECTIONS);
        if (!sections.isEmpty()) {
            String unit = section.getStringField(SectionFields.UNIT);

            if (!unit.equals(sections.get(0).getStringField(SectionFields.UNIT))) {
                section.addError(sectionDD.getField(SectionFields.UNIT), "technologies.section.unit.error.differentUnits");

                return false;
            }
        }
        return true;
    }

}
