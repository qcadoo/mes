package com.qcadoo.mes.core.data.internal;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.definition.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.definition.FieldType;
import com.qcadoo.mes.core.data.definition.FieldTypeFactory;
import com.qcadoo.mes.core.data.definition.GridDefinition;

@Service
public final class DataDefinitionServiceImpl implements DataDefinitionService {

    @Autowired
    private FieldTypeFactory fieldTypeFactory;

    @Override
    public void save(final DataDefinition dataDefinition) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public DataDefinition get(final String entityName) {
        if ("products.product".equals(entityName)) {
            return createProductDefinition();
        } else if ("products.substitute".equals(entityName)) {
            return createSubstituteDefinition();
        }
        return null;
    }

    private DataDefinition createProductDefinition() {
        DataDefinition dataDefinition = new DataDefinition("products.product");
        GridDefinition gridDefinition = new GridDefinition("products");

        FieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.textType());
        FieldDefinition fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType());
        FieldDefinition fieldType = createFieldDefinition("type", fieldTypeFactory.stringType());
        FieldDefinition fieldTypeOfMaterial = createFieldDefinition("typeOfMaterial",
                fieldTypeFactory.enumType("product", "intermediate", "component"));
        FieldDefinition fieldEan = createFieldDefinition("ean", fieldTypeFactory.stringType());
        FieldDefinition fieldCategory = createFieldDefinition("category", fieldTypeFactory.dictionaryType("categories"));
        FieldDefinition fieldUnit = createFieldDefinition("unit", fieldTypeFactory.stringType());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.Product");
        dataDefinition.setGrids(Arrays.asList(new GridDefinition[] { gridDefinition }));
        dataDefinition.setFields(Arrays.asList(new FieldDefinition[] { fieldName, fieldNumber, fieldType, fieldTypeOfMaterial,
                fieldEan, fieldCategory, fieldUnit }));

        ColumnDefinition columnNumber = createColumnDefinition("number", fieldNumber);
        ColumnDefinition columnName = createColumnDefinition("name", fieldName);
        ColumnDefinition columnType = createColumnDefinition("type", fieldType);
        ColumnDefinition columnEan = createColumnDefinition("ean", fieldEan);

        gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnNumber, columnName, columnType, columnEan }));

        return dataDefinition;
    }

    private DataDefinition createSubstituteDefinition() {
        DataDefinition dataDefinition = new DataDefinition("products.substitute");
        GridDefinition gridDefinition = new GridDefinition("substitutes");

        FieldDefinition fieldNo = createFieldDefinition("no", fieldTypeFactory.stringType());
        FieldDefinition fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType());
        FieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.textType());
        FieldDefinition fieldEffectiveDateFrom = createFieldDefinition("effectiveDateFrom", fieldTypeFactory.stringType());
        FieldDefinition fieldEffectiveDateTo = createFieldDefinition("effectiveDateTo", fieldTypeFactory.stringType());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.Substitute");
        dataDefinition.setGrids(Arrays.asList(new GridDefinition[] { gridDefinition }));
        dataDefinition.setFields(Arrays.asList(new FieldDefinition[] { fieldNo, fieldNumber, fieldName, fieldEffectiveDateFrom,
                fieldEffectiveDateTo }));

        ColumnDefinition columnNo = createColumnDefinition("no", fieldNo);
        ColumnDefinition columnNumber = createColumnDefinition("number", fieldNumber);
        ColumnDefinition columnName = createColumnDefinition("name", fieldName);

        gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnNo, columnNumber, columnName }));

        return dataDefinition;
    }

    private ColumnDefinition createColumnDefinition(final String name, final FieldDefinition field) {
        ColumnDefinition columnDefinition = new ColumnDefinition(name);
        columnDefinition.setFields(Arrays.asList(new FieldDefinition[] { field }));
        return columnDefinition;
    }

    private FieldDefinition createFieldDefinition(final String name, final FieldType type) {
        FieldDefinition fieldDefinition = new FieldDefinition(name);
        fieldDefinition.setType(type);
        return fieldDefinition;
    }

    @Override
    public void delete(final String entityName) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public List<DataDefinition> list() {
        throw new UnsupportedOperationException("implement me");
    }

}
