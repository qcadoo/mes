package com.qcadoo.model.internal.dictionaries;

import java.util.List;

public class DictionariesDictionary {

    private Long id;

    private String name;

    private String label;

    private List<DictionariesDictionaryItem> dictionaryItems;

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

    public List<DictionariesDictionaryItem> getDictionaryItems() {
        return dictionaryItems;
    }

    public void setDictionaryItems(final List<DictionariesDictionaryItem> dictionaryItems) {
        this.dictionaryItems = dictionaryItems;
    }

}
