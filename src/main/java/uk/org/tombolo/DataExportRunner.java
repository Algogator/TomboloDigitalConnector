package uk.org.tombolo;

import com.github.fge.jsonschema.core.report.ProcessingReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.utils.DatabaseUtils;
import uk.org.tombolo.core.utils.HibernateUtil;
import uk.org.tombolo.execution.spec.DataExportSpecification;
import uk.org.tombolo.execution.spec.DataExportSpecificationValidator;
import uk.org.tombolo.execution.spec.SpecificationDeserializer;
import uk.org.tombolo.importer.DownloadUtils;
import uk.org.tombolo.importer.ImporterMatcher;

import java.io.*;
import java.util.Properties;

public class DataExportRunner extends AbstractRunner {
    private static final Logger log = LoggerFactory.getLogger(DataExportRunner.class);

    public static void main(String[] args) throws Exception {
        validateArguments(args);

        String executionSpecPath = args[0];
        String outputFile = args[1];
        String forceImports = args[2];
        Boolean clearDatabaseCache = Boolean.parseBoolean(args[3]);

        HibernateUtil.startup();
        if (clearDatabaseCache) {
            DatabaseUtils.clearAllData();
        }

        // Load API keys
        Properties apiKeys = loadApiKeys();

        // Create engine
        DataExportEngine engine = new DataExportEngine(apiKeys, new DownloadUtils());

        validateSpecification(executionSpecPath);

        try (Writer writer = getOutputWriter(outputFile)) {
            engine.execute(
                    getSpecification(executionSpecPath),
                    writer,
                    new ImporterMatcher(forceImports)
            );
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }

    private static void validateSpecification(String executionSpecPath) throws FileNotFoundException {
        ProcessingReport report = DataExportSpecificationValidator.validate(new FileReader(executionSpecPath));
        if (!report.isSuccess()) {
            DataExportSpecificationValidator.display(report);
            System.exit(1);
        }
    }

    private static DataExportSpecification getSpecification(String specificationPath) throws IOException {
        File file = new File(specificationPath);
        if (!file.exists()){
            log.error("File not found: {}", specificationPath);
            System.exit(1);
        }
        return SpecificationDeserializer.fromJsonFile(file, DataExportSpecification.class);
    }

    private static void validateArguments(String[] args) {
        if (args.length != 4){
            log.error("Use: {} {} {} {}",
                    DataExportRunner.class.getCanonicalName(),
                    "dataExportSpecFile",
                    "outputFile",
                    "clearDatabaseCache",
                    "forceImports (className:datasourceId,...)"
            );
            System.exit(1);
        }
    }

    private static Writer getOutputWriter(String path) {
        try {
            return new FileWriter(path);
        } catch (IOException e) {
            log.error("Error initialising output writer: {}", path);
            System.exit(1);
            return null;
        }
    }
}
