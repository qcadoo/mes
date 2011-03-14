package com.qcadoo.plugin.internal.descriptorparser;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Node;

import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginDependencyInformation;
import com.qcadoo.plugin.api.Version;
import com.qcadoo.plugin.api.VersionOfDependency;
import com.qcadoo.plugin.internal.DefaultPlugin;
import com.qcadoo.plugin.internal.PluginException;
import com.qcadoo.plugin.internal.api.Module;
import com.qcadoo.plugin.internal.api.ModuleFactory;
import com.qcadoo.plugin.internal.api.ModuleFactoryAccessor;
import com.qcadoo.plugin.internal.api.PluginDescriptorResolver;

public class PluginDescriptorParserTest {

    private ModuleFactoryAccessor moduleFactoryAccessor;

    private PluginDescriptorResolver pluginDescriptorResolver;

    private DefaultPluginDescriptorParser parser;

    private final Resource xmlFile1 = new FileSystemResource("src/test/resources/xml/testPlugin1.xml");

    private final Resource xmlFile2 = new FileSystemResource("src/test/resources/xml/testPlugin2.xml");

    private final Resource xmlFile3 = new FileSystemResource("src/test/resources/xml/testIncorrectPlugin.xml");

    private Module testModule1;

    private Module testModule2;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Before
    public void init() {
        moduleFactoryAccessor = mock(ModuleFactoryAccessor.class);
        pluginDescriptorResolver = mock(PluginDescriptorResolver.class);

        parser = new DefaultPluginDescriptorParser();
        parser.setModuleFactoryAccessor(moduleFactoryAccessor);
        parser.setPluginDescriptorResolver(pluginDescriptorResolver);

        testModule1 = mock(Module.class);
        testModule2 = mock(Module.class);

        ModuleFactory testModule1Factory = mock(ModuleFactory.class);
        ModuleFactory testModule2Factory = mock(ModuleFactory.class);

        given(testModule1Factory.parse(argThat(new HasNodeName("testModule1", "testModule1Content")))).willReturn(testModule1);
        given(testModule2Factory.parse(argThat(new HasNodeName("testModule2", "testModule2Content")))).willReturn(testModule2);

        given(moduleFactoryAccessor.getModuleFactory("testModule1")).willReturn(testModule1Factory);
        given(moduleFactoryAccessor.getModuleFactory("testModule2")).willReturn(testModule2Factory);
    }

    @Test
    public void shouldParseXml1() {
        // given

        // when
        Plugin result = parser.parse(xmlFile1);

        // then
        assertNotNull(result);
    }

    @Test
    public void shouldParseXml2() {
        // given

        // when
        Plugin result = parser.parse(xmlFile2);

        // then
        assertNotNull(result);
    }

    @Test(expected = PluginException.class)
    public void shouldNotParseXml3() {
        // given

        // when
        parser.parse(xmlFile3);

        // then
    }

    @Test
    public void shouldHaveIdentifierVersionAndSystemForXml1() {
        // given

        // when
        Plugin result = parser.parse(xmlFile1);

        // then
        assertEquals("testPlugin", result.getIdentifier());
        assertEquals(new Version("1.2.3"), result.getVersion());
        assertTrue(result.isSystemPlugin());
    }

    @Test
    public void shouldHaveIdentifierVersionAndSystemForXml2() {
        // given

        // when
        Plugin result = parser.parse(xmlFile2);

        // then
        assertEquals("testPlugin2", result.getIdentifier());
        assertEquals(new Version("2.3.1"), result.getVersion());
        assertFalse(result.isSystemPlugin());
    }

    @Test
    public void shouldHavePluginInformationsForXml1() {
        // given

        // when
        Plugin result = parser.parse(xmlFile1);

        // then
        assertEquals("testPluginName", result.getPluginInformation().getName());
        assertEquals("testPluginDescription", result.getPluginInformation().getDescription());
        assertEquals("testPluginVendorName", result.getPluginInformation().getVendor());
        assertEquals("testPluginVendorUrl", result.getPluginInformation().getVendorUrl());
    }

    @Test
    public void shouldHavePluginInformationsForXml2() {
        // given

        // when
        Plugin result = parser.parse(xmlFile2);

        // then
        assertEquals("testPlugin2Name", result.getPluginInformation().getName());
        assertNull(result.getPluginInformation().getDescription());
        assertNull(result.getPluginInformation().getVendor());
        assertNull(result.getPluginInformation().getVendorUrl());
    }

    @Test
    public void shouldHavePluginDependenciesInformationsForXml1() {
        // given

        // when
        Plugin result = parser.parse(xmlFile1);

        // then
        Set<PluginDependencyInformation> dependencies = result.getRequiredPlugins();
        assertEquals(3, dependencies.size());
        assertTrue(dependencies.contains(new PluginDependencyInformation("testPluginDependency1", new VersionOfDependency(
                "(1.2.3,2.3.4]"))));
        assertTrue(dependencies.contains(new PluginDependencyInformation("testPluginDependency2",
                new VersionOfDependency("1.1.1"))));
        assertTrue(dependencies.contains(new PluginDependencyInformation("testPluginDependency3", new VersionOfDependency(null))));
    }

    @Test
    public void shouldHavePluginDependenciesInformationsForXml2() {
        // given

        // when
        Plugin result = parser.parse(xmlFile2);

        // then
        assertEquals(0, result.getRequiredPlugins().size());
    }

    @Test
    public void shouldHaveModulesForXml1() throws Exception {
        // given

        // when
        Plugin result = parser.parse(xmlFile1);

        // then
        DefaultPlugin castedResult = (DefaultPlugin) result;
        assertEquals(2, castedResult.getMdules().size());
        assertTrue(castedResult.getMdules().contains(testModule1));
        assertTrue(castedResult.getMdules().contains(testModule2));
    }

    @Test
    public void shouldHaveModulesForXml2() throws Exception {
        // given

        // when
        Plugin result = parser.parse(xmlFile2);

        // then
        DefaultPlugin castedResult = (DefaultPlugin) result;
        assertEquals(0, castedResult.getMdules().size());
    }

    private class HasNodeName extends ArgumentMatcher<Node> {

        private final String expectedNodeName;

        private final String expectedNodeText;

        public HasNodeName(final String expectedNodeName, final String expectedNodeText) {
            this.expectedNodeName = expectedNodeName;
            this.expectedNodeText = expectedNodeText;
        }

        @Override
        public boolean matches(final Object node) {
            if (expectedNodeName.equals(((Node) node).getNodeName())) {
                return expectedNodeText.equals(((Node) node).getTextContent());
            }
            return false;
        }
    }

    @Test
    public void shouldParseAllPlugins() throws Exception {
        // given
        Resource[] testXmlsList = new Resource[] { xmlFile1, xmlFile2 };

        given(pluginDescriptorResolver.getDescriptors()).willReturn(testXmlsList);

        Plugin p1 = parser.parse(xmlFile1);
        Plugin p2 = parser.parse(xmlFile2);

        // when
        Set<Plugin> result = parser.loadPlugins();

        // then
        assertEquals(2, result.size());
        assertTrue(result.contains(p1));
        assertTrue(result.contains(p2));
    }

    @Test(expected = PluginException.class)
    public void shouldNotParsePluginsWhenException() throws Exception {
        // given
        Resource[] testXmlsList = new Resource[] { xmlFile1, xmlFile2, xmlFile3 };

        given(pluginDescriptorResolver.getDescriptors()).willReturn(testXmlsList);

        // when
        parser.loadPlugins();

        // then
    }
}
