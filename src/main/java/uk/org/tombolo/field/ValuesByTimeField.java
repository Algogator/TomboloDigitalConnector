package uk.org.tombolo.field;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.execution.spec.AttributeMatcher;

import java.util.stream.Collectors;

/**
 * ValuesByTimeField.java
 * Returns all TimedValues on an Attribute for a given Subject, plus metadata.
 *
 * The metadata is regarding the attribute.
 */
public class ValuesByTimeField implements Field, FieldWithProvider {
    protected String label;
    private AttributeMatcher attribute;
    private Attribute cachedAttribute;

    public ValuesByTimeField(String label, AttributeMatcher attribute) {
        this.label = label;
        this.attribute = attribute;
    }

    public JSONObject jsonValueForSubject(Subject subject) {
        JSONArray arr = new JSONArray();
        arr.addAll(TimedValueUtils.getBySubjectAndAttribute(subject, getAttribute()).stream().map(timedValue -> {
            JSONObject pair = new JSONObject();
            pair.put("timestamp", timedValue.getId().getTimestamp().toString());
            pair.put("value", timedValue.getValue());
            return pair;
        }).collect(Collectors.toList()));
        return withinMetadata(arr);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getHumanReadableName() {
        return getAttribute().getName();
    }

    protected Attribute getAttribute() {
        if (null != cachedAttribute) return cachedAttribute;

        Attribute attr = AttributeUtils.getByProviderAndLabel(attribute.providerLabel, attribute.attributeLabel);
        if (null == attr) {
            throw new IllegalArgumentException(String.format("No attribute found for provider %s and label %s", attribute.providerLabel, attribute.attributeLabel));
        } else {
            cachedAttribute = attr;
            return attr;
        }
    }

    protected JSONObject withinMetadata(JSONAware contents) {
        JSONObject attr = new JSONObject();
        attr.put("name", getHumanReadableName());
        attr.put("provider", getProvider().getName());
        attr.put("values", contents);
        JSONObject obj = new JSONObject();
        obj.put(label, attr);
        return obj;
    }

    @Override
    public Provider getProvider() {
        return getAttribute().getProvider();
    }
}
