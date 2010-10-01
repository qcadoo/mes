package com.qcadoo.mes.core.xml;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.PluginManagementService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.beans.sample.CustomEntityService;
import com.qcadoo.mes.internal.ViewDefinitionServiceImpl;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.HookDefinition;
import com.qcadoo.mes.model.hooks.internal.HookFactory;
import com.qcadoo.mes.model.types.BelongsToType;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.types.internal.StringType;
import com.qcadoo.mes.view.AbstractComponent;
import com.qcadoo.mes.view.Component;
import com.qcadoo.mes.view.ContainerComponent;
import com.qcadoo.mes.view.RootComponent;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.components.CheckBoxComponent;
import com.qcadoo.mes.view.components.EntityComboBoxComponent;
import com.qcadoo.mes.view.components.GridComponent;
import com.qcadoo.mes.view.components.LinkButtonComponent;
import com.qcadoo.mes.view.components.TextInputComponent;
import com.qcadoo.mes.view.components.grid.ColumnAggregationMode;
import com.qcadoo.mes.view.containers.FormComponent;
import com.qcadoo.mes.view.containers.WindowComponent;
import com.qcadoo.mes.view.internal.ViewDefinitionParser;
import com.qcadoo.mes.view.menu.ribbon.Ribbon;
import com.qcadoo.mes.view.menu.ribbon.RibbonActionItem;
import com.qcadoo.mes.view.menu.ribbon.RibbonComboItem;
import com.qcadoo.mes.view.menu.ribbon.RibbonItem;

public class ViewDefinitionParserTest {

    private ViewDefinitionParser viewDefinitionParser;

    private DataDefinitionService dataDefinitionService;

    private ViewDefinitionService viewDefinitionService;

    private HookFactory hookFactory;

    private ApplicationContext applicationContext;

    private PluginManagementService pluginManagementService;

    private InputStream xml;

    private DataDefinition dataDefinitionA;

    private DataDefinition dataDefinitionB;

    @Before
    public void init() throws Exception {
        applicationContext = mock(ApplicationContext.class);
        dataDefinitionService = mock(DataDefinitionService.class);

        pluginManagementService = mock(PluginManagementService.class);

        viewDefinitionService = new ViewDefinitionServiceImpl();
        setField(viewDefinitionService, "pluginManagementService", pluginManagementService);

        hookFactory = new HookFactory();
        setField(hookFactory, "applicationContext", applicationContext);

        viewDefinitionParser = new ViewDefinitionParser();
        setField(viewDefinitionParser, "dataDefinitionService", dataDefinitionService);
        setField(viewDefinitionParser, "viewDefinitionService", viewDefinitionService);
        setField(viewDefinitionParser, "hookFactory", hookFactory);

        xml = new FileInputStream(new File("src/test/resources/view.xml"));

        PluginsPlugin plugin = new PluginsPlugin();
        plugin.setIdentifier("sample");

        given(pluginManagementService.getByIdentifierAndStatus("sample", "active")).willReturn(plugin);
        given(applicationContext.getBean(CustomEntityService.class)).willReturn(new CustomEntityService());

        dataDefinitionA = mock(DataDefinition.class);
        dataDefinitionB = mock(DataDefinition.class);
        FieldDefinition nameA = mock(FieldDefinition.class);
        FieldDefinition nameB = mock(FieldDefinition.class);
        FieldDefinition hasManyB = mock(FieldDefinition.class);
        FieldDefinition belongToA = mock(FieldDefinition.class);
        HasManyType hasManyBType = mock(HasManyType.class);
        BelongsToType belongToAType = mock(BelongsToType.class);

        given(nameA.getType()).willReturn(new StringType());
        given(nameB.getType()).willReturn(new StringType());
        given(hasManyB.getType()).willReturn(hasManyBType);
        given(belongToA.getType()).willReturn(belongToAType);
        given(hasManyBType.getDataDefinition()).willReturn(dataDefinitionB);
        given(belongToAType.getDataDefinition()).willReturn(dataDefinitionA);
        given(dataDefinitionA.getField("beansB")).willReturn(hasManyB);
        given(dataDefinitionA.getField("name")).willReturn(nameA);
        given(dataDefinitionB.getField("beanA")).willReturn(belongToA);
        given(dataDefinitionB.getField("name")).willReturn(nameB);
        given(dataDefinitionA.getName()).willReturn("beanA");
        given(dataDefinitionB.getName()).willReturn("beanB");
        given(dataDefinitionA.getFields()).willReturn(ImmutableMap.of("name", nameA, "beansB", hasManyB));
        given(dataDefinitionB.getFields()).willReturn(ImmutableMap.of("name", nameB, "beanA", belongToA));
        given(dataDefinitionService.get("sample", "beanA")).willReturn(dataDefinitionA);
        given(dataDefinitionService.get("sample", "beanB")).willReturn(dataDefinitionB);
    }

    @Test
    public void shouldParseXml() {
        // given
        ViewDefinition viewDefinition = parseAndGetViewDefinition();
        assertEquals(2, viewDefinitionService.list().size());

        // then
        assertNotNull(viewDefinition);
    }

    @Test
    public void shouldSetViewDefinitionAttributes() {
        // given
        ViewDefinition viewDefinition = parseAndGetViewDefinition();

        // then
        assertEquals("simpleView", viewDefinition.getName());
        assertEquals("sample", viewDefinition.getPluginIdentifier());
        assertThat(viewDefinition.getRoot(), instanceOf(WindowComponent.class));
    }

    @Test
    public void shouldHasRibbon() throws Exception {
        // given
        ViewDefinition viewDefinition = parseAndGetViewDefinition();
        Ribbon ribbon = (Ribbon) getField(viewDefinition.getRoot(), "ribbon");

        // then
        assertNotNull(ribbon);
        assertEquals(2, ribbon.getGroups().size());
        assertEquals("first", ribbon.getGroups().get(0).getName());
        assertEquals(2, ribbon.getGroups().get(0).getItems().size());
        assertEquals("second", ribbon.getGroups().get(1).getName());
        assertEquals(2, ribbon.getGroups().get(1).getItems().size());

        assertEquals("test", ribbon.getGroups().get(0).getItems().get(0).getName());
        assertNull(ribbon.getGroups().get(0).getItems().get(0).getIcon());
        assertEquals(RibbonItem.Type.BIG_BUTTON, ribbon.getGroups().get(0).getItems().get(0).getType());
        assertEquals("#{mainWindow.beanBForm}.save,#{mainWindow}.back", ((RibbonActionItem) ribbon.getGroups().get(0).getItems()
                .get(0)).getAction());
        assertEquals("test2", ribbon.getGroups().get(0).getItems().get(1).getName());
        assertEquals("icon2", ribbon.getGroups().get(0).getItems().get(1).getIcon());
        assertEquals(RibbonItem.Type.SMALL_BUTTON, ribbon.getGroups().get(0).getItems().get(1).getType());
        assertEquals("xxx", ((RibbonActionItem) ribbon.getGroups().get(0).getItems().get(1)).getAction());

        assertEquals("test2", ribbon.getGroups().get(1).getItems().get(0).getName());
        assertNull(ribbon.getGroups().get(1).getItems().get(0).getIcon());
        assertEquals(RibbonItem.Type.BIG_BUTTON, ribbon.getGroups().get(1).getItems().get(0).getType());
        assertNull(((RibbonActionItem) ribbon.getGroups().get(1).getItems().get(0)).getAction());

        assertEquals("combo1", ribbon.getGroups().get(1).getItems().get(1).getName());
        assertNull(ribbon.getGroups().get(1).getItems().get(1).getIcon());
        assertEquals(RibbonItem.Type.BIG_BUTTON, ribbon.getGroups().get(1).getItems().get(1).getType());

        List<RibbonActionItem> items = ((RibbonComboItem) ribbon.getGroups().get(1).getItems().get(1)).getItems();

        assertEquals("test1", items.get(0).getName());
        assertEquals(RibbonItem.Type.BIG_BUTTON, items.get(0).getType());
        assertEquals("yyy1", items.get(0).getAction());
        assertNull(items.get(0).getIcon());

        assertEquals("test2", items.get(1).getName());
        assertEquals(RibbonItem.Type.BIG_BUTTON, items.get(1).getType());
        assertEquals("yyy2", items.get(1).getAction());
        assertEquals("icon2", items.get(1).getIcon());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldSetFields() {
        // given
        ViewDefinition viewDefinition = parseAndGetViewDefinition();
        RootComponent root = viewDefinition.getRoot();

        // then
        checkComponent(root, WindowComponent.class, "mainWindow", "window", "beanB", null, null, null,
                Sets.<String> newHashSet(), 3, ImmutableMap.<String, Object> of("backButton", false, "header", true));

        checkComponent(root.lookupComponent("mainWindow.beanBForm"), FormComponent.class, "mainWindow.beanBForm", "form",
                "beanB", null, null, null, Sets.<String> newHashSet(), 7, ImmutableMap.<String, Object> of("header", false));

        checkComponent(root.lookupComponent("mainWindow.beanBForm.name"), TextInputComponent.class, "mainWindow.beanBForm.name",
                "textInput", "beanB", "name", null, null, Sets.<String> newHashSet(), 0, Maps.<String, Object> newHashMap());

        Component<?> selectBeanA = root.lookupComponent("mainWindow.beanBForm.selectBeanA");
        checkComponent(selectBeanA, EntityComboBoxComponent.class, "mainWindow.beanBForm.selectBeanA", "entityComboBox", "beanA",
                "beanA", null, null, Sets.newHashSet("mainWindow.beanBForm.beansBGrig", "mainWindow.beansBGrig"), 0,
                Maps.<String, Object> newHashMap());

        assertTrue(selectBeanA.isDefaultEnabled());
        assertTrue(selectBeanA.isDefaultVisible());

        Component<?> active = root.lookupComponent("mainWindow.beanBForm.active");
        checkComponent(active, CheckBoxComponent.class, "mainWindow.beanBForm.active", "checkBox", "beanB", "name", null, null,
                Sets.<String> newHashSet(), 0, Maps.<String, Object> newHashMap());

        assertFalse(active.isDefaultEnabled());
        assertFalse(active.isDefaultVisible());

        checkComponent(root.lookupComponent("mainWindow.beanBForm.beanM"), TextInputComponent.class,
                "mainWindow.beanBForm.beanM", "textInput", "beanB", "name", null, null, Sets.<String> newHashSet(), 0,
                Maps.<String, Object> newHashMap());

        checkComponent(root.lookupComponent("mainWindow.beanBForm.beanB"), TextInputComponent.class,
                "mainWindow.beanBForm.beanB", "textInput", "beanA", "beanA.name", null, null, Sets.<String> newHashSet(), 0,
                Maps.<String, Object> newHashMap());

        checkComponent(root.lookupComponent("mainWindow.beanBForm.beanAForm"), FormComponent.class,
                "mainWindow.beanBForm.beanAForm", "form", "beanA", "beanA", null, null, Sets.<String> newHashSet(), 1,
                Maps.<String, Object> newHashMap());

        checkComponent(root.lookupComponent("mainWindow.beanBForm.beanAForm.name"), TextInputComponent.class,
                "mainWindow.beanBForm.beanAForm.name", "textInput", "beanA", "name", null, null, Sets.<String> newHashSet(), 0,
                Maps.<String, Object> newHashMap());

        GridComponent grid = (GridComponent) root.lookupComponent("mainWindow.beanBForm.beansBGrig");
        checkComponent(grid, GridComponent.class, "mainWindow.beanBForm.beansBGrig", "grid", "beanB", null,
                "mainWindow.beanBForm.selectBeanA", "beansB", Sets.<String> newHashSet(), 0, Maps.<String, Object> newHashMap());

        assertThat((List<String>) grid.getOptions().get("columns"), hasItems("name"));
        assertThat((List<String>) grid.getOptions().get("fields"), hasItems("name", "beanA"));
        assertEquals("products/form", grid.getOptions().get("correspondingViewName"));
        assertTrue((Boolean) grid.getOptions().get("header"));
        assertFalse((Boolean) grid.getOptions().get("sortable"));
        assertFalse((Boolean) grid.getOptions().get("filter"));
        assertFalse((Boolean) grid.getOptions().get("multiselect"));
        assertFalse((Boolean) grid.getOptions().get("paginable"));
        assertFalse(grid.getOptions().containsKey("height"));
        assertTrue((Boolean) grid.getOptions().get("canDelete"));
        assertTrue((Boolean) grid.getOptions().get("canNew"));
        assertTrue(((Set<FieldDefinition>) getField(grid, "orderableFields")).isEmpty());
        assertTrue(((Set<FieldDefinition>) getField(grid, "searchableFields")).isEmpty());
        assertEquals("name", grid.getColumns().get(0).getName());
        assertEquals(ColumnAggregationMode.NONE, grid.getColumns().get(0).getAggregationMode());
        assertNull(grid.getColumns().get(0).getExpression());
        assertNull(grid.getColumns().get(0).getWidth());
        assertEquals(1, grid.getColumns().get(0).getFields().size());
        assertThat(grid.getColumns().get(0).getFields(), hasItems(dataDefinitionB.getField("name")));

        GridComponent grid2 = (GridComponent) root.lookupComponent("mainWindow.beansBGrig");
        checkComponent(grid2, GridComponent.class, "mainWindow.beansBGrig", "grid", "beanB", null,
                "mainWindow.beanBForm.selectBeanA", "beansB", Sets.<String> newHashSet(), 0, Maps.<String, Object> newHashMap());

        assertThat((List<String>) grid2.getOptions().get("columns"), hasItems("multicolumn"));
        assertThat((List<String>) grid2.getOptions().get("fields"), hasItems("name", "beanA"));
        assertEquals("products/form", grid2.getOptions().get("correspondingViewName"));
        assertTrue((Boolean) grid2.getOptions().get("header"));
        assertTrue((Boolean) grid2.getOptions().get("sortable"));
        assertTrue((Boolean) grid2.getOptions().get("filter"));
        assertTrue((Boolean) grid2.getOptions().get("multiselect"));
        assertTrue((Boolean) grid2.getOptions().get("paginable"));
        assertFalse((Boolean) grid2.getOptions().get("canDelete"));
        assertFalse((Boolean) grid2.getOptions().get("canNew"));
        assertEquals(Integer.valueOf(450), grid2.getOptions().get("height"));
        assertEquals(1, ((Set<FieldDefinition>) getField(grid2, "orderableFields")).size());
        assertThat(((Set<FieldDefinition>) getField(grid2, "orderableFields")), hasItems(dataDefinitionB.getField("name")));
        assertEquals(2, ((Set<FieldDefinition>) getField(grid2, "searchableFields")).size());
        assertThat(((Set<FieldDefinition>) getField(grid2, "searchableFields")),
                hasItems(dataDefinitionB.getField("name"), dataDefinitionB.getField("beanA")));
        assertEquals("multicolumn", grid2.getColumns().get(0).getName());
        assertEquals(ColumnAggregationMode.SUM, grid2.getColumns().get(0).getAggregationMode());
        assertEquals("2 + 2", grid2.getColumns().get(0).getExpression());
        assertEquals(Integer.valueOf(20), grid2.getColumns().get(0).getWidth());
        assertEquals(2, grid2.getColumns().get(0).getFields().size());
        assertThat(grid2.getColumns().get(0).getFields(),
                hasItems(dataDefinitionB.getField("name"), dataDefinitionB.getField("beanA")));

        checkComponent(root.lookupComponent("mainWindow.link"), LinkButtonComponent.class, "mainWindow.link", "linkButton",
                "beanB", null, null, null, Sets.<String> newHashSet(), 0,
                ImmutableMap.<String, Object> of("url", "download.html"));
    }

    private void checkComponent(final Component<?> component2, final Class<?> clazz, final String path, final String type,
            final String model, final String fieldPath, final String sourceComponentPath, final String sourceFieldPath,
            final Set<String> listeners, final int children, final Map<String, Object> options) {
        AbstractComponent<?> component = (AbstractComponent<?>) component2;
        String name = path.split("\\.")[path.split("\\.").length - 1];
        assertNotNull(component);
        assertThat(component, instanceOf(clazz));
        assertEquals(name, component.getName());
        assertEquals(fieldPath, component.getFieldPath());
        assertEquals(listeners.size(), component.getListeners().size());
        if (listeners.size() > 0) {
            assertThat(component.getListeners(), JUnitMatchers.hasItems(listeners.toArray(new String[listeners.size()])));
        }
        assertEquals(path, component.getPath());
        assertEquals(sourceFieldPath, component.getSourceFieldPath());
        if (StringUtils.hasText(sourceComponentPath)) {
            assertEquals(sourceComponentPath, ((Component<?>) getField(component, "sourceComponent")).getPath());
        } else {
            assertNull(getField(component, "sourceComponent"));
        }

        assertEquals(model, component.getDataDefinition().getName());
        assertEquals(type, component.getType());
        assertEquals("simpleView", component.getViewDefinition().getName());

        if (component instanceof ContainerComponent) {
            assertEquals(children, ((ContainerComponent<?>) component).getComponents().size());
        }

        for (Map.Entry<String, Object> option : options.entrySet()) {
            assertEquals(option.getValue(), component.getOptions().get(option.getKey()));
        }
    }

    @Test
    public void shouldSetHooks() {
        // given
        ViewDefinition viewDefinition = parseAndGetViewDefinition();

        // then
        testHookDefinition(viewDefinition, "viewHook", CustomEntityService.class, "onView");
    }

    private void testHookDefinition(final Object object, final String hookFieldName, final Class<?> hookBeanClass,
            final String hookMethodName) {
        HookDefinition hook = (HookDefinition) getField(object, hookFieldName);

        assertNotNull(hook);
        assertThat(getField(hook, "bean"), instanceOf(hookBeanClass));
        assertEquals(hookMethodName, getField(hook, "methodName"));
    }

    private ViewDefinition parseAndGetViewDefinition() {
        viewDefinitionParser.parse(xml);
        return viewDefinitionService.get("sample", "simpleView");
    }

}
