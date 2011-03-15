/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.model.internal.definitionconverter;

import static com.google.common.base.Preconditions.checkState;
import static org.springframework.util.StringUtils.hasText;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.DictionaryService;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.localization.TranslationService;
import com.qcadoo.model.api.security.PasswordEncoder;
import com.qcadoo.model.api.types.FieldType;
import com.qcadoo.model.api.types.HasManyType;
import com.qcadoo.model.api.types.TreeType;
import com.qcadoo.model.api.utils.DateUtils;
import com.qcadoo.model.beans.dictionaries.DictionariesDictionary;
import com.qcadoo.model.internal.AbstractModelXmlConverter;
import com.qcadoo.model.internal.DataDefinitionImpl;
import com.qcadoo.model.internal.FieldDefinitionImpl;
import com.qcadoo.model.internal.api.DataAccessService;
import com.qcadoo.model.internal.api.EntityHookDefinition;
import com.qcadoo.model.internal.api.ErrorMessageDefinition;
import com.qcadoo.model.internal.api.FieldHookDefinition;
import com.qcadoo.model.internal.api.ModelXmlResolver;
import com.qcadoo.model.internal.api.ModelXmlToDefinitionConverter;
import com.qcadoo.model.internal.hooks.EntityHookDefinitionImpl;
import com.qcadoo.model.internal.hooks.FieldHookDefinitionImpl;
import com.qcadoo.model.internal.types.BelongsToEntityType;
import com.qcadoo.model.internal.types.BooleanType;
import com.qcadoo.model.internal.types.DateTimeType;
import com.qcadoo.model.internal.types.DateType;
import com.qcadoo.model.internal.types.DecimalType;
import com.qcadoo.model.internal.types.DictionaryType;
import com.qcadoo.model.internal.types.EnumType;
import com.qcadoo.model.internal.types.HasManyEntitiesType;
import com.qcadoo.model.internal.types.IntegerType;
import com.qcadoo.model.internal.types.PasswordType;
import com.qcadoo.model.internal.types.PriorityType;
import com.qcadoo.model.internal.types.StringType;
import com.qcadoo.model.internal.types.TextType;
import com.qcadoo.model.internal.types.TreeEntitiesType;
import com.qcadoo.model.internal.utils.ClassNameUtils;
import com.qcadoo.model.internal.validators.CustomEntityValidator;
import com.qcadoo.model.internal.validators.CustomValidator;
import com.qcadoo.model.internal.validators.LengthValidator;
import com.qcadoo.model.internal.validators.PrecisionValidator;
import com.qcadoo.model.internal.validators.RangeValidator;
import com.qcadoo.model.internal.validators.RegexValidator;
import com.qcadoo.model.internal.validators.RequiredValidator;
import com.qcadoo.model.internal.validators.ScaleValidator;
import com.qcadoo.model.internal.validators.UniqueValidator;

@Service
public final class ModelXmlToDefinitionConverterImpl extends AbstractModelXmlConverter implements ModelXmlToDefinitionConverter { // ,

    // ApplicationListener<ContextRefreshedEvent>
    // {

    private static final Logger LOG = LoggerFactory.getLogger(ModelXmlToDefinitionConverterImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private DataAccessService dataAccessService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ModelXmlResolver modelXmlResolver;

    @Autowired
    private TranslationService translationService;

    @Override
    @Transactional
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        convert(modelXmlResolver.getResources());
    }

    @Override
    public Collection<DataDefinition> convert(final Resource... resources) {
        try {
            List<DataDefinition> dataDefinitions = new ArrayList<DataDefinition>();

            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    LOG.info("Creating dataDefinition from " + resource.getURI().toString());

                    dataDefinitions.addAll(parse(resource.getInputStream()));
                }
            }

            return dataDefinitions;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to convert model.xml to dataDefinitions", e);
        }
    }

    private Collection<DataDefinition> parse(final InputStream stream) {
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
            List<DataDefinition> dataDefinitions = new ArrayList<DataDefinition>();

            String pluginIdentifier = null;

            while (reader.hasNext() && reader.next() > 0) {
                if (isTagStarted(reader, TAG_MODELS)) {
                    pluginIdentifier = getPluginIdentifier(reader);
                } else if (isTagStarted(reader, TAG_DICTIONARY)) {
                    getDictionary(reader, pluginIdentifier);
                } else if (isTagStarted(reader, TAG_MODEL)) {
                    dataDefinitions.add(getDataDefinition(reader, pluginIdentifier));
                }
            }

            reader.close();

            return dataDefinitions;
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (FactoryConfigurationError e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void getDictionary(final XMLStreamReader reader, final String pluginIdentifier) throws XMLStreamException {
        String name = getStringAttribute(reader, "name");

        DictionariesDictionary dictionary = (DictionariesDictionary) sessionFactory.getCurrentSession()
                .createCriteria(DictionariesDictionary.class).add(Restrictions.eq("name", name)).setMaxResults(1).uniqueResult();

        if (dictionary != null) {
            return;
        }

        // getBooleanAttribute(reader, "modificable", true);

        dictionary = new DictionariesDictionary();
        dictionary.setName(name);

        // TODO dictionary label
        dictionary.setLabel(name);

        sessionFactory.getCurrentSession().save(dictionary);

        // TODO dictionary values

        while (reader.hasNext() && reader.next() > 0) {
            if (isTagEnded(reader, TAG_DICTIONARY)) {
                break;
            }

            if (isTagStarted(reader, "value")) {
                // System.out.println(" ----> " + reader.getElementText());
            }
        }
    }

    private DataDefinition getDataDefinition(final XMLStreamReader reader, final String pluginIdentifier)
            throws XMLStreamException {
        DataDefinitionImpl dataDefinition = getModelDefinition(reader, pluginIdentifier);

        LOG.info("Creating dataDefinition " + dataDefinition);

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

        for (EntityHookDefinition hook : dataDefinition.getViewHooks()) {
            hook.initialize(dataDefinition);
        }

        for (EntityHookDefinition hook : dataDefinition.getCopyHooks()) {
            hook.initialize(dataDefinition);
        }

        for (EntityHookDefinition hook : dataDefinition.getSaveHooks()) {
            hook.initialize(dataDefinition);
        }

        for (EntityHookDefinition hook : dataDefinition.getUpdateHooks()) {
            hook.initialize(dataDefinition);
        }

        for (EntityHookDefinition hook : dataDefinition.getCreateHooks()) {
            hook.initialize(dataDefinition);
        }

        for (EntityHookDefinition hook : dataDefinition.getValidators()) {
            hook.initialize(dataDefinition);
        }

        for (FieldDefinition field : dataDefinition.getFields().values()) {
            for (FieldHookDefinition hook : ((FieldDefinitionImpl) field).getValidators()) {
                hook.initialize(dataDefinition, field);
            }
        }

        dataDefinitionService.save(dataDefinition);

        return dataDefinition;
    }

    private void addModelElement(final XMLStreamReader reader, final String pluginIdentifier,
            final DataDefinitionImpl dataDefinition, final String tag) throws XMLStreamException {
        ModelTag modelTag = ModelTag.valueOf(tag.toUpperCase(Locale.ENGLISH));

        switch (modelTag) {
            case PRIORITY:
                dataDefinition.addPriorityField(getPriorityFieldDefinition(reader, dataDefinition));
                break;
            case ONVIEW:
                dataDefinition.addViewHook(getHookDefinition(reader));
                break;
            case ONCREATE:
                dataDefinition.addCreateHook(getHookDefinition(reader));
                break;
            case ONUPDATE:
                dataDefinition.addUpdateHook(getHookDefinition(reader));
                break;
            case ONSAVE:
                dataDefinition.addSaveHook(getHookDefinition(reader));
                break;
            case ONCOPY:
                dataDefinition.addCopyHook(getHookDefinition(reader));
                break;
            case VALIDATESWITH:
                dataDefinition.addValidatorHook(new CustomEntityValidator(getHookDefinition(reader)));
                break;
            case IDENTIFIER:
                dataDefinition.setIdentifierExpression(getIdentifierExpression(reader));
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
        dataDefinition.setInsertable(getBooleanAttribute(reader, "insertable", true));
        dataDefinition.setUpdatable(getBooleanAttribute(reader, "updatable", true));
        dataDefinition.setFullyQualifiedClassName(ClassNameUtils.getFullyQualifiedClassName(pluginIdentifier, modelName));
        return dataDefinition;
    }

    private FieldType getDictionaryType(final XMLStreamReader reader) {
        String dictionaryName = getStringAttribute(reader, "dictionary");
        checkState(hasText(dictionaryName), "Dictionary name is required");
        return new DictionaryType(dictionaryName, dictionaryService);
    }

    private FieldType getEnumType(final XMLStreamReader reader, final String translationPath) throws XMLStreamException {
        String values = getStringAttribute(reader, "values");
        return new EnumType(translationService, translationPath, values.split(","));
    }

    private FieldType getHasManyType(final XMLStreamReader reader, final String pluginIdentifier) {
        String plugin = getStringAttribute(reader, "plugin");
        HasManyType.Cascade cascade = "delete".equals(getStringAttribute(reader, "cascade")) ? HasManyType.Cascade.DELETE
                : HasManyType.Cascade.NULLIFY;
        return new HasManyEntitiesType(plugin != null ? plugin : pluginIdentifier, getStringAttribute(reader, TAG_MODEL),
                getStringAttribute(reader, "joinField"), cascade, getBooleanAttribute(reader, "copyable", false),
                dataDefinitionService);
    }

    private FieldType getTreeType(final XMLStreamReader reader, final String pluginIdentifier) {
        String plugin = getStringAttribute(reader, "plugin");
        TreeType.Cascade cascade = "delete".equals(getStringAttribute(reader, "cascade")) ? TreeType.Cascade.DELETE
                : TreeType.Cascade.NULLIFY;
        return new TreeEntitiesType(plugin != null ? plugin : pluginIdentifier, getStringAttribute(reader, TAG_MODEL),
                getStringAttribute(reader, "joinField"), cascade, getBooleanAttribute(reader, "copyable", false),
                dataDefinitionService);
    }

    private FieldType getBelongsToType(final XMLStreamReader reader, final String pluginIdentifier) {
        boolean lazy = getBooleanAttribute(reader, "lazy", true);
        String plugin = getStringAttribute(reader, "plugin");
        String modelName = getStringAttribute(reader, TAG_MODEL);
        if (lazy) {
            return new BelongsToEntityType(plugin != null ? plugin : pluginIdentifier, modelName, dataDefinitionService, true);
        } else {
            return new BelongsToEntityType(plugin != null ? plugin : pluginIdentifier, modelName, dataDefinitionService, false);
        }
    }

    private FieldDefinition getFieldDefinition(final XMLStreamReader reader, final String pluginIdentifier,
            final DataDefinitionImpl dataDefinition, final ModelTag modelTag) throws XMLStreamException {
        String fieldType = reader.getLocalName();
        String name = getStringAttribute(reader, "name");
        FieldDefinitionImpl fieldDefinition = new FieldDefinitionImpl(dataDefinition, name);
        fieldDefinition.withReadOnly(getBooleanAttribute(reader, "readonly", false));
        fieldDefinition.withDefaultValue(getStringAttribute(reader, "default"));
        fieldDefinition.setPersistent(getBooleanAttribute(reader, "persistent", true));
        fieldDefinition.setExpression(getStringAttribute(reader, "expression"));
        FieldType type = getFieldType(reader, dataDefinition, name, modelTag, fieldType);
        fieldDefinition.withType(type);

        if (getBooleanAttribute(reader, "required", false)) {
            fieldDefinition.withValidator(getValidatorDefinition(reader, new RequiredValidator()));
        }

        if (getBooleanAttribute(reader, "unique", false)) {
            fieldDefinition.withValidator(getValidatorDefinition(reader, new UniqueValidator()));
        }

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
            case VALIDATESLENGTH:
                fieldDefinition.withValidator(getValidatorDefinition(reader,
                        new LengthValidator(getIntegerAttribute(reader, "min"), getIntegerAttribute(reader, "is"),
                                getIntegerAttribute(reader, "max"))));
                break;
            case VALIDATESPRECISION:
                fieldDefinition.withValidator(getValidatorDefinition(reader,
                        new PrecisionValidator(getIntegerAttribute(reader, "min"), getIntegerAttribute(reader, "is"),
                                getIntegerAttribute(reader, "max"))));
                break;
            case VALIDATESSCALE:
                fieldDefinition.withValidator(getValidatorDefinition(reader,
                        new ScaleValidator(getIntegerAttribute(reader, "min"), getIntegerAttribute(reader, "is"),
                                getIntegerAttribute(reader, "max"))));
                break;
            case VALIDATESRANGE:
                Object from = getRangeForType(getStringAttribute(reader, "from"), type);
                Object to = getRangeForType(getStringAttribute(reader, "to"), type);
                boolean exclusively = getBooleanAttribute(reader, "exclusively", false);
                fieldDefinition.withValidator(getValidatorDefinition(reader, new RangeValidator(from, to, exclusively)));
                break;
            case VALIDATESWITH:
                fieldDefinition
                        .withValidator(getValidatorDefinition(reader, new CustomValidator(getFieldHookDefinition(reader))));
                break;
            case VALIDATESREGEX:
                fieldDefinition.withValidator(getValidatorDefinition(reader,
                        new RegexValidator(getStringAttribute(reader, "pattern"))));
                break;
            default:
                throw new IllegalStateException("Illegal validator type " + tag);
        }
    }

    private FieldType getFieldType(final XMLStreamReader reader, final DataDefinition dataDefinition, final String fieldName,
            final ModelTag modelTag, final String fieldType) throws XMLStreamException {
        switch (modelTag) {
            case INTEGER:
                return new IntegerType();
            case STRING:
                return new StringType();
            case TEXT:
                return new TextType();
            case DECIMAL:
                return new DecimalType();
            case DATETIME:
                return new DateTimeType();
            case DATE:
                return new DateType();
            case BOOLEAN:
                return new BooleanType();
            case BELONGSTO:
                return getBelongsToType(reader, dataDefinition.getPluginIdentifier());
            case HASMANY:
                return getHasManyType(reader, dataDefinition.getPluginIdentifier());
            case TREE:
                return getTreeType(reader, dataDefinition.getPluginIdentifier());
            case ENUM:
                return getEnumType(reader, dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName() + "."
                        + fieldName);
            case DICTIONARY:
                return getDictionaryType(reader);
            case PASSWORD:
                return new PasswordType(passwordEncoder);
            default:
                throw new IllegalStateException("Illegal field type " + fieldType);
        }
    }

    private Object getRangeForType(final String range, final FieldType type) {
        try {
            if (range == null) {
                return null;
            } else if (type instanceof DateTimeType) {
                return new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT).parse(range);
            } else if (type instanceof DateType) {
                return new SimpleDateFormat(DateUtils.DATE_FORMAT).parse(range);
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

    private FieldHookDefinition getValidatorDefinition(final XMLStreamReader reader, final FieldHookDefinition validator) {
        String customMessage = getStringAttribute(reader, "message");
        if (StringUtils.hasText(customMessage) && validator instanceof ErrorMessageDefinition) {
            ((ErrorMessageDefinition) validator).setErrorMessage(customMessage);
        }
        return validator;
    }

    private EntityHookDefinition getHookDefinition(final XMLStreamReader reader) {
        String className = getStringAttribute(reader, "class");
        String methodName = getStringAttribute(reader, "method");
        return new EntityHookDefinitionImpl(className, methodName, applicationContext);
    }

    private FieldHookDefinition getFieldHookDefinition(final XMLStreamReader reader) {
        String className = getStringAttribute(reader, "class");
        String methodName = getStringAttribute(reader, "method");
        return new FieldHookDefinitionImpl(className, methodName, applicationContext);
    }

    private FieldDefinition getPriorityFieldDefinition(final XMLStreamReader reader, final DataDefinitionImpl dataDefinition) {
        String scopeAttribute = getStringAttribute(reader, "scope");
        FieldDefinition scopedField = null;
        if (scopeAttribute != null) {
            scopedField = dataDefinition.getField(scopeAttribute);
        }
        return new FieldDefinitionImpl(dataDefinition, getStringAttribute(reader, "name"))
                .withType(new PriorityType(scopedField));
    }

}
