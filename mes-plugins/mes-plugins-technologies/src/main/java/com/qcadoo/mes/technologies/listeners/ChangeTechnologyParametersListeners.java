package com.qcadoo.mes.technologies.listeners;

import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.mes.technologies.TechnologyNameAndNumberGenerator;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
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
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChangeTechnologyParametersListeners {



    private static final String L_CHANGE_GROUP = "changeGroup";

    private static final String L_CHANGE_PERFORMANCE_NORM = "changePerformanceNorm";

    public static final String L_STANDARD_PERFORMANCE_TECHNOLOGY = "standardPerformanceTechnology";

    private static final String L_TECHNOLOGY_GROUP = "technologyGroup";

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
            group = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY_GROUP).get(entity.getLongField(L_TECHNOLOGY_GROUP));
            entity.setField(L_TECHNOLOGY_GROUP, group);
        }

        try {
            entity = entity.getDataDefinition().validate(entity);
            if (!entity.isValid()) {
                form.setEntity(entity);
                return;
            }
        } catch (IllegalArgumentException e) {
            form.findFieldComponentByName(L_STANDARD_PERFORMANCE_TECHNOLOGY).addMessage(
                    "qcadooView.validate.field.error.invalidNumericFormat", ComponentState.MessageType.FAILURE);
            generated.setChecked(false);
            return;
        }
        JSONObject context = view.getJsonContext();
        Set<Long> ids = Arrays
                .stream(context.getString("window.mainTab.form.gridLayout.selectedEntities").replaceAll("[\\[\\]]", "")
                        .split(",")).map(Long::valueOf).collect(Collectors.toSet());

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
        ids.forEach(techId -> {
            Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY).get(techId);

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

                Entity savedTech = copyTechnology.getDataDefinition().save(copyTechnology);
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

}
