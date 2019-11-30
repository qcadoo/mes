package com.qcadoo.mes.basic.imports.attribute;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class AttributeImportContainer {

    private Map<String, Map<String, List<String>>> atribiutesValuesByType = Maps.newHashMap();

    private List<String> errors = Lists.newArrayList();

    private List<String> messages = Lists.newArrayList();

    public Map<String, Map<String, List<String>>> getAtribiutesValuesByType() {
        return atribiutesValuesByType;
    }

    public void setAtribiutesValuesByType(Map<String, Map<String, List<String>>> atribiutesValuesByType) {
        this.atribiutesValuesByType = atribiutesValuesByType;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
