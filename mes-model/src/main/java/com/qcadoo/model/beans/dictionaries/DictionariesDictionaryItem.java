package com.qcadoo.model.beans.dictionaries;


public class DictionariesDictionaryItem {

    private Long id;

    private String name;

    private String description;

    private DictionariesDictionary dictionary;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public DictionariesDictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(final DictionariesDictionary dictionary) {
        this.dictionary = dictionary;
    }

}
