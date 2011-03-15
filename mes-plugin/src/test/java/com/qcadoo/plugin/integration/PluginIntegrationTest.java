package com.qcadoo.plugin.integration;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.api.Version;
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

    private SessionFactory sessionFactory;

    @Before
    public void init() throws Exception {
        new File("target/plugins").mkdir();
        new File("target/tmpPlugins").mkdir();

        applicationContext = new ClassPathXmlApplicationContext("com/qcadoo/plugin/integration/spring.xml");
        applicationContext.registerShutdownHook();

        pluginResolver = applicationContext.getBean(PluginDescriptorResolver.class);
        pluginAccessor = applicationContext.getBean(PluginAccessor.class);
        pluginManager = applicationContext.getBean(PluginManager.class);
        pluginFileManager = applicationContext.getBean(PluginFileManager.class);
        sessionFactory = applicationContext.getBean(SessionFactory.class);
    }

    @After
    public void destroy() throws Exception {
        sessionFactory.openSession().createSQLQuery("delete from plugins_plugin").executeUpdate();
        pluginResolver = null;
        pluginResolver = null;
        pluginManager = null;
        pluginFileManager = null;
        sessionFactory.close();
        sessionFactory = null;
        applicationContext.close();
        deleteDirectory(new File("target/plugins"));
        deleteDirectory(new File("target/tmpPlugins"));
    }

    @Test
    public void shouldHavePlugins() throws Exception {
        // then
        assertEquals(3, pluginAccessor.getPlugins().size());
        assertEquals(3, pluginAccessor.getEnabledPlugins().size());
        assertNotNull(pluginAccessor.getPlugin("plugin1"));
        assertNotNull(pluginAccessor.getPlugin("plugin2"));
        assertNotNull(pluginAccessor.getPlugin("plugin3"));
        assertNotNull(pluginAccessor.getEnabledPlugin("plugin1"));
        assertNotNull(pluginAccessor.getEnabledPlugin("plugin2"));
        assertNotNull(pluginAccessor.getEnabledPlugin("plugin3"));
    }

    @Test
    public void shouldEnablePlugin() throws Exception {
        // given
        pluginManager.disablePlugin("plugin1", "plugin2");

        // when
        PluginOperationResult result = pluginManager.enablePlugin("plugin1");

        // then
        assertTrue(result.isSuccess());
        assertFalse(result.isRestartNeccessary());
        assertEquals(2, pluginAccessor.getEnabledPlugins().size());
        assertNotNull(pluginAccessor.getEnabledPlugin("plugin1"));
        assertNull(pluginAccessor.getEnabledPlugin("plugin2"));
    }

    @Test
    public void shouldNotEnablePluginWithDisabledDependencies() throws Exception {
        // given
        pluginManager.disablePlugin("plugin1", "plugin2");

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
        pluginManager.disablePlugin("plugin2");

        // when
        PluginOperationResult result = pluginManager.enablePlugin("plugin2");

        System.out.println(" -----> " + result.getStatus());

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

    @Test
    public void shouldNotDisablePluginWithEnabledDependency() throws Exception {
        // given
        pluginManager.enablePlugin("plugin1", "plugin2");

        // when
        PluginOperationResult result = pluginManager.disablePlugin("plugin1");

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

    @Test
    public void shouldInstallPlugin() throws Exception {
        // given
        JarPluginArtifact artifact = new JarPluginArtifact(new File(
                "src/test/resources/com/qcadoo/plugin/integration/plugin4.jar"));

        // when
        PluginOperationResult result = pluginManager.installPlugin(artifact);

        // then
        assertTrue(result.isSuccess());
        assertFalse(result.isRestartNeccessary());
        assertNotNull(pluginAccessor.getPlugin("plugin4"));
        assertTrue(pluginAccessor.getPlugin("plugin4").hasState(PluginState.TEMPORARY));
    }

    @Test
    public void shouldEnableInstalledPlugin() throws Exception {
        // given
        JarPluginArtifact artifact = new JarPluginArtifact(new File(
                "src/test/resources/com/qcadoo/plugin/integration/plugin4.jar"));
        pluginManager.installPlugin(artifact);

        // when
        PluginOperationResult result = pluginManager.enablePlugin("plugin4");

        // then
        assertTrue(result.isSuccess());
        assertTrue(result.isRestartNeccessary());
        assertNotNull(pluginAccessor.getPlugin("plugin4"));
        assertTrue(pluginAccessor.getPlugin("plugin4").hasState(PluginState.ENABLING));
    }

    @Test
    public void shouldUninstallPlugin() throws Exception {
        // given
        JarPluginArtifact artifact = new JarPluginArtifact(new File(
                "src/test/resources/com/qcadoo/plugin/integration/plugin4.jar"));
        pluginManager.installPlugin(artifact);
        pluginManager.enablePlugin("plugin4");

        // when
        PluginOperationResult result = pluginManager.uninstallPlugin("plugin4");

        // then
        assertTrue(result.isSuccess());
        assertTrue(result.isRestartNeccessary());
        assertNull(pluginAccessor.getPlugin("plugin4"));
    }

    @Test
    public void shouldUpdateEnabledPlugin() throws Exception {
        // given
        JarPluginArtifact artifact = new JarPluginArtifact(new File(
                "src/test/resources/com/qcadoo/plugin/integration/plugin4.jar"));
        JarPluginArtifact artifact2 = new JarPluginArtifact(new File(
                "src/test/resources/com/qcadoo/plugin/integration/plugin4.1.jar"));

        pluginManager.installPlugin(artifact);
        pluginManager.enablePlugin("plugin4");
        pluginAccessor.getPlugin("plugin4").changeStateTo(PluginState.ENABLED);

        // when
        PluginOperationResult result = pluginManager.installPlugin(artifact2);

        // then
        assertTrue(result.isSuccess());
        assertTrue(result.isRestartNeccessary());
        assertNotNull(pluginAccessor.getPlugin("plugin4"));
        assertTrue(pluginAccessor.getPlugin("plugin4").hasState(PluginState.ENABLING));
        assertEquals(new Version("1.2.4"), pluginAccessor.getPlugin("plugin4").getVersion());
    }

    @Test
    public void shouldUpdateTemporaryPlugin() throws Exception {
        // given
        JarPluginArtifact artifact = new JarPluginArtifact(new File(
                "src/test/resources/com/qcadoo/plugin/integration/plugin4.jar"));
        JarPluginArtifact artifact2 = new JarPluginArtifact(new File(
                "src/test/resources/com/qcadoo/plugin/integration/plugin4.1.jar"));

        pluginManager.installPlugin(artifact);

        // when
        PluginOperationResult result = pluginManager.installPlugin(artifact2);

        // then
        assertTrue(result.isSuccess());
        assertFalse(result.isRestartNeccessary());
        assertNotNull(pluginAccessor.getPlugin("plugin4"));
        assertTrue(pluginAccessor.getPlugin("plugin4").hasState(PluginState.TEMPORARY));
        assertEquals(new Version("1.2.4"), pluginAccessor.getPlugin("plugin4").getVersion());
    }

    @Test
    public void shouldNotDisableTemporaryPlugin() throws Exception {
        // given
        JarPluginArtifact artifact = new JarPluginArtifact(new File(
                "src/test/resources/com/qcadoo/plugin/integration/plugin4.jar"));

        pluginManager.installPlugin(artifact);

        // when
        PluginOperationResult result = pluginManager.disablePlugin("plugin4");

        // then
        assertTrue(result.isSuccess());
        assertNotNull(pluginAccessor.getPlugin("plugin4"));
        assertTrue(pluginAccessor.getPlugin("plugin4").hasState(PluginState.TEMPORARY));
    }

    @Test
    public void shouldNotDowngradePlugin() throws Exception {
        // given
        JarPluginArtifact artifact = new JarPluginArtifact(new File(
                "src/test/resources/com/qcadoo/plugin/integration/plugin4.jar"));
        JarPluginArtifact artifact2 = new JarPluginArtifact(new File(
                "src/test/resources/com/qcadoo/plugin/integration/plugin4.1.jar"));

        pluginManager.installPlugin(artifact2);

        // when
        PluginOperationResult result = pluginManager.installPlugin(artifact);

        // then
        assertFalse(result.isSuccess());
        assertEquals(PluginOperationStatus.INCORRECT_VERSION_PLUGIN, result.getStatus());
        assertNotNull(pluginAccessor.getPlugin("plugin4"));
        assertTrue(pluginAccessor.getPlugin("plugin4").hasState(PluginState.TEMPORARY));
        assertEquals(new Version("1.2.4"), pluginAccessor.getPlugin("plugin4").getVersion());
    }

    // TODO test all update cases

}