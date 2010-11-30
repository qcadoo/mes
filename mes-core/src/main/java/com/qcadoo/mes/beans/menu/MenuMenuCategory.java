/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.beans.menu;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Table(name = "menu_menu_category")
public class MenuMenuCategory {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    private String translationName;

    private boolean active;

    private Integer categoryOrder;

    @OneToMany(mappedBy = "menuCategory", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<MenuMenuViewDefinitionItem> viewDefinitionItems;

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

    public List<MenuMenuViewDefinitionItem> getViewDefinitionItems() {
        return viewDefinitionItems;
    }

    public void setViewDefinitionItems(final List<MenuMenuViewDefinitionItem> viewDefinitionItems) {
        this.viewDefinitionItems = viewDefinitionItems;
    }

    public Integer getCategoryOrder() {
        return categoryOrder;
    }

    public void setCategoryOrder(final Integer categoryOrder) {
        this.categoryOrder = categoryOrder;
    }

    public String getTranslationName() {
        return translationName;
    }

    public void setTranslationName(final String translationName) {
        this.translationName = translationName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

}
