package com.qcadoo.mes.view.components;

import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.types.HasManyType;
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

    private String fieldCode;

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
            } else if ("fieldCode".equals(option.getType())) {
                fieldCode = option.getValue();
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

            if (!value.isNull("selectedEntityCode")) {
                lookupData.setSelectedEntityCode(value.getString("selectedEntityCode"));
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

        LookupData lookupData = new LookupData();

        if (getSourceFieldPath() != null) {
            Entity contextEntity = selectedEntities.get(getSourceComponent().getPath());

            if (contextEntity != null) {
                lookupData.setContextEntityId(contextEntity.getId());
            }
        }

        boolean error = false;

        Entity selectedEntity = null;

        if (viewValue != null && viewValue.getValue() != null) {
            if (viewValue.getValue().getSelectedEntityId() != null) {
                selectedEntity = getDataDefinition().get(viewValue.getValue().getSelectedEntityId());
            } else if (StringUtils.hasText(viewValue.getValue().getSelectedEntityCode())) {
                String code = viewValue.getValue().getSelectedEntityCode();

                SearchCriteriaBuilder searchCriteriaBuilder = getDataDefinition().find().restrictedWith(
                        Restrictions.eq(getDataDefinition().getField(fieldCode), code + "*"));

                if (lookupData.getContextEntityId() != null) {
                    DataDefinition gridDataDefinition = getSourceComponent().getDataDefinition();
                    HasManyType hasManyType = getHasManyType(gridDataDefinition, getSourceFieldPath());

                    searchCriteriaBuilder.restrictedWith(Restrictions.belongsTo(
                            getDataDefinition().getField(hasManyType.getJoinFieldName()), lookupData.getContextEntityId()));
                }

                SearchResult results = searchCriteriaBuilder.list();

                if (results.getTotalNumberOfEntities() == 1) {
                    selectedEntity = results.getEntities().get(0);
                } else {
                    lookupData.setSelectedEntityCode(code);
                    error = true;
                }
            }
        }

        if (parentEntity != null && selectedEntity == null && !error && pathsToUpdate.isEmpty()) {
            selectedEntity = (Entity) getFieldValue(parentEntity, getFieldPath());
        }

        if (selectedEntity != null) {
            lookupData.setSelectedEntityValue(ExpressionUtil.getValue(selectedEntity, expression));
            lookupData.setSelectedEntityId(selectedEntity.getId());
            lookupData.setSelectedEntityCode(String.valueOf(selectedEntity.getField(fieldCode)));
            selectedEntities.put(getPath(), selectedEntity);
        }

        ViewValue<LookupData> newViewValue = new ViewValue<LookupData>(lookupData);

        if (error) {
            newViewValue.addErrorMessage(getTranslationService().translate("commons.validate.field.error.lookupCodeNotFound",
                    locale));
        }

        return newViewValue;
    }

    private HasManyType getHasManyType(final DataDefinition dataDefinition, final String fieldPath) {
        checkState(!fieldPath.matches("\\."), "Grid doesn't support sequential path");
        FieldDefinition fieldDefinition = dataDefinition.getField(fieldPath);
        if (fieldDefinition != null && fieldDefinition.getType() instanceof HasManyType) {
            return (HasManyType) fieldDefinition.getType();
        } else {
            throw new IllegalStateException("Grid data definition cannot be found");
        }
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
        windowComponent.addRawOption(new ComponentOption("fixedHeight", ImmutableMap.of("value", "true")));
        windowComponent.addRawOption(new ComponentOption("header", ImmutableMap.of("value", "false")));

        GridComponent gridComponent = new GridComponent("lookupGrid", windowComponent, null, sourceFieldPath,
                getTranslationService());

        addConstantsColumnToLookupGrid(gridComponent);

        for (ComponentOption rawOption : getRawOptions()) {
            gridComponent.addRawOption(rawOption);
        }
        gridComponent.addRawOption(new ComponentOption("isLookup", ImmutableMap.of("value", "true")));

        windowComponent.addComponent(gridComponent);

        addRibbonToLookupWindow(windowComponent);

        lookupViewDefinition.setRoot(windowComponent);

        windowComponent.initialize();

        viewDefinitionService.save(lookupViewDefinition);

        return lookupViewDefinition;
    }

    private void addRibbonToLookupWindow(final WindowComponent windowComponent) {
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
    }

    private void addConstantsColumnToLookupGrid(final GridComponent gridComponent) {
        Map<String, String> valueColumnOptions = new HashMap<String, String>();
        valueColumnOptions.put("name", "lookupValue");
        valueColumnOptions.put("expression", expression);
        valueColumnOptions.put("hidden", "true");
        gridComponent.addRawOption(new ComponentOption("column", valueColumnOptions));
        Map<String, String> codeColumnOptions = new HashMap<String, String>();
        codeColumnOptions.put("name", "lookupCode");
        codeColumnOptions.put("fields", fieldCode);
        codeColumnOptions.put("link", "true");
        gridComponent.addRawOption(new ComponentOption("column", codeColumnOptions));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long getSelectedEntityId(final ViewValue<Long> viewValue) {
        ViewValue<LookupData> value = (ViewValue<LookupData>) lookupViewValue(viewValue);
        return value.getValue().getSelectedEntityId();
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final Locale locale) {
        String codeBase = getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath()
                + ".label";
        List<String> messageCodes = new LinkedList<String>();
        messageCodes.add(codeBase);
        messageCodes.add(getTranslationService().getEntityFieldMessageCode(getParentContainer().getDataDefinition(), getName()));
        translationsMap.put(messageCodes.get(0), getTranslationService().translate(messageCodes, locale));

        List<String> focusMessageCodes = new LinkedList<String>();
        focusMessageCodes.add(codeBase + ".focus");
        focusMessageCodes.add(getTranslationService().getEntityFieldMessageCode(getParentContainer().getDataDefinition(),
                getName())
                + ".focus");
        translationsMap.put(focusMessageCodes.get(0), getTranslationService().translate(focusMessageCodes, locale));
    }

    public String getFieldCode() {
        return fieldCode;
    }

    public String getExpression() {
        return expression;
    }
}
