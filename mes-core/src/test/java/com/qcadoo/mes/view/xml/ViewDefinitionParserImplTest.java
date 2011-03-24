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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.beans.sample.CustomEntityService;
import com.qcadoo.mes.internal.ViewDefinitionServiceImpl;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.HookDefinition;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.components.ButtonComponentPattern;
import com.qcadoo.mes.view.components.CheckBoxComponentPattern;
import com.qcadoo.mes.view.components.TextAreaComponentPattern;
import com.qcadoo.mes.view.components.TextInputComponentPattern;
import com.qcadoo.mes.view.components.form.FormComponentPattern;
import com.qcadoo.mes.view.components.grid.GridComponentPattern;
import com.qcadoo.mes.view.components.window.WindowComponentPattern;
import com.qcadoo.mes.view.hooks.internal.HookFactory;
import com.qcadoo.mes.view.internal.ComponentCustomEvent;
import com.qcadoo.mes.view.internal.ViewComponentsResolver;
import com.qcadoo.mes.view.ribbon.RibbonActionItem;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.types.BelongsToType;
import com.qcadoo.model.api.types.HasManyType;
import com.qcadoo.model.internal.types.StringType;

public class ViewDefinitionParserImplTest {

    private ViewDefinitionParserImpl viewDefinitionParser;

    private DataDefinitionService dataDefinitionService;

    private ViewDefinitionService viewDefinitionService;

    private HookFactory hookFactory;

    private ApplicationContext applicationContext;

    private String xml;

    private DataDefinition dataDefinitionA;

    private DataDefinition dataDefinitionB;

    private TranslationService translationService;

    private static ViewComponentsResolver viewComponentsResolver;

    @BeforeClass
    public static void initClass() throws Exception {
        viewComponentsResolver = new ViewComponentsResolver();
        viewComponentsResolver.refreshAvailableComponentsList();
    }

    @Before
    public void init() throws Exception {
        applicationContext = mock(ApplicationContext.class);
        dataDefinitionService = mock(DataDefinitionService.class);

        translationService = mock(TranslationService.class);

        viewDefinitionService = new ViewDefinitionServiceImpl();

        hookFactory = new HookFactory();
        setField(hookFactory, "applicationContext", applicationContext);

        viewDefinitionParser = new ViewDefinitionParserImpl();
        setField(viewDefinitionParser, "dataDefinitionService", dataDefinitionService);
        setField(viewDefinitionParser, "viewDefinitionService", viewDefinitionService);
        setField(viewDefinitionParser, "hookFactory", hookFactory);
        setField(viewDefinitionParser, "translationService", translationService);
        setField(viewDefinitionParser, "viewComponentsResolver", viewComponentsResolver);

        xml = "view/test.xml";

        given(applicationContext.getBean(CustomEntityService.class)).willReturn(new CustomEntityService());

        dataDefinitionA = mock(DataDefinition.class);
        dataDefinitionB = mock(DataDefinition.class);
        FieldDefinition nameA = mock(FieldDefinition.class, "nameA");
        FieldDefinition nameB = mock(FieldDefinition.class, "nameB");
        FieldDefinition hasManyB = mock(FieldDefinition.class, "hasManyB");
        FieldDefinition belongToA = mock(FieldDefinition.class, "belongsToA");
        HasManyType hasManyBType = mock(HasManyType.class);
        BelongsToType belongToAType = mock(BelongsToType.class);

        given(nameA.getDataDefinition()).willReturn(dataDefinitionA);
        given(nameA.getType()).willReturn(new StringType());
        given(nameB.getType()).willReturn(new StringType());
        given(nameB.getDataDefinition()).willReturn(dataDefinitionA);
        given(hasManyB.getType()).willReturn(hasManyBType);
        given(hasManyB.getDataDefinition()).willReturn(dataDefinitionB);
        given(belongToA.getType()).willReturn(belongToAType);
        given(belongToA.getDataDefinition()).willReturn(dataDefinitionB);
        given(hasManyBType.getDataDefinition()).willReturn(dataDefinitionB);
        given(belongToAType.getDataDefinition()).willReturn(dataDefinitionA);
        given(dataDefinitionA.getField("beansB")).willReturn(hasManyB);
        given(dataDefinitionA.getField("name")).willReturn(nameA);
        given(dataDefinitionB.getField("active")).willReturn(nameA);
        given(dataDefinitionB.getField("beanA")).willReturn(belongToA);
        given(dataDefinitionB.getField("beanM")).willReturn(belongToA);
        given(dataDefinitionB.getField("beanB")).willReturn(belongToA);
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
        List<ViewDefinition> viewDefinitions = parseAndGetViewDefinitions();

        // then
        assertEquals(2, viewDefinitions.size());
        assertNotNull(viewDefinitions.get(0));
        assertNotNull(viewDefinitions.get(1));
    }

    @Test
    public void shouldSetViewDefinitionAttributes() {
        // given
        ViewDefinition viewDefinition = parseAndGetViewDefinition();

        // then
        assertEquals("simpleView", viewDefinition.getName());
        assertEquals("sample", viewDefinition.getPluginIdentifier());
        assertThat(viewDefinition.getComponentByReference("mainWindow"), instanceOf(WindowComponentPattern.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldHasRibbon() throws Exception {
        // given
        ViewDefinition viewDefinition = parseAndGetViewDefinition();
        TranslationService translationService = mock(TranslationService.class);
        setField(viewDefinition, "translationService", translationService);

        JSONObject jsOptions = (JSONObject) ((Map<String, Map<String, Object>>) viewDefinition.prepareView(new JSONObject(),
                Locale.ENGLISH).get("components")).get("mainWindow").get("jsOptions");

        JSONObject ribbon = jsOptions.getJSONObject("ribbon");

        // then
        assertNotNull(ribbon);
        assertEquals(2, ribbon.getJSONArray("groups").length());
        assertEquals("first", ribbon.getJSONArray("groups").getJSONObject(0).getString("name"));
        assertEquals(2, ribbon.getJSONArray("groups").getJSONObject(0).getJSONArray("items").length());
        assertEquals("second", ribbon.getJSONArray("groups").getJSONObject(1).getString("name"));
        assertEquals(2, ribbon.getJSONArray("groups").getJSONObject(1).getJSONArray("items").length());

        JSONObject item11 = ribbon.getJSONArray("groups").getJSONObject(0).getJSONArray("items").getJSONObject(0);

        assertEquals("test", item11.getString("name"));
        assertFalse(item11.has("icon"));
        assertEquals(RibbonActionItem.Type.BIG_BUTTON.toString(), item11.getString("type"));
        // assertEquals("#{mainWindow.beanBForm}.save,#{mainWindow}.back", item11.getString("clickAction"));

        JSONObject item12 = ribbon.getJSONArray("groups").getJSONObject(0).getJSONArray("items").getJSONObject(1);

        assertEquals("test2", item12.getString("name"));
        assertEquals("icon2", item12.getString("icon"));
        assertEquals(RibbonActionItem.Type.SMALL_BUTTON.toString(), item12.getString("type"));
        assertEquals("xxx", item12.getString("clickAction"));

        JSONObject item21 = ribbon.getJSONArray("groups").getJSONObject(1).getJSONArray("items").getJSONObject(0);

        assertEquals("test2", item21.getString("name"));
        assertFalse(item21.has("icon"));
        assertEquals(RibbonActionItem.Type.BIG_BUTTON.toString(), item21.getString("type"));
        assertFalse(item21.has("clickAction"));

        JSONObject item22 = ribbon.getJSONArray("groups").getJSONObject(1).getJSONArray("items").getJSONObject(1);

        assertEquals("combo1", item22.getString("name"));
        assertFalse(item22.has("icon"));
        assertEquals(RibbonActionItem.Type.BIG_BUTTON.toString(), item22.getString("type"));
        assertEquals("yyy3", item22.getString("clickAction"));
        assertEquals(2, item22.getJSONArray("items").length());

        JSONObject item221 = item22.getJSONArray("items").getJSONObject(0);

        assertEquals("test1", item221.getString("name"));
        assertFalse(item221.has("icon"));
        assertEquals(RibbonActionItem.Type.BIG_BUTTON.toString(), item221.getString("type"));
        assertEquals("yyy1", item221.getString("clickAction"));

        JSONObject item222 = item22.getJSONArray("items").getJSONObject(1);

        assertEquals("test2", item222.getString("name"));
        assertEquals("icon2", item222.getString("icon"));
        assertEquals(RibbonActionItem.Type.BIG_BUTTON.toString(), item222.getString("type"));
        assertEquals("yyy2", item222.getString("clickAction"));
    }

    @Test
    public void shouldSetFields() {
        // given
        ViewDefinition vd = parseAndGetViewDefinition();

        // then
        checkComponent(vd.getComponentByReference("mainWindow"), WindowComponentPattern.class, "mainWindow", "beanB");

        checkComponent(vd.getComponentByReference("beanBForm"), FormComponentPattern.class, "beanBForm", "beanB");

        checkComponent(vd.getComponentByReference("referenceToTextarea"), TextAreaComponentPattern.class, "name", "beanB");

        checkComponent(vd.getComponentByReference("active"), CheckBoxComponentPattern.class, "active", "beanB");

        checkComponent(vd.getComponentByReference("selectBeanA"), TextInputComponentPattern.class, "selectBeanA", "beanA");

        checkComponent(vd.getComponentByReference("beanM"), TextAreaComponentPattern.class, "beanM", "beanB");

        checkComponent(vd.getComponentByReference("beansBInnerGrig"), GridComponentPattern.class, "beansBGrig", "beanB");

        checkComponent(vd.getComponentByReference("beanAForm"), FormComponentPattern.class, "beanAForm", "beanA");

        checkComponent(vd.getComponentByReference("beanAFormName"), TextInputComponentPattern.class, "name", "beanA");

        checkComponent(vd.getComponentByReference("beansBGrig"), GridComponentPattern.class, "beansBGrig", "beanB");

        checkComponent(vd.getComponentByReference("link"), ButtonComponentPattern.class, "link", "beanB");

        checkFieldListener(vd.getComponentByReference("beanBForm"), vd.getComponentByReference("referenceToTextarea"), "name");
        checkFieldListener(vd.getComponentByReference("beanBForm"), vd.getComponentByReference("active"), "active");
        checkFieldListener(vd.getComponentByReference("beanBForm"), vd.getComponentByReference("beanAForm"), "beanA");
        checkFieldListener(vd.getComponentByReference("beanBForm"), vd.getComponentByReference("beanM"), "beanM");

    }

    @SuppressWarnings("unchecked")
    private void checkFieldListener(final ComponentPattern component, final ComponentPattern listener, final String field) {
        Map<String, ComponentPattern> listeners = (Map<String, ComponentPattern>) getField(component,
                "fieldEntityIdChangeListeners");
        ComponentPattern actualListener = listeners.get(field);
        assertEquals(listener, actualListener);
    }

    private void checkComponent(final ComponentPattern component, final Class<?> clazz, final String name, final String model) {
        assertNotNull(component);
        assertThat(component, instanceOf(clazz));
        assertEquals(name, component.getName());
    }

    @Test
    public void shouldSetHooks() {
        // given
        ViewDefinition viewDefinition = parseAndGetViewDefinition();

        // then
        testHookDefinition(viewDefinition, "preInitializeHooks", 0, CustomEntityService.class, "onView");
        testHookDefinition(viewDefinition, "postInitializeHooks", 0, CustomEntityService.class, "onView");
        testHookDefinition(viewDefinition, "preRenderHooks", 0, CustomEntityService.class, "onView");
    }

    private void testHookDefinition(final Object object, final String hookFieldName, final int hookPosition,
            final Class<?> hookBeanClass, final String hookMethodName) {
        @SuppressWarnings("unchecked")
        HookDefinition hook = ((List<HookDefinition>) getField(object, hookFieldName)).get(hookPosition);

        assertNotNull(hook);
        assertThat(getField(hook, "bean"), instanceOf(hookBeanClass));
        assertEquals(hookMethodName, getField(hook, "methodName"));
    }

    private List<ViewDefinition> parseAndGetViewDefinitions() {
        return viewDefinitionParser.parseViewXml(new ClassPathResource(xml));
    }

    private ViewDefinition parseAndGetViewDefinition() {
        return viewDefinitionParser.parseViewXml(new ClassPathResource(xml)).get(0);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldSetListeners() throws Exception {
        // given
        ComponentPattern component = parseAndGetViewDefinition().getComponentByReference("beanBForm");

        // then
        List<ComponentCustomEvent> customEvents = (List<ComponentCustomEvent>) getField(component, "customEvents");

        assertEquals("save", customEvents.get(0).getEvent());
        assertThat(customEvents.get(0).getObject(), instanceOf(CustomEntityService.class));
        assertEquals("saveForm", customEvents.get(0).getMethod());

        assertEquals("generate", customEvents.get(1).getEvent());
        assertThat(customEvents.get(1).getObject(), instanceOf(CustomEntityService.class));
        assertEquals("generate", customEvents.get(1).getMethod());
    }

}
