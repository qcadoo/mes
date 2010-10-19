package com.qcadoo.mes.view.components;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.utils.ExpressionUtil;
import com.qcadoo.mes.view.AbstractComponent;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ContainerComponent;
import com.qcadoo.mes.view.SelectableComponent;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.ViewValue;
import com.qcadoo.mes.view.containers.WindowComponent;
import com.qcadoo.mes.view.internal.ViewDefinitionImpl;
import com.qcadoo.mes.view.menu.ribbon.Ribbon;
import com.qcadoo.mes.view.menu.ribbon.RibbonActionItem;
import com.qcadoo.mes.view.menu.ribbon.RibbonActionItem.Type;
import com.qcadoo.mes.view.menu.ribbon.RibbonGroup;

public class LookupComponent extends AbstractComponent<LookupData> implements SelectableComponent {

    private int width;

    private int height;

    private String expression;

    public LookupComponent(final String name, final ContainerComponent<?> parent, final String fieldName,
            final String dataSource, final TranslationService translationService) {
        super(name, parent, fieldName, dataSource, translationService);
    }

    @Override
    public void initializeComponent() {
        for (ComponentOption option : getRawOptions()) {
            if ("width".equals(option.getType())) {
                width = Integer.parseInt(option.getValue());
                addOption("width", width);
            } else if ("height".equals(option.getType())) {
                height = Integer.parseInt(option.getValue());
                addOption("height", height);
            } else if ("expression".equals(option.getType())) {
                expression = option.getValue();
            }
        }
    }

    @Override
    public String getType() {
        return "lookupComponent";
    }

    @Override
    public ViewValue<LookupData> castComponentValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
            throws JSONException {
        LookupData lookupData = new LookupData();

        JSONObject value = viewObject.getJSONObject("value");

        if (value != null) {
            if (!value.isNull("selectedEntityId")) {
                String selectedEntityId = value.getString("selectedEntityId");

                if (selectedEntityId != null && !"null".equals(selectedEntityId)) {
                    Entity selectedEntity = getDataDefinition().get(Long.parseLong(selectedEntityId));
                    selectedEntities.put(getPath(), selectedEntity);
                    lookupData.setSelectedEntityId(Long.parseLong(selectedEntityId));
                }
            }

            if (!value.isNull("contextEntityId")) {
                String contextEntityId = value.getString("contextEntityId");

                if (contextEntityId != null && !"null".equals(contextEntityId)) {
                    lookupData.setContextEntityId(Long.parseLong(contextEntityId));
                }
            }
        }

        return new ViewValue<LookupData>(lookupData);
    }

    @Override
    public ViewValue<LookupData> getComponentValue(final Entity entity, final Entity parentEntity,
            final Map<String, Entity> selectedEntities, final ViewValue<LookupData> viewValue, final Set<String> pathsToUpdate,
            final Locale locale) {

        Entity selectedEntity = null;

        if (getSourceComponent() != null) {
            selectedEntity = (Entity) getFieldValue(selectedEntities.get(getSourceComponent().getPath()), getSourceFieldPath());
        } else if (getSourceFieldPath() != null) {
            selectedEntity = (Entity) getFieldValue(entity, getSourceFieldPath());
        } else {
            selectedEntity = (Entity) getFieldValue(entity, getFieldPath());
        }

        LookupData lookupData = new LookupData();

        if (selectedEntity != null) {
            lookupData.setSelectedEntityValue(ExpressionUtil.getValue(selectedEntity, expression));
            lookupData.setSelectedEntityId(selectedEntity.getId());
        }

        return new ViewValue<LookupData>(lookupData);
    }

    public ViewDefinition getLookupViewDefinition(final ViewDefinitionService viewDefinitionService) {
        String viewName = getViewDefinition().getName() + ".lookup." + getPath();

        ViewDefinition existingLookupViewDefinition = viewDefinitionService.get(getViewDefinition().getPluginIdentifier(),
                viewName);

        if (existingLookupViewDefinition != null) {
            return existingLookupViewDefinition;
        }

        ViewDefinitionImpl lookupViewDefinition = new ViewDefinitionImpl(getViewDefinition().getPluginIdentifier(), viewName);

        DataDefinition dataDefinition;
        String sourceFieldPath;

        if (getSourceComponent() != null) {
            dataDefinition = getSourceComponent().getDataDefinition();
            sourceFieldPath = getSourceFieldPath();
        } else {
            dataDefinition = getDataDefinition();
            sourceFieldPath = null;
        }

        WindowComponent windowComponent = new WindowComponent("mainWindow", dataDefinition, lookupViewDefinition,
                getTranslationService());

        GridComponent gridComponent = new GridComponent("lookupGrid", windowComponent, null, sourceFieldPath,
                getTranslationService());

        for (ComponentOption rawOption : getRawOptions()) {
            gridComponent.addRawOption(rawOption);
        }

        addHiddenColumnToLookupGrid(gridComponent);

        windowComponent.addComponent(gridComponent);

        addRibbonToLookupWindow(windowComponent);

        lookupViewDefinition.setRoot(windowComponent);

        windowComponent.initialize();

        System.out.println(" ---> lookup window " + windowComponent.toString());

        viewDefinitionService.save(lookupViewDefinition);

        return lookupViewDefinition;
    }

    private void addRibbonToLookupWindow(final WindowComponent windowComponent) {
        RibbonActionItem ribbonActionItem = new RibbonActionItem();
        ribbonActionItem.setName("select");
        ribbonActionItem.setAction("#{mainWindow.lookupGrid}.performLookupSelect");
        ribbonActionItem.setType(Type.BIG_BUTTON);

        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("navigation");
        ribbonGroup.addItem(ribbonActionItem);

        Ribbon ribbon = new Ribbon();
        ribbon.addGroup(ribbonGroup);

        windowComponent.setRibbon(ribbon);
    }

    private void addHiddenColumnToLookupGrid(final GridComponent gridComponent) {
        Map<String, String> hiddenColumnOptions = new HashMap<String, String>();
        hiddenColumnOptions.put("name", "lookupValue");
        hiddenColumnOptions.put("expression", expression);
        hiddenColumnOptions.put("hidden", "true");

        gridComponent.addRawOption(new ComponentOption("column", hiddenColumnOptions));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long getSelectedEntityId(final ViewValue<Long> viewValue) {
        ViewValue<LookupData> value = (ViewValue<LookupData>) lookupViewValue(viewValue);
        return value.getValue().getSelectedEntityId();
    }
}
