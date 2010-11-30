package com.qcadoo.mes.newview.patterns;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.newview.AbstractComponentPattern;
import com.qcadoo.mes.newview.ViewDefinition;
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

}
