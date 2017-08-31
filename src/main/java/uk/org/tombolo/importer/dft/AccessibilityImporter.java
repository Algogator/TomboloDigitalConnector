package uk.org.tombolo.importer.dft;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.DatasourceSpec;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.ons.OaImporter;
import uk.org.tombolo.importer.utils.ExcelUtils;
import uk.org.tombolo.importer.utils.extraction.ConstantExtractor;
import uk.org.tombolo.importer.utils.extraction.RowCellExtractor;
import uk.org.tombolo.importer.utils.extraction.TimedValueExtractor;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Importer for DfT Accessibility information.
 *
 * https://www.gov.uk/government/statistical-data-sets/acs05-travel-time-destination-and-origin-indicators-to-key-sites-and-services-by-lower-super-output-area-lsoa
 *
 */
public class AccessibilityImporter extends AbstractDFTImporter {
    private static final Logger log = LoggerFactory.getLogger(AccessibilityImporter.class);
    private static final String DATASET_FILE_SUFFIX = ".xls";
    private static final String DATASOURCE_URL
            = "https://www.gov.uk/government/statistical-data-sets/" +
            "acs05-travel-time-destination-and-origin-indicators-to-key-sites-and-services-" +
            "by-lower-super-output-area-lsoa";

    private enum DatasourceId {
        acs0501(new DatasourceSpec(AccessibilityImporter.class, "acs0501", "Employment centres",
                "Travel time, destination and origin indicators to Employment centres by mode of travel",
                DATASOURCE_URL),
                "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357458/acs0501.xls"),
        acs0502(new DatasourceSpec(AccessibilityImporter.class, "acs0502", "Primary schools",
                "Travel time, destination and origin indicators to Primary schools by mode of travel",
                DATASOURCE_URL),
                "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357460/acs0502.xls"),
        acs0503(new DatasourceSpec(AccessibilityImporter.class, "acs0503", "Secondary schools",
                "Travel time, destination and origin indicators to Secondary schools by mode of travel",
                DATASOURCE_URL),
                "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357461/acs0503.xls"),
        acs0504(new DatasourceSpec(AccessibilityImporter.class, "acs0504","Further Education institutions",
                "Travel time, destination and origin indicators to Further Education institutions by mode of travel",
                DATASOURCE_URL),
                "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357464/acs0504.xls"),
        acs0505(new DatasourceSpec(AccessibilityImporter.class, "acs0505", "GPs",
                "Travel time, destination and origin indicators to GPs by mode of travel",
                DATASOURCE_URL),
                "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357467/acs0505.xls"),
        acs0506(new DatasourceSpec(AccessibilityImporter.class, "acs0506", "Hospitals",
                "Travel time, destination and origin indicators to Hospitals by mode of travel",
                DATASOURCE_URL),
                "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357468/acs0506.xls"),
        acs0507(new DatasourceSpec(AccessibilityImporter.class, "acs0507", "Food stores",
                "Travel time, destination and origin indicators to Food stores by mode of travel",
                DATASOURCE_URL),
                "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357469/acs0507.xls"),
        acs0508(new DatasourceSpec(AccessibilityImporter.class, "acs0508", "Town centres",
                "Travel time, destination and origin indicators to Town centres by mode of travel",
                DATASOURCE_URL),
                "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357467/acs0508.xls"
        );

        private DatasourceSpec datasourceSpec;
        private String dataFile;
        DatasourceId(DatasourceSpec datasourceSpec, String dataFile) {
            this.datasourceSpec = datasourceSpec;
            this.dataFile = dataFile;
        }
    }

    private Workbook workbook;

    ExcelUtils excelUtils = new ExcelUtils();

    public AccessibilityImporter(Config config){
        super(config);
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }


    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        return DatasourceId.valueOf(datasourceId).datasourceSpec;
    }

    @Override
    public List<Attribute> getDatasourceTimedValueAttributes(String datasourceId) throws Exception {
        DatasourceId datasourceIdValue = DatasourceId.valueOf(datasourceId);
        workbook = excelUtils.getWorkbook(
                downloadUtils.fetchInputStream(new URL(datasourceIdValue.dataFile), getProvider().getLabel(), DATASET_FILE_SUFFIX));
        Sheet metadataSheet = workbook.getSheet("Metadata");

        List<Attribute> attributes = new ArrayList<>();
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

            attributes.add(new Attribute(getProvider(), label, name, description, Attribute.DataType.numeric));
        }

        return attributes;
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        SubjectType subjectType = OaImporter.getSubjectType(OaImporter.OaType.lsoa);

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

            RowCellExtractor subjectExtractor = new RowCellExtractor(0, CellType.STRING);
            ConstantExtractor timestampExtractor = new ConstantExtractor(String.valueOf(year));

            // Get the attribute label row and create TimedValueExtractors
            Row attributeLabelRow = sheet.getRow(5);
            for (int columnId = 0; columnId < attributeLabelRow.getLastCellNum(); columnId++){
                RowCellExtractor tmpAttributeLabelExtractor = new RowCellExtractor(columnId, CellType.STRING);
                tmpAttributeLabelExtractor.setRow(attributeLabelRow);
                Attribute attribute = AttributeUtils.getByProviderAndLabel(getProvider(), tmpAttributeLabelExtractor.extract());
                if (attribute != null){
                    ConstantExtractor attributeExtractor = new ConstantExtractor(attribute.getLabel());
                    RowCellExtractor valueExtractor = new RowCellExtractor(columnId, CellType.NUMERIC);
                    timedValueExtractors.add(new TimedValueExtractor(getProvider(), subjectType, subjectExtractor, attributeExtractor, timestampExtractor, valueExtractor));
                }
            }

            // Extract timed values
            excelUtils.extractAndSaveTimedValues(sheet, this, timedValueExtractors);
        }
    }
}
