package com.qcadoo.plugin.internal.module;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;

import com.google.common.collect.Lists;
import com.qcadoo.plugin.internal.api.ModuleFactory;
import com.qcadoo.plugin.internal.module.DefaultModuleFactoryAccessor;

public class ModuleFactoryAccessorTest {

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCallPostInitializeOnAllModuleFactories() throws Exception {
        // given
        ModuleFactory<?> moduleFactory1 = mock(ModuleFactory.class);
        given(moduleFactory1.getIdentifier()).willReturn("module1");
        ModuleFactory<?> moduleFactory2 = mock(ModuleFactory.class);
        given(moduleFactory2.getIdentifier()).willReturn("module2");

        DefaultModuleFactoryAccessor moduleFactoryAccessor = new DefaultModuleFactoryAccessor();
        moduleFactoryAccessor.setModuleFactories(Lists.newArrayList(moduleFactory1, moduleFactory2));

        // when
        moduleFactoryAccessor.postInitialize();

        // then
        InOrder inOrder = inOrder(moduleFactory1, moduleFactory2);
        inOrder.verify(moduleFactory1).postInitialize();
        inOrder.verify(moduleFactory2).postInitialize();
    }

    @Test
    public void shouldReturnModuleFactory() throws Exception {
        // given
        ModuleFactory<?> moduleFactory = mock(ModuleFactory.class);
        given(moduleFactory.getIdentifier()).willReturn("module");

        DefaultModuleFactoryAccessor moduleFactoryAccessor = new DefaultModuleFactoryAccessor();
        moduleFactoryAccessor.setModuleFactories(Collections.<ModuleFactory<?>> singletonList(moduleFactory));

        // when
        ModuleFactory<?> mf = moduleFactoryAccessor.getModuleFactory("module");

        // then
        Assert.assertSame(moduleFactory, mf);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfModuleFactoryNotExists() throws Exception {
        // given
        DefaultModuleFactoryAccessor moduleFactoryAccessor = new DefaultModuleFactoryAccessor();
        moduleFactoryAccessor.setModuleFactories(Collections.<ModuleFactory<?>> emptyList());

        // when
        moduleFactoryAccessor.getModuleFactory("module");
    }
}
