package de.westnordost.osmapi.overpass;

import org.junit.Test;

import java.util.List;

import de.westnordost.osmapi.common.ListHandler;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CsvParserTest
{
	@Test public void parseTable() {
		String table =
				"id	name	phone\n" +
				"123	Rumpelstielzchen	0405642\n" +
				"345	Almanach	0407946\n";

		List<String[]> result = parse(table, "\t");
		assertEquals(3, result.size());
		assertArrayEquals(new String[] {"id", "name", "phone"}, result.get(0));
		assertArrayEquals(new String[] {"123", "Rumpelstielzchen", "0405642"}, result.get(1));
		assertArrayEquals(new String[] {"345", "Almanach", "0407946"}, result.get(2));
	}

	@Test public void parseTableWithDifferentSeparator() {
		String table = "id,name,phone\n";
		List<String[]> result = parse(table, ",");
		assertEquals(1, result.size());
		assertArrayEquals(new String[] {"id", "name", "phone"}, result.get(0));
	}

	private List<String[]> parse(String table, String separator)
	{
		ListHandler<String[]> handler = new ListHandler<>();

		try
		{
			new CsvParser(handler, separator).parse(TestUtils.asInputStream(table));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return handler.get();
	}
}
