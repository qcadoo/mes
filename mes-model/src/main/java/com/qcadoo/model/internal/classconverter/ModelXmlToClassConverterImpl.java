package com.qcadoo.model.internal.classconverter;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.qcadoo.model.internal.AbstractModelXmlConverter;
import com.qcadoo.model.internal.api.ModelXmlToClassConverter;
import com.qcadoo.model.internal.utils.ClassNameUtils;

@Component
public class ModelXmlToClassConverterImpl extends AbstractModelXmlConverter implements ModelXmlToClassConverter {

    private static final Logger LOG = LoggerFactory.getLogger(ModelXmlToClassConverterImpl.class);

    private final ClassPool classPool = ClassPool.getDefault();

    @Override
    public Collection<Class<?>> convert(final Resource... resources) {
        try {
            Map<String, CtClass> ctClasses = new HashMap<String, CtClass>();
            Map<String, Class<?>> existingClasses = new HashMap<String, Class<?>>();

            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    LOG.info("Getting existing classes from " + resource.getURI().toString());
                    existingClasses.putAll(findExistingClasses(resource.getInputStream()));
                }
            }

            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    LOG.info("Creating classes from " + resource.getURI().toString());
                    ctClasses.putAll(createClasses(existingClasses, resource.getInputStream()));
                }
            }

            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    LOG.info("Defining classes from " + resource.getURI().toString() + " to classes");
                    defineClasses(ctClasses, resource.getInputStream());
                }
            }

            List<Class<?>> classes = new ArrayList<Class<?>>();

            for (CtClass ctClass : ctClasses.values()) {
                classes.add(ctClass.toClass());
            }

            classes.addAll(existingClasses.values());

            return classes;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to convert model.xml to classes", e);
        } catch (CannotCompileException e) {
            throw new IllegalStateException("Failed to convert model.xml to classes", e);
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (FactoryConfigurationError e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private Map<String, Class<?>> findExistingClasses(final InputStream stream) throws XMLStreamException, XMLStreamException {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        Map<String, Class<?>> existingClasses = new HashMap<String, Class<?>>();

        String pluginIdentifier = null;

        while (reader.hasNext() && reader.next() > 0) {
            if (isTagStarted(reader, TAG_MODELS)) {
                pluginIdentifier = getPluginIdentifier(reader);
            } else if (isTagStarted(reader, TAG_MODEL)) {
                String modelName = getStringAttribute(reader, "name");
                String className = ClassNameUtils.getFullyQualifiedClassName(pluginIdentifier, modelName);

                try {
                    existingClasses.put(className, ClassLoader.getSystemClassLoader().loadClass(className));
                    LOG.info("Class " + className + " already exists, skipping");
                } catch (ClassNotFoundException e) {
                    // ignoring
                }
            }
        }

        reader.close();

        return existingClasses;
    }

    private Map<String, CtClass> createClasses(final Map<String, Class<?>> existingClasses, final InputStream stream)
            throws XMLStreamException, XMLStreamException {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        Map<String, CtClass> ctClasses = new HashMap<String, CtClass>();

        String pluginIdentifier = null;

        while (reader.hasNext() && reader.next() > 0) {
            if (isTagStarted(reader, TAG_MODELS)) {
                pluginIdentifier = getPluginIdentifier(reader);
            } else if (isTagStarted(reader, TAG_MODEL)) {
                String modelName = getStringAttribute(reader, "name");
                String className = ClassNameUtils.getFullyQualifiedClassName(pluginIdentifier, modelName);

                if (existingClasses.containsKey(className)) {
                    LOG.info("Class " + className + " already exists, skipping");
                } else {
                    LOG.info("Creating class " + className);
                    ctClasses.put(className, classPool.makeClass(className));
                }
            }
        }

        reader.close();

        return ctClasses;
    }

    private void defineClasses(final Map<String, CtClass> ctClasses, final InputStream stream) throws XMLStreamException,
            XMLStreamException {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);

        String pluginIdentifier = null;

        while (reader.hasNext() && reader.next() > 0) {
            if (isTagStarted(reader, TAG_MODELS)) {
                pluginIdentifier = getPluginIdentifier(reader);
            } else if (isTagStarted(reader, TAG_MODEL)) {
                String modelName = getStringAttribute(reader, "name");
                String className = ClassNameUtils.getFullyQualifiedClassName(pluginIdentifier, modelName);

                if (ctClasses.containsKey(className)) {
                    parse(reader, ctClasses.get(className), pluginIdentifier);
                }
            }
        }

        reader.close();
    }

    private void parse(final XMLStreamReader reader, final CtClass ctClass, final String pluginIdentifier)
            throws XMLStreamException, IllegalStateException {

        LOG.info("Defining class " + ctClass.getName());

        createField(ctClass, "id", Long.class.getCanonicalName());

        while (reader.hasNext() && reader.next() > 0) {
            if (isTagEnded(reader, TAG_MODEL)) {
                break;
            }

            String tag = getTagStarted(reader);

            if (tag == null) {
                continue;
            }

            parseField(reader, pluginIdentifier, ctClass, tag);
        }

        // TODO - toString, equals, hashCode
    }

    private void parseField(final XMLStreamReader reader, final String pluginIdentifier, final CtClass ctClass, final String tag)
            throws XMLStreamException {
        ModelTag modelTag = ModelTag.valueOf(tag.toUpperCase(Locale.ENGLISH));

        if (!getBooleanAttribute(reader, "persistent", true)) {
            return;
        }

        if (getStringAttribute(reader, "expression") != null) {
            return;
        }

        switch (modelTag) {
            case PRIORITY:
            case INTEGER:
                createField(ctClass, getStringAttribute(reader, "name"), Integer.class.getCanonicalName());
                break;
            case STRING:
            case TEXT:
            case ENUM:
            case DICTIONARY:
            case PASSWORD:
                createField(ctClass, getStringAttribute(reader, "name"), String.class.getCanonicalName());
                break;
            case DECIMAL:
                createField(ctClass, getStringAttribute(reader, "name"), BigDecimal.class.getCanonicalName());
                break;
            case DATETIME:
            case DATE:
                createField(ctClass, getStringAttribute(reader, "name"), Date.class.getCanonicalName());
                break;
            case BOOLEAN:
                createField(ctClass, getStringAttribute(reader, "name"), Boolean.class.getCanonicalName());
                break;
            case BELONGSTO:
                createBelongsField(ctClass, pluginIdentifier, reader);
                break;
            case HASMANY:
            case TREE:
                createHasManyField(ctClass, pluginIdentifier, reader);
                break;
            default:
                break;
        }

        while (reader.hasNext() && reader.next() > 0) {
            if (isTagEnded(reader, tag)) {
                break;
            }
        }
    }

    private void createHasManyField(final CtClass ctClass, final String pluginIdentifier, final XMLStreamReader reader) {
        // TODO - generics
        createField(ctClass, getStringAttribute(reader, "name"), "java.util.Set");
    }

    private void createBelongsField(final CtClass ctClass, final String pluginIdentifier, final XMLStreamReader reader) {
        String plugin = getStringAttribute(reader, "plugin");

        if (plugin == null) {
            plugin = pluginIdentifier;
        }

        String model = getStringAttribute(reader, "model");

        createField(ctClass, getStringAttribute(reader, "name"), ClassNameUtils.getFullyQualifiedClassName(plugin, model));
    }

    private void createField(final CtClass ctClass, final String name, final String clazz) {
        try {
            ctClass.addField(CtField.make("private " + clazz + " " + name + ";", ctClass));
            ctClass.addMethod(CtNewMethod.make("public " + clazz + " get" + StringUtils.capitalize(name) + "() { return " + name
                    + "; }", ctClass));
            ctClass.addMethod(CtNewMethod.make("public void set" + StringUtils.capitalize(name) + "(" + clazz + " " + name
                    + ") { this." + name + " = " + name + "; }", ctClass));
        } catch (CannotCompileException e) {
            throw new IllegalStateException("Failed to compile class " + ctClass.getName(), e);
        }
    }

}
