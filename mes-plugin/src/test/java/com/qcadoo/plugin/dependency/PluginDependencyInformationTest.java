package com.qcadoo.plugin.dependency;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.qcadoo.plugin.VersionUtils;

public class PluginDependencyInformationTest {

    PluginDependencyInformation dependencyInformation1;

    PluginDependencyInformation dependencyInformation2;

    PluginDependencyInformation dependencyInformation3;

    PluginDependencyInformation dependencyInformation4;

    PluginDependencyInformation dependencyInformation5;

    @Before
    public void init() {
        dependencyInformation1 = new PluginDependencyInformation("testPlugin1", VersionUtils.parse("1"), true,
                VersionUtils.parse("1.2.01"), false);
        dependencyInformation2 = new PluginDependencyInformation("testPlugin2", null, true, VersionUtils.parse("2.2.01"),
                true);
        dependencyInformation3 = new PluginDependencyInformation("testPlugin3", VersionUtils.parse("1.2.10"), true, null,
                false);
        dependencyInformation4 = new PluginDependencyInformation("testPlugin4", VersionUtils.parse("3.0.1"), false,
                VersionUtils.parse("4.2"), true);
        dependencyInformation5 = new PluginDependencyInformation("testPlugin5", VersionUtils.parse("3.0.1"), true,
                VersionUtils.parse("3.0.1"), true);
    }

    @Test
    public void shouldThrowExceptionWhenWrongVersions() throws Exception {
        // given

        // when
        try {
            new PluginDependencyInformation("", VersionUtils.parse("a1"), true, VersionUtils.parse("1"), false);
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            new PluginDependencyInformation("", VersionUtils.parse("1"), true, VersionUtils.parse("2s"), false);
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            new PluginDependencyInformation("", VersionUtils.parse("1.2.3.4"), true, VersionUtils.parse("2s"),
                    false);
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            new PluginDependencyInformation("", VersionUtils.parse("2"), true, VersionUtils.parse("1.2.3.4"), false);
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            new PluginDependencyInformation("", VersionUtils.parse("1.1.1"), true, VersionUtils.parse("1.1.0"),
                    false);
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            new PluginDependencyInformation("", VersionUtils.parse("1.0.0"), false, VersionUtils.parse("1"), true);
            Assert.fail();
        } catch (Exception e) {
        }

        // then
    }

    @Test
    public void shouldReturnTrueWhenVersionIsSattisfied() throws Exception {
        // given
        int[] v1 = VersionUtils.parse("1.1");
        int[] v2 = VersionUtils.parse("0.9");
        int[] v3 = VersionUtils.parse("1.2.10");
        int[] v4 = VersionUtils.parse("4.2.0");
        int[] v5 = VersionUtils.parse("3.0.1");

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
        int[] v1 = VersionUtils.parse("0.9");
        int[] v2 = VersionUtils.parse("2.3");
        int[] v3 = VersionUtils.parse("1.2.09");
        int[] v4 = VersionUtils.parse("3.0.1");
        int[] v5 = VersionUtils.parse("3.0.2");

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
