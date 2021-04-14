package de.westnordost.osmapi.overpass;

import org.jetbrains.annotations.NotNull;

import de.westnordost.osmapi.ApiRequestWriter;
import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.errors.OsmApiException;
import de.westnordost.osmapi.common.errors.OsmBadUserInputException;
import de.westnordost.osmapi.map.MapDataFactory;
import de.westnordost.osmapi.map.MapDataParser;
import de.westnordost.osmapi.map.OsmMapDataFactory;
import de.westnordost.osmapi.map.handler.MapDataHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/** Get map data from the overpass api. Always expects XMl output in the queries. */
public class OverpassMapDataApi
{
	private final OsmConnection osm;
	private final MapDataFactory mapDataFactory;

	public OverpassMapDataApi(@NotNull OsmConnection osm, @NotNull MapDataFactory mapDataFactory)
	{
		this.osm = osm;
		this.mapDataFactory = mapDataFactory;
	}

	public OverpassMapDataApi(@NotNull OsmConnection osm)
	{
		this(osm, new OsmMapDataFactory());
	}

	/**
	 * Executes the given query and expects a response that is alike a map data query on the
	 * OSM API, just returning the map data. For example a query with <code>out body;</code>.
	 *
	 * @param query Query string. Either Overpass QL or Overpass XML query string.
	 * @param handler handler to feed the map data to
	 *
	 * @throws OsmTooManyRequestsException if the user is over his request quota. See getStatus, killMyQueries
	 * @throws OsmBadUserInputException if there is an error in the query
	 */
	public void queryElements(@NotNull String query, @NotNull MapDataHandler handler)
	{
		query(query, new MapDataParser(handler, mapDataFactory));
	}

	/**
	 * Executes the given query that has the <code>geom</code> modificator. For example a query with
	 * <code>out body geom;</code>. See <a href="https://wiki.openstreetmap.org/wiki/Overpass_API/Overpass_QL#out"><code>out statements</code></a>.
	 *
	 * @param query Query string. Either Overpass QL or Overpass XML query string. The query must
	 *              contain the <code>geom</code> modificator.
	 * @param handler function to feed the map data and geometry to
	 *
	 * @throws OsmTooManyRequestsException if the user is over his request quota. See getStatus, killMyQueries
	 * @throws OsmBadUserInputException if there is an error in the query
	 */
	public void queryElementsWithGeometry(@NotNull String query, @NotNull MapDataWithGeometryHandler handler)
	{
		query(query, new MapDataWithGeometryParser(handler, mapDataFactory));
	}

	/**
	 * Executes the given query that has the <a href="https://wiki.openstreetmap.org/wiki/Overpass_API/Overpass_QL#CSV_output_mode">output mode set to CSV</a>.
	 *
	 * @param query Query string. Either Overpass QL or Overpass XML query string. The query must
	 *              use the output mode CSV.
	 * @param handler function to feed the map data and geometry to
	 * @param separator The separator to expect in the CSV response. The default is the tab character.
	 *
	 * @throws OsmTooManyRequestsException if the user is over his request quota. See getStatus, killMyQueries
	 * @throws OsmBadUserInputException if there is an error in the query
	 */
	public void queryTable(@NotNull String query, @NotNull Handler<String[]> handler, @NotNull String separator)
	{
		query(query, new CsvParser(handler, separator));
	}

	/** @see #queryTable(String, Handler) */
	public void queryTable(@NotNull String query, @NotNull Handler<String[]> handler)
	{
		query(query, new CsvParser(handler));
	}

	/**
	 * Executes the given query to count the elements. For example a query with
	 * <code>out count;</code>. See <a href="https://wiki.openstreetmap.org/wiki/Overpass_API/Overpass_QL#out"><code>out statements</code></a>.
	 *
	 * @param query Query string. Either Overpass QL or Overpass XML query string. The query must
	 *              end with <code>out count;</code>
	 * @return the count for each element type
	 *
	 * @throws OsmTooManyRequestsException if the user is over his request quota. See getStatus, killMyQueries
	 * @throws OsmBadUserInputException if there is an error in the query
	 */
	@NotNull public ElementCount queryCount(@NotNull String query) {
		return query(query, new ElementCountParser());
	}

	/**
	 * Executes the given query. Since the output format and the content of the output can vary
	 * greatly depending on the type of query, you must specify yourself how you are going to parse
	 * the result.
	 *
	 * @param query Query string. Either Overpass QL or Overpass XML query string
	 * @param reader the object reading the response
	 * @param <T> what the type of object the reader returns
	 * @return what the supplied reader returns
	 *
	 * @throws OsmTooManyRequestsException if the user is over his request quota. See getStatus, killMyQueries
	 * @throws OsmBadUserInputException if there is an error in the query
	 */
	public synchronized <T> T query(@NotNull final String query, ApiResponseReader<T> reader)
	{
		ApiRequestWriter writer = new ApiRequestWriter()
		{
			@Override public String getContentType() { return "application/x-www-form-urlencoded"; }

			@Override public void write(OutputStream out) throws IOException
			{
				String request = "data=" + urlEncode(query);
				out.write(request.getBytes(Charset.forName(OsmConnection.CHARSET)));
			}
		};
		try
		{
			return osm.makeRequest("interpreter", "POST", false, writer, reader);
		} catch (OsmApiException e)
		{
			if (e.getErrorCode() == 429) throw new OsmTooManyRequestsException(e);
			else throw e;
		}
	}

	/** Kills all the queries sent from this IP. Useful if there is a runaway query that takes far
	 *  too much time and blocks the user from making any more queries
	 */
	public synchronized void killMyQueries()
	{
		osm.makeRequest("kill_my_queries", null);
	}

	/** Get info about how many queries the user may make until reaching his quota
	 *  @return the current quota status
	 */
	public synchronized OverpassStatus getStatus()
	{
		return osm.makeRequest("status", new OverpassStatusParser());
	}

	private String urlEncode(String text)
	{
		try
		{
			return URLEncoder.encode(text, OsmConnection.CHARSET);
		}
		catch (UnsupportedEncodingException e)
		{
			// should never happen since we use UTF-8
			throw new RuntimeException(e);
		}
	}
}
