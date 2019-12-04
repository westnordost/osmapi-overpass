package de.westnordost.osmapi.overpass;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;

/** This class is fed the map data and geometry. */
public interface MapDataWithGeometryHandler
{
	/** The global bounding box has been parsed.
	 *  @param bounds the parsed node
	 */
	void handle(@NotNull BoundingBox bounds);

	/** A node has been parsed.
	 *  @param node the parsed node
	 */
	void handle(@NotNull Node node);

	/** A way and its geometry has been parsed.
	 *  @param way the parsed way
	 *  @param bounds bounding box for the way
	 *  @param geometry a list of positions the way consists of
	 */
	void handle(
			@NotNull Way way,
			@NotNull BoundingBox bounds,
			@NotNull List<LatLon> geometry);

	/** A relation and its geometry has been parsed. Note that the geometry of relation members is
	 *  not included (because the Overpass API does not supply it).
	 *
	 *  @param relation the parsed relation
	 *  @param bounds bounding box for the relation
	 *  @param nodeGeometries a map of node id to position for all nodes that are member of this
	 *                        relation
	 *  @param wayGeometries a map of way id to a list of positions the way consists of for all
	 *                       ways that are member of this relation
	 */
	void handle(
			@NotNull Relation relation,
			@NotNull BoundingBox bounds,
			@NotNull Map<Long, LatLon> nodeGeometries,
			@NotNull Map<Long, List<LatLon>> wayGeometries
	);
}