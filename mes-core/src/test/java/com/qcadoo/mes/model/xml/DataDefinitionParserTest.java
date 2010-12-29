/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.model.xml;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.DictionaryService;
import com.qcadoo.mes.beans.sample.CustomEntityService;
import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;
import com.qcadoo.mes.internal.DataAccessService;
import com.qcadoo.mes.internal.DataDefinitionServiceImpl;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.HookDefinition;
import com.qcadoo.mes.model.hooks.internal.HookFactory;
import com.qcadoo.mes.model.internal.DataDefinitionParser;
import com.qcadoo.mes.model.internal.InternalDataDefinition;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.types.internal.BelongsToEntityType;
import com.qcadoo.mes.model.types.internal.BooleanType;
import com.qcadoo.mes.model.types.internal.DateTimeType;
import com.qcadoo.mes.model.types.internal.DateType;
import com.qcadoo.mes.model.types.internal.DecimalType;
import com.qcadoo.mes.model.types.internal.DictionaryType;
import com.qcadoo.mes.model.types.internal.EnumType;
import com.qcadoo.mes.model.types.internal.FieldTypeFactory;
import com.qcadoo.mes.model.types.internal.FieldTypeFactoryImpl;
import com.qcadoo.mes.model.types.internal.HasManyEntitiesType;
import com.qcadoo.mes.model.types.internal.IntegerType;
import com.qcadoo.mes.model.types.internal.PasswordType;
import com.qcadoo.mes.model.types.internal.PriorityType;
import com.qcadoo.mes.model.types.internal.StringType;
import com.qcadoo.mes.model.validators.EntityValidator;
import com.qcadoo.mes.model.validators.FieldValidator;
import com.qcadoo.mes.model.validators.internal.CustomEntityValidator;
import com.qcadoo.mes.model.validators.internal.CustomValidator;
import com.qcadoo.mes.model.validators.internal.LengthValidator;
import com.qcadoo.mes.model.validators.internal.PrecisionValidator;
import com.qcadoo.mes.model.validators.internal.RangeValidator;
import com.qcadoo.mes.model.validators.internal.RequiredOnCreateValidator;
import com.qcadoo.mes.model.validators.internal.RequiredValidator;
import com.qcadoo.mes.model.validators.internal.ScaleValidator;
import com.qcadoo.mes.model.validators.internal.UniqueValidator;
import com.qcadoo.mes.model.validators.internal.ValidatorFactory;
import com.qcadoo.mes.model.validators.internal.ValidatorFactoryImpl;

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
        dataAccessService = mock(DataAccessService.class);
        dictionaryService = mock(DictionaryService.class);

        dataDefinitionService = new DataDefinitionServiceImpl();

        fieldTypeFactory = new FieldTypeFactoryImpl();
        setField(fieldTypeFactory, "dictionaryService", dictionaryService);
        setField(fieldTypeFactory, "dataDefinitionService", dataDefinitionService);
        setField(fieldTypeFactory, "passwordEncoder", mock(PasswordEncoder.class));

        validatorFactory = new ValidatorFactoryImpl();

        hookFactory = new HookFactory();
        setField(hookFactory, "applicationContext", applicationContext);

        dataDefinitionParser = new DataDefinitionParser();
        setField(dataDefinitionParser, "dataDefinitionService", dataDefinitionService);
        setField(dataDefinitionParser, "dataAccessService", dataAccessService);
        setField(dataDefinitionParser, "fieldTypeFactory", fieldTypeFactory);
        setField(dataDefinitionParser, "hookFactory", hookFactory);
        setField(dataDefinitionParser, "validatorFactory", validatorFactory);

        xml = new FileInputStream(new File("src/test/resources/model/test.xml"));

        given(applicationContext.getBean(CustomEntityService.class)).willReturn(new CustomEntityService());
    }

    @Test
    public void shouldParseXml() {
        // given
        DataDefinition dataDefinition = parseAndGetDataDefinition();

        // then
        assertNotNull(dataDefinition);
    }

    @Test
    public void shouldSetDataDefinitionAttributes() {
        // given
        InternalDataDefinition dataDefinition = (InternalDataDefinition) parseAndGetDataDefinition();

        // then
        assertEquals("simpleDatabaseObject", dataDefinition.getName());
        assertEquals("com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject", dataDefinition.getFullyQualifiedClassName());
        assertThat(dataDefinition.getInstanceForEntity(), instanceOf(SampleSimpleDatabaseObject.class));
        assertEquals("sample", dataDefinition.getPluginIdentifier());
        assertEquals(SampleSimpleDatabaseObject.class, dataDefinition.getClassForEntity());
        assertTrue(dataDefinition.isDeletable());
        assertFalse(dataDefinition.isCreatable());
        assertTrue(dataDefinition.isUpdatable());
    }

    @Test
    public void shouldSetEntityValidators() {
        // given
        DataDefinition dataDefinition = parseAndGetDataDefinition();

        // then
        assertEquals(1, ((InternalDataDefinition) dataDefinition).getValidators().size());

        EntityValidator validator = ((InternalDataDefinition) dataDefinition).getValidators().get(0);

        assertThat(validator, instanceOf(CustomEntityValidator.class));

        testHookDefinition(validator, "entityValidateHook", CustomEntityService.class, "validateEntity");
    }

    @Test
    public void shouldSetFields() {
        // given
        DataDefinition dataDefinition = parseAndGetDataDefinition();

        // then
        assertNotNull(dataDefinition.getField("age"));
        assertThat(dataDefinition.getField("age").getType(), instanceOf(IntegerType.class));
        assertNotNull(dataDefinition.getField("firstname"));
        assertThat(dataDefinition.getField("firstname").getType(), instanceOf(StringType.class));
        assertNotNull(dataDefinition.getField("money"));
        assertThat(dataDefinition.getField("money").getType(), instanceOf(DecimalType.class));
        assertNotNull(dataDefinition.getField("datetime"));
        assertThat(dataDefinition.getField("datetime").getType(), instanceOf(DateTimeType.class));
        assertNotNull(dataDefinition.getField("date"));
        assertThat(dataDefinition.getField("date").getType(), instanceOf(DateType.class));
        assertNotNull(dataDefinition.getField("boolean"));
        assertThat(dataDefinition.getField("boolean").getType(), instanceOf(BooleanType.class));
        assertNotNull(dataDefinition.getField("parent"));
        assertThat(dataDefinition.getField("parent").getType(), instanceOf(BelongsToEntityType.class));
        assertNotNull(dataDefinition.getField("children"));
        assertThat(dataDefinition.getField("children").getType(), instanceOf(HasManyEntitiesType.class));
        assertEquals("child", ((HasManyType) (dataDefinition.getField("children")).getType()).getJoinFieldName());
        assertEquals("people", getField(dataDefinition.getField("children").getType(), "pluginIdentifier"));
        assertEquals("person", getField(dataDefinition.getField("children").getType(), "entityName"));
        assertEquals(HasManyType.Cascade.NULLIFY, getField(dataDefinition.getField("children").getType(), "cascade"));
        assertNotNull(dataDefinition.getField("children2"));
        assertEquals("child2", ((HasManyType) (dataDefinition.getField("children2")).getType()).getJoinFieldName());
        assertEquals("sample", getField(dataDefinition.getField("children2").getType(), "pluginIdentifier"));
        assertEquals("person", getField(dataDefinition.getField("children2").getType(), "entityName"));
        assertEquals(HasManyType.Cascade.DELETE, getField(dataDefinition.getField("children2").getType(), "cascade"));
        assertThat(dataDefinition.getField("children2").getType(), instanceOf(HasManyEntitiesType.class));
        assertNotNull(dataDefinition.getField("typeOfMaterial"));
        assertThat(dataDefinition.getField("typeOfMaterial").getType(), instanceOf(EnumType.class));
        assertThat(((EnumType) dataDefinition.getField("typeOfMaterial").getType()).values(),
                JUnitMatchers.hasItems("03product", "02intermediate", "01component"));
        assertNotNull(dataDefinition.getField("category"));
        assertThat(dataDefinition.getField("category").getType(), instanceOf(DictionaryType.class));
        assertEquals("categories", getField(dataDefinition.getField("category").getType(), "dictionaryName"));
        assertThat(dataDefinition.getField("password").getType(), instanceOf(PasswordType.class));

        assertFalse(dataDefinition.getField("age").isReadOnly());
        assertFalse(dataDefinition.getField("age").isReadOnlyOnUpdate());

        assertTrue(dataDefinition.getField("lastname").isReadOnly());
        assertTrue(dataDefinition.getField("lastname").isReadOnlyOnUpdate());

    }

    @Test
    public void shouldSetFieldValidators() {
        // given
        DataDefinition dataDefinition = parseAndGetDataDefinition();

        // then
        assertFalse(dataDefinition.getField("lastname").isRequired());
        assertFalse(dataDefinition.getField("lastname").isRequiredOnCreate());
        assertFalse(dataDefinition.getField("lastname").isUnique());
        assertEquals(0, dataDefinition.getField("lastname").getValidators().size());

        assertTrue(dataDefinition.getField("age").isRequired());
        assertTrue(dataDefinition.getField("age").isRequiredOnCreate());
        assertTrue(dataDefinition.getField("age").isUnique());
        assertEquals(12, dataDefinition.getField("age").getValidators().size());

        List<FieldValidator> validators = dataDefinition.getField("age").getValidators();

        assertThat(validators.get(0), instanceOf(RequiredValidator.class));
        assertEquals("Error Message", getField(validators.get(0), "errorMessage"));
        assertThat(validators.get(1), instanceOf(RequiredOnCreateValidator.class));
        assertEquals("core.validate.field.error.missing", getField(validators.get(1), "errorMessage"));
        assertThat(validators.get(2), instanceOf(UniqueValidator.class));
        assertThat(validators.get(3), instanceOf(CustomValidator.class));
        testHookDefinition(validators.get(3), "validateHook", CustomEntityService.class, "validateField");
        assertThat(validators.get(4), instanceOf(LengthValidator.class));
        assertEquals(1, getField(validators.get(4), "min"));
        assertEquals(2, getField(validators.get(4), "is"));
        assertEquals(3, getField(validators.get(4), "max"));
        assertThat(validators.get(5), instanceOf(LengthValidator.class));
        assertEquals(null, getField(validators.get(5), "min"));
        assertEquals(2, getField(validators.get(5), "is"));
        assertEquals(null, getField(validators.get(5), "max"));
        assertThat(validators.get(6), instanceOf(LengthValidator.class));
        assertEquals(null, getField(validators.get(6), "min"));
        assertEquals(null, getField(validators.get(6), "is"));
        assertEquals(20, getField(validators.get(6), "max"));
        assertThat(validators.get(7), instanceOf(PrecisionValidator.class));
        assertEquals(2, getField(validators.get(7), "min"));
        assertEquals(2, getField(validators.get(7), "is"));
        assertEquals(4, getField(validators.get(7), "max"));
        assertThat(validators.get(8), instanceOf(PrecisionValidator.class));
        assertEquals(null, getField(validators.get(8), "min"));
        assertEquals(2, getField(validators.get(8), "is"));
        assertEquals(null, getField(validators.get(8), "max"));
        assertThat(validators.get(9), instanceOf(ScaleValidator.class));
        assertEquals(2, getField(validators.get(9), "min"));
        assertEquals(2, getField(validators.get(9), "is"));
        assertEquals(4, getField(validators.get(9), "max"));
        assertThat(validators.get(10), instanceOf(ScaleValidator.class));
        assertEquals(null, getField(validators.get(10), "min"));
        assertEquals(2, getField(validators.get(10), "is"));
        assertEquals(null, getField(validators.get(10), "max"));
        assertThat(validators.get(11), instanceOf(RangeValidator.class));
        assertEquals(18, getField(validators.get(11), "from"));
        assertEquals(null, getField(validators.get(11), "to"));

        assertThat(dataDefinition.getField("firstname").getValidators().get(0), instanceOf(RangeValidator.class));
        assertEquals("daap", getField(dataDefinition.getField("firstname").getValidators().get(0), "from"));
        assertEquals("dzzp", getField(dataDefinition.getField("firstname").getValidators().get(0), "to"));
    }

    @Test
    public void shouldSetPriorityField() {
        // given
        DataDefinition dataDefinition = parseAndGetDataDefinition();

        // then
        assertNotNull(dataDefinition.getPriorityField());
        assertEquals("priority", dataDefinition.getPriorityField().getName());
        assertEquals("parent", ((PriorityType) dataDefinition.getPriorityField().getType()).getScopeFieldDefinition().getName());
        assertTrue(((InternalDataDefinition) dataDefinition).isPrioritizable());
    }

    @Test
    public void shouldSetHooks() {
        // given
        DataDefinition dataDefinition = parseAndGetDataDefinition();

        // then
        testHookDefinition(dataDefinition, "createHook", CustomEntityService.class, "onCreate");
        testHookDefinition(dataDefinition, "updateHook", CustomEntityService.class, "onUpdate");
        testHookDefinition(dataDefinition, "saveHook", CustomEntityService.class, "onSave");
    }

    private void testHookDefinition(final Object object, final String hookFieldName, final Class<?> hookBeanClass,
            final String hookMethodName) {
        HookDefinition hook = (HookDefinition) getField(object, hookFieldName);

        assertNotNull(hook);
        assertThat(getField(hook, "bean"), instanceOf(hookBeanClass));
        assertEquals(hookMethodName, getField(hook, "methodName"));
    }

    private DataDefinition parseAndGetDataDefinition() {
        dataDefinitionParser.parse(xml);
        return dataDefinitionService.get("sample", "simpleDatabaseObject");
    }

}
