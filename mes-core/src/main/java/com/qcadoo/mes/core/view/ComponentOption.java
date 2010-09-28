package com.qcadoo.mes.core.view;

import java.util.Map;

public class ComponentOption {

    private final String name;

    private final Map<String, String> attributes;

    public ComponentOption(final String name, final Map<String, String> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public String getAtrributeValue(final String name) {
        return attributes.get(name);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        if (attributes.size() == 1) {
            return attributes.values().toArray(new String[1])[0];
        } else {
            return attributes.get("value");
        }
    }

}
