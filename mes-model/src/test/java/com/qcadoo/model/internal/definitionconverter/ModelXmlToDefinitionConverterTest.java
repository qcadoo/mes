/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.model.internal.definitionconverter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import com.qcadoo.model.CustomHook;
import com.qcadoo.model.Utils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.DictionaryService;
import com.qcadoo.model.api.types.BelongsToType;
import com.qcadoo.model.api.types.HasManyType;
import com.qcadoo.model.api.types.TreeType;
import com.qcadoo.model.beans.dictionaries.DictionariesDictionary;
import com.qcadoo.model.internal.DataDefinitionServiceImpl;
import com.qcadoo.model.internal.api.DataAccessService;
import com.qcadoo.model.internal.api.EntityHookDefinition;
import com.qcadoo.model.internal.api.FieldHookDefinition;
import com.qcadoo.model.internal.api.InternalDataDefinition;
import com.qcadoo.model.internal.api.InternalFieldDefinition;
import com.qcadoo.model.internal.api.ModelXmlToClassConverter;
import com.qcadoo.model.internal.classconverter.ModelXmlToClassConverterImpl;
import com.qcadoo.model.internal.types.BelongsToEntityType;
import com.qcadoo.model.internal.types.BooleanType;
import com.qcadoo.model.internal.types.DateTimeType;
import com.qcadoo.model.internal.types.DateType;
import com.qcadoo.model.internal.types.DecimalType;
import com.qcadoo.model.internal.types.EnumType;
import com.qcadoo.model.internal.types.HasManyEntitiesType;
import com.qcadoo.model.internal.types.IntegerType;
import com.qcadoo.model.internal.types.PasswordType;
import com.qcadoo.model.internal.types.PriorityType;
import com.qcadoo.model.internal.types.StringType;
import com.qcadoo.model.internal.types.TextType;
import com.qcadoo.model.internal.types.TreeEntitiesType;
import com.qcadoo.model.internal.validators.CustomEntityValidator;
import com.qcadoo.model.internal.validators.CustomValidator;
import com.qcadoo.model.internal.validators.LengthValidator;
import com.qcadoo.model.internal.validators.PrecisionValidator;
import com.qcadoo.model.internal.validators.RangeValidator;
import com.qcadoo.model.internal.validators.RegexValidator;
import com.qcadoo.model.internal.validators.RequiredValidator;
import com.qcadoo.model.internal.validators.ScaleValidator;
import com.qcadoo.model.internal.validators.UniqueValidator;

public class ModelXmlToDefinitionConverterTest {

    private static ModelXmlToDefinitionConverterImpl modelXmlToDefinitionConverter;

    private static DataDefinitionService dataDefinitionService;

    private static DataAccessService dataAccessService;

    private static ApplicationContext applicationContext;

    private static DictionaryService dictionaryService;

    private static InternalDataDefinition dataDefinition;

    private static Collection<DataDefinition> dataDefinitions;

    @BeforeClass
    public static void init() throws Exception {
        applicationContext = mock(ApplicationContext.class);
        dataAccessService = mock(DataAccessService.class);
        dictionaryService = mock(DictionaryService.class);

        DictionariesDictionary dictionary = new DictionariesDictionary();
        SessionFactory sessionFactory = mock(SessionFactory.class, RETURNS_DEEP_STUBS);
        given(
                sessionFactory.getCurrentSession().createCriteria(DictionariesDictionary.class).add(Mockito.any(Criterion.class))
                        .setMaxResults(1).uniqueResult()).willReturn(dictionary);

        dataDefinitionService = new DataDefinitionServiceImpl();

        modelXmlToDefinitionConverter = new ModelXmlToDefinitionConverterImpl();
        setField(modelXmlToDefinitionConverter, "dataDefinitionService", dataDefinitionService);
        setField(modelXmlToDefinitionConverter, "dataAccessService", dataAccessService);
        setField(modelXmlToDefinitionConverter, "applicationContext", applicationContext);
        setField(modelXmlToDefinitionConverter, "sessionFactory", sessionFactory);

        given(applicationContext.getBean(CustomHook.class)).willReturn(new CustomHook());

        ModelXmlToClassConverter modelXmlToClassConverter = new ModelXmlToClassConverterImpl();
        ((ModelXmlToClassConverterImpl) modelXmlToClassConverter).setBeanClassLoader(ClassLoader.getSystemClassLoader());
        modelXmlToClassConverter.convert(Utils.FULL_FIRST_ENTITY_XML_RESOURCE, Utils.FULL_SECOND_ENTITY_XML_RESOURCE,
                Utils.FULL_THIRD_ENTITY_XML_RESOURCE, Utils.OTHER_FIRST_ENTITY_XML_RESOURCE,
                Utils.OTHER_SECOND_ENTITY_XML_RESOURCE);

        dataDefinitions = modelXmlToDefinitionConverter.convert(Utils.FULL_FIRST_ENTITY_XML_RESOURCE,
                Utils.FULL_SECOND_ENTITY_XML_RESOURCE, Utils.FULL_THIRD_ENTITY_XML_RESOURCE,
                Utils.OTHER_FIRST_ENTITY_XML_RESOURCE, Utils.OTHER_SECOND_ENTITY_XML_RESOURCE);

        for (DataDefinition dd : dataDefinitions.toArray(new DataDefinition[dataDefinitions.size()])) {
            if (dd.getName().equals("firstEntity") && dd.getPluginIdentifier().equals("full")) {
                dataDefinition = (InternalDataDefinition) dd;
            }
        }

    }

    @Test
    public void shouldParseXml() {
        assertNotNull(dataDefinition);
    }

    @Test
    public void shouldSetDataDefinitionAttributes() {
        assertEquals(5, dataDefinitions.size());
        assertEquals("firstEntity", dataDefinition.getName());
        assertEquals("com.qcadoo.model.beans.full.FullFirstEntity", dataDefinition.getFullyQualifiedClassName());
        assertEquals("com.qcadoo.model.beans.full.FullFirstEntity", dataDefinition.getInstanceForEntity().getClass()
                .getCanonicalName());
        assertEquals("full", dataDefinition.getPluginIdentifier());
        assertEquals("com.qcadoo.model.beans.full.FullFirstEntity", dataDefinition.getClassForEntity().getCanonicalName());
        assertFalse(dataDefinition.isDeletable());
        assertFalse(dataDefinition.isInstertable());
        assertTrue(dataDefinition.isUpdatable());
    }

    @Test
    public void shouldSetEntityValidators() {
        assertEquals(1, (dataDefinition).getValidators().size());

        EntityHookDefinition validator = (dataDefinition).getValidators().get(0);

        assertThat(validator, instanceOf(CustomEntityValidator.class));

        testHookDefinition(validator, "entityHook", CustomHook.class, "validate");
    }

    @Test
    public void shouldSetFields() {
        assertNotNull(dataDefinition.getField("fieldInteger"));
        assertThat(dataDefinition.getField("fieldInteger").getType(), instanceOf(IntegerType.class));
        assertNotNull(dataDefinition.getField("fieldString"));
        assertThat(dataDefinition.getField("fieldString").getType(), instanceOf(StringType.class));
        assertNotNull(dataDefinition.getField("fieldText"));
        assertThat(dataDefinition.getField("fieldText").getType(), instanceOf(TextType.class));
        assertNotNull(dataDefinition.getField("fieldDecimal"));
        assertThat(dataDefinition.getField("fieldDecimal").getType(), instanceOf(DecimalType.class));
        assertNotNull(dataDefinition.getField("fieldDatetime"));
        assertThat(dataDefinition.getField("fieldDatetime").getType(), instanceOf(DateTimeType.class));
        assertNotNull(dataDefinition.getField("fieldDate"));
        assertThat(dataDefinition.getField("fieldDate").getType(), instanceOf(DateType.class));
        assertNotNull(dataDefinition.getField("fieldBoolean"));
        assertThat(dataDefinition.getField("fieldBoolean").getType(), instanceOf(BooleanType.class));

        assertNotNull(dataDefinition.getField("fieldSecondEntity"));
        assertThat(dataDefinition.getField("fieldSecondEntity").getType(), instanceOf(BelongsToEntityType.class));
        assertEquals("other", ((BelongsToType) (dataDefinition.getField("fieldSecondEntity")).getType()).getDataDefinition()
                .getPluginIdentifier());
        assertEquals("secondEntity", ((BelongsToType) (dataDefinition.getField("fieldSecondEntity")).getType())
                .getDataDefinition().getName());
        assertFalse(((BelongsToType) (dataDefinition.getField("fieldSecondEntity")).getType()).isLazyLoading());

        assertNotNull(dataDefinition.getField("fieldSecondEntity2"));
        assertThat(dataDefinition.getField("fieldSecondEntity2").getType(), instanceOf(BelongsToEntityType.class));
        assertEquals("other", ((BelongsToType) (dataDefinition.getField("fieldSecondEntity2")).getType()).getDataDefinition()
                .getPluginIdentifier());
        assertEquals("secondEntity", ((BelongsToType) (dataDefinition.getField("fieldSecondEntity2")).getType())
                .getDataDefinition().getName());
        assertTrue(((BelongsToType) (dataDefinition.getField("fieldSecondEntity2")).getType()).isLazyLoading());

        assertNotNull(dataDefinition.getField("fieldHasMany"));
        assertThat(dataDefinition.getField("fieldHasMany").getType(), instanceOf(HasManyEntitiesType.class));
        assertEquals("fieldFirstEntity", ((HasManyType) (dataDefinition.getField("fieldHasMany")).getType()).getJoinFieldName());
        assertEquals("full", getField(dataDefinition.getField("fieldHasMany").getType(), "pluginIdentifier"));
        assertEquals("thirdEntity", getField(dataDefinition.getField("fieldHasMany").getType(), "entityName"));
        assertEquals(HasManyType.Cascade.NULLIFY, getField(dataDefinition.getField("fieldHasMany").getType(), "cascade"));

        assertNotNull(dataDefinition.getField("fieldTree"));
        assertThat(dataDefinition.getField("fieldTree").getType(), instanceOf(TreeEntitiesType.class));
        assertEquals("fieldFirstEntity", ((TreeType) (dataDefinition.getField("fieldTree")).getType()).getJoinFieldName());
        assertEquals("full", getField(dataDefinition.getField("fieldTree").getType(), "pluginIdentifier"));
        assertEquals("secondEntity", getField(dataDefinition.getField("fieldTree").getType(), "entityName"));
        assertEquals(TreeType.Cascade.DELETE, getField(dataDefinition.getField("fieldTree").getType(), "cascade"));

        assertNotNull(dataDefinition.getField("fieldEnum"));
        assertThat(dataDefinition.getField("fieldEnum").getType(), instanceOf(EnumType.class));

        // TODO
        // assertThat(((EnumType) dataDefinition.getField("fieldEnum").getType()).values(Locale.ENGLISH).keySet(),
        // hasItems("one", "two", "three"));

        // TODO
        // assertNotNull(dataDefinition.getField("category"));
        // assertThat(dataDefinition.getField("fieldDictionary").getType(), instanceOf(DictionaryType.class));
        // assertEquals("categories", getField(dataDefinition.getField("fieldDictionary").getType(), "dictionaryName"));

        assertThat(dataDefinition.getField("fieldPassword").getType(), instanceOf(PasswordType.class));
        assertFalse(dataDefinition.getField("fieldInteger").isReadOnly());
        assertTrue(dataDefinition.getField("fieldText").isReadOnly());
    }

    // TODO
    // <string name="fieldStringNotPersistent" persistent="false" />
    // <string name="fieldStringWithExpression" expression="#fString" />
    // <toString expression="#fieldString" />

    @Test
    public void shouldSetFieldValidators() {
        assertFalse(dataDefinition.getField("fieldText").isRequired());
        assertFalse(dataDefinition.getField("fieldText").isUnique());
        assertTrue(dataDefinition.getField("fieldInteger").isRequired());
        assertTrue(dataDefinition.getField("fieldInteger").isUnique());

        assertEquals(0, ((InternalFieldDefinition) dataDefinition.getField("fieldDatetime")).getValidators().size());

        assertEquals(6, ((InternalFieldDefinition) dataDefinition.getField("fieldInteger")).getValidators().size());

        List<FieldHookDefinition> validators = ((InternalFieldDefinition) dataDefinition.getField("fieldInteger"))
                .getValidators();

        assertThat(validators.get(0), instanceOf(RequiredValidator.class));
        assertThat(validators.get(1), instanceOf(UniqueValidator.class));
        assertThat(validators.get(2), instanceOf(CustomValidator.class));
        testHookDefinition(validators.get(2), "fieldHook", CustomHook.class, "validateField");
        assertThat(validators.get(3), instanceOf(LengthValidator.class));
        assertEquals(1, getField(validators.get(3), "min"));
        assertNull(getField(validators.get(3), "is"));
        assertEquals(3, getField(validators.get(3), "max"));
        assertThat(validators.get(4), instanceOf(PrecisionValidator.class));
        assertEquals(2, getField(validators.get(4), "min"));
        assertNull(getField(validators.get(4), "is"));
        assertEquals(4, getField(validators.get(4), "max"));
        assertThat(validators.get(5), instanceOf(RangeValidator.class));
        assertEquals(18, getField(validators.get(5), "from"));
        assertEquals(null, getField(validators.get(5), "to"));
        assertEquals(false, getField(validators.get(5), "inclusively"));

        validators = ((InternalFieldDefinition) dataDefinition.getField("fieldString")).getValidators();

        assertThat(validators.get(3), instanceOf(RegexValidator.class));
        assertEquals("d??p", getField(validators.get(3), "regex"));

        validators = ((InternalFieldDefinition) dataDefinition.getField("fieldDecimal")).getValidators();

        assertThat(validators.get(0), instanceOf(ScaleValidator.class));
        assertEquals(2, getField(validators.get(0), "min"));
        assertNull(getField(validators.get(0), "is"));
        assertEquals(4, getField(validators.get(0), "max"));
        assertThat(validators.get(1), instanceOf(PrecisionValidator.class));
        assertNull(getField(validators.get(1), "min"));
        assertEquals(2, getField(validators.get(1), "is"));
        assertNull(getField(validators.get(1), "max"));
    }

    @Test
    public void shouldSetPriorityField() {
        assertNotNull(dataDefinition.getPriorityField());
        assertEquals("fieldPriority", dataDefinition.getPriorityField().getName());
        assertEquals("fieldInteger", ((PriorityType) dataDefinition.getPriorityField().getType()).getScopeFieldDefinition()
                .getName());
        assertTrue((dataDefinition).isPrioritizable());
    }

    @Test
    public void shouldSetHooks() {
        testListHookDefinition(dataDefinition, "createHooks", CustomHook.class, "createHook");
        testListHookDefinition(dataDefinition, "updateHooks", CustomHook.class, "updateHook");
        testListHookDefinition(dataDefinition, "saveHooks", CustomHook.class, "hook");
        testListHookDefinition(dataDefinition, "copyHooks", CustomHook.class, "copyHook");
    }

    private void testListHookDefinition(final Object object, final String hookFieldName, final Class<?> hookBeanClass,
            final String hookMethodName) {
        List<EntityHookDefinition> hook = (List<EntityHookDefinition>) getField(object, hookFieldName);

        assertEquals(1, hook.size());
        assertThat(getField(hook.get(0), "bean"), instanceOf(hookBeanClass));
        assertEquals(hookMethodName, ((Method) getField(hook.get(0), "method")).getName());
    }

    private void testHookDefinition(final Object object, final String hookFieldName, final Class<?> hookBeanClass,
            final String hookMethodName) {
        Object hook = getField(object, hookFieldName);

        assertNotNull(hook);
        assertThat(getField(hook, "bean"), instanceOf(hookBeanClass));
        assertEquals(hookMethodName, ((Method) getField(hook, "method")).getName());
    }

}
