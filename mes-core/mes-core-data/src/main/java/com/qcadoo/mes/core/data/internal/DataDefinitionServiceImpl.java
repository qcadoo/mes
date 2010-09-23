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
import com.qcadoo.mes.core.data.validation.ValidatorFactory;

@Service
public final class DataDefinitionServiceImpl implements DataDefinitionService {

    @Autowired
    private DataAccessService dataAccessService;

    @Autowired
    private FieldTypeFactory fieldTypeFactory;

    @Autowired
    private HookFactory hookFactory;

    @Autowired
    private ValidatorFactory fieldValidationFactory;

    @Override
    public void save(final DataDefinition dataDefinition) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public DataDefinition get(final String pluginIdentifier, final String entityName) {
        DataDefinition dataDefinition = null;
        if ("products".equals(pluginIdentifier) && "product".equals(entityName)) {
            dataDefinition = createProductDefinition();
        } else if ("products".equals(pluginIdentifier) && "substitute".equals(entityName)) {
            dataDefinition = createSubstituteDefinition();
        } else if ("products".equals(pluginIdentifier) && "substituteComponent".equals(entityName)) {
            dataDefinition = createSubstituteComponentDefinition();
        } else if ("users".equals(pluginIdentifier) && "user".equals(entityName)) {
            dataDefinition = createUserDefinition();
        } else if ("users".equals(pluginIdentifier) && "group".equals(entityName)) {
            dataDefinition = createUserGroupDefinition();
        } else if ("products".equals(pluginIdentifier) && "order".equals(entityName)) {
            dataDefinition = createOrderDefinition();
        } else if ("products".equals(pluginIdentifier) && "instruction".equals(entityName)) {
            dataDefinition = createInstructionDefinition();
        } else if ("core".equals(pluginIdentifier) && "dictionary".equals(entityName)) {
            dataDefinition = createDictionaryDefinition();
        } else if ("core".equals(pluginIdentifier) && "dictionaryItem".equals(entityName)) {
            dataDefinition = createDictionaryItemDefinition();
        } else if ("test".equals(pluginIdentifier) && "testBeanA".equals(entityName)) {
            dataDefinition = createTestBeanAItemDefinition();
        } else if ("test".equals(pluginIdentifier) && "testBeanB".equals(entityName)) {
            dataDefinition = createTestBeanBItemDefinition();
        } else if ("test".equals(pluginIdentifier) && "testBeanC".equals(entityName)) {
            dataDefinition = createTestBeanCItemDefinition();
        } else if ("plugins".equals(pluginIdentifier) && "plugin".equals(entityName)) {
            dataDefinition = createPluginDefinition();
        }

        checkNotNull(dataDefinition, "data definition for %s cannot be found", entityName);

        return dataDefinition;
    }

    private DataDefinition createTestBeanAItemDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("test", "testBeanA", dataAccessService);
        dataDefinition.setFullyQualifiedClassName(TestBeanA.class.getCanonicalName());

        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required()).withValidator(fieldValidationFactory.length(null, null, 5));
        FieldDefinitionImpl fieldActive = createFieldDefinition("active", fieldTypeFactory.booleanType());
        FieldDefinitionImpl fieldDescription = createFieldDefinition("description", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldBeanB = createFieldDefinition("beanB",
                fieldTypeFactory.eagerBelongsToType("test", "testBeanB", "name"));
        FieldDefinitionImpl fieldBeanA = createFieldDefinition("beanA",
                fieldTypeFactory.eagerBelongsToType("test", "testBeanA", "name"));
        FieldDefinitionImpl fieldBeansC = createFieldDefinition("beansC",
                fieldTypeFactory.hasManyType("test", "testBeanC", "beanA"));

        dataDefinition.withField(fieldName);
        dataDefinition.withField(fieldActive);
        dataDefinition.withField(fieldDescription);
        dataDefinition.withField(fieldBeanB);
        dataDefinition.withField(fieldBeanA);
        dataDefinition.withField(fieldBeansC);

        return dataDefinition;
    }

    private DataDefinition createTestBeanBItemDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("test", "testBeanB", dataAccessService);
        dataDefinition.setFullyQualifiedClassName(TestBeanB.class.getCanonicalName());

        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldDescription = createFieldDefinition("description", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldBeansA = createFieldDefinition("beansA",
                fieldTypeFactory.hasManyType("test", "testBeanA", "beanB"));
        FieldDefinitionImpl fieldBeanC = createFieldDefinition("beanC",
                fieldTypeFactory.eagerBelongsToType("test", "testBeanC", "name"));

        dataDefinition.withField(fieldName);
        dataDefinition.withField(fieldDescription);
        dataDefinition.withField(fieldBeansA);
        dataDefinition.withField(fieldBeanC);

        return dataDefinition;
    }

    private DataDefinition createTestBeanCItemDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("test", "testBeanC", dataAccessService);
        dataDefinition.setFullyQualifiedClassName(TestBeanC.class.getCanonicalName());

        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldDescription = createFieldDefinition("description", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldBeanA = createFieldDefinition("beanA",
                fieldTypeFactory.eagerBelongsToType("test", "testBeanA", "name"));
        FieldDefinitionImpl fieldBeansB = createFieldDefinition("beansB",
                fieldTypeFactory.hasManyType("test", "testBeanB", "beanC"));

        dataDefinition.withField(fieldName);
        dataDefinition.withField(fieldDescription);
        dataDefinition.withField(fieldBeanA);
        dataDefinition.withField(fieldBeansB);

        return dataDefinition;
    }

    private DataDefinition createProductDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("products", "product", dataAccessService);

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
                fieldTypeFactory.hasManyType("products", "substitute", "product"));

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.products.Product");
        dataDefinition.withField(fieldNumber);
        dataDefinition.withField(fieldName);
        dataDefinition.withField(fieldTypeOfMaterial);
        dataDefinition.withField(fieldEan);
        dataDefinition.withField(fieldCategory);
        dataDefinition.withField(fieldUnit);
        dataDefinition.withField(fieldSubstitutes);

        return dataDefinition;
    }

    private DataDefinition createSubstituteDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("products", "substitute", dataAccessService);

        FieldDefinitionImpl fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.textType()).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldEffectiveDateFrom = createFieldDefinition("effectiveDateFrom", fieldTypeFactory.dateTimeType());
        FieldDefinitionImpl fieldEffectiveDateTo = createFieldDefinition("effectiveDateTo", fieldTypeFactory.dateTimeType());
        FieldDefinitionImpl fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products", "product", "name")).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldComponents = createFieldDefinition("components",
                fieldTypeFactory.hasManyType("products", "substituteComponent", "substitute"));

        FieldDefinitionImpl fieldPriority = createFieldDefinition("priority", fieldTypeFactory.priorityType(fieldProduct))
                .withReadOnly(true);

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.products.Substitute");
        dataDefinition.withField(fieldNumber);
        dataDefinition.withField(fieldName);
        dataDefinition.withField(fieldEffectiveDateFrom);
        dataDefinition.withField(fieldEffectiveDateTo);
        dataDefinition.withField(fieldProduct);
        dataDefinition.withField(fieldComponents);

        dataDefinition.withPriorityField(fieldPriority);

        dataDefinition.withValidator(fieldValidationFactory.customEntity(
                hookFactory.getHook("com.qcadoo.mes.products.ProductService", "checkSubstituteDates")).customErrorMessage(
                "products.validation.error.datesOrder"));

        return dataDefinition;
    }

    private DataDefinition createSubstituteComponentDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("products", "substituteComponent", dataAccessService);

        FieldDefinitionImpl fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products", "product", "name")).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldSubstitute = createFieldDefinition("substitute",
                fieldTypeFactory.eagerBelongsToType("products", "substitute", "name")).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldQuantity = createFieldDefinition("quantity", fieldTypeFactory.decimalType()).withValidator(
                fieldValidationFactory.required());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.products.SubstituteComponent");
        dataDefinition.withField(fieldProduct);
        dataDefinition.withField(fieldSubstitute);
        dataDefinition.withField(fieldQuantity);

        return dataDefinition;
    }

    private DataDefinition createUserDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("users", "user", dataAccessService);

        FieldDefinitionImpl fieldUserName = createFieldDefinition("userName", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required()).withValidator(fieldValidationFactory.unique());
        FieldDefinitionImpl fieldUserGroup = createFieldDefinition("userGroup",
                fieldTypeFactory.eagerBelongsToType("users", "group", "name")).withValidator(fieldValidationFactory.required());
        FieldDefinitionImpl fieldEmail = createFieldDefinition("email", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldFirstName = createFieldDefinition("firstName", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldLastName = createFieldDefinition("lastName", fieldTypeFactory.stringType());
        FieldDefinitionImpl fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType());
        FieldDefinitionImpl fieldPassword = createFieldDefinition("password", fieldTypeFactory.passwordType()).withValidator(
                fieldValidationFactory.requiredOnCreate());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.users.Users");

        dataDefinition.withField(fieldUserName);
        dataDefinition.withField(fieldUserGroup);
        dataDefinition.withField(fieldEmail);
        dataDefinition.withField(fieldFirstName);
        dataDefinition.withField(fieldLastName);
        dataDefinition.withField(fieldDescription);
        dataDefinition.withField(fieldPassword);

        return dataDefinition;
    }

    private DataDefinition createUserGroupDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("users", "group", dataAccessService);

        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.stringType())
                .withValidator(fieldValidationFactory.requiredOnCreate()).withValidator(fieldValidationFactory.unique())
                .withReadOnly(true);
        FieldDefinitionImpl fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType());
        FieldDefinitionImpl fieldRole = createFieldDefinition("role", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.requiredOnCreate()).withReadOnly(true);

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.users.Groups");
        dataDefinition.withField(fieldName);
        dataDefinition.withField(fieldDescription);
        dataDefinition.withField(fieldRole);

        return dataDefinition;
    }

    private DataDefinition createInstructionDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("products", "instruction", dataAccessService);

        FieldDefinitionImpl fieldNumber = createFieldDefinition("number", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.textType()).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldProduct = createFieldDefinition("product",
                fieldTypeFactory.eagerBelongsToType("products", "product", "name")).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldTypeOfMaterial = createFieldDefinition("typeOfMaterial",
                fieldTypeFactory.enumType("product", "intermediate", "component")).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldMaster = createFieldDefinition("master", fieldTypeFactory.booleanType());
        FieldDefinitionImpl fieldDateFrom = createFieldDefinition("dateFrom", fieldTypeFactory.dateTimeType());
        FieldDefinitionImpl fieldDateTo = createFieldDefinition("dateTo", fieldTypeFactory.dateTimeType());
        FieldDefinitionImpl fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType());

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.products.ProductInstruction");

        dataDefinition.withField(fieldNumber);
        dataDefinition.withField(fieldName);
        dataDefinition.withField(fieldProduct);
        dataDefinition.withField(fieldTypeOfMaterial);
        dataDefinition.withField(fieldMaster);
        dataDefinition.withField(fieldDateFrom);
        dataDefinition.withField(fieldDateTo);
        dataDefinition.withField(fieldDescription);

        dataDefinition.withValidator(fieldValidationFactory.customEntity(
                hookFactory.getHook("com.qcadoo.mes.products.ProductService", "checkInstructionDefault")).customErrorMessage(
                "products.validation.error.default"));
        dataDefinition.withValidator(fieldValidationFactory.customEntity(
                hookFactory.getHook("com.qcadoo.mes.products.ProductService", "checkInstructionDates")).customErrorMessage(
                "products.validation.error.datesOrder"));

        return dataDefinition;
    }

    private DataDefinition createOrderDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("products", "order", dataAccessService);

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
                fieldTypeFactory.eagerBelongsToType("products", "product", "name"));
        FieldDefinitionImpl fieldDefaultInstruction = createFieldDefinition("defaultInstruction",
                fieldTypeFactory.eagerBelongsToType("products", "instruction", "name")).withReadOnly(true);
        FieldDefinitionImpl fieldInstruction = createFieldDefinition("instruction",
                fieldTypeFactory.eagerBelongsToType("products", "instruction", "name"));
        FieldDefinitionImpl fieldPlannedQuantity = createFieldDefinition("plannedQuantity", fieldTypeFactory.decimalType());
        FieldDefinitionImpl fieldDoneQuantity = createFieldDefinition("doneQuantity", fieldTypeFactory.decimalType());
        FieldDefinitionImpl fieldEffectiveDateFrom = createFieldDefinition("effectiveDateFrom", fieldTypeFactory.dateType())
                .withReadOnly(true);
        FieldDefinitionImpl fieldEffectiveDateTo = createFieldDefinition("effectiveDateTo", fieldTypeFactory.dateType())
                .withReadOnly(true);
        FieldDefinitionImpl fieldStartWorker = createFieldDefinition("startWorker", fieldTypeFactory.textType()).withReadOnly(
                true);
        FieldDefinitionImpl fieldEndWorker = createFieldDefinition("endWorker", fieldTypeFactory.textType()).withReadOnly(true);

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.plugins.beans.products.ProductOrder");
        dataDefinition.withField(fieldNumber);
        dataDefinition.withField(fieldName);
        dataDefinition.withField(fieldDateFrom);
        dataDefinition.withField(fieldDateTo);
        dataDefinition.withField(fieldState);
        dataDefinition.withField(fieldMachine);
        dataDefinition.withField(fieldProduct);
        dataDefinition.withField(fieldDefaultInstruction);
        dataDefinition.withField(fieldInstruction);
        dataDefinition.withField(fieldPlannedQuantity);
        dataDefinition.withField(fieldDoneQuantity);
        dataDefinition.withField(fieldEffectiveDateFrom);
        dataDefinition.withField(fieldEffectiveDateTo);
        dataDefinition.withField(fieldStartWorker);
        dataDefinition.withField(fieldEndWorker);

        dataDefinition.withValidator(fieldValidationFactory.customEntity(
                hookFactory.getHook("com.qcadoo.mes.products.ProductService", "checkOrderDates")).customErrorMessage(
                "products.validation.error.datesOrder"));
        dataDefinition.withSaveHook(hookFactory.getHook("com.qcadoo.mes.products.ProductService", "fillOrderDatesAndWorkers"));

        return dataDefinition;
    }

    private DataDefinition createDictionaryDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("core", "dictionary", dataAccessService);
        dataDefinition.setFullyQualifiedClassName(Dictionary.class.getCanonicalName());
        dataDefinition.setDeletable(false);

        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.textType()).withValidator(
                fieldValidationFactory.required());

        dataDefinition.withField(fieldName);
        return dataDefinition;
    }

    private DataDefinition createDictionaryItemDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("core", "dictionaryItem", dataAccessService);
        dataDefinition.setFullyQualifiedClassName(DictionaryItem.class.getCanonicalName());

        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.required());
        FieldDefinitionImpl fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType());
        FieldDefinitionImpl fieldDictionary = createFieldDefinition("dictionary",
                fieldTypeFactory.eagerBelongsToType("core", "dictionary", "name"));

        dataDefinition.withField(fieldName);
        dataDefinition.withField(fieldDescription);
        dataDefinition.withField(fieldDictionary);
        return dataDefinition;
    }

    private DataDefinition createPluginDefinition() {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("plugins", "plugin", dataAccessService);

        FieldDefinitionImpl fieldName = createFieldDefinition("name", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.requiredOnCreate()).withReadOnly(true);
        FieldDefinitionImpl fieldDescription = createFieldDefinition("description", fieldTypeFactory.textType()).withReadOnly(
                true);
        FieldDefinitionImpl fieldVendor = createFieldDefinition("vendor", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.requiredOnCreate()).withReadOnly(true);
        FieldDefinitionImpl fieldVersion = createFieldDefinition("version", fieldTypeFactory.stringType()).withValidator(
                fieldValidationFactory.requiredOnCreate()).withReadOnly(true);
        FieldDefinitionImpl fieldStatus = createFieldDefinition("status",
                fieldTypeFactory.enumType("downloaded", "installed", "active")).withValidator(
                fieldValidationFactory.requiredOnCreate());
        FieldDefinitionImpl fieldBase = createFieldDefinition("base", fieldTypeFactory.booleanType()).withValidator(
                fieldValidationFactory.requiredOnCreate()).withReadOnly(true);
        FieldDefinitionImpl fieldIdentifier = createFieldDefinition("identifier", fieldTypeFactory.stringType()).withReadOnly(
                true);
        FieldDefinitionImpl fieldPackageName = createFieldDefinition("packageName", fieldTypeFactory.stringType()).withReadOnly(
                true);
        FieldDefinitionImpl fieldFileName = createFieldDefinition("fileName", fieldTypeFactory.stringType()).withReadOnly(true);

        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.core.data.beans.Plugin");
        dataDefinition.withField(fieldName);
        dataDefinition.withField(fieldDescription);
        dataDefinition.withField(fieldVersion);
        dataDefinition.withField(fieldVendor);
        dataDefinition.withField(fieldStatus);
        dataDefinition.withField(fieldBase);
        dataDefinition.withField(fieldIdentifier);
        dataDefinition.withField(fieldPackageName);
        dataDefinition.withField(fieldFileName);

        return dataDefinition;
    }

    private FieldDefinitionImpl createFieldDefinition(final String name, final FieldType type) {
        return new FieldDefinitionImpl(name).withType(type);
    }

    @Override
    public void delete(final String pluginIdentifier, final String entityName) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public List<DataDefinition> list() {
        throw new UnsupportedOperationException("implement me");
    }

}
