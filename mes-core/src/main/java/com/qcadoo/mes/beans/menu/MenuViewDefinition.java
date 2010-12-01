/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
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
