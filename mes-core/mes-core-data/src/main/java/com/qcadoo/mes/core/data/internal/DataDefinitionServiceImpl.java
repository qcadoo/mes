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
        } else if ("products.substituteComponent".equals(entityName)) {
            return createSubstituteComponentDefinition();
        }
        return null;
    }

    private DataDefinition createProductDefinition() {
        DataDefinition dataDefinition = new DataDefinition("products.product");
        GridDefinition gridDefinition = new GridDefinition("products");

        FieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.textType());
        FieldDefinition fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType());
        FieldDefinition fieldTypeOfMaterial = createFieldDefinition("typeOfMaterial",
                fieldTypeFactory.enumType("product", "intermediate", "component"));
        FieldDefinition fieldEan = createFieldDefinition("ean", fieldTypeFactory.stringType());
        FieldDefinition fieldCategory = createFieldDefinition("category", fieldTypeFactory.dictionaryType("categories"));
        FieldDefinition fieldUnit = createFieldDefinition("unit", fieldTypeFactory.stringType());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.Product");
        dataDefinition.setGrids(Arrays.asList(new GridDefinition[] { gridDefinition }));
        dataDefinition.setFields(Arrays.asList(new FieldDefinition[] { fieldName, fieldNumber, fieldTypeOfMaterial, fieldEan,
                fieldCategory, fieldUnit }));

        ColumnDefinition columnNumber = createColumnDefinition("number", fieldNumber, null);
        ColumnDefinition columnName = createColumnDefinition("name", fieldName, null);
        ColumnDefinition columnType = createColumnDefinition("typeOfMaterial", fieldTypeOfMaterial, null);
        ColumnDefinition columnEan = createColumnDefinition("ean", fieldEan, null);

        gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnNumber, columnName, columnType, columnEan }));

        return dataDefinition;
    }

    private DataDefinition createSubstituteDefinition() {
        DataDefinition dataDefinition = new DataDefinition("products.substitute");
        GridDefinition gridDefinition = new GridDefinition("substitutes");

        FieldDefinition fieldNo = createFieldDefinition("no", fieldTypeFactory.integerType());
        FieldDefinition fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType());
        FieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.textType());
        FieldDefinition fieldEffectiveDateFrom = createFieldDefinition("effectiveDateFrom", fieldTypeFactory.dateTimeType());
        FieldDefinition fieldEffectiveDateTo = createFieldDefinition("effectiveDateTo", fieldTypeFactory.dateTimeType());
        FieldDefinition fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products.product", "name"));

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.Substitute");
        dataDefinition.setGrids(Arrays.asList(new GridDefinition[] { gridDefinition }));
        dataDefinition.setFields(Arrays.asList(new FieldDefinition[] { fieldNo, fieldNumber, fieldName, fieldEffectiveDateFrom,
                fieldEffectiveDateTo, fieldProduct }));

        ColumnDefinition columnNo = createColumnDefinition("no", fieldNo, null);
        ColumnDefinition columnNumber = createColumnDefinition("number", fieldNumber, null);
        ColumnDefinition columnName = createColumnDefinition("name", fieldName, null);

        gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnNo, columnNumber, columnName }));

        return dataDefinition;
    }

    private DataDefinition createSubstituteComponentDefinition() {
        DataDefinition dataDefinition = new DataDefinition("products.substituteComponent");
        GridDefinition gridDefinition = new GridDefinition("substituteComponents");

        FieldDefinition fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products.product", "name"));
        FieldDefinition fieldSubstitute = createFieldDefinition("substitute",
                fieldTypeFactory.eagerBelongsToType("products.substitute", "name"));
        FieldDefinition fieldQuantity = createFieldDefinition("quantity", fieldTypeFactory.decimalType());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.SubstituteComponent");
        dataDefinition.setGrids(Arrays.asList(new GridDefinition[] { gridDefinition }));
        dataDefinition.setFields(Arrays.asList(new FieldDefinition[] { fieldProduct, fieldSubstitute, fieldQuantity }));

        ColumnDefinition columnSubstituteNumber = createColumnDefinition("number", fieldProduct, "product['number']");
        ColumnDefinition columnProductName = createColumnDefinition("name", fieldProduct, "product['name']");
        ColumnDefinition columnQuantity = createColumnDefinition("quantity", fieldQuantity, null);
        gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnSubstituteNumber, columnProductName,
                columnQuantity }));

        return dataDefinition;
    }

    private ColumnDefinition createColumnDefinition(final String name, final FieldDefinition field, final String expression) {
        ColumnDefinition columnDefinition = new ColumnDefinition(name);
        columnDefinition.setFields(Arrays.asList(new FieldDefinition[] { field }));
        columnDefinition.setExpression(expression);
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
