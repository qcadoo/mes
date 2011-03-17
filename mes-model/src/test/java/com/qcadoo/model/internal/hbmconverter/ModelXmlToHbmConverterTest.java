package com.qcadoo.model.internal.hbmconverter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.custommonkey.xmlunit.XMLUnit.buildControlDocument;

import java.io.InputStream;

import org.custommonkey.xmlunit.Validator;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.qcadoo.model.Utils;
import com.qcadoo.model.internal.api.ModelXmlToHbmConverter;

public class ModelXmlToHbmConverterTest {

    private final static ModelXmlToHbmConverter modelXmlToHbmConverter = new ModelXmlToHbmConverterImpl();

    private final static XpathEngine xpathEngine = XMLUnit.newXpathEngine();

    private static InputStream hbmInputStream;

    private static InputSource hbmInputSource;

    private static Document hbmFirstEntity;

    private static Document hbmSecondEntity;

    private static Document hbmThirdEntity;

    @BeforeClass
    public static void init() throws Exception {
        hbmInputStream = modelXmlToHbmConverter.convert(Utils.FULL_FIRST_ENTITY_XML_RESOURCE)[0].getInputStream();
        hbmInputSource = new InputSource(modelXmlToHbmConverter.convert(Utils.FULL_FIRST_ENTITY_XML_RESOURCE)[0].getInputStream());
        hbmFirstEntity = buildControlDocument(new InputSource(
                modelXmlToHbmConverter.convert(Utils.FULL_FIRST_ENTITY_XML_RESOURCE)[0].getInputStream()));
        hbmSecondEntity = buildControlDocument(new InputSource(
                modelXmlToHbmConverter.convert(Utils.FULL_SECOND_ENTITY_XML_RESOURCE)[0].getInputStream()));
        hbmThirdEntity = buildControlDocument(new InputSource(
                modelXmlToHbmConverter.convert(Utils.FULL_THIRD_ENTITY_XML_RESOURCE)[0].getInputStream()));
    }

    @Ignore
    // @Test(expected = IllegalStateException.class)
    public void shouldFailForInvalidModelXml() throws Exception {
        // TODO
        modelXmlToHbmConverter.convert(Utils.MODEL_XML_INVALID_RESOURCE);
    }

    @Test
    public void shouldConvertQcdOrmToHbmWithoutErrors() throws Exception {
        assertNotNull(hbmInputStream);
    }

    @Test
    public void shouldConvertQcdOrmToValidHbm() throws Exception {
        Validator result = new Validator(hbmInputSource, Utils.HBM_DTD_PATH);
        assertTrue(result.toString(), result.isValid());
    }

    @Test
    public void shouldCreateClassSectionForAllNonVirtualModel() throws Exception {
        assertNodeCount(1, "/hibernate-mapping/class", hbmFirstEntity);
        assertNodeCount(1, "/hibernate-mapping/class", hbmSecondEntity);
        assertNodeCount(1, "/hibernate-mapping/class", hbmThirdEntity);
    }

    @Test
    public void shouldCreateNameMergingPluginAndEntityNames() throws Exception {
        assertNodeEquals("com.qcadoo.model.beans.full.FullFirstEntity", "/hibernate-mapping/class[1]/@name", hbmFirstEntity);
        assertNodeEquals("com.qcadoo.model.beans.full.FullSecondEntity", "/hibernate-mapping/class[1]/@name", hbmSecondEntity);
        assertNodeEquals("com.qcadoo.model.beans.full.FullThirdEntity", "/hibernate-mapping/class[1]/@name", hbmThirdEntity);
    }

    @Test
    public void shouldCreateTableMergingPluginAndEntityNames() throws Exception {
        assertNodeEquals("full_firstEntity", "/hibernate-mapping/class[1]/@table", hbmFirstEntity);
        assertNodeEquals("full_secondEntity", "/hibernate-mapping/class[1]/@table", hbmSecondEntity);
        assertNodeEquals("full_thirdEntity", "/hibernate-mapping/class[1]/@table", hbmThirdEntity);
    }

    @Test
    public void shouldDefineSqlDeleteForNotDeletableEntities() throws Exception {
        assertNodeEquals("delete must not be executed on full_firstEntity", "/hibernate-mapping/class[1]/sql-delete/text()",
                hbmFirstEntity);
    }

    @Test
    public void shouldNotDefineSqlDeleteForDeletableEntities() throws Exception {
        assertNodeNotExists("/hibernate-mapping/class[1]/sql-delete", hbmSecondEntity);
    }

    @Test
    public void shouldDefineSqlInsertForNotInsertableEntities() throws Exception {
        assertNodeEquals("insert must not be executed on full_firstEntity", "/hibernate-mapping/class[1]/sql-insert/text()",
                hbmFirstEntity);
    }

    @Test
    public void shouldNotDefineSqlInsertForInsertableEntities() throws Exception {
        assertNodeNotExists("/hibernate-mapping/class[1]/sql-insert", hbmSecondEntity);
    }

    @Test
    public void shouldDefineSqlUpdateForNotUpdatableEntities() throws Exception {
        assertNodeEquals("update must not be executed on full_secondEntity", "/hibernate-mapping/class[1]/sql-update/text()",
                hbmSecondEntity);
    }

    @Test
    public void shouldNotDefineSqlUpdateForUpdatableEntities() throws Exception {
        assertNodeNotExists("/hibernate-mapping/class[1]/sql-update", hbmFirstEntity);
    }

    @Test
    public void shouldDefineIdColumn() throws Exception {
        assertNodeExists("/hibernate-mapping/class[1]/id", hbmFirstEntity);
        assertNodeEquals("id", "/hibernate-mapping/class[1]/id/@column", hbmFirstEntity);
        assertNodeEquals("id", "/hibernate-mapping/class[1]/id/@name", hbmFirstEntity);
        assertNodeEquals("long", "/hibernate-mapping/class[1]/id/@type", hbmFirstEntity);
        assertNodeExists("/hibernate-mapping/class[1]/id/generator", hbmFirstEntity);
        assertNodeEquals("increment", "/hibernate-mapping/class[1]/id/generator/@class", hbmFirstEntity);
    }

    @Test
    public void shouldDefineProperties() throws Exception {
        assertNodeCount(12, "/hibernate-mapping/class[1]/property", hbmFirstEntity);
        assertNodeCount(0, "/hibernate-mapping/class[1]/property", hbmSecondEntity);
        assertNodeCount(0, "/hibernate-mapping/class[1]/property", hbmThirdEntity);
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldInteger' and @type='integer']", hbmFirstEntity);
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldString' and @type='string']", hbmFirstEntity);
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldText' and @type='text']", hbmFirstEntity);
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldDecimal' and @type='big_decimal']", hbmFirstEntity);
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldDatetime' and @type='timestamp']", hbmFirstEntity);
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldDate' and @type='date']", hbmFirstEntity);
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldBoolean' and @type='boolean']", hbmFirstEntity);
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldDictionary' and @type='string']", hbmFirstEntity);
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldOtherDictionary' and @type='string']", hbmFirstEntity);
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldEnum' and @type='string']", hbmFirstEntity);
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldPassword' and @type='string']", hbmFirstEntity);
    }

    @Test
    public void shouldIgnoreNotPersistentProperties() throws Exception {
        assertNodeNotExists("/hibernate-mapping/class[1]/property[@name='fieldStringNotPersistent']", hbmFirstEntity);
    }

    @Test
    public void shouldIgnorePropertiesWithExpression() throws Exception {
        assertNodeNotExists("/hibernate-mapping/class[1]/property[@name='fieldStringWithExpression']", hbmFirstEntity);
    }

    @Test
    public void shouldDefinePriorityProperty() throws Exception {
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldPriority' and @type='integer' and @not-null='true']",
                hbmFirstEntity);
    }

    @Test
    public void shouldDefineUniqueProperty() throws Exception {
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldInteger' and @unique='true']", hbmFirstEntity);
        assertNodeCount(1, "/hibernate-mapping/class/property[@unique='true']", hbmFirstEntity);
    }

    @Test
    public void shouldDefineNotNullProperty() throws Exception {
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldInteger' and @not-null='true']", hbmFirstEntity);
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldString' and @not-null='true']", hbmFirstEntity);
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldPriority' and @not-null='true']", hbmFirstEntity);
        assertNodeCount(3, "/hibernate-mapping/class/property[@not-null='true']", hbmFirstEntity);
    }

    @Test
    public void shouldDefineLengthProperty() throws Exception {
        assertNodeEquals("3", "/hibernate-mapping/class[1]/property[@name='fieldInteger']/@length", hbmFirstEntity);
        assertNodeEquals("2", "/hibernate-mapping/class[1]/property[@name='fieldString']/@length", hbmFirstEntity);
        assertNodeCount(2, "/hibernate-mapping/class/property/@length", hbmFirstEntity);
    }

    @Test
    public void shouldDefineScaleProperty() throws Exception {
        assertNodeEquals("4", "/hibernate-mapping/class[1]/property[@name='fieldDecimal']/@scale", hbmFirstEntity);
        assertNodeCount(1, "/hibernate-mapping/class/property/@scale", hbmFirstEntity);
    }

    @Test
    public void shouldDefinePrecisionProperty() throws Exception {
        assertNodeEquals("4", "/hibernate-mapping/class[1]/property[@name='fieldInteger']/@precision", hbmFirstEntity);
        assertNodeEquals("2", "/hibernate-mapping/class[1]/property[@name='fieldDecimal']/@precision", hbmFirstEntity);
        assertNodeCount(2, "/hibernate-mapping/class/property/@precision", hbmFirstEntity);
    }

    @Test
    public void shouldDefineBelongsToRelation() throws Exception {
        assertNodeCount(2, "/hibernate-mapping/class[1]/many-to-one", hbmFirstEntity);
        assertNodeCount(2, "/hibernate-mapping/class[1]/many-to-one", hbmSecondEntity);
        assertNodeCount(1, "/hibernate-mapping/class[1]/many-to-one", hbmThirdEntity);
        assertNodeExists("/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity']", hbmFirstEntity);
        assertNodeEquals("fieldSecondEntity_id",
                "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity']/column/@name", hbmFirstEntity);
        assertNodeExists("/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity2']", hbmFirstEntity);
        assertNodeEquals("fieldSecondEntity2_id",
                "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity2']/column/@name", hbmFirstEntity);
        assertNodeEquals("com.qcadoo.model.beans.other.OtherSecondEntity",
                "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity']/@class", hbmFirstEntity);
        assertNodeEquals("com.qcadoo.model.beans.other.OtherSecondEntity",
                "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity2']/@class", hbmFirstEntity);
        assertNodeEquals("none", "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity']/@cascade", hbmFirstEntity);
        assertNodeEquals("none", "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity2']/@cascade", hbmFirstEntity);
        assertNodeEquals("false", "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity']/@lazy", hbmFirstEntity);
        assertNodeEquals("proxy", "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity2']/@lazy", hbmFirstEntity);
        assertNodeEquals("false", "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity']/@not-null", hbmFirstEntity);
        assertNodeEquals("true", "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity2']/@not-null", hbmFirstEntity);
    }

    @Test
    public void shouldDefineHasManyRelation() throws Exception {
        assertNodeCount(2, "/hibernate-mapping/class[1]/set", hbmFirstEntity);
        assertNodeCount(1, "/hibernate-mapping/class[1]/set", hbmSecondEntity);
        assertNodeCount(0, "/hibernate-mapping/class[1]/set", hbmThirdEntity);
        assertNodeExists("/hibernate-mapping/class[1]/set[@name='fieldTree']", hbmFirstEntity);
        assertNodeExists("/hibernate-mapping/class[1]/set[@name='fieldHasMany']", hbmFirstEntity);
        assertNodeEquals("com.qcadoo.model.beans.full.FullSecondEntity",
                "/hibernate-mapping/class[1]/set[@name='fieldTree']/one-to-many/@class", hbmFirstEntity);
        assertNodeEquals("com.qcadoo.model.beans.full.FullThirdEntity",
                "/hibernate-mapping/class[1]/set[@name='fieldHasMany']/one-to-many/@class", hbmFirstEntity);
        assertNodeEquals("fieldFirstEntity_id", "/hibernate-mapping/class[1]/set[@name='fieldTree']/key/@column", hbmFirstEntity);
        assertNodeEquals("fieldFirstEntity_id", "/hibernate-mapping/class[1]/set[@name='fieldHasMany']/key/@column",
                hbmFirstEntity);
        assertNodeEquals("true", "/hibernate-mapping/class[1]/set[@name='fieldTree']/@lazy", hbmFirstEntity);
        assertNodeEquals("true", "/hibernate-mapping/class[1]/set[@name='fieldHasMany']/@lazy", hbmFirstEntity);
        assertNodeEquals("delete", "/hibernate-mapping/class[1]/set[@name='fieldTree']/@cascade", hbmFirstEntity);
        assertNodeEquals("none", "/hibernate-mapping/class[1]/set[@name='fieldHasMany']/@cascade", hbmFirstEntity);
    }

    private void assertNodeEquals(final String expectedValue, final String xpath, final Document document) throws Exception {
        assertEquals(expectedValue, xpathEngine.evaluate(xpath, document));
    }

    private void assertNodeExists(final String xpath, final Document document) throws Exception {
        assertTrue(xpathEngine.getMatchingNodes(xpath, document).getLength() > 0);
    }

    private void assertNodeNotExists(final String xpath, final Document document) throws Exception {
        assertTrue(xpathEngine.getMatchingNodes(xpath, document).getLength() == 0);
    }

    private void assertNodeCount(final int expectedCount, final String xpath, final Document document) throws Exception {
        assertEquals(expectedCount, xpathEngine.getMatchingNodes(xpath, document).getLength());
    }

}
