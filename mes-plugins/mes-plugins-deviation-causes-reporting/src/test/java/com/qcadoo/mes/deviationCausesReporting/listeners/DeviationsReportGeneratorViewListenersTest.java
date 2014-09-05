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

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.base.Optional;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.deviationCausesReporting.constants.DeviationReportGeneratorViewReferences;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

public class DeviationsReportGeneratorViewListenersTest {

    private DeviationsReportGeneratorViewListeners deviationsReportGeneratorViewListeners;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private ComponentState dateFromComponent, dateToComponent;

    @Mock
    private FormComponent form;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        deviationsReportGeneratorViewListeners = new DeviationsReportGeneratorViewListeners();

        given(view.tryFindComponentByReference(DeviationReportGeneratorViewReferences.DATE_FROM)).willReturn(
                Optional.of(dateFromComponent));
        given(view.tryFindComponentByReference(DeviationReportGeneratorViewReferences.DATE_TO)).willReturn(
                Optional.of(dateToComponent));
    }

    private void stubDateComponentValue(final ComponentState component, final LocalDate date) {
        String dateString = null;
        if (date != null) {
            dateString = date.toString(DateUtils.L_DATE_FORMAT);
        }
        given(component.getFieldValue()).willReturn(dateString);
    }

    @Test
    public final void shouldNotPassValidation1() {
        performValidationFailureCheck(null, null);
    }

    @Test
    public final void shouldNotPassValidation2() {
        performValidationFailureCheck(null, LocalDate.now());
    }

    @Test
    public final void shouldNotPassValidation3() {
        performValidationFailureCheck(LocalDate.now().plusDays(1), LocalDate.now());
    }

    @Test
    public final void shouldPassValidation1() {
        performValidationPassCheck(LocalDate.now(), null);
    }

    @Test
    public final void shouldPassValidation2() {
        performValidationPassCheck(LocalDate.now(), LocalDate.now());
    }

    @Test
    public final void shouldPassValidation3() {
        performValidationPassCheck(LocalDate.now(), LocalDate.now().plusDays(1));
    }

    private void performValidationFailureCheck(final LocalDate dateFrom, final LocalDate dateTo) {
        // given
        tryRunReportGeneration(dateFrom, dateTo);

        // then
        verify(view, never()).redirectTo(anyString(), anyBoolean(), anyBoolean());
        verify(view, never()).redirectTo(anyString(), anyBoolean(), anyBoolean(), any(Map.class));
        verify(dateFromComponent).addMessage(anyString(), eq(ComponentState.MessageType.FAILURE));
        verify(dateToComponent, never()).addMessage(anyString(), eq(ComponentState.MessageType.FAILURE));
    }

    private void performValidationPassCheck(final LocalDate dateFrom, final LocalDate dateTo) {
        // given
        tryRunReportGeneration(dateFrom, dateTo);

        // then
        verify(view).redirectTo(anyString(), anyBoolean(), anyBoolean());
        verify(dateFromComponent, never()).addMessage(anyString(), eq(ComponentState.MessageType.FAILURE));
        verify(dateToComponent, never()).addMessage(anyString(), eq(ComponentState.MessageType.FAILURE));
    }

    private void tryRunReportGeneration(final LocalDate dateFrom, final LocalDate dateTo) {
        stubDateComponentValue(dateFromComponent, dateFrom);
        stubDateComponentValue(dateToComponent, dateTo);

        // when
        deviationsReportGeneratorViewListeners.generateDeviationsReport(view, form, new String[] {});
    }

}
