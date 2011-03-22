package com.qcadoo.model.beans.menu;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "menu_item")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String pluginIdentifier;

    @Column
    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private MenuCategory category;

    @ManyToOne
    @JoinColumn(name = "view_id")
    private MenuView view;

    @Column
    private boolean active;

    @Column
    private int succession;

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

    public MenuCategory getCategory() {
        return category;
    }

    public void setCategory(final MenuCategory category) {
        this.category = category;
    }

    public MenuView getView() {
        return view;
    }

    public void setView(final MenuView view) {
        this.view = view;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public int getSuccession() {
        return succession;
    }

    public void setSuccession(final int succession) {
        this.succession = succession;
    }

    public String getPluginIdentifier() {
        return pluginIdentifier;
    }

    public void setPluginIdentifier(final String pluginIdentifier) {
        this.pluginIdentifier = pluginIdentifier;
    }

}
