/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.TreeComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class TechnologyService {

    private static final String QUANTITY_FIELD = "quantity";

    private static final String UNIT_SAMPLING_NR_FIELD = "unitSamplingNr";

    private static final String QUALITY_CONTROL_TYPE_FIELD = "qualityControlType";

    private static final String ENTITY_TYPE_FIELD = "entityType";

    private static final String OPERATION_COMPONENTS_FIELD = "operationComponents";

    private static final String REFERENCE_TECHNOLOGY_FIELD = "referenceTechnology";

    private static final String PRODUCT_L = "product";

    private static final String NAME_FIELD = "name";

    private static final String STATE_FIELD = "state";

    private static final String PARENT_FIELD = "parent";

    private static final String ACCEPTED = "02accepted";

    private static final String NUMBER = "number";

    public static final String WASTE = "04waste";

    public static final String FINAL_PRODUCT = "03finalProduct";

    public static final String COMPONENT = "01component";

    public static final String INTERMEDIATE = "02intermediate";

    public static final String UNRELATED = "00unrelated";

    private static final String CONST_TECHNOLOGY = "technology";

    private static final String CONST_MASTER = "master";

    private static final String CONST_ENTITY_TYPE = ENTITY_TYPE_FIELD;

    private static final String CONST_OPERATION = "operation";

    private static final String CONST_OPERATION_COMPONENTS = OPERATION_COMPONENTS_FIELD;

    private static final String CONST_OPERATION_COMPONENT = "operationComponent";

    private static final String CONST_OPERATION_COMP_PRODUCT_IN = "operationProductInComponents";

    private static final String CONST_OPERATION_COMP_PRODUCT_OUT = "operationProductOutComponents";

    private static final String CONST_REFERENCE_MODE = "referenceMode";

    private static final String CONST_STATE = STATE_FIELD;

    private static final String OPERATION_NODE_ENTITY_TYPE = "operation";

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

    private enum ProductDirection {
        IN, OUT;
    }

    public boolean clearMasterOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(CONST_MASTER, false);
        return true;
    }

    public void setFirstTechnologyAsDefault(final DataDefinition dataDefinition, final Entity entity) {
        if ((Boolean) entity.getField(CONST_MASTER)) {
            return;
        }
        SearchCriteriaBuilder searchCriteria = dataDefinition.find();
        searchCriteria.add(SearchRestrictions.belongsTo(PRODUCT_L, entity.getBelongsToField(PRODUCT_L)));
        entity.setField(CONST_MASTER, searchCriteria.list().getTotalNumberOfEntities() == 0);
    }

    public boolean checkTechnologyDefault(final DataDefinition dataDefinition, final Entity entity) {
        if (!((Boolean) entity.getField(CONST_MASTER))) {
            return true;
        }

        SearchCriteriaBuilder searchCriteries = dataDefinition.find();
        searchCriteries.add(SearchRestrictions.eq(CONST_MASTER, true));
        searchCriteries.add(SearchRestrictions.belongsTo(PRODUCT_L, entity.getBelongsToField(PRODUCT_L)));

        if (entity.getId() != null) {
            searchCriteries.add(SearchRestrictions.idNe(entity.getId()));
        }

        if (searchCriteries.list().getTotalNumberOfEntities() == 0) {
            return true;
        }
        entity.addError(dataDefinition.getField(CONST_MASTER), "orders.validate.global.error.default");
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

        if (!REFERENCE_TECHNOLOGY.equals(operationComponent.getStringField(CONST_ENTITY_TYPE))) {
            // inProductsGrid.setEnabled(true);
            inProductsGrid.setEditable(true);
            // outProductsGrid.setEnabled(true);
            outProductsGrid.setEditable(true);
            return;
        }

        Entity technology = operationComponent.getBelongsToField(REFERENCE_TECHNOLOGY);
        EntityTree operations = technology.getTreeField(CONST_OPERATION_COMPONENTS);
        Entity rootOperation = operations.getRoot();

        if (rootOperation != null) {
            outProductsGrid.setEntities(rootOperation.getHasManyField(CONST_OPERATION_COMP_PRODUCT_OUT));
        }

        List<Entity> inProducts = new ArrayList<Entity>();

        Map<Entity, BigDecimal> productQuantities = productQuantitiesService.getNeededProductQuantities(technology,
                BigDecimal.ONE, false);

        for (Entry<Entity, BigDecimal> productQuantity : productQuantities.entrySet()) {
            Entity inProduct = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).create();

            inProduct.setField(CONST_OPERATION_COMPONENT, rootOperation);
            inProduct.setField(PRODUCT_L, productQuantity.getKey());
            inProduct.setField(QUANTITY_FIELD, productQuantity.getValue());
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

        FieldComponent unitSamplingNr = (FieldComponent) viewDefinitionState.getComponentByReference(UNIT_SAMPLING_NR_FIELD);

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
        if (!(componentState instanceof FieldComponent)) {
            throw new IllegalStateException("component is not FieldComponentState");
        }
        FieldComponent number = (FieldComponent) state.getComponentByReference(NUMBER);
        FieldComponent productState = (FieldComponent) componentState;

        if (!numberGeneratorService.checkIfShouldInsertNumber(state, "form", NUMBER) || productState.getFieldValue() == null) {
            return;
        }

        Entity product = getProductById((Long) productState.getFieldValue());

        if (product == null) {
            return;
        }

        String numberValue = product.getField(NUMBER) + "-"
                + numberGeneratorService.generateNumber("technologies", CONST_TECHNOLOGY, 3);
        number.setFieldValue(numberValue);
    }

    public void generateTechnologyName(final ViewDefinitionState state, final ComponentState componentState, final String[] args) {
        if (!(componentState instanceof FieldComponent)) {
            throw new IllegalStateException("component is not FieldComponentState");
        }
        FieldComponent name = (FieldComponent) state.getComponentByReference(NAME_FIELD);
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
                product.getStringField(NAME_FIELD), product.getStringField(NUMBER),
                cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1)));
    }

    public void hideReferenceMode(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        if (form.getEntityId() != null) {
            ComponentState referenceModeComponent = viewDefinitionState.getComponentByReference(CONST_REFERENCE_MODE);
            referenceModeComponent.setFieldValue("01reference");
            referenceModeComponent.setVisible(false);
        }
    }

    private Entity getProductById(final Long productId) {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(productId);
    }

    public boolean copyReferencedTechnology(final DataDefinition dataDefinition, final Entity entity) {
        if (!REFERENCE_TECHNOLOGY.equals(entity.getField(CONST_ENTITY_TYPE)) && entity.getField(REFERENCE_TECHNOLOGY) == null) {
            return true;
        }

        boolean copy = "02copy".equals(entity.getField(CONST_REFERENCE_MODE));

        Entity technology = entity.getBelongsToField(CONST_TECHNOLOGY);
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
            EntityTreeNode root = referencedTechnology.getTreeField(CONST_OPERATION_COMPONENTS).getRoot();
            Entity copiedRoot = copyReferencedTechnologyOperations(root, entity.getBelongsToField(CONST_TECHNOLOGY));

            for (Entry<String, Object> entry : copiedRoot.getFields().entrySet()) {
                if (!(entry.getKey().equals("id") || entry.getKey().equals("parent"))) {
                    entity.setField(entry.getKey(), entry.getValue());
                }
            }

            entity.setField(CONST_ENTITY_TYPE, CONST_OPERATION);
            entity.setField(REFERENCE_TECHNOLOGY, null);
        }

        return true;
    }

    private boolean checkForCyclicReferences(final Set<Long> technologies, final Entity referencedTechnology, final boolean copy) {
        if (!copy && technologies.contains(referencedTechnology.getId())) {
            return true;
        }

        technologies.add(referencedTechnology.getId());

        for (Entity operationComponent : referencedTechnology.getTreeField(CONST_OPERATION_COMPONENTS)) {
            if (REFERENCE_TECHNOLOGY.equals(operationComponent.getField(CONST_ENTITY_TYPE))) {
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
        copy.setField(PARENT_FIELD, null);
        copy.setField(CONST_TECHNOLOGY, technology);

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
        if (CONST_OPERATION.equals(entity.getStringField(CONST_ENTITY_TYPE))) {
            if (entity.getField(CONST_OPERATION) == null) {
                entity.addError(dataDefinition.getField(CONST_OPERATION), "qcadooView.validate.field.error.missing");
                isValid = false;
            }
        } else if (REFERENCE_TECHNOLOGY.equals(entity.getStringField(CONST_ENTITY_TYPE))) {
            if (entity.getField(REFERENCE_TECHNOLOGY) == null) {
                entity.addError(dataDefinition.getField(REFERENCE_TECHNOLOGY), "qcadooView.validate.field.error.missing");
                isValid = false;
            }
            if (entity.getField(CONST_REFERENCE_MODE) == null) {
                entity.setField(CONST_REFERENCE_MODE, "01reference");
            }
        } else {
            throw new IllegalStateException("unknown entityType");
        }
        return isValid;
    }

    public boolean checkIfTechnologyHasAtLeastOneComponent(final DataDefinition dataDefinition, final Entity technology) {
        if (!ACCEPTED.equals(technology.getStringField(CONST_STATE))) {
            return true;
        }
        final Entity savedTechnology = dataDefinition.get(technology.getId());
        final EntityTree operations = savedTechnology.getTreeField(CONST_OPERATION_COMPONENTS);
        if (operations != null && !operations.isEmpty()) {
            for (Entity operation : operations) {
                if (CONST_OPERATION.equals(operation.getStringField(CONST_ENTITY_TYPE))) {
                    return true;
                }
            }
        }
        technology.addGlobalError("technologies.technology.validate.global.error.emptyTechnologyTree");
        return false;
    }

    public boolean checkTopComponentsProducesProductForTechnology(final DataDefinition dataDefinition, final Entity technology) {
        if (!ACCEPTED.equals(technology.getStringField(CONST_STATE))) {
            return true;
        }
        final Entity savedTechnology = dataDefinition.get(technology.getId());
        final Entity product = savedTechnology.getBelongsToField(PRODUCT_L);
        final EntityTree operations = savedTechnology.getTreeField(CONST_OPERATION_COMPONENTS);
        final EntityTreeNode root = operations.getRoot();
        final EntityList productOutComps = root.getHasManyField(CONST_OPERATION_COMP_PRODUCT_OUT);
        for (Entity productOutComp : productOutComps) {
            if (product.getId().equals(productOutComp.getBelongsToField(PRODUCT_L).getId())) {
                return true;
            }
        }
        technology.addGlobalError("technologies.technology.validate.global.error.noFinalProductInTechnologyTree");
        return false;
    }

    public boolean checkIfAllReferenceTechnologiesAreAceepted(final DataDefinition dataDefinition, final Entity technology) {
        if (!ACCEPTED.equals(technology.getStringField(CONST_STATE))) {
            return true;
        }
        final Entity savedTechnology = dataDefinition.get(technology.getId());
        final EntityTree operations = savedTechnology.getTreeField(CONST_OPERATION_COMPONENTS);
        for (Entity operation : operations) {
            if (CONST_OPERATION.equals(operation.getStringField(CONST_ENTITY_TYPE))) {
                continue;
            }
            final Entity referenceTechnology = operation.getBelongsToField(REFERENCE_TECHNOLOGY_FIELD);
            if (referenceTechnology != null && !"02accepted".equals(referenceTechnology.getStringField(CONST_STATE))) {
                technology.addError(dataDefinition.getField(CONST_OPERATION_COMPONENTS),
                        "technologies.technology.validate.global.error.unacceptedReferenceTechnology");
                return false;
            }
        }
        return true;
    }

    public boolean checkIfOperationsUsesSubOperationsProds(final DataDefinition dataDefinition, final Entity technology) {
        if (!ACCEPTED.equals(technology.getStringField(STATE_FIELD))) {
            return true;
        }
        final Entity savedTechnology = dataDefinition.get(technology.getId());
        final EntityTree technologyOperations = savedTechnology.getTreeField(CONST_OPERATION_COMPONENTS);
        if (!checkIfConsumesSubOpsProds(technologyOperations)) {
            technology.addError(dataDefinition.getField(CONST_OPERATION_COMPONENTS),
                    "technologies.technology.validate.global.error.operationDontConsumeSubOperationsProducts");
            return false;
        }
        return true;
    }

    private boolean checkIfAtLeastOneCommonElement(final List<Entity> prodsIn, final List<Entity> prodsOut) {
        for (Entity prodOut : prodsOut) {
            for (Entity prodIn : prodsIn) {
                if (prodIn.getBelongsToField(PRODUCT_L).getId().equals(prodOut.getBelongsToField(PRODUCT_L).getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkIfConsumesSubOpsProds(final EntityTree technologyOperations) {
        for (Entity technologyOperation : technologyOperations) {
            final Entity parent = technologyOperation.getBelongsToField(PARENT_FIELD);
            if (parent == null || "referenceTechnology".equals(parent.getStringField(CONST_ENTITY_TYPE))) {
                continue;
            }
            final EntityList prodsOut = parent.getHasManyField(CONST_OPERATION_COMP_PRODUCT_IN);
            if (CONST_OPERATION.equals(technologyOperation.getStringField(CONST_ENTITY_TYPE))) {
                final EntityList prodsIn = technologyOperation.getHasManyField(CONST_OPERATION_COMP_PRODUCT_OUT);
                if (prodsIn == null || prodsOut == null) {
                    return false;
                }
                if (!checkIfAtLeastOneCommonElement(prodsIn, prodsOut)) {
                    return false;
                }
            } else {
                final Entity prodIn = technologyOperation.getBelongsToField("referenceTechnology");

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
        String qualityControlType = (String) entity.getField(QUALITY_CONTROL_TYPE_FIELD);
        if (qualityControlType != null && qualityControlType.equals("02forUnit")) {
            BigDecimal unitSamplingNr = (BigDecimal) entity.getField(UNIT_SAMPLING_NR_FIELD);
            if (unitSamplingNr == null || unitSamplingNr.scale() > 3 || unitSamplingNr.compareTo(BigDecimal.ZERO) < 0
                    || unitSamplingNr.precision() > 7) {
                entity.addGlobalError("qcadooView.validate.global.error.custom");
                entity.addError(dataDefinition.getField(UNIT_SAMPLING_NR_FIELD),
                        "technologies.technology.validate.global.error.unitSamplingNr");
                return false;
            }
        }
        return true;
    }

    public void setLookupDisableInTechnologyOperationComponent(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        FieldComponent operationLookup = (FieldComponent) viewDefinitionState.getComponentByReference(CONST_OPERATION);

        operationLookup.setEnabled(form.getEntityId() == null);
    }

    public final void performTreeNumbering(final DataDefinition dd, final Entity technology) {
        DataDefinition technologyOperationDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                MODEL_TECHNOLOGY_OPERATION_COMPONENT);
        treeNumberingService.generateNumbersAndUpdateTree(technologyOperationDD, CONST_TECHNOLOGY, technology.getId());

    }

    public void setParentIfRootNodeAlreadyExists(final DataDefinition dd, final Entity technologyOperation) {
        Entity technology = technologyOperation.getBelongsToField(CONST_TECHNOLOGY);
        EntityTreeNode rootNode = technology.getTreeField(CONST_OPERATION_COMPONENTS).getRoot();
        if (rootNode == null || technologyOperation.getBelongsToField(PARENT_FIELD) != null) {
            return;
        }
        technologyOperation.setField(PARENT_FIELD, rootNode);
    }

    public void toggleDetailsViewEnabled(final ViewDefinitionState view) {
        view.getComponentByReference(STATE_FIELD).performEvent(view, "toggleEnabled");
    }

    public boolean invalidateIfBelongsToAcceptedTechnology(final DataDefinition dataDefinition, final Entity entity) {
        Entity technology = null;
        String errorMessageKey = "technologies.technology.state.error.modifyBelongsToAcceptedTechnology";
        if (CONST_TECHNOLOGY.equals(dataDefinition.getName())) {
            technology = entity;
            errorMessageKey = "technologies.technology.state.error.modifyAcceptedTechnology";
        } else if ("technologyOperationComponent".equals(dataDefinition.getName())) {
            technology = entity.getBelongsToField(CONST_TECHNOLOGY);
        } else if ("operationProductOutComponent".equals(dataDefinition.getName())
                || "operationProductInComponent".equals(dataDefinition.getName())) {
            technology = entity.getBelongsToField(CONST_OPERATION_COMPONENT).getBelongsToField(CONST_TECHNOLOGY);
        }

        if (technology == null || technology.getId() == null) {
            return true;
        }

        Entity existingTechnology = technology.getDataDefinition().get(technology.getId());
        if (checkIfDeactivated(dataDefinition, technology, existingTechnology)) {
            return true;
        }
        if (isTechnologyIsAlreadyAccepted(technology, existingTechnology)) {
            entity.addGlobalError(errorMessageKey, technology.getStringField(NAME_FIELD));
            return false;
        }

        return true;
    }

    private boolean checkIfDeactivated(final DataDefinition dataDefinition, final Entity technology,
            final Entity existingTechnology) {
        if (isTechnologyIsAlreadyAccepted(technology, existingTechnology) && CONST_TECHNOLOGY.equals(dataDefinition.getName())) {
            if (technology.isActive() != existingTechnology.isActive()) {
                return true;
            }
        }
        return false;
    }

    private boolean isTechnologyIsAlreadyAccepted(final Entity technology, final Entity existingTechnology) {
        if (technology == null || existingTechnology == null) {
            return false;
        }
        TechnologyState technologyState = TechnologyStateUtils.getStateFromField(technology.getStringField(CONST_STATE));
        TechnologyState existingTechnologyState = TechnologyStateUtils.getStateFromField(existingTechnology
                .getStringField(CONST_STATE));

        return TechnologyState.ACCEPTED.equals(technologyState) && technologyState.equals(existingTechnologyState);
    }

    private boolean productComponentsContainProduct(final List<Entity> components, final Entity product) {
        boolean contains = false;

        for (Entity entity : components) {
            if (entity.getBelongsToField(PRODUCT_L).getId().equals(product.getId())) {
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
        search.createAlias(CONST_OPERATION_COMPONENT, CONST_OPERATION_COMPONENT);
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
            if (technology.getBelongsToField(PRODUCT_L).getId().equals(product.getId())) {
                return FINAL_PRODUCT;
            } else {
                return WASTE;
            }
        }

        if (goesIn && !goesOut) {
            return COMPONENT;
        }

        if (goesIn && goesOut) {
            return INTERMEDIATE;
        }

        if (!goesIn && goesOut) {
            return WASTE;
        }

        return UNRELATED;
    }

    public void switchStateToDraftOnCopy(final DataDefinition technologyDataDefinition, final Entity technology) {
        technology.setField(CONST_STATE, TechnologyState.DRAFT.getStringValue());
    }

    public void addOperationsFromSubtechnologiesToList(final EntityTree entityTree, final List<Entity> operationComponents) {
        for (Entity operationComponent : entityTree) {
            if (OPERATION_NODE_ENTITY_TYPE.equals(operationComponent.getField(ENTITY_TYPE_FIELD))) {
                operationComponents.add(operationComponent);
            } else {
                addOperationsFromSubtechnologiesToList(operationComponent.getBelongsToField(REFERENCE_TECHNOLOGY_FIELD)
                        .getTreeField(OPERATION_COMPONENTS_FIELD), operationComponents);
            }
        }
    }

    public boolean invalidateIfAllreadyInTheSameOperation(final DataDefinition dataDefinition, final Entity operationProduct) {
        if (operationProduct.getId() == null) {
            Entity product = operationProduct.getBelongsToField(PRODUCT_L);
            Entity operationComponent = operationProduct.getBelongsToField(CONST_OPERATION_COMPONENT);

            String fieldName;

            if ("operationProductInComponent".equals(dataDefinition.getName())) {
                fieldName = CONST_OPERATION_COMP_PRODUCT_IN;
            } else {
                fieldName = CONST_OPERATION_COMP_PRODUCT_OUT;
            }

            EntityList products = operationComponent.getHasManyField(fieldName);

            if (product == null || product.getId() == null) {
                throw new IllegalStateException("Cant get product id");
            }

            if (products != null && listContainsProduct(products, product)) {
                operationProduct.addError(dataDefinition.getField(PRODUCT_L),
                        "technologyOperationComponent.validate.error.productAlreadyExistInTechnologyOperation");
                return false;
            }
        }
        return true;
    }

    private boolean listContainsProduct(final EntityList list, final Entity product) {
        for (Entity prod : list) {
            if (prod.getBelongsToField(PRODUCT_L).getId().equals(product.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param operationComponent
     * @return Quantity of the output product associated with this operationComponent. Assuming operation can have only one
     *         product/intermediate.
     */
    public BigDecimal getProductCountForOperationComponent(Entity operationComponent) {
        Entity parentOpComp = operationComponent.getBelongsToField("parent");

        List<Entity> prodOutComps = operationComponent.getHasManyField("operationProductOutComponents");

        if (parentOpComp == null) {
            Entity technology = operationComponent.getBelongsToField("technology");
            Entity product = technology.getBelongsToField("product");

            for (Entity prodOutComp : prodOutComps) {
                if (prodOutComp.getBelongsToField("product").getId().equals(product.getId())) {
                    return (BigDecimal) prodOutComp.getField("quantity");
                }
            }
        } else {
            List<Entity> prodInComps = parentOpComp.getHasManyField("operationProductInComponents");

            for (Entity prodOutComp : prodOutComps) {
                for (Entity prodInComp : prodInComps) {
                    if (prodOutComp.getBelongsToField("product").getId().equals(prodInComp.getBelongsToField("product").getId())) {
                        return (BigDecimal) prodOutComp.getField("quantity");
                    }
                }
            }
        }

        throw new IllegalStateException("operation doesn't have any products nor intermediates");
    }

}
