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
@Table(name = "menu_menu_category")
public class MenuMenuCategory {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    private String translationName;

    private boolean active;

    @Column(nullable = false)
    private boolean deleted;

    private Integer categoryOrder;

    @OneToMany(mappedBy = "menuCategory", fetch = FetchType.LAZY)
    private List<MenuMenuViewDefinitionItem> viewDefinitionItems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public List<MenuMenuViewDefinitionItem> getViewDefinitionItems() {
        return viewDefinitionItems;
    }

    public void setViewDefinitionItems(List<MenuMenuViewDefinitionItem> viewDefinitionItems) {
        this.viewDefinitionItems = viewDefinitionItems;
    }

    public Integer getCategoryOrder() {
        return categoryOrder;
    }

    public void setCategoryOrder(Integer categoryOrder) {
        this.categoryOrder = categoryOrder;
    }

    public String getTranslationName() {
        return translationName;
    }

    public void setTranslationName(String translationName) {
        this.translationName = translationName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
