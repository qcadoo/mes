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

package com.qcadoo.mes.products;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.NumberGeneratorService;
import com.qcadoo.mes.products.print.ReportDataService;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.grid.GridComponentState;
import com.qcadoo.mes.view.components.select.SelectComponentState;
import com.qcadoo.mes.view.components.tree.TreeComponentState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.search.RestrictionOperator;
import com.qcadoo.model.api.search.Restrictions;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;

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

    public void setFirstTechnologyAsDefault(final DataDefinition dataDefinition, final Entity entity) {
        if (!(Boolean) entity.getField("master")) {
            SearchCriteriaBuilder searchCriteria = dataDefinition.find().withMaxResults(1)
                    .restrictedWith(Restrictions.belongsTo(dataDefinition.getField("product"), entity.getField("product")));

            if (searchCriteria.list().getTotalNumberOfEntities() == 0) {
                entity.setField("master", Boolean.TRUE);
            }
        }
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

        GridComponentState outProductsGrid = (GridComponentState) viewDefinitionState.getComponentByReference("outProducts");
        GridComponentState inProductsGrid = (GridComponentState) viewDefinitionState.getComponentByReference("inProducts");

        if (!"referenceTechnology".equals(operationComponent.getStringField("entityType"))) {
            inProductsGrid.setIsEditable(true);
            outProductsGrid.setIsEditable(true);
            return;
        }

        Entity technology = operationComponent.getBelongsToField("referenceTechnology");
        EntityTree operations = technology.getTreeField("operationComponents");
        Entity rootOperation = operations.getRoot();

        if (rootOperation != null) {
            outProductsGrid.setEntities(rootOperation.getHasManyField("operationProductOutComponents"));
        }

        Map<Entity, BigDecimal> inProductsWithCount = new LinkedHashMap<Entity, BigDecimal>();
        List<Entity> inProducts = new ArrayList<Entity>();

        reportDataService.countQuantityForProductsIn(inProductsWithCount, technology, BigDecimal.ONE, false);

        for (Map.Entry<Entity, BigDecimal> inProductWithCount : inProductsWithCount.entrySet()) {
            Entity inProduct = dataDefinitionService.get("products", "operationProductInComponent").create();
            inProduct.setField("operationComponent", rootOperation);
            inProduct.setField("product", inProductWithCount.getKey());
            inProduct.setField("quantity", inProductWithCount.getValue());
            inProducts.add(inProduct);
        }

        inProductsGrid.setEntities(inProducts);
        inProductsGrid.setEnabled(false);
        inProductsGrid.setIsEditable(false);
        outProductsGrid.setEnabled(false);
        outProductsGrid.setIsEditable(false);
    }

    public void checkQualityControlType(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof SelectComponentState)) {
            throw new IllegalStateException("component is not select");
        }

        SelectComponentState qualityControlType = (SelectComponentState) state;

        FieldComponentState unitSamplingNr = (FieldComponentState) viewDefinitionState.getComponentByReference("unitSamplingNr");

        if (qualityControlType.getFieldValue() != null) {
            if (qualityControlType.getFieldValue().equals("02forUnit")) {
                unitSamplingNr.setRequired(true);
                unitSamplingNr.setVisible(true);
            } else {
                unitSamplingNr.setRequired(false);
                unitSamplingNr.setVisible(false);
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

    public boolean copyReferencedTechnology(final DataDefinition dataDefinition, final Entity entity) {
        if (!"referenceTechnology".equals(entity.getField("entityType")) && entity.getField("referenceTechnology") == null) {
            return true;
        }

        boolean copy = "02copy".equals(entity.getField("referenceMode"));

        Entity technology = entity.getBelongsToField("technology");
        Entity referencedTechnology = entity.getBelongsToField("referenceTechnology");

        Set<Long> technologies = new HashSet<Long>();
        technologies.add(technology.getId());

        boolean cyclic = checkForCyclicReferences(technologies, referencedTechnology, copy);

        if (cyclic) {
            entity.addError(dataDefinition.getField("referenceTechnology"),
                    "products.technologyReferenceTechnologyComponent.error.cyclicDependency");
            return false;
        }

        if (copy) {
            EntityTreeNode root = referencedTechnology.getTreeField("operationComponents").getRoot();

            Entity copiedRoot = copyReferencedTechnologyOperations(root, entity.getBelongsToField("technology"));

            entity.setField("entityType", "operation");
            entity.setField("referenceTechnology", null);
            entity.setField("qualityControlRequired", copiedRoot.getField("qualityControlRequired"));
            entity.setField("operation", copiedRoot.getField("operation"));
            entity.setField("children", copiedRoot.getField("children"));
            entity.setField("operationProductInComponents", copiedRoot.getField("operationProductInComponents"));
            entity.setField("operationProductOutComponents", copiedRoot.getField("operationProductOutComponents"));
        }

        return true;
    }

    private boolean checkForCyclicReferences(final Set<Long> technologies, final Entity referencedTechnology, final boolean copy) {
        if (!copy && technologies.contains(referencedTechnology.getId())) {
            return true;
        }

        technologies.add(referencedTechnology.getId());

        for (Entity operationComponent : referencedTechnology.getTreeField("operationComponents")) {
            if ("referenceTechnology".equals(operationComponent.getField("entityType"))) {
                boolean cyclic = checkForCyclicReferences(technologies,
                        operationComponent.getBelongsToField("referenceTechnology"), false);

                if (cyclic) {
                    return true;
                }
            }
        }

        return false;
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

    public boolean validateTechnologyOperationComponent(final DataDefinition dataDefinition, final Entity entity) {
        boolean isValid = true;
        if ("operation".equals(entity.getStringField("entityType"))) {
            if (entity.getField("operation") == null) {
                entity.addError(dataDefinition.getField("operation"), "core.validate.field.error.missing");
                isValid = false;
            }
        } else if ("referenceTechnology".equals(entity.getStringField("entityType"))) {
            if (entity.getField("referenceTechnology") == null) {
                entity.addError(dataDefinition.getField("referenceTechnology"), "core.validate.field.error.missing");
                isValid = false;
            }
            if (entity.getField("referenceMode") == null) {
                entity.setField("referenceMode", "01reference");
            }
        } else {
            throw new IllegalStateException("unknown entityType");
        }
        return isValid;
    }

    // public void setTechnologyReferenceOperationComponentViewRequiredFields(final ViewDefinitionState state, final Locale
    // locale) {
    // ((FieldComponentState) state.getComponentByReference("operation")).setRequired(true);
    // }
    //
    // public void setTechnologyReferenceTechnologyComponentViewRequiredFields(final ViewDefinitionState state, final Locale
    // locale) {
    // ((FieldComponentState) state.getComponentByReference("technology")).setRequired(true);
    // ((FieldComponentState) state.getComponentByReference("referenceMode")).setRequired(true);
    // }

    public boolean checkIfUnitSampligNrIsReq(final DataDefinition dataDefinition, final Entity entity) {
        String qualityControlType = (String) entity.getField("qualityControlType");

        if (qualityControlType != null && qualityControlType.equals("02forUnit")) {

            BigDecimal unitSamplingNr = (BigDecimal) entity.getField("unitSamplingNr");
            if (unitSamplingNr == null || unitSamplingNr.scale() > 3 || unitSamplingNr.compareTo(BigDecimal.ZERO) < 0
                    || unitSamplingNr.precision() > 7) {
                entity.addGlobalError("core.validate.global.error.custom");
                entity.addError(dataDefinition.getField("unitSamplingNr"),
                        "products.technology.validate.global.error.unitSamplingNr");
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

}
