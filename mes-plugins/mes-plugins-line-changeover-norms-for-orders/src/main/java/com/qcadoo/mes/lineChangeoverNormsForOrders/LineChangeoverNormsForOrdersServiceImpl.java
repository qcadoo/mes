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
package com.qcadoo.mes.lineChangeoverNormsForOrders;

import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.NUMBER;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.states.constants.OrderState.ABANDONED;
import static com.qcadoo.mes.orders.states.constants.OrderState.DECLINED;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
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

@Service
public class LineChangeoverNormsForOrdersServiceImpl implements LineChangeoverNormsForOrdersService {

    private static final String L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_PLANNED = "lineChangeoverNormsForOrders.dateIs.planned";

    private static final String L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_CORRECTED = "lineChangeoverNormsForOrders.dateIs.corrected";

    private static final String L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_EFFECTIVE = "lineChangeoverNormsForOrders.dateIs.effective";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Override
    public void fillOrderForm(final ViewDefinitionState view, final List<String> orderFields) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(orderFields.get(0));
        Entity order = orderLookup.getEntity();
        if (order == null) {
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

        if ("previousOrderDateTo".equals(orderFields.get(3))) {
            Date effectiveDateTo = (Date) order.getField(EFFECTIVE_DATE_TO);
            Date correctedDateTo = (Date) order.getField(CORRECTED_DATE_TO);
            Date dateTo = (Date) order.getField(DATE_TO);

            if (effectiveDateTo != null) {
                dateToFrom = effectiveDateTo;
                dateIs = L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_EFFECTIVE;
            } else if (correctedDateTo != null) {
                dateToFrom = correctedDateTo;
                dateIs = L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_CORRECTED;
            } else if (dateTo != null) {
                dateToFrom = dateTo;
                dateIs = L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_PLANNED;
            }
        } else if ("dateFrom".equals(orderFields.get(3))) {
            Date effectiveDateFrom = (Date) order.getField(EFFECTIVE_DATE_FROM);
            Date correctedDateFrom = (Date) order.getField(CORRECTED_DATE_FROM);
            Date dateFrom = (Date) order.getField(DATE_FROM);

            if (effectiveDateFrom != null) {
                dateToFrom = effectiveDateFrom;
                dateIs = L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_EFFECTIVE;
            } else if (correctedDateFrom != null) {
                dateToFrom = correctedDateFrom;
                dateIs = L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_CORRECTED;
            } else if (dateFrom != null) {
                dateToFrom = dateFrom;
                dateIs = L_LINE_CHANGEOVER_NORMS_FOR_ORDERS_DATE_IS_PLANNED;
            }
        }

        if (dateToFrom == null || dateIs == null) {
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
        Entity technologyPrototype = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);

        String technologyNumber = extractTechnologyNumberFrom(technologyPrototype);
        technologyNumberField.setFieldValue(technologyNumber);
        technologyNumberField.requestComponentUpdateState();

        String technologyGroupNumber = extractTechnologyGroupNumberFrom(technologyPrototype);
        technologyGroupNumberField.setFieldValue(technologyGroupNumber);
        technologyGroupNumberField.requestComponentUpdateState();
    }

    private String extractTechnologyGroupNumberFrom(final Entity technology) {
        if (technology == null) {
            return null;
        }
        Entity technologyGroup = technology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP);
        if (technologyGroup == null) {
            return null;
        }
        return technologyGroup.getStringField(TechnologyGroupFields.NUMBER);
    }

    private String extractTechnologyNumberFrom(final Entity technology) {
        if (technology == null) {
            return null;
        }
        return technology.getStringField(TechnologyFields.NUMBER);
    }

    @Override
    public boolean previousOrderEndsBeforeOrIsWithdrawed(final Entity previousOrder, final Entity order) {
        boolean bothOrdersAreNotNull = previousOrder != null && order != null;
        if (bothOrdersAreNotNull && (isDeclinedOrAbandoned(previousOrder) || areDatesCorrect(previousOrder, order))) {
            return false;
        }

        return true;
    }

    private boolean isDeclinedOrAbandoned(final Entity previousOrder) {
        return ABANDONED.getStringValue().equals(previousOrder.getStringField(STATE))
                || DECLINED.getStringValue().equals(previousOrder.getStringField(STATE));
    }

    private boolean areDatesCorrect(final Entity previousOrder, final Entity order) {
        if (previousOrder.getField(DATE_TO) == null || order.getField(DATE_FROM) == null) {
            return true;
        }
        return previousOrder.getDateField(DATE_TO).after(order.getDateField(DATE_FROM));
    }

    @Override
    public Entity getProductionLineFromDB(final Long productionLineId) {
        return dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_PRODUCTION_LINE).get(productionLineId);
    }

    @Override
    public Entity getTechnologyByNumberFromDB(final String number) {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY).find()
                .add(SearchRestrictions.eq(NUMBER, number)).setMaxResults(1).uniqueResult();
    }

    @Override
    public Entity getTechnologyGroupByNumberFromDB(final String number) {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_GROUP)
                .find().add(SearchRestrictions.eq(NUMBER, number)).setMaxResults(1).uniqueResult();
    }

    @Override
    public Entity getPreviousOrderFromDB(final Entity order) {
        return dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                .find()
                .add(SearchRestrictions.or(SearchRestrictions.ne(STATE, DECLINED.getStringValue()),
                        SearchRestrictions.ne(STATE, ABANDONED.getStringValue())))
                .add(SearchRestrictions.lt(DATE_TO, order.getDateField(DATE_FROM))).addOrder(SearchOrders.desc(DATE_TO))
                .setMaxResults(1).uniqueResult();
    }

}
