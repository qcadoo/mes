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

}
