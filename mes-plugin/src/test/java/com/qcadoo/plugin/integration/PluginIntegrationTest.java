package com.qcadoo.plugin.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.plugin.internal.api.PluginDescriptorResolver;
import com.qcadoo.plugin.internal.api.PluginFileManager;
import com.qcadoo.plugin.internal.api.PluginOperationResult;
import com.qcadoo.plugin.internal.api.PluginOperationStatus;
import com.qcadoo.plugin.internal.artifact.JarPluginArtifact;

public class PluginIntegrationTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private PluginAccessor pluginAccessor;

    private PluginManager pluginManager;

    private AbstractApplicationContext applicationContext;

    private PluginFileManager pluginFileManager;

    private PluginDescriptorResolver pluginResolver;

    @Before
    public void init() throws Exception {
        File tmpPlugins = folder.newFolder("tmpPlugins");
        File plugins = folder.newFolder("plugins");

        applicationContext = new ClassPathXmlApplicationContext("com/qcadoo/plugin/integration/spring.xml");
        applicationContext.registerShutdownHook();

        pluginResolver = applicationContext.getBean(PluginDescriptorResolver.class);
        pluginAccessor = applicationContext.getBean(PluginAccessor.class);
        pluginManager = applicationContext.getBean(PluginManager.class);
        pluginFileManager = applicationContext.getBean(PluginFileManager.class);
        setField(pluginFileManager, "pluginsPath", plugins.getAbsolutePath());
        setField(pluginFileManager, "pluginsTmpPath", tmpPlugins.getAbsolutePath());
    }

    @After
    public void destroy() throws Exception {
        applicationContext.destroy();
    }

    @Test
    public void shouldHavePlugins() throws Exception {
        // then
        assertEquals(3, pluginAccessor.getPlugins().size());
        assertEquals(0, pluginAccessor.getEnabledPlugins().size());
        assertNotNull(pluginAccessor.getPlugin("plugin1"));
        assertNotNull(pluginAccessor.getPlugin("plugin2"));
        assertNotNull(pluginAccessor.getPlugin("plugin3"));
        assertNull(pluginAccessor.getEnabledPlugin("plugin1"));
        assertNull(pluginAccessor.getEnabledPlugin("plugin2"));
        assertNull(pluginAccessor.getEnabledPlugin("plugin3"));
    }

    @Test
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

    @Test
    public void shouldNotEnablePluginWithDisabledDependencies() throws Exception {
        // given
        System.out.println(pluginManager.disablePlugin("plugin1").getStatus());

        // when
        PluginOperationResult result = pluginManager.enablePlugin("plugin2");

        // then
        assertFalse(result.isSuccess());
        assertFalse(result.isRestartNeccessary());
        assertEquals(1, result.getPluginDependencyResult().getDependenciesToEnable().size());
        assertEquals("plugin1", result.getPluginDependencyResult().getDependenciesToEnable().iterator().next()
                .getDependencyPluginIdentifier());
        assertEquals(PluginOperationStatus.DISABLED_DEPENDENCIES, result.getStatus());
    }

    @Test
    public void shouldEnablePluginWithDependencies() throws Exception {
        // given
        pluginManager.enablePlugin("plugin1");

        // when
        PluginOperationResult result = pluginManager.enablePlugin("plugin2");

        // then
        assertTrue(result.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS, result.getStatus());
    }

    @Test
    public void shouldEnablePluginAndDependency() throws Exception {
        // given
        pluginManager.disablePlugin("plugin1", "plugin2");

        // when
        PluginOperationResult result = pluginManager.enablePlugin("plugin1", "plugin2");

        // then
        assertTrue(result.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS, result.getStatus());
    }

    @Test
    public void shouldDisablePlugin() throws Exception {
        // when
        PluginOperationResult result = pluginManager.disablePlugin("plugin2");

        // then
        assertTrue(result.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS, result.getStatus());
    }

    // @Test
    @Ignore
    public void shouldNotDisablePluginWithEnabledDependency() throws Exception {
        // given
        PluginOperationResult result1 = pluginManager.enablePlugin("plugin1", "plugin2");
        System.out.println(result1.getStatus());

        // when
        PluginOperationResult result = pluginManager.disablePlugin("plugin1");

        System.out.println(result.getStatus());

        // then
        assertFalse(result.isSuccess());
        assertEquals(1, result.getPluginDependencyResult().getDependenciesToDisable().size());
        assertEquals(PluginOperationStatus.ENABLED_DEPENDENCIES, result.getStatus());
    }

    @Test
    public void shouldNotDisableSystemPlugin() throws Exception {
        // given
        pluginManager.enablePlugin("plugin3");

        // when
        PluginOperationResult result = pluginManager.disablePlugin("plugin3");

        // then
        assertFalse(result.isSuccess());
        assertEquals(PluginOperationStatus.SYSTEM_PLUGIN_DISABLING, result.getStatus());
    }

    // @Test
    @Ignore
    public void shouldname() throws Exception {
        // given
        JarPluginArtifact artifact = new JarPluginArtifact(new File(
                "src/test/resources/com/qcadoo/plugin/integration/plugin4.zip"));

        // when
        PluginOperationResult result = pluginManager.installPlugin(artifact);

        System.out.println(result.getStatus());

        // then
        assertTrue(result.isSuccess());
    }

}