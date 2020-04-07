package de.everyonecounts.geoserver;

import java.io.IOException;
import java.net.URL;

import org.geotools.feature.NameImpl;
import org.junit.jupiter.api.Test;

class EveryoneCountsFeatureReaderTest {

	@Test
	void testGenerateIteratorFromDataStore() throws IOException {
		EveryoneCountsDataStore everyoneCountsDataStore = new EveryoneCountsDataStore(
				new URL("https://im6qye3mc3.execute-api.eu-central-1.amazonaws.com/prod"));
		
		everyoneCountsDataStore.setNamespaceURI("namespace");

		EveryoneCountsFeatureReader reader = new EveryoneCountsFeatureReader(
				(EveryoneCountsFeatureSource) everyoneCountsDataStore.createFeatureSource(
						everyoneCountsDataStore.getEntry(new NameImpl(null, "EveryoneCounts"))),
				null);
		reader.generateIteratorFromDataStore(everyoneCountsDataStore);
	}

}
