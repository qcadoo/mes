package com.qcadoo.mes.productionScheduling;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;

import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public interface OrderRealizationTimeService {

    int MAX_REALIZATION_TIME = 99999 * 60 * 60;

    void changeDateFrom(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args);

    void changeDateTo(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args);

    BigDecimal getBigDecimalFromField(final Object value, final Locale locale);

    int estimateRealizationTimeForOperation(final EntityTreeNode operationComponent, final BigDecimal plannedQuantity);

    Date getDateFromField(final Object value);

    Object setDateToField(final Date date);

}