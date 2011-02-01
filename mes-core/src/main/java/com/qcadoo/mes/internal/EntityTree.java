/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.internal;

import java.util.AbstractList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;

public final class EntityTree extends AbstractList<Entity> {

    private final DataDefinition dataDefinition;

    private final Long belongsToId;

    private final FieldDefinition joinFieldDefinition;

    private List<Entity> entities = null;

    private EntityTreeNode root = null;

    public EntityTree(final DataDefinition dataDefinition, final String joinFieldName, final Long belongsToId) {
        this.dataDefinition = dataDefinition;
        this.joinFieldDefinition = dataDefinition.getField(joinFieldName);
        this.belongsToId = belongsToId;

        if (this.belongsToId == null) {
            entities = Collections.emptyList();
        }
    }

    private void loadEntities() {
        if (entities == null) {
            entities = find().list().getEntities();

            Map<Long, EntityTreeNode> entitiesById = new HashMap<Long, EntityTreeNode>();

            for (Entity entity : entities) {
                entitiesById.put(entity.getId(), new EntityTreeNode(entity));
            }

            for (EntityTreeNode entity : entitiesById.values()) {
                Entity parent = entity.getBelongsToField("parent");

                if (parent == null) {
                    if (root != null) {
                        throw new IllegalStateException("Treen cannot have multiple roots");
                    }

                    root = entity;
                } else {
                    if (entitiesById.get(parent.getId()) == null) {
                        throw new IllegalStateException("Parent for tree node not found");
                    }

                    entitiesById.get(parent.getId()).addChild(entity);
                }
            }

            if (root == null) {
                throw new IllegalStateException("Root for tree not found");
            }
        }
    }

    public SearchCriteriaBuilder find() {
        return dataDefinition.find().restrictedWith(Restrictions.belongsTo(joinFieldDefinition, belongsToId));
    }

    @Override
    public Entity get(final int index) {
        if (entities == null) {
            loadEntities();
        }
        return entities.get(index);
    }

    @Override
    public int size() {
        if (entities == null) {
            loadEntities();
        }
        return entities.size();
    }

    public EntityTreeNode getRoot() {
        if (entities == null) {
            loadEntities();
        }
        return root;
    }

    @Override
    public String toString() {
        return "EntityTree[" + dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName() + "]["
                + joinFieldDefinition.getName() + "=" + belongsToId + "]";
    }

}
