package com.qcadoo.mes.orders;

import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static com.qcadoo.mes.orders.constants.OrdersConstants.BASIC_MODEL_PRODUCT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class TechnologyServiceO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public Entity getDefaultTechnology(final Entity product) {
        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);

        SearchResult searchResult = technologyDD.find().setMaxResults(1).add(SearchRestrictions.eq("master", true))
                .add(SearchRestrictions.eq("active", true)).add(SearchRestrictions.belongsTo(BASIC_MODEL_PRODUCT, product))
                .list();

        if (searchResult.getTotalNumberOfEntities() == 1) {
            return searchResult.getEntities().get(0);
        } else {
            return null;
        }
    }

    public void createOrUpdateTechnology(final DataDefinition dataDefinition, final Entity order) {
        if (OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(order.getStringField(OrderFields.ORDER_TYPE))) {

            if (order.getBelongsToField(TECHNOLOGY) == null) {
                return;
            }

            DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY);

            DataDefinition orderDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);

            if (isTechnologyCopied(order)) {
                if (isTypeOrderChnagedToPattern(order)) {
                    Entity copyOfTechnology = order.getBelongsToField(OrderFields.COPY_OF_TECHNOLOGY);
                    Entity orderDB = orderDD.get(order.getId());
                    orderDB.setField(OrderFields.COPY_OF_TECHNOLOGY, null);
                    orderDB.getDataDefinition().save(orderDB);

                    technologyDD.delete(copyOfTechnology.getId());

                    Entity newCopyOfTechnology = technologyDD.create();
                    newCopyOfTechnology = technologyDD.copy(order.getBelongsToField(TECHNOLOGY).getId()).get(0);
                    newCopyOfTechnology.setField(TechnologyFields.NUMBER, numberGeneratorService.generateNumber(
                            TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY));
                    newCopyOfTechnology.setField("patternTechnology", order.getBelongsToField(TECHNOLOGY));
                    newCopyOfTechnology.setField(TechnologyFields.TECHNOLOGY_TYPE,
                            TechnologyType.WITH_PATTERN_TECHNOLOGY.getStringValue());
                    newCopyOfTechnology = copyOfTechnology.getDataDefinition().save(newCopyOfTechnology);
                    order.setField(OrderFields.COPY_OF_TECHNOLOGY, newCopyOfTechnology);

                } else if (technologyWasChanged(order)) {
                    Entity copyOfTechnology = order.getBelongsToField(OrderFields.COPY_OF_TECHNOLOGY);
                    Entity orderDB = orderDD.get(order.getId());
                    orderDB.setField(OrderFields.COPY_OF_TECHNOLOGY, null);
                    orderDB.getDataDefinition().save(orderDB);

                    technologyDD.delete(copyOfTechnology.getId());

                    Entity newCopyOfTechnology = technologyDD.create();
                    newCopyOfTechnology = technologyDD.copy(order.getBelongsToField(TECHNOLOGY).getId()).get(0);
                    newCopyOfTechnology.setField(TechnologyFields.NUMBER, numberGeneratorService.generateNumber(
                            TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY));
                    newCopyOfTechnology.setField("patternTechnology", order.getBelongsToField(TECHNOLOGY));
                    newCopyOfTechnology.setField(TechnologyFields.TECHNOLOGY_TYPE,
                            TechnologyType.WITH_PATTERN_TECHNOLOGY.getStringValue());
                    newCopyOfTechnology = copyOfTechnology.getDataDefinition().save(newCopyOfTechnology);
                    order.setField(OrderFields.COPY_OF_TECHNOLOGY, newCopyOfTechnology);

                }

            } else {
                if (getExistingOrder(order) == null) {
                    Entity copyOfTechnology = technologyDD.create();
                    copyOfTechnology = technologyDD.copy(order.getBelongsToField(TECHNOLOGY).getId()).get(0);
                    copyOfTechnology.setField(TechnologyFields.NUMBER, numberGeneratorService.generateNumber(
                            TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY));
                    copyOfTechnology.setField("patternTechnology", order.getBelongsToField(TECHNOLOGY));
                    copyOfTechnology.setField(TechnologyFields.TECHNOLOGY_TYPE,
                            TechnologyType.WITH_PATTERN_TECHNOLOGY.getStringValue());
                    copyOfTechnology = copyOfTechnology.getDataDefinition().save(copyOfTechnology);
                    order.setField(OrderFields.COPY_OF_TECHNOLOGY, copyOfTechnology);
                }
            }
        } else if (OrderType.WITH_OWN_TECHNOLOGY.getStringValue().equals(order.getStringField(OrderFields.ORDER_TYPE))) {

            DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY);

            if (isTechnologyCopied(order)) {
                Entity technology = order.getBelongsToField(TECHNOLOGY);
                if (technology != null) {
                    order.setField(OrderFields.TECHNOLOGY, null);
                    Entity copyOfTechnology = order.getBelongsToField(OrderFields.COPY_OF_TECHNOLOGY);
                    copyOfTechnology.setField("patternTechnology", null);
                    if (isTypeOrderChnagedToOwn(order)) {
                        copyOfTechnology.setField(TechnologyFields.TECHNOLOGY_TYPE,
                                TechnologyType.WITH_OWN_TECHNOLOGY.getStringValue());
                    }
                    copyOfTechnology.getDataDefinition().save(copyOfTechnology);
                }

            } else {
                if (getExistingOrder(order) == null) {
                    Entity copyOfTechnology = technologyDD.create();
                    copyOfTechnology.setField(TechnologyFields.NUMBER, numberGeneratorService.generateNumber(
                            TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY));
                    copyOfTechnology.setField(
                            TechnologyFields.NAME,
                            makeTechnologyName(copyOfTechnology.getStringField(TechnologyFields.NUMBER),
                                    order.getBelongsToField(OrderFields.PRODUCT)));
                    copyOfTechnology.setField(TechnologyFields.PRODUCT, order.getBelongsToField(OrderFields.PRODUCT));
                    copyOfTechnology.setField("patternTechnology", order.getBelongsToField(TECHNOLOGY));
                    copyOfTechnology = copyOfTechnology.getDataDefinition().save(copyOfTechnology);
                    copyOfTechnology.setField(TechnologyFields.TECHNOLOGY_TYPE,
                            TechnologyType.WITH_OWN_TECHNOLOGY.getStringValue());
                    order.setField(OrderFields.COPY_OF_TECHNOLOGY, copyOfTechnology);
                    Entity technology = order.getBelongsToField(TECHNOLOGY);
                    if (technology != null) {
                        order.setField(OrderFields.TECHNOLOGY, null);
                    }
                }
            }

        }

    }

    public String makeTechnologyName(final String technologyNumber, final Entity product) {
        return technologyNumber + " - " + product.getStringField(ProductFields.NUMBER);
    }

    private boolean isTypeOrderChnagedToPattern(final Entity order) {

        if (getExistingOrder(order) == null) {
            return false;
        }
        Entity existingOrder = getExistingOrder(order);
        String orderTypeDB = existingOrder.getStringField(OrderFields.ORDER_TYPE);
        if (orderTypeDB.equals(OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue())) {
            return false;
        }
        return true;
    }

    private boolean isTypeOrderChnagedToOwn(final Entity order) {
        Entity existingOrder = getExistingOrder(order);
        String orderTypeDB = existingOrder.getStringField(OrderFields.ORDER_TYPE);
        if (orderTypeDB.equals(OrderType.WITH_OWN_TECHNOLOGY.getStringValue())) {
            return false;
        }
        return true;
    }

    private boolean isTechnologyCopied(final Entity order) {

        if (order.getField(OrderFields.COPY_OF_TECHNOLOGY) == null) {
            return false;
        }

        return true;
    }

    private boolean technologyWasChanged(final Entity order) {
        Entity existingOrder = getExistingOrder(order);
        if (existingOrder == null) {
            return false;
        }

        Entity technology = order.getBelongsToField(TECHNOLOGY);
        Entity existingOrderTechnology = existingOrder.getBelongsToField(TECHNOLOGY);
        if (existingOrderTechnology == null) {
            return true;
        }

        if (!existingOrderTechnology.equals(technology)) {
            if (order.getBelongsToField(OrderFields.COPY_OF_TECHNOLOGY) != null
                    && existingOrder.getBelongsToField(OrderFields.COPY_OF_TECHNOLOGY) == null) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    private Entity getExistingOrder(final Entity order) {
        if (order.getId() == null) {
            return null;
        }
        return order.getDataDefinition().get(order.getId());
    }

}
