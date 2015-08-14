package com.qcadoo.mes.cmmsMachineParts;

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventContextFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.criteriaModifiers.EventCriteriaModifiersCMP;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.internal.components.form.FormComponentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class MaintenanceEventContextService {

    private static final String L_FORM = "form";

    private static final String L_GRID = "grid";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void confirmOrChangeContext(ViewDefinitionState view, ComponentState componentState, String[] args) {
        FormComponent formComponent = (FormComponentState) view.getComponentByReference(L_FORM);
        Entity maintenanceEventContextEntity = prepareContextEntity(formComponent.getEntity());

        if (maintenanceEventContextEntity.getBooleanField(MaintenanceEventContextFields.CONFIRMED)) {
            maintenanceEventContextEntity = changeContext(view, maintenanceEventContextEntity);
        } else {
            maintenanceEventContextEntity = confirmContext(view, maintenanceEventContextEntity);
        }
        formComponent.setEntity(maintenanceEventContextEntity);
    }

    private Entity changeContext(ViewDefinitionState view, Entity maintenanceEventContextEntity) {
        FormComponent formComponent = (FormComponentState) view.getComponentByReference(L_FORM);

        maintenanceEventContextEntity.setField(MaintenanceEventContextFields.CONFIRMED, false);
        maintenanceEventContextEntity = maintenanceEventContextEntity.getDataDefinition().save(maintenanceEventContextEntity);
        formComponent.setEntity(maintenanceEventContextEntity);

        return maintenanceEventContextEntity;
    }

    private Entity confirmContext(ViewDefinitionState view, Entity maintenanceEventContextEntity) {
        FormComponent formComponent = (FormComponentState) view.getComponentByReference(L_FORM);

        maintenanceEventContextEntity.setField(MaintenanceEventContextFields.CONFIRMED, true);
        maintenanceEventContextEntity = maintenanceEventContextEntity.getDataDefinition().save(maintenanceEventContextEntity);
        Long maintenanceEventContextEntityId = maintenanceEventContextEntity.getId();
        dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT).find().list().getEntities().stream().forEach(event -> {
            event.setField(MaintenanceEventFields.MAINTENANCE_EVENT_CONTEXT, maintenanceEventContextEntityId);
            event.getDataDefinition().save(event);
        });

        return maintenanceEventContextEntity;
    }

    private Entity prepareContextEntity(Entity maintenanceEventContextEntity) {
        SearchCriteriaBuilder searchCriteriaBuilder = maintenanceEventContextEntity.getDataDefinition().find();
        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(MaintenanceEventContextFields.DIVISION, maintenanceEventContextEntity.getBelongsToField(MaintenanceEventContextFields.DIVISION)));
        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(MaintenanceEventContextFields.FACTORY, maintenanceEventContextEntity.getBelongsToField(MaintenanceEventContextFields.FACTORY)));


        Entity maintenanceEventContextEntityFromDb = searchCriteriaBuilder.uniqueResult();

        if (maintenanceEventContextEntityFromDb == null) {
            maintenanceEventContextEntity.setField(MaintenanceEventContextFields.CONFIRMED, false);
            maintenanceEventContextEntity = maintenanceEventContextEntity.getDataDefinition().save(maintenanceEventContextEntity);
        } else {
            Long id = maintenanceEventContextEntity.getId();
            maintenanceEventContextEntity = maintenanceEventContextEntityFromDb;
            if (id == null) {
                maintenanceEventContextEntity.setField(MaintenanceEventContextFields.CONFIRMED, false);
            }
        }

        return maintenanceEventContextEntity;
    }

    public void beforeRenderListView(final ViewDefinitionState view) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(L_FORM);
        Entity maintenanceEventContext = formComponent.getEntity();

        if (maintenanceEventContext.getBooleanField(MaintenanceEventContextFields.CONFIRMED)) {
            prepareViewWithContext(view, maintenanceEventContext);
        } else {
            prepareViewWithEmptyContext(view, maintenanceEventContext);
        }
    }

    private void prepareViewWithContext(ViewDefinitionState view, Entity maintenanceEventContext) {
        setEnableOfRibbonActions(view, true);
        setEnableOfContextTab(view, false);
        setEnableOfMainTab(view, true);

        setGridFilterParameters(view, maintenanceEventContext);
    }

    private void prepareViewWithEmptyContext(ViewDefinitionState view, Entity maintenanceEventContext) {
        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);
        grid.setEntities(Arrays.asList());

        setEnableOfRibbonActions(view, false);
        setEnableOfContextTab(view, true);
        setEnableOfMainTab(view, false);
    }

    private void setEnableOfContextTab(ViewDefinitionState view, boolean enabled) {
        ComponentState contextTabComponent = view.getComponentByReference("contextTab");

        view.<FieldComponent>tryFindComponentByReference(MaintenanceEventContextFields.FACTORY).orNull().setEnabled(enabled);
        view.<FieldComponent>tryFindComponentByReference(MaintenanceEventContextFields.DIVISION).orNull().setEnabled(enabled);
    }

    private void setEnableOfMainTab(ViewDefinitionState view, boolean enabled) {
        ComponentState contextTabComponent = view.getComponentByReference("mainTab");
        view.getComponentByReference(L_GRID).setEnabled(enabled);
    }

    private Entity prepareFilteredEvents(Entity maintenanceEventContextEntity) {
        DataDefinition maintenanceEventDD = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT);

        SearchCriteriaBuilder searchCriteriaBuilder = maintenanceEventDD.find();

        Entity divisionEntity = maintenanceEventContextEntity.getBelongsToField(MaintenanceEventContextFields.DIVISION);
        if (divisionEntity != null) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(MaintenanceEventFields.DIVISION, divisionEntity));
        }

        Entity factoryEntity = maintenanceEventContextEntity.getBelongsToField(MaintenanceEventContextFields.FACTORY);
        if (factoryEntity != null) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(MaintenanceEventFields.FACTORY, factoryEntity));
        }

        maintenanceEventContextEntity.setField(MaintenanceEventContextFields.EVENTS, searchCriteriaBuilder.list().getEntities());

        return maintenanceEventContextEntity;
    }

    private void setEnableOfRibbonActions(ViewDefinitionState viewDefinitionState, boolean enabled) {
        WindowComponent window = (WindowComponent) viewDefinitionState.getComponentByReference("window");
        Ribbon ribbon = window.getRibbon();

        for (RibbonActionItem ribbonActionItem : ribbon.getGroupByName("customActions").getItems()) {
            ribbonActionItem.setEnabled(enabled);
            ribbonActionItem.requestUpdate(true);
        }
    }

    private void setGridFilterParameters(ViewDefinitionState view, Entity maintenanceEventContext) {
        GridComponent gridComponent = (GridComponent) view.getComponentByReference(L_GRID);

        FilterValueHolder filterValueHolder = gridComponent.getFilterValue();

        Entity factoryEntity = maintenanceEventContext.getBelongsToField(MaintenanceEventContextFields.FACTORY);
        if(factoryEntity != null) {
            filterValueHolder.put(EventCriteriaModifiersCMP.EVENT_CONTEXT_FILTER_PARAMETER_FACTORY, factoryEntity.getId());
        }

        Entity divisionEntity = maintenanceEventContext.getBelongsToField(MaintenanceEventContextFields.DIVISION);
        if(divisionEntity != null) {
            filterValueHolder.put(EventCriteriaModifiersCMP.EVENT_CONTEXT_FILTER_PARAMETER_DIVISION, divisionEntity.getId());
        }
        gridComponent.setFilterValue(filterValueHolder);
    }

    public void onSelectedEventChange(ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        DataDefinition maintenanceEventDD = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT);

        for (Entity eventEntity : grid.getSelectedEntities()) {
            eventEntity.setField(MaintenanceEventFields.MAINTENANCE_EVENT_CONTEXT, form.getEntityId());
            maintenanceEventDD.save(eventEntity);
        }
    }
}

