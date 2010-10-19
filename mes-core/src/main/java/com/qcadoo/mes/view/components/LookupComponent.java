package com.qcadoo.mes.view.components;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ContainerComponent;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.containers.WindowComponent;
import com.qcadoo.mes.view.internal.ViewDefinitionImpl;
import com.qcadoo.mes.view.menu.ribbon.Ribbon;
import com.qcadoo.mes.view.menu.ribbon.RibbonActionItem;
import com.qcadoo.mes.view.menu.ribbon.RibbonActionItem.Type;
import com.qcadoo.mes.view.menu.ribbon.RibbonGroup;

public class LookupComponent extends SimpleFieldComponent {

    private int width;

    private int height;

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
            }
        }
    }

    @Override
    public String getType() {
        return "lookupComponent";
    }

    @Override
    public String convertToViewValue(final Object value) {
        return String.valueOf(value).trim();
    }

    @Override
    public Object convertToDatabaseValue(final String value) {
        return value;
    }

    public ViewDefinition getLookupViewDefinition(final ViewDefinitionService viewDefinitionService) {

        String viewName = getViewDefinition().getName() + "Lookup_" + getPath().replaceAll("\\.", "_");

        ViewDefinition existingLookupViewDefinition = viewDefinitionService.get(getViewDefinition().getPluginIdentifier(),
                viewName);

        if (existingLookupViewDefinition != null) {
            return existingLookupViewDefinition;
        }

        ViewDefinitionImpl lookupViewDefinition = new ViewDefinitionImpl(getViewDefinition().getPluginIdentifier(), viewName);

        DataDefinition dataDefinition = null;

        System.out.println(" ---> path " + getPath());

        if (getSourceComponent() != null) {
            System.out.println(" ---> source component " + getSourceComponent().getName() + ", "
                    + getSourceComponent().getDataDefinition().getName());
            dataDefinition = getSourceComponent().getDataDefinition();
        } else {
            System.out.println(" ---> parent component " + getParentContainer().getName() + ", "
                    + getParentContainer().getDataDefinition().getName());
            dataDefinition = getParentContainer().getDataDefinition();
        }

        WindowComponent windowComponent = new WindowComponent("mainWindow", dataDefinition, lookupViewDefinition,
                getTranslationService());

        GridComponent gridComponent = new GridComponent("lookupGrid", windowComponent, getFieldPath(), getSourceFieldPath(),
                getTranslationService());

        for (ComponentOption rawOption : getRawOptions()) {
            System.out.println(" ---> option " + rawOption.getType() + " -> " + rawOption.getValue());
            gridComponent.addRawOption(rawOption);
        }
        gridComponent.addRawOption(new ComponentOption("isLookup", ImmutableMap.of("value", "true")));

        windowComponent.addComponent(gridComponent);

        RibbonActionItem ribbonActionItem = new RibbonActionItem();
        ribbonActionItem.setName("select");
        ribbonActionItem.setAction("#{mainWindow.lookupGrid}.performLookupSelect; #{mainWindow}.performClose");
        ribbonActionItem.setType(Type.BIG_BUTTON);

        RibbonActionItem ribbonCancelActionItem = new RibbonActionItem();
        ribbonCancelActionItem.setName("cancel");
        ribbonCancelActionItem.setAction("#{mainWindow}.performClose");
        ribbonCancelActionItem.setType(Type.BIG_BUTTON);

        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("navigation");
        ribbonGroup.addItem(ribbonActionItem);
        ribbonGroup.addItem(ribbonCancelActionItem);

        Ribbon ribbon = new Ribbon();
        ribbon.addGroup(ribbonGroup);

        windowComponent.setRibbon(ribbon);

        lookupViewDefinition.setRoot(windowComponent);

        windowComponent.initialize();

        System.out.println(" ---> lookup window " + windowComponent.toString());

        viewDefinitionService.save(lookupViewDefinition);

        return lookupViewDefinition;
    }
}
