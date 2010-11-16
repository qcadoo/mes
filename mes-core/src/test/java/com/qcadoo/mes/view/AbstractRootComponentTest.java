/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.view;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.DataDefinition;

public class AbstractRootComponentTest {

    private DataDefinition dataDefinition;

    private ViewDefinition viewDefinition;

    private TranslationService translationService;

    private ContainerComponent<?> child2;

    private Component<?> child1;

    private CustomAbstractRootComponent root;

    private Component<?> child3;

    @Before
    public void init() {
        dataDefinition = mock(DataDefinition.class);
        viewDefinition = mock(ViewDefinition.class);
        translationService = mock(TranslationService.class);

        child1 = mock(Component.class);
        given(child1.getName()).willReturn("child1Name");
        given(child1.getPath()).willReturn("child1Path");
        given(child1.isInitialized()).willReturn(false, true);

        child2 = mock(ContainerComponent.class);
        given(child2.getName()).willReturn("child2Name");
        given(child2.getPath()).willReturn("child2Path");
        given(child2.isInitialized()).willReturn(false, true);

        child3 = mock(Component.class);
        given(child3.getName()).willReturn("child3Name");
        given(child3.getPath()).willReturn("child2Path.child3Path");
        given(child3.isInitialized()).willReturn(false, false, true);

        given(child2.getComponents()).willReturn(ImmutableMap.<String, Component<?>> of("child3Name", child3));

        root = new CustomAbstractRootComponent("root", dataDefinition, viewDefinition, translationService);
        root.addComponent(child1);
        root.addComponent(child2);

        root.initialize();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldBeValidAndInitialized() throws Exception {
        // then
        assertEquals(dataDefinition, root.getDataDefinition());
        assertEquals(viewDefinition, root.getViewDefinition());
        assertTrue(root.isInitialized());
        assertTrue(root.isContainer());
        assertEquals(2, root.getComponents().size());
        assertEquals(child1, root.getComponents().get("child1Name"));
        assertEquals(child2, root.getComponents().get("child2Name"));

        verify(child1).getPath();
        verify(child2).getPath();
        verify(child3).getPath();
        verify(child2).getComponents();
        verify(child1).initializeComponent(Mockito.anyMap());
        verify(child2).initializeComponent(Mockito.anyMap());
        verify(child3, times(2)).initializeComponent(Mockito.anyMap());
    }

    @Test
    public void shouldLookupComponent() throws Exception {
        // then
        assertEquals(child3, root.lookupComponent("child2Path.child3Path"));
        assertNull(root.lookupComponent("child2Path.xxx"));
    }

    @Test
    public void shouldLookupListenersForNotExistedPath() throws Exception {
        // then
        assertEquals(0, root.lookupListeners("child2Path.xxx").size());
    }

    @Test
    public void shouldLookupListenersForComponent() throws Exception {
        // given
        given(child1.getListeners()).willReturn(Sets.newHashSet("child2Path.child3Path"));
        given(child3.getListeners()).willReturn(Sets.newHashSet("child1Path"));

        // when
        Set<String> listeners = root.lookupListeners("child1Path");

        // then
        assertThat(listeners, JUnitMatchers.hasItems("child1Path", "child2Path.child3Path"));
        verify(child1, Mockito.times(2)).getListeners();
        verify(child3, Mockito.times(1)).getListeners();
    }

    @Test
    public void shouldLookupListenersForContainer() throws Exception {
        // given
        given(child1.getListeners()).willReturn(Collections.<String> emptySet());
        given(child2.getListeners()).willReturn(Collections.<String> emptySet());
        given(child3.getListeners()).willReturn(Sets.newHashSet("child1Path"));

        // when
        Set<String> listeners = root.lookupListeners("child2Path");

        // then
        assertThat(listeners, JUnitMatchers.hasItems("child1Path"));
        verify(child1, Mockito.times(1)).getListeners();
        verify(child2, Mockito.times(1)).getListeners();
        verify(child3, Mockito.times(1)).getListeners();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailedIfCantInitializeComponent() throws Exception {
        // given
        root = new CustomAbstractRootComponent("root", dataDefinition, viewDefinition, translationService);
        root.addComponent(child1);
        root.addComponent(child2);

        given(child2.isInitialized()).willReturn(false);

        root.initialize();
    }

    private static class CustomAbstractRootComponent extends AbstractRootComponent {

        public CustomAbstractRootComponent(final String name, final DataDefinition dataDefinition,
                final ViewDefinition viewDefinition, final TranslationService translationService) {
            super(name, dataDefinition, viewDefinition, translationService);
        }

        @Override
        public String getType() {
            return "root";
        }

    }

}
