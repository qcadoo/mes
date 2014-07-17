package com.qcadoo.mes.orders.deviations.liteners;

import org.apache.commons.lang.ObjectUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.commons.functional.FluentOptional;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.orders.deviations.DeviationsReportCriteria;
import com.qcadoo.mes.orders.deviations.constants.DeviationReportGeneratorViewReferences;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class DeviationsReportGeneratorViewListeners {

    public void generateDeviationsReport(final ViewDefinitionState view, final ComponentState form, final String[] eventArgs) {
        if (datesAreValid(view)) {
            redirectToPdf(view);
        }
    }

    private void redirectToPdf(final ViewDefinitionState view) {
        Optional<String> dateFrom = getComponentValue(view, DeviationReportGeneratorViewReferences.DATE_FROM);
        Optional<String> dateTo = getComponentValue(view, DeviationReportGeneratorViewReferences.DATE_TO);
        String url = String.format("/orders/deviations.pdf?dateFrom=%s&dateTo=%s", dateFrom.get(), dateTo.orNull());
        view.redirectTo(url, true, false);
    }

    private boolean datesAreValid(final ViewDefinitionState view) {
        for (ComponentState dateFromComponent : view
                .tryFindComponentByReference(DeviationReportGeneratorViewReferences.DATE_FROM).asSet()) {
            if (dateFromIsInvalid(view, dateFromComponent.getFieldValue())) {
                dateFromComponent.addMessage(
                        "orders.deviationsReportGenerator.window.mainTab.form.dateFrom.error.missingOrWrongValue",
                        ComponentState.MessageType.FAILURE);
                return false;
            }
        }
        return true;
    }

    private boolean dateFromIsInvalid(final ViewDefinitionState view, final Object dateFromComponentValue) {
        Either<? extends Exception, Optional<DateTime>> parsedValue = DateUtils.tryParse(dateFromComponentValue);
        return parsedValue.isLeft() || !parsedValue.getRight().isPresent()
                || parsedValue.getRight().get().isAfter(extractDateTo(view));
    }

    private DateTime extractDateTo(final ViewDefinitionState view) {

        return FluentOptional.wrap(getComponentValue(view, DeviationReportGeneratorViewReferences.DATE_TO))
                .flatMap(new Function<String, Optional<DateTime>>() {

                    @Override
                    public Optional<DateTime> apply(final String dateFromComponentValue) {
                        return DateUtils.tryParse(dateFromComponentValue).fold(Functions.constant(Optional.<DateTime> absent()),
                                Functions.<Optional<DateTime>> identity());
                    }
                }).or(DeviationsReportCriteria.getDefaultDateTo());
    }

    private Optional<String> getComponentValue(final ViewDefinitionState view, final String referenceName) {
        return FluentOptional.wrap(view.tryFindComponentByReference(referenceName))
                .flatMap(new Function<ComponentState, Optional<String>>() {

                    @Override
                    public Optional<String> apply(final ComponentState component) {
                        return Optional.fromNullable(ObjectUtils.toString(component.getFieldValue()));
                    }
                }).toOpt();
    }
}
