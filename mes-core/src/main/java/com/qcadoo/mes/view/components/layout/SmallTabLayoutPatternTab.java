package com.qcadoo.mes.view.components.layout;

import java.util.LinkedList;
import java.util.List;

import com.qcadoo.mes.view.ComponentPattern;

public class SmallTabLayoutPatternTab {

    private String name;

    private List<ComponentPattern> components = new LinkedList<ComponentPattern>();

    public SmallTabLayoutPatternTab(String name) {
        this.name = name;
    }

    public void addComponent(ComponentPattern component) {
        components.add(component);
    }

    public String getName() {
        return name;
    }

    public List<ComponentPattern> getComponents() {
        return components;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setComponents(List<ComponentPattern> components) {
        this.components = components;
    }
}
