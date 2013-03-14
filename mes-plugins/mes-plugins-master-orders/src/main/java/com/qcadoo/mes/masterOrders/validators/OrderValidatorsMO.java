package com.qcadoo.mes.masterOrders.validators;

import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.ADD_MASTER_PREFIX_TO_NUMBER;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields.MASTER_ORDER;
import static com.qcadoo.mes.orders.constants.OrderFields.COMPANY;
import static com.qcadoo.mes.orders.constants.OrderFields.DEADLINE;
import static com.qcadoo.mes.orders.constants.OrderFields.NAME;
import static com.qcadoo.mes.orders.constants.OrderFields.PRODUCT;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OrderValidatorsMO {

    private static final String L_MASTER_ORDERS_ORDER_MASTER_ORDER = "masterOrders.order.masterOrder.";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean checkOrderNumber(final DataDefinition orderDD, final Entity order) {
        Entity masterOrder = order.getBelongsToField(MASTER_ORDER);

        if (masterOrder == null) {
            return true;
        }

        if (!masterOrder.getBooleanField(ADD_MASTER_PREFIX_TO_NUMBER)) {
            return true;
        }

        String masterOrderNumber = masterOrder.getStringField(MasterOrderFields.NUMBER);
        String orderNumber = order.getStringField(OrderFields.NUMBER);

        if (!orderNumber.startsWith(masterOrderNumber)) {
            order.addError(orderDD.getField(OrderFields.NUMBER), "masterOrders.order.number.numberHasNotPreffix",
                    masterOrderNumber);

            return false;
        }

        return true;
    }

    public boolean checkCompanyAndDeadline(final DataDefinition orderDD, final Entity order) {
        boolean isValid = true;

        Entity masterOrder = order.getBelongsToField(MASTER_ORDER);

        if (masterOrder == null) {
            return isValid;
        }

        if (!checkIfBelongToFieldIsTheSame(order, masterOrder, COMPANY)) {
            Entity company = masterOrder.getBelongsToField(COMPANY);

            order.addError(orderDD.getField(COMPANY), "masterOrders.order.masterOrder.company.fieldIsNotTheSame",
                    createInfoAboutEntity(company, "company"));

            isValid = false;
        }

        if (!checkIfDeadlineIsCorrect(order, masterOrder)) {
            Date deadline = (Date) masterOrder.getField(DEADLINE);

            order.addError(
                    orderDD.getField(DEADLINE),
                    "masterOrders.order.masterOrder.deadline.fieldIsNotTheSame",
                    deadline == null ? translationService.translate("masterOrders.order.masterOrder.deadline.hasNotDeadline",
                            Locale.getDefault()) : DateUtils.toDateTimeString(deadline));

            isValid = false;
        }

        return isValid;
    }

    public boolean checkProductAndTechnology(final DataDefinition orderDD, final Entity order) {
        Entity masterOrder = order.getBelongsToField(MASTER_ORDER);

        boolean isValid = true;

        if (masterOrder == null) {
            return isValid;
        }

        String masterOrderType = masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE);

        if (masterOrderType == null || masterOrderType.equals(MasterOrderType.UNDEFINED.getStringValue())) {
            return isValid;
        }

        if (masterOrderType.equals(MasterOrderType.ONE_PRODUCT.getStringValue())) {
            if (!checkIfBelongToFieldIsTheSame(order, masterOrder, PRODUCT)) {
                isValid = false;
                Entity product = masterOrder.getBelongsToField(PRODUCT);
                order.addError(orderDD.getField(PRODUCT), L_MASTER_ORDERS_ORDER_MASTER_ORDER + PRODUCT + ""
                        + ".fieldIsNotTheSame", createInfoAboutEntity(product, PRODUCT));
            }
            if (!checkIfBelongToFieldIsTheSame(order, masterOrder, TECHNOLOGY)) {
                isValid = false;
                Entity technology = masterOrder.getBelongsToField(TECHNOLOGY);
                order.addError(orderDD.getField(TECHNOLOGY), L_MASTER_ORDERS_ORDER_MASTER_ORDER + TECHNOLOGY + ""
                        + ".fieldIsNotTheSame", createInfoAboutEntity(technology, TECHNOLOGY));
            }
        } else if (masterOrderType.equals(MasterOrderType.MANY_PRODUCTS.getStringValue())) {
            if (!checkIfExistsMasterOrderWithTech(order, masterOrder)) {
                isValid = false;
                order.addError(orderDD.getField(TECHNOLOGY), L_MASTER_ORDERS_ORDER_MASTER_ORDER + TECHNOLOGY
                        + ".masterOrderProductDoesnotExists");
            }
            if (!checkIfExistsMasterOrderWithProduct(order, masterOrder)) {
                isValid = false;
                order.addError(orderDD.getField(PRODUCT), L_MASTER_ORDERS_ORDER_MASTER_ORDER + PRODUCT
                        + ".masterOrderProductDoesnotExists");
            }
        }

        return isValid;
    }

    private boolean checkIfExistsMasterOrderWithTech(final Entity order, final Entity masterOrder) {
        List<Entity> masterOrderProductsWithProductAndTechnology = dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER_PRODUCT).find()
                .add(SearchRestrictions.belongsTo(PRODUCT, order.getBelongsToField(PRODUCT)))
                .add(SearchRestrictions.belongsTo(MASTER_ORDER, masterOrder))
                .add(SearchRestrictions.belongsTo(TECHNOLOGY, order.getBelongsToField(TECHNOLOGY))).list().getEntities();

        if (masterOrderProductsWithProductAndTechnology.isEmpty()) {
            masterOrderProductsWithProductAndTechnology = dataDefinitionService
                    .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER_PRODUCT).find()
                    .add(SearchRestrictions.belongsTo(PRODUCT, order.getBelongsToField(PRODUCT)))
                    .add(SearchRestrictions.belongsTo(MASTER_ORDER, masterOrder))
                    .add(SearchRestrictions.not(SearchRestrictions.belongsTo(TECHNOLOGY, order.getBelongsToField(TECHNOLOGY))))
                    .list().getEntities();
            if (!masterOrderProductsWithProductAndTechnology.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean checkIfExistsMasterOrderWithProduct(final Entity order, final Entity masterOrder) {
        List<Entity> masterOrderProductsWithProduct = dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER_PRODUCT).find()
                .add(SearchRestrictions.belongsTo(PRODUCT, order.getBelongsToField(PRODUCT)))
                .add(SearchRestrictions.belongsTo(MASTER_ORDER, masterOrder)).list().getEntities();

        if (masterOrderProductsWithProduct.isEmpty()) {
            masterOrderProductsWithProduct = dataDefinitionService
                    .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER_PRODUCT).find()
                    .add(SearchRestrictions.belongsTo(MASTER_ORDER, masterOrder))
                    .add(SearchRestrictions.not(SearchRestrictions.belongsTo(PRODUCT, order.getBelongsToField(PRODUCT)))).list()
                    .getEntities();
            if (!masterOrderProductsWithProduct.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean checkIfBelongToFieldIsTheSame(final Entity order, final Entity masterOrder, final String reference) {
        Entity fieldFromMaster = masterOrder.getBelongsToField(reference);
        Entity fieldFromOrder = order.getBelongsToField(reference);

        if ((fieldFromMaster == null && fieldFromOrder == null) || (fieldFromMaster == null && fieldFromOrder != null)) {
            return true;
        }

        if (fieldFromMaster != null && fieldFromOrder != null && fieldFromOrder.getId().equals(fieldFromMaster.getId())) {
            return true;
        }

        return false;
    }

    private boolean checkIfDeadlineIsCorrect(final Entity order, final Entity masterOrder) {
        Date deadlineFromMaster = masterOrder.getDateField(DEADLINE);
        Date deadlineFromOrder = order.getDateField(DEADLINE);
        if (order.getStringField(OrderFields.STATE).equals(OrderState.PENDING.getStringValue())
                || (deadlineFromMaster == null && deadlineFromOrder == null)
                || (deadlineFromMaster == null && deadlineFromOrder != null)) {
            return true;
        }

        if ((deadlineFromMaster != null && deadlineFromOrder == null)) {
            return false;
        }

        if (deadlineFromOrder.equals(deadlineFromMaster)) {
            return true;
        }
        order.addError(order.getDataDefinition().getField(DEADLINE), "masterOrders.masterOrder.deadline.isIncorrect");
        return false;
    }

    private String createInfoAboutEntity(final Entity entity, final String fieldName) {
        return entity == null ? translationService.translate("masterOrders.order.masterOrder.hasNot" + fieldName,
                Locale.getDefault()) : entity.getStringField(NUMBER) + " - " + entity.getStringField(NAME);
    }

}
