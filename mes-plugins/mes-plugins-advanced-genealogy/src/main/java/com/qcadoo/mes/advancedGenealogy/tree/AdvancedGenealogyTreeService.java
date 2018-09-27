/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.advancedGenealogy.tree;

import static com.qcadoo.mes.advancedGenealogy.constants.BatchFields.ENTITY_TYPE;
import static com.qcadoo.mes.advancedGenealogy.constants.BatchFields.NUMBER;
import static com.qcadoo.mes.advancedGenealogy.constants.BatchFields.PARENT;
import static com.qcadoo.mes.advancedGenealogy.constants.BatchFields.PRIORITY;
import static com.qcadoo.mes.advancedGenealogy.constants.BatchFields.SUPPLIER;
import static com.qcadoo.mes.advancedGenealogy.constants.BatchFields.TRACKING_RECORDS;
import static com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordFields.STATE;
import static com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordFields.USED_BATCHES_SIMPLE;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordFields;
import com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordType;
import com.qcadoo.mes.advancedGenealogy.states.constants.TrackingRecordState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.plugin.api.PluginAccessor;

@Service
public class AdvancedGenealogyTreeService {

    private static final String L_BATCH = "batch";

    @Autowired
    private PluginAccessor pluginAccessor;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public List<Entity> getProducedFromTree(final Entity batch, final boolean includeDrafts, final boolean makeIdsUnique) {
        List<Entity> tree = new ArrayList<Entity>();

        addChild(tree, batch, makeIdsUnique);

        generateProducedFromTree(batch, tree, includeDrafts, makeIdsUnique);

        return tree;
    }

    public List<Entity> getUsedToProduceTree(final Entity batch, final boolean includeDrafts, final boolean makeIdsUnique) {
        List<Entity> tree = new ArrayList<Entity>();

        long realParentId = batch.getId();

        addChild(tree, batch, true);

        generateUsedToProduceTree(batch, tree, includeDrafts, true, realParentId);

        return tree;
    }

    private boolean addChild(final List<Entity> tree, final Entity child, final boolean makeIdsUnique) {
        child.setField(PARENT, null);
        return addToList(tree, child, makeIdsUnique);
    }

    private boolean addChild(final List<Entity> tree, final Entity child, final Entity parent, final boolean makeIdsUnique) {
        child.setField(PARENT, parent);
        return addToList(tree, child, makeIdsUnique);
    }

    private boolean addToList(final List<Entity> tree, final Entity child, final boolean makeIdsUnique) {
        child.setField(PRIORITY, 1);
        child.setField(ENTITY_TYPE, L_BATCH);

        if (checkIfTreeContainsEntity(tree, child)) {
            return false;
        } else {
            if (makeIdsUnique) {
                child.setId((long) tree.size());
            }
            tree.add(child);
            return true;
        }
    }

    private boolean checkIfTreeContainsEntity(final List<Entity> tree, final Entity child) {
        for (Entity entity : tree) {
            if (child.getField(PARENT).equals(entity.getField(PARENT)) && entity.getField(NUMBER).equals(child.getField(NUMBER))) {
                Entity supplierChild = child.getBelongsToField(SUPPLIER);
                Entity supplierEntity = entity.getBelongsToField(SUPPLIER);
                if (supplierChild != null && supplierEntity != null && supplierChild.equals(supplierEntity)) {
                    return true;
                }
                return true;
            }
        }
        return false;
    }

    private void generateUsedToProduceTree(final Entity parent, final List<Entity> tree, final boolean includeDrafts,
            final boolean makeIdsUnique, final long realParentId) {
        List<Entity> batches = getDD().find().list().getEntities();

        for (Entity producedBatch : batches) {
            EntityList trackingRecords = producedBatch.getHasManyField(TRACKING_RECORDS);
            for (Entity trackingRecord : trackingRecords) {
                String type = trackingRecord.getStringField(ENTITY_TYPE);
                if (type == null) {
                    continue;
                }

                String state = trackingRecord.getStringField(STATE);

                if (!TrackingRecordState.ACCEPTED.getStringValue().equals(state)) {
                    boolean isDraftAndWeIncludeDrafts = TrackingRecordState.DRAFT.getStringValue().equals(state) && includeDrafts;

                    if (!isDraftAndWeIncludeDrafts) {
                        continue;
                    }
                }

                if (TrackingRecordType.SIMPLE.equals(type)) {
                    EntityList usedBatches = trackingRecord.getHasManyField(USED_BATCHES_SIMPLE);
                    for (Entity usedBatch : usedBatches) {
                        Entity batch = usedBatch.getBelongsToField(L_BATCH);
                        if (batch.getId().equals(realParentId)) {
                            long realId = producedBatch.getId();
                            boolean addedChild = addChild(tree, producedBatch, parent, makeIdsUnique);
                            if (addedChild) {
                                generateUsedToProduceTree(producedBatch, tree, includeDrafts, makeIdsUnique, realId);
                            }
                        }
                    }
                } else if (TrackingRecordType.FOR_ORDER.equals(type) && isEnabled("advancedGenealogyForOrders")) {
                    EntityList genealogyProductInComponents = trackingRecord.getHasManyField("genealogyProductInComponents");
                    for (Entity genealogyProductInComponent : genealogyProductInComponents) {
                        EntityList productInBatches = genealogyProductInComponent.getHasManyField("productInBatches");
                        for (Entity producedInBatch : productInBatches) {
                            Entity batch = producedInBatch.getBelongsToField(L_BATCH);
                            if (batch == null) {
                                continue;
                            }
                            if (batch.getId().equals(realParentId)) {
                                long realId = producedBatch.getId();
                                boolean addedChild = addChild(tree, producedBatch, parent, makeIdsUnique);
                                if (addedChild) {
                                    generateUsedToProduceTree(producedBatch, tree, includeDrafts, makeIdsUnique, realId);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void generateProducedFromTree(final Entity producedBatch, final List<Entity> tree, final boolean includeDrafts,
            final boolean makeIdsUnique) {
        EntityList trackingRecords = producedBatch.getHasManyField(BatchFields.TRACKING_RECORDS);
        for (Entity trackingRecord : trackingRecords) {
            String type = trackingRecord.getStringField(TrackingRecordFields.ENTITY_TYPE);
            if (type == null) {
                continue;
            }

            String state = trackingRecord.getStringField(STATE);

            if (!TrackingRecordState.ACCEPTED.getStringValue().equals(state)) {
                boolean isDraftAndWeIncludeDrafts = TrackingRecordState.DRAFT.getStringValue().equals(state) && includeDrafts;

                if (!isDraftAndWeIncludeDrafts) {
                    continue;
                }
            }

            if (TrackingRecordType.SIMPLE.equals(type)) {
                EntityList usedBatches = trackingRecord.getHasManyField(USED_BATCHES_SIMPLE);
                for (Entity usedBatch : usedBatches) {
                    Entity batch = usedBatch.getBelongsToField(L_BATCH);
                    boolean addedChild = addChild(tree, batch, producedBatch, makeIdsUnique);
                    if (addedChild) {
                        generateProducedFromTree(batch, tree, includeDrafts, makeIdsUnique);
                    }
                }
            } else if (TrackingRecordType.FOR_ORDER.equals(type) && isEnabled("advancedGenealogyForOrders")) {
                EntityList genealogyProductInComponents = trackingRecord.getHasManyField("genealogyProductInComponents");
                for (Entity genealogyProductInComponent : genealogyProductInComponents) {
                    EntityList productInBatches = genealogyProductInComponent.getHasManyField("productInBatches");
                    for (Entity producedInBatch : productInBatches) {
                        Entity batch = producedInBatch.getBelongsToField(L_BATCH);
                        if (batch == null) {
                            continue;
                        }
                        boolean addedChild = addChild(tree, batch, producedBatch, makeIdsUnique);
                        if (addedChild) {
                            generateProducedFromTree(batch, tree, includeDrafts, makeIdsUnique);
                        }
                    }
                }
            }
        }
    }

    private DataDefinition getDD() {
        return dataDefinitionService.get(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER, AdvancedGenealogyConstants.MODEL_BATCH);
    }

    private boolean isEnabled(final String pluginIdentifier) {
        return pluginAccessor.getPlugin(pluginIdentifier) != null;
    }
}
