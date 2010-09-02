package com.qcadoo.mes.core.data.search;

import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.internal.search.restrictions.BelongsToRestriction;
import com.qcadoo.mes.core.data.internal.search.restrictions.IsNotNullRestriction;
import com.qcadoo.mes.core.data.internal.search.restrictions.IsNullRestriction;
import com.qcadoo.mes.core.data.internal.search.restrictions.LikeRestriction;
import com.qcadoo.mes.core.data.internal.search.restrictions.RestrictionOperator;
import com.qcadoo.mes.core.data.internal.search.restrictions.SimpleRestriction;
import com.qcadoo.mes.core.data.validation.ValidationResults;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.search.Restriction
 */
public final class Restrictions {

    private Restrictions() {
    }

    public static Restriction eq(final FieldDefinition fieldDefinition, final Object expectedValue) {
        ValidationResults validationResults = new ValidationResults();
        Object value = validateValue(fieldDefinition, expectedValue, validationResults);
        if (!validationResults.getErrors().isEmpty()) {
            return null;
        }
        if (value instanceof String && ((String) value).contains("*") || ((String) value).contains("%")
                || ((String) value).contains("?") || ((String) value).contains("_")) {
            String preperadValue = ((String) value).replace('*', '%').replace('?', '_');
            return new LikeRestriction(fieldDefinition.getName(), preperadValue);
        }
        return new SimpleRestriction(fieldDefinition.getName(), value, RestrictionOperator.EQ);
    }

    public static Restriction belongsTo(final FieldDefinition fieldDefinition, final Long id) {
        return new BelongsToRestriction(fieldDefinition.getName(), id);
    }

    public static Restriction idRestriction(final Long id, final RestrictionOperator restrictionOperator) {
        return new SimpleRestriction("id", id, restrictionOperator);
    }

    public static Restriction ge(final FieldDefinition fieldDefinition, final Object expectedValue) {
        ValidationResults validationResults = new ValidationResults();
        Object value = validateValue(fieldDefinition, expectedValue, validationResults);
        if (!validationResults.getErrors().isEmpty()) {
            return null;
        }
        return new SimpleRestriction(fieldDefinition.getName(), value, RestrictionOperator.GE);
    }

    public static Restriction gt(final FieldDefinition fieldDefinition, final Object expectedValue) {
        ValidationResults validationResults = new ValidationResults();
        Object value = validateValue(fieldDefinition, expectedValue, validationResults);
        if (!validationResults.getErrors().isEmpty()) {
            return null;
        }
        return new SimpleRestriction(fieldDefinition.getName(), value, RestrictionOperator.GT);
    }

    public static Restriction isNotNull(final FieldDefinition fieldDefinition) {
        return new IsNotNullRestriction(fieldDefinition.getName(), RestrictionOperator.NOTNULL);
    }

    public static Restriction isNull(final FieldDefinition fieldDefinition) {
        return new IsNullRestriction(fieldDefinition.getName(), RestrictionOperator.NULL);
    }

    public static Restriction le(final FieldDefinition fieldDefinition, final Object expectedValue) {
        ValidationResults validationResults = new ValidationResults();
        Object value = validateValue(fieldDefinition, expectedValue, validationResults);
        if (!validationResults.getErrors().isEmpty()) {
            return null;
        }
        return new SimpleRestriction(fieldDefinition.getName(), value, RestrictionOperator.LE);
    }

    public static Restriction lt(final FieldDefinition fieldDefinition, final Object expectedValue) {
        ValidationResults validationResults = new ValidationResults();
        Object value = validateValue(fieldDefinition, expectedValue, validationResults);
        if (!validationResults.getErrors().isEmpty()) {
            return null;
        }
        return new SimpleRestriction(fieldDefinition.getName(), value, RestrictionOperator.LT);
    }

    public static Restriction ne(final FieldDefinition fieldDefinition, final Object expectedValue) {
        ValidationResults validationResults = new ValidationResults();
        Object value = validateValue(fieldDefinition, expectedValue, validationResults);
        if (!validationResults.getErrors().isEmpty()) {
            return null;
        }
        return new SimpleRestriction(fieldDefinition.getName(), value, RestrictionOperator.NE);
    }

    private static Object validateValue(final FieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        Object fieldValue = value;
        if (fieldValue != null) {
            if (!fieldDefinition.getType().getType().isInstance(fieldValue)) {
                if (fieldValue instanceof String) {
                    fieldValue = fieldDefinition.getType().fromString(fieldDefinition, (String) fieldValue, validationResults);
                } else {
                    validationResults.addError(fieldDefinition, "core.validation.error.wrongType", fieldValue.getClass()
                            .getSimpleName(), fieldDefinition.getType().getType().getSimpleName());
                    return null;
                }
            }
        }
        return fieldValue;
    }
}
