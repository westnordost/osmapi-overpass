package de.westnordost.osmapi.overpass;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.common.XmlParser;
import de.westnordost.osmapi.map.MapDataFactory;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;

/**
 * A map data parser that also parses the geometry of elements as outputted by queries with the
 * <code>geom</code> modificator (for example <code>out body geom;</code>).
 * <br><br>
 * It supports the <a href="https://wiki.openstreetmap.org/wiki/Overpass_API/Overpass_QL#out"><code>out statements</code></a>
 * <code>skel</code>, <code>body</code>, <code>tags</code> and <code>meta</code>
 * but never includes the changeset and user info because it includes personally identifying
 * information and Overpass API is a public API.
 */
public class MapDataWithGeometryParser extends XmlParser implements ApiResponseReader<Void>
{
	private static final String
			NODE = "node",
			WAY = "way",
			RELATION = "relation",
			MEMBER = "member",
			ND = "nd",
			TAG = "tag",
			BOUNDS = "bounds";

	private final MapDataFactory factory;
	private final MapDataWithGeometryHandler handler;

	private long id;
	private int version;
	private Instant timestamp;
	private Double lat;
	private Double lon;
	private Map<String, String> tags;
	private List<RelationMember> members;
	private List<Long> nodeIds;
	private Map<Long, List<LatLon>> nodePositionsByWayId;
	private Map<Long, LatLon> nodePositionByNodeId;
	private List<LatLon> nodePositions;
	private BoundingBox bounds;

	public MapDataWithGeometryParser(
			@NotNull MapDataWithGeometryHandler handler,
			@NotNull MapDataFactory factory)
	{
		this.factory = factory;
		this.handler = handler;
	}

	@Override public Void parse(@NotNull InputStream in) throws IOException
	{
		id = -1;
		version = 0;

		doParse(in);

		return null;
	}

	@Override protected void onStartElement() throws ParseException
	{
		String name = getName();

		switch (name)
		{
			case BOUNDS:
				bounds = new BoundingBox(
						getDoubleAttribute("minlat"),getDoubleAttribute("minlon"),
						getDoubleAttribute("maxlat"),getDoubleAttribute("maxlon")
				);
				// global bounding box
				String parent = getParentName();
				if (!WAY.equals(parent) && !RELATION.equals(parent))
				{
					handler.handle(bounds);
					bounds = null;
				}
				break;

			case TAG:
				if (tags == null)
				{
					tags = new HashMap<>();
				}
				tags.put(getAttribute("k"), getAttribute("v"));
				break;

			case ND:
				if (WAY.equals(getParentName()))
				{
					nodeIds.add(getLongAttribute("ref"));
				}

				LatLon pos = new OsmLatLon(getDoubleAttribute("lat"), getDoubleAttribute("lon"));
				nodePositions.add(pos);
				break;

			case MEMBER:
				long ref = getLongAttribute("ref");
				String role = getAttribute("role");
				Element.Type type = Element.Type.valueOf(getAttribute("type").toUpperCase(Locale.UK));
				members.add(factory.createRelationMember(ref, role, type));

				if (type == Element.Type.NODE)
				{
					nodePositionByNodeId.put(ref, new OsmLatLon(getDoubleAttribute("lat"), getDoubleAttribute("lon")));
				}
				else if (type == Element.Type.WAY)
				{
					nodePositions = new ArrayList<>();
					nodePositionsByWayId.put(ref, nodePositions);
				}
				break;

			case NODE:
				retrieveElementAttributes();
				lat = getDoubleAttribute("lat");
				lon = getDoubleAttribute("lon");
				break;

			case WAY:
				retrieveElementAttributes();
				nodeIds = new ArrayList<>();
				nodePositions = new ArrayList<>();
				break;

			case RELATION:
				retrieveElementAttributes();
				members = new ArrayList<>();
				nodePositionsByWayId = new HashMap<>();
				nodePositionByNodeId = new HashMap<>();
				break;
		}
	}

	private void retrieveElementAttributes() throws ParseException
	{
		id = getLongAttribute("id");
		// version not set for out modes "ids", "skel"
		Integer v = getIntAttribute("version");
		version = v != null ? v : -1;
		// timestamp only set for out mode "meta"
		timestamp = parseDate();
	}

	private Instant parseDate()
	{
		String timestamp = getAttribute("timestamp");
		if(timestamp == null) return null;

		return Instant.parse(timestamp);
	}

	@Override protected void onEndElement()
	{
		String name = getName();

		switch(name)
		{
			case MEMBER:
				nodePositions = null;
				break;

			case NODE:
				Node node = factory.createNode(id, version, lat, lon, tags, null, timestamp);
				id = 0;
				version = -1;
				lat = null;
				lon = null;
				tags = null;
				timestamp = null;

				handler.handle(node);
				break;

			case WAY:
				Way way = factory.createWay(id, version, nodeIds, tags, null, timestamp);
				handler.handle(way, bounds, nodePositions);

				id = 0;
				version = -1;
				nodeIds = null;
				tags = null;
				timestamp = null;
				nodePositions = null;
				bounds = null;
				break;

			case RELATION:
				Relation relation = factory.createRelation(id, version, members, tags, null, timestamp);
				handler.handle(relation, bounds, nodePositionByNodeId, nodePositionsByWayId);

				id = 0;
				version = -1;
				members = null;
				tags = null;
				timestamp = null;
				nodePositionsByWayId = null;
				nodePositionByNodeId = null;
				nodePositions = null;
				bounds = null;
				break;
		}
	}
}
