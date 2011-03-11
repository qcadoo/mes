package com.qcadoo.plugin.internal.dependencymanager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.qcadoo.plugin.api.PluginDependencyInformation;
import com.qcadoo.plugin.api.Version;
import com.qcadoo.plugin.api.VersionOfDependency;

public class PluginDependencyInformationTest {

    PluginDependencyInformation dependencyInformation1;

    PluginDependencyInformation dependencyInformation2;

    PluginDependencyInformation dependencyInformation3;

    PluginDependencyInformation dependencyInformation4;

    PluginDependencyInformation dependencyInformation5;

    @Before
    public void init() {
        dependencyInformation1 = new PluginDependencyInformation("testPlugin1", new VersionOfDependency("[1,1.2.01)"));
        dependencyInformation2 = new PluginDependencyInformation("testPlugin2", new VersionOfDependency("2.2.01]"));
        dependencyInformation3 = new PluginDependencyInformation("testPlugin3", new VersionOfDependency("[1.2.10"));
        dependencyInformation4 = new PluginDependencyInformation("testPlugin4", new VersionOfDependency("(3.0.1,4.2]"));
        dependencyInformation5 = new PluginDependencyInformation("testPlugin5", new VersionOfDependency("[3.0.1,3.0.1]"));
    }

    @Test
    public void shouldThrowExceptionWhenWrongVersions() throws Exception {
        // given

        // when
        try {
            new PluginDependencyInformation("", new VersionOfDependency("[a1,1)"));
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            new PluginDependencyInformation("", new VersionOfDependency("[1,2s)"));
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            new PluginDependencyInformation("", new VersionOfDependency("[1.2.3.4,2s)"));
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            new PluginDependencyInformation("", new VersionOfDependency("[2,1.2.3.4)"));
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            new PluginDependencyInformation("", new VersionOfDependency("[1.1.1,1.1.0)"));
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            new PluginDependencyInformation("", new VersionOfDependency("(1.0.0,1]"));
            Assert.fail();
        } catch (Exception e) {
        }

        // then
    }

    @Test
    public void shouldReturnTrueWhenVersionIsSattisfied() throws Exception {
        // given
        Version v1 = new Version("1.1");
        Version v2 = new Version("0.9");
        Version v3 = new Version("1.2.10");
        Version v4 = new Version("4.2.0");
        Version v5 = new Version("3.0.1");

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
        Version v1 = new Version("0.9");
        Version v2 = new Version("2.3");
        Version v3 = new Version("1.2.09");
        Version v4 = new Version("3.0.1");
        Version v5 = new Version("3.0.2");

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
