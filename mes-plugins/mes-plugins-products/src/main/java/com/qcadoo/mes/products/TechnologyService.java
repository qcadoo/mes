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

package com.qcadoo.mes.products;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.internal.EntityList;
import com.qcadoo.mes.internal.EntityTree;
import com.qcadoo.mes.internal.EntityTreeNode;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.search.RestrictionOperator;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.products.print.ReportDataService;
import com.qcadoo.mes.products.util.NumberGeneratorService;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.mes.view.components.grid.GridComponentState;
import com.qcadoo.mes.view.components.lookup.LookupComponentState;
import com.qcadoo.mes.view.components.tree.TreeComponentState;

@Service
public final class TechnologyService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private ReportDataService reportDataService;

    public boolean clearMasterOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("master", false);
        return true;
    }

    public boolean checkTechnologyDefault(final DataDefinition dataDefinition, final Entity entity) {
        Boolean master = (Boolean) entity.getField("master");

        if (!master) {
            return true;
        }

        SearchCriteriaBuilder searchCriteria = dataDefinition.find().withMaxResults(1)
                .restrictedWith(Restrictions.eq(dataDefinition.getField("master"), true))
                .restrictedWith(Restrictions.belongsTo(dataDefinition.getField("product"), entity.getField("product")));

        if (entity.getId() != null) {
            searchCriteria.restrictedWith(Restrictions.idRestriction(entity.getId(), RestrictionOperator.NE));
        }

        SearchResult searchResult = searchCriteria.list();

        if (searchResult.getTotalNumberOfEntities() == 0) {
            return true;
        } else {
            entity.addError(dataDefinition.getField("master"), "products.validate.global.error.default");
            return false;
        }
    }

    public void loadProductsForReferencedTechnology(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof TreeComponentState)) {
            return;
        }

        TreeComponentState tree = (TreeComponentState) state;

        if (tree.getSelectedEntityId() == null) {
            return;
        }

        Entity operationComponent = dataDefinitionService.get("products", "technologyOperationComponent").get(
                tree.getSelectedEntityId());

        if (!"referenceTechnology".equals(operationComponent.getStringField("entityType"))) {
            return;
        }

        Entity technology = operationComponent.getBelongsToField("referenceTechnology");
        EntityTree operations = technology.getTreeField("operationComponents");
        Entity rootOperation = operations.getRoot();

        GridComponentState outProductsGrid = (GridComponentState) viewDefinitionState.getComponentByReference("outProducts");
        GridComponentState inProductsGrid = (GridComponentState) viewDefinitionState.getComponentByReference("inProducts");

        if (rootOperation != null) {
            outProductsGrid.setEntities(rootOperation.getHasManyField("operationProductOutComponents"));
        }

        Map<Entity, BigDecimal> inProductsWithCount = new LinkedHashMap<Entity, BigDecimal>();
        List<Entity> inProducts = new ArrayList<Entity>();

        reportDataService.countQuantityForProductsIn(inProductsWithCount, technology, BigDecimal.ONE, false);

        for (Map.Entry<Entity, BigDecimal> inProductWithCount : inProductsWithCount.entrySet()) {
            Entity inProduct = new DefaultEntity("products", "operationProductInComponent");
            inProduct.setField("operationComponent", rootOperation);
            inProduct.setField("product", inProductWithCount.getKey());
            inProduct.setField("quantity", inProductWithCount.getValue());
            inProducts.add(inProduct);
        }

        inProductsGrid.setEntities(inProducts);
        inProductsGrid.setEnabled(false);
        outProductsGrid.setEnabled(false);
    }

    public void checkAttributesReq(final ViewDefinitionState viewDefinitionState, final Locale locale) {

        FormComponentState form = (FormComponentState) viewDefinitionState.getComponentByReference("form");

        if (form.getEntityId() != null) {
            // form is already saved
            return;
        }

        SearchResult searchResult = dataDefinitionService.get("genealogies", "currentAttribute").find().withMaxResults(1).list();
        Entity currentAttribute = null;

        if (searchResult.getEntities().size() > 0) {
            currentAttribute = searchResult.getEntities().get(0);
        }

        if (currentAttribute != null) {

            Boolean shiftReq = (Boolean) currentAttribute.getField("shiftReq");
            if (shiftReq != null && shiftReq) {
                FieldComponentState req = (FieldComponentState) viewDefinitionState
                        .getComponentByReference("shiftFeatureRequired");
                req.setFieldValue("1");
            }

            Boolean postReq = (Boolean) currentAttribute.getField("postReq");
            if (postReq != null && postReq) {
                FieldComponentState req = (FieldComponentState) viewDefinitionState
                        .getComponentByReference("postFeatureRequired");
                req.setFieldValue("1");
            }

            Boolean otherReq = (Boolean) currentAttribute.getField("otherReq");
            if (otherReq != null && otherReq) {
                FieldComponentState req = (FieldComponentState) viewDefinitionState
                        .getComponentByReference("otherFeatureRequired");
                req.setFieldValue("1");
            }
        }

    }

    public void checkBatchNrReq(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof LookupComponentState)) {
            throw new IllegalStateException("component is not lookup");
        }

        LookupComponentState product = (LookupComponentState) state;

        FieldComponentState batchReq = (FieldComponentState) viewDefinitionState.getComponentByReference("batchRequired");

        if (product.getFieldValue() != null) {
            if (batchRequired(product.getFieldValue())) {
                batchReq.setFieldValue("1");
            } else {
                batchReq.setFieldValue("0");
            }
        }
    }

    public void generateTechnologyNumber(final ViewDefinitionState state, final ComponentState componentState, final String[] args) {
        if (!(componentState instanceof FieldComponentState)) {
            throw new IllegalStateException("component is not FieldComponentState");
        }
        FieldComponentState number = (FieldComponentState) state.getComponentByReference("number");
        FieldComponentState productState = (FieldComponentState) componentState;

        if (!numberGeneratorService.checkIfShouldInsertNumber(state)) {
            return;
        }
        if (productState.getFieldValue() != null) {
            Entity product = getProductById((Long) productState.getFieldValue());
            if (product != null) {
                String numberValue = product.getField("number") + "-"
                        + numberGeneratorService.generateNumber(state, "technology", 3);
                number.setFieldValue(numberValue);
            }
        }
    }

    private Entity getProductById(final Long productId) {
        DataDefinition instructionDD = dataDefinitionService.get("products", "product");

        SearchCriteriaBuilder searchCriteria = instructionDD.find().withMaxResults(1)
                .restrictedWith(Restrictions.idRestriction(productId, RestrictionOperator.EQ));

        SearchResult searchResult = searchCriteria.list();
        if (searchResult.getTotalNumberOfEntities() == 1) {
            return searchResult.getEntities().get(0);
        }
        return null;
    }

    private boolean batchRequired(final Long selectedProductId) {
        Entity product = getProductById(selectedProductId);
        if (product != null) {
            return (Boolean) product.getField("genealogyBatchReq");
        } else {
            return false;
        }
    }

    public void fillBatchRequiredForTechnology(final DataDefinition dataDefinition, final Entity entity) {
        if ((Boolean) entity.getField("batchRequired")) {
            Entity technology = entity.getBelongsToField("operationComponent").getBelongsToField("technology");
            DataDefinition technologyInDef = dataDefinitionService.get("products", "technology");
            Entity technologyEntity = technologyInDef.get(technology.getId());
            if (!(Boolean) technologyEntity.getField("batchRequired")) {
                technologyEntity.setField("batchRequired", true);
                technologyInDef.save(technologyEntity);
            }
        }
    }

    public boolean copyReferencedTechnology(final DataDefinition dataDefinition, final Entity entity) {
        if (!"referenceTechnology".equals(entity.getField("entityType"))) {
            return true;
        }
        if ("02copy".equals(entity.getField("referenceMode"))) {
            Entity technology = entity.getBelongsToField("referenceTechnology");

            EntityTreeNode root = technology.getTreeField("operationComponents").getRoot();

            Entity copiedRoot = copyReferencedTechnologyOperations(root, entity.getBelongsToField("technology"));

            entity.setField("entityType", "operation");
            entity.setField("referenceTechnology", null);
            entity.setField("qualityControlRequired", copiedRoot.getField("qualityControlRequired"));
            entity.setField("operation", copiedRoot.getField("operation"));
            entity.setField("children", copiedRoot.getField("children"));
            entity.setField("operationProductInComponents", copiedRoot.getField("operationProductInComponents"));
            entity.setField("operationProductOutComponents", copiedRoot.getField("operationProductOutComponents"));
        } else if (entity.getField("referenceTechnology") != null) {
            Entity technology = entity.getBelongsToField("technology");
            Entity referencedTechnology = entity.getBelongsToField("referenceTechnology");

            if (technology.getId().equals(referencedTechnology.getId())) {
                entity.addError(dataDefinition.getField("referenceTechnology"),
                        "products.technologyReferenceTechnologyComponent.error.cyclicDependency");
                return false;
            }
        }

        return true;
    }

    private Entity copyReferencedTechnologyOperations(final EntityTreeNode node, final Entity technology) {
        Entity copy = node.copy();
        copy.setId(null);
        copy.setField("parent", null);
        copy.setField("technology", technology);
        copy.setField("children", copyOperationsChildren(node.getChildren(), technology));
        copy.setField("operationProductInComponents", copyProductComponents(copy.getHasManyField("operationProductInComponents")));
        copy.setField("operationProductOutComponents",
                copyProductComponents(copy.getHasManyField("operationProductOutComponents")));
        return copy;
    }

    private List<Entity> copyProductComponents(final EntityList entities) {
        List<Entity> copies = new ArrayList<Entity>();
        for (Entity entity : entities) {
            Entity copy = entity.copy();
            copy.setId(null);
            copy.setField("operationComponent", null);
            copies.add(copy);
        }
        return copies;
    }

    private List<Entity> copyOperationsChildren(final List<EntityTreeNode> entities, final Entity technology) {
        List<Entity> copies = new ArrayList<Entity>();
        for (EntityTreeNode entity : entities) {
            copies.add(copyReferencedTechnologyOperations(entity, technology));
        }
        return copies;
    }

    public void setQualityControlTypeForTechnology(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getField("qualityControlRequired") != null && (Boolean) entity.getField("qualityControlRequired")) {
            Entity technology = entity.getBelongsToField("technology");
            DataDefinition technologyInDef = dataDefinitionService.get("products", "technology");
            Entity technologyEntity = technologyInDef.get(technology.getId());
            if (technologyEntity.getField("qualityControlType") == null
                    || !technologyEntity.getField("qualityControlType").equals("04forOperation")) {
                technologyEntity.setField("qualityControlType", "04forOperation");
                technologyInDef.save(technologyEntity);
            }
        }
    }

    public void disableBatchRequiredForTechnology(final ViewDefinitionState state, final Locale locale) {
        FormComponentState form = (FormComponentState) state.getComponentByReference("form");
        if (form.getFieldValue() != null) {
            FieldComponentState batchRequired = (FieldComponentState) state.getComponentByReference("batchRequired");
            if (checkProductInComponentsBatchRequired((Long) form.getFieldValue())) {
                batchRequired.setEnabled(false);
                batchRequired.setFieldValue("1");
                batchRequired.requestComponentUpdateState();
            } else {
                batchRequired.setEnabled(true);
            }
        }

    }

    public boolean validateTechnologyOperationComponent(final DataDefinition dataDefinition, final Entity entity) {
        boolean isError = false;
        if ("operation".equals(entity.getStringField("entityType"))) {
            if (entity.getField("operation") == null) {
                entity.addError(dataDefinition.getField("operation"), "core.validate.field.error.missing");
                isError = true;
            }
        } else if ("referenceTechnology".equals(entity.getStringField("entityType"))) {
            if (entity.getField("referenceTechnology") == null) {
                entity.addError(dataDefinition.getField("referenceTechnology"), "core.validate.field.error.missing");
                isError = true;
            }
            if (entity.getField("referenceMode") == null) {
                entity.addError(dataDefinition.getField("referenceMode"), "core.validate.field.error.missing");
                isError = true;
            }
        } else {
            throw new IllegalStateException("unknown entityType");
        }
        return isError;
    }

    public void setTechnologyReferenceOperationComponentViewRequiredFields(final ViewDefinitionState state, final Locale locale) {
        ((FieldComponentState) state.getComponentByReference("operation")).setRequired(true);
    }

    public void setTechnologyReferenceTechnologyComponentViewRequiredFields(final ViewDefinitionState state, final Locale locale) {
        ((FieldComponentState) state.getComponentByReference("technology")).setRequired(true);
        ((FieldComponentState) state.getComponentByReference("referenceMode")).setRequired(true);
    }

    private boolean checkProductInComponentsBatchRequired(final Long entityId) {
        SearchResult searchResult = dataDefinitionService.get("products", "operationProductInComponent").find()
                .restrictedWith(Restrictions.eq("operationComponent.technology.id", entityId))
                .restrictedWith(Restrictions.eq("batchRequired", true)).withMaxResults(1).list();

        return (searchResult.getTotalNumberOfEntities() > 0);

    }

    public void changeQualityControlType(final ViewDefinitionState state, final Locale locale) {
        FormComponentState form = (FormComponentState) state.getComponentByReference("form");
        if (form.getFieldValue() != null) {
            FieldComponentState qualityControlType = (FieldComponentState) state.getComponentByReference("qualityControlType");
            if (checkOperationQualityControlRequired((Long) form.getFieldValue())) {
                qualityControlType.setFieldValue("04forOperation");
                qualityControlType.setEnabled(false);
                qualityControlType.requestComponentUpdateState();
            } else {
                qualityControlType.setEnabled(true);
            }
        }

    }

    private boolean checkOperationQualityControlRequired(final Long entityId) {
        SearchResult searchResult = dataDefinitionService.get("products", "technologyOperationComponent").find()
                .restrictedWith(Restrictions.eq("technology.id", entityId))
                .restrictedWith(Restrictions.eq("qualityControlRequired", true)).withMaxResults(1).list();

        return (searchResult.getTotalNumberOfEntities() > 0);

    }
}
