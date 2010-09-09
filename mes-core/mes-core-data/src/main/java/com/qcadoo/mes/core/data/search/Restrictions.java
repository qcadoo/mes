package com.qcadoo.mes.core.data.search;

import org.apache.commons.beanutils.PropertyUtils;

import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
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

    public static Restriction eq(final DataFieldDefinition fieldDefinition, final Object expectedValue) {
        ValidationResults validationResults = new ValidationResults();
        Object value = validateValue(fieldDefinition, expectedValue, validationResults);
        if (!validationResults.getErrors().isEmpty()) {
            return null;
        }
        if (value instanceof String
                && (((String) value).contains("*") || ((String) value).contains("%") || ((String) value).contains("?") || ((String) value)
                        .contains("_"))) {
            String preperadValue = ((String) value).replace('*', '%').replace('?', '_');
            return new LikeRestriction(fieldDefinition.getName(), preperadValue);
        }
        return new SimpleRestriction(fieldDefinition.getName(), value, RestrictionOperator.EQ);
    }

    public static Restriction belongsTo(final DataFieldDefinition fieldDefinition, final Object entityOrId) {
        if (entityOrId instanceof Long) {
            return new BelongsToRestriction(fieldDefinition.getName(), (Long) entityOrId);
        } else {
            try {
                return new BelongsToRestriction(fieldDefinition.getName(), (Long) PropertyUtils.getProperty(entityOrId, "id"));
            } catch (Exception e) {
                throw new IllegalStateException("cannot get value of the property: " + entityOrId.getClass().getSimpleName()
                        + ", id", e);
            }
        }
    }

    public static Restriction idRestriction(final Long id, final RestrictionOperator restrictionOperator) {
        return new SimpleRestriction("id", id, restrictionOperator);
    }

    public static Restriction ge(final DataFieldDefinition fieldDefinition, final Object expectedValue) {
        ValidationResults validationResults = new ValidationResults();
        Object value = validateValue(fieldDefinition, expectedValue, validationResults);
        if (!validationResults.getErrors().isEmpty()) {
            return null;
        }
        return new SimpleRestriction(fieldDefinition.getName(), value, RestrictionOperator.GE);
    }

    public static Restriction gt(final DataFieldDefinition fieldDefinition, final Object expectedValue) {
        ValidationResults validationResults = new ValidationResults();
        Object value = validateValue(fieldDefinition, expectedValue, validationResults);
        if (!validationResults.getErrors().isEmpty()) {
            return null;
        }
        return new SimpleRestriction(fieldDefinition.getName(), value, RestrictionOperator.GT);
    }

    public static Restriction isNotNull(final DataFieldDefinition fieldDefinition) {
        return new IsNotNullRestriction(fieldDefinition.getName());
    }

    public static Restriction isNull(final DataFieldDefinition fieldDefinition) {
        return new IsNullRestriction(fieldDefinition.getName());
    }

    public static Restriction le(final DataFieldDefinition fieldDefinition, final Object expectedValue) {
        ValidationResults validationResults = new ValidationResults();
        Object value = validateValue(fieldDefinition, expectedValue, validationResults);
        if (!validationResults.getErrors().isEmpty()) {
            return null;
        }
        return new SimpleRestriction(fieldDefinition.getName(), value, RestrictionOperator.LE);
    }

    public static Restriction lt(final DataFieldDefinition fieldDefinition, final Object expectedValue) {
        ValidationResults validationResults = new ValidationResults();
        Object value = validateValue(fieldDefinition, expectedValue, validationResults);
        if (!validationResults.getErrors().isEmpty()) {
            return null;
        }
        return new SimpleRestriction(fieldDefinition.getName(), value, RestrictionOperator.LT);
    }

    public static Restriction ne(final DataFieldDefinition fieldDefinition, final Object expectedValue) {
        ValidationResults validationResults = new ValidationResults();
        Object value = validateValue(fieldDefinition, expectedValue, validationResults);
        if (!validationResults.getErrors().isEmpty()) {
            return null;
        }
        return new SimpleRestriction(fieldDefinition.getName(), value, RestrictionOperator.NE);
    }

    private static Object validateValue(final DataFieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        Object fieldValue = value;
        if (fieldValue != null) {
            if (!fieldDefinition.getType().getType().isInstance(fieldValue)) {
                if (fieldValue instanceof String) {
                    fieldValue = fieldDefinition.getType().toObject(fieldDefinition, fieldValue, validationResults);
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
