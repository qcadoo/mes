/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.view.xml;

import java.io.InputStream;

import org.springframework.context.ApplicationContext;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.hooks.internal.HookFactory;
import com.qcadoo.mes.view.internal.ViewComponentsResolver;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;

public class ViewDefinitionParserImplTest {

    private ViewDefinitionParserImpl viewDefinitionParser;

    private DataDefinitionService dataDefinitionService;

    private ViewDefinitionService viewDefinitionService;

    private HookFactory hookFactory;

    private ApplicationContext applicationContext;

    private InputStream xml;

    private DataDefinition dataDefinitionA;

    private DataDefinition dataDefinitionB;

    private TranslationService translationService;

    private static ViewComponentsResolver viewComponentsResolver;

    // @BeforeClass
    // public static void initClass() throws Exception {
    // viewComponentsResolver = new ViewComponentsResolver();
    // viewComponentsResolver.refreshAvailableComponentsList();
    // }
    //
    // @Before
    // public void init() throws Exception {
    // applicationContext = mock(ApplicationContext.class);
    // dataDefinitionService = mock(DataDefinitionService.class);
    //
    // translationService = mock(TranslationService.class);
    //
    // viewDefinitionService = new ViewDefinitionServiceImpl();
    //
    // hookFactory = new HookFactory();
    // setField(hookFactory, "applicationContext", applicationContext);
    //
    // viewDefinitionParser = new ViewDefinitionParserImpl();
    // setField(viewDefinitionParser, "dataDefinitionService", dataDefinitionService);
    // setField(viewDefinitionParser, "viewDefinitionService", viewDefinitionService);
    // setField(viewDefinitionParser, "hookFactory", hookFactory);
    // setField(viewDefinitionParser, "translationService", translationService);
    // setField(viewDefinitionParser, "viewComponentsResolver", viewComponentsResolver);
    //
    // xml = new FileInputStream(new File("src/test/resources/view/test.xml"));
    //
    // given(applicationContext.getBean(CustomEntityService.class)).willReturn(new CustomEntityService());
    //
    // dataDefinitionA = mock(DataDefinition.class);
    // dataDefinitionB = mock(DataDefinition.class);
    // FieldDefinition nameA = mock(FieldDefinition.class);
    // FieldDefinition nameB = mock(FieldDefinition.class);
    // FieldDefinition hasManyB = mock(FieldDefinition.class);
    // FieldDefinition belongToA = mock(FieldDefinition.class);
    // HasManyType hasManyBType = mock(HasManyType.class);
    // BelongsToType belongToAType = mock(BelongsToType.class);
    //
    // given(nameA.getType()).willReturn(new StringType());
    // given(nameB.getType()).willReturn(new StringType());
    // given(hasManyB.getType()).willReturn(hasManyBType);
    // given(belongToA.getType()).willReturn(belongToAType);
    // given(hasManyBType.getDataDefinition()).willReturn(dataDefinitionB);
    // given(belongToAType.getDataDefinition()).willReturn(dataDefinitionA);
    // given(dataDefinitionA.getField("beansB")).willReturn(hasManyB);
    // given(dataDefinitionA.getField("name")).willReturn(nameA);
    // given(dataDefinitionB.getField("active")).willReturn(nameA);
    // given(dataDefinitionB.getField("beanA")).willReturn(belongToA);
    // given(dataDefinitionB.getField("beanM")).willReturn(belongToA);
    // given(dataDefinitionB.getField("beanB")).willReturn(belongToA);
    // given(dataDefinitionB.getField("name")).willReturn(nameB);
    // given(dataDefinitionA.getName()).willReturn("beanA");
    // given(dataDefinitionB.getName()).willReturn("beanB");
    // given(dataDefinitionA.getFields()).willReturn(ImmutableMap.of("name", nameA, "beansB", hasManyB));
    // given(dataDefinitionB.getFields()).willReturn(ImmutableMap.of("name", nameB, "beanA", belongToA));
    // given(dataDefinitionService.get("sample", "beanA")).willReturn(dataDefinitionA);
    // given(dataDefinitionService.get("sample", "beanB")).willReturn(dataDefinitionB);
    // }
    //
    // @Test
    // public void shouldParseXml() {
    // // given
    // ViewDefinition viewDefinition = parseAndGetViewDefinition();
    // assertEquals(2, viewDefinitionService.list().size());
    //
    // // then
    // assertNotNull(viewDefinition);
    // }
    //
    // @Test
    // public void shouldSetViewDefinitionAttributes() {
    // // given
    // ViewDefinition viewDefinition = parseAndGetViewDefinition();
    //
    // // then
    // assertEquals("simpleView", viewDefinition.getName());
    // assertEquals("sample", viewDefinition.getPluginIdentifier());
    // assertThat(viewDefinition.getComponentByReference("mainWindow"), instanceOf(WindowComponentPattern.class));
    // }
    //
    // @SuppressWarnings("unchecked")
    // @Test
    // public void shouldHasRibbon() throws Exception {
    // // given
    // ViewDefinition viewDefinition = parseAndGetViewDefinition();
    // TranslationService translationService = mock(TranslationService.class);
    // setField(viewDefinition, "translationService", translationService);
    //
    // JSONObject jsOptions = (JSONObject) ((Map<String, Map<String, Object>>) viewDefinition.prepareView(new JSONObject(),
    // Locale.ENGLISH).get("components")).get("mainWindow").get("jsOptions");
    //
    // JSONObject ribbon = jsOptions.getJSONObject("ribbon");
    //
    // // then
    // assertNotNull(ribbon);
    // assertEquals(2, ribbon.getJSONArray("groups").length());
    // assertEquals("first", ribbon.getJSONArray("groups").getJSONObject(0).getString("name"));
    // assertEquals(2, ribbon.getJSONArray("groups").getJSONObject(0).getJSONArray("items").length());
    // assertEquals("second", ribbon.getJSONArray("groups").getJSONObject(1).getString("name"));
    // assertEquals(2, ribbon.getJSONArray("groups").getJSONObject(1).getJSONArray("items").length());
    //
    // JSONObject item11 = ribbon.getJSONArray("groups").getJSONObject(0).getJSONArray("items").getJSONObject(0);
    //
    // assertEquals("test", item11.getString("name"));
    // assertFalse(item11.has("icon"));
    // assertEquals(RibbonActionItem.Type.BIG_BUTTON.toString(), item11.getString("type"));
    // // assertEquals("#{mainWindow.beanBForm}.save,#{mainWindow}.back", item11.getString("clickAction"));
    //
    // JSONObject item12 = ribbon.getJSONArray("groups").getJSONObject(0).getJSONArray("items").getJSONObject(1);
    //
    // assertEquals("test2", item12.getString("name"));
    // assertEquals("icon2", item12.getString("icon"));
    // assertEquals(RibbonActionItem.Type.SMALL_BUTTON.toString(), item12.getString("type"));
    // assertEquals("xxx", item12.getString("clickAction"));
    //
    // JSONObject item21 = ribbon.getJSONArray("groups").getJSONObject(1).getJSONArray("items").getJSONObject(0);
    //
    // assertEquals("test2", item21.getString("name"));
    // assertFalse(item21.has("icon"));
    // assertEquals(RibbonActionItem.Type.BIG_BUTTON.toString(), item21.getString("type"));
    // assertFalse(item21.has("clickAction"));
    //
    // JSONObject item22 = ribbon.getJSONArray("groups").getJSONObject(1).getJSONArray("items").getJSONObject(1);
    //
    // assertEquals("combo1", item22.getString("name"));
    // assertFalse(item22.has("icon"));
    // assertEquals(RibbonActionItem.Type.BIG_BUTTON.toString(), item22.getString("type"));
    // assertEquals("yyy3", item22.getString("clickAction"));
    // assertEquals(2, item22.getJSONArray("items").length());
    //
    // JSONObject item221 = item22.getJSONArray("items").getJSONObject(0);
    //
    // assertEquals("test1", item221.getString("name"));
    // assertFalse(item221.has("icon"));
    // assertEquals(RibbonActionItem.Type.BIG_BUTTON.toString(), item221.getString("type"));
    // assertEquals("yyy1", item221.getString("clickAction"));
    //
    // JSONObject item222 = item22.getJSONArray("items").getJSONObject(1);
    //
    // assertEquals("test2", item222.getString("name"));
    // assertEquals("icon2", item222.getString("icon"));
    // assertEquals(RibbonActionItem.Type.BIG_BUTTON.toString(), item222.getString("type"));
    // assertEquals("yyy2", item222.getString("clickAction"));
    // }
    //
    // @Test
    // public void shouldSetFields() {
    // // given
    // ViewDefinition vd = parseAndGetViewDefinition();
    //
    // // then
    // checkComponent(vd.getComponentByReference("mainWindow"), null, WindowComponentPattern.class, "mainWindow", "beanB");
    //
    // checkComponent(vd.getComponentByReference("mainWindow.beanBForm"), vd.getComponentByReference("mainWindow"),
    // FormComponentPattern.class, "beanBForm", "beanB");
    //
    // checkComponent(vd.getComponentByReference("referenceToTextarea"), vd.getComponentByReference("mainWindow"),
    // TextAreaComponentPattern.class, "name", "beanB");
    //
    // checkComponent(vd.getComponentByReference("mainWindow.beanBForm.active"),
    // vd.getComponentByReference("mainWindow.beanBForm"), CheckBoxComponentPattern.class, "active", "beanB");
    //
    // checkComponent(vd.getComponentByReference("mainWindow.beanBForm.selectBeanA"),
    // vd.getComponentByReference("mainWindow.beanBForm"), TextInputComponentPattern.class, "selectBeanA", "beanA");
    //
    // checkComponent(vd.getComponentByReference("mainWindow.beanBForm.beanM"),
    // vd.getComponentByReference("mainWindow.beanBForm"), TextAreaComponentPattern.class, "beanM", "beanB");
    //
    // checkComponent(vd.getComponentByReference("mainWindow.beanBForm.beansBGrig"),
    // vd.getComponentByReference("mainWindow.beanBForm"), GridComponentPattern.class, "beansBGrig", "beanB");
    //
    // checkComponent(vd.getComponentByReference("mainWindow.beanBForm.beanAForm"),
    // vd.getComponentByReference("mainWindow.beanBForm"), FormComponentPattern.class, "beanAForm", "beanA");
    //
    // checkComponent(vd.getComponentByReference("mainWindow.beanBForm.beanAForm.name"),
    // vd.getComponentByReference("mainWindow.beanBForm.beanAForm"), TextInputComponentPattern.class, "name", "beanA");
    //
    // checkComponent(vd.getComponentByReference("mainWindow.beansBGrig"), vd.getComponentByReference("mainWindow"),
    // GridComponentPattern.class, "beansBGrig", "beanB");
    //
    // checkComponent(vd.getComponentByReference("mainWindow.link"), vd.getComponentByReference("mainWindow"),
    // ButtonComponentPattern.class, "link", "beanB");
    //
    // checkFieldListener(vd.getComponentByReference("mainWindow.beanBForm"), vd.getComponentByReference("referenceToTextarea"),
    // "name");
    // checkFieldListener(vd.getComponentByReference("mainWindow.beanBForm"),
    // vd.getComponentByReference("mainWindow.beanBForm.active"), "active");
    // checkFieldListener(vd.getComponentByReference("mainWindow.beanBForm"),
    // vd.getComponentByReference("mainWindow.beanBForm.beanAForm"), "beanA");
    // checkFieldListener(vd.getComponentByReference("mainWindow.beanBForm"),
    // vd.getComponentByReference("mainWindow.beanBForm.beanM"), "beanM");
    // //
    // // checkComponent(root, WindowComponent.class, "mainWindow", "window", "beanB", null, null, null,
    // // Sets.<String> newHashSet(), 3, ImmutableMap.<String, Object> of("backButton", false, "header", true));
    // //
    // // checkComponent(vd.getComponentByReference("mainWindow.beanBForm"), FormComponent.class, "mainWindow.beanBForm", "form",
    // // "beanB", null, null, null, Sets.<String> newHashSet(), 7, ImmutableMap.<String, Object> of("header", false));
    // //
    // // checkComponent(vd.getComponentByReference("mainWindow.beanBForm.name"), TextAreaComponent.class,
    // // "mainWindow.beanBForm.name",
    // // "textArea", "beanB", "name", null, null, Sets.<String> newHashSet(), 0, Maps.<String, Object> newHashMap());
    // //
    // // ComponentPattern selectBeanA = vd.getComponentByReference("mainWindow.beanBForm.selectBeanA");
    // // checkComponent(selectBeanA, LookupComponent.class, "mainWindow.beanBForm.selectBeanA", "lookupComponent", "beanA",
    // // "beanA", null, null, Sets.newHashSet("mainWindow.beanBForm.beansBGrig", "mainWindow.beansBGrig"), 0,
    // // Maps.<String, Object> newHashMap());
    // //
    // // assertTrue(selectBeanA.isDefaultEnabled());
    // // assertTrue(selectBeanA.isDefaultVisible());
    // //
    // // ComponentPattern active = vd.getComponentByReference("mainWindow.beanBForm.active");
    // // checkComponent(active, CheckBoxComponent.class, "mainWindow.beanBForm.active", "checkBox", "beanB", "name", null, null,
    // // Sets.<String> newHashSet(), 0, Maps.<String, Object> newHashMap());
    // //
    // // assertFalse(active.isDefaultEnabled());
    // // assertFalse(active.isDefaultVisible());
    // //
    // // checkComponent(vd.getComponentByReference("mainWindow.beanBForm.beanM"), TextAreaComponent.class,
    // // "mainWindow.beanBForm.beanM", "textArea", "beanB", "name", null, null, Sets.<String> newHashSet(), 0,
    // // Maps.<String, Object> newHashMap());
    // //
    // // checkComponent(vd.getComponentByReference("mainWindow.beanBForm.beanB"), TextAreaComponent.class,
    // // "mainWindow.beanBForm.beanB", "textArea", "beanA", "beanA.name", null, null, Sets.<String> newHashSet(), 0,
    // // Maps.<String, Object> newHashMap());
    // //
    // // checkComponent(vd.getComponentByReference("mainWindow.beanBForm.beanAForm"), FormComponent.class,
    // // "mainWindow.beanBForm.beanAForm", "form", "beanA", "beanA", null, null, Sets.<String> newHashSet(), 1,
    // // Maps.<String, Object> newHashMap());
    // //
    // // checkComponent(vd.getComponentByReference("mainWindow.beanBForm.beanAForm.name"), TextAreaComponent.class,
    // // "mainWindow.beanBForm.beanAForm.name", "textArea", "beanA", "name", null, null, Sets.<String> newHashSet(), 0,
    // // Maps.<String, Object> newHashMap());
    // //
    // // GridComponentPattern grid = (GridComponentPattern) vd.getComponentByReference("mainWindow.beanBForm.beansBGrig");
    // // checkComponent(grid, GridComponentPattern.class, "mainWindow.beanBForm.beansBGrig", "grid", "beanB", null,
    // // "mainWindow.beanBForm.selectBeanA", "beansB", Sets.<String> newHashSet(), 0, Maps.<String, Object> newHashMap());
    // // List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>(grid.getColumns());
    // //
    // // // assertThat((List<ColumnDefinition>) grid.getOptions().get("columns"), hasItems("name"));
    // // assertThat((List<String>) grid.getOptions().get("fields"), hasItems("name", "beanA"));
    // // assertEquals("products/form", grid.getOptions().get("correspondingViewName"));
    // // assertTrue((Boolean) grid.getOptions().get("header"));
    // // assertFalse((Boolean) grid.getOptions().get("sortable"));
    // // assertFalse((Boolean) grid.getOptions().get("filter"));
    // // assertFalse((Boolean) grid.getOptions().get("multiselect"));
    // // assertFalse((Boolean) grid.getOptions().get("paginable"));
    // // assertFalse(grid.getOptions().containsKey("height"));
    // // assertFalse((Boolean) grid.getOptions().get("canDelete"));
    // // assertFalse((Boolean) grid.getOptions().get("canNew"));
    // // assertTrue(((Set<ColumnDefinition>) getField(grid, "orderableColumns")).isEmpty());
    // // assertTrue(((Set<FieldDefinition>) getField(grid, "searchableFields")).isEmpty());
    // // assertEquals("name", columns.get(0).getName());
    // // assertEquals(ColumnAggregationMode.NONE, columns.get(0).getAggregationMode());
    // // assertNull(columns.get(0).getExpression());
    // // assertNull(columns.get(0).getWidth());
    // // assertEquals(1, columns.get(0).getFields().size());
    // // assertThat(columns.get(0).getFields(), hasItems(dataDefinitionB.getField("name")));
    // //
    // // GridComponentPattern grid2 = (GridComponentPattern) vd.getComponentByReference("mainWindow.beansBGrig");
    // // checkComponent(grid2, GridComponentPattern.class, "mainWindow.beansBGrig", "grid", "beanB", null,
    // // "mainWindow.beanBForm.selectBeanA", "beansB", Sets.<String> newHashSet(), 0, Maps.<String, Object> newHashMap());
    // // List<ColumnDefinition> columns2 = new ArrayList<ColumnDefinition>(grid2.getColumns());
    // //
    // // // assertThat((List<String>) grid2.getOptions().get("columns"), hasItems("name", "multicolumn"));
    // // assertThat((List<String>) grid2.getOptions().get("fields"), hasItems("name", "beanA"));
    // // assertEquals("products/form", grid2.getOptions().get("correspondingViewName"));
    // // assertTrue((Boolean) grid2.getOptions().get("header"));
    // // assertTrue((Boolean) grid2.getOptions().get("sortable"));
    // // assertTrue((Boolean) grid2.getOptions().get("filter"));
    // // assertTrue((Boolean) grid2.getOptions().get("multiselect"));
    // // assertTrue((Boolean) grid2.getOptions().get("paginable"));
    // // assertFalse((Boolean) grid2.getOptions().get("canDelete"));
    // // assertFalse((Boolean) grid2.getOptions().get("canNew"));
    // // assertEquals(Integer.valueOf(450), grid2.getOptions().get("height"));
    // // assertEquals(1, ((Set<String>) getField(grid2, "orderableColumns")).size());
    // // assertThat(((Set<String>) getField(grid2, "orderableColumns")), hasItems(columns2.get(0).getName()));
    // // assertEquals(2, ((Set<FieldDefinition>) getField(grid2, "searchableFields")).size());
    // // assertThat(((Set<FieldDefinition>) getField(grid2, "searchableFields")),
    // // hasItems(dataDefinitionB.getField("name"), dataDefinitionB.getField("beanA")));
    // // assertEquals("multicolumn", columns2.get(1).getName());
    // // assertEquals(ColumnAggregationMode.SUM, columns2.get(1).getAggregationMode());
    // // assertEquals("2 + 2", columns2.get(1).getExpression());
    // // assertEquals(Integer.valueOf(20), columns2.get(1).getWidth());
    // // assertEquals(2, columns2.get(1).getFields().size());
    // // assertThat(columns2.get(1).getFields(), hasItems(dataDefinitionB.getField("name"), dataDefinitionB.getField("beanA")));
    // //
    // // checkComponent(vd.getComponentByReference("mainWindow.link"), LinkButtonComponent.class, "mainWindow.link",
    // // "linkButton",
    // // "beanB", null, null, null, Sets.<String> newHashSet(), 0,
    // // ImmutableMap.<String, Object> of("url", "download.html"));
    // }
    //
    // @SuppressWarnings("unchecked")
    // private void checkFieldListener(final ComponentPattern component, final ComponentPattern listener, final String field) {
    // Map<String, ComponentPattern> listeners = (Map<String, ComponentPattern>) getField(component,
    // "fieldEntityIdChangeListeners");
    // ComponentPattern actualListener = listeners.get(field);
    // assertEquals(listener, actualListener);
    // }
    //
    // private void checkComponent(final ComponentPattern component, final ComponentPattern parent, final Class<?> clazz,
    // final String name, final String model) {
    // assertNotNull(component);
    // assertThat(component, instanceOf(clazz));
    // assertEquals(name, component.getName());
    // assertEquals(parent, getField(component, "parent"));
    // // assertEquals(model, ((DataDefinition) getField(component, "dataDefinition")).getName());
    // }
    //
    // @Test
    // public void shouldSetHooks() {
    // // given
    // ViewDefinition viewDefinition = parseAndGetViewDefinition();
    //
    // // then
    // testHookDefinition(viewDefinition, "preInitializeHooks", 0, CustomEntityService.class, "onView");
    // testHookDefinition(viewDefinition, "postInitializeHooks", 0, CustomEntityService.class, "onView");
    // testHookDefinition(viewDefinition, "preRenderHooks", 0, CustomEntityService.class, "onView");
    // }
    //
    // private void testHookDefinition(final Object object, final String hookFieldName, final int hookPosition,
    // final Class<?> hookBeanClass, final String hookMethodName) {
    // @SuppressWarnings("unchecked")
    // HookDefinition hook = ((List<HookDefinition>) getField(object, hookFieldName)).get(hookPosition);
    //
    // assertNotNull(hook);
    // assertThat(getField(hook, "bean"), instanceOf(hookBeanClass));
    // assertEquals(hookMethodName, getField(hook, "methodName"));
    // }
    //
    // private ViewDefinition parseAndGetViewDefinition() {
    // viewDefinitionParser.parse(xml);
    // return viewDefinitionService.get("sample", "simpleView");
    // }
    //
    // @Test
    // public void shouldSetListeners() throws Exception {
    // // given
    // ComponentPattern component = parseAndGetViewDefinition().getComponentByReference("mainWindow.beanBForm");
    //
    // // then
    // List<ComponentCustomEvent> customEvents = (List<ComponentCustomEvent>) getField(component, "customEvents");
    //
    // assertEquals("save", customEvents.get(0).getEvent());
    // assertThat(customEvents.get(0).getObject(), instanceOf(CustomEntityService.class));
    // assertEquals("saveForm", customEvents.get(0).getMethod());
    //
    // assertEquals("generate", customEvents.get(1).getEvent());
    // assertThat(customEvents.get(1).getObject(), instanceOf(CustomEntityService.class));
    // assertEquals("generate", customEvents.get(1).getMethod());
    // }

}
