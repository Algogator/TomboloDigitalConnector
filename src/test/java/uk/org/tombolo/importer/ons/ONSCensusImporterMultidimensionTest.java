package uk.org.tombolo.importer.ons;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.utils.AttributeUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Using the following test data files:
 *
 * Remote: http://data.statistics.gov.uk/ons/datasets/csv/CSV_QS103EW_2011STATH_NAT_OA_REL_1.A.A_EN.zip
 * Local: aHR0cDovL2RhdGEuc3RhdGlzdGljcy5nb3YudWsvb25zL2RhdGFzZXRzL2Nzdi9DU1ZfUVMxMDNFV18yMDExU1RBVEhfTkFUX09BX1JFTF8xLkEuQV9FTi56aXA=.zip
 */
public class ONSCensusImporterMultidimensionTest extends AbstractONSCensusImporterTest {

	// Age by single year
	private static final String datasourceId = "QS103EW";

	@Before
	public void addSubjectFixtures() {
		TestFactory.makeNamedSubject(TestFactory.DEFAULT_PROVIDER, "E01002766");
	}
		
	@Test
	public void testGetDatasetDetails() throws Exception{
				
		Datasource datasourceDetails = importer.getDatasource(datasourceId);
		
		assertEquals(datasourceId, datasourceDetails.getName());
		assertEquals("Age by single year",datasourceDetails.getDescription());
		assertEquals(102, datasourceDetails.getTimedValueAttributes().size());
		assertEquals("CL_0000053_1", datasourceDetails.getTimedValueAttributes().get(0).getLabel());
		assertEquals("T.b.a.", datasourceDetails.getTimedValueAttributes().get(0).getDescription());
		assertEquals("CL_0000053_3", datasourceDetails.getTimedValueAttributes().get(2).getLabel());
		assertEquals("T.b.a.", datasourceDetails.getTimedValueAttributes().get(2).getDescription());
		assertEquals("http://data.statistics.gov.uk/ons/datasets/csv/CSV_QS103EW_2011STATH_NAT_OA_REL_1.A.A_EN.zip", datasourceDetails.getRemoteDatafile());
	}

	@Test
	public void testLoadDataset() throws Exception{
		
		importer.importDatasource(datasourceId);
		
		assertEquals(102, importer.getTimedValueCount());

		Attribute attribute0 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "CL_0000053_0");
		assertNull(attribute0);
		
		Attribute attribute1 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "CL_0000053_1");
		assertEquals("Age (T102A) - Total: All categories: Age", attribute1.getName());

		Attribute attribute2 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "CL_0000053_2");
		assertEquals("Age (T102A) - Age under 1", attribute2.getName());

		Attribute attribute3 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "CL_0000053_3");
		assertEquals("Age (T102A) - Age 1", attribute3.getName());

		Attribute attribute25 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "CL_0000053_25");
		assertEquals("Age (T102A) - Age 23", attribute25.getName());

		Attribute attribute101 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "CL_0000053_101");
		assertEquals("Age (T102A) - Age 99", attribute101.getName());

		Attribute attribute102 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "CL_0000053_102");
		assertEquals("Age (T102A) - Age 100 and over", attribute102.getName());
		
		Attribute attribute103 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "CL_0000053_103");
		assertNull(attribute103);
	}
	
}
