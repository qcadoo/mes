package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.mes.technologies.constants.DimensionControlAttributeFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchConjunction;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DimensionControlAttributeHooks {

    public boolean validatesWith(final DataDefinition dimensionControlAttributeDD, final Entity dimensionControlAttribute) {
        return checkIfAttributeAlreadyExists(dimensionControlAttributeDD, dimensionControlAttribute);
    }

    private boolean checkIfAttributeAlreadyExists(final DataDefinition dimensionControlAttributeDD, final Entity dimensionControlAttribute) {
        Entity parameter = dimensionControlAttribute.getBelongsToField(DimensionControlAttributeFields.PARAMETER);
        Entity attribute = dimensionControlAttribute.getBelongsToField(DimensionControlAttributeFields.ATTRIBUTE);

        SearchConjunction conjunction = SearchRestrictions.conjunction();

        conjunction.add(SearchRestrictions.belongsTo(DimensionControlAttributeFields.PARAMETER, parameter));
        conjunction.add(SearchRestrictions.belongsTo(DimensionControlAttributeFields.ATTRIBUTE, attribute));

        if (Objects.nonNull(dimensionControlAttribute.getId())) {
            conjunction.add(SearchRestrictions.ne("id", dimensionControlAttribute.getId()));
        }

        if (dimensionControlAttributeDD.count(conjunction) != 0) {
            dimensionControlAttribute.addError(dimensionControlAttributeDD.getField(DimensionControlAttributeFields.ATTRIBUTE),
                    "technologies.dimensionControlAttribute.error.alreadyExists");

            return false;
        }

        return true;
    }

}
