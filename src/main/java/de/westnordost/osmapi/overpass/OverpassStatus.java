package de.westnordost.osmapi.overpass;

import org.jetbrains.annotations.Nullable;

public class OverpassStatus
{
	/** Max number of concurrent queries the user may trigger */
	public int maxAvailableSlots;
	/** How many more queries the user may trigger until reaching his quota */
	public int availableSlots;
	/** Time until the next slot becomes available again in seconds. May be null if there is no
	 *  info on that. (Usually because the max available slots are available)*/
	@Nullable public Integer nextAvailableSlotIn;
}
