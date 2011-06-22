package com.qcadoo.mes.productionScheduling;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OrderRealizationTimeService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void changeDateFrom(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent dateTo = (FieldComponent) state;
        FieldComponent dateFrom = (FieldComponent) viewDefinitionState.getComponentByReference("dateFrom");
        if (StringUtils.hasText((String) dateTo.getFieldValue()) && !StringUtils.hasText((String) dateFrom.getFieldValue())) {
            // TODO KRNA update dateFrom
        }
        // TODO KRNA value > max
    }

    public void changeDateTo(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent dateFrom = (FieldComponent) state;
        FieldComponent dateTo = (FieldComponent) viewDefinitionState.getComponentByReference("dateTo");
        if (!StringUtils.hasText((String) dateTo.getFieldValue()) && StringUtils.hasText((String) dateFrom.getFieldValue())) {
            // TODO KRNA update dateTo
        }
        // TODO KRNA value > max
    }

    public void changeRealizationTime(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent plannedQuantity = (FieldComponent) viewDefinitionState.getComponentByReference("plannedQuantity");
        FieldComponent technology = (FieldComponent) viewDefinitionState.getComponentByReference("technology");
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");

        if (technology.getFieldValue() != null && StringUtils.hasText((String) plannedQuantity.getFieldValue())) {
            realizationTime.setFieldValue(BigDecimal.valueOf(estimateRealizationTime((Long) technology.getFieldValue())));
            // TODO KRNA value > max
        } else {
            realizationTime.setFieldValue(BigDecimal.ZERO);
        }
        // TODO KRNA what with product lookup ?
    }

    private int estimateRealizationTime(final Long technologyId) {
        Entity entity = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);
        return 1;
    }

}
