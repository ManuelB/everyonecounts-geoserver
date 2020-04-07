package de.everyonecounts.geoserver;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

class EveryoneCountsDataStoreTest {

	@Test
	void testEveryoneCountsDataStore() {
		Map<String, String> map = EveryoneCountsDataStore.ags2landkreise.get("01001");
		assertEquals("54.7851543293153", map.get("Y"));
		assertEquals("9.43826229387093", map.get("X"));
	}

}
