/*
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs.loaders;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;


/**
 * Unit tests for the {@code EnumLoader} class.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class EnumLoaderTest {

	@Test
	void testLoad() {

		EnumLoader<TimeUnit> loader = new EnumLoader<>(TimeUnit.YEAR);

		TimeUnit expected = TimeUnit.WEEK;
		Properties props = new Properties();
		props.put("timeUnit", expected.name());
		TimeUnit actual = loader.load("timeUnit", props.getProperty("timeUnit"), props);

		Assertions.assertEquals(expected, actual);
	}

	@Test
	void testSave() {

		EnumLoader<TimeUnit> loader = new EnumLoader<>(TimeUnit.YEAR);

		TimeUnit expected = TimeUnit.WEEK;
		Properties props = new Properties();
		String actual = loader.save("timeUnit", expected, props);

		Assertions.assertEquals(expected.name(), actual);
	}

	private enum TimeUnit {
		DAY, WEEK, MONTH, YEAR
	}
}
