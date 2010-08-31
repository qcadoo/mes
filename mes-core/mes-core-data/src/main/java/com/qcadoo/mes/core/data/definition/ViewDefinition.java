package com.qcadoo.mes.core.data.definition;

import java.util.LinkedList;
import java.util.List;

public class ViewDefinition {

    private String name;

    private List<ViewElementDefinition> elements = new LinkedList<ViewElementDefinition>();

    public ViewDefinition(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ViewElementDefinition> getElements() {
        return elements;
    }

    public void setElements(List<ViewElementDefinition> elements) {
        this.elements = elements;
    }

    public ViewElementDefinition getElementByName(String elementName) {
        for (ViewElementDefinition element : elements) {
            if (elementName.equals(element.getName())) {
                return element;
            }
        }
        return null;
    }

}
