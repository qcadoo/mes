/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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
package com.qcadoo.mes.qualityControls;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.qualityControls.constants.QualityControlsConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public final class QualityControlService {

    private static final String QCADOO_VIEW_VALIDATE_GLOBAL_ERROR_CUSTOM = "qcadooView.validate.global.error.custom";

    private static final String CONTROL_INSTRUCTION_LITERAL = "controlInstruction";

    private static final String DATE_TO_LITERAL = "dateTo";

    private static final String DATE_FROM_LITERAL = "dateFrom";

    private static final String GRID_LITERAL = "grid";

    private static final String FORM_LITERAL = "form";

    private static final String QUALITY_CONTROLS_FOR_OPERATION_LITERAL = "qualityControlsForOperation";

    private static final String QUALITY_CONTROLS_FOR_ORDER_LITERAL = "qualityControlsForOrder";

    private static final String QUALITY_CONTROLS_FOR_BATCH_LITERAL = "qualityControlsForBatch";

    private static final String OPERATION_COMPONENTS_LITERAL = "operationComponents";

    private static final String TYPE_03FOR_ORDER = "03forOrder";

    private static final String QUALITY_CONTROLS_FOR_UNIT_LITERAL = "qualityControlsForUnit";

    private static final String PLANNED_QUANTITY_LITERAL = "plannedQuantity";

    private static final String DONE_QUANTITY_LITERAL = "doneQuantity";

    private static final String UNIT_SAMPLING_NR_LITERAL = "unitSamplingNr";

    private static final String TYPE_02FOR_UNIT = "02forUnit";

    private static final String TYPE_01FOR_BATCH = "01forBatch";

    private static final String NUMBER_LITERAL = "number";

    private static final String ORDER_LITERAL = "order";

    private static final String CONTROLLED_QUANTITY_LITERAL = "controlledQuantity";

    private static final String OPERATION_LITERAL = "operation";

    private static final String REJECTED_QUANTITY_LITERAL = "rejectedQuantity";

    private static final String TAKEN_FOR_CONTROL_QUANTITY_LITERAL = "takenForControlQuantity";

    private static final String DATE_LITERAL = "date";

    private static final String STAFF_LITERAL = "staff";

    private static final String CLOSED_LITERAL = "closed";

    private static final String ACCEPTED_DEFECTS_QUANTITY_LITERAL = "acceptedDefectsQuantity";

    private static final String TYPE_04FOR_OPERATION = "04forOperation";

    private static final Integer DIGITS_NUMBER = 6;

    private static final String FIELD_COMMENT = "comment";

    private static final String FIELD_CONTROL_RESULT = "controlResult";

    private static final String FIELD_QUALITY_CONTROL_REQUIRED = "qualityControlRequired";

    private static final String TECHNOLOGY_LITERAL = "technology";

    private static final String QUALITY_CONTROL_TYPE_LITERAL = "qualityControlType";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private QualityControlForNumberService qualityControlForNumber;

    public void checkIfCommentIsRequiredBasedOnResult(final ViewDefinitionState state) {
        FieldComponent comment = (FieldComponent) state.getComponentByReference(FIELD_COMMENT);

        FieldComponent controlResult = (FieldComponent) state.getComponentByReference(FIELD_CONTROL_RESULT);

        if (controlResult != null && controlResult.getFieldValue() != null && "03objection".equals(controlResult.getFieldValue())) {
            comment.setRequired(true);
            comment.requestComponentUpdateState();
        } else {
            comment.setRequired(false);
        }

    }

    public void setQualityControlTypeForTechnology(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getField(FIELD_QUALITY_CONTROL_REQUIRED) != null && (Boolean) entity.getField(FIELD_QUALITY_CONTROL_REQUIRED)) {
            Entity technology = entity.getBelongsToField(TECHNOLOGY_LITERAL);
            DataDefinition technologyInDef = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY);
            Entity technologyEntity = technologyInDef.get(technology.getId());
            if (technologyEntity.getField(QUALITY_CONTROL_TYPE_LITERAL) == null
                    || !technologyEntity.getField(QUALITY_CONTROL_TYPE_LITERAL).equals(TYPE_04FOR_OPERATION)) {
                technologyEntity.setField(QUALITY_CONTROL_TYPE_LITERAL, TYPE_04FOR_OPERATION);
                technologyInDef.save(technologyEntity);
            }
        }
    }

    public void checkIfCommentIsRequiredBasedOnDefects(final ViewDefinitionState state) {
        FieldComponent comment = (FieldComponent) state.getComponentByReference(FIELD_COMMENT);

        FieldComponent acceptedDefectsQuantity = (FieldComponent) state
                .getComponentByReference(ACCEPTED_DEFECTS_QUANTITY_LITERAL);

        if (acceptedDefectsQuantity.getFieldValue() != null
                && !acceptedDefectsQuantity.getFieldValue().toString().isEmpty()
                && (new BigDecimal(acceptedDefectsQuantity.getFieldValue().toString().replace(",", "."))
                        .compareTo(BigDecimal.ZERO) > 0)) {
            comment.setRequired(true);
            comment.requestComponentUpdateState();
        } else {
            comment.setRequired(false);
        }

    }

    public boolean checkIfCommentForResultOrQuantityIsReq(final DataDefinition dataDefinition, final Entity entity) {
        String qualityControlType = (String) entity.getField(QUALITY_CONTROL_TYPE_LITERAL);

        if (hasControlResult(qualityControlType)) {
            return checkIfCommentForResultIsReq(dataDefinition, entity);
        } else {
            return checkIfCommentForQuantityIsReq(dataDefinition, entity);
        }

    }

    public boolean checkIfCommentForResultIsReq(final DataDefinition dataDefinition, final Entity entity) {
        String resultType = (String) entity.getField(FIELD_CONTROL_RESULT);

        if (resultType != null && "03objection".equals(resultType)) {

            String comment = (String) entity.getField(FIELD_COMMENT);
            if (comment == null || comment.isEmpty()) {
                entity.addGlobalError(QCADOO_VIEW_VALIDATE_GLOBAL_ERROR_CUSTOM);
                entity.addError(dataDefinition.getField(FIELD_COMMENT),
                        "qualityControls.quality.control.validate.global.error.comment");
                return false;
            }
        }
        return true;

    }

    public boolean checkIfCommentForQuantityIsReq(final DataDefinition dataDefinition, final Entity entity) {
        BigDecimal acceptedDefectsQuantity = (BigDecimal) entity.getField(ACCEPTED_DEFECTS_QUANTITY_LITERAL);

        if (acceptedDefectsQuantity != null) {
            String comment = (String) entity.getField(FIELD_COMMENT);

            if ((comment == null || comment.isEmpty()) && acceptedDefectsQuantity.compareTo(BigDecimal.ZERO) > 0) {
                entity.addGlobalError(QCADOO_VIEW_VALIDATE_GLOBAL_ERROR_CUSTOM);
                entity.addError(dataDefinition.getField(FIELD_COMMENT),
                        "qualityControls.quality.control.validate.global.error.comment");
                return false;
            }
        }
        return true;

    }

    public void checkQualityControlResult(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            throw new IllegalStateException("component is not select");
        }

        FieldComponent resultType = (FieldComponent) state;
        FieldComponent comment = (FieldComponent) viewDefinitionState.getComponentByReference(FIELD_COMMENT);

        if (resultType.getFieldValue() != null && "03objection".equals(resultType.getFieldValue())) {
            comment.setRequired(true);
        } else {
            comment.setRequired(false);
        }
    }

    public void closeQualityControl(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (state.getFieldValue() != null) {
            if (state instanceof FormComponent) {
                FieldComponent controlResult = (FieldComponent) viewDefinitionState.getComponentByReference(FIELD_CONTROL_RESULT);

                String qualityControlType = ((FieldComponent) viewDefinitionState
                        .getComponentByReference(QUALITY_CONTROL_TYPE_LITERAL)).getFieldValue().toString();

                if (hasControlResult(qualityControlType) && controlResult != null
                        && (controlResult.getFieldValue() == null || ((String) controlResult.getFieldValue()).isEmpty())) {
                    controlResult.addMessage(
                            translationService.translate("qualityControls.quality.control.result.missing", state.getLocale()),
                            MessageType.FAILURE);
                    state.addMessage(
                            translationService.translate("qualityControls.quality.control.result.missing", state.getLocale()),
                            MessageType.FAILURE);
                    return;
                } else if (!hasControlResult(qualityControlType)
                        || (controlResult != null && ((controlResult.getFieldValue() != null) || !((String) controlResult
                                .getFieldValue()).isEmpty()))) {

                    FieldComponent closed = (FieldComponent) viewDefinitionState.getComponentByReference(CLOSED_LITERAL);
                    FieldComponent staff = (FieldComponent) viewDefinitionState.getComponentByReference(STAFF_LITERAL);
                    FieldComponent date = (FieldComponent) viewDefinitionState.getComponentByReference(DATE_LITERAL);

                    staff.setFieldValue(securityService.getCurrentUserName());
                    date.setFieldValue(new SimpleDateFormat(DateUtils.DATE_FORMAT).format(new Date()));

                    closed.setFieldValue(true);

                    ((FormComponent) state).performEvent(viewDefinitionState, "save", new String[0]);
                }

            } else if (state instanceof GridComponent) {
                DataDefinition qualityControlDD = dataDefinitionService.get(QualityControlsConstants.PLUGIN_IDENTIFIER,
                        QualityControlsConstants.MODEL_QUALITY_CONTROL);
                Entity qualityControl = qualityControlDD.get((Long) state.getFieldValue());

                FieldDefinition controlResultField = qualityControlDD.getField(FIELD_CONTROL_RESULT);

                Object controlResult = qualityControl.getField(FIELD_CONTROL_RESULT);
                String qualityControlType = (String) qualityControl.getField(QUALITY_CONTROL_TYPE_LITERAL);

                if (hasControlResult(qualityControlType) && controlResultField != null
                        && (controlResult == null || controlResult.toString().isEmpty())) {
                    state.addMessage(
                            translationService.translate("qualityControls.quality.control.result.missing", state.getLocale()),
                            MessageType.FAILURE);
                    return;
                } else if (!hasControlResult(qualityControlType)
                        || (controlResultField == null || (controlResult != null && !controlResult.toString().isEmpty()))) {

                    qualityControl.setField(STAFF_LITERAL, securityService.getCurrentUserName());
                    qualityControl.setField(DATE_LITERAL, new Date());
                    qualityControl.setField(CLOSED_LITERAL, true);
                    qualityControlDD.save(qualityControl);

                    state.performEvent(viewDefinitionState, "refresh", new String[0]);
                }
            }
            state.addMessage(translationService.translate("qualityControls.quality.control.closed.success", state.getLocale()),
                    MessageType.SUCCESS);
        } else {
            if (state instanceof FormComponent) {
                state.addMessage(translationService.translate("qcadooView.form.entityWithoutIdentifier", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                state.addMessage(translationService.translate("qcadooView.grid.noRowSelectedError", state.getLocale()),
                        MessageType.FAILURE);
            }
        }
    }

    public void autoGenerateQualityControl(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        boolean inProgressState = Boolean.parseBoolean(args[0]);
        if (inProgressState && isQualityControlAutoGenEnabled()) {
            generateQualityControl(viewDefinitionState, state, args);
        }
    }

    public void generateOnSaveQualityControl(final DataDefinition dataDefinition, final Entity entity) {
        Entity order = entity.getBelongsToField(ORDER_LITERAL);
        Entity technology = order.getBelongsToField(TECHNOLOGY_LITERAL);
        if (technology == null) {
            return;
        } else {
            Object qualityControl = technology.getField(QUALITY_CONTROL_TYPE_LITERAL);

            if (qualityControl == null) {
                return;
            } else {
                boolean qualityControlType = TYPE_01FOR_BATCH
                        .equals(technology.getField(QUALITY_CONTROL_TYPE_LITERAL).toString());

                if (isQualityControlAutoGenEnabled() || qualityControlType) {
                    createAndSaveControlForSingleBatch(order, entity);
                }
            }
        }
    }

    public void generateQualityControl(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state.getFieldValue() == null) {
            if (state instanceof FormComponent) {
                state.addMessage(translationService.translate("qcadooView.form.entityWithoutIdentifier", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                state.addMessage(translationService.translate("qcadooView.grid.noRowSelectedError", state.getLocale()),
                        MessageType.FAILURE);
            }
        } else {
            DataDefinition orderDataDefinition = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                    OrdersConstants.MODEL_ORDER);
            Entity order = orderDataDefinition.get((Long) state.getFieldValue());

            Entity technology = (Entity) order.getField(TECHNOLOGY_LITERAL);

            if (technology == null) {
                return;
            }

            if (technology.getField(QUALITY_CONTROL_TYPE_LITERAL) == null) {
                state.addMessage(
                        translationService.translate("qualityControls.qualityControls.qualityType.missing", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                String qualityControlType = technology.getField(QUALITY_CONTROL_TYPE_LITERAL).toString();

                if (isQualityControlForOrderExists(order)) {
                    state.addMessage(
                            translationService.translate("qualityControls.qualityControls.generated.failure", state.getLocale()),
                            MessageType.FAILURE);
                } else {
                    generateQualityControlForGivenType(qualityControlType, technology, order);

                    state.addMessage(
                            translationService.translate("qualityControls.qualityControls.generated.success", state.getLocale()),
                            MessageType.SUCCESS);
                }

                state.performEvent(viewDefinitionState, "refresh", new String[0]);
            }
        }

    }

    private boolean isQualityControlForOrderExists(final Entity order) {
        DataDefinition dataDefinition = dataDefinitionService.get(QualityControlsConstants.PLUGIN_IDENTIFIER,
                QualityControlsConstants.MODEL_QUALITY_CONTROL);
        return dataDefinition.find().add(SearchRestrictions.belongsTo(ORDER_LITERAL, order)).list().getTotalNumberOfEntities() != 0;
    }

    public void checkAcceptedDefectsQuantity(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            throw new IllegalStateException("component is not input");
        }

        FieldComponent acceptedDefectsQuantity = (FieldComponent) state;

        FieldComponent comment = (FieldComponent) viewDefinitionState.getComponentByReference(FIELD_COMMENT);

        if (acceptedDefectsQuantity.getFieldValue() != null) {
            if (isNumber(acceptedDefectsQuantity.getFieldValue().toString())
                    && (new BigDecimal(acceptedDefectsQuantity.getFieldValue().toString())).compareTo(BigDecimal.ZERO) > 0) {
                comment.setRequired(true);
            } else {
                comment.setRequired(false);
            }
        }
    }

    public void setQualityControlInstruction(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }

        FieldComponent order = (FieldComponent) state;
        FieldComponent controlInstruction = (FieldComponent) viewDefinitionState
                .getComponentByReference(CONTROL_INSTRUCTION_LITERAL);

        if (controlInstruction == null) {
            return;
        } else {
            controlInstruction.setFieldValue("");
        }

        if (order.getFieldValue() != null) {
            String qualityControlInstruction = getInstructionForOrder((Long) order.getFieldValue());
            if (qualityControlInstruction != null) {
                controlInstruction.setFieldValue(qualityControlInstruction);
            }
        }
    }

    public void enableCalendarsOnRender(final ViewDefinitionState state) {
        FieldComponent dateFrom = (FieldComponent) state.getComponentByReference(DATE_FROM_LITERAL);
        FieldComponent dateTo = (FieldComponent) state.getComponentByReference(DATE_TO_LITERAL);

        dateFrom.setEnabled(true);
        dateTo.setEnabled(true);
    }

    public void setQuantitiesToDefaulIfEmpty(final ViewDefinitionState state) {
        FieldComponent takenForControlQuantity = (FieldComponent) state
                .getComponentByReference(TAKEN_FOR_CONTROL_QUANTITY_LITERAL);
        FieldComponent rejectedQuantity = (FieldComponent) state.getComponentByReference(REJECTED_QUANTITY_LITERAL);
        FieldComponent acceptedDefectsQuantity = (FieldComponent) state
                .getComponentByReference(ACCEPTED_DEFECTS_QUANTITY_LITERAL);

        if (takenForControlQuantity.getFieldValue() == null || takenForControlQuantity.getFieldValue().toString().isEmpty()) {
            takenForControlQuantity.setFieldValue(BigDecimal.ONE);
        }

        if (rejectedQuantity.getFieldValue() == null || rejectedQuantity.getFieldValue().toString().isEmpty()) {
            rejectedQuantity.setFieldValue(BigDecimal.ZERO);
        }

        if (acceptedDefectsQuantity.getFieldValue() == null || acceptedDefectsQuantity.getFieldValue().toString().isEmpty()) {
            acceptedDefectsQuantity.setFieldValue(BigDecimal.ZERO);
        }

    }

    public void addRestrictionToQualityControlGrid(final ViewDefinitionState viewDefinitionState) {
        final GridComponent qualityControlsGrid = (GridComponent) viewDefinitionState.getComponentByReference(GRID_LITERAL);
        final String qualityControlType = qualityControlsGrid.getName();

        qualityControlsGrid.setCustomRestriction(new CustomRestriction() {

            @Override
            public void addRestriction(final SearchCriteriaBuilder searchCriteriaBuilder) {
                searchCriteriaBuilder.add(SearchRestrictions.eq(QUALITY_CONTROL_TYPE_LITERAL, qualityControlType));
            }

        });
    }

    public void setQualityControlTypeHiddenField(final ViewDefinitionState viewDefinitionState) {
        FormComponent qualityControlsForm = (FormComponent) viewDefinitionState.getComponentByReference(FORM_LITERAL);
        String qualityControlTypeString = qualityControlsForm.getName().replace("Control", "Controls");
        FieldComponent qualityControlType = (FieldComponent) viewDefinitionState
                .getComponentByReference(QUALITY_CONTROL_TYPE_LITERAL);

        qualityControlType.setFieldValue(qualityControlTypeString);
    }

    public void setOperationAsRequired(final ViewDefinitionState state) {
        FieldComponent operation = (FieldComponent) state.getComponentByReference(OPERATION_LITERAL);
        operation.setRequired(true);
    }

    public boolean checkIfOperationIsRequired(final DataDefinition dataDefinition, final Entity entity) {
        String qualityControlType = (String) entity.getField(QUALITY_CONTROL_TYPE_LITERAL);

        if (QUALITY_CONTROLS_FOR_OPERATION_LITERAL.equals(qualityControlType)) {
            Object operation = entity.getField(OPERATION_LITERAL);

            if (operation == null) {
                entity.addGlobalError(QCADOO_VIEW_VALIDATE_GLOBAL_ERROR_CUSTOM);
                entity.addError(dataDefinition.getField(OPERATION_LITERAL),
                        "qualityControls.quality.control.validate.global.error.operation");
                return false;
            }
        }

        return true;
    }

    public boolean checkIfQuantitiesAreCorrect(final DataDefinition dataDefinition, final Entity entity) {
        String qualityControlType = (String) entity.getField(QUALITY_CONTROL_TYPE_LITERAL);

        if (hasQuantitiesToBeChecked(qualityControlType)) {
            BigDecimal controlledQuantity = (BigDecimal) entity.getField(CONTROLLED_QUANTITY_LITERAL);
            BigDecimal takenForControlQuantity = (BigDecimal) entity.getField(TAKEN_FOR_CONTROL_QUANTITY_LITERAL);
            BigDecimal rejectedQuantity = (BigDecimal) entity.getField(REJECTED_QUANTITY_LITERAL);
            BigDecimal acceptedDefectsQuantity = (BigDecimal) entity.getField(ACCEPTED_DEFECTS_QUANTITY_LITERAL);

            if (controlledQuantity == null) {
                controlledQuantity = BigDecimal.ZERO;
            }
            if (takenForControlQuantity == null) {
                takenForControlQuantity = BigDecimal.ZERO;
            }
            if (rejectedQuantity == null) {
                rejectedQuantity = BigDecimal.ZERO;
            }
            if (acceptedDefectsQuantity == null) {
                acceptedDefectsQuantity = BigDecimal.ZERO;
            }

            if (rejectedQuantity.compareTo(takenForControlQuantity) > 0) {
                entity.addGlobalError(QCADOO_VIEW_VALIDATE_GLOBAL_ERROR_CUSTOM);
                entity.addError(dataDefinition.getField(REJECTED_QUANTITY_LITERAL),
                        "qualityControls.quality.control.validate.global.error.rejectedQuantity.tooLarge");
                return false;
            }

            if (acceptedDefectsQuantity.compareTo(takenForControlQuantity.subtract(rejectedQuantity)) > 0) {
                entity.addGlobalError(QCADOO_VIEW_VALIDATE_GLOBAL_ERROR_CUSTOM);
                entity.addError(dataDefinition.getField(ACCEPTED_DEFECTS_QUANTITY_LITERAL),
                        "qualityControls.quality.control.validate.global.error.acceptedDefectsQuantity.tooLarge");
                return false;
            }

            entity.setField(CONTROLLED_QUANTITY_LITERAL, controlledQuantity);
            entity.setField(TAKEN_FOR_CONTROL_QUANTITY_LITERAL, takenForControlQuantity);
            entity.setField(REJECTED_QUANTITY_LITERAL, rejectedQuantity);
            entity.setField(ACCEPTED_DEFECTS_QUANTITY_LITERAL, acceptedDefectsQuantity);
        }

        return true;
    }

    public void disableFormForClosedControl(final ViewDefinitionState state) {
        FormComponent qualityControl = (FormComponent) state.getComponentByReference(FORM_LITERAL);
        boolean disabled = false;

        if (qualityControl.getEntityId() != null) {
            Entity entity = dataDefinitionService.get(QualityControlsConstants.PLUGIN_IDENTIFIER,
                    QualityControlsConstants.MODEL_QUALITY_CONTROL).get(qualityControl.getEntityId());

            if (entity != null && (Boolean) entity.getField(CLOSED_LITERAL) && qualityControl.isValid()) {
                disabled = true;
            }
        }

        qualityControl.setFormEnabled(!disabled);
        state.getComponentByReference(CLOSED_LITERAL).setEnabled(false);
        state.getComponentByReference(STAFF_LITERAL).setEnabled(false);
        state.getComponentByReference(DATE_LITERAL).setEnabled(false);

    }

    public boolean clearQualityControlOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(CLOSED_LITERAL, "0");
        entity.setField(FIELD_CONTROL_RESULT, null);
        entity.setField(NUMBER_LITERAL, qualityControlForNumber.generateNumber(dataDefinition.getPluginIdentifier(),
                dataDefinition.getName(), DIGITS_NUMBER, entity.getStringField(QUALITY_CONTROL_TYPE_LITERAL)));
        return true;
    }

    public boolean setStaffAndDateIfClosed(final DataDefinition dataDefinition, final Entity entity) {
        if ((Boolean) entity.getField(CLOSED_LITERAL)) {
            entity.setField(DATE_LITERAL, new Date());
            entity.setField(STAFF_LITERAL, securityService.getCurrentUserName());
        }
        return true;
    }

    public void changeQualityControlType(final ViewDefinitionState state) {
        FormComponent form = (FormComponent) state.getComponentByReference(FORM_LITERAL);
        FieldComponent qualityControlType = (FieldComponent) state.getComponentByReference(QUALITY_CONTROL_TYPE_LITERAL);
        if (form.getFieldValue() != null) {
            if (checkOperationQualityControlRequired((Long) form.getFieldValue())) {
                qualityControlType.setFieldValue(TYPE_04FOR_OPERATION);
                qualityControlType.setEnabled(false);
                qualityControlType.requestComponentUpdateState();
            } else {
                qualityControlType.setEnabled(true);
            }
        }
        FieldComponent unitSamplingNr = (FieldComponent) state.getComponentByReference(UNIT_SAMPLING_NR_LITERAL);
        if (qualityControlType.getFieldValue() == null || !qualityControlType.getFieldValue().equals(TYPE_02FOR_UNIT)) {
            unitSamplingNr.setRequired(false);
            unitSamplingNr.setVisible(false);
        } else if (qualityControlType.getFieldValue().equals(TYPE_02FOR_UNIT)) {
            unitSamplingNr.setRequired(true);
            unitSamplingNr.setVisible(true);
        }
    }

    private boolean checkOperationQualityControlRequired(final Long entityId) {
        if (dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).getField(FIELD_QUALITY_CONTROL_REQUIRED) == null) {
            return false;
        }

        SearchResult searchResult = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).find()
                .belongsTo(TECHNOLOGY_LITERAL, entityId).add(SearchRestrictions.eq(FIELD_QUALITY_CONTROL_REQUIRED, true))
                .setMaxResults(1).list();

        return (searchResult.getTotalNumberOfEntities() > 0);

    }

    private boolean hasQuantitiesToBeChecked(final String qualityControlType) {
        if (QUALITY_CONTROLS_FOR_UNIT_LITERAL.equals(qualityControlType)
                || QUALITY_CONTROLS_FOR_BATCH_LITERAL.equals(qualityControlType)) {
            return true;
        }

        return false;
    }

    private boolean hasControlResult(final String qualityControlType) {
        if (QUALITY_CONTROLS_FOR_ORDER_LITERAL.equals(qualityControlType)
                || QUALITY_CONTROLS_FOR_OPERATION_LITERAL.equals(qualityControlType)) {
            return true;
        }

        return false;
    }

    private String getInstructionForOrder(final Long fieldValue) {
        DataDefinition orderDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);

        SearchCriteriaBuilder searchCriteria = orderDD.find().setMaxResults(1).add(SearchRestrictions.eq("id", fieldValue));

        return (String) searchCriteria.list().getEntities().get(0).getBelongsToField(TECHNOLOGY_LITERAL)
                .getField("qualityControlInstruction");
    }

    private boolean isNumber(final String value) {
        try {
            new BigDecimal(value);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private boolean isQualityControlAutoGenEnabled() {
        SearchResult searchResult = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER)
                .find().setMaxResults(1).list();

        Entity parameter = null;
        if (searchResult.getEntities().size() > 0) {
            parameter = searchResult.getEntities().get(0);
        }

        if (parameter == null) {
            return false;
        } else {
            return parameter.getField("autoGenerateQualityControl") == null ? false : (Boolean) parameter
                    .getField("autoGenerateQualityControl");
        }
    }

    private void generateQualityControlForGivenType(final String qualityControlType, final Entity technology, final Entity order) {

        if (TYPE_01FOR_BATCH.equals(qualityControlType)) {
            List<Entity> genealogies = getGenealogiesForOrder(order.getId());

            for (Entity genealogy : genealogies) {
                createAndSaveControlForSingleBatch(order, genealogy);
            }

        } else if (TYPE_02FOR_UNIT.equals(qualityControlType)) {
            BigDecimal sampling = (BigDecimal) technology.getField(UNIT_SAMPLING_NR_LITERAL);

            BigDecimal doneQuantity = (BigDecimal) order.getField(DONE_QUANTITY_LITERAL);
            BigDecimal plannedQuantity = (BigDecimal) order.getField(PLANNED_QUANTITY_LITERAL);
            BigDecimal numberOfControls = doneQuantity == null ? plannedQuantity.divide(sampling, RoundingMode.HALF_UP)
                    : doneQuantity.divide(sampling, RoundingMode.HALF_UP);

            for (int i = 0; i <= numberOfControls.intValue(); i++) {
                DataDefinition qualityForUnitDataDefinition = dataDefinitionService.get(
                        QualityControlsConstants.PLUGIN_IDENTIFIER, QualityControlsConstants.MODEL_QUALITY_CONTROL);

                Entity forUnit = qualityForUnitDataDefinition.create();
                forUnit.setField(ORDER_LITERAL, order);
                forUnit.setField(NUMBER_LITERAL, qualityControlForNumber.generateNumber(
                        QualityControlsConstants.PLUGIN_IDENTIFIER, QualityControlsConstants.MODEL_QUALITY_CONTROL,
                        DIGITS_NUMBER, QUALITY_CONTROLS_FOR_UNIT_LITERAL));
                forUnit.setField(CLOSED_LITERAL, false);
                forUnit.setField(QUALITY_CONTROL_TYPE_LITERAL, QUALITY_CONTROLS_FOR_UNIT_LITERAL);
                forUnit.setField(TAKEN_FOR_CONTROL_QUANTITY_LITERAL, BigDecimal.ONE);
                forUnit.setField(REJECTED_QUANTITY_LITERAL, BigDecimal.ZERO);
                forUnit.setField(ACCEPTED_DEFECTS_QUANTITY_LITERAL, BigDecimal.ZERO);

                if (i < numberOfControls.intValue()) {
                    forUnit.setField(CONTROLLED_QUANTITY_LITERAL, sampling);
                } else {
                    BigDecimal numberOfRemainders = doneQuantity == null ? plannedQuantity.divideAndRemainder(sampling)[1]
                            : doneQuantity.divideAndRemainder(sampling)[1];
                    forUnit.setField(CONTROLLED_QUANTITY_LITERAL, numberOfRemainders);

                    if (numberOfRemainders.compareTo(BigDecimal.ZERO) < 1) {
                        return;
                    }
                }
                setControlInstruction(order, forUnit);
                qualityForUnitDataDefinition.save(forUnit);
            }
        } else if (TYPE_03FOR_ORDER.equals(qualityControlType)) {
            createAndSaveControlForSingleOrder(order);
        } else if (TYPE_04FOR_OPERATION.equals(qualityControlType)) {
            EntityTree tree = technology.getTreeField(OPERATION_COMPONENTS_LITERAL);
            for (Entity entity : tree) {
                if (entity.getField(FIELD_QUALITY_CONTROL_REQUIRED) != null
                        && (Boolean) entity.getField(FIELD_QUALITY_CONTROL_REQUIRED)) {

                    createAndSaveControlForOperation(order, entity);
                }

            }
        }
    }

    private void createAndSaveControlForOperation(final Entity order, final Entity entity) {
        DataDefinition qualityForOperationDataDefinition = dataDefinitionService.get(QualityControlsConstants.PLUGIN_IDENTIFIER,
                QualityControlsConstants.MODEL_QUALITY_CONTROL);

        Entity forOperation = qualityForOperationDataDefinition.create();
        forOperation.setField(ORDER_LITERAL, order);
        forOperation.setField(NUMBER_LITERAL, qualityControlForNumber.generateNumber(QualityControlsConstants.PLUGIN_IDENTIFIER,
                QualityControlsConstants.MODEL_QUALITY_CONTROL, DIGITS_NUMBER, "qualityControlForOperation"));
        forOperation.setField(OPERATION_LITERAL, entity.getBelongsToField(OPERATION_LITERAL));
        forOperation.setField(CLOSED_LITERAL, false);
        forOperation.setField(QUALITY_CONTROL_TYPE_LITERAL, QUALITY_CONTROLS_FOR_OPERATION_LITERAL);

        setControlInstruction(order, forOperation);

        qualityForOperationDataDefinition.save(forOperation);
    }

    private void createAndSaveControlForSingleOrder(final Entity order) {
        DataDefinition qualityForOrderDataDefinition = dataDefinitionService.get(QualityControlsConstants.PLUGIN_IDENTIFIER,
                QualityControlsConstants.MODEL_QUALITY_CONTROL);

        Entity forOrder = qualityForOrderDataDefinition.create();
        forOrder.setField(ORDER_LITERAL, order);
        forOrder.setField(NUMBER_LITERAL, qualityControlForNumber.generateNumber(QualityControlsConstants.PLUGIN_IDENTIFIER,
                QualityControlsConstants.MODEL_QUALITY_CONTROL, DIGITS_NUMBER, QUALITY_CONTROLS_FOR_ORDER_LITERAL));
        forOrder.setField(CLOSED_LITERAL, false);
        forOrder.setField(QUALITY_CONTROL_TYPE_LITERAL, QUALITY_CONTROLS_FOR_ORDER_LITERAL);

        setControlInstruction(order, forOrder);

        qualityForOrderDataDefinition.save(forOrder);
    }

    private void createAndSaveControlForSingleBatch(final Entity order, final Entity genealogy) {
        DataDefinition qualityForBatchDataDefinition = dataDefinitionService.get(QualityControlsConstants.PLUGIN_IDENTIFIER,
                QualityControlsConstants.MODEL_QUALITY_CONTROL);

        Entity forBatch = qualityForBatchDataDefinition.create();
        forBatch.setField(ORDER_LITERAL, order);
        forBatch.setField(NUMBER_LITERAL, qualityControlForNumber.generateNumber(QualityControlsConstants.PLUGIN_IDENTIFIER,
                QualityControlsConstants.MODEL_QUALITY_CONTROL, DIGITS_NUMBER, QUALITY_CONTROLS_FOR_BATCH_LITERAL));
        forBatch.setField("batchNr", genealogy.getField("batch"));
        forBatch.setField(CLOSED_LITERAL, false);
        forBatch.setField(QUALITY_CONTROL_TYPE_LITERAL, QUALITY_CONTROLS_FOR_BATCH_LITERAL);

        if (getGenealogiesForOrder(order.getId()).size() == 1) {
            BigDecimal doneQuantity = (BigDecimal) order.getField(DONE_QUANTITY_LITERAL);
            BigDecimal plannedQuantity = (BigDecimal) order.getField(PLANNED_QUANTITY_LITERAL);

            if (doneQuantity != null) {
                forBatch.setField(CONTROLLED_QUANTITY_LITERAL, doneQuantity);
            } else if (plannedQuantity != null) {
                forBatch.setField(CONTROLLED_QUANTITY_LITERAL, plannedQuantity);
            }
        } else {
            forBatch.setField(CONTROLLED_QUANTITY_LITERAL, BigDecimal.ZERO);
        }

        forBatch.setField(TAKEN_FOR_CONTROL_QUANTITY_LITERAL, BigDecimal.ONE);
        forBatch.setField(REJECTED_QUANTITY_LITERAL, BigDecimal.ZERO);
        forBatch.setField(ACCEPTED_DEFECTS_QUANTITY_LITERAL, BigDecimal.ZERO);

        setControlInstruction(order, forBatch);

        qualityForBatchDataDefinition.save(forBatch);
    }

    private void setControlInstruction(final Entity order, final Entity qualityControl) {
        String qualityControlInstruction = (String) order.getBelongsToField(TECHNOLOGY_LITERAL).getField(
                "qualityControlInstruction");

        if (qualityControlInstruction != null) {
            qualityControl.setField(CONTROL_INSTRUCTION_LITERAL, qualityControlInstruction);
        }
    }

    private List<Entity> getGenealogiesForOrder(final Long id) {
        DataDefinition genealogyDD = dataDefinitionService.get("genealogies", "genealogy");

        SearchCriteriaBuilder searchCriteria = genealogyDD.find().belongsTo(ORDER_LITERAL, id);

        return searchCriteria.list().getEntities();
    }

}
