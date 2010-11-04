package com.qcadoo.mes.beans.menu;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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

    @Column(nullable = false)
    private boolean deleted;

    @OneToMany(mappedBy = "viewDefinition", fetch = FetchType.LAZY)
    private List<MenuMenuViewDefinitionItem> menuViewDefinitionItems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public String getPluginIdentifier() {
        return pluginIdentifier;
    }

    public void setPluginIdentifier(String pluginIdentifier) {
        this.pluginIdentifier = pluginIdentifier;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public List<MenuMenuViewDefinitionItem> getMenuViewDefinitionItems() {
        return menuViewDefinitionItems;
    }

    public void setMenuViewDefinitionItems(List<MenuMenuViewDefinitionItem> menuViewDefinitionItems) {
        this.menuViewDefinitionItems = menuViewDefinitionItems;
    }

}
