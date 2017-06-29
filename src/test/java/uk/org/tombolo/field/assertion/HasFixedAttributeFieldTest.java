package uk.org.tombolo.field.assertion;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.execution.spec.AttributeMatcher;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HasFixedAttributeFieldTest extends AbstractTest {
    private static final String ATTRIBUTE_LABEL = "tobecounted";

    @Test
    public void valueForSubject() throws Exception {
        // Create dummy subjects
        Subject subjectWithAttributeAndOneValueMatch = TestFactory.makeNamedSubject("E01000001");
        Subject subjectWithAttribtueAndTwoValueMatches = TestFactory.makeNamedSubject("E09000019");
        Subject subjectWithAttributeButOtherValue = TestFactory.makeNamedSubject("E09000001");
        Subject subjectWithoutAttribute = TestFactory.makeNamedSubject("E01000002");

        // Crate dummy attribute
        Attribute testAttribute = new Attribute(TestFactory.DEFAULT_PROVIDER,ATTRIBUTE_LABEL, "", "", Attribute.DataType.string);
        AttributeUtils.save(testAttribute);

        // Assign attribute values
        FixedValueUtils.save(new FixedValue(subjectWithAttributeAndOneValueMatch, testAttribute, "value1"));
        FixedValueUtils.save(new FixedValue(subjectWithAttribtueAndTwoValueMatches, testAttribute, "value1"));
        FixedValueUtils.save(new FixedValue(subjectWithAttribtueAndTwoValueMatches, testAttribute, "value2"));
        FixedValueUtils.save(new FixedValue(subjectWithAttributeButOtherValue, testAttribute, "value3"));

        // Create field
        AttributeMatcher attributeMatcher = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), ATTRIBUTE_LABEL);
        List<String> testValues = Arrays.asList("value1", "value2");
        HasFixedAttributeField field = new HasFixedAttributeField("blafield", attributeMatcher, testValues);

        // Test
        assertEquals("1",field.valueForSubject(subjectWithAttributeAndOneValueMatch));
        assertEquals("1", field.valueForSubject(subjectWithAttribtueAndTwoValueMatches));
        assertEquals("0", field.valueForSubject(subjectWithAttributeButOtherValue));
        assertEquals("0", field.valueForSubject(subjectWithoutAttribute));
    }

}