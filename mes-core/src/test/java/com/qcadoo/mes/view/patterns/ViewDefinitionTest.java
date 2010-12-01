package com.qcadoo.mes.view.patterns;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ContainerPattern;
import com.qcadoo.mes.view.ContainerState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.TextInputComponentPattern;
import com.qcadoo.mes.view.internal.ViewDefinitionImpl;
import com.qcadoo.mes.view.internal.ViewDefinitionStateFactory;

public class ViewDefinitionTest {

    @Test
    public void shouldHaveChildrenWhenAddSome() throws Exception {
        // given
        ViewDefinitionImpl vd = new ViewDefinitionImpl("name", "plugin", mock(DataDefinition.class), true);
        ComponentPattern child1 = new TextInputComponentPattern("test1", null, null, null);
        ComponentPattern child2 = new TextInputComponentPattern("test2", null, null, null);
        vd.addChild(child1);
        vd.addChild(child2);

        // when
        Map<String, ComponentPattern> children = vd.getChildren();

        // then
        Assert.assertEquals(2, children.size());
        Assert.assertEquals(child1, children.get("test1"));
        Assert.assertEquals(child2, children.get("test2"));
    }

    @Test
    public void shouldReturnChildByName() throws Exception {
        // given
        ViewDefinitionImpl vd = new ViewDefinitionImpl("name", "plugin", mock(DataDefinition.class), true);
        ComponentPattern child1 = new TextInputComponentPattern("test1", null, null, null);
        ComponentPattern child2 = new TextInputComponentPattern("test2", null, null, null);
        vd.addChild(child1);
        vd.addChild(child2);

        // when
        ComponentPattern testChild1 = vd.getChild("test1");
        ComponentPattern testChild2 = vd.getChild("test2");
        ComponentPattern testChild3 = vd.getChild("test3");

        // then
        Assert.assertEquals(child1, testChild1);
        Assert.assertEquals(child2, testChild2);
        Assert.assertNull(testChild3);
    }

    @Test
    public void shouldCallInitializeOnChildren() throws Exception {
        // given
        ViewDefinitionImpl vd = new ViewDefinitionImpl("name", "plugin", mock(DataDefinition.class), true);
        ComponentPattern child1 = Mockito.mock(ComponentPattern.class);
        given(child1.getName()).willReturn("test1");
        ComponentPattern child2 = Mockito.mock(ComponentPattern.class);
        given(child2.getName()).willReturn("test2");
        vd.addChild(child1);
        vd.addChild(child2);

        // when
        vd.initialize();

        // then
        Mockito.verify(child1).initialize(vd);
        Mockito.verify(child2).initialize(vd);
    }

    @Test
    public void shouldFindComponentByPathWhenOneStep() throws Exception {
        // given
        ViewDefinitionImpl vd = new ViewDefinitionImpl("name", "plugin", mock(DataDefinition.class), true);
        ComponentPattern child1 = Mockito.mock(ComponentPattern.class);
        given(child1.getName()).willReturn("test1");
        vd.addChild(child1);

        // when
        ComponentPattern testChild1 = vd.getComponentByPath("test1");

        // then
        Assert.assertEquals(child1, testChild1);
    }

    @Test
    public void shouldFindComponentByPathWhenMoreThanOneStep() throws Exception {
        // given
        ViewDefinitionImpl vd = new ViewDefinitionImpl("name", "plugin", mock(DataDefinition.class), true);

        ContainerPattern child1 = Mockito.mock(ContainerPattern.class);
        ContainerPattern child2 = Mockito.mock(ContainerPattern.class);
        ContainerPattern child3 = Mockito.mock(ContainerPattern.class);

        given(child1.getName()).willReturn("test1");
        given(child1.getChild("test2")).willReturn(child2);
        given(child2.getChild("test3")).willReturn(child3);

        vd.addChild(child1);

        // when
        ComponentPattern testChild1 = vd.getComponentByPath("test1.test2.test3");

        // then
        Assert.assertEquals(child3, testChild1);
    }

    @Test
    public void shouldReturnNullWhenNoComponentFound() throws Exception {
        // given
        ViewDefinitionImpl vd = new ViewDefinitionImpl("name", "plugin", mock(DataDefinition.class), true);

        ContainerPattern child1 = Mockito.mock(ContainerPattern.class);
        ContainerPattern child2 = Mockito.mock(ContainerPattern.class);
        ContainerPattern child3 = Mockito.mock(ContainerPattern.class);

        given(child1.getName()).willReturn("test1");
        given(child1.getChild("test2")).willReturn(child2);
        given(child2.getChild("test3")).willReturn(child3);

        vd.addChild(child1);

        // when
        ComponentPattern testChild1 = vd.getComponentByPath("test1.test2.test4");

        // then
        Assert.assertNull(testChild1);
    }

    @Test
    public void shouldCallEvent() throws Exception {
        // given
        ViewDefinitionImpl vd = new ViewDefinitionImpl("name", "plugin", mock(DataDefinition.class), true);

        AbstractContainerPattern child1 = Mockito.mock(AbstractContainerPattern.class);
        AbstractContainerPattern child2 = Mockito.mock(AbstractContainerPattern.class);
        given(child1.getName()).willReturn("test1");
        given(child2.getName()).willReturn("test2");
        vd.addChild(child1);
        vd.addChild(child2);

        ContainerState child1state = Mockito.mock(ContainerState.class);
        ContainerState child2state = Mockito.mock(ContainerState.class);
        given(child1.createComponentState()).willReturn(child1state);
        given(child2.createComponentState()).willReturn(child2state);

        ViewDefinitionState vds = Mockito.mock(ViewDefinitionState.class);
        vd.setViewDefinitionStateFactory(new TestViewDefinitionStateFactory(vds));
        JSONObject resultObj = new JSONObject();
        BDDMockito.given(vds.render()).willReturn(resultObj);

        JSONObject obj = new JSONObject();
        JSONObject event = new JSONObject();
        obj.put("event", event);
        event.put("name", "TestEvent");
        event.put("component", "TestComponent");
        JSONArray eventArgs = new JSONArray();
        eventArgs.put("arg1");
        eventArgs.put("arg2");
        event.put("args", eventArgs);

        // when
        JSONObject result = vd.performEvent(obj, Locale.ENGLISH);

        // then
        Mockito.verify(vds).addChild(child1state);
        Mockito.verify(vds).addChild(child2state);
        Mockito.verify(vds).initialize(obj, Locale.ENGLISH);
        Mockito.verify(child1).updateComponentStateListeners(vds);
        Mockito.verify(child2).updateComponentStateListeners(vds);
        Mockito.verify(vds).performEvent("TestComponent", "TestEvent", new String[] { "arg1", "arg2" });
        Mockito.verify(vds).beforeRender();
        Mockito.verify(vds).render();
        Assert.assertEquals(resultObj, result);
    }

    @Test
    public void shouldReturnValidJavaScriptFilesSet() throws Exception {
        // given
        ViewDefinitionImpl vd = new ViewDefinitionImpl("name", "plugin", mock(DataDefinition.class), true);

        ContainerPattern child1 = Mockito.mock(ContainerPattern.class);
        ContainerPattern child2 = Mockito.mock(ContainerPattern.class);
        ComponentPattern child3 = Mockito.mock(ComponentPattern.class);

        Map<String, ComponentPattern> childrenMap = new HashMap<String, ComponentPattern>();
        childrenMap.put("child2", child2);
        childrenMap.put("child3", child3);

        given(child1.getName()).willReturn("test1");
        given(child1.getChildren()).willReturn(childrenMap);

        vd.addChild(child1);

        given(child1.getJavaScriptFilePath()).willReturn("testPath1");
        given(child2.getJavaScriptFilePath()).willReturn("testPath2");
        given(child3.getJavaScriptFilePath()).willReturn("testPath1");

        // when
        Set<String> paths = vd.getJavaScriptFilePaths();

        // then
        Assert.assertEquals(2, paths.size());
        Assert.assertTrue(paths.contains("testPath1"));
        Assert.assertTrue(paths.contains("testPath2"));
    }

    private class TestViewDefinitionStateFactory implements ViewDefinitionStateFactory {

        private final ViewDefinitionState vds;

        public TestViewDefinitionStateFactory(final ViewDefinitionState vds) {
            this.vds = vds;
        }

        @Override
        public ViewDefinitionState getInstance() {
            return vds;
        }

    }
}
