package com.qcadoo.mes.plugins.products.data.mock;

import java.util.LinkedList;
import java.util.List;

import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;

public class DataDefinitionMock implements DataDefinition {

    private String entityName;

    public DataDefinitionMock(String entityName) {
        this.entityName = entityName;
    }

    @Override
    public String getEntityName() {
        return entityName;
    }

    @Override
    public List<FieldDefinition> getFields() {
        List<FieldDefinition> fields = new LinkedList<FieldDefinition>();
        fields.add(new FieldDefinitionMock("Numer"));
        fields.add(new FieldDefinitionMock("Nazwa"));
        fields.add(new FieldDefinitionMock("Typ materialu"));
        fields.add(new FieldDefinitionMock("Kod EAN"));
        fields.add(new FieldDefinitionMock("Kategoria"));
        fields.add(new FieldDefinitionMock("Jednostka"));
        return fields;
    }

    @Override
    public List<GridDefinition> getGrids() {
        List<GridDefinition> gridDefList = new LinkedList<GridDefinition>();
        gridDefList.add(new GridDefinitionMock("basicList"));
        return gridDefList;
    }

    @Override
    public boolean isPluginTable() {
        return false;
    }

    @Override
    public String getDiscriminator() {
        return null;
    }

    @Override
    public String getFullyQualifiedClassName() {
        return null;
    }

    @Override
    public boolean isVirtualTable() {
        return false;
    }

    @Override
    public boolean isCoreTable() {
        return false;
    }
}
