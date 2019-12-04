package de.westnordost.osmapi.overpass;

import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Way;

public class WayWithGeometry
{
	public WayWithGeometry(Way way, BoundingBox bounds, List<LatLon> geometry)
	{
		this.way = way;
		this.bounds = bounds;
		this.geometry = geometry;
	}

	public final Way way;
	public final BoundingBox bounds;
	public final List<LatLon> geometry;
}
