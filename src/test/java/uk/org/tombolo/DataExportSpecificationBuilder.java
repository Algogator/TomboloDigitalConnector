package uk.org.tombolo;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.org.tombolo.execution.spec.DataExportSpecification;
import uk.org.tombolo.exporter.GeoJsonExporter;

public class DataExportSpecificationBuilder {
    private JSONObject jsonSpec;
    private JSONArray geographySpec;
    private JSONArray datasourceSpec;
    private JSONArray transformSpec;
    private JSONArray attributeSpec;

    private DataExportSpecificationBuilder() {
        jsonSpec = new JSONObject();
        JSONObject datasetSpec = new JSONObject();
        geographySpec = new JSONArray();
        datasourceSpec = new JSONArray();
        transformSpec = new JSONArray();
        attributeSpec = new JSONArray();

        jsonSpec.put("datasetSpecification", datasetSpec);
        datasetSpec.put("geographySpecification", geographySpec);
        datasetSpec.put("datasourceSpecification", datasourceSpec);
        datasetSpec.put("transformSpecification", transformSpec);
        datasetSpec.put("attributeSpecification", attributeSpec);
    }

    public static DataExportSpecificationBuilder fromBlankGeoJson() {
        DataExportSpecificationBuilder builder =  new DataExportSpecificationBuilder();
        builder.setExporterClass(GeoJsonExporter.class.getCanonicalName());
        return builder;
    }

    public DataExportSpecificationBuilder setExporterClass(String exporterClass) {
        jsonSpec.put("exporterClass", exporterClass);
        return this;
    }

    public DataExportSpecification build() {
        return DataExportSpecification.fromJson(buildString());
    }

    public String buildString() {
        return jsonSpec.toJSONString();
    }
}
