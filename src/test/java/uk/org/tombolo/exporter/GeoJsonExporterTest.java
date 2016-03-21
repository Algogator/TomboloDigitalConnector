package uk.org.tombolo.exporter;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;

public class GeoJsonExporterTest {
	GeoJsonExporter exporter = new GeoJsonExporter();
	
	@Test
	public void testWrite() throws Exception{
		Writer writer = new FileWriter(new File("/tmp/GeoJsonExporterTest.json"));
		DatasetSpecification spec = new DatasetSpecification();
		List<GeographySpecification> geographySpecification = new ArrayList<GeographySpecification>();
		geographySpecification.add(new GeographySpecification("localAuthority","E09%"));
		spec.setGeographySpecification(geographySpecification);
		
		exporter.write(writer, spec);
		writer.flush();
		
		//FIXME: Write assertsions
	}
	
}
