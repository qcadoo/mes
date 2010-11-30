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
@Table(name = "menu_view_definition")
public class MenuViewDefinition {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String menuName;

    @Column(nullable = false)
    private String viewName;

    @Column(nullable = false)
    private String pluginIdentifier;

    @OneToMany(mappedBy = "viewDefinition", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<MenuMenuViewDefinitionItem> menuViewDefinitionItems;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(final String menuName) {
        this.menuName = menuName;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(final String viewName) {
        this.viewName = viewName;
    }

    public String getPluginIdentifier() {
        return pluginIdentifier;
    }

    public void setPluginIdentifier(final String pluginIdentifier) {
        this.pluginIdentifier = pluginIdentifier;
    }

    public List<MenuMenuViewDefinitionItem> getMenuViewDefinitionItems() {
        return menuViewDefinitionItems;
    }

    public void setMenuViewDefinitionItems(final List<MenuMenuViewDefinitionItem> menuViewDefinitionItems) {
        this.menuViewDefinitionItems = menuViewDefinitionItems;
    }

}
