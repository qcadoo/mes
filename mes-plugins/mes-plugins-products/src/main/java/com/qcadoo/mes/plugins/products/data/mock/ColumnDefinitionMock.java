package com.qcadoo.mes.plugins.products.data.mock;

import java.util.LinkedList;
import java.util.List;

import com.qcadoo.mes.core.data.definition.ColumnAggregationMode;
import com.qcadoo.mes.core.data.definition.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;

public class ColumnDefinitionMock implements ColumnDefinition {

    private String name;

    private List<FieldDefinition> fields;

    public ColumnDefinitionMock(String name) {
        this.name = name;
        fields = new LinkedList<FieldDefinition>();
        fields.add(new FieldDefinitionMock(name));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<FieldDefinition> getFields() {
        return fields;
    }

    @Override
    public ColumnAggregationMode getAggregationMode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getExpression() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getWidth() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fields == null) ? 0 : fields.hashCode());
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
        ColumnDefinitionMock other = (ColumnDefinitionMock) obj;
        if (fields == null) {
            if (other.fields != null)
                return false;
        } else if (!fields.equals(other.fields))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
