/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.model.search;

import org.apache.commons.beanutils.PropertyUtils;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.restrictions.internal.BelongsToRestriction;
import com.qcadoo.mes.model.search.restrictions.internal.IsNotNullRestriction;
import com.qcadoo.mes.model.search.restrictions.internal.IsNullRestriction;
import com.qcadoo.mes.model.search.restrictions.internal.LikeRestriction;
import com.qcadoo.mes.model.search.restrictions.internal.SimpleRestriction;

/**
 * Class creates restrictions for search criteria.
 * 
 * @apiviz.uses com.qcadoo.mes.model.search.Restriction
 * @apiviz.uses com.qcadoo.mes.model.FieldDefinition
 */
public final class Restrictions {

    private Restrictions() {
    }

    /**
     * Create "equals" restriction for given field's definition. If expected value contains "%", "*", "?" or "_" sql "like"
     * restriction will be used.
     * 
     * @param fieldDefinition
     *            field's definition
     * @param expectedValue
     *            expected value
     * @return restriction
     */
    public static Restriction eq(final FieldDefinition fieldDefinition, final Object expectedValue) {
        Entity validatedEntity = new DefaultEntity("", "");
        Object value = validateValue(fieldDefinition, expectedValue, validatedEntity);
        if (!validatedEntity.getErrors().isEmpty()) {
            return null;
        }
        return createEqRestriction(fieldDefinition.getName(), value);
    }

    /**
     * Create "equals" restriction for given field's name. If expected value contains "%", "*", "?" or "_" sql "like" restriction
     * will be used.
     * 
     * @param fieldName
     *            field's name
     * @param expectedValue
     *            expected value
     * @return restriction
     */
    public static Restriction eq(final String fieldName, final String expectedValue) {
        return createEqRestriction(fieldName, expectedValue);
    }

    private static Restriction createEqRestriction(final String fieldName, final Object expectedValue) {
        if (expectedValue instanceof String && ((String) expectedValue).matches(".*[\\*%\\?_].*")) {
            String preperadValue = ((String) expectedValue).replace('*', '%').replace('?', '_');
            return new LikeRestriction(fieldName, preperadValue);
        }
        return new SimpleRestriction(fieldName, expectedValue, RestrictionOperator.EQ);
    }

    /**
     * Create "belongsTo" restriction for given field's definition. Expected value can be both entity object or its id.
     * 
     * @param fieldDefinition
     *            field's definition
     * @param entityOrId
     *            entity or id
     * @return restriction
     */
    public static Restriction belongsTo(final FieldDefinition fieldDefinition, final Object entityOrId) {
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

    /**
     * Create simple restriction for id field, comparing it to expected value using given operator.
     * 
     * @param id
     *            expected value
     * @param operator
     *            operator
     * @return restriction
     */
    public static Restriction idRestriction(final Long id, final RestrictionOperator operator) {
        return new SimpleRestriction("id", id, operator);
    }

    /**
     * Create "greater or equals" restriction for given field's definition.
     * 
     * @param fieldDefinition
     *            field's definition
     * @param expectedValue
     *            expected value
     * @return restriction
     */
    public static Restriction ge(final FieldDefinition fieldDefinition, final Object expectedValue) {
        Entity validatedEntity = new DefaultEntity("", "");
        Object value = validateValue(fieldDefinition, expectedValue, validatedEntity);
        if (!validatedEntity.getErrors().isEmpty()) {
            return null;
        }
        return new SimpleRestriction(fieldDefinition.getName(), value, RestrictionOperator.GE);
    }

    /**
     * Create "greater than" restriction for given field's definition.
     * 
     * @param fieldDefinition
     *            field's definition
     * @param expectedValue
     *            expected value
     * @return restriction
     */
    public static Restriction gt(final FieldDefinition fieldDefinition, final Object expectedValue) {
        Entity validatedEntity = new DefaultEntity("", "");
        Object value = validateValue(fieldDefinition, expectedValue, validatedEntity);
        if (!validatedEntity.getErrors().isEmpty()) {
            return null;
        }
        return new SimpleRestriction(fieldDefinition.getName(), value, RestrictionOperator.GT);
    }

    /**
     * Create "is not null" restriction for given field's definition.
     * 
     * @param fieldDefinition
     *            field's definition
     * @return restriction
     */
    public static Restriction isNotNull(final FieldDefinition fieldDefinition) {
        return new IsNotNullRestriction(fieldDefinition.getName());
    }

    /**
     * Create "is null" restriction for given field's definition.
     * 
     * @param fieldDefinition
     *            field's definition
     * @return restriction
     */
    public static Restriction isNull(final FieldDefinition fieldDefinition) {
        return new IsNullRestriction(fieldDefinition.getName());
    }

    /**
     * Create "less or equals" restriction for given field's definition.
     * 
     * @param fieldDefinition
     *            field's definition
     * @param expectedValue
     *            expected value
     * @return restriction
     */
    public static Restriction le(final FieldDefinition fieldDefinition, final Object expectedValue) {
        Entity validatedEntity = new DefaultEntity("", "");
        Object value = validateValue(fieldDefinition, expectedValue, validatedEntity);
        if (!validatedEntity.getErrors().isEmpty()) {
            return null;
        }
        return new SimpleRestriction(fieldDefinition.getName(), value, RestrictionOperator.LE);
    }

    /**
     * Create "greater than" restriction for given field's definition.
     * 
     * @param fieldDefinition
     *            field's definition
     * @param expectedValue
     *            expected value
     * @return restriction
     */
    public static Restriction lt(final FieldDefinition fieldDefinition, final Object expectedValue) {
        Entity validatedEntity = new DefaultEntity("", "");
        Object value = validateValue(fieldDefinition, expectedValue, validatedEntity);
        if (!validatedEntity.getErrors().isEmpty()) {
            return null;
        }
        return new SimpleRestriction(fieldDefinition.getName(), value, RestrictionOperator.LT);
    }

    /**
     * Create "not equals" restriction for given field's definition.
     * 
     * @param fieldDefinition
     *            field's definition
     * @param expectedValue
     *            expected value
     * @return restriction
     */
    public static Restriction ne(final FieldDefinition fieldDefinition, final Object expectedValue) {
        Entity validatedEntity = new DefaultEntity("", "");
        Object value = validateValue(fieldDefinition, expectedValue, validatedEntity);
        if (!validatedEntity.getErrors().isEmpty()) {
            return null;
        }
        return new SimpleRestriction(fieldDefinition.getName(), value, RestrictionOperator.NE);
    }

    private static Object validateValue(final FieldDefinition fieldDefinition, final Object value, final Entity validatedEntity) {
        Object fieldValue = value;
        if (fieldValue != null && !fieldDefinition.getType().getType().isInstance(fieldValue)) {
            if (fieldValue instanceof String) {
                fieldValue = fieldDefinition.getType().toObject(fieldDefinition, fieldValue, validatedEntity);
            } else {
                validatedEntity.addError(fieldDefinition, "core.validation.error.wrongType", fieldValue.getClass()
                        .getSimpleName(), fieldDefinition.getType().getType().getSimpleName());
                return null;
            }
        }
        return fieldValue;
    }
}
