package de.westnordost.osmapi.overpass;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import de.westnordost.osmapi.map.OsmMapDataFactory;
import de.westnordost.osmapi.map.data.*;

import static org.junit.Assert.*;

public class MapDataWithGeometryParserTest
{
	@Test public void bounds()
	{
		String xml = "<bounds minlat='53.550' minlon='9.994' maxlat='53.551' maxlon='9.995'/>";
		assertEquals(new BoundingBox(53.550, 9.994, 53.551, 9.995), parse(xml).bounds);
	}

	@Test public void node()
	{
		String xml = "<node id='5' lat='51.7463194' lon='0.2428181'/>";
		Node node = parse(xml).nodes.get(0);

		LatLon pos = new OsmLatLon(51.7463194, 0.2428181);
		assertEquals(pos, node.getPosition());
		assertEquals(5, node.getId());
		assertEquals(-1, node.getVersion());
		assertNull(node.getTags());
	}

	@Test public void parsesOptionalNodeAttributes()
	{
		String xml = "<node id='5' version='3' lat='0' lon='0' timestamp='2019-03-28T21:43:50Z'/>";
		Node node = parse(xml).nodes.get(0);

		assertEquals(3, node.getVersion());

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
		c.set(2019, Calendar.MARCH, 28, 21, 43, 50);
		assertEquals(c.getTimeInMillis() / 1000, node.getDateEdited().getTime() / 1000);
	}

	@Test public void parsesNodeTags()
	{
		String xml = "<node id='5' lat='0' lon='0'><tag k='a' v='b'/><tag k='c' v='d'/></node>";
		Node node = parse(xml).nodes.get(0);

		assertNotNull(node.getTags());
		assertEquals("b", node.getTags().get("a"));
		assertEquals("d", node.getTags().get("c"));
	}

	@Test public void way()
	{
		String xml =
				"<way id='5'>" +
				"    <bounds minlat='53.550' minlon='9.994' maxlat='53.551' maxlon='9.995'/> " +
				"    <nd ref='2' lat='1.0' lon='2.0' />" +
				"    <nd ref='3' lat='3.0' lon='4.0' />" +
				"</way>";

		WayWithGeometry wayWithGeometry = parse(xml).waysWithGeometry.get(0);
		Way way = wayWithGeometry.way;

		assertEquals(5, way.getId());
		assertEquals(-1, way.getVersion());
		assertNull(way.getTags());
		assertEquals(Arrays.asList(2L,3L), way.getNodeIds());

		List<LatLon> ps = Arrays.<LatLon>asList(new OsmLatLon(1.0, 2.0), new OsmLatLon(3.0, 4.0));
		assertEquals(ps, wayWithGeometry.geometry);

		assertEquals(new BoundingBox(53.550, 9.994, 53.551, 9.995), wayWithGeometry.bounds);
	}

	@Test public void parsesOptionalWayAttributes()
	{
		String xml =
				"<way id='5' version='3' timestamp='2019-03-28T21:43:50Z'>" +
				"    <bounds minlat='53.550' minlon='9.994' maxlat='53.551' maxlon='9.995'/> " +
				"    <nd ref='2' lat='1.0' lon='2.0' />" +
				"    <nd ref='3' lat='3.0' lon='4.0' />" +
				"</way>";
		Way way = parse(xml).waysWithGeometry.get(0).way;

		assertEquals(3, way.getVersion());

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
		c.set(2019, Calendar.MARCH, 28, 21, 43, 50);
		assertEquals(c.getTimeInMillis() / 1000, way.getDateEdited().getTime() / 1000);
	}

	@Test public void parsesWayTags()
	{
		String xml =
				"<way id='5' version='3' timestamp='2019-03-28T21:43:50Z'>" +
				"    <bounds minlat='53.550' minlon='9.994' maxlat='53.551' maxlon='9.995'/> " +
				"    <nd ref='2' lat='1.0' lon='2.0' />" +
				"    <nd ref='3' lat='3.0' lon='4.0' />" +
				"    <tag k='a' v='b'/>" +
				"    <tag k='c' v='d'/>" +
				"</way>";
		Way way = parse(xml).waysWithGeometry.get(0).way;

		assertNotNull(way.getTags());
		assertEquals("b", way.getTags().get("a"));
		assertEquals("d", way.getTags().get("c"));
	}

	@Test public void relation() {
		String xml =
				"<relation id='10'>" +
				"    <bounds minlat='53.550' minlon='9.994' maxlat='53.551' maxlon='9.995'/> " +
				"    <member type='relation' ref='4' role=''/>" +
				"    <member type='way' ref='1' role='outer'>" +
				"        <nd lat='1.0' lon='2.0'/>" +
				"        <nd lat='3.0' lon='4.0'/>" +
				"    </member>" +
				"    <member type='way' ref='2' role='inner'>" +
				"        <nd lat='5.0' lon='6.0'/>" +
				"        <nd lat='7.0' lon='8.0'/>" +
				"    </member>" +
				"    <member type='node' ref='3' role='point' lat='9.0' lon='10.0'/>" +
				"</relation>";

		RelationWithGeometry relationWithGeometry = parse(xml).relationsWithGeometry.get(0);
		Relation relation = relationWithGeometry.relation;

		assertEquals(10, relation.getId());
		assertEquals(-1, relation.getVersion());
		assertNull(relation.getTags());
		assertEquals(Arrays.asList(
			new OsmRelationMember(4, "", Element.Type.RELATION),
			new OsmRelationMember(1, "outer", Element.Type.WAY),
			new OsmRelationMember(2, "inner", Element.Type.WAY),
			new OsmRelationMember(3, "point", Element.Type.NODE)
		), relation.getMembers());

		Map<Long, List<LatLon>> wayGeom = relationWithGeometry.wayGeometries;
		assertEquals(Arrays.asList(new OsmLatLon(1.0, 2.0), new OsmLatLon(3.0, 4.0)), wayGeom.get(1L));
		assertEquals(Arrays.asList(new OsmLatLon(5.0, 6.0), new OsmLatLon(7.0, 8.0)), wayGeom.get(2L));

		assertEquals(new OsmLatLon(9.0, 10.0), relationWithGeometry.nodeGeometries.get(3L));

		assertEquals(new BoundingBox(53.550, 9.994, 53.551, 9.995), relationWithGeometry.bounds);
	}

	@Test public void parsesOptionalRelationAttributes()
	{
		String xml =
				"<relation id='5' version='3' timestamp='2019-03-28T21:43:50Z'>" +
				"    <bounds minlat='53.550' minlon='9.994' maxlat='53.551' maxlon='9.995'/> " +
				"    <member type='relation' ref='4' role=''/>" +
				"</relation>";
		Relation relation = parse(xml).relationsWithGeometry.get(0).relation;

		assertEquals(3, relation.getVersion());

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
		c.set(2019, Calendar.MARCH, 28, 21, 43, 50);
		assertEquals(c.getTimeInMillis() / 1000, relation.getDateEdited().getTime() / 1000);
	}

	@Test public void parsesRelationTags()
	{
		String xml =
				"<relation id='5'>" +
				"    <bounds minlat='53.550' minlon='9.994' maxlat='53.551' maxlon='9.995'/> " +
				"    <member type='relation' ref='4' role=''/>" +
				"    <tag k='a' v='b'/>" +
				"    <tag k='c' v='d'/>" +
				"</relation>";
		Relation relation = parse(xml).relationsWithGeometry.get(0).relation;

		assertNotNull(relation.getTags());
		assertEquals("b", relation.getTags().get("a"));
		assertEquals("d", relation.getTags().get("c"));
	}

	@Test public void parseSeveral()
	{
		String xml =
			"<node id='1' lat='1' lon='4'/>" +
			"<node id='2' lat='1' lon='4'/>" +
			"<way id='1'><nd ref='2' lat='1' lon='3'/><nd ref='3' lat='2' lon='4'/>" +
			"    <bounds minlat='53.550' minlon='9.994' maxlat='53.551' maxlon='9.995'/> " +
			"</way>" +
			"<way id='2'><nd ref='2' lat='1' lon='3'/><nd ref='3' lat='2' lon='4'/>" +
			"    <bounds minlat='53.550' minlon='9.994' maxlat='53.551' maxlon='9.995'/> " +
			"</way>" +
			"<relation id='1' >" +
			"    <bounds minlat='53.550' minlon='9.994' maxlat='53.551' maxlon='9.995'/> " +
			"    <member type='way' ref='2' role=''><nd lat='1' lon='3'/><nd lat='2' lon='4'/></member>" +
			"</relation>" +
			"<relation id='2' >" +
			"    <bounds minlat='53.550' minlon='9.994' maxlat='53.551' maxlon='9.995'/> " +
			"    <member type='node' lat='1' lon='3' ref='2' role=''/>" +
			"</relation>";

		MapDataWithGeometryCollection all = parse(xml);
		assertEquals(2, all.nodes.size());
		assertEquals(2, all.waysWithGeometry.size());
		assertEquals(2, all.relationsWithGeometry.size());
	}

	private MapDataWithGeometryCollection parse(String xml)
	{
		MapDataWithGeometryCollection collection = new MapDataWithGeometryCollection();
		MapDataWithGeometryParser parser = new MapDataWithGeometryParser(collection, new OsmMapDataFactory());
		try
		{
			parser.parse(TestUtils.asInputStream(xml));
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		return collection;
	}
}
