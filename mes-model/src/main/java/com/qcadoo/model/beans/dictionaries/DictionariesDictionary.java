package com.qcadoo.model.beans.dictionaries;

import java.util.Set;

public class DictionariesDictionary {

    private Long id;

    private String name;

    // TODO take me from properties file
    private String label;

    private Set<DictionariesDictionaryItem> dictionaryItems;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public Set<DictionariesDictionaryItem> getDictionaryItems() {
        return dictionaryItems;
    }

    public void setDictionaryItems(final Set<DictionariesDictionaryItem> dictionaryItems) {
        this.dictionaryItems = dictionaryItems;
    }

}
