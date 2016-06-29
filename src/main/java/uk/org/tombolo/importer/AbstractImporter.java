package uk.org.tombolo.importer;

import org.json.simple.parser.ParseException;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.io.IOException;
import java.util.Properties;

public abstract class AbstractImporter implements Importer {
	protected Properties properties = new Properties();
	protected DownloadUtils downloadUtils;
	protected TimedValueUtils timedValueUtils = new TimedValueUtils();

	public AbstractImporter() { }

	public void setDownloadUtils(DownloadUtils downloadUtils){
		this.downloadUtils = downloadUtils;
	}
	public void setTimedValueUtils(TimedValueUtils timedValueUtils){
		this.timedValueUtils = timedValueUtils;
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
		// Get the details for the data source
		Datasource datasource = getDatasource(datasourceId);
		return importDatasource(datasource);
	}

	/**
	 * Loads the given properties resource into the main properties object
	 *
     */
	@Override
	public void configure(Properties properties) {
		this.properties.putAll(properties);
	}

	@Override
	public Properties getConfiguration(){
		return properties;
	}
}
