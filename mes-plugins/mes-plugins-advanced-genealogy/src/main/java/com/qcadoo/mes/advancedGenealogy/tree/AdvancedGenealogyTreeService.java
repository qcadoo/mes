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
import static com.qcadoo.mes.advancedGenealogy.constants.BatchFields.GENEALOGY_TREE_NODE_LABEL;
import static com.qcadoo.mes.advancedGenealogy.constants.BatchFields.NUMBER;
import static com.qcadoo.mes.advancedGenealogy.constants.BatchFields.PARENT;
import static com.qcadoo.mes.advancedGenealogy.constants.BatchFields.PRIORITY;
import static com.qcadoo.mes.advancedGenealogy.constants.BatchFields.PRODUCT;
import static com.qcadoo.mes.advancedGenealogy.constants.BatchFields.SUPPLIER;
import static com.qcadoo.mes.advancedGenealogy.constants.BatchFields.TRACKING_RECORDS;
import static com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordFields.STATE;
import static com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordFields.USED_BATCHES_SIMPLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordFields;
import com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordType;
import com.qcadoo.mes.advancedGenealogy.constants.UsedBatchSimpleFields;
import com.qcadoo.mes.advancedGenealogy.states.constants.TrackingRecordState;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.plugin.api.PluginAccessor;

@Service
public class AdvancedGenealogyTreeService {

    private static final String L_BATCH = "batch";

    private static final String L_ORDER = "order";

    private static final String L_NUMBER = "number";

    private static final String L_ADVANCED_GENEALOGY_FOR_ORDERS = "advancedGenealogyForOrders";

    @Autowired
    private PluginAccessor pluginAccessor;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    public List<Entity> getProducedFromTree(final Entity batch, final boolean includeDrafts, final boolean makeIdsUnique) {
        List<Entity> tree = new ArrayList<>();

        addChild(tree, batch, makeIdsUnique);

        generateProducedFromTree(batch, tree, includeDrafts, makeIdsUnique);

        return tree;
    }

    public List<Entity> getUsedToProduceTree(final Entity batch, final boolean includeDrafts, final boolean makeIdsUnique) {
        List<Entity> tree = new ArrayList<>();

        long realParentId = batch.getId();

        addChild(tree, batch, true);

        generateUsedToProduceTree(batch, tree, includeDrafts, true, realParentId);

        return tree;
    }

    private boolean addChild(final List<Entity> tree, final Entity child, final boolean makeIdsUnique) {
        child.setField(PARENT, null);
        String genealogyTreeNodeLabel = createGenealogyTreeNodeLabel(child);
        child.setField(GENEALOGY_TREE_NODE_LABEL, genealogyTreeNodeLabel);
        return addToList(tree, child, makeIdsUnique);
    }

    private String createGenealogyTreeNodeLabel(Entity batch) {
        Entity product = batch.getBelongsToField(PRODUCT);
        Entity supplier = batch.getBelongsToField(SUPPLIER);
        StringBuilder sb = new StringBuilder();
        sb.append(" - ");
        sb.append(product.getStringField(ProductFields.NAME));
        sb.append(" (");
        sb.append(product.getStringField(ProductFields.NUMBER));
        sb.append(')');
        if (supplier != null) {
            sb.append(" - ");
            sb.append(supplier.getStringField(CompanyFields.NAME));
        }
        String orders = getOrdersForBatch(batch);
        if (!orders.isEmpty()) {
            sb.append(" - ");
            sb.append(translationService.translate("advancedGenealogy.batch.report.order", LocaleContextHolder.getLocale()));
            sb.append(AdvancedGenealogyConstants.L_SPACER);
            sb.append(orders);
        }
        return sb.toString();
    }

    private boolean addChild(final List<Entity> tree, final Entity child, final Entity parent, final boolean makeIdsUnique) {
        child.setField(PARENT, parent);
        String genealogyTreeNodeLabel = createGenealogyTreeNodeLabel(child);
        child.setField(GENEALOGY_TREE_NODE_LABEL, genealogyTreeNodeLabel);
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
            if (child.getField(PARENT).equals(entity.getField(PARENT))
                    && entity.getField(NUMBER).equals(child.getField(NUMBER))) {
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
                String entityType = trackingRecord.getStringField(ENTITY_TYPE);
                if (entityType == null) {
                    continue;
                }

                String state = trackingRecord.getStringField(STATE);

                if (!TrackingRecordState.ACCEPTED.getStringValue().equals(state)) {
                    boolean isDraftAndWeIncludeDrafts = TrackingRecordState.DRAFT.getStringValue().equals(state) && includeDrafts;

                    if (!isDraftAndWeIncludeDrafts) {
                        continue;
                    }
                }

                if (TrackingRecordType.SIMPLE.equals(entityType)) {
                    EntityList usedBatches = trackingRecord.getHasManyField(USED_BATCHES_SIMPLE);
                    for (Entity usedBatch : usedBatches) {
                        Entity batch = usedBatch.getBelongsToField(UsedBatchSimpleFields.BATCH);
                        if (batch.getId().equals(realParentId)) {
                            long realId = producedBatch.getId();
                            boolean addedChild = addChild(tree, producedBatch, parent, makeIdsUnique);
                            if (addedChild) {
                                generateUsedToProduceTree(producedBatch, tree, includeDrafts, makeIdsUnique, realId);
                            }
                        }
                    }
                } else if (TrackingRecordType.FOR_ORDER.equals(entityType) && isEnabled(L_ADVANCED_GENEALOGY_FOR_ORDERS)) {
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
                    Entity batch = usedBatch.getBelongsToField(UsedBatchSimpleFields.BATCH);
                    boolean addedChild = addChild(tree, batch, producedBatch, makeIdsUnique);
                    if (addedChild) {
                        generateProducedFromTree(batch, tree, includeDrafts, makeIdsUnique);
                    }
                }
            } else if (TrackingRecordType.FOR_ORDER.equals(type) && isEnabled(L_ADVANCED_GENEALOGY_FOR_ORDERS)) {
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

    public String getOrdersForBatch(Entity batch) {
        if (isEnabled(L_ADVANCED_GENEALOGY_FOR_ORDERS)) {
            return batch.getHasManyField(TRACKING_RECORDS).stream().map(e -> e.getBelongsToField(L_ORDER))
                    .filter(Objects::nonNull).map(e -> e.getStringField(L_NUMBER)).distinct().collect(Collectors.joining(", "));
        } else {
            return "";
        }
    }

}
