package com.qcadoo.mes.orders.deviations.hooks;

import java.util.Date;

import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.orders.deviations.constants.DeviationReportGeneratorViewReferences;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class DeviationReportGeneratorViewHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        markDateFromAsRequired(view);
        if (!isViewAlreadyInitialized(view)) {
            setDefaultDateRange(view);
            markViewAsAlreadyInitialized(view);
        }
    }

    private void setDefaultDateRange(final ViewDefinitionState view) {
        for (ComponentState dateFromComponent : view
                .tryFindComponentByReference(DeviationReportGeneratorViewReferences.DATE_FROM).asSet()) {
            Date firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1).toDate();
            dateFromComponent.setFieldValue(DateUtils.toDateString(firstDayOfCurrentMonth));
        }
        Optional<ComponentState> maybeDateFromComponent = view
                .tryFindComponentByReference(DeviationReportGeneratorViewReferences.DATE_TO);
        for (ComponentState dateFromComponent : maybeDateFromComponent.asSet()) {
            Date today = LocalDate.now().toDate();
            dateFromComponent.setFieldValue(DateUtils.toDateString(today));
        }
    }

    private void markDateFromAsRequired(final ViewDefinitionState view) {
        Optional<FieldComponent> maybeDateFromComponent = view
                .tryFindComponentByReference(DeviationReportGeneratorViewReferences.DATE_FROM);
        for (FieldComponent dateFromComponent : maybeDateFromComponent.asSet()) {
            dateFromComponent.setRequired(true);
        }
    }

    private boolean isViewAlreadyInitialized(final ViewDefinitionState view) {
        Optional<CheckBoxComponent> maybeCheckBox = view
                .tryFindComponentByReference(DeviationReportGeneratorViewReferences.IS_VIEW_INITIALIZED);
        return maybeCheckBox.transform(new Function<CheckBoxComponent, Boolean>() {

            @Override
            public Boolean apply(final CheckBoxComponent component) {
                return component.isChecked();
            }
        }).or(false);
    }

    private void markViewAsAlreadyInitialized(final ViewDefinitionState view) {
        Optional<CheckBoxComponent> maybeCheckBox = view
                .tryFindComponentByReference(DeviationReportGeneratorViewReferences.IS_VIEW_INITIALIZED);
        for (CheckBoxComponent isViewInitializedCheckbox : maybeCheckBox.asSet()) {
            isViewInitializedCheckbox.setChecked(true);
        }
    }

}
