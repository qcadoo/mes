package com.qcadoo.mes.core.data.xml;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.beans.test.CustomEntityService;
import com.qcadoo.mes.beans.test.SimpleDatabaseObject;
import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.api.DictionaryService;
import com.qcadoo.mes.core.data.internal.hooks.HookFactory;
import com.qcadoo.mes.core.data.internal.types.FieldTypeFactoryImpl;
import com.qcadoo.mes.core.data.internal.xml.DataDefinitionParser;
import com.qcadoo.mes.core.data.model.DataDefinition;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.ValidatorFactory;

public class DataDefinitionParserTest {

    private DataDefinitionParser dataDefinitionParser;

    private DataDefinitionService dataDefinitionService;

    private DataAccessService dataAccessService;

    private FieldTypeFactory fieldTypeFactory;

    private HookFactory hookFactory;

    private ValidatorFactory validatorFactory;

    private ApplicationContext applicationContext;

    private DictionaryService dictionaryService;

    private InputStream xml;

    @Before
    public void init() throws Exception {
        applicationContext = mock(ApplicationContext.class);
        validatorFactory = mock(ValidatorFactory.class);
        dataAccessService = mock(DataAccessService.class);
        dictionaryService = mock(DictionaryService.class);

        dataDefinitionService = new DataDefinitionService() {

            private final Map<String, DataDefinition> dataDefinitions = new HashMap<String, DataDefinition>();

            @Override
            public void save(final DataDefinition dataDefinition) {
                dataDefinitions.put(dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName(), dataDefinition);
            }

            @Override
            public DataDefinition get(final String pluginIdentifier, final String modelName) {
                return dataDefinitions.get(pluginIdentifier + "." + modelName);
            }

            @Override
            public void delete(final String pluginIdentifier, final String entityName) {
            }

            @Override
            public List<DataDefinition> list() {
                return new ArrayList<DataDefinition>(dataDefinitions.values());
            }

        };

        fieldTypeFactory = new FieldTypeFactoryImpl();
        ReflectionTestUtils.setField(fieldTypeFactory, "dictionaryService", dictionaryService);
        ReflectionTestUtils.setField(fieldTypeFactory, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(fieldTypeFactory, "passwordEncoder", mock(PasswordEncoder.class));

        hookFactory = new HookFactory();
        ReflectionTestUtils.setField(hookFactory, "applicationContext", applicationContext);

        dataDefinitionParser = new DataDefinitionParser();
        ReflectionTestUtils.setField(dataDefinitionParser, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(dataDefinitionParser, "dataAccessService", dataAccessService);
        ReflectionTestUtils.setField(dataDefinitionParser, "fieldTypeFactory", fieldTypeFactory);
        ReflectionTestUtils.setField(dataDefinitionParser, "hookFactory", hookFactory);
        ReflectionTestUtils.setField(dataDefinitionParser, "validatorFactory", validatorFactory);

        xml = new FileInputStream(new File("src/test/resources/dataDefinition.xml"));

        BDDMockito.given(applicationContext.getBean(CustomEntityService.class)).willReturn(new CustomEntityService());
    }

    @Test
    public void shouldParseXml() {
        // when
        dataDefinitionParser.parse(xml);

        // then
        DataDefinition dataDefinition = dataDefinitionService.get("test", "simpleDatabaseObject");

        Assert.assertNotNull(dataDefinition);
        Assert.assertEquals("simpleDatabaseObject", dataDefinition.getName());
        Assert.assertEquals("com.qcadoo.mes.beans.test.SimpleDatabaseObject", dataDefinition.getFullyQualifiedClassName());
        Assert.assertEquals(SimpleDatabaseObject.class, dataDefinition.getInstanceForEntity().getClass());
        Assert.assertEquals("test", dataDefinition.getPluginIdentifier());
        Assert.assertEquals(SimpleDatabaseObject.class, dataDefinition.getClassForEntity());
    }
}
