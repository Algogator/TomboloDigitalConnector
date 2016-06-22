package uk.org.tombolo.execution.spec;

public class AttributeMatcher {
    public final String providerLabel;
    public final String attributeLabel;

    public AttributeMatcher(String providerLabel, String attributeLabel) {
        this.providerLabel = providerLabel;
        this.attributeLabel = attributeLabel;
    }
}
