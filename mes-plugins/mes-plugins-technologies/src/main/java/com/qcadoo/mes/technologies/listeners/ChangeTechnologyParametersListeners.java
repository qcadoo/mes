package com.qcadoo.mes.technologies.listeners;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.mes.technologies.TechnologyNameAndNumberGenerator;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.states.TechnologyStateChangeViewClient;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ChangeTechnologyParametersListeners {

    private static final String L_CHANGE_GROUP = "changeGroup";

    private static final String L_CHANGE_PERFORMANCE_NORM = "changePerformanceNorm";

    private static final String L_STANDARD_PERFORMANCE_TECHNOLOGY = "standardPerformanceTechnology";

    private static final String L_TECHNOLOGY_GROUP = "technologyGroup";

    private static final Set<String> FIELDS_OPERATION = Sets.newHashSet("tpz", "tj", "productionInOneCycle",
            "nextOperationAfterProducedType", "nextOperationAfterProducedQuantity", "nextOperationAfterProducedQuantityUNIT",
            "timeNextOperation", "machineUtilization", "laborUtilization", "productionInOneCycleUNIT",
            "areProductQuantitiesDivisible", "isTjDivisible");

    private static final String L_UPDATE_OPERATION_TIME_NORMS = "updateOperationTimeNorms";

    private static final String NEXT_OPERATION_AFTER_PRODUCED_TYPE = "nextOperationAfterProducedType";

    private static final String PRODUCTION_IN_ONE_CYCLE = "productionInOneCycle";

    private static final String NEXT_OPERATION_AFTER_PRODUCED_QUANTITY = "nextOperationAfterProducedQuantity";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyNameAndNumberGenerator technologyNameAndNumberGenerator;

    @Autowired
    private TechnologyStateChangeViewClient technologyStateChangeViewClient;

    public void changeTechnologyParameters(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws JSONException {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent generated = (CheckBoxComponent) view.getComponentByReference("generated");

        LookupComponent lookupComponent = (LookupComponent) view.getComponentByReference(L_TECHNOLOGY_GROUP);
        String code = lookupComponent.getCurrentCode();
        if (StringUtils.isNoneEmpty(code) && Objects.isNull(lookupComponent.getFieldValue())) {
            form.findFieldComponentByName(L_TECHNOLOGY_GROUP).addMessage("qcadooView.lookup.noMatchError",
                    ComponentState.MessageType.FAILURE);
            generated.setChecked(false);
            return;
        }
        Entity group = null;
        Entity entity = form.getPersistedEntityWithIncludedFormValues();

        if (entity.getBooleanField(L_CHANGE_GROUP) && Objects.nonNull(entity.getLongField(L_TECHNOLOGY_GROUP))) {
            group = dataDefinitionService
                    .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_GROUP)
                    .get(entity.getLongField(L_TECHNOLOGY_GROUP));
            entity.setField(L_TECHNOLOGY_GROUP, group);
        }

        try {
            entity = entity.getDataDefinition().validate(entity);
            if (!entity.isValid()) {
                form.setEntity(entity);
                return;
            }
        } catch (IllegalArgumentException e) {
            form.findFieldComponentByName(L_STANDARD_PERFORMANCE_TECHNOLOGY)
                    .addMessage("qcadooView.validate.field.error.invalidNumericFormat", ComponentState.MessageType.FAILURE);
            generated.setChecked(false);
            return;
        }
        JSONObject context = view.getJsonContext();
        Set<Long> ids = Arrays.stream(
                context.getString("window.mainTab.form.gridLayout.selectedEntities").replaceAll("[\\[\\]]", "").split(","))
                .map(Long::valueOf).collect(Collectors.toSet());

        BigDecimal standardPerformanceTechnology = null;
        if (entity.getBooleanField(L_CHANGE_PERFORMANCE_NORM)) {
            standardPerformanceTechnology = entity.getDecimalField(L_STANDARD_PERFORMANCE_TECHNOLOGY);
        }

        try {
            createCustomizedTechnologies(view, state, ids, entity, group, standardPerformanceTechnology);
        } catch (Exception exc) {
            view.addMessage("technologies.changeTechnologyParameters.error.technologiesNotCreated",
                    ComponentState.MessageType.FAILURE);
        }
        generated.setChecked(true);
    }

    @Transactional
    private void createCustomizedTechnologies(ViewDefinitionState view, ComponentState state, Set<Long> ids, Entity entity,
            Entity finalGroup, BigDecimal finalStandardPerformanceTechnology) {
        boolean updateOperationTimeNorms = entity.getBooleanField(L_UPDATE_OPERATION_TIME_NORMS);
        ids.forEach(techId -> {
            Entity technology = dataDefinitionService
                    .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY).get(techId);

            technology.setField(TechnologyFields.MASTER, Boolean.FALSE);
            technology = technology.getDataDefinition().save(technology);
            if (technology.isValid()) {
                Entity copyTechnology = technology.getDataDefinition().copy(technology.getId()).get(0);
                Entity product = technology.getBelongsToField(TechnologyFields.PRODUCT);
                copyTechnology.setField(TechnologyFields.NUMBER, technologyNameAndNumberGenerator.generateNumber(product));
                copyTechnology.setField(TechnologyFields.NAME, technologyNameAndNumberGenerator.generateName(product));

                if (entity.getBooleanField(L_CHANGE_GROUP)) {
                    copyTechnology.setField(TechnologyFields.TECHNOLOGY_GROUP, finalGroup);
                }

                if (entity.getBooleanField(L_CHANGE_PERFORMANCE_NORM)) {
                    copyTechnology.setField(TechnologyFields.STANDARD_PERFORMANCE_TECHNOLOGY, finalStandardPerformanceTechnology);
                }
                copyTechnology = copyTechnology.getDataDefinition().save(copyTechnology);
                Entity copyTechnologyDb = copyTechnology.getDataDefinition().get(copyTechnology.getId());
                if (updateOperationTimeNorms) {
                    List<Entity> tocs = copyTechnologyDb.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);
                    tocs.forEach(toc -> {
                        Entity operation = toc.getBelongsToField(TechnologyOperationComponentFields.OPERATION);
                        for (String fieldName : FIELDS_OPERATION) {
                            toc.setField(fieldName, operation.getField(fieldName));
                        }
                        if (operation.getField(NEXT_OPERATION_AFTER_PRODUCED_TYPE) == null) {
                            toc.setField(NEXT_OPERATION_AFTER_PRODUCED_TYPE, "01all");
                        }

                        if (operation.getField(PRODUCTION_IN_ONE_CYCLE) == null) {
                            toc.setField(PRODUCTION_IN_ONE_CYCLE, "1");
                        }

                        if (operation.getField(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY) == null) {
                            toc.setField(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY, "0");
                        }
                        copyOperationWorkstationTimes(toc, operation);
                        toc.getDataDefinition().save(toc);
                    });

                }
                technologyStateChangeViewClient.changeState(new ViewContextHolder(view, state),
                        TechnologyStateStringValues.OUTDATED, technology);
                Entity savedTech = copyTechnologyDb.getDataDefinition().get(copyTechnologyDb.getId());
                if (savedTech.isValid()) {
                    technologyStateChangeViewClient.changeState(new ViewContextHolder(view, state),
                            TechnologyStateStringValues.ACCEPTED, savedTech);
                    Entity tech = savedTech.getDataDefinition().get(savedTech.getId());
                    tech.setField(TechnologyFields.MASTER, Boolean.TRUE);
                    tech.getDataDefinition().save(tech);
                } else {
                    throw new IllegalStateException("There was a problem creating the technology");
                }
            } else {
                throw new IllegalStateException("There was a problem creating the technology");
            }
        });
    }

    private void copyOperationWorkstationTimes(Entity toc, Entity operation) {
        for (Entity operationWorkstationTime : operation.getHasManyField("operationWorkstationTimes")) {
            for (Entity techOperCompWorkstationTime : toc.getHasManyField("techOperCompWorkstationTimes")) {
                if (techOperCompWorkstationTime.getBelongsToField("workstation").getId()
                        .equals(operationWorkstationTime.getBelongsToField("workstation").getId())) {
                    techOperCompWorkstationTime.setField("tpz", operationWorkstationTime.getField("tpz"));
                    techOperCompWorkstationTime.setField("tj", operationWorkstationTime.getField("tj"));
                    techOperCompWorkstationTime.setField("timeNextOperation",
                            operationWorkstationTime.getField("timeNextOperation"));
                    techOperCompWorkstationTime.getDataDefinition().save(techOperCompWorkstationTime);
                }
            }
        }
    }

    public void onChangePerformanceNorm(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        CheckBoxComponent changePerformanceNorm = (CheckBoxComponent) state;
        FieldComponent standardPerformanceTechnology = (FieldComponent) view
                .getComponentByReference("standardPerformanceTechnology");
        if (changePerformanceNorm.isChecked()) {
            standardPerformanceTechnology.setEnabled(true);
        } else {
            standardPerformanceTechnology.setEnabled(false);
            standardPerformanceTechnology.setFieldValue(null);
        }

    }

    public void onChangeChangeGroup(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        CheckBoxComponent changeGroup = (CheckBoxComponent) state;
        FieldComponent technologyGroup = (FieldComponent) view.getComponentByReference("technologyGroup");
        if (changeGroup.isChecked()) {
            technologyGroup.setEnabled(true);
        } else {
            technologyGroup.setEnabled(false);
            technologyGroup.setFieldValue(null);
        }
    }

    public void onChangeUpdateOperationTimeNorms(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {

    }

}
