package com.qcadoo.mes.core.data.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Dictionary;
import com.qcadoo.mes.core.data.beans.DictionaryItem;
import com.qcadoo.mes.core.data.beans.TestBeanA;
import com.qcadoo.mes.core.data.beans.TestBeanB;
import com.qcadoo.mes.core.data.beans.TestBeanC;
import com.qcadoo.mes.core.data.internal.hooks.HookFactory;
import com.qcadoo.mes.core.data.internal.model.DataDefinitionImpl;
import com.qcadoo.mes.core.data.internal.model.FieldDefinitionImpl;
import com.qcadoo.mes.core.data.model.DataDefinition;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.FieldValidatorFactory;

@Service
public final class DataDefinitionServiceImpl implements DataDefinitionService {

    @Autowired
    private DataAccessService dataAccessService;

    @Autowired
    private FieldTypeFactory fieldTypeFactory;

    @Autowired
    private HookFactory hookFactory;

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
        } else if ("products.order".equals(entityName)) {
            dataDefinition = createOrderDefinition();
        } else if ("products.instruction".equals(entityName)) {
            dataDefinition = createInstructionDefinition();
        } else if ("core.dictionary".equals(entityName)) {
            dataDefinition = createDictionaryDefinition();
        } else if ("core.dictionaryItem".equals(entityName)) {
            dataDefinition = createDictionaryItemDefinition();
        } else if ("test.testBeanA".equals(entityName)) {
            dataDefinition = createTestBeanAItemDefinition();
        } else if ("test.testBeanB".equals(entityName)) {
            dataDefinition = createTestBeanBItemDefinition();
        } else if ("test.testBeanC".equals(entityName)) {
            dataDefinition = createTestBeanCItemDefinition();
        } else if ("plugins.plugin".equals(entityName)) {
            dataDefinition = createPluginDefinition();
        }

        checkNotNull(dataDefinition, "data definition for %s cannot be found", entityName);

        return dataDefinition;
    }

    private DataDefinition createTestBeanAItemDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("test.testBeanA", dataAccessService);
        dataDefinition.setFullyQualifiedClassName(TestBeanA.class.getCanonicalName());

        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required()).withValidator(fieldValidationFactory.length(5));
        FieldDefinitionImpl fieldDescription = createFieldDefinition("description", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldBeanB = createFieldDefinition("beanB",
                fieldTypeFactory.eagerBelongsToType("test.testBeanB", "name"));
        FieldDefinitionImpl fieldBeansC = createFieldDefinition("beansC", fieldTypeFactory.hasManyType("test.testBeanC", "beanA"));

        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldDescription);
        dataDefinition.addField(fieldBeanB);
        dataDefinition.addField(fieldBeansC);

        return dataDefinition;
    }

    private DataDefinition createTestBeanBItemDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("test.testBeanB", dataAccessService);
        dataDefinition.setFullyQualifiedClassName(TestBeanB.class.getCanonicalName());

        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldDescription = createFieldDefinition("description", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldBeansA = createFieldDefinition("beansA", fieldTypeFactory.hasManyType("test.testBeanA", "beanB"));
        FieldDefinitionImpl fieldBeanC = createFieldDefinition("beanC",
                fieldTypeFactory.eagerBelongsToType("test.testBeanC", "name"));

        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldDescription);
        dataDefinition.addField(fieldBeansA);
        dataDefinition.addField(fieldBeanC);

        return dataDefinition;
    }

    private DataDefinition createTestBeanCItemDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("test.testBeanC", dataAccessService);
        dataDefinition.setFullyQualifiedClassName(TestBeanC.class.getCanonicalName());

        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldDescription = createFieldDefinition("description", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldBeanA = createFieldDefinition("beanA",
                fieldTypeFactory.eagerBelongsToType("test.testBeanA", "name"));
        FieldDefinitionImpl fieldBeansB = createFieldDefinition("beansB", fieldTypeFactory.hasManyType("test.testBeanB", "beanC"));

        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldDescription);
        dataDefinition.addField(fieldBeanA);
        dataDefinition.addField(fieldBeansB);

        return dataDefinition;
    }

    private DataDefinition createProductDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("products.product", dataAccessService);

        FieldDefinitionImpl fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.textType()).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldTypeOfMaterial = createFieldDefinition("typeOfMaterial",
                fieldTypeFactory.enumType("product", "intermediate", "component")).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldEan = createFieldDefinition("ean", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldCategory = createFieldDefinition("category", fieldTypeFactory.dictionaryType("categories"));
        FieldDefinitionImpl fieldUnit = createFieldDefinition("unit", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldSubstitutes = createFieldDefinition("substitutes",
                fieldTypeFactory.hasManyType("products.substitute", "product"));

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.products.Product");
        dataDefinition.addField(fieldNumber);
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldTypeOfMaterial);
        dataDefinition.addField(fieldEan);
        dataDefinition.addField(fieldCategory);
        dataDefinition.addField(fieldUnit);
        dataDefinition.addField(fieldSubstitutes);

        return dataDefinition;
    }

    private DataDefinition createSubstituteDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("products.substitute", dataAccessService);

        FieldDefinitionImpl fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.textType()).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldEffectiveDateFrom = createFieldDefinition("effectiveDateFrom", fieldTypeFactory.dateTimeType());
        FieldDefinitionImpl fieldEffectiveDateTo = createFieldDefinition("effectiveDateTo", fieldTypeFactory.dateTimeType());
        FieldDefinitionImpl fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products.product", "name")).withValidator(fieldValidationFactory.required());
        FieldDefinitionImpl fieldComponents = createFieldDefinition("components",
                fieldTypeFactory.hasManyType("products.substituteComponent", "substitute"));

        FieldDefinitionImpl fieldPriority = createFieldDefinition("priority", fieldTypeFactory.priorityType(fieldProduct))
                .readOnly();

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.products.Substitute");
        dataDefinition.addField(fieldNumber);
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldEffectiveDateFrom);
        dataDefinition.addField(fieldEffectiveDateTo);
        dataDefinition.addField(fieldProduct);
        dataDefinition.addField(fieldComponents);

        dataDefinition.setPriorityField(fieldPriority);

        dataDefinition.addValidator(fieldValidationFactory.customEntity("com.qcadoo.mes.products.ProductService",
                "checkSubstituteDates").customErrorMessage("products.validation.error.datesOrder"));

        return dataDefinition;
    }

    private DataDefinition createSubstituteComponentDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("products.substituteComponent", dataAccessService);

        FieldDefinitionImpl fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products.product", "name")).withValidator(fieldValidationFactory.required());
        FieldDefinitionImpl fieldSubstitute = createFieldDefinition("substitute",
                fieldTypeFactory.eagerBelongsToType("products.substitute", "name")).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldQuantity = createFieldDefinition("quantity", fieldTypeFactory.decimalType()).withValidator(
                fieldValidationFactory.required());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.products.SubstituteComponent");
        dataDefinition.addField(fieldProduct);
        dataDefinition.addField(fieldSubstitute);
        dataDefinition.addField(fieldQuantity);

        return dataDefinition;
    }

    private DataDefinition createUserDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("users.user", dataAccessService);

        FieldDefinitionImpl fieldUserName = createFieldDefinition("userName", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required()).withValidator(fieldValidationFactory.unique());
        FieldDefinitionImpl fieldUserGroup = createFieldDefinition("userGroup",
                fieldTypeFactory.eagerBelongsToType("users.group", "name")).withValidator(fieldValidationFactory.required());
        FieldDefinitionImpl fieldEmail = createFieldDefinition("email", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldFirstName = createFieldDefinition("firstName", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldLastName = createFieldDefinition("lastName", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType());
        FieldDefinitionImpl fieldPassword = createFieldDefinition("password", fieldTypeFactory.passwordType()).withValidator(
                fieldValidationFactory.requiredOnCreate());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.users.Users");

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
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("users.group", dataAccessService);

        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.stringType())
                .withValidator(fieldValidationFactory.requiredOnCreate()).withValidator(fieldValidationFactory.unique())
                .readOnly();
        FieldDefinitionImpl fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType());
        FieldDefinitionImpl fieldRole = createFieldDefinition("role", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.requiredOnCreate()).readOnly();

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.users.Groups");
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldDescription);
        dataDefinition.addField(fieldRole);

        return dataDefinition;
    }

    private DataDefinition createInstructionDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("products.instruction", dataAccessService);

        FieldDefinitionImpl fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.textType()).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products.product", "name")).withValidator(fieldValidationFactory.required());
        FieldDefinitionImpl fieldTypeOfMaterial = createFieldDefinition("typeOfMaterial",
                fieldTypeFactory.enumType("product", "intermediate", "component")).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldMaster = createFieldDefinition("master", fieldTypeFactory.booleanType());
        FieldDefinitionImpl fieldDateFrom = createFieldDefinition("dateFrom", fieldTypeFactory.dateTimeType());
        FieldDefinitionImpl fieldDateTo = createFieldDefinition("dateTo", fieldTypeFactory.dateTimeType());
        FieldDefinitionImpl fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.products.ProductInstruction");

        dataDefinition.addField(fieldNumber);
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldProduct);
        dataDefinition.addField(fieldTypeOfMaterial);
        dataDefinition.addField(fieldMaster);
        dataDefinition.addField(fieldDateFrom);
        dataDefinition.addField(fieldDateTo);
        dataDefinition.addField(fieldDescription);

        dataDefinition.addValidator(fieldValidationFactory.customEntity("com.qcadoo.mes.products.ProductService",
                "checkInstructionDefault").customErrorMessage("products.validation.error.default"));
        dataDefinition.addValidator(fieldValidationFactory.customEntity("com.qcadoo.mes.products.ProductService",
                "checkInstructionDates").customErrorMessage("products.validation.error.datesOrder"));

        return dataDefinition;
    }

    private DataDefinition createOrderDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("products.order", dataAccessService);

        FieldDefinitionImpl fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required()).withValidator(fieldValidationFactory.unique());
        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.textType()).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldDateFrom = createFieldDefinition("dateFrom", fieldTypeFactory.dateType()).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldDateTo = createFieldDefinition("dateTo", fieldTypeFactory.dateType()).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldState = createFieldDefinition("state", fieldTypeFactory.enumType("pending", "done"))
                .withValidator(fieldValidationFactory.required());
        FieldDefinitionImpl fieldMachine = createFieldDefinition("machine", fieldTypeFactory.enumType("Maszyna 1", "Maszyna 2"));
        FieldDefinitionImpl fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products.product", "name"));
        FieldDefinitionImpl fieldDefaultInstruction = createFieldDefinition("defaultInstruction",
                fieldTypeFactory.eagerBelongsToType("products.instruction", "name")).readOnly();
        FieldDefinitionImpl fieldInstruction = createFieldDefinition("instruction",
                fieldTypeFactory.eagerBelongsToType("products.instruction", "name"));
        FieldDefinitionImpl fieldPlannedQuantity = createFieldDefinition("plannedQuantity", fieldTypeFactory.decimalType());
        FieldDefinitionImpl fieldDoneQuantity = createFieldDefinition("doneQuantity", fieldTypeFactory.decimalType());
        FieldDefinitionImpl fieldEffectiveDateFrom = createFieldDefinition("effectiveDateFrom", fieldTypeFactory.dateType())
                .readOnly();
        FieldDefinitionImpl fieldEffectiveDateTo = createFieldDefinition("effectiveDateTo", fieldTypeFactory.dateType())
                .readOnly();
        FieldDefinitionImpl fieldStartWorker = createFieldDefinition("startWorker", fieldTypeFactory.textType()).readOnly();
        FieldDefinitionImpl fieldEndWorker = createFieldDefinition("endWorker", fieldTypeFactory.textType()).readOnly();

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.products.ProductOrder");
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

        dataDefinition.addValidator(fieldValidationFactory.customEntity("com.qcadoo.mes.products.ProductService",
                "checkOrderDates").customErrorMessage("products.validation.error.datesOrder"));
        dataDefinition.setSaveHook(hookFactory.getHook("com.qcadoo.mes.products.ProductService", "fillOrderDatesAndWorkers"));

        return dataDefinition;
    }

    private DataDefinition createDictionaryDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("core.dictionary", dataAccessService);
        dataDefinition.setFullyQualifiedClassName(Dictionary.class.getCanonicalName());
        dataDefinition.setDeletable(false);

        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.textType()).withValidator(
                fieldValidationFactory.required());

        dataDefinition.addField(fieldName);
        return dataDefinition;
    }

    private DataDefinition createDictionaryItemDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("core.dictionaryItem", dataAccessService);
        dataDefinition.setFullyQualifiedClassName(DictionaryItem.class.getCanonicalName());

        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType());
        FieldDefinitionImpl fieldDictionary = createFieldDefinition("dictionary",
                fieldTypeFactory.eagerBelongsToType("core.dictionary", "name"));

        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldDescription);
        dataDefinition.addField(fieldDictionary);
        return dataDefinition;
    }

    private DataDefinition createPluginDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("plugins.plugin", dataAccessService);

        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.requiredOnCreate()).readOnly();
        FieldDefinitionImpl fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType()).readOnly();
        FieldDefinitionImpl fieldPublisher = createFieldDefinition("publisher", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.requiredOnCreate()).readOnly();
        FieldDefinitionImpl fieldVersion = createFieldDefinition("version", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.requiredOnCreate()).readOnly();
        FieldDefinitionImpl fieldStatus = createFieldDefinition("status",
                fieldTypeFactory.enumType("downloaded", "installed", "active")).withValidator(
                fieldValidationFactory.requiredOnCreate());
        FieldDefinitionImpl fieldBase = createFieldDefinition("base", fieldTypeFactory.booleanType()).withValidator(
                fieldValidationFactory.requiredOnCreate()).readOnly();
        FieldDefinitionImpl fieldIdentifier = createFieldDefinition("identifier", fieldTypeFactory.stringType()).readOnly();
        FieldDefinitionImpl fieldPackageName = createFieldDefinition("packageName", fieldTypeFactory.stringType()).readOnly();
        FieldDefinitionImpl fieldFileName = createFieldDefinition("fileName", fieldTypeFactory.stringType()).readOnly();

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.plugins.Plugin");
        dataDefinition.addField(fieldName);
        dataDefinition.addField(fieldDescription);
        dataDefinition.addField(fieldVersion);
        dataDefinition.addField(fieldPublisher);
        dataDefinition.addField(fieldStatus);
        dataDefinition.addField(fieldBase);
        dataDefinition.addField(fieldIdentifier);
        dataDefinition.addField(fieldPackageName);
        dataDefinition.addField(fieldFileName);

        return dataDefinition;
    }

    private FieldDefinitionImpl createFieldDefinition(final String name, final FieldType type) {
        return new FieldDefinitionImpl(name).withType(type);
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
