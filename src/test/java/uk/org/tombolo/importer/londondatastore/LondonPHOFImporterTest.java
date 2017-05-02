package uk.org.tombolo.importer.londondatastore;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Using the following test data files:
 *
 * Local: aHR0cHM6Ly9maWxlcy5kYXRhcHJlc3MuY29tL2xvbmRvbi9kYXRhc2V0L3B1YmxpYy1oZWFsdGgtb3V0Y29tZXMtZnJhbWV3b3JrLWluZGljYXRvcnMvMjAxNS0xMS0xMFQxMjowNTo1My9waG9mLWluZGljYXRvcnMtZGF0YS1sb25kb24tYm9yb3VnaC54bHN4.xlsx
 */
public class LondonPHOFImporterTest extends AbstractTest {
	private static final String DATASOURCE_ID = "phofIndicatorsLondonBorough";
	private LondonPHOFImporter importer;

	Subject cityOfLondon;

	@Before
	public void before(){
		importer = new LondonPHOFImporter(TestFactory.DEFAULT_CONFIG);
		mockDownloadUtils(importer);
	}

	@Before
	public void addSubjectFixtures() {
		cityOfLondon = TestFactory.makeNamedSubject(TestFactory.DEFAULT_PROVIDER, "E09000001");
	}

	@Test
	public void testGetDatasource() throws Exception {
		Datasource datasource = importer.getDatasource(DATASOURCE_ID);
		
		List<Attribute> attributes = datasource.getTimedValueAttributes();
		assertEquals(150, attributes.size());
		
		String a5Name = "1.02ii - School Readiness: The percentage of Year 1 pupils achieving the expected level in the phonics screening check";
		String a5Label = DigestUtils.md5Hex(a5Name);
		Attribute a5 = attributes.stream().filter(a -> a.getLabel().equals(a5Label)).findFirst().get();
		assertEquals(importer.getProvider(), a5.getProvider());
		assertEquals(a5Label, a5.getLabel());
		assertEquals(a5Name, a5.getName());
		assertEquals(a5Name, a5.getDescription());

		String a135Name = "Supporting Information - Deprivation score (IMD 2010)";
		String a135Label = DigestUtils.md5Hex(a135Name);
		Attribute a135 = attributes.stream().filter(a -> a.getLabel().equals(a135Label)).findFirst().get();
		assertEquals(importer.getProvider(), a135.getProvider());
		assertEquals(a135Label, a135.getLabel());
		assertEquals(a135Name, a135.getName());
		assertEquals(a135Name, a135.getDescription());
	}
	
	@Test
	public void testImportDatasource() throws Exception{
		importer.importDatasource(DATASOURCE_ID);
		assertEquals(233, importer.getTimedValueCount());

		Attribute attribute = AttributeUtils.getByProviderAndLabel(
				importer.getProvider(),
				DigestUtils.md5Hex("1.01ii - Children in poverty (under 16s)"));

		TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon, attribute);
		assertEquals(11.43d, timedValue.getValue(), 0.01d);
	}
	
}
