package uk.org.tombolo.field;

import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.importer.ons.AbstractONSImporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class FieldValueSumFieldTest extends AbstractTest {
    FieldValueSumField field;

    Subject subject;

    @Before
    public void setUp() throws Exception {
        List<FieldSpecification> fieldSpecificationList = new ArrayList<>();

        FieldSpecification fs1 = new FieldSpecification("uk.org.tombolo.field.LatestValueField",
                "{" +
                        "\"label\": \"f1\"," +
                        "\"attribute\": " +
                        "{" +
                        "  \"providerLabel\": \"default_provider_label\", " +
                        "  \"attributeLabel\": \"f1a_label\"" +
                        "}" +
                        "}");

        FieldSpecification fs2 = new FieldSpecification("uk.org.tombolo.field.LatestValueField",
                "{" +
                        "\"label\": \"f2\"," +
                        "\"attribute\": " +
                        "{" +
                        "  \"providerLabel\": \"default_provider_label\", " +
                        "  \"attributeLabel\": \"f2a_label\"" +
                        "}" +
                        "}");

        fieldSpecificationList.add(fs1);
        fieldSpecificationList.add(fs2);

        subject = TestFactory.makeNamedSubject("E01002766");
        Attribute f1 = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "f1a");
        Attribute f2 = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "f2a");
        TestFactory.makeTimedValue("E01002766", f1, "2011-01-01T00:00:00", 10d);
        TestFactory.makeTimedValue("E01002766", f2, "2011-01-01T00:00:00", 40d);

        field = new FieldValueSumField("FVSF-label", "FVSF-name", fieldSpecificationList);
    }

    @Test
    public void initialize() throws Exception {
        field.initialize();
        assertEquals(2, field.fields.size());

        assertEquals(LatestValueField.class.getName(), field.fields.get(0).getClass().getName());
        assertEquals("f1", field.fields.get(0).getLabel());

        assertEquals(LatestValueField.class.getName(), field.fields.get(1).getClass().getName());
        assertEquals("f2", field.fields.get(1).getLabel());
    }

    @Test
    public void valueForSubject() throws Exception {
        String value = field.valueForSubject(subject);
        assertEquals(50d,Double.valueOf(value),1d);
    }

    @Test
    public void jsonValueForSubject() throws Exception {
        String jsonString = field.jsonValueForSubject(subject).toJSONString();

        System.err.println(jsonString);

        JSONAssert.assertEquals("{" +
                "  FVSF-label: {" +
                "    values: [" +
                "      {" +
                "        value: 50.0" +
                "      }" +
                "    ]" +
                "  }" +
                "}", jsonString, false);
    }

    @Test
    public void getLabel() throws Exception {
        assertEquals("FVSF-label", field.getLabel());
    }

    @Test
    public void getHumanReadableName() throws Exception {
        assertEquals("FVSF-name", field.getHumanReadableName());
    }

}