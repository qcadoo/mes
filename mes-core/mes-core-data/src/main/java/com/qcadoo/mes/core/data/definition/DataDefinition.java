package com.qcadoo.mes.core.data.definition;

import java.util.HashMap;
import java.util.Map;

/**
 * Object defines database structure and its representation on grids and forms. The {@link DataDefinition#getEntityName()} points
 * to virtual table ("virtual.tablename"), plugin table ("pluginname.tablename") or core table ("core.tablename").
 * 
 * The method {@link DataDefinition#getFullyQualifiedClassName()} returns the full name of the class that is used for mapping
 * table.
 * 
 * The method {@link DataDefinition#getDiscriminator()} returns value of the column that discriminate which virtual table is used.
 * 
 * @apiviz.owns com.qcadoo.mes.core.data.definition.FieldDefinition
 * @apiviz.owns com.qcadoo.mes.core.data.definition.GridDefinition
 */
public final class DataDefinition {

    private final String entityName;

    private String fullyQualifiedClassName;

    private String discriminator;

    private Map<String, FieldDefinition> fields = new HashMap<String, FieldDefinition>();

    public DataDefinition(final String entityName) {
        this.entityName = entityName;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getFullyQualifiedClassName() {
        return fullyQualifiedClassName;
    }

    public void setFullyQualifiedClassName(final String fullyQualifiedClassName) {
        this.fullyQualifiedClassName = fullyQualifiedClassName;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public void setDiscriminator(final String discriminator) {
        this.discriminator = discriminator;
    }

    public Map<String, FieldDefinition> getFields() {
        return fields;
    }

    public void setFields(final Map<String, FieldDefinition> fields) {
        this.fields = fields;
    }

    public void addField(FieldDefinition field) {
        fields.put(field.getName(), field);
    }

    public FieldDefinition getField(String fieldName) {
        return fields.get(fieldName);
    }

    public boolean isVirtualTable() {
        return entityName.startsWith("virtual.");
    }

    public boolean isCoreTable() {
        return entityName.startsWith("core.");
    }

    public boolean isPluginTable() {
        return !isCoreTable() && !isVirtualTable();
    }

}
