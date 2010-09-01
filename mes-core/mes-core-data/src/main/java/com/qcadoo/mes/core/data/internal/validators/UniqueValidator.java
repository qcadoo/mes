package com.qcadoo.mes.core.data.internal.validators;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.search.Restrictions;
import com.qcadoo.mes.core.data.search.ResultSet;
import com.qcadoo.mes.core.data.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.data.validation.FieldValidator;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class UniqueValidator implements FieldValidator {

    private static final String UNIQUE_ERROR = "core.validation.error.duplicated";

    private final DataAccessService dataAccessService;

    private String errorMessage = UNIQUE_ERROR;

    public UniqueValidator(final DataAccessService dataAccessService) {
        this.dataAccessService = dataAccessService;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        ResultSet results = dataAccessService.find(
                dataDefinition.getEntityName(),
                SearchCriteriaBuilder.forEntity(dataDefinition.getEntityName()).withMaxResults(1)
                        .restrictedWith(Restrictions.eq(fieldDefinition.getName(), value)).build());
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
