package com.qcadoo.mes.technologies.listeners;

import com.google.common.collect.Sets;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.mes.technologies.TechnologyNameAndNumberGenerator;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.*;
import com.qcadoo.mes.technologies.states.TechnologyStateChangeViewClient;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChangeTechnologyParametersListeners {

    private static final String L_TPZ = "tpz";

    private static final String L_TJ = "tj";

    private static final String L_TIME_NEXT_OPERATION = "timeNextOperation";

    private static final String L_PRODUCTION_IN_ONE_CYCLE = "productionInOneCycle";

    private static final String L_NEXT_OPERATION_AFTER_PRODUCED_TYPE = "nextOperationAfterProducedType";

    private static final String L_NEXT_OPERATION_AFTER_PRODUCED_QUANTITY = "nextOperationAfterProducedQuantity";

    private static final String L_TECH_OPER_COMP_WORKSTATION_TIMES = "techOperCompWorkstationTimes";

    private static final String L_OPERATION_WORKSTATION_TIMES = "operationWorkstationTimes";

    private static final String L_WORKSTATION = "workstation";

    private static final Set<String> OPERATION_TIME_FIELDS = Sets.newHashSet(L_TPZ, L_TJ, L_TIME_NEXT_OPERATION,
            L_PRODUCTION_IN_ONE_CYCLE, L_NEXT_OPERATION_AFTER_PRODUCED_TYPE, L_NEXT_OPERATION_AFTER_PRODUCED_QUANTITY,
            "productionInOneCycleUNIT", "nextOperationAfterProducedQuantityUNIT", "areProductQuantitiesDivisible", "isTjDivisible",
            "laborUtilization", "machineUtilization", "minStaff", "optimalStaff", "tjDecreasesForEnlargedStaff", "pieceworkProduction"
    );

    private static final Set<String> OPERATION_COST_FIELDS = Sets.newHashSet("pieceRate", "laborHourlyCost", "machineHourlyCost");

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyNameAndNumberGenerator technologyNameAndNumberGenerator;

    @Autowired
    private TechnologyStateChangeViewClient technologyStateChangeViewClient;

    @Autowired
    private TechnologyService technologyService;

    public void changeTechnologyParameters(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws JSONException {
        FormComponent changeTechnologyParametersForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference(ChangeTechnologyParametersFields.GENERATED);
        LookupComponent technologyGroupLookup = (LookupComponent) view.getComponentByReference(ChangeTechnologyParametersFields.TECHNOLOGY_GROUP);

        String code = technologyGroupLookup.getCurrentCode();

        if (StringUtils.isNoneEmpty(code) && Objects.isNull(technologyGroupLookup.getFieldValue())) {
            changeTechnologyParametersForm.findFieldComponentByName(ChangeTechnologyParametersFields.TECHNOLOGY_GROUP).addMessage("qcadooView.lookup.noMatchError",
                    ComponentState.MessageType.FAILURE);

            generatedCheckBox.setChecked(false);

            return;
        }

        Entity changeTechnologyParameters = changeTechnologyParametersForm.getPersistedEntityWithIncludedFormValues();

        boolean changePerformanceNorm = changeTechnologyParameters.getBooleanField(ChangeTechnologyParametersFields.CHANGE_PERFORMANCE_NORM);
        boolean changeGroup = changeTechnologyParameters.getBooleanField(ChangeTechnologyParametersFields.CHANGE_GROUP);
        Long technologyGroupId = changeTechnologyParameters.getLongField(ChangeTechnologyParametersFields.TECHNOLOGY_GROUP);

        try {
            changeTechnologyParameters = changeTechnologyParameters.getDataDefinition().validate(changeTechnologyParameters);

            if (!changeTechnologyParameters.isValid()) {
                changeTechnologyParametersForm.setEntity(changeTechnologyParameters);

                return;
            }
        } catch (IllegalArgumentException e) {
            changeTechnologyParametersForm.findFieldComponentByName(ChangeTechnologyParametersFields.STANDARD_PERFORMANCE)
                    .addMessage("qcadooView.validate.field.error.invalidNumericFormat", ComponentState.MessageType.FAILURE);

            generatedCheckBox.setChecked(false);

            return;
        }

        BigDecimal standardPerformance = null;

        if (changePerformanceNorm) {
            standardPerformance = changeTechnologyParameters.getDecimalField(ChangeTechnologyParametersFields.STANDARD_PERFORMANCE);
        }

        Entity technologyGroup = null;

        if (changeGroup && Objects.nonNull(technologyGroupId)) {
            technologyGroup = getTechnologyGroupDD().get(technologyGroupId);

            changeTechnologyParameters.setField(ChangeTechnologyParametersFields.TECHNOLOGY_GROUP, technologyGroup);
        }

        JSONObject context = view.getJsonContext();

        Set<Long> technologyIds = Arrays.stream(
                        context.getString("window.mainTab.form.gridLayout.selectedEntities").replaceAll("[\\[\\]]", "").split(","))
                .map(Long::valueOf).collect(Collectors.toSet());

        try {
            createCustomizedTechnologies(view, state, technologyIds, changeTechnologyParameters, technologyGroup, standardPerformance);
        } catch (Exception exc) {
            view.addMessage("technologies.changeTechnologyParameters.error.technologiesNotCreated",
                    ComponentState.MessageType.FAILURE);
        }

        generatedCheckBox.setChecked(true);
    }

    @Transactional
    private void createCustomizedTechnologies(final ViewDefinitionState view, final ComponentState state, final Set<Long> technologyIds,
                                              final Entity changeTechnologyParameters, final Entity technologyGroup, final BigDecimal standardPerformance) {
        boolean changePerformanceNorm = changeTechnologyParameters.getBooleanField(ChangeTechnologyParametersFields.CHANGE_PERFORMANCE_NORM);
        boolean changeGroup = changeTechnologyParameters.getBooleanField(ChangeTechnologyParametersFields.CHANGE_GROUP);
        boolean updateOperationTimeNorms = changeTechnologyParameters.getBooleanField(ChangeTechnologyParametersFields.UPDATE_OPERATION_TIME_NORMS);
        boolean updateOperationCostNorms = changeTechnologyParameters.getBooleanField(ChangeTechnologyParametersFields.UPDATE_OPERATION_COST_NORMS);
        boolean updateOperationWorkstations = changeTechnologyParameters.getBooleanField(ChangeTechnologyParametersFields.UPDATE_OPERATION_WORKSTATIONS);

        technologyIds.forEach(technologyId -> {
            Entity technology = getTechnologyDD().get(technologyId);

            if (changePerformanceNorm) {
                Optional<Entity> productionLine = technologyService.getProductionLine(technology);

                if (!productionLine.isPresent()) {
                    view.addMessage("technologies.changeTechnologyParameters.error.noDefaultProductionLine", ComponentState.MessageType.FAILURE, technology.getStringField(TechnologyFields.NUMBER));

                    throw new IllegalStateException("There was a problem creating the technology");
                }
            }

            technology.setField(TechnologyFields.MASTER, Boolean.FALSE);

            technology = technology.getDataDefinition().save(technology);

            if (technology.isValid()) {
                Entity copyTechnology = technology.getDataDefinition().copy(technology.getId()).get(0);

                Entity product = technology.getBelongsToField(TechnologyFields.PRODUCT);

                copyTechnology.setField(TechnologyFields.NUMBER, technologyNameAndNumberGenerator.generateNumber(product));
                copyTechnology.setField(TechnologyFields.NAME, technologyNameAndNumberGenerator.generateName(product));

                if (changePerformanceNorm) {
                    technologyService.getMasterTechnologyProductionLine(copyTechnology).ifPresent(
                            technologyProductionLine -> {
                                technologyProductionLine.setField(TechnologyProductionLineFields.STANDARD_PERFORMANCE, standardPerformance);

                                technologyProductionLine.getDataDefinition().save(technologyProductionLine);
                            }
                    );
                }

                if (changeGroup) {
                    copyTechnology.setField(TechnologyFields.TECHNOLOGY_GROUP, technologyGroup);
                }

                copyTechnology = copyTechnology.getDataDefinition().save(copyTechnology);

                copyTechnology = copyTechnology.getDataDefinition().get(copyTechnology.getId());

                if (updateOperationTimeNorms || updateOperationCostNorms || updateOperationWorkstations) {
                    List<Entity> operationComponents = copyTechnology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);

                    operationComponents.forEach(technologyOperationComponent -> {
                        Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

                        if (updateOperationTimeNorms) {
                            for (String fieldName : OPERATION_TIME_FIELDS) {
                                technologyOperationComponent.setField(fieldName, operation.getField(fieldName));
                            }

                            if (Objects.isNull(operation.getField(L_PRODUCTION_IN_ONE_CYCLE))) {
                                technologyOperationComponent.setField(L_PRODUCTION_IN_ONE_CYCLE, "1");
                            }

                            if (Objects.isNull(operation.getField(L_NEXT_OPERATION_AFTER_PRODUCED_TYPE))) {
                                technologyOperationComponent.setField(L_NEXT_OPERATION_AFTER_PRODUCED_TYPE, "01all");
                            }

                            if (Objects.isNull(operation.getField(L_NEXT_OPERATION_AFTER_PRODUCED_QUANTITY))) {
                                technologyOperationComponent.setField(L_NEXT_OPERATION_AFTER_PRODUCED_QUANTITY, "0");
                            }

                            copyOperationWorkstationTimes(technologyOperationComponent, operation);
                        }

                        if (updateOperationCostNorms) {
                            for (String fieldName : OPERATION_COST_FIELDS) {
                                technologyOperationComponent.setField(fieldName, operation.getField(fieldName));
                            }
                        }

                        if (updateOperationWorkstations) {
                            technologyOperationComponent.setField(TechnologyOperationComponentFields.WORKSTATIONS, operation.getField(OperationFields.WORKSTATIONS));
                        }

                        technologyOperationComponent.getDataDefinition().save(technologyOperationComponent);
                    });
                }

                copyTechnology = copyTechnology.getDataDefinition().get(copyTechnology.getId());

                if (copyTechnology.isValid()) {
                    technologyStateChangeViewClient.changeState(new ViewContextHolder(view, state),
                            TechnologyStateStringValues.ACCEPTED, copyTechnology);

                    copyTechnology = copyTechnology.getDataDefinition().get(copyTechnology.getId());

                    copyTechnology.setField(TechnologyFields.MASTER, Boolean.TRUE);

                    copyTechnology.getDataDefinition().save(copyTechnology);
                } else {
                    throw new IllegalStateException("There was a problem creating the technology");
                }

                technologyStateChangeViewClient.changeState(new ViewContextHolder(view, state),
                        TechnologyStateStringValues.OUTDATED, technology);
            } else {
                throw new IllegalStateException("There was a problem creating the technology");
            }
        });
    }

    private void copyOperationWorkstationTimes(final Entity technologyOperationComponent, final Entity operation) {
        for (Entity operationWorkstationTime : operation.getHasManyField(L_OPERATION_WORKSTATION_TIMES)) {
            for (Entity techOperCompWorkstationTime : technologyOperationComponent.getHasManyField(L_TECH_OPER_COMP_WORKSTATION_TIMES)) {
                if (techOperCompWorkstationTime.getBelongsToField(L_WORKSTATION).getId()
                        .equals(operationWorkstationTime.getBelongsToField(L_WORKSTATION).getId())) {
                    techOperCompWorkstationTime.setField(L_TPZ, operationWorkstationTime.getField(L_TPZ));
                    techOperCompWorkstationTime.setField(L_TJ, operationWorkstationTime.getField(L_TJ));
                    techOperCompWorkstationTime.setField(L_TIME_NEXT_OPERATION, operationWorkstationTime.getField(L_TIME_NEXT_OPERATION));

                    techOperCompWorkstationTime.getDataDefinition().save(techOperCompWorkstationTime);

                    break;
                }
            }
        }
    }

    public void onChangePerformanceNorm(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        CheckBoxComponent changePerformanceNormCheckBox = (CheckBoxComponent) state;
        FieldComponent standardPerformanceField = (FieldComponent) view.getComponentByReference(ChangeTechnologyParametersFields.STANDARD_PERFORMANCE);

        if (changePerformanceNormCheckBox.isChecked()) {
            standardPerformanceField.setEnabled(true);
        } else {
            standardPerformanceField.setEnabled(false);
            standardPerformanceField.setFieldValue(null);
        }
    }

    public void onChangeChangeGroup(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        CheckBoxComponent changeGroupCheckBox = (CheckBoxComponent) state;
        LookupComponent technologyGroupLookup = (LookupComponent) view.getComponentByReference(ChangeTechnologyParametersFields.TECHNOLOGY_GROUP);

        if (changeGroupCheckBox.isChecked()) {
            technologyGroupLookup.setEnabled(true);
        } else {
            technologyGroupLookup.setEnabled(false);
            technologyGroupLookup.setFieldValue(null);
        }
    }

    private DataDefinition getTechnologyDD() {
        return dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }

    private DataDefinition getTechnologyGroupDD() {
        return dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_GROUP);
    }

}
