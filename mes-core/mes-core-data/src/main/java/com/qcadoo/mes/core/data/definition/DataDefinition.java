package com.qcadoo.mes.core.data.definition;

import java.util.List;

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
public interface DataDefinition {

    String getEntityName();

    String getFullyQualifiedClassName();

    String getDiscriminator();

    List<FieldDefinition> getFields();

    List<GridDefinition> getGrids();

    boolean isVirtualTable();

    boolean isCoreTable();

    boolean isPluginTable();

}
