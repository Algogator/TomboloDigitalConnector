package uk.org.tombolo.importer.dft;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;

public class TrafficCountImporterTest {

	TrafficCountImporter importer = new TrafficCountImporter();
	
	@Test
	public void testGetProvider(){
		Provider provider = importer.getProvider();
		assertEquals("uk.gov.dft", provider.getLabel());
		assertEquals("Department for Transport", provider.getName());
	}
	
	@Test
	public void testGetAllDatasources() throws Exception {
		List<Datasource> datasources = importer.getAllDatasources();
		assertEquals(220,datasources.size());
	}
	
	@Test
	public void testGetDatasource() throws Exception {
		Datasource datasource = importer.getDatasource("London");
		assertEquals("http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/region/London.csv",datasource.getRemoteDatafile());
		assertEquals("dft/traffic/region/London.csv", datasource.getLocalDatafile());
		
		datasource = importer.getDatasource("North East");
		assertEquals("http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/region/North+East.csv",datasource.getRemoteDatafile());
		assertEquals("dft/traffic/region/North_East.csv", datasource.getLocalDatafile());
		
		datasource = importer.getDatasource("Aberdeen City");
		assertEquals("http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/la/Aberdeen+City.csv",datasource.getRemoteDatafile());
		assertEquals("dft/traffic/la/Aberdeen_City.csv", datasource.getLocalDatafile());
		
		datasource = importer.getDatasource("Bristol, City of");
		assertEquals("http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/la/Bristol%2C+City+of.csv",datasource.getRemoteDatafile());
		assertEquals("dft/traffic/la/Bristol__City_of.csv", datasource.getLocalDatafile());
	}
	
	@Test
	public void testImportDatasource() throws Exception {
		int count = importer.importDatasource("London");
	}
	
}
