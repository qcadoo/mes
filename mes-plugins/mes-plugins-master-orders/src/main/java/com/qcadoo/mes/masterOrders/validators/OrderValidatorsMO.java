/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.masterOrders.validators;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.id;
import static com.qcadoo.model.api.search.SearchRestrictions.*;

@Service
public class OrderValidatorsMO {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean checkOrderNumber(final DataDefinition orderDD, final Entity order) {
        Entity masterOrder = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);

        if (masterOrder == null) {
            return true;
        }

        if (!masterOrder.getBooleanField(MasterOrderFields.ADD_MASTER_PREFIX_TO_NUMBER)) {
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

        Entity masterOrder = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);

        if (masterOrder == null) {
            return isValid;
        }

        if (!checkIfBelongToFieldIsTheSame(order, masterOrder, OrderFields.COMPANY)) {
            Entity company = masterOrder.getBelongsToField(OrderFields.COMPANY);

            order.addError(orderDD.getField(OrderFields.COMPANY), "masterOrders.order.masterOrder.company.fieldIsNotTheSame",
                    createInfoAboutEntity(company, "company"));

            isValid = false;
        }

        if (!checkIfDeadlineIsCorrect(order, masterOrder)) {
            Date deadline = masterOrder.getDateField(MasterOrderFields.DEADLINE);

            order.addError(orderDD.getField(OrderFields.DEADLINE), "masterOrders.order.masterOrder.deadline.fieldIsNotTheSame",
                    deadline == null ? translationService.translate("masterOrders.order.masterOrder.deadline.hasNotDeadline",
                            Locale.getDefault()) : DateUtils.toDateTimeString(deadline));

            isValid = false;
        }

        return isValid;
    }

    public boolean checkProductAndTechnology(final DataDefinition orderDD, final Entity order) {
        Entity mo = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);

        if (Objects.isNull(mo)) {
            return true;
        }

        if(order.getBooleanField("isUpdateTechnologiesOnPendingOrders")) {
            return true;
        }

        StringBuilder query = new StringBuilder();

        query.append("SELECT masterOrder.id as masterOrderId, masterOrder.number as masterOrderNumber, ");
        query.append(
                "(select count(mproduct)  FROM #masterOrders_masterOrderProduct mproduct WHERE mproduct.masterOrder.id = masterOrder.id) as positions ");
        query.append("FROM #masterOrders_masterOrder masterOrder ");
        query.append("WHERE masterOrder.id = :oid");

        Entity masterOrder = orderDD.find(query.toString()).setLong("oid", mo.getId()).setMaxResults(1).uniqueResult();

        if (Objects.isNull(masterOrder) || masterOrder.getLongField("positions") == 0l) {
            return true;
        }

        return checkIfOrderMatchesAnyOfMasterOrderProductsWithTechnology(order, masterOrder);
    }

    private boolean checkIfOrderMatchesAnyOfMasterOrderProductsWithTechnology(final Entity order, final Entity masterOrder) {
        if (hasMatchingMasterOrderProducts(order, masterOrder)) {
            return true;
        }

        addMatchValidationError(order, OrderFields.PRODUCT, null);

        return false;
    }

    private boolean hasMatchingMasterOrderProducts(final Entity order, final Entity masterOrder) {
        Entity orderTechnologyPrototype = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);
        Entity orderProduct = order.getBelongsToField(OrderFields.PRODUCT);

        SearchCriteriaBuilder masterCriteria = dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER).find();
        masterCriteria.setProjection(alias(id(), "id"));
        masterCriteria.add(idEq(masterOrder.getLongField("masterOrderId")));

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
        Date deadlineFromMaster = masterOrder.getDateField(MasterOrderFields.DEADLINE);
        Date deadlineFromOrder = order.getDateField(OrderFields.DEADLINE);

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

        order.addError(order.getDataDefinition().getField(OrderFields.DEADLINE), "masterOrders.masterOrder.deadline.isIncorrect");

        return false;
    }

    private String createInfoAboutEntity(final Entity entity, final String fieldName) {
        return entity == null
                ? translationService.translate("masterOrders.order.masterOrder.hasNot" + fieldName, Locale.getDefault())
                : entity.getStringField(MasterOrderFields.NUMBER) + " - " + entity.getStringField(MasterOrderFields.NAME);
    }

}
