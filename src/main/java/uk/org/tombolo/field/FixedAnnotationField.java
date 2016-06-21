package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;

public class FixedAnnotationField implements SingleValueField {
    private final String label;
    private final String value;

    public FixedAnnotationField(String label, String value) {
        this.label = label;
        this.value = value;
    }

    @Override
    public String valueForSubject(Subject subject) {
        return value;
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) {
        JSONObject obj = new JSONObject();
        obj.put(label, value);
        return obj;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getHumanReadableName() {
        return label;
    }
}
