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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.commons.functional.FluentOptional;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressType;
import com.qcadoo.mes.productionPerShift.constants.TechnologyOperationComponentFieldsPPS;
import com.qcadoo.mes.productionPerShift.dataProvider.ProgressForDayDataProvider;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class ProgressPerShiftViewSaver {

    private static final Predicate<Entity> IS_VALID = new Predicate<Entity>() {

        @Override
        public boolean apply(final Entity progressForDay) {
            return progressForDay.isValid();
        }
    };

    private static final Function<Entity, Entity> SAVE = new Function<Entity, Entity>() {

        @Override
        public Entity apply(final Entity input) {
            return input.getDataDefinition().save(input);
        }
    };

    private static final Function<LookupComponent, Optional<Entity>> GET_LOOKUP_ENTITY = new Function<LookupComponent, Optional<Entity>>() {

        @Override
        public Optional<Entity> apply(final LookupComponent lookup) {
            return Optional.fromNullable(lookup.getEntity());
        }
    };

    private static final String FORM_COMPONENT_REF = "form";

    private static final String PROGRESS_ADL_REF = "progressForDays";

    private static final String OPERATION_LOOKUP_REF = "productionPerShiftOperation";

    private static final String PROGRESS_TYPE_COMBO_REF = "plannedProgressType";

    private static final String CORRECTION_CAUSE_TYPES_ADL_REF = "plannedProgressCorrectionTypes";

    @Autowired
    private ProgressForDayDataProvider progressForDayDataProvider;

    public boolean save(final ViewDefinitionState view) {
        Optional<Entity> maybeToc = getEntityFromLookup(view, OPERATION_LOOKUP_REF);
        if (maybeToc.isPresent()) {
            return saveProgressesAndForm(view, maybeToc.get());
        } else {
            return saveForm(view);
        }
    }

    @Transactional
    private boolean saveProgressesAndForm(final ViewDefinitionState view, final Entity technologyOperationComponent) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(PROGRESS_ADL_REF);
        List<Entity> progressForDays = progressForDaysADL.getEntities();
        ProgressType progressType = extractProgressType(view);
        boolean hasCorrections = progressType == ProgressType.CORRECTED;
        List<Entity> savedProgresses = validateAndSaveProgresses(progressForDays, technologyOperationComponent.getId(),
                hasCorrections);
        if (Iterables.all(savedProgresses, IS_VALID)) {
            return tryUpdateTechnologyOperation(view, technologyOperationComponent, hasCorrections, savedProgresses);
        } else {
            progressForDaysADL.setFieldValue(savedProgresses);
            showValidationErrors(view,
                    FluentIterable.from(savedProgresses).transformAndConcat(new Function<Entity, Iterable<ErrorMessage>>() {

                        @Override
                        public Iterable<ErrorMessage> apply(final Entity input) {
                            return input.getGlobalErrors();
                        }
                    }));
            rollbackCurrentTransaction();
            return false;
        }
    }

    private boolean tryUpdateTechnologyOperation(final ViewDefinitionState view, final Entity technologyOperationComponent,
            final boolean hasCorrections, final List<Entity> savedProgresses) {
        Either<List<ErrorMessage>, Entity> tocSetupResults = setupTechnologyOperation(technologyOperationComponent,
                savedProgresses, hasCorrections);
        if (tocSetupResults.isRight()) {
            return saveForm(view);
        } else {
            showValidationErrors(view, tocSetupResults.getLeft());
            rollbackCurrentTransaction();
            return false;
        }
    }

    private void rollbackCurrentTransaction() {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    private Either<List<ErrorMessage>, Entity> setupTechnologyOperation(final Entity toc, final List<Entity> savedProgresses,
            final boolean hasCorrections) {
        List<Entity> otherTypeProgresses = findProgressesMatching(toc, !hasCorrections);
        List<Entity> tocProgresses = Lists.newLinkedList(Iterables.concat(otherTypeProgresses, savedProgresses));
        toc.setField(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS, tocProgresses);
        toc.setField(TechnologyOperationComponentFieldsPPS.HAS_CORRECTIONS, hasCorrections);
        if (saved(toc).isValid()) {
            return Either.right(toc);
        }
        return Either.left(toc.getGlobalErrors());
    }

    private List<Entity> findProgressesMatching(final Entity technologyOperationComponent, final boolean hasCorrections) {
        return progressForDayDataProvider.findForOperation(technologyOperationComponent, hasCorrections);
    }

    private ProgressType extractProgressType(final ViewDefinitionState view) {
        String progressTypeStringValue = ObjectUtils.toString(view.getComponentByReference(PROGRESS_TYPE_COMBO_REF)
                .getFieldValue());
        return ProgressType.parseString(progressTypeStringValue);
    }

    private Optional<Entity> getEntityFromLookup(final ViewDefinitionState view, final String referenceName) {
        Optional<LookupComponent> maybeLookupComponent = view.tryFindComponentByReference(referenceName);
        return FluentOptional.wrap(maybeLookupComponent).flatMap(GET_LOOKUP_ENTITY).toOpt();
    }

    private boolean saveForm(final ViewDefinitionState view) {
        FormComponent form = getFormComponent(view);
        Entity ppsEntity = form.getPersistedEntityWithIncludedFormValues();
        if (trySaveCorrectionCauses(view, ppsEntity)) {
            return trySavePps(view, ppsEntity);
        }
        return false;
    }

    private boolean trySavePps(final ViewDefinitionState view, final Entity ppsEntity) {
        Entity savedPps = saved(ppsEntity);
        if (savedPps.isValid()) {
            showSaveSuccessMessage(view);
            return true;
        } else {
            showValidationErrors(view, savedPps.getGlobalErrors());
            return false;
        }
    }

    private void showSaveSuccessMessage(final ViewDefinitionState view) {
        FormComponent form = getFormComponent(view);
        form.addMessage("qcadooView.message.saveMessage", ComponentState.MessageType.SUCCESS);
    }

    private boolean trySaveCorrectionCauses(final ViewDefinitionState view, final Entity ppsEntity) {
        List<Entity> savedCorrectionCauses = FluentIterable
                .from(ppsEntity.getHasManyField(ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_TYPES)).transform(SAVE)
                .toList();
        ppsEntity.setField(ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_TYPES, savedCorrectionCauses);
        if (Iterables.all(savedCorrectionCauses, IS_VALID)) {
            return true;
        }
        setCorrectionCauses(view, savedCorrectionCauses);
        showValidationErrors(view, Collections.<ErrorMessage> emptyList());
        return false;
    }

    private void setCorrectionCauses(final ViewDefinitionState view, final List<Entity> savedCorrectionCauses) {
        for (AwesomeDynamicListComponent correctionCausesAdl : view.<AwesomeDynamicListComponent> tryFindComponentByReference(
                CORRECTION_CAUSE_TYPES_ADL_REF).asSet()) {
            correctionCausesAdl.setFieldValue(savedCorrectionCauses);
        }
    }

    private Entity saved(final Entity entity) {
        return SAVE.apply(entity);
    }

    private void showValidationErrors(final ViewDefinitionState view, final Iterable<ErrorMessage> errorMessages) {
        FormComponent form = getFormComponent(view);
        form.addMessage("qcadooView.message.saveFailedMessage", ComponentState.MessageType.FAILURE);
        for (ErrorMessage errorMessage : errorMessages) {
            form.addMessage(errorMessage.getMessage(), ComponentState.MessageType.FAILURE, errorMessage.getVars());
        }
    }

    private FormComponent getFormComponent(final ViewDefinitionState view) {
        return (FormComponent) view.getComponentByReference(FORM_COMPONENT_REF);
    }

    private List<Entity> validateAndSaveProgresses(final List<Entity> progressForDays, final Long technologyOperationId,
            final boolean hasCorrections) {
        List<Entity> progressWithValidatedDailyProgresses = validateDailyProgresses(progressForDays, technologyOperationId,
                hasCorrections);
        if (Iterables.all(progressWithValidatedDailyProgresses, IS_VALID)) {
            return FluentIterable.from(progressWithValidatedDailyProgresses).transform(SAVE).toList();
        }
        return progressWithValidatedDailyProgresses;
    }

    private List<Entity> validateDailyProgresses(final List<Entity> progressForDays, final Long technologyOperationId,
            final boolean hasCorrections) {
        return FluentIterable.from(progressForDays).transform(new Function<Entity, Entity>() {

            @Override
            public Entity apply(final Entity progressForDay) {
                return validateDailyProgressesFor(progressForDay, technologyOperationId, hasCorrections);
            }
        }).toList();
    }

    private Entity validateDailyProgressesFor(final Entity progressForDay, final Long technologyOperationId,
            final boolean hasCorrections) {
        Either<? extends List<Entity>, Void> validationResults = validateDailyProgressesFor(progressForDay);
        if (validationResults.isLeft()) {
            progressForDay.setField(ProgressForDayFields.DAILY_PROGRESS, validationResults.getLeft());
            progressForDay.setNotValid();
            return progressForDay;
        } else {
            progressForDay.setField(ProgressForDayFields.CORRECTED, hasCorrections);
            progressForDay.setField(ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationId);
            return progressForDay;
        }
    }

    private Either<? extends List<Entity>, Void> validateDailyProgressesFor(final Entity progressForDay) {
        List<Entity> savedDailyProgresses = FluentIterable
                .from(progressForDay.getHasManyField(ProgressForDayFields.DAILY_PROGRESS))
                .transform(new Function<Entity, Entity>() {

                    @Override
                    public Entity apply(final Entity dailyProgress) {
                        return tryValidateDailyProgress(dailyProgress).fold(Functions.<Entity> identity(),
                                Functions.constant(dailyProgress));
                    }
                }).toList();
        if (Iterables.all(savedDailyProgresses, IS_VALID)) {
            return Either.right(null);
        }
        return Either.left(savedDailyProgresses);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Either<Entity, Void> tryValidateDailyProgress(final Entity dailyProgress) {
        Entity savedDailyProgress = saved(dailyProgress);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        // reset id to avoid 'entity cannot be found' exceptions.
        savedDailyProgress.setId(dailyProgress.getId());
        if (savedDailyProgress.isValid()) {
            return Either.right(null);
        }
        return Either.left(savedDailyProgress);
    }

}
