package uk.org.tombolo.field.assertion;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.execution.spec.AttributeMatcher;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns true if a subject has any fixed value for the listed attributes.
 */
public class HasFixedAttributeField extends AbstractField implements SingleValueField {

    private List<AttributeMatcher> attributes;

    private List<Attribute> cachedAttributes;

    public HasFixedAttributeField(String label, List<AttributeMatcher> attributes){
        super(label);
        this.attributes = attributes;
    }

    public void initialise(){
        cachedAttributes = new ArrayList<>();
        for (AttributeMatcher attribute : attributes){
            cachedAttributes.add(AttributeUtils.getByProviderAndLabel(attribute.providerLabel, attribute.attributeLabel));
        }
    }

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        if (cachedAttributes == null)
            initialise();
        for (Attribute cachedAttribute : cachedAttributes) {
            FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subject, cachedAttribute);
            if (fixedValue != null) {
                return "1";
            }
        }
        return "0";
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        JSONObject obj = new JSONObject();
        obj.put("value", valueForSubject(subject));
        return obj;
    }

}
