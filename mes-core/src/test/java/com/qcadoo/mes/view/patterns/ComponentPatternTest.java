package com.qcadoo.mes.view.patterns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.components.FormComponentPattern;
import com.qcadoo.mes.view.components.FormComponentState;
import com.qcadoo.mes.view.components.TextInputComponentPattern;

public class ComponentPatternTest {

    @Test
    public void shouldHaveValidInstance() throws Exception {
        // given
        ComponentPattern pattern = new FormComponentPattern("testName", null, null, null);

        // when
        ComponentState state = pattern.createComponentState();

        // then
        Assert.assertTrue(state instanceof FormComponentState);
    }

    @Test
    public void shouldHaveName() throws Exception {
        // given
        ComponentPattern pattern = new FormComponentPattern("testName", null, null, null);

        // when
        String name = pattern.getName();

        // then
        Assert.assertEquals("testName", name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithoutName() throws Exception {
        // given
        ComponentPattern pattern = new FormComponentPattern(null, null, null, null);

        // when
        pattern.createComponentState();
    }

    @Test
    public void shouldReturnValidPath() throws Exception {
        // given
        ComponentPattern root = new TextInputComponentPattern("rootName", null, null, null);
        ComponentPattern child1 = new TextInputComponentPattern("child1", null, null, root);
        ComponentPattern child2 = new TextInputComponentPattern("child2", null, null, root);
        ComponentPattern child11 = new TextInputComponentPattern("child11", null, null, child1);

        // when
        String rootPathName = root.getPathName();
        String child1PathName = child1.getPathName();
        String child2PathName = child2.getPathName();
        String child11PathName = child11.getPathName();

        // then
        Assert.assertEquals("rootName", rootPathName);
        Assert.assertEquals("rootName.child1", child1PathName);
        Assert.assertEquals("rootName.child2", child2PathName);
        Assert.assertEquals("rootName.child1.child11", child11PathName);
    }

    @Test
    public void shouldAddItselfToParentOnInitialize() throws Exception {
        // given
        AbstractComponentPattern parent = Mockito.mock(AbstractComponentPattern.class);
        ComponentPattern pattern = new TextInputComponentPattern("testName", "testField", null, parent);

        // when
        pattern.initialize(null);

        // then
        Mockito.verify(parent).addFieldEntityIdChangeListener("testField", pattern);
    }

    @Test
    public void shouldHaveDefaultEnabledFlag() throws Exception {
        // given
        AbstractComponentPattern pattern = new TextInputComponentPattern("testName", null, null, null);
        pattern.setDefaultEnabled(true);

        // then
        assertTrue(pattern.isDefaultEnabled());
    }

    @Test
    public void shouldHaveDefaultVisibleFlag() throws Exception {
        // given
        AbstractComponentPattern pattern = new TextInputComponentPattern("testName", null, null, null);
        pattern.setDefaultVisible(true);

        // then
        assertTrue(pattern.isDefaultVisible());
    }

    @Test
    public void shouldHaveHasDescriptionFlag() throws Exception {
        // given
        AbstractComponentPattern pattern = new TextInputComponentPattern("testName", null, null, null);
        pattern.setHasDescription(true);

        // then
        assertTrue(pattern.isHasDescription());
    }

    @Test
    public void shouldHaveReferenceName() throws Exception {
        // given
        AbstractComponentPattern pattern = new TextInputComponentPattern("testName", null, null, null);
        pattern.setReference("uniqueReferenceName");

        // then
        assertEquals("uniqueReferenceName", pattern.getReference());
    }

    @Test
    public void shouldHaveEmptyOptions() throws Exception {
        // given
        AbstractComponentPattern pattern = new TextInputComponentPattern("testName", null, null, null);

        // when
        JSONObject options = pattern.getStaticJavaScriptOptions();

        // then
        assertEquals(0, options.length());
    }

    @Test
    public void shouldHaveOptionsWhenAdded() throws Exception {
        // given
        AbstractComponentPattern pattern = new TextInputComponentPattern("testName", null, null, null);
        pattern.addStaticJavaScriptOption("test1", "testVal");
        pattern.addStaticJavaScriptOption("test2", 3);
        JSONObject obj = new JSONObject();
        pattern.addStaticJavaScriptOption("test3", obj);

        // when
        JSONObject options = pattern.getStaticJavaScriptOptions();

        // then
        assertEquals("testVal", options.get("test1"));
        assertEquals(3, options.get("test2"));
        assertEquals(obj, options.get("test3"));
    }

    @Test
    public void shouldHaveListenersInOptions() throws Exception {
        // given
        // given
        ViewDefinition vd = mock(ViewDefinition.class);

        ComponentPatternMock pattern = new ComponentPatternMock("f1", null, null, null);

        ComponentPatternMock t1 = new ComponentPatternMock("t1", "t1", null, pattern);
        ComponentPatternMock t2 = new ComponentPatternMock("t2", "t2", null, pattern);
        ComponentPatternMock t3 = new ComponentPatternMock("t3", null, "t3", pattern);

        pattern.addChild(t1);
        pattern.addChild(t2);
        pattern.addChild(t3);

        pattern.initialize(vd);

        // when
        JSONObject options = pattern.getStaticJavaScriptOptions();

        // then
        assertEquals(1, options.length());

        JSONArray listenersArray = options.getJSONArray("listeners");
        assertEquals(3, listenersArray.length());
        assertTrue(JsonArrayContain(listenersArray, "f1.t1"));
        assertTrue(JsonArrayContain(listenersArray, "f1.t2"));
        assertTrue(JsonArrayContain(listenersArray, "f1.t3"));
    }

    private boolean JsonArrayContain(JSONArray array, String value) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            if (array.getString(i).equals(value)) {
                return true;
            }
        }
        return false;
    }
}
