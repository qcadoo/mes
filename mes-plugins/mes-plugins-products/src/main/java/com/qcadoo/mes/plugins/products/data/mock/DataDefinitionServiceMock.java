package com.qcadoo.mes.plugins.products.data.mock;

import java.util.LinkedList;
import java.util.List;

import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.definition.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;

public class DataDefinitionServiceMock implements DataDefinitionService {

    public void save(DataDefinition dataDefinition) {

    }

    public DataDefinition get(String entityName) {
        if (!"product".equals(entityName)) {
            return null;
        }
        DataDefinition dataDef = new DataDefinition();
        List<GridDefinition> grids = new LinkedList<GridDefinition>();
        GridDefinition gridDef = new GridDefinition();
        List<ColumnDefinition> columns = new LinkedList<ColumnDefinition>();
        ColumnDefinition c1 = new ColumnDefinition();
        c1.setName("Numer");
        columns.add(c1);
        ColumnDefinition c2 = new ColumnDefinition();
        c2.setName("Nazwa");
        columns.add(c2);
        ColumnDefinition c3 = new ColumnDefinition();
        c3.setName("Typ materialu");
        columns.add(c3);
        ColumnDefinition c4 = new ColumnDefinition();
        c4.setName("EAN");
        columns.add(c4);
        ColumnDefinition c5 = new ColumnDefinition();
        c5.setName("Kategoria");
        columns.add(c5);
        ColumnDefinition c6 = new ColumnDefinition();
        c6.setName("Jednostka");
        columns.add(c6);
        gridDef.setColumns(columns);
        grids.add(gridDef);
        dataDef.setGrids(grids);
        return dataDef;
    }

    public void delete(String entityName) {

    }

    public List<DataDefinition> list() {
        return null;
    }

}
