package com.qcadoo.mes.core.data.internal.validators;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.internal.search.restrictions.RestrictionOperator;
import com.qcadoo.mes.core.data.search.Restrictions;
import com.qcadoo.mes.core.data.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.data.search.SearchResult;
import com.qcadoo.mes.core.data.validation.FieldValidator;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class UniqueValidator implements FieldValidator {

    private static final String UNIQUE_ERROR = "commons.validate.field.error.duplicated";

    private final DataAccessService dataAccessService;

    private String errorMessage = UNIQUE_ERROR;

    public UniqueValidator(final DataAccessService dataAccessService) {
        this.dataAccessService = dataAccessService;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        return true;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Entity entity,
            final ValidationResults validationResults) {
        SearchCriteriaBuilder searchCriteriaBuilder = SearchCriteriaBuilder.forEntity(dataDefinition)
                .restrictedWith(Restrictions.eq(fieldDefinition, entity.getField(fieldDefinition.getName()))).withMaxResults(1);
        if (entity.getId() != null) {
            searchCriteriaBuilder.restrictedWith(Restrictions.idRestriction(entity.getId(), RestrictionOperator.NE));
        }
        SearchResult results = dataAccessService.find(searchCriteriaBuilder.build());
        if (results.getTotalNumberOfEntities() == 0) {
            return true;
        } else {
            validationResults.addError(fieldDefinition, errorMessage);
            return false;
        }
    }

    @Override
    public FieldValidator customErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

}
