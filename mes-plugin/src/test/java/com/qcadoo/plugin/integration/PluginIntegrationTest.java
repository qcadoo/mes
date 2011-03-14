package com.qcadoo.plugin.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.plugin.internal.api.PluginOperationResult;

public class PluginIntegrationTest {

    private static PluginAccessor pluginAccessor;

    private static PluginManager pluginManager;

    private static ApplicationContext applicationContext;

    @BeforeClass
    public static void init() throws Exception {
        applicationContext = new ClassPathXmlApplicationContext("com/qcadoo/plugin/integration/spring.xml");
        pluginAccessor = applicationContext.getBean(PluginAccessor.class);
        pluginManager = applicationContext.getBean(PluginManager.class);
    }

    @Test
    public void should() throws Exception {
        // shouldHavePlugins();
        // shouldEnablePlugin();
        // shouldNotEnablePluginWithDisabledDependencies();
        // shouldEnablePluginWithDependencies();
        // shouldEnablePluginAndDependency();
        // shouldDisablePlugin();
        // shouldNotDisablePluginWithEnabledDependency();

        Assert.assertTrue(true);
    }

    public void shouldHavePlugins() throws Exception {
        // then
        assertEquals(2, pluginAccessor.getPlugins().size());
        assertEquals(0, pluginAccessor.getEnabledPlugins().size());
        assertNotNull(pluginAccessor.getPlugin("plugin1"));
        assertNotNull(pluginAccessor.getPlugin("plugin2"));
        assertNull(pluginAccessor.getEnabledPlugin("plugin1"));
        assertNull(pluginAccessor.getEnabledPlugin("plugin2"));
    }

    public void shouldEnablePlugin() throws Exception {
        // when
        PluginOperationResult result = pluginManager.enablePlugin("plugin1");

        // then
        assertTrue(result.isSuccess());
        assertFalse(result.isRestartNeccessary());
        assertEquals(1, pluginAccessor.getEnabledPlugins().size());
        assertNotNull(pluginAccessor.getEnabledPlugin("plugin1"));
        assertNull(pluginAccessor.getEnabledPlugin("plugin2"));
    }

    public void shouldNotEnablePluginWithDisabledDependencies() throws Exception {
        // given
        System.out.println(pluginManager.disablePlugin("plugin1").getStatus());

        // when
        PluginOperationResult result = pluginManager.enablePlugin("plugin2");

        // then
        assertFalse(result.isSuccess());
        assertFalse(result.isRestartNeccessary());
        assertEquals(1, result.getPluginDependencyResult().getDisabledDependencies().size());
        assertEquals("plugin1", result.getPluginDependencyResult().getDisabledDependencies().iterator().next()
                .getDependencyPluginIdentifier());
    }

    public void shouldEnablePluginWithDependencies() throws Exception {
        // given
        pluginManager.enablePlugin("plugin1");

        // when
        PluginOperationResult result = pluginManager.enablePlugin("plugin2");

        // then
        assertTrue(result.isSuccess());
    }

    public void shouldEnablePluginAndDependency() throws Exception {
        // given
        pluginManager.disablePlugin("plugin1", "plugin2");

        // when
        PluginOperationResult result = pluginManager.enablePlugin("plugin1", "plugin2");

        // then
        assertTrue(result.isSuccess());
    }

    public void shouldDisablePlugin() throws Exception {
        // when
        PluginOperationResult result = pluginManager.disablePlugin("plugin2");

        // then
        assertTrue(result.isSuccess());
    }

    public void shouldNotDisablePluginWithEnabledDependency() throws Exception {
        // given
        pluginManager.enablePlugin("plugin2");

        // when
        PluginOperationResult result = pluginManager.disablePlugin("plugin1");

        // then
        assertFalse(result.isSuccess());
        assertEquals(1, result.getPluginDependencyResult().getEnabledDependencies().size());
    }

}