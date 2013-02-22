package com.qcadoo.mes.masterOrders.validators;

import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.ADD_MASTER_PREFIX_TO_NUMBER;
import static com.qcadoo.mes.masterOrders.constants.OrderFieldsMO.MASTER_ORDER;
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
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

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
                            Locale.getDefault()) : deadline.toString());
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
        if (masterOrderType == null) {
            return isValid;
        }
        if (masterOrderType.equals(MasterOrderType.ONE_PRODUCT.getStringValue())) {
            if (!checkTechnologyAndProductFieldForOneProductType(orderDD, order, masterOrder)) {
                isValid = false;
            }
        } else if (masterOrderType.equals(MasterOrderType.MANY_PRODUCTS.getStringValue())) {
            if (checkIfExistsMasterOrderWithTechAndProduct(orderDD, order, masterOrder)) {
                isValid = false;
            }
        }
        if (!isValid) {
            addErrorToEntity(orderDD, order, masterOrder, TECHNOLOGY);
            addErrorToEntity(orderDD, order, masterOrder, PRODUCT);
        }
        return isValid;
    }

    private boolean checkTechnologyAndProductFieldForOneProductType(final DataDefinition orderDD, final Entity order,
            final Entity masterOrder) {
        boolean isValid = true;
        if (!checkIfBelongToFieldIsTheSame(order, masterOrder, PRODUCT)) {
            isValid = false;
        }
        if (!checkIfBelongToFieldIsTheSame(order, masterOrder, TECHNOLOGY)) {
            isValid = false;
        }
        return isValid;
    }

    private boolean checkIfExistsMasterOrderWithTechAndProduct(final DataDefinition orderDD, final Entity order,
            final Entity masterOrder) {
        List<Entity> masterOrdersWithProductAndTechnology = masterOrder.getDataDefinition().find()
                .add(SearchRestrictions.belongsTo(PRODUCT, order.getBelongsToField(PRODUCT)))
                .add(SearchRestrictions.belongsTo(TECHNOLOGY, order.getBelongsToField(TECHNOLOGY))).list().getEntities();
        return masterOrdersWithProductAndTechnology.isEmpty();
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
        Date deadlineFromMaster = (Date) masterOrder.getField(DEADLINE);
        Date deadlineFromOrder = (Date) order.getField(DEADLINE);
        if ((deadlineFromMaster == null && deadlineFromOrder == null)
                || (deadlineFromMaster == null && deadlineFromOrder != null)) {
            return true;
        }
        if ((deadlineFromMaster != null && deadlineFromOrder == null)
                || (deadlineFromMaster != null && deadlineFromOrder == null)) {
            return false;
        }
        if (deadlineFromOrder.equals(deadlineFromMaster)) {
            return true;
        }
        return false;
    }

    private void addErrorToEntity(final DataDefinition orderDD, final Entity masterOrder, final Entity order,
            final String fieldName) {
        Entity entity = masterOrder.getBelongsToField(fieldName);
        order.addError(orderDD.getField(fieldName), "masterOrders.order.masterOrder." + fieldName + "" + ".fieldIsNotTheSame",
                createInfoAboutEntity(entity, fieldName));
    }

    private String createInfoAboutEntity(final Entity entity, final String fieldName) {
        return entity == null ? translationService.translate("masterOrders.order.masterOrder.hasNot" + fieldName,
                Locale.getDefault()) : entity.getStringField(NUMBER) + " - " + entity.getStringField(NAME);
    }

}
