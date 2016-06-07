package uk.org.tombolo;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import uk.org.tombolo.execution.spec.DataExportSpecification;
import uk.org.tombolo.exporter.CSVExporter;
import uk.org.tombolo.exporter.GeoJsonExporter;

public class DataExportSpecificationBuilder implements JSONAware {
    private JSONObject jsonSpec;
    private JSONArray subjectSpec;
    private JSONArray datasourceSpec;
    private JSONArray transformSpec;
    private JSONArray attributeSpec;

    private DataExportSpecificationBuilder() {
        jsonSpec = new JSONObject();
        JSONObject datasetSpec = new JSONObject();
        subjectSpec = new JSONArray();
        datasourceSpec = new JSONArray();
        transformSpec = new JSONArray();
        attributeSpec = new JSONArray();

        jsonSpec.put("datasetSpecification", datasetSpec);
        datasetSpec.put("subjectSpecification", subjectSpec);
        datasetSpec.put("datasourceSpecification", datasourceSpec);
        datasetSpec.put("transformSpecification", transformSpec);
        datasetSpec.put("attributeSpecification", attributeSpec);
    }

    public DataExportSpecificationBuilder setExporterClass(String exporterClass) {
        jsonSpec.put("exporterClass", exporterClass);
        return this;
    }

    public DataExportSpecification build() {
        return DataExportSpecification.fromJson(toJSONString());
    }

    @Override
    public String toJSONString() {
        return jsonSpec.toJSONString();
    }

    public DataExportSpecificationBuilder addSubjectSpecification(SubjectSpecificationBuilder subjectSpecificationBuilder) {
        subjectSpec.add(subjectSpecificationBuilder);
        return this;
    }

    public DataExportSpecificationBuilder addAttributeSpecification(String providerLabel, String attributeLabel) {
        JSONObject attribute = new JSONObject();
        attribute.put("providerLabel", providerLabel);
        attribute.put("attributeLabel", attributeLabel);
        attributeSpec.add(attribute);
        return this;
    }

    public DataExportSpecificationBuilder addDatasourceSpecification(String importerClass, String datasourceId) {
        JSONObject datasource = new JSONObject();
        datasource.put("importerClass", importerClass);
        datasource.put("datasourceId", datasourceId);
        datasourceSpec.add(datasource);
        return this;
    }

    public DataExportSpecificationBuilder addTransformSpecification(TransformSpecificationBuilder transformSpecificationBuilder) {
        transformSpec.add(transformSpecificationBuilder);
        return this;
    }

    public static DataExportSpecificationBuilder withCSVExporter() {
        DataExportSpecificationBuilder builder =  new DataExportSpecificationBuilder();
        builder.setExporterClass(CSVExporter.class.getCanonicalName());
        return builder;
    }

    public static DataExportSpecificationBuilder withGeoJsonExporter() {
        DataExportSpecificationBuilder builder = new DataExportSpecificationBuilder();
        builder.setExporterClass(GeoJsonExporter.class.getCanonicalName());
        return builder;
    }
}
