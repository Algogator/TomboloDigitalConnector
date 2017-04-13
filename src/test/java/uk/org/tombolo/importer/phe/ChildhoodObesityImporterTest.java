package uk.org.tombolo.importer.phe;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Using the following test data files:
 *
 * Remote: http://www.noo.org.uk/securefiles/161024_1352/20150511_MSOA_Ward_Obesity.xlsx
 * Local: aHR0cDovL3d3dy5ub28ub3JnLnVrL3NlY3VyZWZpbGVzLzE2MTAyNF8xMzUyLzIwMTUwNTExX01TT0FfV2FyZF9PYmVzaXR5Lnhsc3g=.xlsx
 */
public class ChildhoodObesityImporterTest extends AbstractTest {
    private ChildhoodObesityImporter importer;

    private Subject cityOfLondon001;
    private Subject islington011;

    private Subject leeds;

    @Before
    public void setUp() throws Exception {
        importer = new ChildhoodObesityImporter();
        mockDownloadUtils(importer);

        cityOfLondon001 = TestFactory.makeNamedSubject(TestFactory.DEFAULT_PROVIDER, "E02000001");  // City of London 001
        islington011 = TestFactory.makeNamedSubject(TestFactory.DEFAULT_PROVIDER, "E02000564");  // Islington 011
        leeds = TestFactory.makeNamedSubject(TestFactory.DEFAULT_PROVIDER, "E08000035"); // Leeds
    }

    @Test
    public void getDatasourceIds() throws Exception {
        List<String> datasources = importer.getDatasourceIds();
        assertEquals(Arrays.asList("childhoodObesity"), datasources);
    }

    @Test
    public void getDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("childhoodObesity");

        assertEquals(18, datasource.getTimedValueAttributes().size());
    }

    @Test
    public void importDatasourceWard() throws Exception {
        //FIXME: Implement this if/when we decide to support wards as Subjects
    }

    @Test
    public void importDatasourceMSOA() throws Exception {
        importer.importDatasource("childhoodObesity", Arrays.asList("msoa"), null);

        Map<String, Double> groundTruthCoL001 = new HashMap();

        groundTruthCoL001.put("receptionNumberMeasured", 81d);
        groundTruthCoL001.put("year6NumberMeasured", 56d);
        groundTruthCoL001.put("receptionNumberObese", 9d);
        groundTruthCoL001.put("receptionPercentageObese", 0.1111d);
        groundTruthCoL001.put("receptionPercentageObeseLowerLimit", 0.0595d);
        groundTruthCoL001.put("receptionPercentageObeseUpperLimit", 0.1978d);
        groundTruthCoL001.put("year6NumberObese", 13d);
        groundTruthCoL001.put("year6PercentageObese", 0.2321d);
        groundTruthCoL001.put("year6PercentageObeseLowerLimit", 0.1409d);
        groundTruthCoL001.put("year6PercentageObeseUpperLimit", 0.3576d);
        groundTruthCoL001.put("receptionNumberExcessWeight", 12d);
        groundTruthCoL001.put("receptionPercentageExcessWeight", 0.1481d);
        groundTruthCoL001.put("receptionPercentageExcessWeightLowerLimit", 0.0868d);
        groundTruthCoL001.put("receptionPercentageExcessWeightUpperLimit", 0.2413d);
        groundTruthCoL001.put("year6NumberExcessWeight", 19d);
        groundTruthCoL001.put("year6PercentageExcessWeight", 0.3392d);
        groundTruthCoL001.put("year6PercentageExcessWeightLowerLimit", 0.2291d);
        groundTruthCoL001.put("year6PercentageExcessWeightUpperLimit", 0.4700d);

        for (String attributeName : groundTruthCoL001.keySet()) {
            Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeName);
            TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon001, attribute);
            assertEquals("Value for "+attributeName, groundTruthCoL001.get(attributeName), timedValue.getValue(), 0.0001d);
        }
    }

    @Test
    public void importDatasourceMSOAWithEmpty() throws Exception {
        importer.importDatasource("childhoodObesity", Arrays.asList("msoa"), null);

        Map<String, Double> groundTruthCoL001 = new HashMap();

        groundTruthCoL001.put("receptionNumberMeasured", 211d);
        groundTruthCoL001.put("year6NumberMeasured", 208d);
        groundTruthCoL001.put("receptionNumberObese", null);
        groundTruthCoL001.put("receptionPercentageObese", 0.1279d);
        groundTruthCoL001.put("receptionPercentageObeseLowerLimit", 0.0894d);
        groundTruthCoL001.put("receptionPercentageObeseUpperLimit", 0.1797d);
        groundTruthCoL001.put("year6NumberObese", null);
        groundTruthCoL001.put("year6PercentageObese", 0.2115);
        groundTruthCoL001.put("year6PercentageObeseLowerLimit", 0.1615d);
        groundTruthCoL001.put("year6PercentageObeseUpperLimit", 0.2720d);
        groundTruthCoL001.put("receptionNumberExcessWeight", null);
        groundTruthCoL001.put("receptionPercentageExcessWeight", 0.2748d);
        groundTruthCoL001.put("receptionPercentageExcessWeightLowerLimit", 0.2190d);
        groundTruthCoL001.put("receptionPercentageExcessWeightUpperLimit", 0.3387d);
        groundTruthCoL001.put("year6NumberExcessWeight", null);
        groundTruthCoL001.put("year6PercentageExcessWeight", 0.3509d);
        groundTruthCoL001.put("year6PercentageExcessWeightLowerLimit", 0.2893d);
        groundTruthCoL001.put("year6PercentageExcessWeightUpperLimit", 0.4179d);

        for (String attributeName : groundTruthCoL001.keySet()) {
            Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeName);
            TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(islington011, attribute);
            if (groundTruthCoL001.get(attributeName) == null) {
                assertNull(timedValue);
            }else{
                assertEquals("Value for " + attributeName, groundTruthCoL001.get(attributeName), timedValue.getValue(), 0.0001d);
            }
        }
    }

    @Test
    public void importDatasourceLA() throws Exception {
        importer.importDatasource("childhoodObesity", Arrays.asList("la"), null);

        Map<String, Double> groundTruthCoL001 = new HashMap();

        groundTruthCoL001.put("receptionNumberMeasured", 26128d);
        groundTruthCoL001.put("year6NumberMeasured", 19299d);
        groundTruthCoL001.put("receptionNumberObese", 2396d);
        groundTruthCoL001.put("receptionPercentageObese", 0.0917d);
        groundTruthCoL001.put("receptionPercentageObeseLowerLimit", 0.0882d);
        groundTruthCoL001.put("receptionPercentageObeseUpperLimit", 0.0952d);
        groundTruthCoL001.put("year6NumberObese", 3772d);
        groundTruthCoL001.put("year6PercentageObese", 0.1954d);
        groundTruthCoL001.put("year6PercentageObeseLowerLimit", 0.1899d);
        groundTruthCoL001.put("year6PercentageObeseUpperLimit", 0.2011d);
        groundTruthCoL001.put("receptionNumberExcessWeight", 5984d);
        groundTruthCoL001.put("receptionPercentageExcessWeight", 0.2290d);
        groundTruthCoL001.put("receptionPercentageExcessWeightLowerLimit", 0.2239d);
        groundTruthCoL001.put("receptionPercentageExcessWeightUpperLimit", 0.2341d);
        groundTruthCoL001.put("year6NumberExcessWeight", 6633d);
        groundTruthCoL001.put("year6PercentageExcessWeight", 0.3436d);
        groundTruthCoL001.put("year6PercentageExcessWeightLowerLimit", 0.3370d);
        groundTruthCoL001.put("year6PercentageExcessWeightUpperLimit", 0.3504d);

        for (String attributeName : groundTruthCoL001.keySet()) {
            Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeName);
            TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(leeds, attribute);
            assertEquals("Value for "+attributeName, groundTruthCoL001.get(attributeName), timedValue.getValue(), 0.0001d);
        }
    }

}