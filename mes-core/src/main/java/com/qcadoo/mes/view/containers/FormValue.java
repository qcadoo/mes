package com.qcadoo.mes.view.containers;

public final class FormValue {

    private Long selectedValue;

    private String header;

    public FormValue() {
    }

    public FormValue(final Long selectedValue) {
        this.selectedValue = selectedValue;
    }

    public Long getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(final Long selectedValue) {
        this.selectedValue = selectedValue;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(final String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        if (selectedValue == null) {
            return ""; // FIXME masz toString cannot return null
        }
        return selectedValue.toString();
    }

}
