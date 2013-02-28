package com.qcadoo.mes.masterOrders.validators;

import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.COMPANY;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.DEADLINE;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

@Service
public class MasterOrderValidators {

    public boolean checkIfCanChangedCompany(final DataDefinition masterOrderDD, final Entity masterOrder) {
        if (masterOrder.getId() == null) {
            return true;
        }

        Entity masterOrderFromDB = masterOrderDD.get(masterOrder.getId());
        Entity company = masterOrder.getBelongsToField(COMPANY);
        Entity companyFromDB = masterOrderFromDB.getBelongsToField(COMPANY);

        if ((company == null && companyFromDB == null)
                || (company != null && companyFromDB != null && company.getId().equals(companyFromDB.getId()))) {
            return true;
        }

        if (checkIfMasterOrderHaveOrders(masterOrder)) {
            masterOrder.addError(masterOrderDD.getField(COMPANY), "masterOrders.masterOrder.company.orderAlreadyExists");

            return false;
        }

        return true;
    }

    public boolean checkIfCanChangedDeadline(final DataDefinition masterOrderDD, final Entity masterOrder) {
        if (masterOrder.getId() == null) {
            return true;
        }

        Entity masterOrderFromDB = masterOrderDD.get(masterOrder.getId());
        Date deadline = (Date) masterOrder.getField(DEADLINE);
        Date deadlineFromDB = (Date) masterOrderFromDB.getField(DEADLINE);

        if ((deadline == null && deadlineFromDB == null)
                || (deadline != null && deadlineFromDB != null && deadline.equals(deadlineFromDB))) {
            return true;
        }

        if (checkIfMasterOrderHaveOrders(masterOrder)) {
            masterOrder.addError(masterOrderDD.getField(DEADLINE), "masterOrders.masterOrder.deadline.orderAlreadyExists");

            return false;
        }

        return true;
    }

    public boolean checkIfMasterOrderHaveOrderWithWrongName(final DataDefinition masterOrderDD,
            final FieldDefinition fieldDefinition, final Entity masterOrder, final Object prefixMasterOrderDB,
            final Object prefixMasterOrder) {

        if (!(!(Boolean) prefixMasterOrderDB && (Boolean) prefixMasterOrder)) {
            return true;
        }

        List<Entity> orders = masterOrder.getHasManyField(MasterOrderFields.ORDERS);

        if (orders.isEmpty()) {
            return true;
        }

        boolean isValid = true;

        String masterOrderNumber = masterOrder.getStringField(MasterOrderFields.NUMBER);
        StringBuilder ordersNumbers = new StringBuilder();

        for (Entity order : orders) {
            String orderNumber = order.getStringField(OrderFields.NUMBER);

            if (!orderNumber.startsWith(masterOrderNumber)) {
                ordersNumbers.append(orderNumber + ", ");

                isValid = false;
            }
        }

        if (!isValid) {
            masterOrder.addError(masterOrderDD.getField(MasterOrderFields.NUMBER),
                    "masterOrders.order.number.alreadyExistsOrderWithWrongNumber", ordersNumbers.toString());
        }

        return isValid;
    }

    public boolean checkIfCanMakeChanges(final DataDefinition masterOrderDD, final Entity masterOrder) {
        Long masterOrderId = masterOrder.getId();
        if (masterOrderId == null) {
            return true;
        }
        Entity masterOrderFromDB = masterOrderDD.get(masterOrderId);
        if (!masterOrderFromDB.equals(masterOrder)) {
            return false;
        }
        return true;
    }

    private boolean checkIfMasterOrderHaveOrders(final Entity masterOrder) {
        return !masterOrder.getHasManyField(MasterOrderFields.ORDERS).find().list().getEntities().isEmpty();
    }

}
