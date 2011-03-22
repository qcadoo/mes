package com.qcadoo.model.beans.menu;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "menu_category")
public class MenuCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String pluginIdentifier;

    @Column
    private String name;

    @Column
    private boolean active;

    @Column
    private int succession;

    @OneToMany(mappedBy = "category")
    public List<MenuItem> items;

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

    public List<MenuItem> getItems() {
        return items;
    }

    public void setItems(final List<MenuItem> items) {
        this.items = items;
    }

}
