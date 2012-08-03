package com.qcadoo.mes.lineChangeoverNormsForOrders;

import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.NUMBER;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static com.qcadoo.mes.orders.states.constants.OrderState.ABANDONED;
import static com.qcadoo.mes.orders.states.constants.OrderState.DECLINED;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.TECHNOLOGY_GROUP;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

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
        FieldComponent orderField = (FieldComponent) view.getComponentByReference(orderFields.get(0));

        Long orderId = (Long) orderField.getFieldValue();

        if (orderId != null) {
            Entity order = getOrderFromDB(orderId);

            if (order != null) {
                FieldComponent technologyNumberField = (FieldComponent) view.getComponentByReference(orderFields.get(1));
                FieldComponent technologyGroupNumberField = (FieldComponent) view.getComponentByReference(orderFields.get(2));
                FieldComponent dateToFromField = (FieldComponent) view.getComponentByReference(orderFields.get(3));
                FieldComponent dateIsField = (FieldComponent) view.getComponentByReference(orderFields.get(4));

                orderField.setFieldValue(order.getId());
                orderField.requestComponentUpdateState();

                Entity technology = order.getBelongsToField(TECHNOLOGY);

                if (technology != null) {
                    Entity technologyGroup = technology.getBelongsToField(TECHNOLOGY_GROUP);

                    String technologyNumber = technology.getStringField(NUMBER);

                    technologyNumberField.setFieldValue(technologyNumber);

                    if (technologyGroup != null) {
                        String technologyGroupNumber = technologyGroup.getStringField(NUMBER);

                        technologyGroupNumberField.setFieldValue(technologyGroupNumber);
                    }
                }

                technologyGroupNumberField.requestComponentUpdateState();
                technologyNumberField.requestComponentUpdateState();

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

                if ((dateToFromField == null) || (dateIs == null)) {
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
        }
    }

    @Override
    public boolean checkIfOrderHasCorrectStateAndIsPrevious(final Entity previousOrder, final Entity order) {
        if ((previousOrder != null)
                && (order != null)
                && (ABANDONED.getStringValue().equals(previousOrder.getStringField(STATE))
                        || DECLINED.getStringValue().equals(previousOrder.getStringField(STATE)) || !checkIfDateIsUncorrect(
                            previousOrder, order))) {
            return false;
        }

        return true;
    }

    private boolean checkIfDateIsUncorrect(final Entity previousOrder, final Entity order) {
        if (previousOrder.getField(DATE_TO) == null || order.getField(DATE_FROM) == null) {
            return false;
        }
        if (((Date) previousOrder.getField(DATE_TO)).getTime() > ((Date) order.getField(DATE_FROM)).getTime()) {
            return false;
        }

        return true;
    }

    @Override
    public Entity getProductionLineFromDB(final Long productionLineId) {
        return dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_PRODUCTION_LINE).get(productionLineId);
    }

    @Override
    public Entity getOrderFromDB(final Long orderId) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
    }

    @Override
    public Entity getTechnologyFromDB(final Long technologyId) {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY).get(
                technologyId);
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
                .add(SearchRestrictions.lt(DATE_TO, (Date) order.getField(DATE_FROM))).orderDescBy(DATE_TO).setMaxResults(1)
                .uniqueResult();
    }

}
