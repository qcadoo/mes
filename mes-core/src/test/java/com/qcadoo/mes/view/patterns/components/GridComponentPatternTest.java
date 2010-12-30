package com.qcadoo.mes.view.patterns.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Locale;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.BelongsToType;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.types.internal.EnumType;
import com.qcadoo.mes.model.types.internal.IntegerType;
import com.qcadoo.mes.model.types.internal.StringType;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.TextInputComponentPattern;
import com.qcadoo.mes.view.components.grid.GridComponentPattern;
import com.qcadoo.mes.view.components.grid.GridComponentState;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;
import com.qcadoo.mes.view.patterns.AbstractPatternTest;

public class GridComponentPatternTest extends AbstractPatternTest {

    @Test
    public void shouldInitializeOptions() throws Exception {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);
        ViewDefinition viewDefinition = mock(ViewDefinition.class);
        TranslationService translationService = mock(TranslationService.class);
        given(translationService.translate(Mockito.anyString(), Mockito.any(Locale.class))).willReturn("i18n");
        FieldDefinition nameFieldDefinition = mock(FieldDefinition.class);
        given(nameFieldDefinition.getType()).willReturn(new EnumType("v1", "v2"));

        FieldDefinition numberFieldDefinition = mock(FieldDefinition.class);
        given(numberFieldDefinition.getType()).willReturn(new IntegerType());

        FieldDefinition productFieldDefinition = mock(FieldDefinition.class);
        given(productFieldDefinition.getType()).willReturn(new StringType());

        given(dataDefinition.getField("name")).willReturn(nameFieldDefinition);
        given(dataDefinition.getField("number")).willReturn(numberFieldDefinition);
        given(dataDefinition.getField("product")).willReturn(productFieldDefinition);
        given(dataDefinition.isPrioritizable()).willReturn(true);

        given(viewDefinition.getDataDefinition()).willReturn(dataDefinition);
        ComponentDefinition componentDefinition = getComponentDefinition("grid", viewDefinition);
        componentDefinition.setTranslationService(translationService);
        GridComponentPattern pattern = new GridComponentPattern(componentDefinition);

        pattern.addOption(new ComponentOption("correspondingView", ImmutableMap.of("value", "plugin/details")));
        pattern.addOption(new ComponentOption("correspondingComponent", ImmutableMap.of("value", "window.form")));
        pattern.addOption(new ComponentOption("paginable", ImmutableMap.of("value", "true")));
        pattern.addOption(new ComponentOption("creatable", ImmutableMap.of("value", "true")));
        pattern.addOption(new ComponentOption("deletable", ImmutableMap.of("value", "true")));
        pattern.addOption(new ComponentOption("width", ImmutableMap.of("value", "200")));
        pattern.addOption(new ComponentOption("height", ImmutableMap.of("value", "400")));
        pattern.addOption(new ComponentOption("lookup", ImmutableMap.of("value", "true")));
        pattern.addOption(new ComponentOption("searchable", ImmutableMap.of("value", "name,number,product")));
        pattern.addOption(new ComponentOption("orderable", ImmutableMap.of("value", "name,number")));
        pattern.addOption(new ComponentOption("order", ImmutableMap.of("column", "name", "direction", "asc")));
        pattern.addOption(new ComponentOption("column", ImmutableMap.of("name", "number", "fields", "number", "link", "true",
                "width", "200")));
        pattern.addOption(new ComponentOption("column", ImmutableMap.of("name", "name", "fields", "name", "hidden", "true")));
        pattern.addOption(new ComponentOption("column", ImmutableMap.of("name", "product", "expression", "#product['name']")));

        // when
        pattern.initialize();

        // then
        JSONObject options = getJsOptions(pattern);

        assertEquals("plugin/details", options.getString("correspondingView"));
        assertEquals("window.form", options.getString("correspondingComponent"));
        assertFalse(options.getBoolean("fullscreen"));
        assertTrue(options.getBoolean("paginable"));
        assertTrue(options.getBoolean("lookup"));
        assertTrue(options.getBoolean("creatable"));
        assertTrue(options.getBoolean("deletable"));
        assertTrue(options.getBoolean("prioritizable"));
        assertEquals(200, options.getInt("width"));
        assertEquals(400, options.getInt("height"));
        assertEquals(2, options.getJSONArray("orderableColumns").length());
        assertEquals("name", options.getJSONArray("orderableColumns").getString(0));
        assertEquals("number", options.getJSONArray("orderableColumns").getString(1));
        assertEquals(3, options.getJSONArray("searchableColumns").length());
        assertEquals("product", options.getJSONArray("searchableColumns").getString(0));
        assertEquals("name", options.getJSONArray("searchableColumns").getString(1));
        assertEquals("number", options.getJSONArray("searchableColumns").getString(2));
        assertFalse(options.has("belongsToFieldName"));
        assertEquals(3, options.getJSONArray("columns").length());

        JSONObject number = options.getJSONArray("columns").getJSONObject(0);
        JSONObject name = options.getJSONArray("columns").getJSONObject(1);
        JSONObject product = options.getJSONArray("columns").getJSONObject(2);

        assertEquals("number", number.getString("name"));
        assertTrue(number.getBoolean("link"));
        assertFalse(number.getBoolean("hidden"));
        assertEquals(200, number.getInt("width"));
        assertEquals("right", number.getString("align"));
        assertFalse(number.has("filterValues"));

        assertEquals("name", name.getString("name"));
        assertFalse(name.getBoolean("link"));
        assertTrue(name.getBoolean("hidden"));
        assertEquals(100, name.getInt("width"));
        assertEquals("left", name.getString("align"));
        assertEquals(2, name.getJSONObject("filterValues").length());
        assertEquals("i18n", name.getJSONObject("filterValues").getString("v1"));
        assertEquals("i18n", name.getJSONObject("filterValues").getString("v2"));

        assertEquals("product", product.getString("name"));
        assertFalse(product.getBoolean("link"));
        assertFalse(product.getBoolean("hidden"));
        assertEquals(100, product.getInt("width"));
        assertEquals("left", product.getString("align"));
        assertFalse(product.has("filterValues"));
    }

    @Test
    public void shouldHaveDefaultValues() throws Exception {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);
        ViewDefinition viewDefinition = mock(ViewDefinition.class);
        TranslationService translationService = mock(TranslationService.class);
        given(viewDefinition.getDataDefinition()).willReturn(dataDefinition);
        ComponentDefinition componentDefinition = getComponentDefinition("grid", viewDefinition);
        componentDefinition.setTranslationService(translationService);
        GridComponentPattern pattern = new GridComponentPattern(componentDefinition);
        pattern.addOption(new ComponentOption("order", ImmutableMap.of("column", "name", "direction", "asc")));

        // when
        pattern.initialize();

        // then
        JSONObject options = getJsOptions(pattern);

        assertFalse(options.has("correspondingView"));
        assertFalse(options.has("correspondingComponent"));
        assertFalse(options.has("belongsToFieldName"));
        assertTrue(options.getBoolean("paginable"));
        assertFalse(options.getBoolean("lookup"));
        assertFalse(options.getBoolean("creatable"));
        assertFalse(options.getBoolean("deletable"));
        assertFalse(options.getBoolean("prioritizable"));
        assertFalse(options.getBoolean("fullscreen"));
        assertEquals(300, options.getInt("width"));
        assertEquals(300, options.getInt("height"));
        assertEquals(0, options.getJSONArray("orderableColumns").length());
        assertEquals(0, options.getJSONArray("searchableColumns").length());
        assertEquals(0, options.getJSONArray("columns").length());
    }

    @Test
    public void shouldBeFullscreen() throws Exception {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);
        ViewDefinition viewDefinition = mock(ViewDefinition.class);
        TranslationService translationService = mock(TranslationService.class);
        given(viewDefinition.getDataDefinition()).willReturn(dataDefinition);
        ComponentDefinition componentDefinition = getComponentDefinition("grid", viewDefinition);
        componentDefinition.setTranslationService(translationService);
        GridComponentPattern pattern = new GridComponentPattern(componentDefinition);

        pattern.addOption(new ComponentOption("fullscreen", ImmutableMap.of("value", "true")));
        pattern.addOption(new ComponentOption("order", ImmutableMap.of("column", "name", "direction", "asc")));

        // when
        pattern.initialize();

        // then
        JSONObject options = getJsOptions(pattern);

        assertTrue(options.getBoolean("fullscreen"));
        assertEquals(0, options.getInt("width"));
        assertEquals(0, options.getInt("height"));
    }

    @Test
    public void shouldHaveScopeFieldName() throws Exception {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);
        TranslationService translationService = mock(TranslationService.class);

        BelongsToType belongsToFieldType = mock(BelongsToType.class);

        FieldDefinition belongsToFieldDefinition = mock(FieldDefinition.class);
        given(belongsToFieldDefinition.getName()).willReturn("joinName");
        given(belongsToFieldDefinition.getType()).willReturn(belongsToFieldType);
        given(belongsToFieldDefinition.getDataDefinition()).willReturn(dataDefinition);

        HasManyType hasManyFieldType = mock(HasManyType.class);
        given(hasManyFieldType.getJoinFieldName()).willReturn("joinName");
        given(hasManyFieldType.getDataDefinition()).willReturn(dataDefinition);

        FieldDefinition hasManyFieldDefinition = mock(FieldDefinition.class);
        given(hasManyFieldDefinition.getName()).willReturn("fieldName");
        given(hasManyFieldDefinition.getType()).willReturn(hasManyFieldType);
        given(hasManyFieldDefinition.getDataDefinition()).willReturn(dataDefinition);

        given(dataDefinition.getField("field")).willReturn(hasManyFieldDefinition);
        given(dataDefinition.getField("joinName")).willReturn(belongsToFieldDefinition);

        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractComponentPattern sourceComponent = new TextInputComponentPattern(getComponentDefinition("component",
                viewDefinition));
        setField(sourceComponent, "dataDefinition", dataDefinition);
        setField(sourceComponent, "initialized", true);

        given(viewDefinition.getComponentByReference("component")).willReturn(sourceComponent);

        ComponentDefinition componentDefinition = getComponentDefinition("grid", null, "#{component}.field", null, viewDefinition);
        componentDefinition.setTranslationService(translationService);
        GridComponentPattern pattern = new GridComponentPattern(componentDefinition);

        pattern.addOption(new ComponentOption("order", ImmutableMap.of("column", "name", "direction", "asc")));

        // when
        pattern.initialize();

        // then
        JSONObject options = getJsOptions(pattern);

        assertEquals("joinName", options.getString("belongsToFieldName"));
    }

    @Test
    public void shouldReturnState() throws Exception {
        // given
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        DataDefinition dataDefinition = mock(DataDefinition.class);
        TranslationService translationService = mock(TranslationService.class);

        BelongsToType belongsToFieldType = mock(BelongsToType.class);

        FieldDefinition belongsToFieldDefinition = mock(FieldDefinition.class);
        given(belongsToFieldDefinition.getName()).willReturn("joinName");
        given(belongsToFieldDefinition.getType()).willReturn(belongsToFieldType);

        HasManyType hasManyFieldType = mock(HasManyType.class);
        given(hasManyFieldType.getJoinFieldName()).willReturn("joinName");
        given(hasManyFieldType.getDataDefinition()).willReturn(dataDefinition);

        FieldDefinition hasManyFieldDefinition = mock(FieldDefinition.class);
        given(hasManyFieldDefinition.getName()).willReturn("fieldName");
        given(hasManyFieldDefinition.getType()).willReturn(hasManyFieldType);

        given(dataDefinition.getField("field")).willReturn(hasManyFieldDefinition);
        given(dataDefinition.getField("joinName")).willReturn(belongsToFieldDefinition);

        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractComponentPattern sourceComponent = new TextInputComponentPattern(getComponentDefinition("component",
                viewDefinition));
        setField(sourceComponent, "dataDefinition", dataDefinition);
        setField(sourceComponent, "initialized", true);

        given(viewDefinition.getComponentByReference("component")).willReturn(sourceComponent);

        ComponentDefinition componentDefinition = getComponentDefinition("grid", null, "#{component}.field", null, viewDefinition);
        componentDefinition.setTranslationService(translationService);
        GridComponentPattern pattern = new GridComponentPattern(componentDefinition);

        pattern.addOption(new ComponentOption("order", ImmutableMap.of("column", "name", "direction", "asc")));

        pattern.initialize();

        // when
        ComponentState state = pattern.createComponentState(viewDefinitionState);

        // then
        assertTrue(state instanceof GridComponentState);

        assertEquals(belongsToFieldDefinition, getField(state, "belongsToFieldDefinition"));
        assertEquals(getField(pattern, "columns"), getField(state, "columns"));

    }

}
