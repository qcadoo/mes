package com.qcadoo.mes.cmmsMachineParts.hooks;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.UserFields;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.StaffWorkTimeFields;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventStateChangeFields;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventStateStringValues;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.constants.QcadooSecurityConstants;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service public class StaffWorkTimeDetailsHooks {

    @Autowired private SecurityService securityService;

    @Autowired private DataDefinitionService dataDefinitionService;

    public void onBeforeRender(final ViewDefinitionState view) {
        fillDefaultValues(view);
    }

    private void fillDefaultValues(final ViewDefinitionState view) {
        Long loggedUser = securityService.getCurrentUserId();
        LookupComponent lookupComponent = (LookupComponent) view.getComponentByReference(StaffWorkTimeFields.WORKER);
        if (lookupComponent.getFieldValue() == null) {
            Entity user = dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER)
                    .get(loggedUser);
            if (user.getBelongsToField(UserFields.STAFF) != null) {
                lookupComponent.setFieldValue(user.getBelongsToField(UserFields.STAFF).getId());
                lookupComponent.requestComponentUpdateState();
            }

        }
        FormComponent form = (FormComponent) view.getComponentByReference("form");

        Entity event = form.getPersistedEntityWithIncludedFormValues().getBelongsToField(StaffWorkTimeFields.MAINTENANCE_EVNET);

        Optional<Date> from = findDate(event, MaintenanceEventStateStringValues.IN_PROGRESS);
        Optional<Date> to = findDate(event, MaintenanceEventStateStringValues.EDITED);

        FieldComponent dateFrom = (FieldComponent) view
                .getComponentByReference(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_START);
        FieldComponent dateTo = (FieldComponent) view.getComponentByReference(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_END);

        if (from.isPresent() && dateFrom.getFieldValue() == null) {
            dateFrom.setFieldValue(DateUtils.toDateTimeString(from.get()));
            dateFrom.requestComponentUpdateState();
        }

        if (to.isPresent() && dateTo.getFieldValue() == null) {
            dateTo.setFieldValue(DateUtils.toDateTimeString(to.get()));
            dateTo.requestComponentUpdateState();
            FieldComponent laborTimeFieldComponent = (FieldComponent) view
                    .getComponentByReference(StaffWorkTimeFields.LABOR_TIME);
            calculateLaborTime(laborTimeFieldComponent, from, to);
        }

    }

    private void calculateLaborTime(FieldComponent laborTimeFieldComponent, Optional<Date> from, Optional<Date> to) {

        if (from.isPresent() && to.isPresent() && from.get().before(to.get())) {
            Seconds seconds = Seconds.secondsBetween(new DateTime(from.get()), new DateTime(to.get()));
            laborTimeFieldComponent.setFieldValue(Integer.valueOf(seconds.getSeconds()));
            laborTimeFieldComponent.requestComponentUpdateState();
        }
    }

    private Optional<Date> findDate(final Entity event, String state) {

        SearchCriteriaBuilder sb = dataDefinitionService
                .get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT_STATE_CHANGE)
                .find();

        sb.add(SearchRestrictions
                .belongsTo(MaintenanceEventStateChangeFields.MAINTENANCE_EVENT, CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                        CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT, event.getId()));
        sb.add(SearchRestrictions.eq(MaintenanceEventStateChangeFields.TARGET_STATE, state));
        sb.add(SearchRestrictions.eq(MaintenanceEventStateChangeFields.STATUS, "03successful"));
        sb.addOrder(SearchOrders.asc(MaintenanceEventStateChangeFields.DATE_AND_TIME));
        sb.setMaxResults(1);
        Entity result = sb.uniqueResult();
        if (result == null) {
            return Optional.ofNullable(null);
        }

        return Optional.ofNullable(result.getDateField(MaintenanceEventStateChangeFields.DATE_AND_TIME));
    }

}