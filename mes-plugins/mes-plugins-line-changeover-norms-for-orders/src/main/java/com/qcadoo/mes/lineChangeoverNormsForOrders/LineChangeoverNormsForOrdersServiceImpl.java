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
package com.qcadoo.mes.lineChangeoverNormsForOrders;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyGroupFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class LineChangeoverNormsForOrdersServiceImpl implements LineChangeoverNormsForOrdersService {

    private static final String L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_PLANNED = "lineChangeoverNormsForOrders.dateIs.planned";

    private static final String L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_CORRECTED = "lineChangeoverNormsForOrders.dateIs.corrected";

    private static final String L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_EFFECTIVE = "lineChangeoverNormsForOrders.dateIs.effective";

    private static final String L_PREVIOUS_ORDER_DATE_TO = "previousOrderDateTo";

    private static final String L_DATE_FROM = "dateFrom";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ChangeoverNormsService changeoverNormsService;

    @Override
    public void fillOrderForm(final ViewDefinitionState view, final List<String> orderFields) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(orderFields.get(0));
        Entity order = orderLookup.getEntity();

        if (Objects.isNull(order)) {
            return;
        }

        FieldComponent technologyNumberField = (FieldComponent) view.getComponentByReference(orderFields.get(1));
        FieldComponent technologyGroupNumberField = (FieldComponent) view.getComponentByReference(orderFields.get(2));
        FieldComponent dateToFromField = (FieldComponent) view.getComponentByReference(orderFields.get(3));
        FieldComponent dateIsField = (FieldComponent) view.getComponentByReference(orderFields.get(4));

        orderLookup.setFieldValue(order.getId());
        orderLookup.requestComponentUpdateState();

        setUpTechnologyFields(order, technologyNumberField, technologyGroupNumberField);

        Date dateToFrom = null;
        String dateIs = null;

        if (L_PREVIOUS_ORDER_DATE_TO.equals(orderFields.get(3))) {
            Date effectiveDateTo = order.getDateField(OrderFields.EFFECTIVE_DATE_TO);
            Date correctedDateTo = order.getDateField(OrderFields.CORRECTED_DATE_TO);
            Date dateTo = order.getDateField(OrderFields.DATE_TO);

            if (Objects.nonNull(effectiveDateTo)) {
                dateToFrom = effectiveDateTo;
                dateIs = L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_EFFECTIVE;
            } else if (Objects.nonNull(correctedDateTo)) {
                dateToFrom = correctedDateTo;
                dateIs = L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_CORRECTED;
            } else if (Objects.nonNull(dateTo)) {
                dateToFrom = dateTo;
                dateIs = L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_PLANNED;
            }
        } else if (L_DATE_FROM.equals(orderFields.get(3))) {
            Date effectiveDateFrom = order.getDateField(OrderFields.EFFECTIVE_DATE_FROM);
            Date correctedDateFrom = order.getDateField(OrderFields.CORRECTED_DATE_FROM);
            Date dateFrom = order.getDateField(OrderFields.DATE_FROM);

            if (Objects.nonNull(effectiveDateFrom)) {
                dateToFrom = effectiveDateFrom;
                dateIs = L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_EFFECTIVE;
            } else if (Objects.nonNull(correctedDateFrom)) {
                dateToFrom = correctedDateFrom;
                dateIs = L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_CORRECTED;
            } else if (Objects.nonNull(dateFrom)) {
                dateToFrom = dateFrom;
                dateIs = L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_PLANNED;
            }
        }

        if (Objects.isNull(dateToFrom)) {
            dateToFromField.setFieldValue(null);
            dateIsField.setFieldValue(null);
        } else {
            dateToFromField.setFieldValue(new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault())
                    .format(dateToFrom));
            dateIsField.setFieldValue(translationService.translate(dateIs, LocaleContextHolder.getLocale()));
        }

        dateToFromField.requestComponentUpdateState();
        dateIsField.requestComponentUpdateState();
    }

    private void setUpTechnologyFields(final Entity order, final FieldComponent technologyNumberField,
                                       final FieldComponent technologyGroupNumberField) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        String technologyNumber = extractTechnologyNumberFrom(technology);
        technologyNumberField.setFieldValue(technologyNumber);
        technologyNumberField.requestComponentUpdateState();

        String technologyGroupNumber = extractTechnologyGroupNumberFrom(technology);
        technologyGroupNumberField.setFieldValue(technologyGroupNumber);
        technologyGroupNumberField.requestComponentUpdateState();
    }

    private String extractTechnologyGroupNumberFrom(final Entity technology) {
        if (Objects.isNull(technology)) {
            return null;
        }

        Entity technologyGroup = technology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP);

        if (Objects.isNull(technologyGroup)) {
            return null;
        }

        return technologyGroup.getStringField(TechnologyGroupFields.NUMBER);
    }

    private String extractTechnologyNumberFrom(final Entity technology) {
        if (Objects.isNull(technology)) {
            return null;
        }

        return technology.getStringField(TechnologyFields.NUMBER);
    }

    @Override
    public boolean previousOrderEndsBeforeOrIsWithdrawed(final Entity previousOrder, final Entity order) {
        boolean bothOrdersAreNotNull = (Objects.nonNull(previousOrder) && Objects.nonNull(order));

        return !bothOrdersAreNotNull || (!isDeclinedOrAbandoned(previousOrder) && !areDatesCorrect(previousOrder, order));
    }

    private boolean isDeclinedOrAbandoned(final Entity previousOrder) {
        return (OrderState.ABANDONED.getStringValue().equals(previousOrder.getStringField(OrderFields.STATE)) || OrderState.DECLINED
                .getStringValue().equals(previousOrder.getStringField(OrderFields.STATE)));
    }

    private boolean areDatesCorrect(final Entity previousOrder, final Entity order) {
        if (Objects.isNull(previousOrder.getField(OrderFields.FINISH_DATE)) || Objects.isNull(order.getField(OrderFields.START_DATE))) {
            return true;
        }

        return previousOrder.getDateField(OrderFields.FINISH_DATE).after(order.getDateField(OrderFields.START_DATE));
    }

    @Override
    public Entity getProductionLineFromDB(final Long productionLineId) {
        return dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_PRODUCTION_LINE).get(productionLineId);
    }

    @Override
    public Entity getTechnologyByNumberFromDB(final String number) {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY).find()
                .add(SearchRestrictions.eq(TechnologyFields.NUMBER, number)).setMaxResults(1).uniqueResult();
    }

    @Override
    public Entity getTechnologyGroupByNumberFromDB(final String number) {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_GROUP)
                .find().add(SearchRestrictions.eq(TechnologyGroupFields.NUMBER, number)).setMaxResults(1).uniqueResult();
    }

    @Override
    public Entity getPreviousOrderFromDB(final Entity order) {
        return dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                .find()
                .add(SearchRestrictions.belongsTo(OrderFields.PRODUCTION_LINE,
                        order.getBelongsToField(OrderFields.PRODUCTION_LINE)))
                .add(SearchRestrictions.not(SearchRestrictions.in(OrderFields.STATE, Lists.newArrayList(OrderState.DECLINED.getStringValue(), OrderState.ABANDONED.getStringValue()))))
                .add(SearchRestrictions.lt(OrderFields.FINISH_DATE, order.getDateField(OrderFields.START_DATE)))
                .addOrder(SearchOrders.desc(OrderFields.FINISH_DATE)).setMaxResults(1).uniqueResult();
    }

    @Override
    public Entity getChangeover(final Entity previousOrder, final Entity toTechnology, final Entity productionLine) {
        if (Objects.isNull(previousOrder) || Objects.isNull(previousOrder.getBelongsToField(OrderFields.TECHNOLOGY))
                || Objects.isNull(toTechnology)) {
            return null;
        }

        Entity fromTechnology = previousOrder.getBelongsToField(OrderFields.TECHNOLOGY);

        return changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology, productionLine);
    }

}
