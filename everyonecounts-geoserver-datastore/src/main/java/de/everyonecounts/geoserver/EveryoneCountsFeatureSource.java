package de.everyonecounts.geoserver;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.cache.Cache;
import javax.json.JsonObject;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class EveryoneCountsFeatureSource extends ContentFeatureSource {

	private static Logger log = Logger.getLogger(EveryoneCountsFeatureSource.class.getName());

	static Map<String, Class<?>> fieldsEveryoneCounts;

	static {
		fieldsEveryoneCounts = new ConcurrentHashMap<String, Class<?>>();
		fieldsEveryoneCounts.put("hystreet_score", Double.class);
		fieldsEveryoneCounts.put("zug_score", Double.class);
		fieldsEveryoneCounts.put("nationalExpress_score", Double.class);
		fieldsEveryoneCounts.put("regional_score", Double.class);
		fieldsEveryoneCounts.put("suburban_score", Double.class);
		fieldsEveryoneCounts.put("national_score", Double.class);
		fieldsEveryoneCounts.put("bus_score", Double.class);
		fieldsEveryoneCounts.put("tomtom_score", Double.class);
		fieldsEveryoneCounts.put("webcam_score", Double.class);
		fieldsEveryoneCounts.put("bike_score", Double.class);
		fieldsEveryoneCounts.put("gmap_score", Double.class);
		fieldsEveryoneCounts.put("lemgoDigital", Double.class);
		fieldsEveryoneCounts.put("date", Date.class);
	}
	
	static Map<String, Class<?>> fieldsLandkreise;

	static {
		fieldsLandkreise = new ConcurrentHashMap<String, Class<?>>();

		fieldsLandkreise.put("RS", String.class);
		fieldsLandkreise.put("AGS", String.class);
		fieldsLandkreise.put("GEN", String.class);
		fieldsLandkreise.put("BEZ", String.class);
		fieldsLandkreise.put("EWZ", Integer.class);
		fieldsLandkreise.put("Shape_Area", Double.class);
	}

	Cache<String, JsonObject> cache;
	URL url;
	CoordinateReferenceSystem sourceCRS;

	public EveryoneCountsFeatureSource(ContentEntry entry, Query query) {
		super(entry, query);
		try {
			sourceCRS = CRS.decode("EPSG:4326");
		} catch (FactoryException e) {
			log.log(Level.WARNING, "Could not load WGS 84 EPSG:4326", e);
		}
	}

	public EveryoneCountsFeatureSource(URL url, Cache<String, JsonObject> cache, ContentEntry entry, Query all) {
		this(entry, all);
		this.url = url;
		this.cache = cache;

	}

	@Override
	protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
		return new ReferencedEnvelope(sourceCRS);
	}

	@Override
	protected int getCountInternal(Query query) throws IOException {
		return 1;
	}

	@Override
	protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
		return new EveryoneCountsFeatureReader(this, query);
	}
	
	public EveryoneCountsDataStore getDataStore() {
		return (EveryoneCountsDataStore) entry.getDataStore();
	}

	@Override
	protected SimpleFeatureType buildFeatureType() throws IOException {
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName(entry.getName());
		builder.setCRS(sourceCRS); // <- Coordinate reference system
		builder.add("geom", Point.class);
		
		builder.setDefaultGeometry("geom");

		for(Entry<String, Class<?>> entry : fieldsEveryoneCounts.entrySet()) {	
			builder.add(entry.getKey(), entry.getValue());
		}
		
		for(Entry<String, Class<?>> entry : fieldsLandkreise.entrySet()) {	
			builder.add(entry.getKey(), entry.getValue());
		}

		return builder.buildFeatureType();
	}

	@Override
	protected boolean canFilter() {
		return true;
	}

	@Override
	protected boolean canLimit() {
		return true;
	}

	@Override
	protected boolean canReproject() {
		return true;
	}

	@Override
	protected boolean canRetype() {
		return false;
	}

	@Override
	protected boolean canSort() {
		return true;
	}

	@Override
	protected boolean canTransact() {
		return true;
	}

}
