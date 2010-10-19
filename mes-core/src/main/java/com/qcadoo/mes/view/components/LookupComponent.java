package com.qcadoo.mes.view.components;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.ContainerComponent;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.containers.WindowComponent;
import com.qcadoo.mes.view.internal.ViewDefinitionImpl;
import com.qcadoo.mes.view.menu.ribbon.Ribbon;
import com.qcadoo.mes.view.menu.ribbon.RibbonActionItem;
import com.qcadoo.mes.view.menu.ribbon.RibbonGroup;

public class LookupComponent extends SimpleFieldComponent {

    public LookupComponent(final String name, final ContainerComponent<?> parent, final String fieldName,
            final String dataSource, final TranslationService translationService) {
        super(name, parent, fieldName, dataSource, translationService);
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

    public ViewDefinition getLookupViewDefinition() {
        ViewDefinitionImpl lookupViewDefinition = new ViewDefinitionImpl(getViewDefinition().getPluginIdentifier(),
                getViewDefinition().getName() + "LookupFor" + getName());
        WindowComponent windowComponent = new WindowComponent("mainWindow", getParentContainer().getDataDefinition(),
                lookupViewDefinition, getTranslationService());

        GridComponent gridComponent = new GridComponent("lookupGrid", windowComponent, getFieldPath(), getSourceFieldPath(),
                getTranslationService());

        windowComponent.addComponent(gridComponent);

        RibbonActionItem ribbonActionItem = new RibbonActionItem();
        ribbonActionItem.setName("select");
        ribbonActionItem.setAction("#{mainWindow}.performLookupSelect");

        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("navigation");
        ribbonGroup.addItem(ribbonActionItem);

        Ribbon ribbon = new Ribbon();
        ribbon.addGroup(ribbonGroup);

        windowComponent.setRibbon(ribbon);

        lookupViewDefinition.setRoot(windowComponent);

        windowComponent.initialize();

        System.out.println(" ---> lookup window " + windowComponent.toString());

        return lookupViewDefinition;
    }

}
