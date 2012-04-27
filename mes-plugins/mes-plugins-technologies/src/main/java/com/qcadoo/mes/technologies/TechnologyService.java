/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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
package com.qcadoo.mes.technologies;

import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.REFERENCE_TECHNOLOGY;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.STATE;
import static com.qcadoo.mes.technologies.constants.TechnologyState.ACCEPTED;
import static com.qcadoo.mes.technologies.constants.TechnologyState.CHECKED;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.mes.technologies.states.TechnologyStateUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.utils.TreeNumberingService;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.TreeComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class TechnologyService {

    private static final String L_FORM = "form";

    private static final String L_QUANTITY = "quantity";

    private static final String L_UNIT_SAMPLING_NR = "unitSamplingNr";

    private static final String L_QUALITY_CONTROL_TYPE = "qualityControlType";

    private static final String L_ENTITY_TYPE = "entityType";

    private static final String L_OPERATION_COMPONENTS = "operationComponents";

    private static final String L_REFERENCE_TECHNOLOGY = "referenceTechnology";

    private static final String L_PRODUCT = "product";

    private static final String L_NAME = "name";

    private static final String L_PARENT = "parent";

    private static final String L_NUMBER = "number";

    public static final String L_04_WASTE = "04waste";

    public static final String L_03_FINAL_PRODUCT = "03finalProduct";

    public static final String L_01_COMPONENT = "01component";

    public static final String L_02_INTERMEDIATE = "02intermediate";

    public static final String L_00_UNRELATED = "00unrelated";

    private static final String L_TECHNOLOGY = "technology";

    private static final String L_MASTER = "master";

    private static final String L_OPERATION = "operation";

    private static final String L_OPERATION_COMPONENT = "operationComponent";

    private static final String L_OPERATION_PRODUCT_IN_COMPONENTS = "operationProductInComponents";

    private static final String L_OPERATION_PRODUCT_OUT_COMPONENTS = "operationProductOutComponents";

    private static final String L_REFERENCE_MODE = "referenceMode";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private TreeNumberingService treeNumberingService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private PluginAccessor pluginAccessor;

    public boolean isTechnologyUsedInActiveOrder(final Entity technology) {
        if (!ordersPluginIsEnabled()) {
            return false;
        }
        SearchCriteriaBuilder searchCriteria = getOrderDataDefinition().find();
        searchCriteria.add(SearchRestrictions.belongsTo("technology", technology));
        searchCriteria.add(SearchRestrictions.in("state",
                Lists.newArrayList("01pending", "02accepted", "03inProgress", "06interrupted")));
        searchCriteria.setMaxResults(1);
        return searchCriteria.uniqueResult() != null;
    }

    private boolean ordersPluginIsEnabled() {
        return pluginAccessor.getPlugin("orders") != null;
    }

    private DataDefinition getOrderDataDefinition() {
        return dataDefinitionService.get("orders", "order");
    }

    private enum ProductDirection {
        IN, OUT;
    }

    public boolean clearMasterOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(L_MASTER, false);
        return true;
    }

    public void setFirstTechnologyAsDefault(final DataDefinition dataDefinition, final Entity entity) {
        if ((Boolean) entity.getField(L_MASTER)) {
            return;
        }
        SearchCriteriaBuilder searchCriteria = dataDefinition.find();
        searchCriteria.add(SearchRestrictions.belongsTo(L_PRODUCT, entity.getBelongsToField(L_PRODUCT)));
        entity.setField(L_MASTER, searchCriteria.list().getTotalNumberOfEntities() == 0);
    }

    public boolean checkTechnologyDefault(final DataDefinition dataDefinition, final Entity entity) {
        if (!((Boolean) entity.getField(L_MASTER))) {
            return true;
        }

        SearchCriteriaBuilder searchCriteries = dataDefinition.find();
        searchCriteries.add(SearchRestrictions.eq(L_MASTER, true));
        searchCriteries.add(SearchRestrictions.belongsTo(L_PRODUCT, entity.getBelongsToField(L_PRODUCT)));

        if (entity.getId() != null) {
            searchCriteries.add(SearchRestrictions.idNe(entity.getId()));
        }

        if (searchCriteries.list().getTotalNumberOfEntities() == 0) {
            return true;
        }
        entity.addError(dataDefinition.getField(L_MASTER), "orders.validate.global.error.default");
        return false;
    }

    public void loadProductsForReferencedTechnology(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof TreeComponent)) {
            return;
        }

        TreeComponent tree = (TreeComponent) state;

        if (tree.getSelectedEntityId() == null) {
            return;
        }

        Entity operationComponent = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(tree.getSelectedEntityId());

        GridComponent outProductsGrid = (GridComponent) viewDefinitionState.getComponentByReference("outProducts");
        GridComponent inProductsGrid = (GridComponent) viewDefinitionState.getComponentByReference("inProducts");

        if (!REFERENCE_TECHNOLOGY.equals(operationComponent.getStringField(L_ENTITY_TYPE))) {
            // inProductsGrid.setEnabled(true);
            inProductsGrid.setEditable(true);
            // outProductsGrid.setEnabled(true);
            outProductsGrid.setEditable(true);
            return;
        }

        Entity technology = operationComponent.getBelongsToField(REFERENCE_TECHNOLOGY);
        EntityTree operations = technology.getTreeField(L_OPERATION_COMPONENTS);
        Entity rootOperation = operations.getRoot();

        if (rootOperation != null) {
            outProductsGrid.setEntities(rootOperation.getHasManyField(L_OPERATION_PRODUCT_OUT_COMPONENTS));
        }

        List<Entity> inProducts = new ArrayList<Entity>();

        Map<Entity, BigDecimal> productQuantities = productQuantitiesService.getNeededProductQuantities(technology,
                BigDecimal.ONE, false);

        for (Entry<Entity, BigDecimal> productQuantity : productQuantities.entrySet()) {
            Entity inProduct = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).create();

            inProduct.setField(L_OPERATION_COMPONENT, rootOperation);
            inProduct.setField(L_PRODUCT, productQuantity.getKey());
            inProduct.setField(L_QUANTITY, productQuantity.getValue());
            inProducts.add(inProduct);
        }

        inProductsGrid.setEntities(inProducts);
        inProductsGrid.setEnabled(false);
        inProductsGrid.setEditable(false);
        outProductsGrid.setEnabled(false);
        outProductsGrid.setEditable(false);
    }

    public void checkQualityControlType(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            throw new IllegalStateException("component is not select");
        }

        FieldComponent qualityControlType = (FieldComponent) state;

        FieldComponent unitSamplingNr = (FieldComponent) viewDefinitionState.getComponentByReference(L_UNIT_SAMPLING_NR);

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

    public void generateTechnologyGroupNumber(final ViewDefinitionState viewDefinitionState) {
        numberGeneratorService.generateAndInsertNumber(viewDefinitionState, TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_GROUP, L_FORM, L_NUMBER);
    }

    public void generateTechnologyNumber(final ViewDefinitionState state, final ComponentState componentState, final String[] args) {
        if (!(componentState instanceof FieldComponent)) {
            throw new IllegalStateException("component is not FieldComponentState");
        }
        FieldComponent number = (FieldComponent) state.getComponentByReference(L_NUMBER);
        FieldComponent productState = (FieldComponent) componentState;

        if (!numberGeneratorService.checkIfShouldInsertNumber(state, L_FORM, L_NUMBER) || productState.getFieldValue() == null) {
            return;
        }

        Entity product = getProductById((Long) productState.getFieldValue());

        if (product == null) {
            return;
        }

        String numberValue = product.getField(L_NUMBER)
                + "-"
                + numberGeneratorService.generateNumber(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_TECHNOLOGY, 3);
        number.setFieldValue(numberValue);
    }

    public void generateTechnologyName(final ViewDefinitionState state, final ComponentState componentState, final String[] args) {
        if (!(componentState instanceof FieldComponent)) {
            throw new IllegalStateException("component is not FieldComponentState");
        }
        FieldComponent name = (FieldComponent) state.getComponentByReference(L_NAME);
        FieldComponent productState = (FieldComponent) componentState;

        if (StringUtils.hasText((String) name.getFieldValue()) || productState.getFieldValue() == null) {
            return;
        }

        Entity product = getProductById((Long) productState.getFieldValue());

        if (product == null) {
            return;
        }

        Calendar cal = Calendar.getInstance(state.getLocale());
        cal.setTime(new Date());

        name.setFieldValue(translationService.translate("technologies.operation.name.default", state.getLocale(),
                product.getStringField(L_NAME), product.getStringField(L_NUMBER),
                cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1)));
    }

    public void hideReferenceMode(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);
        if (form.getEntityId() != null) {
            ComponentState referenceModeComponent = viewDefinitionState.getComponentByReference(L_REFERENCE_MODE);
            referenceModeComponent.setFieldValue("01reference");
            referenceModeComponent.setVisible(false);
        }
    }

    private Entity getProductById(final Long productId) {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(productId);
    }

    public boolean copyReferencedTechnology(final DataDefinition dataDefinition, final Entity entity) {
        if (!REFERENCE_TECHNOLOGY.equals(entity.getField(L_ENTITY_TYPE)) && entity.getField(REFERENCE_TECHNOLOGY) == null) {
            return true;
        }

        boolean copy = "02copy".equals(entity.getField(L_REFERENCE_MODE));

        Entity technology = entity.getBelongsToField(L_TECHNOLOGY);
        Entity referencedTechnology = entity.getBelongsToField(REFERENCE_TECHNOLOGY);

        Set<Long> technologies = new HashSet<Long>();
        technologies.add(technology.getId());

        boolean cyclic = checkForCyclicReferences(technologies, referencedTechnology, copy);

        if (cyclic) {
            entity.addError(dataDefinition.getField(REFERENCE_TECHNOLOGY),
                    "technologies.technologyReferenceTechnologyComponent.error.cyclicDependency");
            return false;
        }

        if (copy) {
            EntityTreeNode root = referencedTechnology.getTreeField(L_OPERATION_COMPONENTS).getRoot();
            Entity copiedRoot = copyReferencedTechnologyOperations(root, entity.getBelongsToField(L_TECHNOLOGY));

            for (Entry<String, Object> entry : copiedRoot.getFields().entrySet()) {
                if (!(entry.getKey().equals("id") || entry.getKey().equals(L_PARENT))) {
                    entity.setField(entry.getKey(), entry.getValue());
                }
            }

            entity.setField(L_ENTITY_TYPE, L_OPERATION);
            entity.setField(REFERENCE_TECHNOLOGY, null);
        }

        return true;
    }

    private boolean checkForCyclicReferences(final Set<Long> technologies, final Entity referencedTechnology, final boolean copy) {
        if (!copy && technologies.contains(referencedTechnology.getId())) {
            return true;
        }

        technologies.add(referencedTechnology.getId());

        for (Entity operationComponent : referencedTechnology.getTreeField(L_OPERATION_COMPONENTS)) {
            if (REFERENCE_TECHNOLOGY.equals(operationComponent.getField(L_ENTITY_TYPE))) {
                boolean cyclic = checkForCyclicReferences(technologies,
                        operationComponent.getBelongsToField(REFERENCE_TECHNOLOGY), false);

                if (cyclic) {
                    return true;
                }
            }
        }

        return false;
    }

    private Entity copyReferencedTechnologyOperations(final Entity node, final Entity technology) {
        Entity copy = node.copy();

        copy.setId(null);
        copy.setField(L_PARENT, null);
        copy.setField(L_TECHNOLOGY, technology);

        for (Entry<String, Object> entry : node.getFields().entrySet()) {
            Object value = entry.getValue();
            if (value instanceof EntityList) {
                EntityList entities = (EntityList) value;
                List<Entity> copies = new ArrayList<Entity>();
                for (Entity entity : entities) {
                    copies.add(copyReferencedTechnologyOperations(entity, technology));
                }

                copy.setField(entry.getKey(), copies);
            }
        }

        return copy;
    }

    public boolean validateTechnologyOperationComponent(final DataDefinition dataDefinition, final Entity entity) {
        boolean isValid = true;
        if (L_OPERATION.equals(entity.getStringField(L_ENTITY_TYPE))) {
            if (entity.getField(L_OPERATION) == null) {
                entity.addError(dataDefinition.getField(L_OPERATION), "qcadooView.validate.field.error.missing");
                isValid = false;
            }
        } else if (REFERENCE_TECHNOLOGY.equals(entity.getStringField(L_ENTITY_TYPE))) {
            if (entity.getField(REFERENCE_TECHNOLOGY) == null) {
                entity.addError(dataDefinition.getField(REFERENCE_TECHNOLOGY), "qcadooView.validate.field.error.missing");
                isValid = false;
            }
            if (entity.getField(L_REFERENCE_MODE) == null) {
                entity.setField(L_REFERENCE_MODE, "01reference");
            }
        } else {
            throw new IllegalStateException("unknown entityType");
        }
        return isValid;
    }

    public boolean checkIfTechnologyHasAtLeastOneComponent(final DataDefinition dataDefinition, final Entity technology) {
        if (!ACCEPTED.getStringValue().equals(technology.getStringField(STATE))
                && !CHECKED.getStringValue().equals(technology.getStringField(STATE))) {
            return true;
        }
        final Entity savedTechnology = dataDefinition.get(technology.getId());
        final EntityTree operations = savedTechnology.getTreeField(L_OPERATION_COMPONENTS);
        if (operations != null && !operations.isEmpty()) {
            for (Entity operation : operations) {
                if (L_OPERATION.equals(operation.getStringField(L_ENTITY_TYPE))) {
                    return true;
                }
            }
        }
        technology.addGlobalError("technologies.technology.validate.global.error.emptyTechnologyTree");
        return false;
    }

    public boolean checkTopComponentsProducesProductForTechnology(final DataDefinition dataDefinition, final Entity technology) {
        if (!ACCEPTED.getStringValue().equals(technology.getStringField(STATE))
                && !CHECKED.getStringValue().equals(technology.getStringField(STATE))) {
            return true;
        }
        final Entity savedTechnology = dataDefinition.get(technology.getId());
        final Entity product = savedTechnology.getBelongsToField(L_PRODUCT);
        final EntityTree operations = savedTechnology.getTreeField(L_OPERATION_COMPONENTS);
        final EntityTreeNode root = operations.getRoot();
        final EntityList productOutComps = root.getHasManyField(L_OPERATION_PRODUCT_OUT_COMPONENTS);
        for (Entity productOutComp : productOutComps) {
            if (product.getId().equals(productOutComp.getBelongsToField(L_PRODUCT).getId())) {
                return true;
            }
        }
        technology.addGlobalError("technologies.technology.validate.global.error.noFinalProductInTechnologyTree");
        return false;
    }

    public boolean checkIfAllReferenceTechnologiesAreAceepted(final DataDefinition dataDefinition, final Entity technology) {
        if (!ACCEPTED.getStringValue().equals(technology.getStringField(STATE))
                && !CHECKED.getStringValue().equals(technology.getStringField(STATE))) {
            return true;
        }
        final Entity savedTechnology = dataDefinition.get(technology.getId());
        final EntityTree operations = savedTechnology.getTreeField(L_OPERATION_COMPONENTS);
        for (Entity operation : operations) {
            if (L_OPERATION.equals(operation.getStringField(L_ENTITY_TYPE))) {
                continue;
            }
            final Entity referenceTechnology = operation.getBelongsToField(L_REFERENCE_TECHNOLOGY);
            if (referenceTechnology != null
                    && !TechnologyState.ACCEPTED.getStringValue().equals(referenceTechnology.getStringField(STATE))) {
                technology.addError(dataDefinition.getField(L_OPERATION_COMPONENTS),
                        "technologies.technology.validate.global.error.unacceptedReferenceTechnology");
                return false;
            }
        }
        return true;
    }

    public boolean checkIfOperationsUsesSubOperationsProds(final DataDefinition dataDefinition, final Entity technology) {
        if (!ACCEPTED.getStringValue().equals(technology.getStringField(STATE))
                && !CHECKED.getStringValue().equals(technology.getStringField(STATE))) {
            return true;
        }
        final Entity savedTechnology = dataDefinition.get(technology.getId());
        final EntityTree technologyOperations = savedTechnology.getTreeField(L_OPERATION_COMPONENTS);
        if (!checkIfConsumesSubOpsProds(technologyOperations)) {
            technology.addError(dataDefinition.getField(L_OPERATION_COMPONENTS),
                    "technologies.technology.validate.global.error.operationDontConsumeSubOperationsProducts");
            return false;
        }
        return true;
    }

    private boolean checkIfAtLeastOneCommonElement(final List<Entity> prodsIn, final List<Entity> prodsOut) {
        for (Entity prodOut : prodsOut) {
            for (Entity prodIn : prodsIn) {
                if (prodIn.getBelongsToField(L_PRODUCT).getId().equals(prodOut.getBelongsToField(L_PRODUCT).getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkIfConsumesSubOpsProds(final EntityTree technologyOperations) {
        for (Entity technologyOperation : technologyOperations) {
            final Entity parent = technologyOperation.getBelongsToField(L_PARENT);
            if (parent == null || L_REFERENCE_TECHNOLOGY.equals(parent.getStringField(L_ENTITY_TYPE))) {
                continue;
            }
            final EntityList prodsOut = parent.getHasManyField(L_OPERATION_PRODUCT_IN_COMPONENTS);
            if (L_OPERATION.equals(technologyOperation.getStringField(L_ENTITY_TYPE))) {
                final EntityList prodsIn = technologyOperation.getHasManyField(L_OPERATION_PRODUCT_OUT_COMPONENTS);
                if (prodsIn == null || prodsOut == null) {
                    return false;
                }
                if (!checkIfAtLeastOneCommonElement(prodsIn, prodsOut)) {
                    return false;
                }
            } else {
                final Entity prodIn = technologyOperation.getBelongsToField(L_REFERENCE_TECHNOLOGY);

                if (prodIn == null || prodsOut == null) {
                    return false;
                }
                if (!checkIfAtLeastOneCommonElement(Arrays.asList(prodIn), prodsOut)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkIfUnitSampligNrIsReq(final DataDefinition dataDefinition, final Entity entity) {
        String qualityControlType = (String) entity.getField(L_QUALITY_CONTROL_TYPE);
        if (qualityControlType != null && qualityControlType.equals("02forUnit")) {
            BigDecimal unitSamplingNr = (BigDecimal) entity.getField(L_UNIT_SAMPLING_NR);
            if (unitSamplingNr == null || unitSamplingNr.scale() > 3 || unitSamplingNr.compareTo(BigDecimal.ZERO) < 0
                    || unitSamplingNr.precision() > 7) {
                entity.addGlobalError("qcadooView.validate.global.error.custom");
                entity.addError(dataDefinition.getField(L_UNIT_SAMPLING_NR),
                        "technologies.technology.validate.global.error.unitSamplingNr");
                return false;
            }
        }
        return true;
    }

    public void setLookupDisableInTechnologyOperationComponent(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);
        FieldComponent operationLookup = (FieldComponent) viewDefinitionState.getComponentByReference(L_OPERATION);

        operationLookup.setEnabled(form.getEntityId() == null);
    }

    public final void performTreeNumbering(final DataDefinition dd, final Entity technology) {
        DataDefinition technologyOperationDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                MODEL_TECHNOLOGY_OPERATION_COMPONENT);
        treeNumberingService.generateNumbersAndUpdateTree(technologyOperationDD, L_TECHNOLOGY, technology.getId());

    }

    public void setParentIfRootNodeAlreadyExists(final DataDefinition dd, final Entity technologyOperation) {
        Entity technology = technologyOperation.getBelongsToField(L_TECHNOLOGY);
        EntityTreeNode rootNode = technology.getTreeField(L_OPERATION_COMPONENTS).getRoot();
        if (rootNode == null || technologyOperation.getBelongsToField(L_PARENT) != null) {
            return;
        }
        technologyOperation.setField(L_PARENT, rootNode);
    }

    public void toggleDetailsViewEnabled(final ViewDefinitionState view) {
        view.getComponentByReference(STATE).performEvent(view, "toggleEnabled");
    }

    public boolean invalidateIfBelongsToAcceptedTechnology(final DataDefinition dataDefinition, final Entity entity) {
        Entity technology = null;
        String errorMessageKey = "technologies.technology.state.error.modifyBelongsToAcceptedTechnology";
        if (L_TECHNOLOGY.equals(dataDefinition.getName())) {
            technology = entity;
            errorMessageKey = "technologies.technology.state.error.modifyAcceptedTechnology";
        } else if ("technologyOperationComponent".equals(dataDefinition.getName())) {
            technology = entity.getBelongsToField(L_TECHNOLOGY);
        } else if ("operationProductOutComponent".equals(dataDefinition.getName())
                || "operationProductInComponent".equals(dataDefinition.getName())) {
            technology = entity.getBelongsToField(L_OPERATION_COMPONENT).getBelongsToField(L_TECHNOLOGY);
        }

        if (technology == null || technology.getId() == null) {
            return true;
        }

        Entity existingTechnology = technology.getDataDefinition().get(technology.getId());
        if (checkIfDeactivated(dataDefinition, technology, existingTechnology)) {
            return true;
        }
        if (isTechnologyIsAlreadyAccepted(technology, existingTechnology)) {
            entity.addGlobalError(errorMessageKey, technology.getStringField(L_NAME));
            return false;
        }

        return true;
    }

    private boolean checkIfDeactivated(final DataDefinition dataDefinition, final Entity technology,
            final Entity existingTechnology) {
        if (isTechnologyIsAlreadyAccepted(technology, existingTechnology) && L_TECHNOLOGY.equals(dataDefinition.getName())
                && technology.isActive() != existingTechnology.isActive()) {
            return true;
        }
        return false;
    }

    private boolean isTechnologyIsAlreadyAccepted(final Entity technology, final Entity existingTechnology) {
        if (technology == null || existingTechnology == null) {
            return false;
        }
        TechnologyState technologyState = TechnologyStateUtils.getStateFromField(technology.getStringField(STATE));
        TechnologyState existingTechnologyState = TechnologyStateUtils
                .getStateFromField(existingTechnology.getStringField(STATE));

        return TechnologyState.ACCEPTED.equals(technologyState) && technologyState.equals(existingTechnologyState);
    }

    private boolean productComponentsContainProduct(final List<Entity> components, final Entity product) {
        boolean contains = false;

        for (Entity entity : components) {
            if (entity.getBelongsToField(L_PRODUCT).getId().equals(product.getId())) {
                contains = true;
                break;
            }
        }

        return contains;
    }

    private SearchCriteriaBuilder createSearchCriteria(final Entity product, final Entity technology,
            final ProductDirection direction) {
        String model = direction.equals(ProductDirection.IN) ? TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT
                : TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT;

        DataDefinition dd = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, model);

        SearchCriteriaBuilder search = dd.find();
        search.add(SearchRestrictions.eq("product.id", product.getId()));
        search.createAlias(L_OPERATION_COMPONENT, L_OPERATION_COMPONENT);
        search.add(SearchRestrictions.belongsTo("operationComponent.technology", technology));

        return search;
    }

    public String getProductType(final Entity product, final Entity technology) {
        SearchCriteriaBuilder searchIns = createSearchCriteria(product, technology, ProductDirection.IN);
        SearchCriteriaBuilder searchOuts = createSearchCriteria(product, technology, ProductDirection.OUT);
        SearchCriteriaBuilder searchOutsForRoots = createSearchCriteria(product, technology, ProductDirection.OUT);
        searchOutsForRoots.add(SearchRestrictions.isNull("operationComponent.parent"));

        boolean goesIn = productComponentsContainProduct(searchIns.list().getEntities(), product);
        boolean goesOut = productComponentsContainProduct(searchOuts.list().getEntities(), product);
        boolean goesOutInAroot = productComponentsContainProduct(searchOutsForRoots.list().getEntities(), product);

        if (goesOutInAroot) {
            if (technology.getBelongsToField(L_PRODUCT).getId().equals(product.getId())) {
                return L_03_FINAL_PRODUCT;
            } else {
                return L_04_WASTE;
            }
        }

        if (goesIn && !goesOut) {
            return L_01_COMPONENT;
        }

        if (goesIn && goesOut) {
            return L_02_INTERMEDIATE;
        }

        if (!goesIn && goesOut) {
            return L_04_WASTE;
        }

        return L_00_UNRELATED;
    }

    public void switchStateToDraftOnCopy(final DataDefinition technologyDataDefinition, final Entity technology) {
        technology.setField(STATE, TechnologyState.DRAFT.getStringValue());
    }

    public void addOperationsFromSubtechnologiesToList(final EntityTree entityTree, final List<Entity> operationComponents) {
        for (Entity operationComponent : entityTree) {
            if (L_OPERATION.equals(operationComponent.getField(L_ENTITY_TYPE))) {
                operationComponents.add(operationComponent);
            } else {
                addOperationsFromSubtechnologiesToList(
                        operationComponent.getBelongsToField(L_REFERENCE_TECHNOLOGY).getTreeField(L_OPERATION_COMPONENTS),
                        operationComponents);
            }
        }
    }

    public boolean invalidateIfAllreadyInTheSameOperation(final DataDefinition dataDefinition, final Entity operationProduct) {
        if (operationProduct.getId() == null) {
            Entity product = operationProduct.getBelongsToField(L_PRODUCT);
            Entity operationComponent = operationProduct.getBelongsToField(L_OPERATION_COMPONENT);

            String fieldName;

            if ("operationProductInComponent".equals(dataDefinition.getName())) {
                fieldName = L_OPERATION_PRODUCT_IN_COMPONENTS;
            } else {
                fieldName = L_OPERATION_PRODUCT_OUT_COMPONENTS;
            }

            EntityList products = operationComponent.getHasManyField(fieldName);

            if (product == null || product.getId() == null) {
                throw new IllegalStateException("Cant get product id");
            }

            if (products != null && listContainsProduct(products, product)) {
                operationProduct.addError(dataDefinition.getField(L_PRODUCT),
                        "technologyOperationComponent.validate.error.productAlreadyExistInTechnologyOperation");
                return false;
            }
        }
        return true;
    }

    private boolean listContainsProduct(final EntityList list, final Entity product) {
        for (Entity prod : list) {
            if (prod.getBelongsToField(L_PRODUCT).getId().equals(product.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param operationComponent
     * @return productOutComponent. Assuming operation can have only one product/intermediate.
     */
    public Entity getMainOutputProductComponent(Entity operationComponent) {
        if (L_REFERENCE_TECHNOLOGY.equals(operationComponent.getStringField(L_ENTITY_TYPE))) {
            operationComponent = operationComponent.getBelongsToField(L_REFERENCE_TECHNOLOGY)
                    .getTreeField(L_OPERATION_COMPONENTS).getRoot();
        }

        if (operationComponent.getDataDefinition().getName().equals("technologyInstanceOperationComponent")) {
            operationComponent = operationComponent.getBelongsToField("technologyOperationComponent");
        }

        Entity parentOpComp = operationComponent.getBelongsToField(L_PARENT);

        List<Entity> prodOutComps = operationComponent.getHasManyField(L_OPERATION_PRODUCT_OUT_COMPONENTS);

        if (parentOpComp == null) {
            Entity technology = operationComponent.getBelongsToField(L_TECHNOLOGY);
            Entity product = technology.getBelongsToField(L_PRODUCT);

            for (Entity prodOutComp : prodOutComps) {
                if (prodOutComp.getBelongsToField(L_PRODUCT).getId().equals(product.getId())) {
                    return prodOutComp;
                }
            }
        } else {
            List<Entity> prodInComps = parentOpComp.getHasManyField(L_OPERATION_PRODUCT_IN_COMPONENTS);

            for (Entity prodOutComp : prodOutComps) {
                for (Entity prodInComp : prodInComps) {
                    if (prodOutComp.getBelongsToField(L_PRODUCT).getId().equals(prodInComp.getBelongsToField(L_PRODUCT).getId())) {
                        return prodOutComp;
                    }
                }
            }
        }

        throw new IllegalStateException("OperationComponent doesn't have any products nor intermediates, id = "
                + operationComponent.getId());
    }

    /**
     * 
     * @param operationComponent
     * @return Quantity of the output product associated with this operationComponent. Assuming operation can have only one
     *         product/intermediate.
     */
    public BigDecimal getProductCountForOperationComponent(final Entity operationComponent) {
        return getMainOutputProductComponent(operationComponent).getDecimalField(L_QUANTITY);
    }

}
