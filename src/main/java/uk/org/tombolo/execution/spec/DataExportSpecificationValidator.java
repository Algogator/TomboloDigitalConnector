package uk.org.tombolo.execution.spec;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DataExportSpecificationValidator {
    static Logger log = LoggerFactory.getLogger(DataExportSpecificationValidator.class);

    public static ProcessingReport validate(File jsonFile) {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            JsonNode node = JsonLoader.fromURL(loader.getResource("data_export_specification_schema.json"));
            JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            ObjectMapper mapper = new ObjectMapper().configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            return factory.getJsonSchema(node).validate(mapper.readTree(jsonFile));
        } catch (Exception e) {
            throw new Error("Validator JSON Schema is invalid", e);
        }
    }

    public static void display(ProcessingReport report) {
        String logString = "The specification file contains errors\n\n";

        for (ProcessingMessage message : report) {
            logString += message.getMessage() + "\n";
            logString += message.toString() + "\n\n";
        }

        log.error(logString);
    }
}
