package com.qcadoo.plugin.dependency;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PluginDependencyInformationTest {

    PluginDependencyInformation dependencyInformation1;

    PluginDependencyInformation dependencyInformation2;

    PluginDependencyInformation dependencyInformation3;

    PluginDependencyInformation dependencyInformation4;

    PluginDependencyInformation dependencyInformation5;

    @Before
    public void init() {
        dependencyInformation1 = new PluginDependencyInformation("testPlugin1", "1", true, "1.2.01", false);
        dependencyInformation2 = new PluginDependencyInformation("testPlugin2", null, true, "2.2.01", true);
        dependencyInformation3 = new PluginDependencyInformation("testPlugin3", "1.2.10", true, null, false);
        dependencyInformation4 = new PluginDependencyInformation("testPlugin4", "3.0.1", false, "4.2", true);
        dependencyInformation5 = new PluginDependencyInformation("testPlugin5", "3.0.1", true, "3.0.1", true);
    }

    @Test
    public void shouldThrowExceptionWhenWrongVersions() throws Exception {
        // given

        // when
        try {
            new PluginDependencyInformation("", "a1", true, "1", false);
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            new PluginDependencyInformation("", "1", true, "2s", false);
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            new PluginDependencyInformation("", "1.2.3.4", true, "2s", false);
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            new PluginDependencyInformation("", "2", true, "1.2.3.4", false);
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            new PluginDependencyInformation("", "1.1.1", true, "1.1.0", false);
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            new PluginDependencyInformation("", "1.0.0", false, "1", true);
            Assert.fail();
        } catch (Exception e) {
        }

        // then
    }

    @Test
    public void shouldReturnTrueWhenVersionIsSattisfied() throws Exception {
        // given
        String v1 = "1.1";
        String v2 = "0.9";
        String v3 = "1.2.10";
        String v4 = "4.2.0";
        String v5 = "3.0.1";

        // when
        boolean res1 = dependencyInformation1.isVersionSattisfied(v1);
        boolean res2 = dependencyInformation2.isVersionSattisfied(v2);
        boolean res3 = dependencyInformation3.isVersionSattisfied(v3);
        boolean res4 = dependencyInformation4.isVersionSattisfied(v4);
        boolean res5 = dependencyInformation5.isVersionSattisfied(v5);

        // then
        Assert.assertTrue(res1);
        Assert.assertTrue(res2);
        Assert.assertTrue(res3);
        Assert.assertTrue(res4);
        Assert.assertTrue(res5);
    }

    @Test
    public void shouldReturnTrueWhenVersionIsNotSattisfied() throws Exception {
        // given
        String v1 = "0.9";
        String v2 = "2.3";
        String v3 = "1.2.09";
        String v4 = "3.0.1";
        String v5 = "3.0.2";

        // when
        boolean res1 = dependencyInformation1.isVersionSattisfied(v1);
        boolean res2 = dependencyInformation2.isVersionSattisfied(v2);
        boolean res3 = dependencyInformation3.isVersionSattisfied(v3);
        boolean res4 = dependencyInformation4.isVersionSattisfied(v4);
        boolean res5 = dependencyInformation5.isVersionSattisfied(v5);

        // then
        Assert.assertFalse(res1);
        Assert.assertFalse(res2);
        Assert.assertFalse(res3);
        Assert.assertFalse(res4);
        Assert.assertFalse(res5);
    }

}
