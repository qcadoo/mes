/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.model.internal;

import static com.google.common.base.Preconditions.checkState;
import static org.springframework.util.StringUtils.hasText;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

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
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.types.TreeType;
import com.qcadoo.mes.model.types.internal.DateTimeType;
import com.qcadoo.mes.model.types.internal.DateType;
import com.qcadoo.mes.model.types.internal.DecimalType;
import com.qcadoo.mes.model.types.internal.FieldTypeFactory;
import com.qcadoo.mes.model.types.internal.IntegerType;
import com.qcadoo.mes.model.validators.FieldValidator;
import com.qcadoo.mes.model.validators.internal.ValidatorFactory;

@Service
public final class DataDefinitionParser {

    private static enum ModelTag {
        PRIORITY, ONCREATE, ONUPDATE, ONSAVE, ONCOPY, VALIDATESWITH, INTEGER, STRING, TEXT, DECIMAL, DATETIME, DATE, BOOLEAN, BELONGSTO, HASMANY, TREE, ENUM, DICTIONARY, PASSWORD
    }

    private static enum FieldTag {
        VALIDATESPRESENCE, VALIDATESPRESENCEONCREATE, VALIDATESLENGTH, VALIDATESPRECISION, VALIDATESSCALE, VALIDATESRANGE, VALIDATESUNIQUENESS, VALIDATESWITH, VALIDATESREGEX
    }

    private static final String TAG_MODELS = "models";

    private static final String TAG_MODEL = "model";

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

    public void parse() {
        LOG.info("Reading model definitions ...");

        try {
            Resource[] resources = applicationContext.getResources("classpath*:model/*.xml");
            for (Resource resource : resources) {
                parse(resource.getInputStream());
            }
        } catch (IOException e) {
            LOG.error("Cannot read data definition", e);
        }
    }

    public void parse(final InputStream dataDefinitionInputStream) {
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(dataDefinitionInputStream);
            String pluginIdentifier = null;

            while (reader.hasNext() && reader.next() > 0) {
                if (isTagStarted(reader, TAG_MODELS)) {
                    pluginIdentifier = getPluginIdentifier(reader);
                } else if (isTagStarted(reader, TAG_MODEL)) {
                    parseModelDefinition(reader, pluginIdentifier);
                }
            }

            reader.close();
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (FactoryConfigurationError e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void parseModelDefinition(final XMLStreamReader reader, final String pluginIdentifier) throws XMLStreamException {
        DataDefinitionImpl dataDefinition = getModelDefinition(reader, pluginIdentifier);

        while (reader.hasNext() && reader.next() > 0) {
            if (isTagEnded(reader, TAG_MODEL)) {
                break;
            }

            String tag = getTagStarted(reader);

            if (tag == null) {
                continue;
            }

            addModelElement(reader, pluginIdentifier, dataDefinition, tag);

        }

        dataDefinitionService.save(dataDefinition);
    }

    private void addModelElement(final XMLStreamReader reader, final String pluginIdentifier,
            final DataDefinitionImpl dataDefinition, final String tag) throws XMLStreamException {
        ModelTag modelTag = ModelTag.valueOf(tag.toUpperCase(Locale.ENGLISH));

        switch (modelTag) {
            case PRIORITY:
                dataDefinition.withPriorityField(getPriorityFieldDefinition(reader, dataDefinition));
                break;
            case ONCREATE:
                dataDefinition.withCreateHook(getHookDefinition(reader));
                break;
            case ONUPDATE:
                dataDefinition.withUpdateHook(getHookDefinition(reader));
                break;
            case ONSAVE:
                dataDefinition.withSaveHook(getHookDefinition(reader));
                break;
            case ONCOPY:
                dataDefinition.withCopyHook(getHookDefinition(reader));
                break;
            case VALIDATESWITH:
                dataDefinition.withValidator(validatorFactory.customEntity(getHookDefinition(reader)));
                break;
            default:
                dataDefinition.withField(getFieldDefinition(reader, pluginIdentifier, dataDefinition, modelTag));
                break;
        }
    }

    private DataDefinitionImpl getModelDefinition(final XMLStreamReader reader, final String pluginIdentifier) {
        String modelName = getStringAttribute(reader, "name");

        LOG.info("Reading model " + modelName + " for plugin " + pluginIdentifier);

        DataDefinitionImpl dataDefinition = new DataDefinitionImpl(pluginIdentifier, modelName, dataAccessService);
        dataDefinition.setDeletable(getBooleanAttribute(reader, "deletable", true));
        dataDefinition.setCreatable(getBooleanAttribute(reader, "creatable", true));
        dataDefinition.setUpdatable(getBooleanAttribute(reader, "updatable", true));
        dataDefinition.setFullyQualifiedClassName("com.qcadoo.mes.beans." + pluginIdentifier + "."
                + StringUtils.capitalize(pluginIdentifier) + StringUtils.capitalize(modelName));
        return dataDefinition;
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
        return fieldTypeFactory.hasManyType(plugin != null ? plugin : pluginIdentifier, getStringAttribute(reader, TAG_MODEL),
                getStringAttribute(reader, "joinField"), cascade, getBooleanAttribute(reader, "copyable", false));
    }

    private FieldType getTreeType(final XMLStreamReader reader, final String pluginIdentifier) {
        String plugin = getStringAttribute(reader, "plugin");
        TreeType.Cascade cascade = "delete".equals(getStringAttribute(reader, "cascade")) ? TreeType.Cascade.DELETE
                : TreeType.Cascade.NULLIFY;
        return fieldTypeFactory.treeType(plugin != null ? plugin : pluginIdentifier, getStringAttribute(reader, TAG_MODEL),
                getStringAttribute(reader, "joinField"), cascade, getBooleanAttribute(reader, "copyable", false));
    }

    private FieldType getBelongsToType(final XMLStreamReader reader, final String pluginIdentifier) {
        boolean lazy = getBooleanAttribute(reader, "lazy", true);
        String plugin = getStringAttribute(reader, "plugin");
        String modelName = getStringAttribute(reader, TAG_MODEL);
        String lookupFieldName = getStringAttribute(reader, "lookupField");
        if (lazy) {
            return fieldTypeFactory.lazyBelongsToType(plugin != null ? plugin : pluginIdentifier, modelName, lookupFieldName);
        } else {
            return fieldTypeFactory.eagerBelongsToType(plugin != null ? plugin : pluginIdentifier, modelName, lookupFieldName);
        }
    }

    private FieldDefinition getFieldDefinition(final XMLStreamReader reader, final String pluginIdentifier,
            final DataDefinitionImpl dataDefinition, final ModelTag modelTag) throws XMLStreamException {
        String fieldType = reader.getLocalName();
        FieldDefinitionImpl fieldDefinition = new FieldDefinitionImpl(dataDefinition, getStringAttribute(reader, "name"));
        fieldDefinition.withReadOnly(getBooleanAttribute(reader, "readonly", false));
        fieldDefinition.withReadOnlyOnUpdate(getBooleanAttribute(reader, "readonlyOnCreate", false));
        fieldDefinition.withDefaultValue(getStringAttribute(reader, "default"));
        fieldDefinition.setPersistent(getBooleanAttribute(reader, "persistent", true));
        fieldDefinition.setExpression(getStringAttribute(reader, "expression"));
        FieldType type = getFieldType(reader, pluginIdentifier, modelTag, fieldType);
        fieldDefinition.withType(type);

        while (reader.hasNext() && reader.next() > 0) {
            if (isTagEnded(reader, fieldType)) {
                break;
            }

            String tag = getTagStarted(reader);

            if (tag == null) {
                continue;
            }

            addFieldElement(reader, fieldDefinition, type, tag);
        }

        return fieldDefinition;
    }

    private void addFieldElement(final XMLStreamReader reader, final FieldDefinitionImpl fieldDefinition, final FieldType type,
            final String tag) {
        switch (FieldTag.valueOf(tag.toUpperCase(Locale.ENGLISH))) {
            case VALIDATESPRESENCE:
                fieldDefinition.withValidator(getValidatorDefinition(reader, validatorFactory.required()));
                break;
            case VALIDATESPRESENCEONCREATE:
                fieldDefinition.withValidator(getValidatorDefinition(reader, validatorFactory.requiredOnCreate()));
                break;
            case VALIDATESLENGTH:
                fieldDefinition.withValidator(getValidatorDefinition(reader, validatorFactory
                        .length(getIntegerAttribute(reader, "min"), getIntegerAttribute(reader, "is"),
                                getIntegerAttribute(reader, "max"))));
                break;
            case VALIDATESPRECISION:
                fieldDefinition.withValidator(getValidatorDefinition(reader, validatorFactory
                        .precision(getIntegerAttribute(reader, "min"), getIntegerAttribute(reader, "is"),
                                getIntegerAttribute(reader, "max"))));
                break;
            case VALIDATESSCALE:
                fieldDefinition.withValidator(getValidatorDefinition(reader, validatorFactory
                        .scale(getIntegerAttribute(reader, "min"), getIntegerAttribute(reader, "is"),
                                getIntegerAttribute(reader, "max"))));
                break;
            case VALIDATESRANGE:
                Object from = getRangeForType(getStringAttribute(reader, "from"), type);
                Object to = getRangeForType(getStringAttribute(reader, "to"), type);
                boolean inclusive = getBooleanAttribute(reader, "inclusive", true);
                fieldDefinition.withValidator(getValidatorDefinition(reader, validatorFactory.range(from, to, inclusive)));
                break;
            case VALIDATESUNIQUENESS:
                fieldDefinition.withValidator(getValidatorDefinition(reader, validatorFactory.unique()));
                break;
            case VALIDATESWITH:
                fieldDefinition.withValidator(getValidatorDefinition(reader, validatorFactory.custom(getHookDefinition(reader))));
                break;
            case VALIDATESREGEX:
                fieldDefinition.withValidator(getValidatorDefinition(reader,
                        validatorFactory.regex(getStringAttribute(reader, "regex"))));
                break;
            default:
                throw new IllegalStateException("Illegal validator type " + tag);
        }
    }

    private FieldType getFieldType(final XMLStreamReader reader, final String pluginIdentifier, final ModelTag modelTag,
            final String fieldType) throws XMLStreamException {
        switch (modelTag) {
            case INTEGER:
                return fieldTypeFactory.integerType();
            case STRING:
                return fieldTypeFactory.stringType();
            case TEXT:
                return fieldTypeFactory.textType();
            case DECIMAL:
                return fieldTypeFactory.decimalType();
            case DATETIME:
                return fieldTypeFactory.dateTimeType();
            case DATE:
                return fieldTypeFactory.dateType();
            case BOOLEAN:
                return fieldTypeFactory.booleanType();
            case BELONGSTO:
                return getBelongsToType(reader, pluginIdentifier);
            case HASMANY:
                return getHasManyType(reader, pluginIdentifier);
            case TREE:
                return getTreeType(reader, pluginIdentifier);
            case ENUM:
                return getEnumType(reader);
            case DICTIONARY:
                return getDictionaryType(reader);
            case PASSWORD:
                return fieldTypeFactory.passwordType();
            default:
                throw new IllegalStateException("Illegal field type " + fieldType);
        }
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
        String scopeAttribute = getStringAttribute(reader, "scope");
        FieldDefinition scopedField = null;
        if (scopeAttribute != null) {
            scopedField = dataDefinition.getField(scopeAttribute);
        }
        return new FieldDefinitionImpl(dataDefinition, getStringAttribute(reader, "name")).withType(fieldTypeFactory
                .priorityType(scopedField));
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

    private String getTagStarted(final XMLStreamReader reader) {
        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
            return reader.getLocalName();
        } else {
            return null;
        }
    }

    private boolean isTagStarted(final XMLStreamReader reader, final String tagName) {
        return (reader.getEventType() == XMLStreamConstants.START_ELEMENT && tagName.equals(reader.getLocalName()));
    }

    private boolean isTagEnded(final XMLStreamReader reader, final String tagName) {
        return (reader.getEventType() == XMLStreamConstants.END_ELEMENT && tagName.equals(reader.getLocalName()));
    }

}
