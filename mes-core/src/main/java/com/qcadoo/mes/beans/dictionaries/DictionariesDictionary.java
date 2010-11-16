/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.beans.dictionaries;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "dictionaries_dictionary")
public class DictionariesDictionary {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

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

    @OneToMany(mappedBy = "dictionary", fetch = FetchType.LAZY)
    private List<DictionariesDictionaryItem> dictionaryItems;

    public List<DictionariesDictionaryItem> getDictionaryItems() {
        return dictionaryItems;
    }

    public void setDictionaryItems(final List<DictionariesDictionaryItem> dictionaryItems) {
        this.dictionaryItems = dictionaryItems;
    }

}
