package uk.org.tombolo.importer.tfl;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.Importer;

public abstract class TfLImporter implements Importer {

	// FIXME: This should be loaded from a configuration file but not in source code
	protected static final String API_APP_ID = "c9d407ae";
	protected static final String API_APP_KEY = "f33aa676ebb4518fe9ffd972d263daff";

	public static final Provider PROVIDER = new Provider(
			"uk.gov.tfl",
			"Transport for London"
			);

	@Override
	public Provider getProvider() {
		return PROVIDER;
	}

	
}
