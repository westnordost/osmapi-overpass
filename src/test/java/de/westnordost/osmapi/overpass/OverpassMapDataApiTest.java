package de.westnordost.osmapi.overpass;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.common.ListHandler;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.osmapi.map.handler.MapDataHandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OverpassMapDataApiTest
{
	// a piece of central Hamburg
	private static String BBOX = "[bbox:53.548,9.992,53.551,9.995];";

	@Test public void queryCount()
	{
		ElementCount count = createApi().queryCount(BBOX + "way[building];out count;");
		// cause we are querying for ways only
		assertEquals(0, count.nodes);
		assertEquals(0, count.relations);
		assertNotEquals(0, count.ways);
		assertEquals(count.total, count.ways);
	}

	@Test public void queryTable()
	{
		ListHandler<String[]> handler = new ListHandler<>();
		createApi().queryTable("[out:csv(name, highway)]" + BBOX + "way[highway][name];out;", handler);
		List<String[]> table = handler.get();
		assertNotEquals(0, table.size());
		Set<String> names = new HashSet<>();
		for (String[] row : table)
		{
			assertEquals(2, row.length);
			names.add(row[0]);
		}
		assertTrue(names.contains("Rathausmarkt"));
		assertTrue(names.contains("Alter Wall"));
		assertTrue(names.contains("Dornbusch"));
	}

	@Test public void queryElements()
	{
		final int[] wayCount = {0};
		createApi().queryElements(BBOX + "way[name=\"Alter Wall\"];out meta;", new MapDataHandler()
		{
			@Override public void handle(BoundingBox bounds) {}
			@Override public void handle(Node node)
			{
				fail("There should not be any nodes in the result");
			}
			@Override public void handle(Relation relation)
			{
				fail("There should not be any relations in the result");
			}

			@Override public void handle(Way way)
			{
				assertEquals("Alter Wall", way.getTags().get("name"));
				wayCount[0]++;
			}
		});
		assertTrue(wayCount[0] > 0);
	}

	@Test public void queryElementsWithGeometry()
	{
		createApi().queryElementsWithGeometry(BBOX + "nwr[building];out meta geom;", new MapDataWithGeometryHandler()
		{
			@Override public void handle(@NotNull BoundingBox bounds)
			{
				assertNotNull(bounds);
			}

			@Override public void handle(@NotNull Node node)
			{
				assertNotNull(node);
			}

			@Override public void handle(
					@NotNull Way way, @NotNull BoundingBox bounds, @NotNull List<LatLon> geometry)
			{
				assertNotNull(way);
				assertNotNull(bounds);
				assertNotNull(geometry);
				assertEquals(way.getNodeIds().size(), geometry.size());
			}

			@Override public void handle(
					@NotNull Relation relation,
					@NotNull BoundingBox bounds,
					@NotNull Map<Long, LatLon> nodeGeometries,
					@NotNull Map<Long, List<LatLon>> wayGeometries)
			{
				assertNotNull(relation);
				assertNotNull(bounds);
				assertNotNull(nodeGeometries);
				assertNotNull(wayGeometries);
				for (RelationMember member : relation.getMembers())
				{
					if (member.getType() == Element.Type.NODE)
					{
						LatLon geometry = nodeGeometries.get(member.getRef());
						assertNotNull(geometry);
					}
					if (member.getType() == Element.Type.WAY)
					{
						List<LatLon> geometry = wayGeometries.get(member.getRef());
						assertNotNull(geometry);
					}
				}
			}
		});
	}

	private OverpassMapDataApi createApi()
	{
		OsmConnection osm = new OsmConnection("https://lz4.overpass-api.de/api/","osmapi unit test", null);
		return new OverpassMapDataApi(osm);
	}
}
