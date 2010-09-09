package com.qcadoo.mes.core.data.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.internal.callbacks.CallbackFactory;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.FieldValidatorFactory;

@Service
public final class DataDefinitionServiceImpl implements DataDefinitionService {

    @Autowired
    private FieldTypeFactory fieldTypeFactory;

    @Autowired
    private CallbackFactory callbackFactory;

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
        } else if ("core.dictionary".equals(entityName)) {
            dataDefinition = createDictionaryDefinition();
        } else if ("core.dictionaryItem".equals(entityName)) {
            dataDefinition = createDictionaryItemDefinition();
        }

        checkNotNull(dataDefinition, "data definition for %s cannot be found", entityName);

        return dataDefinition;
    }

    private DataDefinition createProductDefinition() {
        DataDefinition dataDefinition = new DataDefinition("products.product");

        DataFieldDefinition fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.textType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldTypeOfMaterial = createFieldDefinition("typeOfMaterial",
                fieldTypeFactory.enumType("product", "intermediate", "component")).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldEan = createFieldDefinition("ean", fieldTypeFactory.stringType());
        DataFieldDefinition fieldCategory = createFieldDefinition("category", fieldTypeFactory.dictionaryType("categories"));
        DataFieldDefinition fieldUnit = createFieldDefinition("unit", fieldTypeFactory.stringType());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.Product");
        dataDefinition.addField(fieldNumber);
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldTypeOfMaterial);
        dataDefinition.addField(fieldEan);
        dataDefinition.addField(fieldCategory);
        dataDefinition.addField(fieldUnit);

        return dataDefinition;
    }

    private DataDefinition createSubstituteDefinition() {
        DataDefinition dataDefinition = new DataDefinition("products.substitute");

        DataFieldDefinition fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.textType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldEffectiveDateFrom = createFieldDefinition("effectiveDateFrom", fieldTypeFactory.dateTimeType());
        DataFieldDefinition fieldEffectiveDateTo = createFieldDefinition("effectiveDateTo", fieldTypeFactory.dateTimeType());
        DataFieldDefinition fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products.product", "name")).withValidator(fieldValidationFactory.required());

        DataFieldDefinition fieldPriority = createFieldDefinition("priority", fieldTypeFactory.priorityType(fieldProduct))
                .readOnly();

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.Substitute");
        dataDefinition.addField(fieldNumber);
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldEffectiveDateFrom);
        dataDefinition.addField(fieldEffectiveDateTo);
        dataDefinition.addField(fieldProduct);

        dataDefinition.setPriorityField(fieldPriority);

        dataDefinition.setValidators(fieldValidationFactory.customEntity("productService", "checkSubstituteDates")
                .customErrorMessage("products.validation.error.datesOrder"));

        return dataDefinition;
    }

    private DataDefinition createSubstituteComponentDefinition() {
        DataDefinition dataDefinition = new DataDefinition("products.substituteComponent");

        DataFieldDefinition fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products.product", "name")).withValidator(fieldValidationFactory.required());
        DataFieldDefinition fieldSubstitute = createFieldDefinition("substitute",
                fieldTypeFactory.eagerBelongsToType("products.substitute", "name")).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldQuantity = createFieldDefinition("quantity", fieldTypeFactory.decimalType()).withValidator(
                fieldValidationFactory.required());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.SubstituteComponent");
        dataDefinition.addField(fieldProduct);
        dataDefinition.addField(fieldSubstitute);
        dataDefinition.addField(fieldQuantity);

        return dataDefinition;
    }

    private DataDefinition createUserDefinition() {
        DataDefinition dataDefinition = new DataDefinition("users.user");

        DataFieldDefinition fieldUserName = createFieldDefinition("userName", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required()).withValidator(fieldValidationFactory.unique());
        DataFieldDefinition fieldUserGroup = createFieldDefinition("userGroup",
                fieldTypeFactory.eagerBelongsToType("users.group", "name")).withValidator(fieldValidationFactory.required());
        DataFieldDefinition fieldEmail = createFieldDefinition("email", fieldTypeFactory.stringType());
        DataFieldDefinition fieldFirstName = createFieldDefinition("firstName", fieldTypeFactory.stringType());
        DataFieldDefinition fieldLastName = createFieldDefinition("lastName", fieldTypeFactory.stringType());
        DataFieldDefinition fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType());
        DataFieldDefinition fieldPassword = createFieldDefinition("password", fieldTypeFactory.passwordType()).withValidator(
                fieldValidationFactory.requiredOnCreate());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.Users");

        dataDefinition.addField(fieldUserName);
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

        DataFieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required()).withValidator(fieldValidationFactory.unique());
        DataFieldDefinition fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType());
        // TODO KRNA zamienic na relacje
        DataFieldDefinition fieldRole = createFieldDefinition("role", fieldTypeFactory.enumType("read", "write", "delete"));

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.UserGroup");
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldDescription);
        dataDefinition.addField(fieldRole);

        return dataDefinition;
    }

    private DataDefinition createOrderDefinition() {
        DataDefinition dataDefinition = new DataDefinition("orders.order");

        DataFieldDefinition fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required()).withValidator(fieldValidationFactory.unique());
        DataFieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.textType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldDateFrom = createFieldDefinition("dateFrom", fieldTypeFactory.dateType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldDateTo = createFieldDefinition("dateTo", fieldTypeFactory.dateType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldState = createFieldDefinition("state", fieldTypeFactory.enumType("pending", "done"));
        DataFieldDefinition fieldMachine = createFieldDefinition("machine", fieldTypeFactory.enumType("Maszyna 1", "Maszyna 2"));
        DataFieldDefinition fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products.product", "name"));
        DataFieldDefinition fieldDefaultInstruction = createFieldDefinition("defaultInstruction", fieldTypeFactory.textType())
                .readOnlyOnUpdate();
        DataFieldDefinition fieldInstruction = createFieldDefinition("instruction",
                fieldTypeFactory.enumType("Instrukcja 1", "Instrukcja 2"));
        DataFieldDefinition fieldPlannedQuantity = createFieldDefinition("plannedQuantity", fieldTypeFactory.decimalType());
        DataFieldDefinition fieldDoneQuantity = createFieldDefinition("doneQuantity", fieldTypeFactory.decimalType());
        DataFieldDefinition fieldEffectiveDateFrom = createFieldDefinition("effectiveDateFrom", fieldTypeFactory.dateType())
                .readOnly();
        DataFieldDefinition fieldEffectiveDateTo = createFieldDefinition("effectiveDateTo", fieldTypeFactory.dateType())
                .readOnly();
        DataFieldDefinition fieldStartWorker = createFieldDefinition("startWorker", fieldTypeFactory.textType()).readOnly();
        DataFieldDefinition fieldEndWorker = createFieldDefinition("endWorker", fieldTypeFactory.textType()).readOnly();

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
        dataDefinition.addField(fieldStartWorker);
        dataDefinition.addField(fieldEndWorker);

        dataDefinition.setValidators(fieldValidationFactory.customEntity("productService", "checkOrderDates").customErrorMessage(
                "products.validation.error.datesOrder"));
        dataDefinition.setOnSave(callbackFactory.getCallback("productService", "fillOrderDatesAndWorkers"));

        return dataDefinition;
    }

    private DataDefinition createDictionaryDefinition() {
        DataDefinition dataDefinition = new DataDefinition("core.dictionary");
        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.Dictionary");
        dataDefinition.setDeletable(false);

        DataFieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.textType()).withValidator(
                fieldValidationFactory.required());

        dataDefinition.addField(fieldName);
        return dataDefinition;
    }

    private DataDefinition createDictionaryItemDefinition() {
        DataDefinition dataDefinition = new DataDefinition("core.dictionaryItem");
        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.DictionaryItem");

        DataFieldDefinition fieldName = createFieldDefinition("name", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required());
        DataFieldDefinition fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType());
        DataFieldDefinition fieldDictionary = createFieldDefinition("dictionary",
                fieldTypeFactory.eagerBelongsToType("core.dictionary", "name"));

        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldDescription);
        dataDefinition.addField(fieldDictionary);
        return dataDefinition;
    }

    private DataFieldDefinition createFieldDefinition(final String name, final FieldType type) {
        return new DataFieldDefinition(name).withType(type);
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
