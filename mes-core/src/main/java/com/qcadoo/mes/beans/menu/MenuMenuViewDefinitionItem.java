/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.beans.menu;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "menu_menu_view_definition_item")
public class MenuMenuViewDefinitionItem {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    private String translationName;

    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private MenuMenuCategory menuCategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private MenuViewDefinition viewDefinition;

    private Integer itemOrder;

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

    public MenuMenuCategory getMenuCategory() {
        return menuCategory;
    }

    public void setMenuCategory(final MenuMenuCategory menuCategory) {
        this.menuCategory = menuCategory;
    }

    public Integer getItemOrder() {
        return itemOrder;
    }

    public void setItemOrder(final Integer itemOrder) {
        this.itemOrder = itemOrder;
    }

    public MenuViewDefinition getViewDefinition() {
        return viewDefinition;
    }

    public void setViewDefinition(final MenuViewDefinition viewDefinition) {
        this.viewDefinition = viewDefinition;
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
