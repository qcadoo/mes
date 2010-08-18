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

}
