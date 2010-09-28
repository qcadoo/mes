package com.qcadoo.mes.core;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.core.api.PluginManagementService;
import com.qcadoo.mes.core.internal.PluginManagementServiceImpl;

public final class PluginManagementServiceTest {

    private final SessionFactory sessionFactory = mock(SessionFactory.class, RETURNS_DEEP_STUBS);

    private PluginManagementService pluginManagementService = null;

    @Before
    public void init() {
        pluginManagementService = new PluginManagementServiceImpl();
        ReflectionTestUtils.setField(pluginManagementService, "sessionFactory", sessionFactory);
    }

    @Test
    public void shouldReturnListOfPlugins() {
        // given
        PluginsPlugin pluginsPlugin1 = new PluginsPlugin();
        pluginsPlugin1.setName("plugin1");
        PluginsPlugin pluginsPlugin2 = new PluginsPlugin();
        pluginsPlugin2.setName("plugin2");
        PluginsPlugin pluginsPlugin3 = new PluginsPlugin();
        pluginsPlugin3.setName("plugin3");

        given(
                sessionFactory.getCurrentSession().createCriteria(PluginsPlugin.class).add(any(Criterion.class))
                        .add(any(Criterion.class)).list()).willReturn(
                newArrayList(pluginsPlugin1, pluginsPlugin2, pluginsPlugin3));

        // when
        List<PluginsPlugin> plugins = pluginManagementService.getActivePlugins();

        // then
        assertThat(plugins.size(), equalTo(3));
        assertThat(plugins.get(0).getName(), equalTo("plugin1"));
        assertThat(plugins.get(1).getName(), equalTo("plugin2"));
        assertThat(plugins.get(2).getName(), equalTo("plugin3"));
    }

    @Test
    public void shouldReturnValidPluginByIdentifierAndStatus() {
        // given
        PluginsPlugin plugin = new PluginsPlugin();
        plugin.setName("plugins");
        plugin.setDeleted(false);
        plugin.setStatus("active");

        given(
                sessionFactory.getCurrentSession().createCriteria(PluginsPlugin.class).add(any(Criterion.class))
                        .add(any(Criterion.class)).add(any(Criterion.class)).uniqueResult()).willReturn(plugin);

        // when
        PluginsPlugin databasePlugin = pluginManagementService.getPluginByIdentifierAndStatus("plugins", "active");

        // then
        assertEquals("plugins", databasePlugin.getName());
        assertEquals(false, databasePlugin.isDeleted());
        assertEquals("active", databasePlugin.getStatus());
    }

    @Test
    public void shouldReturnValidPluginByIdentifier() {
        // given
        PluginsPlugin plugin = new PluginsPlugin();
        plugin.setName("plugins");
        plugin.setDeleted(false);
        plugin.setStatus("active");

        given(
                sessionFactory.getCurrentSession().createCriteria(PluginsPlugin.class).add(any(Criterion.class))
                        .add(any(Criterion.class)).uniqueResult()).willReturn(plugin);

        // when
        PluginsPlugin databasePlugin = pluginManagementService.getPluginByIdentifier("plugins");

        // then
        assertEquals("plugins", databasePlugin.getName());
        assertEquals(false, databasePlugin.isDeleted());
        assertEquals("active", databasePlugin.getStatus());
    }

    @Test
    public void shouldReturnValidPluginById() {
        // given
        PluginsPlugin plugin = new PluginsPlugin();
        plugin.setName("plugins");
        plugin.setDeleted(false);
        plugin.setStatus("active");

        given(
                sessionFactory.getCurrentSession().createCriteria(PluginsPlugin.class).add(any(Criterion.class))
                        .add(any(Criterion.class)).uniqueResult()).willReturn(plugin);

        // when
        PluginsPlugin databasePlugin = pluginManagementService.getPluginById("1");

        // then
        assertEquals("plugins", databasePlugin.getName());
        assertEquals(false, databasePlugin.isDeleted());
        assertEquals("active", databasePlugin.getStatus());
    }

    @Test
    public void shouldReturnValidPluginByNameAndVendor() {
        // given
        PluginsPlugin plugin = new PluginsPlugin();
        plugin.setName("plugins");
        plugin.setDeleted(false);
        plugin.setStatus("active");

        given(
                sessionFactory.getCurrentSession().createCriteria(PluginsPlugin.class).add(any(Criterion.class))
                        .add(any(Criterion.class)).add(any(Criterion.class)).uniqueResult()).willReturn(plugin);

        // when
        PluginsPlugin databasePlugin = pluginManagementService.getPluginByNameAndVendor("name", "vendor");

        // then
        assertEquals("plugins", databasePlugin.getName());
        assertEquals(false, databasePlugin.isDeleted());
        assertEquals("active", databasePlugin.getStatus());
    }

    @Test
    public void shouldSaveNewPlugin() throws Exception {
        // given
        PluginsPlugin plugin = new PluginsPlugin();
        plugin.setName("plugins");
        plugin.setDeleted(false);
        plugin.setStatus("active");

        // when
        pluginManagementService.savePlugin(plugin);

        // then
        verify(sessionFactory.getCurrentSession()).save(plugin);

    }

    @Test
    public void shouldSaveExistingPlugin() throws Exception {
        // given
        PluginsPlugin databasePlugin = new PluginsPlugin();
        databasePlugin.setName("plugins");
        databasePlugin.setDeleted(false);
        databasePlugin.setStatus("active");

        given(
                sessionFactory.getCurrentSession().createCriteria(PluginsPlugin.class).add(any(Criterion.class))
                        .add(any(Criterion.class)).uniqueResult()).willReturn(databasePlugin);

        PluginsPlugin plugin = new PluginsPlugin();
        plugin.setName("products");
        plugin.setDeleted(false);
        plugin.setStatus("active");

        // when
        pluginManagementService.savePlugin(plugin);

        // then
        verify(sessionFactory.getCurrentSession()).save(plugin);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfPluginIdentifierIsNullAndStatusIsValid() {
        // when
        pluginManagementService.getPluginByIdentifierAndStatus(null, "active");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfPluginStatusIsNull() {
        // when
        pluginManagementService.getPluginByIdentifierAndStatus("plugins", null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfPluginIdentifierIsNull() {
        // when
        pluginManagementService.getPluginByIdentifier(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfPluginIdIsNull() {
        // when
        pluginManagementService.getPluginById(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfPluginNameIsNull() {
        // when
        pluginManagementService.getPluginByNameAndVendor(null, "vendor");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfPluginVendorIsNull() {
        // when
        pluginManagementService.getPluginByNameAndVendor("name", null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfPluginIsNull() {
        // when
        pluginManagementService.savePlugin(null);
    }
}
