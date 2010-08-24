package com.qcadoo.mes.core.data.beans;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public final class DictionaryItem {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    private Dictionary dictionary;

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

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(final Dictionary dictionary) {
        this.dictionary = dictionary;
    }

}
