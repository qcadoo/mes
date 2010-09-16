package com.qcadoo.mes.core.data.definition.view;

import java.util.Map;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;

public abstract class ComponentDefinition {

    public static final int TYPE_CONTAINER_WINDOW = 1;

    public static final int TYPE_CONTAINER_FORM = 2;

    public static final int TYPE_ELEMENT_GRID = 3;

    public static final int TYPE_ELEMENT_TEXT_INPUT = 4;

    private String name;

    private Map<String, String> options;

    // private String parent; // null, "entityId", "viewElement:{name}"

    // private String parentField;

    // private String correspondingViewName;

    // private boolean correspondingViewModal = false;

    private boolean hidden;

    private boolean editable;

    private final String dataSource; // entity, entity.field[.field], #fieldName.field[.field]

    private boolean lisinable = false;

    public abstract int getType();

    public abstract Object getValue(DataDefinition dataDefinition, DataAccessService dataAccessService, Entity entity);

    public abstract Object getUpdateValues(Map<String, String> updateComponents);

    public ComponentDefinition(final String name, final String dataSource) {
        this.name = name;
        this.dataSource = dataSource;
        // / this.dataDefinition = dataDefinition;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(final Map<String, String> options) {
        this.options = options;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getDataSource() {
        return dataSource;
    }

    public boolean isLisinable() {
        return lisinable;
    }

    public void setLisinable(boolean lisinable) {
        this.lisinable = lisinable;
    }

    // public String getParentField() {
    // return parentField;
    // }
    //
    // public void setParentField(final String parentField) {
    // this.parentField = parentField;
    // }
    //
    // public String getParent() {
    // return parent;
    // }
    //
    // public void setParent(final String parent) {
    // this.parent = parent;
    // }
    //
    // public String getCorrespondingViewName() {
    // return correspondingViewName;
    // }
    //
    // public void setCorrespondingViewName(final String correspondingViewName) {
    // this.correspondingViewName = correspondingViewName;
    // }
    //
    // public boolean isCorrespondingViewModal() {
    // return correspondingViewModal;
    // }
    //
    // public void setCorrespondingViewModal(final boolean correspondingViewModal) {
    // this.correspondingViewModal = correspondingViewModal;
    // }
    //
    // public String getHeader() {
    // return header;
    // }
    //
    // public void setHeader(final String header) {
    // this.header = header;
    // }

}
