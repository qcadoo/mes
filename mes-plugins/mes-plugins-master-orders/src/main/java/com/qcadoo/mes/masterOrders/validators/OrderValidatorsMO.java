package com.qcadoo.mes.masterOrders.validators;

import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.ADD_MASTER_PREFIX_TO_NUMBER;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields.MASTER_ORDER;
import static com.qcadoo.mes.orders.constants.OrderFields.*;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.id;
import static com.qcadoo.model.api.search.SearchRestrictions.*;

import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;

@Service
public class OrderValidatorsMO {

    @Autowired
    private TranslationService translationService;

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
            order.addError(orderDD.getField(OrderFields.NUMBER), "masterOrders.order.number.numberHasNotPrefix",
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

        if (masterOrder == null) {
            return true;
        }

        MasterOrderType masterOrderType = MasterOrderType.of(masterOrder);

        if (masterOrderType != MasterOrderType.UNDEFINED && !orderHasPatternTechnology(orderDD, order)) {
            return false;
        }

        if (masterOrderType == MasterOrderType.ONE_PRODUCT) {
            return checkIfOrderMatchesMasterOrderSingleProductAndTechnology(order, masterOrder);
        }
        if (masterOrderType == MasterOrderType.MANY_PRODUCTS) {
            return checkIfOrderMatchesAnyOfMasterOrderProductsWithTechnology(order, masterOrder);
        }

        return true;
    }

    /* Precondition - order is not null */
    private boolean orderHasPatternTechnology(final DataDefinition orderDD, final Entity order) {
        if (OrderType.of(order) != OrderType.WITH_PATTERN_TECHNOLOGY) {
            order.addError(orderDD.getField(OrderFields.ORDER_TYPE), "masterOrders.order.masterOrder.wrongOrderType");
            return false;
        }
        return true;
    }

    private boolean checkIfOrderMatchesMasterOrderSingleProductAndTechnology(final Entity order, final Entity masterOrder) {
        boolean orderMatchesCriteria = checkIfOrderMatchesMasterOrderSingleProduct(order, masterOrder);
        orderMatchesCriteria = checkIfOrderMatchesMasterOrderSingleTechnology(order, masterOrder) && orderMatchesCriteria;
        return orderMatchesCriteria;
    }

    private boolean checkIfOrderMatchesMasterOrderSingleProduct(final Entity order, final Entity masterOrder) {
        Entity masterOrderProduct = masterOrder.getBelongsToField(MasterOrderFields.PRODUCT);
        if (masterOrderProduct == null) {
            return true;
        }
        Entity orderProduct = order.getBelongsToField(OrderFields.PRODUCT);
        if (ObjectUtils.equals(orderProduct.getId(), masterOrderProduct.getId())) {
            return true;
        }
        addMatchValidationError(order, OrderFields.PRODUCT, createInfoAboutEntity(masterOrderProduct, OrderFields.PRODUCT));
        return false;
    }

    private boolean checkIfOrderMatchesMasterOrderSingleTechnology(final Entity order, final Entity masterOrder) {
        Entity masterOrderTechnology = masterOrder.getBelongsToField(MasterOrderFields.TECHNOLOGY);
        if (masterOrderTechnology == null) {
            return true;
        }
        Entity orderTechnologyPrototype = order.getBelongsToField(TECHNOLOGY_PROTOTYPE);
        if (orderTechnologyPrototype != null
                && ObjectUtils.equals(masterOrderTechnology.getId(), orderTechnologyPrototype.getId())) {
            return true;
        }

        addMatchValidationError(order, OrderFields.TECHNOLOGY_PROTOTYPE, createInfoAboutEntity(masterOrderTechnology, TECHNOLOGY));
        return false;
    }

    private boolean checkIfOrderMatchesAnyOfMasterOrderProductsWithTechnology(final Entity order, final Entity masterOrder) {
        if (hasMatchingMasterOrderProducts(order, masterOrder)) {
            return true;
        }
        addMatchValidationError(order, OrderFields.PRODUCT, null);
        return false;
    }

    private boolean hasMatchingMasterOrderProducts(final Entity order, final Entity masterOrder) {
        Entity orderTechnologyPrototype = order.getBelongsToField(TECHNOLOGY_PROTOTYPE);
        Entity orderProduct = order.getBelongsToField(OrderFields.PRODUCT);

        SearchCriteriaBuilder masterCriteria = masterOrder.getDataDefinition().find();
        masterCriteria.setProjection(alias(id(), "id"));
        masterCriteria.add(idEq(masterOrder.getId()));

        SearchCriteriaBuilder masterProductsCriteria = masterCriteria.createCriteria(MasterOrderFields.MASTER_ORDER_PRODUCTS,
                "masterProducts", JoinType.INNER);
        masterProductsCriteria.add(belongsTo(MasterOrderProductFields.PRODUCT, orderProduct));
        if (orderTechnologyPrototype == null) {
            masterProductsCriteria.add(isNull(MasterOrderProductFields.TECHNOLOGY));
        } else {
            masterProductsCriteria.add(or(isNull(MasterOrderProductFields.TECHNOLOGY),
                    belongsTo(MasterOrderProductFields.TECHNOLOGY, orderTechnologyPrototype)));
        }
        return masterCriteria.setMaxResults(1).uniqueResult() != null;
    }

    private void addMatchValidationError(final Entity toOrder, final String fieldName, final String entityInfo) {
        if (entityInfo == null) {
            String errorMessage = String.format("masterOrders.order.masterOrder.%s.masterOrderProductDoesNotExist", fieldName);
            toOrder.addError(toOrder.getDataDefinition().getField(fieldName), errorMessage);
        } else {
            String errorMessage = String.format("masterOrders.order.masterOrder.%s.fieldIsNotTheSame", fieldName);
            toOrder.addError(toOrder.getDataDefinition().getField(fieldName), errorMessage, entityInfo);
        }
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
        if ((deadlineFromMaster == null && deadlineFromOrder == null)
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
