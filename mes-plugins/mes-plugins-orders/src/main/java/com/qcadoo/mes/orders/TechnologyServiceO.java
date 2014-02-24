package com.qcadoo.mes.orders;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.productionLines.ProductionLinesService;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.productionLines.constants.TechOperCompWorkstationFields;
import com.qcadoo.mes.productionLines.constants.TechnologyOperationComponentFieldsPL;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class TechnologyServiceO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private ProductionLinesService productionLinesService;

    @Transactional
    public void createOrUpdateTechnology(final DataDefinition orderDD, final Entity order) {
        String orderType = order.getStringField(OrderFields.ORDER_TYPE);
        Entity technologyPrototype = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);

        if (OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(orderType)) {
            if (technologyPrototype != null) {
                createOrUpdateTechnologyForWithPatternTechnology(order, technologyPrototype);
            }
        } else if (OrderType.WITH_OWN_TECHNOLOGY.getStringValue().equals(orderType)) {
            createOrUpdateForOwnTechnology(order, technologyPrototype);
        }
    }

    private void createOrUpdateTechnologyForWithPatternTechnology(final Entity order, final Entity technologyPrototype) {
        Entity existingOrder = getExistingOrder(order);

        if (isTechnologyCopied(order)) {
            if (isOrderTypeChnagedToWithPatternTechnology(order)) {
                Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

                deleteTechnology(technology);

                order.setField(OrderFields.TECHNOLOGY, copyTechnology(technologyPrototype));
            } else if (technologyWasChanged(order)) {
                Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

                deleteTechnology(technology);

                order.setField(OrderFields.TECHNOLOGY, copyTechnology(technologyPrototype));
            }
        } else {
            if (existingOrder == null) {
                order.setField(OrderFields.TECHNOLOGY, copyTechnology(technologyPrototype));
            } else if (!isTechnologySet(order)) {
                order.setField(OrderFields.TECHNOLOGY, copyTechnology(technologyPrototype));
            }
        }
    }

    private void createOrUpdateForOwnTechnology(final Entity order, final Entity technologyPrototype) {
        Entity existingOrder = getExistingOrder(order);

        if (isTechnologyCopied(order)) {
            if (technologyPrototype != null) {
                Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

                updateTechnology(technology);

                order.setField(OrderFields.TECHNOLOGY_PROTOTYPE, null);
            }
        } else {
            if (existingOrder == null) {
                order.setField(OrderFields.TECHNOLOGY, createTechnology(technologyPrototype));

                if (technologyPrototype != null) {
                    order.setField(OrderFields.TECHNOLOGY_PROTOTYPE, null);
                }
            } else if (existingOrder.getBelongsToField(OrderFields.TECHNOLOGY) == null) {
                order.setField(OrderFields.TECHNOLOGY, createTechnology(technologyPrototype));

                if (technologyPrototype != null) {
                    order.setField(OrderFields.TECHNOLOGY_PROTOTYPE, null);
                }
            }
        }
    }

    private Entity getExistingOrder(final Entity order) {
        if (order.getId() == null) {
            return null;
        }

        return order.getDataDefinition().get(order.getId());
    }

    private boolean isTechnologyCopied(final Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (technology == null) {
            return false;
        }

        return true;
    }

    private boolean isTechnologySet(final Entity order) {
        Entity existingOrder = getExistingOrder(order);

        if (existingOrder == null) {
            return false;
        }

        Entity technology = existingOrder.getBelongsToField(OrderFields.TECHNOLOGY);

        if (technology == null) {
            return false;
        }

        return true;
    }

    private boolean isOrderTypeChnagedToWithPatternTechnology(final Entity order) {
        Entity existingOrder = getExistingOrder(order);

        if (existingOrder == null) {
            return false;
        }

        String orderType = existingOrder.getStringField(OrderFields.ORDER_TYPE);

        if (OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(orderType)) {
            return false;
        }

        return true;
    }

    private boolean technologyWasChanged(final Entity order) {
        Entity existingOrder = getExistingOrder(order);

        if (existingOrder == null) {
            return false;
        }

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);
        Entity existingOrderTechnology = existingOrder.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);

        if (existingOrderTechnology == null) {
            return true;
        }

        if (!existingOrderTechnology.equals(technology)) {
            if (order.getBelongsToField(OrderFields.TECHNOLOGY) != null
                    && existingOrder.getBelongsToField(OrderFields.TECHNOLOGY) == null) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    private Entity createTechnology(final Entity technologyPrototype) {
        Entity newTechnology = getTechnologyDD().create();

        String number = newTechnology.getStringField(TechnologyFields.NUMBER);
        Entity product = technologyPrototype.getBelongsToField(TechnologyFields.PRODUCT);

        newTechnology.setField(TechnologyFields.NUMBER, numberGeneratorService.generateNumber(
                TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY));
        newTechnology.setField(TechnologyFields.NAME, makeTechnologyName(number, product));
        newTechnology.setField(TechnologyFields.PRODUCT, product);
        newTechnology.setField(TechnologyFields.TECHNOLOGY_PROTOTYPE, technologyPrototype);
        newTechnology.setField(TechnologyFields.TECHNOLOGY_TYPE, TechnologyType.WITH_OWN_TECHNOLOGY.getStringValue());

        newTechnology = newTechnology.getDataDefinition().save(newTechnology);

        return newTechnology;
    }

    private Entity copyTechnology(final Entity technologyPrototype) {
        Entity copyOfTechnology = getTechnologyDD().create();

        copyOfTechnology = copyOfTechnology.getDataDefinition().copy(technologyPrototype.getId()).get(0);

        copyOfTechnology.setField(TechnologyFields.NUMBER, numberGeneratorService.generateNumber(
                TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY));
        copyOfTechnology.setField(TechnologyFields.TECHNOLOGY_PROTOTYPE, technologyPrototype);
        copyOfTechnology.setField(TechnologyFields.TECHNOLOGY_TYPE, TechnologyType.WITH_PATTERN_TECHNOLOGY.getStringValue());

        copyOfTechnology = copyOfTechnology.getDataDefinition().save(copyOfTechnology);

        return copyOfTechnology;
    }

    private void updateTechnology(final Entity technology) {
        String number = technology.getStringField(TechnologyFields.NUMBER);
        Entity product = technology.getBelongsToField(TechnologyFields.PRODUCT);

        technology.setField(TechnologyFields.NAME, makeTechnologyName(number, product));
        technology.setField(TechnologyFields.TECHNOLOGY_PROTOTYPE, null);
        technology.setField(TechnologyFields.TECHNOLOGY_TYPE, TechnologyType.WITH_OWN_TECHNOLOGY.getStringValue());

        EntityTree operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        if ((operationComponents != null) && !operationComponents.isEmpty()) {
            EntityTreeNode root = operationComponents.getRoot();

            root.getDataDefinition().delete(root.getId());
        }

        technology.setField(TechnologyFields.OPERATION_COMPONENTS, Lists.newArrayList());

        technology.getDataDefinition().save(technology);
    }

    private void deleteTechnology(final Entity technology) {
        technology.getDataDefinition().delete(technology.getId());
    }

    public DataDefinition getTechnologyDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }

    public String makeTechnologyName(final String technologyNumber, final Entity product) {
        return technologyNumber + " - " + product.getStringField(ProductFields.NUMBER);
    }

    public void setQuantityOfWorkstationTypes(final Entity order, final Entity technology) {
        if ((technology == null) || (technology.getId() == null)) {
            return;
        }

        Entity technologyFromDB = technology.getDataDefinition().get(technology.getId());

        if (technologyFromDB == null) {
            return;
        }

        List<Entity> technologyOperationComponents = technologyFromDB.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);

        if ((technologyOperationComponents == null) || technologyOperationComponents.isEmpty()) {
            return;
        }

        DataDefinition quantityOfWorkstationTypesDD = dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_TECH_OPER_COMP_WORKSTATION);

        Entity techOperCompWorkstation = quantityOfWorkstationTypesDD.create();

        for (Entity technologyOperationComponent : technologyOperationComponents) {
            techOperCompWorkstation = quantityOfWorkstationTypesDD.create();

            techOperCompWorkstation.setField(
                    TechOperCompWorkstationFields.QUANTITY_OF_WORKSTATION_TYPES,
                    productionLinesService.getWorkstationTypesCount(technologyOperationComponent,
                            order.getBelongsToField(OrderFields.PRODUCTION_LINE)));

            techOperCompWorkstation = quantityOfWorkstationTypesDD.save(techOperCompWorkstation);

            technologyOperationComponent.setField(TechnologyOperationComponentFieldsPL.TECH_OPER_COMP_WORKSTATION,
                    techOperCompWorkstation);

            technologyOperationComponent.getDataDefinition().save(technologyOperationComponent);
        }
    }

    public void setQuantityOfWorkstationTypes(final Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if ((technology == null) || (technology.getId() == null)) {
            return;
        }

        String sql = "select toc from #technologies_technologyOperationComponent as toc where technology.id = "
                + technology.getId();

        List<Entity> technologyOperationComponents = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)
                .find(sql).list().getEntities();

        DataDefinition quantityOfWorkstationTypesDD = dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_TECH_OPER_COMP_WORKSTATION);

        Entity techOperCompWorkstation = quantityOfWorkstationTypesDD.create();

        for (Entity technologyOperationComponent : technologyOperationComponents) {
            if (technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFieldsPL.TECH_OPER_COMP_WORKSTATION) == null) {
                techOperCompWorkstation = quantityOfWorkstationTypesDD.create();

                techOperCompWorkstation.setField(
                        TechOperCompWorkstationFields.QUANTITY_OF_WORKSTATION_TYPES,
                        productionLinesService.getWorkstationTypesCount(technologyOperationComponent,
                                order.getBelongsToField(OrderFields.PRODUCTION_LINE)));

                techOperCompWorkstation = quantityOfWorkstationTypesDD.save(techOperCompWorkstation);

                technologyOperationComponent.setField(TechnologyOperationComponentFieldsPL.TECH_OPER_COMP_WORKSTATION,
                        techOperCompWorkstation);

                technologyOperationComponent.getDataDefinition().save(technologyOperationComponent);
            }
        }
    }

    public Entity getDefaultTechnology(final Entity product) {
        SearchResult searchResult = getTechnologyDD().find().setMaxResults(1)
                .add(SearchRestrictions.eq(TechnologyFields.MASTER, true)).add(SearchRestrictions.eq("active", true))
                .add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, product)).list();

        if (searchResult.getTotalNumberOfEntities() == 1) {
            return searchResult.getEntities().get(0);
        } else {
            return null;
        }
    }

}
