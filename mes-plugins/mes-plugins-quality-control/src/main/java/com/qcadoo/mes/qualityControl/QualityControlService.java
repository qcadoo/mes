package com.qcadoo.mes.qualityControl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.NumberGeneratorService;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.internal.EntityTree;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.RestrictionOperator;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.types.internal.DateType;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.mes.view.components.grid.GridComponentState;
import com.qcadoo.mes.view.components.lookup.LookupComponentState;
import com.qcadoo.mes.view.components.select.SelectComponentState;

@Service
public class QualityControlService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void checkIfCommentIsRequiredBasedOnResult(final ViewDefinitionState state, final Locale locale) {
        FormComponentState form = (FormComponentState) state.getComponentByReference("form");
        if (form.getFieldValue() != null) {
            FieldComponentState comment = (FieldComponentState) state.getComponentByReference("comment");

            FieldComponentState controlResult = (FieldComponentState) state.getComponentByReference("controlResult");

            if (controlResult != null && controlResult.getFieldValue().equals("03objection")) {
                comment.setRequired(true);
                comment.requestComponentUpdateState();
            } else {
                comment.setRequired(false);
            }
        }
    }

    public void checkIfCommentIsRequiredBasedOnDefects(final ViewDefinitionState state, final Locale locale) {
        FormComponentState form = (FormComponentState) state.getComponentByReference("form");
        if (form.getFieldValue() != null) {

            FieldComponentState comment = (FieldComponentState) state.getComponentByReference("comment");

            FieldComponentState acceptedDefectsQuantity = (FieldComponentState) state
                    .getComponentByReference("acceptedDefectsQuantity");

            if (acceptedDefectsQuantity.getFieldValue() != null
                    && (new BigDecimal(acceptedDefectsQuantity.getFieldValue().toString().replace(",", "."))
                            .compareTo(BigDecimal.ZERO) > 0)) {
                comment.setRequired(true);
                comment.requestComponentUpdateState();
            } else {
                comment.setRequired(false);
            }
        }
    }

    public boolean checkIfCommentForResultIsReq(final DataDefinition dataDefinition, final Entity entity) {
        String resultType = (String) entity.getField("controlResult");

        if (resultType != null && resultType.equals("03objection")) {

            String comment = (String) entity.getField("comment");
            if (comment == null || comment.isEmpty()) {
                entity.addGlobalError("core.validate.global.error.custom");
                entity.addError(dataDefinition.getField("comment"),
                        "qualityControl.quality.control.validate.global.error.comment");
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public boolean checkIfCommentForQuantityIsReq(final DataDefinition dataDefinition, final Entity entity) {
        BigDecimal acceptedDefectsQuantity = (BigDecimal) entity.getField("acceptedDefectsQuantity");

        if (acceptedDefectsQuantity != null) {
            String comment = (String) entity.getField("comment");

            if ((comment == null || comment.isEmpty()) && acceptedDefectsQuantity.compareTo(BigDecimal.ZERO) > 0) {
                entity.addGlobalError("core.validate.global.error.custom");
                entity.addError(dataDefinition.getField("comment"),
                        "qualityControl.quality.control.validate.global.error.comment");
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public void checkQualityControlResult(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof SelectComponentState)) {
            throw new IllegalStateException("component is not select");
        }

        SelectComponentState resultType = (SelectComponentState) state;

        FieldComponentState comment = (FieldComponentState) viewDefinitionState.getComponentByReference("comment");

        if (resultType.getFieldValue() != null && resultType.getFieldValue().equals("03objection")) {
            comment.setRequired(true);
        } else {
            comment.setRequired(false);
        }
    }

    public void closeQualityControl(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (state.getFieldValue() != null) {

            String controlType = args[0];

            if (state instanceof FormComponentState) {
                FieldComponentState controlResult = (FieldComponentState) viewDefinitionState
                        .getComponentByReference("controlResult");

                if (controlResult != null
                        && (controlResult.getFieldValue() == null || ((String) controlResult.getFieldValue()).isEmpty())) {
                    controlResult.addMessage(
                            translationService.translate("qualityControl.quality.control.result.missing", state.getLocale()),
                            MessageType.FAILURE);
                    state.addMessage(
                            translationService.translate("qualityControl.quality.control.result.missing", state.getLocale()),
                            MessageType.FAILURE);
                    return;
                } else if (controlResult != null
                        && ((controlResult.getFieldValue() != null) || !((String) controlResult.getFieldValue()).isEmpty())) {

                    FieldComponentState closed = (FieldComponentState) viewDefinitionState.getComponentByReference("closed");
                    FieldComponentState staff = (FieldComponentState) viewDefinitionState.getComponentByReference("staff");
                    FieldComponentState date = (FieldComponentState) viewDefinitionState.getComponentByReference("date");

                    if (staff != null) {
                        staff.setFieldValue(securityService.getCurrentUserName());
                    }

                    if (date != null) {
                        date.setFieldValue(new SimpleDateFormat(DateType.DATE_FORMAT).format(new Date()));
                    }

                    closed.setFieldValue(true);

                    ((FormComponentState) state).performEvent(viewDefinitionState, "save", new String[0]);
                }

            } else if (state instanceof GridComponentState) {
                DataDefinition qualityControlDD = dataDefinitionService.get("qualityControl", controlType);
                Entity qualityControl = qualityControlDD.get((Long) state.getFieldValue());

                FieldDefinition controlResultField = qualityControlDD.getField("controlResult");

                Object controlResult = qualityControl.getField("controlResult");

                if (controlResultField != null && (controlResult == null || controlResult.toString().isEmpty())) {
                    state.addMessage(
                            translationService.translate("qualityControl.quality.control.result.missing", state.getLocale()),
                            MessageType.FAILURE);
                    return;
                } else if (controlResultField == null || (controlResult != null && !controlResult.toString().isEmpty())) {

                    qualityControl.setField("staff", securityService.getCurrentUserName());
                    qualityControl.setField("date", new Date());
                    qualityControl.setField("closed", true);
                    qualityControlDD.save(qualityControl);

                    ((GridComponentState) state).performEvent(viewDefinitionState, "refresh", new String[0]);
                }
            }
            state.addMessage(translationService.translate("qualityControl.quality.control.closed.success", state.getLocale()),
                    MessageType.SUCCESS);
        } else {
            if (state instanceof FormComponentState) {
                state.addMessage(translationService.translate("core.form.entityWithoutIdentifier", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                state.addMessage(translationService.translate("core.grid.noRowSelectedError", state.getLocale()),
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
        Entity technology = order.getBelongsToField("technology");

        Object qualityControl = technology.getField("qualityControlType");

        if (qualityControl != null) {
            boolean qualityControlType = technology.getField("qualityControlType").toString().equals("01forBatch");

            if (isQualityControlAutoGenEnabled() || qualityControlType) {
                createAndSaveControlForSingleBatch(order, entity);
            }
        }
    }

    public void generateQualityControl(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state.getFieldValue() != null) {
            DataDefinition orderDataDefinition = dataDefinitionService.get("products", "order");
            Entity order = orderDataDefinition.get((Long) state.getFieldValue());

            Entity technology = (Entity) order.getField("technology");

            if (technology == null) {
                return;
            }

            if (technology.getField("qualityControlType") != null) {

                String qualityControlType = technology.getField("qualityControlType").toString();

                generateQualityControlForGivenType(qualityControlType, technology, order);

                state.addMessage(
                        translationService.translate("qualityControl.qualityControl.generated.success", state.getLocale()),
                        MessageType.SUCCESS);

                state.performEvent(viewDefinitionState, "refresh", new String[0]);
            } else {
                state.addMessage(
                        translationService.translate("qualityControl.qualityControl.qualityType.missing", state.getLocale()),
                        MessageType.FAILURE);
            }

        } else {
            if (state instanceof FormComponentState) {
                state.addMessage(translationService.translate("core.form.entityWithoutIdentifier", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                state.addMessage(translationService.translate("core.grid.noRowSelectedError", state.getLocale()),
                        MessageType.FAILURE);
            }
        }
    }

    public void checkAcceptedDefectsQuantity(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponentState)) {
            throw new IllegalStateException("component is not input");
        }

        FieldComponentState acceptedDefectsQuantity = (FieldComponentState) state;

        FieldComponentState comment = (FieldComponentState) viewDefinitionState.getComponentByReference("comment");

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
        if (!(state instanceof LookupComponentState)) {
            return;
        }

        LookupComponentState order = (LookupComponentState) state;
        FieldComponentState controlInstruction = (FieldComponentState) viewDefinitionState
                .getComponentByReference("controlInstruction");

        if (controlInstruction != null) {
            controlInstruction.setFieldValue("");
        } else {
            return;
        }

        if (order.getFieldValue() != null) {
            String qualityControlInstruction = getInstructionForOrder(order.getFieldValue());
            if (qualityControlInstruction != null) {
                controlInstruction.setFieldValue(qualityControlInstruction);
            }
        }
    }

    private String getInstructionForOrder(final Long fieldValue) {
        DataDefinition orderDD = dataDefinitionService.get("products", "order");

        SearchCriteriaBuilder searchCriteria = orderDD.find().withMaxResults(1)
                .restrictedWith(Restrictions.idRestriction(fieldValue, RestrictionOperator.EQ));

        return (String) searchCriteria.list().getEntities().get(0).getBelongsToField("technology")
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
        SearchResult searchResult = dataDefinitionService.get("basic", "parameter").find().withMaxResults(1).list();

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
        if (qualityControlType.equals("01forBatch")) {
            List<Entity> genealogies = getGenealogiesForOrder(order.getId());

            for (Entity genealogy : genealogies) {
                createAndSaveControlForSingleBatch(order, genealogy);
            }

        } else if (qualityControlType.equals("02forUnit")) {
            BigDecimal sampling = (BigDecimal) technology.getField("unitSamplingNr");

            BigDecimal doneQuantity = (BigDecimal) order.getField("doneQuantity");
            BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");
            BigDecimal numberOfControls = doneQuantity != null ? doneQuantity.divide(sampling, RoundingMode.HALF_UP)
                    : plannedQuantity.divide(sampling, RoundingMode.HALF_UP);

            for (int i = 0; i <= numberOfControls.intValue(); i++) {
                DataDefinition qualityForUnitDataDefinition = dataDefinitionService.get("qualityControl", "qualityForUnit");

                Entity forUnit = new DefaultEntity("qualityControl", "qualityForUnit");
                forUnit.setField("order", order);
                forUnit.setField("number", numberGeneratorService.generateNumber("qualityControl", "qualityForUnit"));
                forUnit.setField("closed", false);

                if (i < numberOfControls.intValue()) {
                    forUnit.setField("controlledQuantity", sampling);
                } else {
                    BigDecimal numberOfRemainders = doneQuantity != null ? doneQuantity.divideAndRemainder(sampling)[1]
                            : plannedQuantity.divideAndRemainder(sampling)[1];
                    forUnit.setField("controlledQuantity", numberOfRemainders);

                    if (numberOfRemainders.compareTo(new BigDecimal("0")) < 1) {
                        return;
                    }
                }

                setControlInstruction(order, forUnit);

                qualityForUnitDataDefinition.save(forUnit);
            }
        } else if (qualityControlType.equals("03forOrder")) {
            createAndSaveControlForSingleOrder(order);
        } else if (qualityControlType.equals("04forOperation")) {
            EntityTree tree = technology.getTreeField("operationComponents");
            for (Entity entity : tree) {
                if (entity.getField("qualityControlRequired") != null && (Boolean) entity.getField("qualityControlRequired")) {
                    createAndSaveControlForOperation(order, entity);
                }

            }
        }

    }

    private void createAndSaveControlForOperation(final Entity order, final Entity entity) {
        DataDefinition qualityForOperationDataDefinition = dataDefinitionService.get("qualityControl", "qualityForOperation");

        Entity forOperation = new DefaultEntity("qualityControl", "qualityForOperation");
        forOperation.setField("order", order);
        forOperation.setField("number", numberGeneratorService.generateNumber("qualityControl", "qualityForOperation"));
        forOperation.setField("operation", entity.getBelongsToField("operation"));
        forOperation.setField("closed", false);

        setControlInstruction(order, forOperation);

        qualityForOperationDataDefinition.save(forOperation);
    }

    private void createAndSaveControlForSingleOrder(final Entity order) {
        DataDefinition qualityForOrderDataDefinition = dataDefinitionService.get("qualityControl", "qualityForOrder");

        Entity forOrder = new DefaultEntity("qualityControl", "qualityForOrder");
        forOrder.setField("order", order);
        forOrder.setField("number", numberGeneratorService.generateNumber("qualityControl", "qualityForOrder"));
        forOrder.setField("closed", false);

        setControlInstruction(order, forOrder);

        qualityForOrderDataDefinition.save(forOrder);
    }

    private void createAndSaveControlForSingleBatch(final Entity order, final Entity genealogy) {
        DataDefinition qualityForBatchDataDefinition = dataDefinitionService.get("qualityControl", "qualityForBatch");

        Entity forBatch = new DefaultEntity("qualityControl", "qualityForBatch");
        forBatch.setField("order", order);
        forBatch.setField("number", numberGeneratorService.generateNumber("qualityControl", "qualityForBatch"));
        forBatch.setField("batchNr", genealogy.getField("batch"));
        forBatch.setField("closed", false);

        BigDecimal doneQuantity = (BigDecimal) order.getField("doneQuantity");
        BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");

        if (doneQuantity != null) {
            forBatch.setField("controlledQuantity", doneQuantity);
        } else if (plannedQuantity != null) {
            forBatch.setField("controlledQuantity", plannedQuantity);
        }

        setControlInstruction(order, forBatch);

        qualityForBatchDataDefinition.save(forBatch);
    }

    private void setControlInstruction(final Entity order, final Entity qualityControl) {
        String qualityControlInstruction = (String) order.getBelongsToField("technology").getField("qualityControlInstruction");

        if (qualityControlInstruction != null) {
            qualityControl.setField("controlInstruction", qualityControlInstruction);
        }
    }

    private List<Entity> getGenealogiesForOrder(final Long id) {
        DataDefinition genealogyDD = dataDefinitionService.get("genealogies", "genealogy");

        SearchCriteriaBuilder searchCriteria = genealogyDD.find().restrictedWith(Restrictions.eq("order.id", id));

        return searchCriteria.list().getEntities();
    }

}
