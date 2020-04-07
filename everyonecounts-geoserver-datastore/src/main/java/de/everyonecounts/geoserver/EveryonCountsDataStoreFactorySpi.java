package de.everyonecounts.geoserver;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.Parameter;
import org.geotools.util.KVP;

public class EveryonCountsDataStoreFactorySpi implements DataStoreFactorySpi {
	private static Logger log = Logger.getLogger(EveryonCountsDataStoreFactorySpi.class.getName());

	/** Optional - uri of the FeatureType's namespace */
	public static final Param NAMESPACEP = new Param("namespace", URI.class, "uri to a the namespace", false, null,
			new KVP(Parameter.LEVEL, "advanced"));
	public static final Param URLP = new Param("url", URL.class,
			"Everyonecounts json url e.g. https://im6qye3mc3.execute-api.eu-central-1.amazonaws.com/prod", false);

	@Override
	public String getDisplayName() {
		return "Everycounts Datastore";
	}

	@Override
	public String getDescription() {
		return "This datastore loads data from the URL https://im6qye3mc3.execute-api.eu-central-1.amazonaws.com/prod fly to JTS features.";
	}

	@Override
	public Param[] getParametersInfo() {
		return new Param[] { NAMESPACEP, URLP };
	}

	@Override
	public boolean canProcess(Map<String, Serializable> params) {
		return !params.containsKey("dbtype");
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	/** No implementation hints required at this time */
	public Map<Key, ?> getImplementationHints() {
		return Collections.emptyMap();
	}

	@Override
	public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
		URI namespace = lookup(NAMESPACEP, params);
		URL url = lookup(URLP, params);
		EveryoneCountsDataStore store = new EveryoneCountsDataStore(url);

		if (namespace != null) {
			store.setNamespaceURI(namespace.toString());
		}
		log.log(Level.INFO, "Creating EveryoneCounts Datastore from {0} and {1}", new Object[] { namespace, url });
		return store;
	}

	/**
	 * Looks up a parameter, if not found it returns the default value, assuming
	 * there is one, or null otherwise
	 *
	 * @param <T>
	 * @param param
	 * @param params
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	<T> T lookup(Param param, Map<String, Serializable> params) throws IOException {
		T result = (T) param.lookUp(params);
		if (result == null) {
			return (T) param.getDefaultValue();
		} else {
			return result;
		}
	}

	@Override
	public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
		throw new UnsupportedOperationException("EveryoneCounts Datastore is read only");
	}
}
