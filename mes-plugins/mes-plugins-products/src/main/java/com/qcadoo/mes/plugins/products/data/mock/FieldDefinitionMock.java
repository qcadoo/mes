package com.qcadoo.mes.plugins.products.data.mock;

import java.util.Set;

import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.definition.FieldType;
import com.qcadoo.mes.core.data.definition.FieldTypes;
import com.qcadoo.mes.core.data.definition.FieldValidator;

public class FieldDefinitionMock implements FieldDefinition {

    private String name;

    public FieldDefinitionMock(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FieldType getType() {
        return FieldTypes.stringType();
    }

    @Override
    public Set<FieldValidator> getValidators() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isEditable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequired() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCustomField() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isHidden() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object getDefaultValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isUnique() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FieldDefinitionMock other = (FieldDefinitionMock) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
