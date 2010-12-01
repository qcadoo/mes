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

    @Column(nullable = false)
    private boolean deleted;

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

    public MenuMenuCategory getMenuCategory() {
        return menuCategory;
    }

    public void setMenuCategory(MenuMenuCategory menuCategory) {
        this.menuCategory = menuCategory;
    }

    public Integer getItemOrder() {
        return itemOrder;
    }

    public void setItemOrder(Integer itemOrder) {
        this.itemOrder = itemOrder;
    }

    public MenuViewDefinition getViewDefinition() {
        return viewDefinition;
    }

    public void setViewDefinition(MenuViewDefinition viewDefinition) {
        this.viewDefinition = viewDefinition;
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
