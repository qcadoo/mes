package com.qcadoo.mes.core.data.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.FieldValidatorFactory;

@Service
public final class DataDefinitionServiceImpl implements DataDefinitionService {

    @Autowired
    private FieldTypeFactory fieldTypeFactory;

    @Autowired
    private FieldValidatorFactory fieldValidationFactory;

    @Override
    public void save(final DataDefinition dataDefinition) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public DataDefinition get(final String entityName) {
        DataDefinition dataDefinition = null;
        if ("products.product".equals(entityName)) {
            dataDefinition = createProductDefinition();
        } else if ("products.substitute".equals(entityName)) {
            dataDefinition = createSubstituteDefinition();
        } else if ("products.substituteComponent".equals(entityName)) {
            dataDefinition = createSubstituteComponentDefinition();
        } else if ("users.user".equals(entityName)) {
            dataDefinition = createUserDefinition();
        } else if ("users.group".equals(entityName)) {
            dataDefinition = createUserGroupDefinition();
        } else if ("orders.order".equals(entityName)) {
            dataDefinition = createOrderDefinition();
        }

        checkNotNull(dataDefinition, "data definition for %s cannot be found", entityName);

        return dataDefinition;
    }

    private DataDefinition createProductDefinition() {
        DataDefinition dataDefinition = new DataDefinition("products.product");
        // GridDefinition gridDefinition = new GridDefinition("products");

        FieldDefinition fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType());
        fieldNumber.setValidators(fieldValidationFactory.required());
        FieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.textType());
        fieldName.setValidators(fieldValidationFactory.required());
        FieldDefinition fieldTypeOfMaterial = createFieldDefinition("typeOfMaterial",
                fieldTypeFactory.enumType("product", "intermediate", "component"));
        fieldTypeOfMaterial.setValidators(fieldValidationFactory.required());
        FieldDefinition fieldEan = createFieldDefinition("ean", fieldTypeFactory.stringType());
        FieldDefinition fieldCategory = createFieldDefinition("category", fieldTypeFactory.dictionaryType("categories"));
        FieldDefinition fieldUnit = createFieldDefinition("unit", fieldTypeFactory.stringType());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.Product");
        // dataDefinition.setGrids(Arrays.asList(new GridDefinition[] { gridDefinition }));
        dataDefinition.addField(fieldNumber);
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldTypeOfMaterial);
        dataDefinition.addField(fieldEan);
        dataDefinition.addField(fieldCategory);
        dataDefinition.addField(fieldUnit);

        // ColumnDefinition columnNumber = createColumnDefinition("number", fieldNumber, null);
        // ColumnDefinition columnName = createColumnDefinition("name", fieldName, null);
        // ColumnDefinition columnType = createColumnDefinition("typeOfMaterial", fieldTypeOfMaterial, null);
        // ColumnDefinition columnEan = createColumnDefinition("ean", fieldEan, null);

        // gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnNumber, columnName, columnType, columnEan }));

        return dataDefinition;
    }

    private DataDefinition createSubstituteDefinition() {
        DataDefinition dataDefinition = new DataDefinition("products.substitute");
        // GridDefinition gridDefinition = new GridDefinition("substitutes");

        FieldDefinition fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType());
        fieldNumber.setValidators(fieldValidationFactory.required());
        FieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.textType());
        fieldName.setValidators(fieldValidationFactory.required());
        FieldDefinition fieldPriority = createFieldDefinition("priority", fieldTypeFactory.integerType());
        fieldPriority.setValidators(fieldValidationFactory.required());
        FieldDefinition fieldEffectiveDateFrom = createFieldDefinition("effectiveDateFrom", fieldTypeFactory.dateTimeType());
        FieldDefinition fieldEffectiveDateTo = createFieldDefinition("effectiveDateTo", fieldTypeFactory.dateTimeType());
        FieldDefinition fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products.product", "name"));
        fieldProduct.setValidators(fieldValidationFactory.required());
        fieldProduct.setHidden(true);

        // TODO masz dataDefinition.setValidators(...) data pocz�tkowa mniejsza od ko�cowej
        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.Substitute");
        // dataDefinition.setGrids(Arrays.asList(new GridDefinition[] { gridDefinition }));
        dataDefinition.addField(fieldNumber);
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldPriority);
        dataDefinition.addField(fieldEffectiveDateFrom);
        dataDefinition.addField(fieldEffectiveDateTo);
        dataDefinition.addField(fieldProduct);

        // ColumnDefinition columnNumber = createColumnDefinition("number", fieldNumber, null);
        // ColumnDefinition columnName = createColumnDefinition("name", fieldName, null);
        // ColumnDefinition columnPriority = createColumnDefinition("priority", fieldPriority, null);
        //
        // gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnNumber, columnName, columnPriority }));

        return dataDefinition;
    }

    private DataDefinition createSubstituteComponentDefinition() {
        DataDefinition dataDefinition = new DataDefinition("products.substituteComponent");
        // GridDefinition gridDefinition = new GridDefinition("substituteComponents");

        FieldDefinition fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products.product", "name"));
        fieldProduct.setValidators(fieldValidationFactory.required());
        FieldDefinition fieldSubstitute = createFieldDefinition("substitute",
                fieldTypeFactory.eagerBelongsToType("products.substitute", "name"));
        fieldSubstitute.setValidators(fieldValidationFactory.required());
        fieldSubstitute.setHidden(true);
        FieldDefinition fieldQuantity = createFieldDefinition("quantity", fieldTypeFactory.decimalType());
        fieldQuantity.setValidators(fieldValidationFactory.required());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.SubstituteComponent");
        // dataDefinition.setGrids(Arrays.asList(new GridDefinition[] { gridDefinition }));
        dataDefinition.addField(fieldProduct);
        dataDefinition.addField(fieldSubstitute);
        dataDefinition.addField(fieldQuantity);

        // ColumnDefinition columnSubstituteNumber = createColumnDefinition("number", fieldProduct,
        // "fields['product'].fields['number']");
        // ColumnDefinition columnProductName = createColumnDefinition("name", fieldProduct, "fields['product'].fields['name']");
        // ColumnDefinition columnQuantity = createColumnDefinition("quantity", fieldQuantity, null);
        // gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnSubstituteNumber, columnProductName,
        // columnQuantity }));

        return dataDefinition;
    }

    // private ColumnDefinition createColumnDefinition(final String name, final FieldDefinition field, final String expression) {
    // ColumnDefinition columnDefinition = new ColumnDefinition(name);
    // columnDefinition.setFields(Arrays.asList(new FieldDefinition[] { field }));
    // columnDefinition.setExpression(expression);
    // return columnDefinition;
    // }

    private DataDefinition createUserDefinition() {
        DataDefinition dataDefinition = new DataDefinition("users.user");

        FieldDefinition fieldLogin = createFieldDefinition("login", fieldTypeFactory.stringType());
        fieldLogin.setValidators(fieldValidationFactory.required(), fieldValidationFactory.unique());
        // TODO KRNA zamienic na relacje
        FieldDefinition fieldUserGroup = createFieldDefinition("userGroup",
                fieldTypeFactory.enumType("Administrator", "Operator - Full", "Operator - ReadOnly"));

        fieldUserGroup.setValidators(fieldValidationFactory.required());
        FieldDefinition fieldEmail = createFieldDefinition("email", fieldTypeFactory.stringType());
        FieldDefinition fieldFirstName = createFieldDefinition("firstName", fieldTypeFactory.stringType());
        FieldDefinition fieldLastName = createFieldDefinition("lastName", fieldTypeFactory.stringType());
        FieldDefinition fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType());
        FieldDefinition fieldPassword = createFieldDefinition("password", fieldTypeFactory.passwordType());
        fieldPassword.setValidators(fieldValidationFactory.required());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.SystemUser");

        dataDefinition.addField(fieldLogin);
        dataDefinition.addField(fieldUserGroup);
        dataDefinition.addField(fieldEmail);
        dataDefinition.addField(fieldFirstName);
        dataDefinition.addField(fieldLastName);
        dataDefinition.addField(fieldDescription);
        dataDefinition.addField(fieldPassword);

        return dataDefinition;
    }

    private DataDefinition createUserGroupDefinition() {
        DataDefinition dataDefinition = new DataDefinition("users.group");

        FieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.stringType());
        fieldName.setValidators(fieldValidationFactory.required(), fieldValidationFactory.unique());
        FieldDefinition fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType());
        // TODO KRNA zamienic na relacje
        FieldDefinition fieldRole = createFieldDefinition("role", fieldTypeFactory.enumType("read", "write", "delete"));

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.UserGroup");
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldDescription);
        dataDefinition.addField(fieldRole);

        return dataDefinition;
    }

    private DataDefinition createOrderDefinition() {
        DataDefinition dataDefinition = new DataDefinition("orders.order");

        FieldDefinition fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType());
        fieldNumber.setValidators(fieldValidationFactory.required(), fieldValidationFactory.unique());
        FieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.textType());
        fieldName.setValidators(fieldValidationFactory.required());
        FieldDefinition fieldDateFrom = createFieldDefinition("dateFrom", fieldTypeFactory.dateType());
        fieldDateFrom.setValidators(fieldValidationFactory.required());
        FieldDefinition fieldDateTo = createFieldDefinition("dateTo", fieldTypeFactory.dateType());
        fieldDateTo.setValidators(fieldValidationFactory.required());
        FieldDefinition fieldState = createFieldDefinition("state", fieldTypeFactory.enumType("New", "Pending", "Done"));
        fieldName.setValidators(fieldValidationFactory.required());
        FieldDefinition fieldMachine = createFieldDefinition("machine", fieldTypeFactory.enumType("Maszyna 1", "Maszyna 2"));
        FieldDefinition fieldProduct = createFieldDefinition("product", fieldTypeFactory.enumType("Produkt 1", "Produkt 2"));
        FieldDefinition fieldDefaultInstruction = createFieldDefinition("defaultInstruction", fieldTypeFactory.textType());
        FieldDefinition fieldInstruction = createFieldDefinition("instruction",
                fieldTypeFactory.enumType("Instrukcja 1", "Instrukcja 2"));
        FieldDefinition fieldPlannedQuantity = createFieldDefinition("plannedQuantity", fieldTypeFactory.decimalType());
        FieldDefinition fieldDoneQuantity = createFieldDefinition("doneQuantity", fieldTypeFactory.decimalType());
        FieldDefinition fieldEffectiveDateFrom = createFieldDefinition("effectiveDateFrom", fieldTypeFactory.dateType());
        FieldDefinition fieldEffectiveDateTo = createFieldDefinition("effectiveDateTo", fieldTypeFactory.dateType());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.ProductOrder");
        dataDefinition.addField(fieldNumber);
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldDateFrom);
        dataDefinition.addField(fieldDateTo);
        dataDefinition.addField(fieldState);
        dataDefinition.addField(fieldMachine);
        dataDefinition.addField(fieldProduct);
        dataDefinition.addField(fieldDefaultInstruction);
        dataDefinition.addField(fieldInstruction);
        dataDefinition.addField(fieldPlannedQuantity);
        dataDefinition.addField(fieldDoneQuantity);
        dataDefinition.addField(fieldEffectiveDateFrom);
        dataDefinition.addField(fieldEffectiveDateTo);

        return dataDefinition;
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
