/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.deviationCausesReporting.listeners;

import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.commons.functional.FluentOptional;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.deviationCausesReporting.DeviationsReportCriteria;
import com.qcadoo.mes.deviationCausesReporting.constants.DeviationReportGeneratorViewReferences;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class DeviationsReportGeneratorViewListeners {

    private static final String MISSING_OR_WRONG_DATE_TO_VALUE = "deviationCausesReporting.deviationsReportGenerator.window"
            + ".mainTab.form.dateFrom.error.missingOrWrongValue";

    public void generateDeviationsReport(final ViewDefinitionState view, final ComponentState form, final String[] eventArgs) {
        if (datesAreValid(view)) {
            redirectToPdf(view);
        }
    }

    private void redirectToPdf(final ViewDefinitionState view) {
        Optional<String> dateFrom = getComponentValue(view, DeviationReportGeneratorViewReferences.DATE_FROM);
        Optional<String> dateTo = getComponentValue(view, DeviationReportGeneratorViewReferences.DATE_TO);
        String url = String.format("/deviationCausesReporting/deviations.pdf?dateFrom=%s&dateTo=%s", dateFrom.get(),
                dateTo.orNull());
        view.redirectTo(url, true, false);
    }

    private boolean datesAreValid(final ViewDefinitionState view) {
        for (ComponentState dateFromComponent : view
                .tryFindComponentByReference(DeviationReportGeneratorViewReferences.DATE_FROM).asSet()) {
            if (dateFromIsInvalid(view, dateFromComponent.getFieldValue())) {
                dateFromComponent.addMessage(MISSING_OR_WRONG_DATE_TO_VALUE, ComponentState.MessageType.FAILURE);
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
