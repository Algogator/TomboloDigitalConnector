package uk.org.tombolo.importer.ons;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.utils.HibernateUtil;
import uk.org.tombolo.datacatalogue.DatasourceDetails;
import uk.org.tombolo.datacatalogue.DatasourceSpecification;
import uk.org.tombolo.importer.DownloadUtils;
import uk.org.tombolo.importer.Importer;

/**
 * A class for importing ONS Census data into the Tombolo Digital Connector platform.
 * 
 * All Census datasets
 * http://data.ons.gov.uk/ons/api/data/collections.xml?apikey=kil61db9uf&context=Census&firstRecord=1&noOfRecords=5
 * 
 * Get all Hierarchies for a dataset
 * http://data.ons.gov.uk/ons/api/data/hierarchies/OT102EW.json?context=Census&apikey=kil61db9uf
 *
 * Get description of a dataset
 * http://data.ons.gov.uk/ons/api/data/datasetdetails/OT102EW.json?context=Census&apikey=kil61db9uf&geog=2011STATH
 * 
 * Census population for LSOAs
 * http://data.ons.gov.uk/ons/api/data/dataset/OT102EW/set.json?context=Census&apikey=kil61db9uf&geog=2011STATH&startobs=1&noobs=10
 * 
 */
public class ONSCensusImporter extends AbstractONSImporter implements Importer{
	private static final String ONS_API_URL = "http://data.ons.gov.uk/ons/api/data/";
	private static final String ONS_API_KEY = "kil61db9uf";	//FIXME: This should not be in the source code!!!
	
	private static final String ONS_LANGUAGE_ATTRIBUTE_KEY = "@xml.lang";
	private static final String ONS_LANGUAGE_ATTRIBUTE_VALUE_EN = "en";
	private static final String ONS_ATTRIBUTE_VALUE_KEY = "$";
	
	private static final String baseUrl = "http://data.statistics.gov.uk/ons/datasets/";
	private static final String filePrefix = "csv/CSV_";
	private static final String filePostfix = "_2011STATH_1_EN.zip";

	DownloadUtils downloadUtils = new DownloadUtils();
	
	public List<DatasourceDetails> getAllDatasources() throws IOException, ParseException{
		List<DatasourceDetails> datasources = new ArrayList<DatasourceDetails>();
	
		String baseUrl = ONS_API_URL + "collections.json?";
		Map<String,String> params = new HashMap<String,String>();
		params.put("apikey", ONS_API_KEY);
		params.put("context", "Census");		
		String paramsString = DownloadUtils.paramsToString(params);
		URL url = new URL(baseUrl + paramsString);
		
		// Parse the json content
		JSONParser parser = new JSONParser();
		URLConnection connection = url.openConnection();
		connection.setRequestProperty("Accept", "application/json");
		
		JSONObject rootObject = (JSONObject) parser.parse(new InputStreamReader(connection.getInputStream()));		
		JSONObject ons = (JSONObject)rootObject.get("ons");

		JSONArray collections = (JSONArray)((JSONObject)ons.get("collectionList")).get("collection");
		for (int i=0; i<collections.size(); i++){
			JSONObject collection = (JSONObject)collections.get(i);
			String datasetId = (String)collection.get("id");
			JSONArray names = (JSONArray)((JSONObject)collection.get("names")).get("name");
			String datasetDescription = getEnglishValue(names);
			
			DatasourceDetails datasourceDetails = new DatasourceDetails(getProvider(),datasetId,datasetDescription);
			datasources.add(datasourceDetails);
		}
		
		return datasources;
	}
	
	/**
	 * Loads the dataset identified by datasetId into the underlying data store 
	 * 
	 * @param datasetId
	 * @return the number of data values loaded
	 * @throws IOException
	 * @throws ParseException 
	 */
	public int loadDataset(String datasetId) throws IOException, ParseException{
		
		// Get the provider details
		Provider provider = getProvider();
		
		// Store the provider
		saveProvider(provider);

		// Get the details for the data source
		DatasourceDetails details = getDatasetDetails(datasetId);

		// Store attributes in database
		saveAttributes(details.getAttributes());
		
		DatasourceSpecification datasource = new DatasourceSpecification(
				datasetId,	getProvider().getLabel(),						// Dataset id and provider
				"http://www.ons.gov.uk/ons/datasets-and-tables/index.html",	// Dataset location (description)
				baseUrl + filePrefix + datasetId + filePostfix,				// Remote file
				filePrefix + datasetId + filePostfix,						// Local file (relative to local data root)
				DatasourceSpecification.DatafileType.zip);					// File type
				
		// Store data in database
		File localFile = downloadUtils.getDatasourceFile(datasource);
		ZipFile zipFile = new ZipFile(localFile);
		ZipArchiveEntry zipEntry = null;
		Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
		while(entries.hasMoreElements()){
			zipEntry = entries.nextElement();

			if (zipEntry.getName().equals("CSV_"+datasetId+"_2011STATH_1_EN.csv")){
			
				BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)));
				
				int lineCount = 0;
				String line = null;
				while ((line = br.readLine()) != null){
					lineCount++;

					String[] fields = line.split(",");
					
					if (fields.length == 2 + details.getAttributes().size()
							&& fields[0].startsWith("\"")){
						try{
							String areaId = dequote(fields[0]);
							List<Double> values = new ArrayList<Double>();
							for (int i=2; i<2+details.getAttributes().size(); i++){
								values.add(Double.parseDouble(dequote(fields[i])));
							}
							System.err.println(line);
							System.err.print(areaId);
							for (int i=0; i<values.size(); i++){
								System.err.print("\t"+values.get(i));
							}
							System.err.println();
							
							// FIXME: Update TimedValue table
							
						}catch (NumberFormatException e){
							// Ignoring this line since it does not contain numeric values for the attributes
						}
					}
					if (lineCount > 15)
						break;
				}
				br.close();
			}
			
		}
		zipFile.close();		
		
		return -1;
	}
	
	public DatasourceDetails getDatasetDetails(String datasetId) throws IOException, ParseException{
		// Set-up the basic url and the parameters
		String baseUrl = ONS_API_URL + "datasetdetails/" + datasetId + ".json?";
		Map<String,String> params = new HashMap<String,String>();
		params.put("context", "Census");
		params.put("apikey", ONS_API_KEY);
		params.put("geog", "2011STATH");
		String paramsString = DownloadUtils.paramsToString(params);
		URL url = new URL(baseUrl + paramsString);
		
		// Parse the json content
		JSONParser parser = new JSONParser();
		URLConnection connection = url.openConnection();
		connection.setRequestProperty("Accept", "application/json");
		
		JSONObject rootObject = (JSONObject) parser.parse(new InputStreamReader(connection.getInputStream()));
		
		JSONObject ons = (JSONObject)rootObject.get("ons");
		JSONObject datasetDetail = (JSONObject)ons.get("datasetDetail");
		
		// Get dataset English name
		JSONArray names = (JSONArray)((JSONObject)datasetDetail.get("names")).get("name");
		String datasetDescription = getEnglishValue(names);
		DatasourceDetails datasourceDetails = new DatasourceDetails(getProvider(),datasetId,datasetDescription);
		
		// Get dataset dimensions
		JSONArray dimensions = (JSONArray)((JSONObject)datasetDetail.get("dimensions")).get("dimension");
		for (int dimIndex = 0; dimIndex<dimensions.size(); dimIndex++){
			JSONObject dimension = (JSONObject)dimensions.get(dimIndex);
			String dimensionType = (String)dimension.get("dimensionType");
			if (dimensionType.equals("Topic")){
				// The dimension is of type "Topic", i.e. an attribute in our terminology
				String attributeName = (String)dimension.get("dimensionId");
				JSONArray dimensionTitles = (JSONArray)((JSONObject)dimension.get("dimensionTitles")).get("dimensionTitle");
				String attributeDescription = getEnglishValue(dimensionTitles);
				// FIXME: The Attribute.DataType value should be gotten from somewhere rather than defaulting to numeric
				Attribute attribute = new Attribute(getProvider(), attributeName, attributeDescription, attributeDescription, Attribute.DataType.numeric);
				datasourceDetails.addAttribute(attribute);
			}
		}
		
		return datasourceDetails;
	}
	
	protected void saveProvider(Provider provider){
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		// FIXME: This might be inefficient if we are updating the provider over and over again without actually changing it
		session.saveOrUpdate(provider);
		session.getTransaction().commit();
	}
	
	protected void saveAttributes(List<Attribute> attributes){
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		for (Attribute attribute : attributes){
			// FIXME: This might be inefficient if we are updating the attribute over and over again without actually changing it			
			Criteria criteria = session.createCriteria(Attribute.class);
			Map<String,Object> restrictions = new HashMap<String,Object>();
			restrictions.put("provider", attribute.getProvider());
			restrictions.put("label", attribute.getLabel());
			Attribute savedAttribute = (Attribute)criteria.add(Restrictions.allEq(restrictions)).uniqueResult();
			if (savedAttribute == null)
				session.saveOrUpdate(attribute);
		}
		session.getTransaction().commit();
	}
	
	private static String getEnglishValue(JSONArray array){
		for (int index = 0; index<array.size(); index++){
			JSONObject name = (JSONObject)array.get(index);
			if (name.get(ONS_LANGUAGE_ATTRIBUTE_KEY).equals(ONS_LANGUAGE_ATTRIBUTE_VALUE_EN)){
				return (String)name.get(ONS_ATTRIBUTE_VALUE_KEY);
			}
		}
		return null;
	}
	
	private String dequote(String string){
		if (!string.startsWith("\""))
			return string;
		if (!string.endsWith("\""))
			return string;
		return string.substring(1, string.length()-1);
	}
}
