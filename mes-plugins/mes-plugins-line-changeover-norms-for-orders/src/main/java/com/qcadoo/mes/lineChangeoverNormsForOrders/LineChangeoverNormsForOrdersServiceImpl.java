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
import org.springframework.stereotype.Service;

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

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public void fillOrderForm(final ViewDefinitionState view, final List<String> orderFields) {
        FieldComponent orderField = (FieldComponent) view.getComponentByReference(orderFields.get(0));

        Long orderId = (Long) orderField.getFieldValue();

        if (orderId != null) {
            Entity order = getOrderFromDB(orderId);

            if (order != null) {
                FieldComponent technologyNumberField = (FieldComponent) view.getComponentByReference(orderFields.get(1));
                FieldComponent technologyGroupNumberField = (FieldComponent) view.getComponentByReference(orderFields.get(2));
                FieldComponent dateFromField = (FieldComponent) view.getComponentByReference(orderFields.get(3));
                FieldComponent dateToField = (FieldComponent) view.getComponentByReference(orderFields.get(4));

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

                Date dateFrom = (Date) order.getField(DATE_FROM);
                Date correctedDateFrom = (Date) order.getField(CORRECTED_DATE_FROM);
                Date effectiveDateFrom = (Date) order.getField(EFFECTIVE_DATE_FROM);

                if (effectiveDateFrom != null) {
                    dateFromField.setFieldValue(new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault())
                            .format(effectiveDateFrom));
                } else if (correctedDateFrom != null) {
                    dateFromField.setFieldValue(new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault())
                            .format(correctedDateFrom));
                } else if (dateFrom != null) {
                    dateFromField.setFieldValue(new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault())
                            .format(dateFrom));
                }

                Date dateTo = (Date) order.getField(DATE_TO);
                Date correctedDateTo = (Date) order.getField(CORRECTED_DATE_TO);
                Date effectiveDateTo = (Date) order.getField(EFFECTIVE_DATE_TO);

                if (effectiveDateTo != null) {
                    dateToField.setFieldValue(new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault())
                            .format(effectiveDateTo));
                } else if (correctedDateTo != null) {
                    dateToField.setFieldValue(new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault())
                            .format(correctedDateTo));
                } else if (dateTo != null) {
                    dateToField.setFieldValue(new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault())
                            .format(dateTo));
                }
            }
        }
    }

    @Override
    public boolean checkIfOrderHasCorrectStateAndIsPrevious(final Entity previousOrder, final Entity order) {
        if ((previousOrder != null)
                && order != null
                && (ABANDONED.getStringValue().equals(previousOrder.getStringField(STATE))
                        || DECLINED.getStringValue().equals(previousOrder.getStringField(STATE)) || !checkDateIfUncorrect(
                        previousOrder, order))) {
            return false;
        }

        return true;
    }

    private boolean checkDateIfUncorrect(final Entity previousOrder, final Entity order) {
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
