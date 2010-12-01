/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
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

package com.qcadoo.mes.internal.plugins;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.api.PluginManagementService;
import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.internal.PluginManagementServiceImpl;

public final class PluginManagementServiceTest {

    private final SessionFactory sessionFactory = mock(SessionFactory.class, RETURNS_DEEP_STUBS);

    private PluginManagementService pluginManagementService = null;

    @Before
    public void init() {
        pluginManagementService = new PluginManagementServiceImpl();
        ReflectionTestUtils.setField(pluginManagementService, "sessionFactory", sessionFactory);
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
        PluginsPlugin databasePlugin = pluginManagementService.getByIdentifierAndStatus("plugins", "active");

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
        PluginsPlugin databasePlugin = pluginManagementService.getByIdentifier("plugins");

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
        PluginsPlugin databasePlugin = pluginManagementService.get(1L);

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
        PluginsPlugin databasePlugin = pluginManagementService.getByNameAndVendor("name", "vendor");

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
        pluginManagementService.save(plugin);

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
        pluginManagementService.save(plugin);

        // then
        verify(sessionFactory.getCurrentSession()).save(plugin);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfPluginIdentifierIsNullAndStatusIsValid() {
        // when
        pluginManagementService.getByIdentifierAndStatus(null, "active");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfPluginStatusIsNull() {
        // when
        pluginManagementService.getByIdentifierAndStatus("plugins", null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfPluginIdentifierIsNull() {
        // when
        pluginManagementService.getByIdentifier(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfPluginIdIsNull() {
        // when
        pluginManagementService.get(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfPluginNameIsNull() {
        // when
        pluginManagementService.getByNameAndVendor(null, "vendor");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfPluginVendorIsNull() {
        // when
        pluginManagementService.getByNameAndVendor("name", null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfPluginIsNull() {
        // when
        pluginManagementService.save(null);
    }
}
