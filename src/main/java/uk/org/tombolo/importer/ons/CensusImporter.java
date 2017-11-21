package uk.org.tombolo.importer.ons;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.DownloadUtils;
import uk.org.tombolo.importer.utils.JSONReader;

import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

/**
 * Importer for the ONS 2011 Census using the Nomisweb API.
 */
public class CensusImporter extends AbstractONSImporter {
    private static Logger log = LoggerFactory.getLogger(CensusImporter.class);
    private static final LocalDateTime TIMESTAMP = TimedValueUtils.parseTimestampString("2011");
    private static final String SEED_URL = "https://www.nomisweb.co.uk/api/v01/dataset/def.sdmx.json";
    private ArrayList<CensusDescription> descriptions = new ArrayList<>();
    private static final Set<String> BLACK_LIST_HEADERS
            = new HashSet<>(Arrays.asList("date", "geography", "geography code", "Rural Urban"));


    public CensusImporter(Config config) throws IOException {
        super(config);
        datasourceIds = getDataSourceIDs();
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceIdString) throws Exception {
        return getDataSourceSpecObject(datasourceIdString);
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceIdString) throws Exception {
        String headerRowUrl = getDataUrl(datasourceIdString) + "&recordlimit=0";
        File headerRowStream = downloadUtils.fetchFile(new URL(headerRowUrl), getProvider().getLabel(), ".csv");
        CSVParser csvParser = new CSVParser(new FileReader(headerRowStream), CSVFormat.RFC4180.withFirstRecordAsHeader());

        List<Attribute> attributes = new ArrayList<>();
        for (String header : csvParser.getHeaderMap().keySet()) {
            if (!BLACK_LIST_HEADERS.contains(header)) {
                String attributeLabel = attributeLabelFromHeader(header);
                attributes.add(new Attribute(getProvider(), attributeLabel, header));
            }
        }
        return attributes;
    }

    private String attributeLabelFromHeader(String header) {
        // FIXME: Make sure that this generalises over all datasets
        int end = header.indexOf(";");
        return header.substring(0, end);
    }

    protected String getDataUrl(String datasourceIdString) {
        return "https://www.nomisweb.co.uk/api/v01/dataset/"
                + getRecordId(datasourceIdString)
                + ".bulk.csv?"
                + "time=latest"
                + "&" + "measures=20100"
                + "&" + "rural_urban=total"
                + "&" + "geography=TYPE298";
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {

        // Collect materialised attributes
        List<Attribute> attributes = new ArrayList<>();
        for (Attribute attribute : datasource.getTimedValueAttributes()) {
            attributes.add(AttributeUtils.getByProviderAndLabel(attribute.getProvider(), attribute.getLabel()));
        }

        // FIXME: Generalise this beyond LSOA
        SubjectType lsoa = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                OaImporter.OaType.lsoa.name(), OaImporter.OaType.lsoa.datasourceSpec.getDescription());
        List<TimedValue> timedValueBuffer = new ArrayList<>();
        String dataUrl = getDataUrl(datasource.getDatasourceSpec().getId());

        // FIXME: Use stream instead of file
        InputStream dataStream = downloadUtils.fetchInputStream(
                new URL(dataUrl), getProvider().getLabel(), ".csv");

        CSVParser csvParser = new CSVParser(new InputStreamReader(dataStream),
                CSVFormat.RFC4180.withFirstRecordAsHeader());

        csvParser.forEach(record -> {
            Subject subject = SubjectUtils.getSubjectByTypeAndLabel(lsoa, record.get("geography code"));
            if (subject != null) {
                attributes.forEach(attribute -> {
                    String value = record.get(attribute.getDescription());
                    TimedValue timedValue = new TimedValue(subject, attribute, TIMESTAMP, Double.valueOf(value));
                    timedValueBuffer.add(timedValue);
                });
            }
        });

        saveAndClearTimedValueBuffer(timedValueBuffer);
    }

    private ArrayList<CensusDescription> getSeedData() throws IOException {

        ArrayList<LinkedHashMap<String, List<String>>> jsonData =
                new JSONReader(new DownloadUtils("/tmp")
                        .fetchJSONStream(new URL(SEED_URL), "uk.gov.ons"),
                        Arrays.asList("id", "value")).getData();

        String regEx = "(qs)(\\d+)(ew)";
        Pattern pattern = Pattern.compile(regEx);

        jsonData.forEach(value -> {
            String prev = "";
            for (List<String> v : value.values()) {
                for (String s : v) {
                    if (s.toLowerCase().startsWith("nm_")) prev = s.toLowerCase();

                    Matcher matcher = pattern.matcher(s.toLowerCase());
                    if (matcher.find()) {
                        CensusDescription description = new CensusDescription();
                        description.setDataSetID(prev);
                        description.setDataSetTable(matcher.group());
                        description.setDataSetDescription(s.toLowerCase().substring(matcher.end() + 3).trim());
                        descriptions.add(description);
                    }
                }

            }
        });

        return descriptions;
    }

    private ArrayList<String> getDataSourceIDs() throws IOException {
        return getSeedData().stream().map(CensusDescription::getDataSetTable)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private DatasourceSpec getDataSourceSpecObject(String dataSourceId) {
        for (CensusDescription description : descriptions) {
            if (description.getDataSetTable().equalsIgnoreCase(dataSourceId)) {
                return new DatasourceSpec(getClass(), dataSourceId, description.getDataSetDescription(), description.getDataSetDescription(),
                        "https://www.nomisweb.co.uk/census/2011/" + dataSourceId);
            }
        }
        throw new Error("Unknown data-source-id: " + dataSourceId);
    }

    private String getRecordId(String dataSourceId) {
        for (CensusDescription description : descriptions) {
            if (description.getDataSetTable().equalsIgnoreCase(dataSourceId)) {
                return description.getDataSetID();
            }
        }
        throw new Error("Unknown data-source-id: " + dataSourceId);
    }

    class CensusDescription {
        private String dataSetID;
        private String dataSetTable;
        private String dataSetDescription;

        String getDataSetID() {
            return dataSetID;
        }

        String getDataSetTable() {
            return dataSetTable;
        }

        String getDataSetDescription() {
            return dataSetDescription;
        }

        void setDataSetID(String dataSetID) {
            this.dataSetID = dataSetID;
        }

        void setDataSetTable(String dataSetTable) {
            this.dataSetTable = dataSetTable;
        }

        void setDataSetDescription(String dataSetDescription) {
            this.dataSetDescription = dataSetDescription;
        }
    }
}