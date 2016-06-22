package uk.org.tombolo.exporter;

import com.jayway.jsonpath.JsonPath;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.field.FieldWithProvider;
import uk.org.tombolo.field.FixedAnnotationField;
import uk.org.tombolo.field.ValuesByTimeField;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;


public class GeoJsonExporterTest extends AbstractTest {
	GeoJsonExporter exporter = new GeoJsonExporter();

	@Before
	public void addSubjectFixtures() {
		TestFactory.makeNamedSubject("E09000001");
	}

	@Test
	public void testWrite() throws Exception{
		Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr");
		TestFactory.makeTimedValue("E09000001", attribute, TestFactory.TIMESTAMP, 100d);

		Writer writer = new StringWriter();
		
		exporter.write(writer, Collections.singletonList(
				SubjectUtils.getSubjectByLabel("E09000001")
		), Collections.singletonList(
				new ValuesByTimeField("attr_label",
						new ValuesByTimeField.AttributeStruct("default_provider_label", "attr_label"))
		));

		assertEquals("E09000001", getFirstFeatureLabel(writer.toString()));
	}

	@Test
	public void testWriteWithFields() throws Exception {
		Writer writer = new StringWriter();

		exporter.write(writer,
				Arrays.asList(SubjectUtils.getSubjectByLabel("E09000001")),
				Arrays.asList(new FixedAnnotationField("some_label", "some_value"))
		);

		assertEquals("some_value", JsonPath.read(writer.toString(), "$.features[0].properties.some_label").toString());
	}

	private String getFirstFeatureLabel(String jsonString) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject root = (JSONObject) parser.parse(jsonString);
		JSONArray features = (JSONArray) root.get("features");
		JSONObject firstFeature = (JSONObject) features.get(0);
		JSONObject firstFeatureProperties = (JSONObject) firstFeature.get("properties");
		return firstFeatureProperties.get("label").toString();
	}
}
