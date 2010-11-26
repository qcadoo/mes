package com.qcadoo.mes.newview;

import java.util.Locale;

import org.junit.Test;
import org.mockito.Mockito;

public class AbstractComponentStateTest {

    @Test
    public void shouldI() throws Exception {
        // given
        ComponentState componentState = Mockito.mock(AbstractComponentState.class, Mockito.CALLS_REAL_METHODS);

        // when
        componentState.initialize(null, Locale.ENGLISH);

        // then
    }

}
