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

package com.qcadoo.mes.view.patterns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.model.types.BelongsToType;
import com.qcadoo.mes.model.types.FieldType;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.components.TextInputComponentPattern;
import com.qcadoo.mes.view.components.form.FormComponentPattern;
import com.qcadoo.mes.view.components.window.WindowComponentPattern;
import com.qcadoo.mes.view.internal.ViewDefinitionImpl;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.FieldDefinition;

public class InitializationTest extends AbstractPatternTest {

    @Test
    public void shouldTakeDataDefinitionFromView() throws Exception {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);
        ViewDefinition viewDefinition = mock(ViewDefinition.class);
        given(viewDefinition.getDataDefinition()).willReturn(dataDefinition);

        AbstractComponentPattern pattern = new TextInputComponentPattern(getComponentDefinition("test", viewDefinition));

        // when
        pattern.initialize();

        // then
        Assert.assertEquals(dataDefinition, ReflectionTestUtils.getField(pattern, "dataDefinition"));
    }

    @Test
    public void shouldTakeDataDefinitionFromParent() throws Exception {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);
        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractContainerPattern parent = new WindowComponentPattern(getComponentDefinition("parent", viewDefinition));
        setField(parent, "dataDefinition", dataDefinition);

        AbstractComponentPattern pattern = new TextInputComponentPattern(getComponentDefinition("test", parent, viewDefinition));

        // when
        pattern.initialize();

        // then
        Assert.assertEquals(dataDefinition, getField(pattern, "dataDefinition"));
    }

    @Test
    public void shouldTakeDataDefinitionFromFieldComponent() throws Exception {
        // given
        FieldDefinition fieldDefinition = mock(FieldDefinition.class);

        DataDefinition dataDefinition = mock(DataDefinition.class);
        given(dataDefinition.getField("field")).willReturn(fieldDefinition);

        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractComponentPattern fieldComponent = new TextInputComponentPattern(getComponentDefinition("component",
                viewDefinition));
        setField(fieldComponent, "dataDefinition", dataDefinition);
        setField(fieldComponent, "initialized", true);

        given(viewDefinition.getComponentByReference("component")).willReturn(fieldComponent);

        AbstractComponentPattern pattern = new TextInputComponentPattern(getComponentDefinition("test", "#{component}.field",
                null, null, viewDefinition));

        // when
        pattern.initialize();

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

        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractContainerPattern parent = new WindowComponentPattern(getComponentDefinition("parent", viewDefinition));
        setField(parent, "dataDefinition", dataDefinition);
        setField(parent, "initialized", true);

        AbstractComponentPattern pattern = new TextInputComponentPattern(getComponentDefinition("test", "field", null, parent,
                viewDefinition));

        // when
        pattern.initialize();

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

        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractComponentPattern sourceComponent = new TextInputComponentPattern(getComponentDefinition("component",
                viewDefinition));
        setField(sourceComponent, "dataDefinition", dataDefinition);
        setField(sourceComponent, "initialized", true);

        given(viewDefinition.getComponentByReference("component")).willReturn(sourceComponent);

        AbstractComponentPattern pattern = new TextInputComponentPattern(getComponentDefinition("test", null,
                "#{component}.field", null, viewDefinition));

        // when
        pattern.initialize();

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

        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractContainerPattern parent = new WindowComponentPattern(getComponentDefinition("parent", viewDefinition));
        setField(parent, "dataDefinition", dataDefinition);
        setField(parent, "initialized", true);

        AbstractComponentPattern pattern = new TextInputComponentPattern(getComponentDefinition("test", null, "field", parent,
                viewDefinition));

        // when
        pattern.initialize();

        // then
        Assert.assertEquals(dataDefinition, getField(pattern, "dataDefinition"));
        Assert.assertEquals(fieldDefinition, getField(pattern, "scopeFieldDefinition"));
    }

    @Test
    public void shouldGetDataDefinitionFromBelongsToTypeFieldDefinition() throws Exception {
        // given
        BelongsToType fieldType = mock(BelongsToType.class);

        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        given(fieldDefinition.getType()).willReturn(fieldType);

        DataDefinition dataDefinition = mock(DataDefinition.class);
        given(dataDefinition.getField("field")).willReturn(fieldDefinition);

        DataDefinition belongsToDefinition = mock(DataDefinition.class);
        given(fieldType.getDataDefinition()).willReturn(belongsToDefinition);

        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractContainerPattern parent = new WindowComponentPattern(getComponentDefinition("parent", viewDefinition));
        setField(parent, "dataDefinition", dataDefinition);
        setField(parent, "initialized", true);

        AbstractComponentPattern pattern = new TextInputComponentPattern(getComponentDefinition("test", "field", null, parent,
                viewDefinition));

        // when
        pattern.initialize();

        // then
        Assert.assertEquals(belongsToDefinition, getField(pattern, "dataDefinition"));
        Assert.assertEquals(fieldDefinition, getField(pattern, "fieldDefinition"));
    }

    @Test
    public void shouldGetDataDefinitionFromHasManyTypeFieldDefinition() throws Exception {
        // given
        HasManyType fieldType = mock(HasManyType.class);

        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        given(fieldDefinition.getType()).willReturn(fieldType);

        DataDefinition dataDefinition = mock(DataDefinition.class);
        given(dataDefinition.getField("field")).willReturn(fieldDefinition);

        DataDefinition belongsToDefinition = mock(DataDefinition.class);
        given(fieldType.getDataDefinition()).willReturn(belongsToDefinition);

        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractContainerPattern parent = new WindowComponentPattern(getComponentDefinition("parent", viewDefinition));
        setField(parent, "dataDefinition", dataDefinition);
        setField(parent, "initialized", true);

        AbstractComponentPattern pattern = new TextInputComponentPattern(getComponentDefinition("test", "field", null, parent,
                viewDefinition));

        // when
        pattern.initialize();

        // then
        Assert.assertEquals(belongsToDefinition, getField(pattern, "dataDefinition"));
        Assert.assertEquals(fieldDefinition, getField(pattern, "fieldDefinition"));
    }

    @Test
    public void shouldGetDataDefinitionFromBelongsToTypeScopeFieldDefinition() throws Exception {
        // given
        BelongsToType fieldType = mock(BelongsToType.class);

        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        given(fieldDefinition.getType()).willReturn(fieldType);

        DataDefinition dataDefinition = mock(DataDefinition.class);
        given(dataDefinition.getField("field")).willReturn(fieldDefinition);

        DataDefinition belongsToDefinition = mock(DataDefinition.class);
        given(fieldType.getDataDefinition()).willReturn(belongsToDefinition);

        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractContainerPattern parent = new WindowComponentPattern(getComponentDefinition("parent", viewDefinition));
        setField(parent, "dataDefinition", dataDefinition);
        setField(parent, "initialized", true);

        AbstractComponentPattern pattern = new TextInputComponentPattern(getComponentDefinition("test", null, "field", parent,
                viewDefinition));

        // when
        pattern.initialize();

        // then
        Assert.assertEquals(belongsToDefinition, getField(pattern, "dataDefinition"));
        Assert.assertEquals(fieldDefinition, getField(pattern, "scopeFieldDefinition"));
    }

    @Test
    public void shouldGetDataDefinitionFromHasManyTypeScopeFieldDefinition() throws Exception {
        // given
        HasManyType fieldType = mock(HasManyType.class);

        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        given(fieldDefinition.getType()).willReturn(fieldType);

        DataDefinition dataDefinition = mock(DataDefinition.class);
        given(dataDefinition.getField("field")).willReturn(fieldDefinition);

        DataDefinition belongsToDefinition = mock(DataDefinition.class);
        given(fieldType.getDataDefinition()).willReturn(belongsToDefinition);

        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractContainerPattern parent = new WindowComponentPattern(getComponentDefinition("parent", viewDefinition));
        setField(parent, "dataDefinition", dataDefinition);
        setField(parent, "initialized", true);

        AbstractComponentPattern pattern = new TextInputComponentPattern(getComponentDefinition("test", null, "field", parent,
                viewDefinition));

        // when
        pattern.initialize();

        // then
        Assert.assertEquals(belongsToDefinition, getField(pattern, "dataDefinition"));
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
        given(belongsToDataDefinition.getField("hasMany")).willReturn(hasManyFieldDefinition);
        given(dataDefinition.getField("belongsTo")).willReturn(belongsToFieldDefinition);
        given(dataDefinition.getField("field")).willReturn(fieldDefinition);
        given(fieldDefinition.getType()).willReturn(fieldType);
        given(hasManyFieldDefinition.getType()).willReturn(hasManyType);
        given(belongsToFieldDefinition.getType()).willReturn(belongsToType);
        given(hasManyType.getDataDefinition()).willReturn(hasManyDataDefinition);
        given(belongsToType.getDataDefinition()).willReturn(belongsToDataDefinition);

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("view", "plugin", dataDefinition, true, null);

        AbstractContainerPattern parent = new FormComponentPattern(getComponentDefinition("parent", viewDefinition));
        AbstractContainerPattern form = new FormComponentPattern(getComponentDefinition("form", null, null, parent,
                viewDefinition));
        AbstractComponentPattern input = new TextInputComponentPattern(getComponentDefinition("input", "field", null, form,
                viewDefinition));
        AbstractComponentPattern select = new TextInputComponentPattern(getComponentDefinition("select", "belongsTo", null, form,
                viewDefinition));
        AbstractComponentPattern subselect = new TextInputComponentPattern(getComponentDefinition("subselect",
                "#{parent.form}.hasMany", "#{parent.form.select}.hasMany", form, viewDefinition));
        AbstractComponentPattern grid = new TextInputComponentPattern(getComponentDefinition("grid", null,
                "#{parent.form}.hasMany", parent, viewDefinition));

        parent.addChild(form);
        parent.addChild(grid);
        form.addChild(input);
        form.addChild(select);
        form.addChild(subselect);

        viewDefinition.addComponentPattern(parent);

        // when
        viewDefinition.initialize();

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
