package de.westnordost.osmapi.overpass;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ElementCountParserTest
{
	@Test public void parseCounts()
	{
		ElementCount count = parse(
				"<count id=\"0\">" +
				"    <tag k=\"nodes\" v=\"2\"/>" +
				"    <tag k=\"ways\" v=\"3\"/>" +
				"    <tag k=\"relations\" v=\"4\"/>" +
				"    <tag k=\"total\" v=\"9\"/>" +
	  			"</count>"
		);
		assertEquals(2, count.nodes);
		assertEquals(3, count.ways);
		assertEquals(4, count.relations);
		assertEquals(9, count.total);
	}

	private ElementCount parse(String xml)
	{
		try
		{
			return new ElementCountParser().parse(TestUtils.asInputStream(xml));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
