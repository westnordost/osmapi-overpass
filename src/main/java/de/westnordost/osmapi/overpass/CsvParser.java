package de.westnordost.osmapi.overpass;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.common.Handler;

/** Parses a response in the form of a CSV, so with the
 *  <a href="https://wiki.openstreetmap.org/wiki/Overpass_API/Overpass_QL#CSV_output_mode">output mode set to CSV</a>. */
public class CsvParser implements ApiResponseReader<Void>
{
	private static final String DEFAULT_SEPARATOR = "\t";

	private final Handler<String[]> handler;
	private final String separator;

	public CsvParser(@NotNull Handler<String[]> handler, String separator)
	{
		this.handler = handler;
		this.separator = separator;
	}

	public CsvParser(@NotNull Handler<String[]> handler) { this(handler, DEFAULT_SEPARATOR); }

	@Override public Void parse(InputStream in) throws Exception
	{
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8")))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] csv = line.split(separator);
				handler.handle(csv);
			}
		}
		return null;
	}
}
