package uk.org.tombolo.importer;

import org.json.simple.parser.ParseException;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.utils.ImportCacheMarkerUtils;

import java.io.IOException;
import java.util.Properties;

public abstract class AbstractImporter implements Importer {
	protected Properties properties = new Properties();
	protected DownloadUtils downloadUtils;

	public AbstractImporter() { }

	public void setDownloadUtils(DownloadUtils downloadUtils){
		this.downloadUtils = downloadUtils;
	}
	
	/**
	 * Loads the data-source identified by datasourceId into the underlying data store 
	 * 
	 * @param datasourceId
	 * @return the number of data values loaded
	 * @throws IOException
	 * @throws ParseException 
	 */
	public int importDatasource(String datasourceId) throws Exception {
		if (ImportCacheMarkerUtils.isCached(getCacheKeyForDatasourceId(datasourceId))) { return 0; }
		// Get the details for the data source
		Datasource datasource = getDatasource(datasourceId);
		int count = importDatasource(datasource);
		ImportCacheMarkerUtils.markCached(getCacheKeyForDatasourceId(datasourceId));
		return count;
	}

	protected abstract String getCacheKeyForDatasourceId(String datasourceId);

	protected abstract int importDatasource(Datasource datasource) throws Exception;

	/**
	 * Loads the given properties resource into the main properties object
	 *
     */
	@Override
	public void configure(Properties properties) throws ConfigurationException {
		this.properties.putAll(properties);
		verifyConfiguration();
	}

	@Override
	public void verifyConfiguration() throws ConfigurationException {
		// Do nothing by default
		// Importers that need configuration will override this
	}

	@Override
	public Properties getConfiguration(){
		return properties;
	}
}
