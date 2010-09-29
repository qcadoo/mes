package com.qcadoo.mes.core.view;

import java.util.Map;

public final class ComponentOption {

    private final String type;

    private final Map<String, String> attributes;

    public ComponentOption(final String type, final Map<String, String> attributes) {
        this.type = type;
        this.attributes = attributes;
    }

    public String getAtrributeValue(final String name) {
        return attributes.get(name);
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        if (attributes.size() == 1) {
            return attributes.values().toArray(new String[1])[0];
        } else {
            return attributes.get("value");
        }
    }

}
