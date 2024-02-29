package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.SectionFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SectionHooks {

    public boolean validatesWith(final DataDefinition sectionDD, final Entity section) {
        return checkSectionUnit(sectionDD, section);
    }

    private boolean checkSectionUnit(final DataDefinition sectionDD, final Entity section) {
        Entity operationProductInComponent = section.getBelongsToField(SectionFields.OPERATION_PRODUCT_IN_COMPONENT);
        String unit = section.getStringField(SectionFields.UNIT);

        if (Objects.nonNull(operationProductInComponent) && Objects.nonNull(unit)) {
            String givenUnit = operationProductInComponent.getStringField(OperationProductInComponentFields.GIVEN_UNIT);

            if (!unit.equals(givenUnit)) {
                section.addError(sectionDD.getField(SectionFields.UNIT), "technologies.section.unit.error.notSameAsGivenUnit");

                return false;
            }
        }

        return true;
    }

}
