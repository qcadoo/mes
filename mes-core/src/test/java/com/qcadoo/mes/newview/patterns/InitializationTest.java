package com.qcadoo.mes.newview.patterns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.BelongsToType;
import com.qcadoo.mes.model.types.FieldType;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.newview.AbstractComponentPattern;
import com.qcadoo.mes.newview.AbstractContainerPattern;
import com.qcadoo.mes.newview.ViewDefinition;
import com.qcadoo.mes.newview.components.FormComponentPattern;
import com.qcadoo.mes.newview.components.TextInputComponentPattern;

public class InitializationTest {

    @Test
    public void shouldTakeDataDefinitionFromView() throws Exception {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);
        ViewDefinition viewDefinition = mock(ViewDefinition.class);
        given(viewDefinition.getDataDefinition()).willReturn(dataDefinition);

        AbstractComponentPattern pattern = new TextInputComponentPattern("test", null, null, null);

        // when
        pattern.initialize(viewDefinition);

        // then
        Assert.assertEquals(dataDefinition, ReflectionTestUtils.getField(pattern, "dataDefinition"));
    }

    @Test
    public void shouldTakeDataDefinitionFromParent() throws Exception {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);
        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractComponentPattern parent = new TextInputComponentPattern("parent", null, null, null);
        setField(parent, "dataDefinition", dataDefinition);

        AbstractComponentPattern pattern = new TextInputComponentPattern("test", null, null, parent);

        // when
        pattern.initialize(viewDefinition);

        // then
        Assert.assertEquals(dataDefinition, getField(pattern, "dataDefinition"));
    }

    @Test
    public void shouldTakeDataDefinitionFromFieldComponent() throws Exception {
        // given
        FieldDefinition fieldDefinition = mock(FieldDefinition.class);

        DataDefinition dataDefinition = mock(DataDefinition.class);
        given(dataDefinition.getField("field")).willReturn(fieldDefinition);

        AbstractComponentPattern fieldComponent = new TextInputComponentPattern("parent", null, null, null);
        setField(fieldComponent, "dataDefinition", dataDefinition);

        ViewDefinition viewDefinition = mock(ViewDefinition.class);
        given(viewDefinition.getComponentByPath("component")).willReturn(fieldComponent);

        AbstractComponentPattern pattern = new TextInputComponentPattern("test", "#{component}.field", null, null);

        // when
        pattern.initialize(viewDefinition);

        // then
        Assert.assertEquals(dataDefinition, getField(pattern, "dataDefinition"));
        Assert.assertEquals(fieldDefinition, getField(pattern, "fieldDefinition"));
    }

    @Test
    public void shouldSetFieldDefinitionWithoutFieldComponent() throws Exception {
        // given
        FieldDefinition fieldDefinition = mock(FieldDefinition.class);

        DataDefinition dataDefinition = mock(DataDefinition.class);
        given(dataDefinition.getField("field")).willReturn(fieldDefinition);

        AbstractComponentPattern parent = new TextInputComponentPattern("parent", null, null, null);
        setField(parent, "dataDefinition", dataDefinition);

        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractComponentPattern pattern = new TextInputComponentPattern("test", "field", null, parent);

        // when
        pattern.initialize(viewDefinition);

        // then
        Assert.assertEquals(dataDefinition, getField(pattern, "dataDefinition"));
        Assert.assertEquals(fieldDefinition, getField(pattern, "fieldDefinition"));
    }

    @Test
    public void shouldTakeDataDefinitionFromScopeComponent() throws Exception {
        // given
        FieldDefinition fieldDefinition = mock(FieldDefinition.class);

        DataDefinition dataDefinition = mock(DataDefinition.class);
        given(dataDefinition.getField("field")).willReturn(fieldDefinition);

        AbstractComponentPattern sourceComponent = new TextInputComponentPattern("parent", null, null, null);
        setField(sourceComponent, "dataDefinition", dataDefinition);

        ViewDefinition viewDefinition = mock(ViewDefinition.class);
        given(viewDefinition.getComponentByPath("component")).willReturn(sourceComponent);

        AbstractComponentPattern pattern = new TextInputComponentPattern("test", null, "#{component}.field", null);

        // when
        pattern.initialize(viewDefinition);

        // then
        Assert.assertEquals(dataDefinition, getField(pattern, "dataDefinition"));
        Assert.assertEquals(fieldDefinition, getField(pattern, "scopeFieldDefinition"));
    }

    @Test
    public void shouldSetFieldDefinitionWithoutScopeComponent() throws Exception {
        // given
        FieldDefinition fieldDefinition = mock(FieldDefinition.class);

        DataDefinition dataDefinition = mock(DataDefinition.class);
        given(dataDefinition.getField("field")).willReturn(fieldDefinition);

        AbstractComponentPattern parent = new TextInputComponentPattern("parent", null, null, null);
        setField(parent, "dataDefinition", dataDefinition);

        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractComponentPattern pattern = new TextInputComponentPattern("test", null, "field", parent);

        // when
        pattern.initialize(viewDefinition);

        // then
        Assert.assertEquals(dataDefinition, getField(pattern, "dataDefinition"));
        Assert.assertEquals(fieldDefinition, getField(pattern, "scopeFieldDefinition"));
    }

    @Test
    public void shouldInitializeAllComponents() throws Exception {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);
        DataDefinition hasManyDataDefinition = mock(DataDefinition.class);
        DataDefinition belongsToDataDefinition = mock(DataDefinition.class);
        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        FieldDefinition hasManyFieldDefinition = mock(FieldDefinition.class);
        FieldDefinition belongsToFieldDefinition = mock(FieldDefinition.class);
        FieldType fieldType = mock(FieldType.class);
        BelongsToType belongsToType = mock(BelongsToType.class);
        HasManyType hasManyType = mock(HasManyType.class);
        given(dataDefinition.getField("hasMany")).willReturn(hasManyFieldDefinition);
        given(dataDefinition.getField("belongsTo")).willReturn(belongsToFieldDefinition);
        given(dataDefinition.getField("field")).willReturn(fieldDefinition);
        given(fieldDefinition.getType()).willReturn(fieldType);
        given(hasManyFieldDefinition.getType()).willReturn(hasManyType);
        given(belongsToFieldDefinition.getType()).willReturn(belongsToType);
        given(hasManyType.getDataDefinition()).willReturn(hasManyDataDefinition);
        given(belongsToType.getDataDefinition()).willReturn(belongsToDataDefinition);

        AbstractContainerPattern parent = new FormComponentPattern("parent", null, null, null);
        AbstractContainerPattern form = new FormComponentPattern("form", null, null, parent);
        AbstractComponentPattern input = new TextInputComponentPattern("input", "field", null, form);
        AbstractComponentPattern select = new TextInputComponentPattern("select", "belongsTo", null, form);
        AbstractComponentPattern subselect = new TextInputComponentPattern("subselect", "#{parent.form}.hasMany",
                "#{parent.form.select}.hasMany", form);
        AbstractComponentPattern grid = new TextInputComponentPattern("grid", null, "#{parent.form}.hasMany", parent);

        parent.addChild(form);
        parent.addChild(grid);
        form.addChild(input);
        form.addChild(select);
        form.addChild(subselect);

        ViewDefinition viewDefinition = mock(ViewDefinition.class);
        given(viewDefinition.getDataDefinition()).willReturn(dataDefinition);
        given(viewDefinition.getComponentByPath("parent")).willReturn(parent);
        given(viewDefinition.getComponentByPath("parent.form")).willReturn(form);
        given(viewDefinition.getComponentByPath("parent.form.input")).willReturn(input);
        given(viewDefinition.getComponentByPath("parent.form.select")).willReturn(select);
        given(viewDefinition.getComponentByPath("parent.form.subselect")).willReturn(subselect);
        given(viewDefinition.getComponentByPath("parent.grid")).willReturn(grid);

        // when
        parent.initialize(viewDefinition);

        // then
        assertEquals(dataDefinition, getField(parent, "dataDefinition"));
        assertEquals(dataDefinition, getField(form, "dataDefinition"));
        assertEquals(dataDefinition, getField(input, "dataDefinition"));
        assertEquals(belongsToDataDefinition, getField(select, "dataDefinition"));
        assertEquals(hasManyDataDefinition, getField(subselect, "dataDefinition"));
        assertEquals(hasManyDataDefinition, getField(grid, "dataDefinition"));
        assertNull(getField(parent, "scopeFieldDefinition"));
        assertNull(getField(form, "scopeFieldDefinition"));
        assertNull(getField(input, "scopeFieldDefinition"));
        assertNull(getField(select, "scopeFieldDefinition"));
        assertEquals(hasManyFieldDefinition, getField(subselect, "scopeFieldDefinition"));
        assertEquals(hasManyFieldDefinition, getField(grid, "scopeFieldDefinition"));
        assertNull(getField(parent, "fieldDefinition"));
        assertNull(getField(form, "fieldDefinition"));
        assertEquals(fieldDefinition, getField(input, "fieldDefinition"));
        assertEquals(belongsToFieldDefinition, getField(select, "fieldDefinition"));
        assertEquals(hasManyFieldDefinition, getField(subselect, "fieldDefinition"));
        assertNull(getField(grid, "fieldDefinition"));
    }
}
