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

    private String description;

    private boolean deleted;

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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

}
