package com.qcadoo.mes.view.containers;

public final class FormValue {

    private Long id;

    private String header;

    private String headerEntityIdentifier;

    public FormValue() {
    }

    public FormValue(final Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(final String header) {
        this.header = header;
    }

    public void setHeaderEntityIdentifier(final String headerEntityIdentifier) {
        this.headerEntityIdentifier = headerEntityIdentifier;
    }

    public String getHeaderEntityIdentifier() {
        return headerEntityIdentifier;
    }

    @Override
    public String toString() {
        if (id == null) {
            return ""; // FIXME masz toString cannot return null
        }
        return id.toString();
    }

}
