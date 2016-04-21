package uk.org.tombolo.exporter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.json.JsonValue;

import org.geotools.geojson.geom.GeometryJSON;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.GeographyType;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.GeographyTypeUtils;
import uk.org.tombolo.core.utils.GeographyUtils;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.execution.spec.AttributeSpecification;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;

public class GeoJsonExporter implements Exporter {
	
	// FIXME: Rewriter using geotools ... I could not get it to work quicly in the initial implementation (borkur)
	
	@Override
	public void write(Writer writer, DatasetSpecification datasetSpecification) throws Exception {

		// Write beginning of geography list
		writer.write("{");
		writeStringProperty(writer, 0, "type", "FeatureCollection");
		writeObjectPropertyOpening(writer, 1, "features",JsonValue.ValueType.ARRAY);
		
		int geographyCount = 0;
		for(GeographySpecification geographySpecification : datasetSpecification.getGeographySpecification()){
			GeographyType geographyType = GeographyTypeUtils.getGeographyTypeByLabel(geographySpecification.getGeographyType());
			List<Geography> geographyList = GeographyUtils
					.getGeographyByTypeAndLabelPattern(geographyType, geographySpecification.getLabelPattern());
			for (Geography geography : geographyList){
				// Geography is an a polygon or point for which data is to be output

				if (geographyCount > 0){
					// This is not the first geography
					writer.write(",\n");
				}
								
				// Open geography object
				writer.write("{");
				writeStringProperty(writer, 0, "type","Feature");
								
				// Write geometry
				GeometryJSON geoJson = new GeometryJSON();
				StringWriter geoJsonWriter = new StringWriter();
				geoJson.write(geography.getShape(),geoJsonWriter);
				writer.write(", \"geometry\" : ");
				geoJson.write(geography.getShape(), writer);

				// Open property list
				writeObjectPropertyOpening(writer, 1, "properties", JsonValue.ValueType.OBJECT);
				int propertyCount = 0;
								
				// Geography label
				writeStringProperty(writer, propertyCount, "label", geography.getLabel());
				propertyCount++;
				
				// Geography name
				writeStringProperty(writer, propertyCount, "name", geography.getName());
				propertyCount++;				
				
				// Write Attributes
				List<AttributeSpecification> attributeSpecs = datasetSpecification.getAttributeSpecification();
				writeObjectPropertyOpening(writer, propertyCount, "attributes", JsonValue.ValueType.OBJECT);
				int attributeCount = 0;
				for (AttributeSpecification attributeSpec : attributeSpecs){
					Provider provider = ProviderUtils.getByLabel(attributeSpec.getProviderLabel());
					Attribute attribute = AttributeUtils.getByProviderAndLabel(provider, attributeSpec.getAttributeLabel());
					
					// Write TimedValues
					writeAttributeProperty(writer, attributeCount, geography, attribute, attributeSpec);
					attributeCount++;
				}
				// Close attribute list
				writer.write("}");
				propertyCount++;
				
				// Close property list
				writer.write("}");
				
				// Close geography object
				writer.write("}");
				
				geographyCount++;
			}
		}
		
		// Write end of geography list
		writer.write("]}");
	}

	protected void writeStringProperty(Writer writer, int propertyCount, String key, String value) throws IOException{
		
		if (propertyCount > 0)
			writer.write(",");
		
		writer.write("\""+key+"\":\""+value+"\"");
	}

	protected void writeDoubleProperty(Writer writer, int propertyCount, String key, Double value) throws IOException{
		
		if (propertyCount > 0)
			writer.write(",");
		
		writer.write("\""+key+"\":"+value+"");
	}
	
	protected void writeObjectPropertyOpening(Writer writer, int propertyCount, String key, JsonValue.ValueType valueType) throws IOException{
		if (propertyCount > 0)
			writer.write(",");

		writer.write("\""+key+"\":");
		
		switch(valueType){
			case ARRAY:
				writer.write("[");
				break;
			case OBJECT:
				writer.write("{");
				break;
			default:
				break;	
		}
	}

	protected void writeAttributeProperty(Writer writer, int propertyCount, Geography geography, Attribute attribute, AttributeSpecification attributeSpec) throws IOException{
		// Open attribute
		writeObjectPropertyOpening(writer, propertyCount, attribute.getLabel(), JsonValue.ValueType.OBJECT);
		int subPropertyCount = 0;

		// Write name
		writeStringProperty(writer, subPropertyCount, "name", attribute.getName());
		subPropertyCount++;

		// Write provider
		writeStringProperty(writer, subPropertyCount, "provider", attribute.getProvider().getName());
		subPropertyCount++;
		
		// Write metadata
		if (attributeSpec.getAttributes() != null && attributeSpec.getAttributes().get("unit") != null){
			writeObjectPropertyOpening(writer, subPropertyCount, "metadatas", JsonValue.ValueType.ARRAY);
			switch(attributeSpec.getAttributes().get("unit")){
			case "count":
				writer.write("{");
				writeStringProperty(writer, 0, "name","unit");
				writeStringProperty(writer, 1, "type","urn:oc:uom:count");
				writeStringProperty(writer, 2, "value","count");
				writer.write("},{");
				writeStringProperty(writer, 0, "name","datatype");
				writeStringProperty(writer, 1, "type","urn:oc:datatype:numeric");
				writeStringProperty(writer, 2, "value","numeric");
				writer.write("}");
				break;
			case "fraction":
				writer.write("{");
				writeStringProperty(writer, 0, "name","unit");
				writeStringProperty(writer, 1, "type","urn:oc:uom:fraction");
				writeStringProperty(writer, 2, "value","fraction");
				writer.write("},{");
				writeStringProperty(writer, 0, "name","datatype");
				writeStringProperty(writer, 1, "type","urn:oc:datatype:numeric");
				writeStringProperty(writer, 2, "value","numeric");
				writer.write("}");
				break;
			case "percentage":
				writer.write("{");
				writeStringProperty(writer, 0, "name","unit");
				writeStringProperty(writer, 1, "type","urn:oc:uom:percentage");
				writeStringProperty(writer, 2, "value","percentage");
				writer.write("},{");
				writeStringProperty(writer, 0, "name","datatype");
				writeStringProperty(writer, 1, "type","urn:oc:datatype:numeric");
				writeStringProperty(writer, 2, "value","numeric");
				writer.write("}");
				break;
				
			}
			// Close metadatas
			writer.write("]");

			subPropertyCount++;
		}
		
		
		// Write timed values
		List<TimedValue> values = TimedValueUtils.getByGeographyAndAttribute(geography, attribute);
				
		// Open values
		writeObjectPropertyOpening(writer, subPropertyCount, "values", JsonValue.ValueType.OBJECT);
		int valueCount = 0;
		for (TimedValue value : values){
			writeDoubleProperty(writer, valueCount, value.getId().getTimestamp().toString(), value.getValue());
			valueCount++;
		}
		// Close values
		writer.write("}");
		subPropertyCount++;
		
		// Close attribute
		writer.write("}");	
	}
}
