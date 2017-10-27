package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.recipe.FieldRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * WrapperField.java
 * A field that wraps subfields in a JSON object.
 *
 * Takes a field exactly like the root field does.
 * Can be nested.
 */
public class WrapperField extends AbstractField implements ParentField {
    private static Logger log = LoggerFactory.getLogger(WrapperField.class);
    private final List<FieldRecipe> fields;
    private ArrayList<Field> wrapperFields;

    WrapperField(String label, List<FieldRecipe> fields) {
        super(label);
        this.fields = fields;
    }

    public void initialize() {
        this.wrapperFields = new ArrayList<>();
        for (FieldRecipe fieldSpec : fields) {
            try {
                wrapperFields.add(fieldSpec.toField());
            } catch (ClassNotFoundException e) {
                throw new Error("Field not valid");
            }
        }
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject, Boolean timeStamp) {
        if (null == wrapperFields) { initialize(); }
        JSONObject obj = new JSONObject();
        JSONObject inner = new JSONObject();
        wrapperFields.forEach(field -> {
            try {
                inner.putAll(field.jsonValueForSubject(subject, timeStamp));
            } catch (IncomputableFieldException e) {
                log.warn("Could not compute Field {} for Subject {}, reason: {}", field.getLabel(), subject.getLabel(), e.getMessage());
            }
        });
        obj.put(label, inner);
        return obj;
    }

    @Override
    public List<Field> getChildFields() {
        if (wrapperFields == null)
            initialize();
        return wrapperFields;
    }
}
