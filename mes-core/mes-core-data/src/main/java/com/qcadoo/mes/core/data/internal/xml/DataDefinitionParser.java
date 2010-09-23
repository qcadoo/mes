package com.qcadoo.mes.core.data.internal.xml;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.springframework.util.StringUtils.hasText;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.internal.hooks.HookFactory;
import com.qcadoo.mes.core.data.internal.model.DataDefinitionImpl;
import com.qcadoo.mes.core.data.internal.model.FieldDefinitionImpl;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.model.HookDefinition;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.ValidatorFactory;

@Service
public class DataDefinitionParser {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private DataAccessService dataAccessService;

    @Autowired
    private FieldTypeFactory fieldTypeFactory;

    @Autowired
    private HookFactory hookFactory;

    @Autowired
    private ValidatorFactory validatorFactory;

    public static void main(final String[] args) throws Exception {
        DataDefinitionParser ddp = new DataDefinitionParser();
        ddp.parse(new FileInputStream(new File("src/main/resources/data.xml")));
    }

    public void parse(final InputStream dataDefinitionInputStream) throws XMLStreamException, FactoryConfigurationError {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(dataDefinitionInputStream);

        while (reader.hasNext() && reader.next() > 0) {
            if (isTagStarted(reader, "plugin")) {
                getPluginDefinition(reader);
            } else if (isTagStarted(reader, "model")) {
                getModelDefinition(reader);
            } else if (isTagStarted(reader, "view")) {
                getViewDefinition(reader);
            }
        }

        reader.close();
    }

    private void getModelDefinition(final XMLStreamReader reader) throws XMLStreamException {
        DataDefinitionImpl dataDefinition = new DataDefinitionImpl("", getStringAttribute(reader, "name"), dataAccessService);
        dataDefinition.setDeletable(getBooleanAttribute(reader, "deletable"));

        // dataDefinition.setFullyQualifiedClassName(fullyQualifiedClassName)

        while (reader.hasNext() && reader.next() > 0) {
            if (isTagEnded(reader, "model")) {
                break;
            } else if (isTagStarted(reader, "priority")) {
                dataDefinition.withPriorityField(getPriorityFieldDefinition(reader, dataDefinition));
            } else if (isTagStarted(reader, "onCreate")) {
                dataDefinition.withCreateHook(getHookDefinition(reader));
            } else if (isTagStarted(reader, "onUpdate")) {
                dataDefinition.withUpdateHook(getHookDefinition(reader));
            } else if (isTagStarted(reader, "onSave")) {
                dataDefinition.withSaveHook(getHookDefinition(reader));
            } else if (isTagStarted(reader, "validates_with")) {
                dataDefinition.withValidator(validatorFactory.customEntity(getHookDefinition(reader)));
            } else if (isTagStarted(reader, "integer")) {
                dataDefinition.withField(getFieldDefinition(reader, fieldTypeFactory.integerType()));
            } else if (isTagStarted(reader, "string")) {
                dataDefinition.withField(getFieldDefinition(reader, fieldTypeFactory.stringType()));
            } else if (isTagStarted(reader, "text")) {
                dataDefinition.withField(getFieldDefinition(reader, fieldTypeFactory.textType()));
            } else if (isTagStarted(reader, "float")) {
                // TODO
            } else if (isTagStarted(reader, "decimal")) {
                dataDefinition.withField(getFieldDefinition(reader, fieldTypeFactory.decimalType()));
            } else if (isTagStarted(reader, "datetime")) {
                dataDefinition.withField(getFieldDefinition(reader, fieldTypeFactory.dateTimeType()));
            } else if (isTagStarted(reader, "timestamp")) {
                // TODO
            } else if (isTagStarted(reader, "time")) {
                // TODO
            } else if (isTagStarted(reader, "date")) {
                dataDefinition.withField(getFieldDefinition(reader, fieldTypeFactory.dateType()));
            } else if (isTagStarted(reader, "boolean")) {
                dataDefinition.withField(getFieldDefinition(reader, fieldTypeFactory.booleanType()));
            } else if (isTagStarted(reader, "belongsTo")) {
                dataDefinition.withField(getFieldDefinition(reader, getBelongsToType(reader)));
            } else if (isTagStarted(reader, "hasMany")) {
                dataDefinition.withField(getFieldDefinition(reader, getHasManyType(reader)));
            }
        }

        dataDefinitionService.save(dataDefinition);
    }

    private FieldType getHasManyType(final XMLStreamReader reader) {
        return fieldTypeFactory.hasManyType(getStringAttribute(reader, "plugin"), getStringAttribute(reader, "model"),
                getStringAttribute(reader, "joinField"));
    }

    private FieldType getBelongsToType(final XMLStreamReader reader) {
        boolean lazy = getBooleanAttribute(reader, "lazy");
        String pluginIdentifier = getStringAttribute(reader, "plugin");
        String modelName = getStringAttribute(reader, "model");
        String lookupFieldName = getStringAttribute(reader, "lookupField");

        if (lazy) {
            return fieldTypeFactory.lazyBelongsToType(pluginIdentifier, modelName, lookupFieldName);
        } else {
            return fieldTypeFactory.eagerBelongsToType(pluginIdentifier, modelName, lookupFieldName);
        }
    }

    private FieldDefinition getFieldDefinition(final XMLStreamReader reader, final FieldType integerType)
            throws XMLStreamException {
        String fieldType = reader.getLocalName();
        FieldDefinitionImpl fieldDefinition = new FieldDefinitionImpl(getStringAttribute(reader, "name"));
        fieldDefinition.withReadOnly(getBooleanAttribute(reader, "readonly"));
        fieldDefinition.withReadOnlyOnUpdate(getBooleanAttribute(reader, "readonly_on_create"));
        fieldDefinition.withDefaultValue(getStringAttribute(reader, "default"));

        while (reader.hasNext() && reader.next() > 0) {
            if (isTagEnded(reader, fieldType)) {
                break;
            } else if (isTagStarted(reader, "validates_presence")) {
                fieldDefinition.withValidator(validatorFactory.required());
            } else if (isTagStarted(reader, "validates_presence_on_create")) {
                fieldDefinition.withValidator(validatorFactory.requiredOnCreate());
            } else if (isTagStarted(reader, "validates_length")) {
                fieldDefinition.withValidator(validatorFactory.length(getIntegerAttribute(reader, "min"),
                        getIntegerAttribute(reader, "is"), getIntegerAttribute(reader, "max")));
            } else if (isTagStarted(reader, "validates_precision")) {
                fieldDefinition.withValidator(validatorFactory.precision(getIntegerAttribute(reader, "min"),
                        getIntegerAttribute(reader, "is"), getIntegerAttribute(reader, "max")));
            } else if (isTagStarted(reader, "validates_scale")) {
                fieldDefinition.withValidator(validatorFactory.scale(getIntegerAttribute(reader, "min"),
                        getIntegerAttribute(reader, "is"), getIntegerAttribute(reader, "max")));
            } else if (isTagStarted(reader, "validates_range")) {
                fieldDefinition.withValidator(validatorFactory.range(getIntegerAttribute(reader, "from"),
                        getIntegerAttribute(reader, "to")));
            } else if (isTagStarted(reader, "validates_uniqueness")) {
                fieldDefinition.withValidator(validatorFactory.unique());
                // TODO scope="lastname"
            } else if (isTagStarted(reader, "validates_format")) {
                // TODO with="d*a"
            } else if (isTagStarted(reader, "validates_with")) {
                fieldDefinition.withValidator(validatorFactory.custom(getHookDefinition(reader)));
            } else if (isTagStarted(reader, "validates_exclusion")) {
                // TODO <exclude>nowak</exclude> <exclude>kowalski</exclude>
            } else if (isTagStarted(reader, "validates_inclusion")) {
                // TODO <include>szczytowski</include>
            }
        }

        return fieldDefinition;
    }

    private HookDefinition getHookDefinition(final XMLStreamReader reader) {
        String fullyQualifiedClassName = getStringAttribute(reader, "bean");
        String methodName = getStringAttribute(reader, "method");
        checkState(hasText(fullyQualifiedClassName), "Hook bean name is required");
        checkState(hasText(methodName), "Hook method name is required");
        return hookFactory.getHook(fullyQualifiedClassName, methodName);
    }

    private FieldDefinition getPriorityFieldDefinition(final XMLStreamReader reader, final DataDefinitionImpl dataDefinition) {
        FieldDefinition scopedField = dataDefinition.getField(getStringAttribute(reader, "scope"));
        checkNotNull(scopedField, "Scoped field for priority is required");
        return new FieldDefinitionImpl(getStringAttribute(reader, "name")).withType(fieldTypeFactory.priorityType(scopedField))
                .withReadOnly(true);
    }

    private void getViewDefinition(final XMLStreamReader reader) {
        // TODO Auto-generated method stub
    }

    private String getPluginDefinition(final XMLStreamReader reader) {
        return getStringAttribute(reader, "name");

    }

    private Integer getIntegerAttribute(final XMLStreamReader reader, final String name) {
        String stringValue = reader.getAttributeValue(null, name);
        if (stringValue != null) {
            return Integer.valueOf(stringValue);
        } else {
            return null;
        }
    }

    private boolean getBooleanAttribute(final XMLStreamReader reader, final String name) {
        String stringValue = reader.getAttributeValue(null, name);
        if (stringValue != null) {
            return Boolean.valueOf(stringValue);
        } else {
            return false;
        }
    }

    private String getStringAttribute(final XMLStreamReader reader, final String name) {
        return reader.getAttributeValue(null, name);
    }

    private boolean isTagStarted(final XMLStreamReader reader, final String tagName) {
        return (reader.getEventType() == XMLStreamConstants.START_ELEMENT && tagName.equals(reader.getLocalName()));
    }

    private boolean isTagEnded(final XMLStreamReader reader, final String tagName) {
        return (reader.getEventType() == XMLStreamConstants.END_ELEMENT && tagName.equals(reader.getLocalName()));
    }

}
