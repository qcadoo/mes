package com.qcadoo.mes.plugins.products.data.mock;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.definition.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.definition.FieldTypeFactory;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.core.data.internal.FieldTypeFactoryImpl;

public class DataDefinitionServiceMock implements DataDefinitionService {

    public void save(DataDefinition dataDefinition) {

    }

    public DataDefinition get(String entityName) {
        FieldTypeFactory fieldTypeFactory = new FieldTypeFactoryImpl();
        FieldDefinition fieldNumber = new FieldDefinition("number");
        fieldNumber.setRequired(true);
        fieldNumber.setType(fieldTypeFactory.stringType());
        FieldDefinition fieldType = new FieldDefinition("type");
        fieldType.setType(fieldTypeFactory.stringType());
        FieldDefinition fieldTypeOfMaterial = new FieldDefinition("typeOfMaterial");
        fieldTypeOfMaterial.setType(fieldTypeFactory.stringType());
        FieldDefinition fieldEan = new FieldDefinition("ean");
        fieldEan.setType(fieldTypeFactory.stringType());
        FieldDefinition fieldCategory = new FieldDefinition("category");
        fieldCategory.setType(fieldTypeFactory.stringType());
        FieldDefinition fieldUnit = new FieldDefinition("unit");
        fieldUnit.setType(fieldTypeFactory.stringType());
        DataDefinition dataDef = new DataDefinition("Produkt");
        List<GridDefinition> grids = new LinkedList<GridDefinition>();
        GridDefinition gridDef = new GridDefinition("Grid");
        List<ColumnDefinition> columns = new LinkedList<ColumnDefinition>();
        ColumnDefinition c1 = new ColumnDefinition("Numer");
        columns.add(c1);
        ColumnDefinition c2 = new ColumnDefinition("Nazwa");
        columns.add(c2);
        ColumnDefinition c3 = new ColumnDefinition("Typ materialu");
        columns.add(c3);
        ColumnDefinition c4 = new ColumnDefinition("EAN");
        columns.add(c4);
        ColumnDefinition c5 = new ColumnDefinition("Kategoria");
        columns.add(c5);
        ColumnDefinition c6 = new ColumnDefinition("Jednostka");
        columns.add(c6);
        gridDef.setColumns(columns);
        grids.add(gridDef);
        dataDef.setGrids(grids);
        dataDef.setFields(Arrays.asList(new FieldDefinition[] { fieldNumber, fieldType, fieldTypeOfMaterial, fieldEan,
                fieldCategory, fieldUnit }));

        return dataDef;
    }

    public void delete(String entityName) {

    }

    public List<DataDefinition> list() {
        return null;
    }

}
