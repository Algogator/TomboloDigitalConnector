package uk.org.tombolo.importer.ons;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Using the following test data files:
 *
 * lsoas aHR0cDovL2dlb3BvcnRhbC5zdGF0aXN0aWNzLmdvdi51ay9kYXRhc2V0cy9kYTgzMWY4MDc2NDM0Njg4OTgzN2M3MjUwOGYwNDZmYV8yLmdlb2pzb24=.json
 * msoas aHR0cDovL2dlb3BvcnRhbC5zdGF0aXN0aWNzLmdvdi51ay9kYXRhc2V0cy84MjZkYzg1ZmI2MDA0NDA4ODk0ODBmNGQ5ZGJiMWEyNF8yLmdlb2pzb24=.json
 * las aHR0cDovL2dlb3BvcnRhbC5zdGF0aXN0aWNzLmdvdi51ay9kYXRhc2V0cy8zOTQzYzIxMTRkNzY0Mjk0YTdjMDA3OWM0MDIwZDU1OF80Lmdlb2pzb24=.json
 */
public class OaImporterTest extends AbstractTest {
    OaImporter importer;

    @Before
    public void setUp() throws Exception {
        importer = new OaImporter(TestFactory.DEFAULT_CONFIG);
        importer.setDownloadUtils(makeTestDownloadUtils());
    }

    @Test
    public void testGetDatasourceIds() throws Exception {
        List<String> datasources = importer.getDatasourceIds();
        assertEquals(Arrays.asList("lsoa", "msoa", "localAuthority"), datasources);
    }

    @Test
    public void testGetDatasourceLSOA() throws Exception {
        Datasource datasource = importer.getDatasource("lsoa");
        assertEquals("lsoa", datasource.getId());
        assertEquals("uk.gov.ons", datasource.getProvider().getLabel());
        assertEquals("LSOA", datasource.getName());
        assertEquals("Lower Layer Super Output Areas", datasource.getDescription());
    }

    @Test
    public void testGetDatasourceMSOA() throws Exception {
        Datasource datasource = importer.getDatasource("msoa");
        assertEquals("msoa", datasource.getId());
        assertEquals("uk.gov.ons", datasource.getProvider().getLabel());
        assertEquals("MSOA", datasource.getName());
        assertEquals("Middle Layer Super Output Areas", datasource.getDescription());
    }

    @Test
    public void testImportLsoas() throws Exception {
        importer.importDatasource("lsoa");
        SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(importer.getProvider().getLabel(), "lsoa");
        Subject lsoa = SubjectUtils.getSubjectByTypeAndLabel(subjectType, "E01000002");

        assertEquals("City of London 001B", lsoa.getName());
        assertEquals("lsoa", lsoa.getSubjectType().getLabel());
        assertEquals(-0.09252710274629854, lsoa.getShape().getCentroid().getX(), 0.1E-6);
        assertEquals(51.51821627457435, lsoa.getShape().getCentroid().getY(), 0.1E-6);
        assertEquals(100, importer.getSubjectCount());
    }

    @Test
    public void testImportMsoas() throws Exception {
        importer.importDatasource("msoa");
        SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(importer.getProvider().getLabel(), "msoa");
        Subject lsoa = SubjectUtils.getSubjectByTypeAndLabel(subjectType,"E02000093");

        assertEquals("Brent 001", lsoa.getName());
        assertEquals("msoa", lsoa.getSubjectType().getLabel());
        assertEquals(-0.2746307279027593, lsoa.getShape().getCentroid().getX(), 0.1E-6);
        assertEquals(51.59338282612998, lsoa.getShape().getCentroid().getY(), 0.1E-6);
        assertEquals(100, importer.getSubjectCount());
    }

    @Test
    public void testImportLocalAuthorities() throws Exception {
        importer.importDatasource("localAuthority");
        SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(importer.getProvider().getLabel(), "localAuthority");
        Subject localAuthority = SubjectUtils.getSubjectByTypeAndLabel(subjectType,"E06000001");

        assertEquals("Hartlepool", localAuthority.getName());
        assertEquals("localAuthority", localAuthority.getSubjectType().getLabel());
        assertEquals(-1.2592784934731256, localAuthority.getShape().getCentroid().getX(), 0.1E-6);
        assertEquals(54.66957856523336, localAuthority.getShape().getCentroid().getY(), 0.1E-6);
        assertEquals(7, importer.getSubjectCount());
    }
}