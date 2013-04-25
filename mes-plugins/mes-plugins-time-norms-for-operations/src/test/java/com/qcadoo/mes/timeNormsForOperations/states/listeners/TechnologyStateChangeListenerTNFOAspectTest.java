package com.qcadoo.mes.timeNormsForOperations.states.listeners;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TechnologyStateChangeListenerTNFOAspectTest {

    @Test
    public final void checkAopXmlEntryConsistency() {
        final String expectedCanonicalName = TechnologyStateChangeListenerTNFOAspect.class.getCanonicalName();
        final String nameCopiedFromAopXml = "com.qcadoo.mes.timeNormsForOperations.states.listeners.TechnologyStateChangeListenerTNFOAspect";
        assertEquals(expectedCanonicalName, nameCopiedFromAopXml);
    }

}
