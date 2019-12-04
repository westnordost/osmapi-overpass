package de.westnordost.osmapi.overpass;

import org.junit.Test;

import static org.junit.Assert.*;

public class OverpassStatusParserTest
{
	@Test public void parseRateLimit()
	{
		assertEquals(2, parse("Rate limit: 2").maxAvailableSlots);
	}

	@Test public void parseAvailableSlots()
	{
		assertEquals(33, parse("33 slots available now.").availableSlots);
	}

	@Test public void parseNoAvailableSlots()
	{
		Integer nextAvailableSlotIn = parse("Slot available after: 2016-11-20T18:08:05Z, in 25 seconds.").nextAvailableSlotIn;
		assertNotNull(nextAvailableSlotIn);
		assertEquals(25, (int) nextAvailableSlotIn);
	}

	@Test public void parseNoAvailableSlotsMultiple()
	{
		Integer nextAvailableSlotIn = parse(
				"Slot available after: 2016-11-20T18:08:05Z, in 25 seconds.\n" +
						"Slot available after: 2016-11-20T20:08:05Z, in 564 seconds.\n").nextAvailableSlotIn;
		assertEquals(25, (int) nextAvailableSlotIn);
	}

	private OverpassStatus parse(String xml)
	{
		try
		{
			return new OverpassStatusParser().parse(TestUtils.asInputStream(xml));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
