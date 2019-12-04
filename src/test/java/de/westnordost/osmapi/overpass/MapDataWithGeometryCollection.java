package de.westnordost.osmapi.overpass;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;

public class MapDataWithGeometryCollection implements MapDataWithGeometryHandler
{
	public BoundingBox bounds = null;
	public final List<Node> nodes = new ArrayList<>();
	public final List<WayWithGeometry> waysWithGeometry = new ArrayList<>();
	public final List<RelationWithGeometry> relationsWithGeometry = new ArrayList<>();

	@Override public void handle(@NotNull BoundingBox bounds)
	{
		this.bounds = bounds;
	}

	@Override public void handle(@NotNull Node node)
	{
		nodes.add(node);
	}

	@Override public void handle(
			@NotNull Way way,
			@NotNull BoundingBox bounds,
			@NotNull List<LatLon> geometry)
	{
		waysWithGeometry.add(new WayWithGeometry(way, bounds, geometry));
	}

	@Override public void handle(
			@NotNull Relation relation,
			@NotNull BoundingBox bounds,
			@NotNull Map<Long, LatLon> nodeGeometries,
			@NotNull Map<Long, List<LatLon>> wayGeometries)
	{
		relationsWithGeometry.add(new RelationWithGeometry(relation, bounds, nodeGeometries, wayGeometries));
	}
}
