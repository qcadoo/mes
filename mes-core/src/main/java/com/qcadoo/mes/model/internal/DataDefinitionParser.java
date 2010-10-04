package com.qcadoo.mes.model.internal;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.springframework.util.StringUtils.hasText;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.internal.DataAccessService;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.HookDefinition;
import com.qcadoo.mes.model.hooks.internal.HookFactory;
import com.qcadoo.mes.model.types.FieldType;
import com.qcadoo.mes.model.types.FieldTypeFactory;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.types.internal.DateTimeType;
import com.qcadoo.mes.model.types.internal.DateType;
import com.qcadoo.mes.model.types.internal.DecimalType;
import com.qcadoo.mes.model.types.internal.IntegerType;
import com.qcadoo.mes.model.validation.EntityValidator;
import com.qcadoo.mes.model.validation.FieldValidator;
import com.qcadoo.mes.model.validation.ValidatorFactory;
import com.qcadoo.mes.view.internal.ViewDefinitionParser;

@Service
public final class DataDefinitionParser {

    private static final Logger LOG = LoggerFactory.getLogger(DataDefinitionParser.class);

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

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ViewDefinitionParser viewDefinitionParser;

    public void parse() {
        LOG.info("Reading model definitions ...");

        try {
            Resource[] resources = applicationContext.getResources("classpath*:model.xml");
            for (Resource resource : resources) {
                parse(resource.getInputStream());
            }
        } catch (IOException e) {
            LOG.error("Cannot read data definition", e);
        }

        viewDefinitionParser.parse();
    }

    public void parse(final InputStream dataDefinitionInputStream) {
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(dataDefinitionInputStream);

            String pluginIdentifier = null;

            while (reader.hasNext() && reader.next() > 0) {
                if (isTagStarted(reader, "models")) {
                    pluginIdentifier = getPluginIdentifier(reader);
                } else if (isTagStarted(reader, "model")) {
                    getModelDefinition(reader, pluginIdentifier);
                }
            }

            reader.close();
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (FactoryConfigurationError e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void getModelDefinition(final XMLStreamReader reader, final String pluginIdentifier) throws XMLStreamException {
        String modelName = getStringAttribute(reader, "name");

        LOG.info("Reading model " + modelName + " for plugin " + pluginIdentifier);

        DataDefinitionImpl dataDefinition = new DataDefinitionImpl(pluginIdentifier, modelName, dataAccessService);
        dataDefinition.setDeletable(getBooleanAttribute(reader, "deletable", true));
        dataDefinition.setCreatable(getBooleanAttribute(reader, "creatable", true));
        dataDefinition.setUpdatable(getBooleanAttribute(reader, "updatable", true));
        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.beans." + pluginIdentifier + "."
                + StringUtils.capitalize(pluginIdentifier) + StringUtils.capitalize(modelName));

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
            } else if (isTagStarted(reader, "validatesWith")) {
                dataDefinition.withValidator(getEntityValidatorDefinition(reader));
            } else if (isTagStarted(reader, "integer")) {
                dataDefinition.withField(getFieldDefinition(reader, pluginIdentifier));
            } else if (isTagStarted(reader, "string")) {
                dataDefinition.withField(getFieldDefinition(reader, pluginIdentifier));
            } else if (isTagStarted(reader, "text")) {
                dataDefinition.withField(getFieldDefinition(reader, pluginIdentifier));
            } else if (isTagStarted(reader, "decimal")) {
                dataDefinition.withField(getFieldDefinition(reader, pluginIdentifier));
            } else if (isTagStarted(reader, "datetime")) {
                dataDefinition.withField(getFieldDefinition(reader, pluginIdentifier));
            } else if (isTagStarted(reader, "date")) {
                dataDefinition.withField(getFieldDefinition(reader, pluginIdentifier));
            } else if (isTagStarted(reader, "boolean")) {
                dataDefinition.withField(getFieldDefinition(reader, pluginIdentifier));
            } else if (isTagStarted(reader, "belongsTo")) {
                dataDefinition.withField(getFieldDefinition(reader, pluginIdentifier));
            } else if (isTagStarted(reader, "hasMany")) {
                dataDefinition.withField(getFieldDefinition(reader, pluginIdentifier));
            } else if (isTagStarted(reader, "enum")) {
                dataDefinition.withField(getFieldDefinition(reader, pluginIdentifier));
            } else if (isTagStarted(reader, "dictionary")) {
                dataDefinition.withField(getFieldDefinition(reader, pluginIdentifier));
            } else if (isTagStarted(reader, "password")) {
                dataDefinition.withField(getFieldDefinition(reader, pluginIdentifier));
            }

            // "time"
            // "timestamp"
            // "float"
            // "onLoad"
        }

        dataDefinitionService.save(dataDefinition);
    }

    private FieldType getDictionaryType(final XMLStreamReader reader) {
        String dictionaryName = getStringAttribute(reader, "dictionary");
        checkState(hasText(dictionaryName), "Dictionary name is required");
        return fieldTypeFactory.dictionaryType(dictionaryName);
    }

    private FieldType getEnumType(final XMLStreamReader reader) throws XMLStreamException {
        String values = getStringAttribute(reader, "values");
        return fieldTypeFactory.enumType(values.split(","));
    }

    private FieldType getHasManyType(final XMLStreamReader reader, final String pluginIdentifier) {
        String plugin = getStringAttribute(reader, "plugin");
        HasManyType.Cascade cascade = "delete".equals(getStringAttribute(reader, "cascade")) ? HasManyType.Cascade.DELETE
                : HasManyType.Cascade.NULLIFY;
        return fieldTypeFactory.hasManyType(plugin != null ? plugin : pluginIdentifier, getStringAttribute(reader, "model"),
                getStringAttribute(reader, "joinField"), cascade);
    }

    private FieldType getBelongsToType(final XMLStreamReader reader, final String pluginIdentifier) {
        boolean lazy = getBooleanAttribute(reader, "lazy", true);
        String plugin = getStringAttribute(reader, "plugin");
        String modelName = getStringAttribute(reader, "model");
        String lookupFieldName = getStringAttribute(reader, "lookupField");
        if (lazy) {
            return fieldTypeFactory.lazyBelongsToType(plugin != null ? plugin : pluginIdentifier, modelName, lookupFieldName);
        } else {
            return fieldTypeFactory.eagerBelongsToType(plugin != null ? plugin : pluginIdentifier, modelName, lookupFieldName);
        }
    }

    private FieldDefinition getFieldDefinition(final XMLStreamReader reader, final String pluginIdentifier)
            throws XMLStreamException {
        String fieldType = reader.getLocalName();
        FieldDefinitionImpl fieldDefinition = new FieldDefinitionImpl(getStringAttribute(reader, "name"));
        fieldDefinition.withReadOnly(getBooleanAttribute(reader, "readonly", false));
        fieldDefinition.withReadOnlyOnUpdate(getBooleanAttribute(reader, "readonlyOnCreate", false));
        fieldDefinition.withDefaultValue(getStringAttribute(reader, "default"));
        FieldType type = null;

        if ("integer".equals(fieldType)) {
            type = fieldTypeFactory.integerType();
        } else if ("string".equals(fieldType)) {
            type = fieldTypeFactory.stringType();
        } else if ("text".equals(fieldType)) {
            type = fieldTypeFactory.textType();
        } else if ("decimal".equals(fieldType)) {
            type = fieldTypeFactory.decimalType();
        } else if ("datetime".equals(fieldType)) {
            type = fieldTypeFactory.dateTimeType();
        } else if ("date".equals(fieldType)) {
            type = fieldTypeFactory.dateType();
        } else if ("boolean".equals(fieldType)) {
            type = fieldTypeFactory.booleanType();
        } else if ("belongsTo".equals(fieldType)) {
            type = getBelongsToType(reader, pluginIdentifier);
        } else if ("hasMany".equals(fieldType)) {
            type = getHasManyType(reader, pluginIdentifier);
        } else if ("enum".equals(fieldType)) {
            type = getEnumType(reader);
        } else if ("dictionary".equals(fieldType)) {
            type = getDictionaryType(reader);
        } else if ("password".equals(fieldType)) {
            type = fieldTypeFactory.passwordType();
        }

        fieldDefinition.withType(type);

        while (reader.hasNext() && reader.next() > 0) {
            if (isTagEnded(reader, fieldType)) {
                break;
            } else if (isTagStarted(reader, "validatesPresence")) {
                fieldDefinition.withValidator(getValidatorDefinition(reader, validatorFactory.required()));
            } else if (isTagStarted(reader, "validatesPresenceOnCreate")) {
                fieldDefinition.withValidator(getValidatorDefinition(reader, validatorFactory.requiredOnCreate()));
            } else if (isTagStarted(reader, "validatesLength")) {
                fieldDefinition.withValidator(getValidatorDefinition(reader, validatorFactory
                        .length(getIntegerAttribute(reader, "min"), getIntegerAttribute(reader, "is"),
                                getIntegerAttribute(reader, "max"))));
            } else if (isTagStarted(reader, "validatesPrecision")) {
                fieldDefinition.withValidator(getValidatorDefinition(reader, validatorFactory
                        .precision(getIntegerAttribute(reader, "min"), getIntegerAttribute(reader, "is"),
                                getIntegerAttribute(reader, "max"))));
            } else if (isTagStarted(reader, "validatesScale")) {
                fieldDefinition.withValidator(getValidatorDefinition(reader, validatorFactory
                        .scale(getIntegerAttribute(reader, "min"), getIntegerAttribute(reader, "is"),
                                getIntegerAttribute(reader, "max"))));
            } else if (isTagStarted(reader, "validatesRange")) {
                Object from = getRangeForType(getStringAttribute(reader, "from"), type);
                Object to = getRangeForType(getStringAttribute(reader, "to"), type);
                fieldDefinition.withValidator(getValidatorDefinition(reader, validatorFactory.range(from, to)));
            } else if (isTagStarted(reader, "validatesUniqueness")) {
                fieldDefinition.withValidator(getValidatorDefinition(reader, validatorFactory.unique()));
            } else if (isTagStarted(reader, "validatesWith")) {
                fieldDefinition.withValidator(getValidatorDefinition(reader, validatorFactory.custom(getHookDefinition(reader))));
            }

            // validatesUniqueness scope="lastname"
            // validatesWithScript
            // validatesExclusion <exclude>nowak</exclude> <exclude>kowalski</exclude>
            // validatesInclusion <include>szczytowski</include>
            // validatesFormat with="d*a"
        }

        return fieldDefinition;
    }

    private Object getRangeForType(final String range, final FieldType type) {
        try {
            if (range == null) {
                return null;
            } else if (type instanceof DateTimeType) {
                return new SimpleDateFormat(DateTimeType.DATE_TIME_FORMAT).parse(range);
            } else if (type instanceof DateType) {
                return new SimpleDateFormat(DateType.DATE_FORMAT).parse(range);
            } else if (type instanceof DecimalType) {
                return new BigDecimal(range);
            } else if (type instanceof IntegerType) {
                return Integer.parseInt(range);
            } else {
                return range;
            }
        } catch (ParseException e) {
            throw new IllegalStateException("Cannot parse data definition", e);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Cannot parse data definition", e);
        }
    }

    private EntityValidator getEntityValidatorDefinition(final XMLStreamReader reader) {
        EntityValidator validator = validatorFactory.customEntity(getHookDefinition(reader));
        String customMessage = getStringAttribute(reader, "message");
        if (StringUtils.hasText(customMessage)) {
            validator.customErrorMessage(customMessage);
        }
        return validator;
    }

    private FieldValidator getValidatorDefinition(final XMLStreamReader reader, final FieldValidator validator) {
        String customMessage = getStringAttribute(reader, "message");
        if (StringUtils.hasText(customMessage)) {
            validator.customErrorMessage(customMessage);
        }
        return validator;
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

    private String getPluginIdentifier(final XMLStreamReader reader) {
        return getStringAttribute(reader, "plugin");
    }

    private Integer getIntegerAttribute(final XMLStreamReader reader, final String name) {
        String stringValue = reader.getAttributeValue(null, name);
        if (stringValue != null) {
            return Integer.valueOf(stringValue);
        } else {
            return null;
        }
    }

    private boolean getBooleanAttribute(final XMLStreamReader reader, final String name, final boolean defaultValue) {
        String stringValue = reader.getAttributeValue(null, name);
        if (stringValue != null) {
            return Boolean.valueOf(stringValue);
        } else {
            return defaultValue;
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