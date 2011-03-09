package com.qcadoo.plugin;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Runtime.class })
public class PluginServerTest {

    private DefaultPluginServerManager defaultPluginServerManager;

    @Before
    public void init() throws IOException {
        defaultPluginServerManager = new DefaultPluginServerManager();
        defaultPluginServerManager.setRestartCommand("cd");
    }
/*
    @Test
    public void shouldRestartServer() throws Exception {
        // given
        mockStatic(Runtime.class);

        Runtime runtime = mock(Runtime.class);
        Process process = mock(Process.class);

        // when(Runtime.getRuntime()).thenReturn(runtime);
        given(runtime.exec("cd")).willReturn(process);

        // when
        defaultPluginServerManager.restart();

        // then
        verify(process).waitFor();
        verify(process).exitValue();
    }

    @Test(expected = PluginException.class)
    public void shouldThrowExceptionOnRestartServerWhenIOExceptionThrown() throws Exception {
        // given
        defaultPluginServerManager.setRestartCommand("restart");
        mockStatic(Runtime.class);

        Runtime runtime = mock(Runtime.class);

        when(Runtime.getRuntime()).thenReturn(runtime);

        // when
        defaultPluginServerManager.restart();

        // then

    }

    @Test(expected = PluginException.class)
    public void shouldThrowExceptionOnRestartServerWhenInterruptExceptionThrown() throws Exception {
        // given
        mockStatic(Runtime.class);

        Runtime runtime = mock(Runtime.class);

        when(Runtime.getRuntime()).thenReturn(runtime);
        given(runtime.exec("cd")).willThrow(new InterruptedException());

        // when
        defaultPluginServerManager.restart();

        // then
    }*/
}
