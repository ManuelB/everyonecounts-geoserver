package de.everyonecounts.geoserver;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class EveryoneCountsFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

	private static Logger log = Logger.getLogger(EveryoneCountsFeatureReader.class.getName());
	Iterator<SimpleFeature> iterator;
	private ContentFeatureSource everyoneCountsFeatureSource;

	GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

	public EveryoneCountsFeatureReader(EveryoneCountsFeatureSource everyoneCountsFeatureSource, Query query) {
		this.everyoneCountsFeatureSource = everyoneCountsFeatureSource;
		EveryoneCountsDataStore dataStore = everyoneCountsFeatureSource.getDataStore();

		iterator = generateIteratorFromDataStore(dataStore);
	}

	Iterator<SimpleFeature> generateIteratorFromDataStore(EveryoneCountsDataStore dataStore) {

		List<SimpleFeature> list = new CopyOnWriteArrayList<SimpleFeature>();

		JsonObject jsonObject = dataStore.getCachedEveryoneCountsJsonObject();
		JsonObject date2landkreise = jsonObject.getJsonObject("body");
		for (Entry<String, JsonValue> date2landkreis : date2landkreise.entrySet()) {
			try {
				for (Entry<String, JsonValue> landkreis2values : ((JsonObject) date2landkreis.getValue()).entrySet()) {
					synchronized (SimpleFeatureBuilder.class) {
						String ags = landkreis2values.getKey();
						JsonObject values = (JsonObject) landkreis2values.getValue();
						Map<String, String> landkreis = EveryoneCountsDataStore.ags2landkreise.get(ags);

						if (landkreis == null) {
							log.fine("Did not find: " + ags + " in landkreis");
							continue;
						}

						SimpleFeatureBuilder builder = new SimpleFeatureBuilder(getFeatureType());
						Point p = geometryFactory.createPoint(new Coordinate(Double.parseDouble(landkreis.get("X")),
								Double.parseDouble(landkreis.get("Y"))));
						builder.set("geom", p);

						for (String key : EveryoneCountsFeatureSource.fieldsLandkreise.keySet()) {
							builder.set(key, landkreis.get(key));
						}

						for (Entry<String, Class<?>> entry : EveryoneCountsFeatureSource.fieldsEveryoneCounts
								.entrySet()) {
							String key = entry.getKey();
							if (key.equals("date")) {
								LocalDate dateToConvert = LocalDate.parse(values.getJsonString(key).getString());
								Date date = Date
										.from(dateToConvert.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
								builder.set(key, date);
							} else {
								try {
									if (values.containsKey(key) && !values.isNull(key)) {
										builder.set(key, values.getJsonNumber(key).doubleValue());
									}
								} catch (Exception ex) {
									log.warning("Problem reading key: " + key + " for time: " + date2landkreis.getKey()
											+ " and AGS: " + landkreis2values.getKey());
								}
							}
						}
						String objectId = null;

						list.add(builder.buildFeature(objectId));
					}
				}
			} catch (Exception e) {
				log.log(Level.WARNING, "Problem during generating features", e);
			}
		}

		return list.iterator();
	}

	@Override
	public SimpleFeatureType getFeatureType() {
		return everyoneCountsFeatureSource.getSchema();
	}

	@Override
	public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
		return iterator.next();
	}

	@Override
	public boolean hasNext() throws IOException {
		return iterator.hasNext();
	}

	@Override
	public void close() throws IOException {

	}

}
