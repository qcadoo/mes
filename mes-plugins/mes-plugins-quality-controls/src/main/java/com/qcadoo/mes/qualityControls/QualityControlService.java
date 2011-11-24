/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private QualityControlForNumberService qualityControlForNumber;

    private static final Integer DIGITS_NUMBER = 6;

    private static final String FIELD_COMMENT = "comment";

    private static final String FIELD_CONTROL_RESULT = "controlResult";

    private static final String FIELD_QUALITY_CONTROL_REQUIRED = "qualityControlRequired";

    private static final String FIELD_TECHNOLOGY = "technology";

    private static final String FIELD_QUALITY_CONTROL_TYPE = "qualityControlType";

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
            Entity technology = entity.getBelongsToField(FIELD_TECHNOLOGY);
            DataDefinition technologyInDef = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY);
            Entity technologyEntity = technologyInDef.get(technology.getId());
            if (technologyEntity.getField(FIELD_QUALITY_CONTROL_TYPE) == null
                    || !technologyEntity.getField(FIELD_QUALITY_CONTROL_TYPE).equals("04forOperation")) {
                technologyEntity.setField(FIELD_QUALITY_CONTROL_TYPE, "04forOperation");
                technologyInDef.save(technologyEntity);
            }
        }
    }

    public void checkIfCommentIsRequiredBasedOnDefects(final ViewDefinitionState state) {
        FieldComponent comment = (FieldComponent) state.getComponentByReference(FIELD_COMMENT);

        FieldComponent acceptedDefectsQuantity = (FieldComponent) state.getComponentByReference("acceptedDefectsQuantity");

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
        String qualityControlType = (String) entity.getField(FIELD_QUALITY_CONTROL_TYPE);

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
                entity.addGlobalError("qcadooView.validate.global.error.custom");
                entity.addError(dataDefinition.getField(FIELD_COMMENT),
                        "qualityControls.quality.control.validate.global.error.comment");
                return false;
            }
        }
        return true;

    }

    public boolean checkIfCommentForQuantityIsReq(final DataDefinition dataDefinition, final Entity entity) {
        BigDecimal acceptedDefectsQuantity = (BigDecimal) entity.getField("acceptedDefectsQuantity");

        if (acceptedDefectsQuantity != null) {
            String comment = (String) entity.getField(FIELD_COMMENT);

            if ((comment == null || comment.isEmpty()) && acceptedDefectsQuantity.compareTo(BigDecimal.ZERO) > 0) {
                entity.addGlobalError("qcadooView.validate.global.error.custom");
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
                        .getComponentByReference(FIELD_QUALITY_CONTROL_TYPE)).getFieldValue().toString();

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

                    FieldComponent closed = (FieldComponent) viewDefinitionState.getComponentByReference("closed");
                    FieldComponent staff = (FieldComponent) viewDefinitionState.getComponentByReference("staff");
                    FieldComponent date = (FieldComponent) viewDefinitionState.getComponentByReference("date");

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
                String qualityControlType = (String) qualityControl.getField(FIELD_QUALITY_CONTROL_TYPE);

                if (hasControlResult(qualityControlType) && controlResultField != null
                        && (controlResult == null || controlResult.toString().isEmpty())) {
                    state.addMessage(
                            translationService.translate("qualityControls.quality.control.result.missing", state.getLocale()),
                            MessageType.FAILURE);
                    return;
                } else if (!hasControlResult(qualityControlType)
                        || (controlResultField == null || (controlResult != null && !controlResult.toString().isEmpty()))) {

                    qualityControl.setField("staff", securityService.getCurrentUserName());
                    qualityControl.setField("date", new Date());
                    qualityControl.setField("closed", true);
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
        Entity order = entity.getBelongsToField("order");
        Entity technology = order.getBelongsToField(FIELD_TECHNOLOGY);
        if (technology != null) {
            Object qualityControl = technology.getField(FIELD_QUALITY_CONTROL_TYPE);

            if (qualityControl != null) {
                boolean qualityControlType = "01forBatch".equals(technology.getField(FIELD_QUALITY_CONTROL_TYPE).toString());

                if (isQualityControlAutoGenEnabled() || qualityControlType) {
                    createAndSaveControlForSingleBatch(order, entity);
                }
            }

        }
    }

    public void generateQualityControl(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state.getFieldValue() != null) {

            DataDefinition orderDataDefinition = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                    OrdersConstants.MODEL_ORDER);
            Entity order = orderDataDefinition.get((Long) state.getFieldValue());

            Entity technology = (Entity) order.getField(FIELD_TECHNOLOGY);

            if (technology == null) {
                return;
            }

            if (technology.getField(FIELD_QUALITY_CONTROL_TYPE) != null) {

                String qualityControlType = technology.getField(FIELD_QUALITY_CONTROL_TYPE).toString();

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
            } else {
                state.addMessage(
                        translationService.translate("qualityControls.qualityControls.qualityType.missing", state.getLocale()),
                        MessageType.FAILURE);
            }

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

    private boolean isQualityControlForOrderExists(final Entity order) {
        DataDefinition dataDefinition = dataDefinitionService.get(QualityControlsConstants.PLUGIN_IDENTIFIER,
                QualityControlsConstants.MODEL_QUALITY_CONTROL);
        return dataDefinition.find().add(SearchRestrictions.belongsTo("order", order)).list().getTotalNumberOfEntities() != 0;
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
        FieldComponent controlInstruction = (FieldComponent) viewDefinitionState.getComponentByReference("controlInstruction");

        if (controlInstruction != null) {
            controlInstruction.setFieldValue("");
        } else {
            return;
        }

        if (order.getFieldValue() != null) {
            String qualityControlInstruction = getInstructionForOrder((Long) order.getFieldValue());
            if (qualityControlInstruction != null) {
                controlInstruction.setFieldValue(qualityControlInstruction);
            }
        }
    }

    public void enableCalendarsOnRender(final ViewDefinitionState state) {
        FieldComponent dateFrom = (FieldComponent) state.getComponentByReference("dateFrom");
        FieldComponent dateTo = (FieldComponent) state.getComponentByReference("dateTo");

        dateFrom.setEnabled(true);
        dateTo.setEnabled(true);
    }

    public void setQuantitiesToDefaulIfEmpty(final ViewDefinitionState state) {
        FieldComponent takenForControlQuantity = (FieldComponent) state.getComponentByReference("takenForControlQuantity");
        FieldComponent rejectedQuantity = (FieldComponent) state.getComponentByReference("rejectedQuantity");
        FieldComponent acceptedDefectsQuantity = (FieldComponent) state.getComponentByReference("acceptedDefectsQuantity");

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
        final GridComponent qualityControlsGrid = (GridComponent) viewDefinitionState.getComponentByReference("grid");
        final String qualityControlType = qualityControlsGrid.getName();

        qualityControlsGrid.setCustomRestriction(new CustomRestriction() {

            @Override
            public void addRestriction(final SearchCriteriaBuilder searchCriteriaBuilder) {
                searchCriteriaBuilder.isEq(FIELD_QUALITY_CONTROL_TYPE, qualityControlType);
            }

        });
    }

    public void setQualityControlTypeHiddenField(final ViewDefinitionState viewDefinitionState) {
        FormComponent qualityControlsForm = (FormComponent) viewDefinitionState.getComponentByReference("form");
        String qualityControlTypeString = qualityControlsForm.getName().replace("Control", "Controls");
        FieldComponent qualityControlType = (FieldComponent) viewDefinitionState
                .getComponentByReference(FIELD_QUALITY_CONTROL_TYPE);

        qualityControlType.setFieldValue(qualityControlTypeString);
    }

    public void setOperationAsRequired(final ViewDefinitionState state) {
        FieldComponent operation = (FieldComponent) state.getComponentByReference("operation");
        operation.setRequired(true);
    }

    public boolean checkIfOperationIsRequired(final DataDefinition dataDefinition, final Entity entity) {
        String qualityControlType = (String) entity.getField(FIELD_QUALITY_CONTROL_TYPE);

        if ("qualityControlsForOperation".equals(qualityControlType)) {
            Object operation = entity.getField("operation");

            if (operation == null) {
                entity.addGlobalError("qcadooView.validate.global.error.custom");
                entity.addError(dataDefinition.getField("operation"),
                        "qualityControls.quality.control.validate.global.error.operation");
                return false;
            }
        }

        return true;
    }

    public boolean checkIfQuantitiesAreCorrect(final DataDefinition dataDefinition, final Entity entity) {
        String qualityControlType = (String) entity.getField(FIELD_QUALITY_CONTROL_TYPE);

        if (hasQuantitiesToBeChecked(qualityControlType)) {
            BigDecimal controlledQuantity = (BigDecimal) entity.getField("controlledQuantity");
            BigDecimal takenForControlQuantity = (BigDecimal) entity.getField("takenForControlQuantity");
            BigDecimal rejectedQuantity = (BigDecimal) entity.getField("rejectedQuantity");
            BigDecimal acceptedDefectsQuantity = (BigDecimal) entity.getField("acceptedDefectsQuantity");

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
                entity.addGlobalError("qcadooView.validate.global.error.custom");
                entity.addError(dataDefinition.getField("rejectedQuantity"),
                        "qualityControls.quality.control.validate.global.error.rejectedQuantity.tooLarge");
                return false;
            }

            if (acceptedDefectsQuantity.compareTo(takenForControlQuantity.subtract(rejectedQuantity)) > 0) {
                entity.addGlobalError("qcadooView.validate.global.error.custom");
                entity.addError(dataDefinition.getField("acceptedDefectsQuantity"),
                        "qualityControls.quality.control.validate.global.error.acceptedDefectsQuantity.tooLarge");
                return false;
            }

            entity.setField("controlledQuantity", controlledQuantity);
            entity.setField("takenForControlQuantity", takenForControlQuantity);
            entity.setField("rejectedQuantity", rejectedQuantity);
            entity.setField("acceptedDefectsQuantity", acceptedDefectsQuantity);
        }

        return true;
    }

    public void disableFormForClosedControl(final ViewDefinitionState state) {
        FormComponent qualityControl = (FormComponent) state.getComponentByReference("form");
        boolean disabled = false;

        if (qualityControl.getEntityId() != null) {
            Entity entity = dataDefinitionService.get("qualityControls", "qualityControl").get(qualityControl.getEntityId());

            if (entity != null && (Boolean) entity.getField("closed") && qualityControl.isValid()) {
                disabled = true;
            }
        }

        qualityControl.setFormEnabled(!disabled);
        state.getComponentByReference("closed").setEnabled(false);
        state.getComponentByReference("staff").setEnabled(false);
        state.getComponentByReference("date").setEnabled(false);

    }

    public boolean clearQualityControlOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("closed", "0");
        entity.setField(FIELD_CONTROL_RESULT, null);
        entity.setField("number", qualityControlForNumber.generateNumber(dataDefinition.getPluginIdentifier(),
                dataDefinition.getName(), DIGITS_NUMBER, entity.getStringField(FIELD_QUALITY_CONTROL_TYPE)));
        return true;
    }

    public boolean setStaffAndDateIfClosed(final DataDefinition dataDefinition, final Entity entity) {
        if ((Boolean) entity.getField("closed")) {
            entity.setField("date", new Date());
            entity.setField("staff", securityService.getCurrentUserName());
        }
        return true;
    }

    public void changeQualityControlType(final ViewDefinitionState state) {
        FormComponent form = (FormComponent) state.getComponentByReference("form");
        FieldComponent qualityControlType = (FieldComponent) state.getComponentByReference(FIELD_QUALITY_CONTROL_TYPE);
        if (form.getFieldValue() != null) {
            if (checkOperationQualityControlRequired((Long) form.getFieldValue())) {
                qualityControlType.setFieldValue("04forOperation");
                qualityControlType.setEnabled(false);
                qualityControlType.requestComponentUpdateState();
            } else {
                qualityControlType.setEnabled(true);
            }
        }
        FieldComponent unitSamplingNr = (FieldComponent) state.getComponentByReference("unitSamplingNr");
        if (qualityControlType.getFieldValue() == null || !qualityControlType.getFieldValue().equals("02forUnit")) {
            unitSamplingNr.setRequired(false);
            unitSamplingNr.setVisible(false);
        } else if (qualityControlType.getFieldValue().equals("02forUnit")) {
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
                .belongsTo(FIELD_TECHNOLOGY, entityId).isEq(FIELD_QUALITY_CONTROL_REQUIRED, true).setMaxResults(1).list();

        return (searchResult.getTotalNumberOfEntities() > 0);

    }

    private boolean hasQuantitiesToBeChecked(final String qualityControlType) {
        if ("qualityControlsForUnit".equals(qualityControlType) || "qualityControlsForBatch".equals(qualityControlType)) {
            return true;
        }

        return false;
    }

    private boolean hasControlResult(final String qualityControlType) {
        if ("qualityControlsForOrder".equals(qualityControlType) || "qualityControlsForOperation".equals(qualityControlType)) {
            return true;
        }

        return false;
    }

    private String getInstructionForOrder(final Long fieldValue) {
        DataDefinition orderDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);

        SearchCriteriaBuilder searchCriteria = orderDD.find().setMaxResults(1).isIdEq(fieldValue);

        return (String) searchCriteria.list().getEntities().get(0).getBelongsToField(FIELD_TECHNOLOGY)
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

        if (parameter != null) {
            return parameter.getField("autoGenerateQualityControl") == null ? false : (Boolean) parameter
                    .getField("autoGenerateQualityControl");
        } else {
            return false;
        }
    }

    private void generateQualityControlForGivenType(final String qualityControlType, final Entity technology, final Entity order) {

        if ("01forBatch".equals(qualityControlType)) {
            List<Entity> genealogies = getGenealogiesForOrder(order.getId());

            for (Entity genealogy : genealogies) {
                createAndSaveControlForSingleBatch(order, genealogy);
            }

        } else if ("02forUnit".equals(qualityControlType)) {
            BigDecimal sampling = (BigDecimal) technology.getField("unitSamplingNr");

            BigDecimal doneQuantity = (BigDecimal) order.getField("doneQuantity");
            BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");
            BigDecimal numberOfControls = doneQuantity != null ? doneQuantity.divide(sampling, RoundingMode.HALF_UP)
                    : plannedQuantity.divide(sampling, RoundingMode.HALF_UP);

            for (int i = 0; i <= numberOfControls.intValue(); i++) {
                DataDefinition qualityForUnitDataDefinition = dataDefinitionService.get(
                        QualityControlsConstants.PLUGIN_IDENTIFIER, QualityControlsConstants.MODEL_QUALITY_CONTROL);

                Entity forUnit = qualityForUnitDataDefinition.create();
                forUnit.setField("order", order);
                forUnit.setField("number", qualityControlForNumber.generateNumber(QualityControlsConstants.PLUGIN_IDENTIFIER,
                        QualityControlsConstants.MODEL_QUALITY_CONTROL, DIGITS_NUMBER, "qualityControlsForUnit"));
                forUnit.setField("closed", false);
                forUnit.setField(FIELD_QUALITY_CONTROL_TYPE, "qualityControlsForUnit");
                forUnit.setField("takenForControlQuantity", BigDecimal.ONE);
                forUnit.setField("rejectedQuantity", BigDecimal.ZERO);
                forUnit.setField("acceptedDefectsQuantity", BigDecimal.ZERO);

                if (i < numberOfControls.intValue()) {
                    forUnit.setField("controlledQuantity", sampling);
                } else {
                    BigDecimal numberOfRemainders = doneQuantity != null ? doneQuantity.divideAndRemainder(sampling)[1]
                            : plannedQuantity.divideAndRemainder(sampling)[1];
                    forUnit.setField("controlledQuantity", numberOfRemainders);

                    if (numberOfRemainders.compareTo(BigDecimal.ZERO) < 1) {
                        return;
                    }
                }
                setControlInstruction(order, forUnit);
                qualityForUnitDataDefinition.save(forUnit);
            }
        } else if ("03forOrder".equals(qualityControlType)) {
            createAndSaveControlForSingleOrder(order);
        } else if ("04forOperation".equals(qualityControlType)) {
            EntityTree tree = technology.getTreeField("operationComponents");
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
        forOperation.setField("order", order);
        forOperation.setField("number", qualityControlForNumber.generateNumber(QualityControlsConstants.PLUGIN_IDENTIFIER,
                QualityControlsConstants.MODEL_QUALITY_CONTROL, DIGITS_NUMBER, "qualityControlForOperation"));
        forOperation.setField("operation", entity.getBelongsToField("operation"));
        forOperation.setField("closed", false);
        forOperation.setField(FIELD_QUALITY_CONTROL_TYPE, "qualityControlsForOperation");

        setControlInstruction(order, forOperation);

        qualityForOperationDataDefinition.save(forOperation);
    }

    private void createAndSaveControlForSingleOrder(final Entity order) {
        DataDefinition qualityForOrderDataDefinition = dataDefinitionService.get(QualityControlsConstants.PLUGIN_IDENTIFIER,
                QualityControlsConstants.MODEL_QUALITY_CONTROL);

        Entity forOrder = qualityForOrderDataDefinition.create();
        forOrder.setField("order", order);
        forOrder.setField("number", qualityControlForNumber.generateNumber(QualityControlsConstants.PLUGIN_IDENTIFIER,
                QualityControlsConstants.MODEL_QUALITY_CONTROL, DIGITS_NUMBER, "qualityControlsForOrder"));
        forOrder.setField("closed", false);
        forOrder.setField(FIELD_QUALITY_CONTROL_TYPE, "qualityControlsForOrder");

        setControlInstruction(order, forOrder);

        qualityForOrderDataDefinition.save(forOrder);
    }

    private void createAndSaveControlForSingleBatch(final Entity order, final Entity genealogy) {
        DataDefinition qualityForBatchDataDefinition = dataDefinitionService.get(QualityControlsConstants.PLUGIN_IDENTIFIER,
                QualityControlsConstants.MODEL_QUALITY_CONTROL);

        Entity forBatch = qualityForBatchDataDefinition.create();
        forBatch.setField("order", order);
        forBatch.setField("number", qualityControlForNumber.generateNumber(QualityControlsConstants.PLUGIN_IDENTIFIER,
                QualityControlsConstants.MODEL_QUALITY_CONTROL, DIGITS_NUMBER, "qualityControlsForBatch"));
        forBatch.setField("batchNr", genealogy.getField("batch"));
        forBatch.setField("closed", false);
        forBatch.setField(FIELD_QUALITY_CONTROL_TYPE, "qualityControlsForBatch");

        if (getGenealogiesForOrder(order.getId()).size() == 1) {
            BigDecimal doneQuantity = (BigDecimal) order.getField("doneQuantity");
            BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");

            if (doneQuantity != null) {
                forBatch.setField("controlledQuantity", doneQuantity);
            } else if (plannedQuantity != null) {
                forBatch.setField("controlledQuantity", plannedQuantity);
            }
        } else {
            forBatch.setField("controlledQuantity", BigDecimal.ZERO);
        }

        forBatch.setField("takenForControlQuantity", BigDecimal.ONE);
        forBatch.setField("rejectedQuantity", BigDecimal.ZERO);
        forBatch.setField("acceptedDefectsQuantity", BigDecimal.ZERO);

        setControlInstruction(order, forBatch);

        qualityForBatchDataDefinition.save(forBatch);
    }

    private void setControlInstruction(final Entity order, final Entity qualityControl) {
        String qualityControlInstruction = (String) order.getBelongsToField(FIELD_TECHNOLOGY).getField(
                "qualityControlInstruction");

        if (qualityControlInstruction != null) {
            qualityControl.setField("controlInstruction", qualityControlInstruction);
        }
    }

    private List<Entity> getGenealogiesForOrder(final Long id) {
        DataDefinition genealogyDD = dataDefinitionService.get("genealogies", "genealogy");

        SearchCriteriaBuilder searchCriteria = genealogyDD.find().belongsTo("order", id);

        return searchCriteria.list().getEntities();
    }

}
