package de.westnordost.osmapi.overpass;

import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Relation;

public class RelationWithGeometry
{
	RelationWithGeometry(Relation relation, BoundingBox bounds, Map<Long, LatLon> nodeGeometries, Map<Long, List<LatLon>> wayGeometries)
	{
		this.relation = relation;
		this.bounds = bounds;
		this.nodeGeometries = nodeGeometries;
		this.wayGeometries = wayGeometries;
	}

	public final Relation relation;
	public final BoundingBox bounds;
	public final Map<Long, LatLon> nodeGeometries;
	public final Map<Long, List<LatLon>> wayGeometries;
}
