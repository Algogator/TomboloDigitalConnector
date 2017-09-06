package uk.org.tombolo.importer.osm;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for green space importer from open street map
 *
 * File: 87e9e913-8849-3dc9-8f67-9dac9fdeccb4.osm.pbf
 */
public class GreenSpaceImporterTest extends AbstractTest {
    private static final String TEST_AREA = "europe/great-britain/england/herefordshire";
    private static GreenSpaceImporter importer;

    @Before
    public void before(){
        importer = new GreenSpaceImporter(TestFactory.DEFAULT_CONFIG);
        mockDownloadUtils(importer);
    }

    @Test
    public void getDatasource() throws Exception {
        List<String> datasources = importer.getDatasourceIds();

        assertEquals(1, datasources.size());
        assertEquals("OSMGreenSpace", datasources.get(0));
    }

    @Test
    public void getFixedValueAttributes() throws Exception {
        List<Attribute> attributes = importer.getFixedValueAttributes("OSMGreenSpace");
        assertEquals(3, attributes.size());
    }

    @Test
    public void importDatasource() throws Exception {
        importer.importDatasource("OSMGreenSpace", Arrays.asList(TEST_AREA), Collections.emptyList(), null);

        // Test attribute import
        Attribute landuse = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "landuse");
        assertEquals("org.openstreetmap", landuse.getProvider().getLabel());
        assertEquals("landuse", landuse.getLabel());

        Attribute natural = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "natural");
        assertEquals("org.openstreetmap", natural.getProvider().getLabel());
        assertEquals("natural", natural.getLabel());

        // Test subjects import
        SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel("org.openstreetmap","OSMEntity");
        Subject osm1 = SubjectUtils.getSubjectByTypeAndLabel(subjectType,"osm35833175");
        assertNull(osm1.getName());
        testFixedValue(osm1, "natural", "wood");

        Subject osm2 = SubjectUtils.getSubjectByTypeAndLabel(subjectType, "osm363465081");
        assertNull(osm2.getName());
        testFixedValue(osm2, "landuse", "forest");
        testFixedValue(osm2, "description", "Plantation");

        Subject osm3 = SubjectUtils.getSubjectByTypeAndLabel(subjectType, "osm126115156");
        assertEquals("Putson Coppice", osm3.getName());
        testFixedValue(osm3, "natural", "wood");
        testFixedValue(osm3, "source", "Bing/OSOpenData");
    }

    private void testFixedValue(Subject subject, String attributeLabel, String value) {
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
        FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subject, attribute);
        assertEquals("Value for key (" + subject.getLabel() + "," + attributeLabel + ")", value, fixedValue.getValue());
    }
}
