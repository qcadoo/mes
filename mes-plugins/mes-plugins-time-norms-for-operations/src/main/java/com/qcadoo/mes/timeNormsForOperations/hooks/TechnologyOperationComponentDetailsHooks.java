package com.qcadoo.mes.timeNormsForOperations.hooks;

import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.COUNT_MACHINE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.COUNT_MACHINE_UNIT;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.COUNT_REALIZED;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.TIME_NEXT_OPERATION;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TechnologyOperationComponentDetailsHooks {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private NumberService numberService;

    public void checkOperationOutputQuantities(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");

        Entity operationComponent = form.getEntity();
        operationComponent = operationComponent.getDataDefinition().get(operationComponent.getId());

        BigDecimal timeNormsQuantity = operationComponent.getDecimalField("productionInOneCycle");

        Entity productOutComponent = null;

        try {
            productOutComponent = technologyService.getMainOutputProductComponent(operationComponent);
        } catch (IllegalStateException e) {
            return;
        }

        Locale locale = LocaleContextHolder.getLocale();

        BigDecimal currentQuantity = productOutComponent.getDecimalField("quantity");

        if (timeNormsQuantity.compareTo(currentQuantity) != 0) { // Not using equals intentionally
            ComponentState productionInOneCycle = view.getComponentByReference("productionInOneCycle");

            productionInOneCycle.addMessage("technologies.technologyOperationComponent.validate.error.invalidQuantity",
                    MessageType.FAILURE, numberService.format(currentQuantity), productOutComponent.getBelongsToField("product")
                            .getStringField("unit"));
        }
    }

    public void updateCountMachineFieldStateonWindowLoad(final ViewDefinitionState viewDefinitionState) {
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_REALIZED);
        FieldComponent countMachine = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_MACHINE);
        FieldComponent countMachineUNIT = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_MACHINE_UNIT);

        if ("02specified".equals(countRealized.getFieldValue())) {
            countMachine.setVisible(true);
            countMachine.setEnabled(true);
            countMachineUNIT.setVisible(true);
            countMachineUNIT.setEnabled(true);
        } else {
            countMachine.setVisible(false);
            countMachineUNIT.setVisible(false);

        }
    }

    public void updateFieldsStateOnWindowLoad(final ViewDefinitionState viewDefinitionState) {
        FieldComponent tpzNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tpz");
        FieldComponent tjNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tj");
        FieldComponent productionInOneCycle = (FieldComponent) viewDefinitionState
                .getComponentByReference(PRODUCTION_IN_ONE_CYCLE);
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_REALIZED);
        FieldComponent timeNextOperation = (FieldComponent) viewDefinitionState.getComponentByReference(TIME_NEXT_OPERATION);
        Object value = countRealized.getFieldValue();

        tpzNorm.setEnabled(true);
        tjNorm.setEnabled(true);
        productionInOneCycle.setEnabled(true);

        countRealized.setEnabled(true);
        if (!"02specified".equals(value)) {
            countRealized.setFieldValue("01all");
        }
        timeNextOperation.setEnabled(true);
    }

    public void fillUnitFields(final ViewDefinitionState view) {
        FieldComponent component = null;
        Entity formEntity = ((FormComponent) view.getComponentByReference("form")).getEntity();

        // we can pass units only to technology level operations
        if (formEntity.getId() == null || !TECHNOLOGY_OPERATION_COMPONENT.equals(formEntity.getDataDefinition().getName())) {
            return;
        }

        // be sure that entity isn't in detached state before you wander through the relationship
        formEntity = formEntity.getDataDefinition().get(formEntity.getId());
        // you can use someEntity.getSTH().getSTH() only when you are 100% sure that all the passers-relations
        // will not return null (i.e. all relations using below are mandatory on the model definition level)
        String unit = formEntity.getBelongsToField("technology").getBelongsToField("product").getField("unit").toString();
        for (String referenceName : Sets.newHashSet(COUNT_MACHINE_UNIT, "productionInOneCycleUNIT")) {
            component = (FieldComponent) view.getComponentByReference(referenceName);
            if (component == null) {
                continue;
            }
            component.setFieldValue(unit);
            component.requestComponentUpdateState();
        }
    }
}
