package com.qcadoo.mes.view.menu.ribbon;

import java.util.List;

public final class Ribbon {

    private String name;

    private List<RibbonGroup> groups;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<RibbonGroup> getGroups() {
        return groups;
    }

    public void setGroups(final List<RibbonGroup> groups) {
        this.groups = groups;
    }

}
