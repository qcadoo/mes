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

package com.qcadoo.mes.model.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.FieldType;
import com.qcadoo.mes.model.validators.FieldValidator;
import com.qcadoo.mes.model.validators.internal.RequiredOnCreateValidator;
import com.qcadoo.mes.model.validators.internal.RequiredValidator;
import com.qcadoo.mes.model.validators.internal.UniqueValidator;

public final class FieldDefinitionImpl implements FieldDefinition {

    private final String name;

    private FieldType type;

    private final List<FieldValidator> validators = new ArrayList<FieldValidator>();

    private boolean readOnlyOnUpdate;

    private boolean readOnly;

    private boolean required;

    private boolean requiredOnCreate;

    private boolean customField;

    private boolean unique;

    private boolean persistent = true;

    private Object defaultValue;

    private final DataDefinition dataDefinition;

    public FieldDefinitionImpl(final DataDefinition dataDefinition, final String name) {
        this.dataDefinition = dataDefinition;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue(final Object value, final Locale locale) {
        if (value == null) {
            return null;
        } else {
            return type.toString(value, locale);
        }
    }

    @Override
    public FieldType getType() {
        return type;
    }

    public FieldDefinitionImpl withType(final FieldType type) {
        this.type = type;
        return this;
    }

    @Override
    public List<FieldValidator> getValidators() {
        return validators;
    }

    @Override
    public DataDefinition getDataDefinition() {
        return dataDefinition;
    }

    public FieldDefinitionImpl withValidator(final FieldValidator validator) {
        if (validator instanceof RequiredValidator) {
            required = true;
        }
        if (validator instanceof RequiredOnCreateValidator) {
            requiredOnCreate = true;
        }
        if (validator instanceof UniqueValidator) {
            unique = true;
        }
        this.validators.add(validator);
        return this;
    }

    @Override
    public boolean isReadOnlyOnUpdate() {
        return readOnlyOnUpdate;
    }

    public FieldDefinition withReadOnlyOnUpdate(final boolean readOnlyOnUpdate) {
        this.readOnlyOnUpdate = readOnlyOnUpdate;
        return this;
    }

    public FieldDefinitionImpl withReadOnly(final boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean isRequiredOnCreate() {
        return requiredOnCreate;
    }

    public void setCustomField(final boolean customField) {
        this.customField = customField;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    public FieldDefinition withDefaultValue(final Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public boolean isUnique() {
        return unique;
    }

    public void setPersistent(final boolean persistent) {
        this.persistent = persistent;
    }

    @Override
    public boolean isPersistent() {
        return persistent;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 31).append(customField).append(defaultValue).append(readOnlyOnUpdate).append(name)
                .append(required).append(type).append(unique).append(validators).append(readOnly).append(requiredOnCreate)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FieldDefinitionImpl)) {
            return false;
        }
        FieldDefinitionImpl other = (FieldDefinitionImpl) obj;
        return new EqualsBuilder().append(customField, other.customField).append(defaultValue, other.defaultValue)
                .append(readOnlyOnUpdate, other.readOnlyOnUpdate).append(name, other.name).append(required, other.required)
                .append(type, other.type).append(unique, other.unique).append(validators, other.validators)
                .append(readOnly, other.readOnly).append(requiredOnCreate, other.requiredOnCreate).isEquals();
    }

    @Override
    public String toString() {
        return name;
    }

}
