package uk.org.tombolo.importer.dft;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.ConfigurationException;
import uk.org.tombolo.importer.utils.ExcelUtils;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.utils.extraction.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Importer for DfT Accessibility information.
 *
 * https://www.gov.uk/government/statistical-data-sets/acs05-travel-time-destination-and-origin-indicators-to-key-sites-and-services-by-lower-super-output-area-lsoa
 *
 */
public class AccessibilityImporter extends AbstractDFTImporter implements Importer{
    private static final Logger log = LoggerFactory.getLogger(AccessibilityImporter.class);

    private enum DatasourceId {
        acs0501, acs0502, acs0503, acs0504, acs0505, acs0506, acs0507, acs0508
    };

    private static final String DATASOURCE_URL
            = "https://www.gov.uk/government/statistical-data-sets/" +
                "acs05-travel-time-destination-and-origin-indicators-to-key-sites-and-services-" +
                "by-lower-super-output-area-lsoa";

    private String[] datasetFiles = {
            "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357458/acs0501.xls",
            "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357460/acs0502.xls",
            "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357461/acs0503.xls",
            "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357464/acs0504.xls",
            "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357467/acs0505.xls",
            "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357468/acs0506.xls",
            "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357469/acs0507.xls",
            "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357467/acs0508.xls"
    };

    private String[] datasetDescriptions = {
            "Travel time, destination and origin indicators to Employment centres by mode of travel",
            "Travel time, destination and origin indicators to Primary schools by mode of travel",
            "Travel time, destination and origin indicators to Secondary schools by mode of travel",
            "Travel time, destination and origin indicators to Further Education institutions by mode of travel",
            "Travel time, destination and origin indicators to GPs by mode of travel",
            "Travel time, destination and origin indicators to Hospitals by mode of travel",
            "Travel time, destination and origin indicators to Food stores by mode of travel",
            "Travel time, destination and origin indicators to Town centres by mode of travel"
    };

    private int timedValueBufferSize = 1000000;

    ExcelUtils excelUtils;

    @Override
    public List<Datasource> getAllDatasources() throws Exception {
        List<Datasource> datasources = new ArrayList<>();
        for(DatasourceId datasourceId : DatasourceId.values()){
            datasources.add(getDatasource(datasourceId.name()));
        }
        return datasources;
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {
        if (excelUtils == null)
            initalize();
        DatasourceId datasourceIdValue = DatasourceId.valueOf(datasourceId);
        if (datasourceIdValue == null)
            throw new ConfigurationException("Unknown datasourceId: " + datasourceId);

        Datasource datasource = new Datasource(
                datasourceId,
                getProvider(),
                datasourceId,
                datasetDescriptions[datasourceIdValue.ordinal()]);
        datasource.setUrl(DATASOURCE_URL);
        datasource.setLocalDatafile("dft/accessibility/"+datasourceIdValue.name()+".xls");
        datasource.setRemoteDatafile(datasetFiles[datasourceIdValue.ordinal()]);

        // Attributes
        // In order to get the attributes we need to download the entire xls file, which is a bit of an overload.
        // In addition, if we want to get a list of all available datasets we need to download all the xls file.
        // An alternative would be to use a pre-compiled list of attributes with the downside that it is not
        // robust to changes in the underlying xls file.
        // FIXME: Consider using a pre-compiled list of attributes
        Workbook workbook = excelUtils.getWorkbook(datasource);
        Sheet metadataSheet = workbook.getSheet("Metadata");

        int rowId = 12;
        while(true){
            rowId++;
            Row row = metadataSheet.getRow(rowId);
            if (row == null || row.getCell(0) == null)
                break;
            String name = row.getCell(0).getStringCellValue();
            String label = row.getCell(1).getStringCellValue();
            String description = row.getCell(2).getStringCellValue();
            String parameterValue = row.getCell(3).getStringCellValue();

            if (parameterValue.startsWith("Reference"))
                continue;

            datasource.addAttribute(new Attribute(getProvider(), label, name, description, Attribute.DataType.numeric));
        }

        return datasource;
    }

    @Override
    protected int importDatasource(Datasource datasource) throws Exception {
        Workbook workbook = excelUtils.getWorkbook(datasource);
        int valueCount = 0;
        List<TimedValue> timedValueBuffer = new ArrayList<>();

        // Save Provider and Attributes
        saveProviderAndAttributes(datasource);

        // Loop over years
        for (int sheetId = 0; sheetId < workbook.getNumberOfSheets(); sheetId++){
            Sheet sheet = workbook.getSheetAt(sheetId);

            int year = -1;
            try {
                year = Integer.parseInt(sheet.getSheetName().substring(sheet.getSheetName().length()-4, sheet.getSheetName().length()));
            }catch (NumberFormatException e){
                // Sheetname does not end in a year
                continue;
            }

            // Create extractors for each timed value
            List<TimedValueExtractor> timedValueExtractors = new ArrayList<>();

            RowCellExtractor subjectExtractor = new RowCellExtractor(0, Cell.CELL_TYPE_STRING);
            ConstantExtractor timestampExtractor = new ConstantExtractor(String.valueOf(year));

            // Get the attribute label row and create TimedValueExtractors
            Row attributeLabelRow = sheet.getRow(5);
            for (int columnId = 0; columnId < attributeLabelRow.getLastCellNum(); columnId++){
                RowCellExtractor tmpAttributeLabelExtractor = new RowCellExtractor(columnId,Cell.CELL_TYPE_STRING);
                tmpAttributeLabelExtractor.setRow(attributeLabelRow);
                Attribute attribute = AttributeUtils.getByProviderAndLabel(getProvider(), tmpAttributeLabelExtractor.extract());
                if (attribute != null){
                    ConstantExtractor attributeExtractor = new ConstantExtractor(attribute.getLabel());
                    RowCellExtractor valueExtractor = new RowCellExtractor(columnId, Cell.CELL_TYPE_NUMERIC);
                    timedValueExtractors.add(new TimedValueExtractor(getProvider(), subjectExtractor, attributeExtractor, timestampExtractor, valueExtractor));
                }
            }

            // Extract timed values
            Iterator<Row> rowIterator = sheet.rowIterator();
            while(rowIterator.hasNext()){
                Row row = rowIterator.next();
                for (TimedValueExtractor extractor : timedValueExtractors){
                    subjectExtractor.setRow(row);
                    ((RowCellExtractor)extractor.getValueExtractor()).setRow(row);
                    try {
                        TimedValue timedValue = extractor.extract();
                        timedValueBuffer.add(timedValue);
                        valueCount++;
                        if (valueCount % timedValueBufferSize == 0) {
                            // Buffer is full ... we write values to db
                            saveBuffer(timedValueBuffer, valueCount);
                        }
                    }catch (BlankCellException e){
                        // We ignore this since there may be multiple blank cells in the data without having to worry
                    }catch (ExtractorException e){
                        log.warn("Could not extract value: {}",e.getMessage());
                    }
                }
            }
        }
        saveBuffer(timedValueBuffer, valueCount);

        return valueCount;
    }

    private void initalize(){
        excelUtils = new ExcelUtils(downloadUtils);
    }

    private static void saveBuffer(List<TimedValue> timedValueBuffer, int valueCount){
        log.info("Preparing to write a batch of {} values ...", timedValueBuffer.size());
        TimedValueUtils.save(timedValueBuffer);
        timedValueBuffer.clear();
        log.info("Total values written: {}", valueCount);
    }
}
