package com.qcadoo.mes.core.data.definition.view;

import java.util.LinkedList;
import java.util.List;

public class ViewDefinition {

    private String name;

    private List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();

    private String header;

    private String codeId;

    public ViewDefinition(final String name, final String codeId) {
        this.name = name;
        this.codeId = codeId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<ComponentDefinition> getElements() {
        return elements;
    }

    public void setElements(final List<ComponentDefinition> elements) {
        this.elements = elements;
    }

    public ComponentDefinition getElementByName(final String elementName) {
        for (ComponentDefinition element : elements) {
            if (elementName.equals(element.getName())) {
                return element;
            }
        }
        return null;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(final String header) {
        this.header = header;
    }

    public String getCodeId() {
        return codeId;
    }

    public void setCodeId(final String codeId) {
        this.codeId = codeId;
    }

}
