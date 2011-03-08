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

    private static Document hbmDocument;

    @BeforeClass
    public static void init() throws Exception {
        hbmInputStream = modelXmlToHbmConverter.convert(Utils.FULL_XML_RESOURCE).iterator().next();
        hbmInputSource = new InputSource(modelXmlToHbmConverter.convert(Utils.FULL_XML_RESOURCE).iterator().next());
        hbmDocument = buildControlDocument(new InputSource(modelXmlToHbmConverter.convert(Utils.FULL_XML_RESOURCE).iterator()
                .next()));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailForInvalidModelXml() throws Exception {
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
        assertNodeCount(3, "/hibernate-mapping/class");
    }

    @Test
    public void shouldCreateNameMergingPluginAndEntityNames() throws Exception {
        assertNodeEquals("com.qcadoo.model.beans.full.FullFirstEntity", "/hibernate-mapping/class[1]/@name");
        assertNodeEquals("com.qcadoo.model.beans.full.FullSecondEntity", "/hibernate-mapping/class[2]/@name");
        assertNodeEquals("com.qcadoo.model.beans.full.FullThirdEntity", "/hibernate-mapping/class[3]/@name");
    }

    @Test
    public void shouldCreateTableMergingPluginAndEntityNames() throws Exception {
        assertNodeEquals("full_firstEntity", "/hibernate-mapping/class[1]/@table");
        assertNodeEquals("full_secondEntity", "/hibernate-mapping/class[2]/@table");
        assertNodeEquals("full_thirdEntity", "/hibernate-mapping/class[3]/@table");
    }

    @Test
    public void shouldDefineSqlDeleteForNotDeletableEntities() throws Exception {
        assertNodeEquals("delete must not be executed on full_firstEntity", "/hibernate-mapping/class[1]/sql-delete/text()");
    }

    @Test
    public void shouldNotDefineSqlDeleteForDeletableEntities() throws Exception {
        assertNodeNotExists("/hibernate-mapping/class[2]/sql-delete");
    }

    @Test
    public void shouldDefineSqlInsertForNotInsertableEntities() throws Exception {
        assertNodeEquals("insert must not be executed on full_firstEntity", "/hibernate-mapping/class[1]/sql-insert/text()");
    }

    @Test
    public void shouldNotDefineSqlInsertForInsertableEntities() throws Exception {
        assertNodeNotExists("/hibernate-mapping/class[2]/sql-insert");
    }

    @Test
    public void shouldDefineSqlUpdateForNotUpdatableEntities() throws Exception {
        assertNodeEquals("update must not be executed on full_secondEntity", "/hibernate-mapping/class[2]/sql-update/text()");
    }

    @Test
    public void shouldNotDefineSqlUpdateForUpdatableEntities() throws Exception {
        assertNodeNotExists("/hibernate-mapping/class[1]/sql-update");
    }

    @Test
    public void shouldDefineIdColumn() throws Exception {
        assertNodeExists("/hibernate-mapping/class[1]/id");
        assertNodeEquals("id", "/hibernate-mapping/class[1]/id/@column");
        assertNodeEquals("id", "/hibernate-mapping/class[1]/id/@name");
        assertNodeEquals("long", "/hibernate-mapping/class[1]/id/@type");
        assertNodeExists("/hibernate-mapping/class[1]/id/generator");
        assertNodeEquals("increment", "/hibernate-mapping/class[1]/id/generator/@class");
    }

    @Test
    public void shouldDefineProperties() throws Exception {
        assertNodeCount(12, "/hibernate-mapping/class[1]/property");
        assertNodeCount(0, "/hibernate-mapping/class[2]/property");
        assertNodeCount(0, "/hibernate-mapping/class[3]/property");
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldInteger' and @type='integer']");
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldString' and @type='string']");
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldText' and @type='text']");
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldDecimal' and @type='big_decimal']");
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldDatetime' and @type='timestamp']");
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldDate' and @type='date']");
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldBoolean' and @type='boolean']");
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldDictionary' and @type='string']");
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldOtherDictionary' and @type='string']");
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldEnum' and @type='string']");
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldPassword' and @type='string']");
    }

    @Test
    public void shouldIgnoreNotPersistentProperties() throws Exception {
        assertNodeNotExists("/hibernate-mapping/class[1]/property[@name='fieldStringNotPersistent']");
    }

    @Test
    public void shouldIgnorePropertiesWithExpression() throws Exception {
        assertNodeNotExists("/hibernate-mapping/class[1]/property[@name='fieldStringWithExpression']");
    }

    @Test
    public void shouldDefinePriorityProperty() throws Exception {
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldPriority' and @type='integer' and @not-null='true']");
    }

    @Test
    public void shouldDefineUniqueProperty() throws Exception {
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldInteger' and @unique='true']");
        assertNodeCount(1, "/hibernate-mapping/class/property[@unique='true']");
    }

    @Test
    public void shouldDefineNotNullProperty() throws Exception {
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldInteger' and @not-null='true']");
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldString' and @not-null='true']");
        assertNodeExists("/hibernate-mapping/class[1]/property[@name='fieldPriority' and @not-null='true']");
        assertNodeCount(3, "/hibernate-mapping/class/property[@not-null='true']");
    }

    @Test
    public void shouldDefineLengthProperty() throws Exception {
        assertNodeEquals("3", "/hibernate-mapping/class[1]/property[@name='fieldInteger']/@length");
        assertNodeEquals("2", "/hibernate-mapping/class[1]/property[@name='fieldString']/@length");
        assertNodeCount(2, "/hibernate-mapping/class/property/@length");
    }

    @Test
    public void shouldDefineScaleProperty() throws Exception {
        assertNodeEquals("4", "/hibernate-mapping/class[1]/property[@name='fieldDecimal']/@scale");
        assertNodeCount(1, "/hibernate-mapping/class/property/@scale");
    }

    @Test
    public void shouldDefinePrecisionProperty() throws Exception {
        assertNodeEquals("4", "/hibernate-mapping/class[1]/property[@name='fieldInteger']/@precision");
        assertNodeEquals("2", "/hibernate-mapping/class[1]/property[@name='fieldDecimal']/@precision");
        assertNodeCount(2, "/hibernate-mapping/class/property/@precision");
    }

    @Test
    public void shouldDefineBelongsToRelation() throws Exception {
        assertNodeCount(2, "/hibernate-mapping/class[1]/many-to-one");
        assertNodeCount(2, "/hibernate-mapping/class[2]/many-to-one");
        assertNodeCount(1, "/hibernate-mapping/class[3]/many-to-one");
        assertNodeExists("/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity']");
        assertNodeEquals("fieldSecondEntity_id",
                "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity']/column/@name");
        assertNodeExists("/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity2']");
        assertNodeEquals("fieldSecondEntity2_id",
                "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity2']/column/@name");
        assertNodeEquals("com.qcadoo.model.beans.other.OtherSecondEntity",
                "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity']/@class");
        assertNodeEquals("com.qcadoo.model.beans.other.OtherSecondEntity",
                "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity2']/@class");
        assertNodeEquals("none", "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity']/@cascade");
        assertNodeEquals("none", "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity2']/@cascade");
        assertNodeEquals("false", "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity']/@lazy");
        assertNodeEquals("proxy", "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity2']/@lazy");
        assertNodeEquals("false", "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity']/@not-null");
        assertNodeEquals("true", "/hibernate-mapping/class[1]/many-to-one[@name='fieldSecondEntity2']/@not-null");
    }

    @Test
    public void shouldDefineHasManyRelation() throws Exception {
        assertNodeCount(2, "/hibernate-mapping/class[1]/set");
        assertNodeCount(1, "/hibernate-mapping/class[2]/set");
        assertNodeCount(0, "/hibernate-mapping/class[3]/set");
        assertNodeExists("/hibernate-mapping/class[1]/set[@name='fieldTree']");
        assertNodeExists("/hibernate-mapping/class[1]/set[@name='fieldHasMany']");
        assertNodeEquals("com.qcadoo.model.beans.full.FullSecondEntity",
                "/hibernate-mapping/class[1]/set[@name='fieldTree']/one-to-many/@class");
        assertNodeEquals("com.qcadoo.model.beans.full.FullThirdEntity",
                "/hibernate-mapping/class[1]/set[@name='fieldHasMany']/one-to-many/@class");
        assertNodeEquals("fieldFirstEntity_id", "/hibernate-mapping/class[1]/set[@name='fieldTree']/key/@column");
        assertNodeEquals("fieldFirstEntity_id", "/hibernate-mapping/class[1]/set[@name='fieldHasMany']/key/@column");
        assertNodeEquals("true", "/hibernate-mapping/class[1]/set[@name='fieldTree']/@lazy");
        assertNodeEquals("true", "/hibernate-mapping/class[1]/set[@name='fieldHasMany']/@lazy");
        assertNodeEquals("delete", "/hibernate-mapping/class[1]/set[@name='fieldTree']/@cascade");
        assertNodeEquals("none", "/hibernate-mapping/class[1]/set[@name='fieldHasMany']/@cascade");
    }

    private void assertNodeEquals(final String expectedValue, final String xpath) throws Exception {
        assertEquals(expectedValue, xpathEngine.evaluate(xpath, hbmDocument));
    }

    private void assertNodeExists(final String xpath) throws Exception {
        assertTrue(xpathEngine.getMatchingNodes(xpath, hbmDocument).getLength() > 0);
    }

    private void assertNodeNotExists(final String xpath) throws Exception {
        assertTrue(xpathEngine.getMatchingNodes(xpath, hbmDocument).getLength() == 0);
    }

    private void assertNodeCount(final int expectedCount, final String xpath) throws Exception {
        assertEquals(expectedCount, xpathEngine.getMatchingNodes(xpath, hbmDocument).getLength());
    }

}
