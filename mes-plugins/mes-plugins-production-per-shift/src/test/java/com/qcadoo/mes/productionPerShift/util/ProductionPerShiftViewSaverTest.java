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
package com.qcadoo.mes.productionPerShift.util;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubHasManyField;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.aspectj.AnnotationTransactionAspect;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.qcadoo.mes.productionPerShift.PpsDetailsViewAwareTest;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressType;
import com.qcadoo.mes.productionPerShift.constants.TechnologyOperationComponentFieldsPPS;
import com.qcadoo.mes.productionPerShift.dataProvider.ProgressForDayDataProvider;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

public class ProductionPerShiftViewSaverTest extends PpsDetailsViewAwareTest {

    private ProgressPerShiftViewSaver progressPerShiftViewSaver;

    @Mock
    private ProgressForDayDataProvider progressForDayDataProvider;

    @Mock
    private DataDefinition dataDefinition;

    private Entity ppsEntity;

    @Mock
    private AwesomeDynamicListComponent progressForDaysAdl;

    private FormComponent form;

    @Mock
    private TransactionStatus txStatus;

    @Before
    public void init() {
        super.init();

        progressPerShiftViewSaver = new ProgressPerShiftViewSaver();
        ReflectionTestUtils.setField(progressPerShiftViewSaver, "progressForDayDataProvider", progressForDayDataProvider);

        given(dataDefinition.save(any(Entity.class))).willAnswer(new Answer<Entity>() {

            @Override
            public Entity answer(final InvocationOnMock invocation) throws Throwable {
                return (Entity) invocation.getArguments()[0];
            }
        });

        ppsEntity = mockEntity(dataDefinition);
        stubHasManyField(ppsEntity, ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_TYPES, Collections.<Entity> emptyList());

        form = mockForm(ppsEntity);

        stubViewComponent("progressForDays", progressForDaysAdl);
        stubViewComponent("form", form);

        stubTransactionStatus();
    }

    private void stubPfdDataProviderFindForoperation(final List<Entity> pfds) {
        given(progressForDayDataProvider.findForOperation(any(Entity.class), anyBoolean()))
                .willReturn(ImmutableList.copyOf(pfds));
        given(progressForDayDataProvider.findForOperation(any(Entity.class), any(ProgressType.class))).willReturn(
                ImmutableList.copyOf(pfds));
    }

    private void stubTransactionStatus() {
        given(txStatus.isRollbackOnly()).willReturn(false);

        PlatformTransactionManager txManager = mock(PlatformTransactionManager.class);
        given(txManager.getTransaction((TransactionDefinition) Mockito.anyObject())).willReturn(txStatus);

        AnnotationTransactionAspect txAspect = AnnotationTransactionAspect.aspectOf();
        txAspect.setTransactionManager(txManager);
    }

    private void stubProgressForDaysAdlEntities(final Entity... pfdEntities) {
        given(progressForDaysAdl.getEntities()).willReturn(Arrays.asList(pfdEntities));
        given(progressForDaysAdl.getFieldValue()).willReturn(Arrays.asList(pfdEntities));
    }

    private void stubTechnologyOperation(final Entity technologyOperation) {
        LookupComponent technologyOperationLookup = mockLookup(technologyOperation);
        stubViewComponent(OPERATION_LOOKUP_REF, technologyOperationLookup);
    }

    private Entity mockProgressForDayEntity(final Entity... dailyProgresses) {
        Entity pfdEntity = mockEntity(dataDefinition);
        stubHasManyField(pfdEntity, ProgressForDayFields.DAILY_PROGRESS, Arrays.asList(dailyProgresses));
        given(pfdEntity.isValid()).willReturn(true);
        return pfdEntity;
    }

    private Entity mockDailyProgress() {
        Entity dailyProgress = mockEntity(dataDefinition);
        given(dailyProgress.isValid()).willReturn(true);
        return dailyProgress;
    }

    @Test
    public final void shouldSaveOnlyForm() {
        // given
        stubProgressType(ProgressType.PLANNED);
        stubTechnologyOperation(null);
        given(ppsEntity.isValid()).willReturn(true);

        // when
        progressPerShiftViewSaver.save(view);

        // then
        verify(txStatus, never()).setRollbackOnly();

        verify(dataDefinition).save(ppsEntity);
        verify(form).addMessage("qcadooView.message.saveMessage", ComponentState.MessageType.SUCCESS);
    }

    @Test
    public final void shouldNotSaveFormBecauseOfValidationErrors() {
        // given
        stubProgressType(ProgressType.PLANNED);
        stubTechnologyOperation(null);
        Entity invalidPpsEntity = mockEntity(dataDefinition);
        given(invalidPpsEntity.isValid()).willReturn(false);
        given(dataDefinition.save(ppsEntity)).willReturn(invalidPpsEntity);

        // when
        progressPerShiftViewSaver.save(view);

        // then
        verify(txStatus, never()).setRollbackOnly();

        verify(dataDefinition).save(ppsEntity);
        verify(form).addMessage("qcadooView.message.saveFailedMessage", ComponentState.MessageType.FAILURE);
    }

    @Test
    public final void shouldNotSaveFormBecauseOfDeviationCausesValidationErrors() {
        // given
        stubProgressType(ProgressType.PLANNED);
        stubTechnologyOperation(null);

        Entity deviationCauseEntity = mockEntity(dataDefinition);
        Entity invalidDeviationCauseEntity = mockEntity(dataDefinition);
        given(invalidDeviationCauseEntity.isValid()).willReturn(false);
        given(dataDefinition.save(deviationCauseEntity)).willReturn(invalidDeviationCauseEntity);

        AwesomeDynamicListComponent deviationCausesAdl = mock(AwesomeDynamicListComponent.class);
        stubViewComponent(CORRECTION_CAUSE_TYPES_ADL_REF, deviationCausesAdl);
        stubHasManyField(ppsEntity, ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_TYPES,
                Lists.newArrayList(deviationCauseEntity));

        // when
        progressPerShiftViewSaver.save(view);

        // then
        verify(txStatus, never()).setRollbackOnly();

        verify(dataDefinition).save(deviationCauseEntity);
        verify(dataDefinition, never()).save(ppsEntity);
        verify(deviationCausesAdl).setFieldValue(Lists.newArrayList(invalidDeviationCauseEntity));
        verify(form).addMessage("qcadooView.message.saveFailedMessage", ComponentState.MessageType.FAILURE);
    }

    @Test
    public final void shouldSaveFormAndPlannedProgresses() {
        // given
        Long tocId = 1001L;

        Entity pfd1dp1 = mockDailyProgress();
        Entity pfd1dp2 = mockDailyProgress();
        Entity pfd1 = mockProgressForDayEntity(pfd1dp1, pfd1dp2);

        Entity pfd2dp1 = mockDailyProgress();
        Entity pfd2 = mockProgressForDayEntity(pfd2dp1);

        stubProgressForDaysAdlEntities(pfd1, pfd2);

        stubProgressType(ProgressType.PLANNED);

        Entity technologyOperation = mockEntity(tocId, dataDefinition);
        given(technologyOperation.isValid()).willReturn(true);
        stubTechnologyOperation(technologyOperation);

        given(ppsEntity.isValid()).willReturn(true);

        // when
        progressPerShiftViewSaver.save(view);

        // then
        verify(txStatus, times(3)).setRollbackOnly(); // one rollback for each DailyProgress
        verifyTocSetup(technologyOperation, Lists.newArrayList(pfd1, pfd2), false);

        verify(dataDefinition).save(pfd1dp1);
        verify(dataDefinition).save(pfd1dp2);
        verify(dataDefinition).save(pfd1);
        verify(pfd1).setField(ProgressForDayFields.CORRECTED, false);
        verify(pfd1).setField(ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT, tocId);

        verify(dataDefinition).save(pfd2dp1);
        verify(dataDefinition).save(pfd2);
        verify(pfd2).setField(ProgressForDayFields.CORRECTED, false);
        verify(pfd2).setField(ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT, tocId);

        verify(dataDefinition).save(ppsEntity);

        verify(form).addMessage("qcadooView.message.saveMessage", ComponentState.MessageType.SUCCESS);
    }

    @Test
    public final void shouldSaveFormAndCorrectedProgresses() {
        // given
        Long tocId = 1001L;

        Entity pfd1dp1 = mockDailyProgress();
        Entity pfd1dp2 = mockDailyProgress();
        Entity pfd1 = mockProgressForDayEntity(pfd1dp1, pfd1dp2);

        Entity pfd2dp1 = mockDailyProgress();
        Entity pfd2 = mockProgressForDayEntity(pfd2dp1);

        stubProgressForDaysAdlEntities(pfd1, pfd2);

        stubProgressType(ProgressType.CORRECTED);

        Entity technologyOperation = mockEntity(tocId, dataDefinition);
        given(technologyOperation.isValid()).willReturn(true);
        stubTechnologyOperation(technologyOperation);

        given(ppsEntity.isValid()).willReturn(true);

        List<Entity> plannedPfds = Lists.newArrayList(mockEntity(), mockEntity(), mockEntity());
        stubPfdDataProviderFindForoperation(plannedPfds);

        // when
        progressPerShiftViewSaver.save(view);

        // then
        verify(txStatus, times(3)).setRollbackOnly(); // one rollback for each DailyProgress
        List<Entity> expectedPfds = Lists.newLinkedList(Iterables.concat(plannedPfds, Lists.newArrayList(pfd1, pfd2)));
        verifyTocSetup(technologyOperation, expectedPfds, true);

        verify(dataDefinition).save(pfd1dp1);
        verify(dataDefinition).save(pfd1dp2);
        verify(dataDefinition).save(pfd1);
        verify(pfd1).setField(ProgressForDayFields.CORRECTED, true);
        verify(pfd1).setField(ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT, tocId);

        verify(dataDefinition).save(pfd2dp1);
        verify(dataDefinition).save(pfd2);
        verify(pfd2).setField(ProgressForDayFields.CORRECTED, true);
        verify(pfd2).setField(ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT, tocId);

        verify(dataDefinition).save(ppsEntity);

        verify(form).addMessage("qcadooView.message.saveMessage", ComponentState.MessageType.SUCCESS);
    }

    @Test
    public final void shouldNotSaveFormAndCorrectedProgressesDueToDailyProgressesValidationErrors() {
        // given
        Long tocId = 1001L;

        Entity pfd1dp1 = mockDailyProgress();
        Entity pfd1dp2 = mockDailyProgress();
        Entity pfd1 = mockProgressForDayEntity(pfd1dp1, pfd1dp2);

        Entity pfd2dp1 = mockDailyProgress();
        Entity pfd2 = mockProgressForDayEntity(pfd2dp1);
        given(pfd2.isValid()).willReturn(false);
        Entity invalidPfd2dp1 = mockDailyProgress();
        given(invalidPfd2dp1.isValid()).willReturn(false);
        given(dataDefinition.save(pfd2dp1)).willReturn(invalidPfd2dp1);

        stubProgressForDaysAdlEntities(pfd1, pfd2);

        stubProgressType(ProgressType.PLANNED);

        Entity technologyOperation = mockEntity(tocId, dataDefinition);
        given(technologyOperation.isValid()).willReturn(true);
        stubTechnologyOperation(technologyOperation);

        given(ppsEntity.isValid()).willReturn(true);

        // when
        progressPerShiftViewSaver.save(view);

        // then
        verify(txStatus, times(4)).setRollbackOnly(); // one rollback for each DailyProgress + 1 global rollback
        verifyTocSetupAbsence(technologyOperation);

        verify(dataDefinition).save(pfd1dp1);
        verify(dataDefinition).save(pfd1dp2);
        verify(dataDefinition, never()).save(pfd1);
        verify(pfd1).setField(ProgressForDayFields.CORRECTED, false);
        verify(pfd1).setField(ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT, tocId);

        verify(dataDefinition).save(pfd2dp1);
        verify(pfd2).setNotValid();
        verify(dataDefinition, never()).save(pfd2);
        verify(pfd2, never()).setField(ProgressForDayFields.CORRECTED, false);
        verify(pfd2, never()).setField(ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT, tocId);

        verify(dataDefinition, never()).save(ppsEntity);

        verify(form).addMessage("qcadooView.message.saveFailedMessage", ComponentState.MessageType.FAILURE);
    }

    private void verifyTocSetup(final Entity technologyOperation, final List<Entity> progresses, final boolean hasCorrections) {
        verify(technologyOperation).setField(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS, progresses);
        verify(technologyOperation).setField(TechnologyOperationComponentFieldsPPS.HAS_CORRECTIONS, hasCorrections);
        verify(dataDefinition).save(technologyOperation);
    }

    private void verifyTocSetupAbsence(final Entity technologyOperation) {
        verify(technologyOperation, never()).setField(eq(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS), anyList());
        verify(technologyOperation, never()).setField(eq(TechnologyOperationComponentFieldsPPS.HAS_CORRECTIONS), anyBoolean());
        verify(dataDefinition, never()).save(technologyOperation);
    }

}
