package com.qcadoo.model.internal;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public abstract class AbstractModelXmlConverter {

    protected static enum FieldsTag {
        PRIORITY, INTEGER, STRING, TEXT, DECIMAL, DATETIME, DATE, BOOLEAN, BELONGSTO, HASMANY, TREE, ENUM, DICTIONARY, PASSWORD
    }

    protected static enum HooksTag {
        ONVIEW, ONCREATE, ONUPDATE, ONSAVE, ONCOPY, VALIDATESWITH
    }

    protected static enum OtherTag {
        IDENTIFIER
    }

    protected static enum FieldTag {
        VALIDATESLENGTH, VALIDATESPRECISION, VALIDATESSCALE, VALIDATESRANGE, VALIDATESWITH, VALIDATESREGEX
    }

    protected static final String TAG_MODEL = "model";

    protected static final String TAG_FIELDS = "fields";

    protected static final String TAG_HOOKS = "hooks";

    protected static final String TAG_PLUGIN = "plugin";

    protected String getPluginIdentifier(final XMLStreamReader reader) {
        return getStringAttribute(reader, "plugin");
    }

    protected String getIdentifierExpression(final XMLStreamReader reader) {
        return getStringAttribute(reader, "expression");
    }

    protected Integer getIntegerAttribute(final XMLStreamReader reader, final String name) {
        String stringValue = reader.getAttributeValue(null, name);
        if (stringValue != null) {
            return Integer.valueOf(stringValue);
        } else {
            return null;
        }
    }

    protected boolean getBooleanAttribute(final XMLStreamReader reader, final String name, final boolean defaultValue) {
        String stringValue = reader.getAttributeValue(null, name);
        if (stringValue != null) {
            return Boolean.valueOf(stringValue);
        } else {
            return defaultValue;
        }
    }

    protected String getStringAttribute(final XMLStreamReader reader, final String name) {
        return reader.getAttributeValue(null, name);
    }

    protected String getTagStarted(final XMLStreamReader reader) {
        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
            return reader.getLocalName();
        } else {
            return null;
        }
    }

    protected boolean isTagStarted(final XMLStreamReader reader, final String tagName) {
        return (reader.getEventType() == XMLStreamConstants.START_ELEMENT && tagName.equals(reader.getLocalName()));
    }

    protected boolean isTagEnded(final XMLStreamReader reader, final String tagName) {
        return (reader.getEventType() == XMLStreamConstants.END_ELEMENT && tagName.equals(reader.getLocalName()));
    }

}
