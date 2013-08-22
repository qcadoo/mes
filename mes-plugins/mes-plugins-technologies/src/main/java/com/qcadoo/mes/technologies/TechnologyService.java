/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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

import static com.qcadoo.mes.technologies.constants.OperationFields.ATTACHMENT;
import static com.qcadoo.mes.technologies.constants.OperationFields.COMMENT;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.REFERENCE_TECHNOLOGY;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.STATE;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT;
import static com.qcadoo.mes.technologies.states.constants.TechnologyState.ACCEPTED;
import static com.qcadoo.mes.technologies.states.constants.TechnologyState.CHECKED;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.TreeComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class TechnologyService {

    private static final String L_FORM = "form";

    private static final String L_QUANTITY = "quantity";

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

    private static final String L_OPERATION = "operation";

    private static final String L_OPERATION_COMPONENT = "operationComponent";

    private static final String L_OPERATION_PRODUCT_IN_COMPONENTS = "operationProductInComponents";

    private static final String L_OPERATION_PRODUCT_OUT_COMPONENTS = "operationProductOutComponents";

    private static final String IS_SYNCHRONIZED_QUERY = String.format(
            "SELECT t.id as id, t.%s as %s from #%s_%s t where t.id = :technologyId", TechnologyFields.EXTERNAL_SYNCHRONIZED,
            TechnologyFields.EXTERNAL_SYNCHRONIZED, TechnologiesConstants.PLUGIN_IDENTIFIER,
            TechnologiesConstants.MODEL_TECHNOLOGY);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private PluginAccessor pluginAccessor;

    public void copyCommentAndAttachmentFromTechnologyOperationComponent(
            final DataDefinition technologyInstanceOperationComponentDD, final Entity technologyInstanceOperationComponent) {
        copyCommentAndAttachmentFromLowerInstance(technologyInstanceOperationComponent, TECHNOLOGY_OPERATION_COMPONENT);
    }

    public void copyCommentAndAttachmentFromLowerInstance(final Entity operationComponent, final String belongsToName) {
        Entity operation = operationComponent.getBelongsToField(belongsToName);

        if (operation != null) {
            operationComponent.setField(COMMENT, operation.getStringField(COMMENT));
            operationComponent.setField(ATTACHMENT, operation.getStringField(ATTACHMENT));
        }
    }

    public boolean checkIfTechnologyStateIsOtherThanCheckedAndAccepted(final Entity technology) {
        return !ACCEPTED.getStringValue().equals(technology.getStringField(STATE))
                && !CHECKED.getStringValue().equals(technology.getStringField(STATE));
    }

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
            inProductsGrid.setEditable(true);
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
                BigDecimal.ONE, MrpAlgorithm.ALL_PRODUCTS_IN);

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

    public void generateTechnologyGroupNumber(final ViewDefinitionState viewDefinitionState) {
        numberGeneratorService.generateAndInsertNumber(viewDefinitionState, TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_GROUP, L_FORM, L_NUMBER);
    }

    public void generateTechnologyNumber(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            throw new IllegalStateException("component is not FieldComponentState");
        }

        FieldComponent numberField = (FieldComponent) view.getComponentByReference(TechnologyFields.NUMBER);
        LookupComponent productLookup = (LookupComponent) state;

        Entity product = productLookup.getEntity();

        if (product == null) {
            return;
        }

        String number = product.getField(ProductFields.NUMBER)
                + "-"
                + numberGeneratorService.generateNumber(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_TECHNOLOGY, 3);

        numberField.setFieldValue(number);
        numberField.requestComponentUpdateState();
    }

    public void generateTechnologyName(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            throw new IllegalStateException("component is not FieldComponentState");
        }

        FieldComponent nameField = (FieldComponent) view.getComponentByReference(TechnologyFields.NAME);
        LookupComponent productLookup = (LookupComponent) state;

        Entity product = productLookup.getEntity();

        if (product == null) {
            return;
        }

        Calendar calendar = Calendar.getInstance(state.getLocale());
        calendar.setTime(new Date());

        String name = translationService.translate("technologies.operation.name.default", state.getLocale(),
                product.getStringField(ProductFields.NAME), product.getStringField(ProductFields.NUMBER),
                calendar.get(Calendar.YEAR) + "." + (calendar.get(Calendar.MONTH) + 1));

        nameField.setFieldValue(name);
        nameField.requestComponentUpdateState();
    }

    public void setLookupDisableInTechnologyOperationComponent(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);
        FieldComponent operationLookup = (FieldComponent) viewDefinitionState.getComponentByReference(L_OPERATION);

        operationLookup.setEnabled(form.getEntityId() == null);
    }

    public void toggleDetailsViewEnabled(final ViewDefinitionState view) {
        view.getComponentByReference(STATE).performEvent(view, "toggleEnabled");
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

    /**
     * Check if technology with given id is external synchronized.
     * 
     * @param technologyId
     *            identifier of the queried technology
     * @return true if technology is external synchronized
     */
    public boolean isExternalSynchronized(final Long technologyId) {
        SearchQueryBuilder sqb = getTechnologyDataDefinition().find(IS_SYNCHRONIZED_QUERY);
        sqb.setLong("technologyId", technologyId).setMaxResults(1);
        Entity projection = sqb.uniqueResult();
        Preconditions.checkNotNull(projection, String.format("Technology with id = '%s' does not exists.", technologyId));
        return projection.getBooleanField(TechnologyFields.EXTERNAL_SYNCHRONIZED);
    }

    private DataDefinition getTechnologyDataDefinition() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }

}
