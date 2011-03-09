package com.qcadoo.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Assert;
import org.junit.Test;

public class PluginChangeStateToTest {

    @Test
    public void shouldChangeState() throws Exception {
        assertOperationNotSupported(null, PluginState.UNKNOWN);
        assertOperationSupported(null, PluginState.TEMPORARY, false, false);
        assertOperationSupported(null, PluginState.ENABLING, false, false);
        assertOperationSupported(null, PluginState.ENABLED, false, false);
        assertOperationSupported(null, PluginState.DISABLED, false, false);

        assertOperationNotSupported(PluginState.TEMPORARY, PluginState.DISABLED);
        assertOperationNotSupported(PluginState.TEMPORARY, PluginState.TEMPORARY);
        assertOperationSupported(PluginState.TEMPORARY, PluginState.ENABLING, true, true);
        assertOperationNotSupported(PluginState.TEMPORARY, PluginState.ENABLED);
        assertOperationNotSupported(PluginState.TEMPORARY, PluginState.DISABLED);

        assertOperationNotSupported(PluginState.ENABLING, PluginState.UNKNOWN);
        assertOperationNotSupported(PluginState.ENABLING, PluginState.TEMPORARY);
        assertOperationNotSupported(PluginState.ENABLING, PluginState.ENABLING);
        assertOperationSupported(PluginState.ENABLING, PluginState.ENABLED, true, false);
        assertOperationNotSupported(PluginState.ENABLING, PluginState.DISABLED);

        assertOperationNotSupported(PluginState.ENABLED, PluginState.DISABLED);
        assertOperationNotSupported(PluginState.ENABLED, PluginState.TEMPORARY);
        assertOperationNotSupported(PluginState.ENABLED, PluginState.ENABLING);
        assertOperationNotSupported(PluginState.ENABLED, PluginState.ENABLED);
        assertOperationSupported(PluginState.ENABLED, PluginState.DISABLED, false, true);

        assertOperationNotSupported(PluginState.DISABLED, PluginState.UNKNOWN);
        assertOperationNotSupported(PluginState.DISABLED, PluginState.TEMPORARY);
        assertOperationSupported(PluginState.DISABLED, PluginState.ENABLING, false, false);
        assertOperationSupported(PluginState.DISABLED, PluginState.ENABLED, true, false);
        assertOperationNotSupported(PluginState.DISABLED, PluginState.DISABLED);
    }

    private void assertOperationNotSupported(final PluginState from, final PluginState to) throws Exception {
        // given
        DefaultPlugin plugin = new DefaultPlugin();

        if (from != null) {
            plugin.changeStateTo(from);
        }

        // when
        try {
            plugin.changeStateTo(to);
            Assert.fail();
        } catch (IllegalStateException e) {
            // ignore
        }
    }

    private void assertOperationSupported(final PluginState from, final PluginState to, final boolean callEnable,
            final boolean callDisable) throws Exception {
        // given
        Module module1 = mock(Module.class);
        Module module2 = mock(Module.class);

        DefaultPlugin plugin = new DefaultPlugin();
        plugin.addModule(module1);
        plugin.addModule(module2);

        if (from != null) {
            plugin.changeStateTo(from);
        }

        // when
        plugin.changeStateTo(to);

        // then
        assertEquals(to, plugin.getPluginState());
        assertTrue(plugin.hasState(to));

        if (callEnable) {
            verify(module1).enable();
            verify(module2).enable();
        } else {
            verify(module1, never()).enable();
            verify(module2, never()).enable();
        }
        if (callDisable) {
            verify(module1).disable();
            verify(module2).disable();
        } else {
            verify(module1, never()).disable();
            verify(module2, never()).disable();
        }
    }

}
