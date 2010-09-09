package com.qcadoo.mes.core.data.definition.form;

import com.qcadoo.mes.core.data.controls.FieldControl;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;

public class FormFieldDefinition {

    private final String name;

    private DataFieldDefinition dataField;

    private FieldControl control;

    private boolean hidden;

    public FormFieldDefinition(final String name) {
        this.name = name;
    }

    public DataFieldDefinition getDataField() {
        return dataField;
    }

    public void setDataField(final DataFieldDefinition dataField) {
        this.dataField = dataField;
    }

    public String getName() {
        return name;
    }

    public FieldControl getControl() {
        return control;
    }

    public void setControl(final FieldControl control) {
        this.control = control;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

}
