package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.mes.basic.criteriaModifiers.AttributeValueCriteriaModifiers;
import com.qcadoo.mes.technologies.constants.WorkstationChangeoverNormChangeoverType;
import com.qcadoo.mes.technologies.constants.WorkstationChangeoverNormFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class WorkstationChangeoverDetailsHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        setFieldsRequired(view);
        setLookupsEnabledAndFilterValueHolders(view);
    }

    private void setFieldsRequired(final ViewDefinitionState view) {
        FieldComponent nameField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverNormFields.NAME);

        nameField.setRequired(true);
    }

    private void setLookupsEnabledAndFilterValueHolders(final ViewDefinitionState view) {
        LookupComponent workstationTypeLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverNormFields.WORKSTATION_TYPE);
        LookupComponent workstationLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverNormFields.WORKSTATION);
        LookupComponent attributeLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverNormFields.ATTRIBUTE);
        FieldComponent changeoverTypeField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverNormFields.CHANGEOVER_TYPE);
        LookupComponent fromAttributeValueLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverNormFields.FROM_ATTRIBUTE_VALUE);
        LookupComponent toAttributeValueLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverNormFields.TO_ATTRIBUTE_VALUE);

        Entity workstationType = workstationTypeLookup.getEntity();
        Entity workstation = workstationLookup.getEntity();
        Entity attribute = attributeLookup.getEntity();
        String changeoverType = (String) changeoverTypeField.getFieldValue();

        setWorkstationOrWorkstationTypeLookup(workstationLookup, workstationType);
        setWorkstationOrWorkstationTypeLookup(workstationTypeLookup, workstation);

        setAttributeValueLookup(fromAttributeValueLookup, attribute, changeoverType);
        setAttributeValueLookup(toAttributeValueLookup, attribute, changeoverType);
    }

    private void setWorkstationOrWorkstationTypeLookup(final LookupComponent lookupComponent, final Entity entity) {
        lookupComponent.setEnabled(Objects.isNull(entity));
        lookupComponent.requestComponentUpdateState();
    }

    private void setAttributeValueLookup(final LookupComponent lookupComponent, final Entity attribute, final String changeoverType) {
        FilterValueHolder filterValueHolder = lookupComponent.getFilterValue();

        boolean isEnabled = true;

        Object fieldValue = null;

        if (WorkstationChangeoverNormChangeoverType.BETWEEN_VALUES.getStringValue().equals(changeoverType)) {
            if (Objects.nonNull(attribute)) {
                fieldValue = lookupComponent.getFieldValue();

                filterValueHolder.put(AttributeValueCriteriaModifiers.L_ATTRIBUTE_ID, attribute.getId());
            } else {
                filterValueHolder.remove(AttributeValueCriteriaModifiers.L_ATTRIBUTE_ID);
            }
        } else {
            filterValueHolder.remove(AttributeValueCriteriaModifiers.L_ATTRIBUTE_ID);

            isEnabled = false;
        }

        lookupComponent.setEnabled(isEnabled);
        lookupComponent.setFieldValue(fieldValue);
        lookupComponent.setFilterValue(filterValueHolder);
        lookupComponent.requestComponentUpdateState();
    }

}
