package com.qcadoo.mes.plugins.products.data.mock;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.qcadoo.mes.core.data.definition.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.core.data.search.Order;
import com.qcadoo.mes.core.data.search.Restriction;

public class GridDefinitionMock implements GridDefinition {

    private String name;

    public GridDefinitionMock(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<FieldDefinition> getSearchableFields() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ColumnDefinition> getColumns() {
        List<ColumnDefinition> columnDefinitions = new LinkedList<ColumnDefinition>();
        columnDefinitions.add(new ColumnDefinitionMock("Numer"));
        columnDefinitions.add(new ColumnDefinitionMock("Nazwa"));
        columnDefinitions.add(new ColumnDefinitionMock("Typ materialu"));
        columnDefinitions.add(new ColumnDefinitionMock("EAN"));
        columnDefinitions.add(new ColumnDefinitionMock("Kategoria"));
        columnDefinitions.add(new ColumnDefinitionMock("Jednostka"));
        return columnDefinitions;
    }

    @Override
    public Order getDefaultOrder() {
        return Order.asc();
    }

    @Override
    public Set<Restriction> getDefaultRestrictions() {
        // TODO Auto-generated method stub
        return null;
    }

}
