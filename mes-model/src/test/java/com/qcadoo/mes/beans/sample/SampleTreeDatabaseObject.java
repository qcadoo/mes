/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.beans.sample;

import java.util.List;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

public class SampleTreeDatabaseObject implements HibernateProxy {

    private static final long serialVersionUID = -3703171948011903671L;

    private Long id;

    private String name;

    private List<SampleTreeDatabaseObject> children;

    private SampleTreeDatabaseObject parent;

    private SampleParentDatabaseObject owner;

    public SampleTreeDatabaseObject() {
    }

    public SampleTreeDatabaseObject(final Long id) {
        this.id = id;
    }

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

    public List<SampleTreeDatabaseObject> getChildren() {
        return children;
    }

    public void setChildren(final List<SampleTreeDatabaseObject> children) {
        this.children = children;
    }

    public SampleTreeDatabaseObject getParent() {
        return parent;
    }

    public void setParent(final SampleTreeDatabaseObject parent) {
        this.parent = parent;
    }

    public SampleParentDatabaseObject getOwner() {
        return owner;
    }

    public void setOwner(final SampleParentDatabaseObject owner) {
        this.owner = owner;
    }

    @Override
    public Object writeReplace() {
        return null;
    }

    @Override
    public LazyInitializer getHibernateLazyInitializer() {
        return null;
    }

}
