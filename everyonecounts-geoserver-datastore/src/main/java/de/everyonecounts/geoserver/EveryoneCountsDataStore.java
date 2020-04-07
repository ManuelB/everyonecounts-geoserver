package de.everyonecounts.geoserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.TouchedExpiryPolicy;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.ClientBuilder;

import org.geotools.data.Query;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

public class EveryoneCountsDataStore extends ContentDataStore {
	private static Logger log = Logger.getLogger(EveryoneCountsDataStore.class.getName());

	Cache<String, JsonObject> cache;
	URL url;
	static Map<String, Map<String, String>> ags2landkreise = new ConcurrentHashMap<String, Map<String, String>>();

	static {
		try (InputStream is = EveryoneCountsDataStore.class.getClassLoader().getResourceAsStream("Landkreise.csv");
				Scanner s = new Scanner(is)) {
			String header = s.nextLine();
			String[] fieldNames = header.split(",");
			log.info("Header from CSV: " + header);
			while (s.hasNextLine()) {
				String line = s.nextLine();
				String[] values = line.split(",");
				int i = 0;
				Map<String, String> valueMap = new ConcurrentHashMap<String, String>();
				for (String fieldName : fieldNames) {
					valueMap.put(fieldName, values[i]);
					i++;
				}
				if (ags2landkreise.containsKey(valueMap.get("AGS"))) {
					log.warning(valueMap.get("AGS") + " is more than once in Landkreise.csv");
				} else {
					ags2landkreise.put(valueMap.get("AGS"), valueMap);
				}
			}

		} catch (IOException e) {
			log.log(Level.SEVERE, "Problem reading Landkreise.csv", e);
		}

	}

	public EveryoneCountsDataStore(URL url) {
		cache = (Cache<String, JsonObject>) ((Cache<?, ?>) getCache());
		this.url = url;
		try {
			// make sure that the entries are loaded
			entry(new NameImpl("", ""));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Get a global cache object that can map objects, to objects. Other types might
	 * trigger: Caused by: java.lang.ClassCastException: ISPN021011: Incompatible
	 * cache value types specified, expected class java.lang.String but class
	 * java.lang.Object was specified
	 * 
	 * @return
	 */
	public Cache<Object, Object> getCache() {
		try {
			// Retrieve the system wide cache manager
			CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
			// Define a named cache with default JCache configuration
			Cache<Object, Object> cache = cacheManager.getCache("global", Object.class, Object.class);

			Duration cacheExpiry = new Duration(TimeUnit.MINUTES, 15);

			MutableConfiguration<Object, Object> config = new MutableConfiguration<>().setStoreByValue(false)
					.setStatisticsEnabled(true).setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(cacheExpiry));
			if (cache == null) {
				cache = cacheManager.createCache("global", config);
			}
			log.fine("Create cache global");
			return cache;
		} catch (CacheException ex) {
			log.log(Level.WARNING, "Was not able to create cache", ex);
			return null;
		}
	}

	@Override
	protected List<Name> createTypeNames() throws IOException {
		return Arrays.asList(name("EveryoneCounts"));
	}

	@Override
	protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
		return new EveryoneCountsFeatureSource(url, cache, entry, Query.ALL);
	}

	JsonObject getEveryoneCountsJsonObject() {
		String json = ClientBuilder.newClient().target(url.toString()).request().get(String.class);
		return Json.createReader(new StringReader(json.replaceAll("NaN", "null"))).readObject();
	}

	public JsonObject getCachedEveryoneCountsJsonObject() {
		JsonObject jsonObject = cache == null ? null : cache.get("everyonecounts");
		if (jsonObject == null) {
			jsonObject = getEveryoneCountsJsonObject();
			if(cache != null) {
				cache.put("everyonecounts", jsonObject);
			}
		}
		return jsonObject;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public void setCache(Cache<String, JsonObject> cache) {
		this.cache = cache;
	}
}
