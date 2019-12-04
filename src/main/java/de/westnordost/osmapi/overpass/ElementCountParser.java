package de.westnordost.osmapi.overpass;

import org.jetbrains.annotations.NotNull;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.common.XmlParser;

import java.io.IOException;
import java.io.InputStream;

/** Parses a <code>out count;</code> response. */
public class ElementCountParser extends XmlParser implements ApiResponseReader<ElementCount>
{
	private ElementCount elementCount = null;

	@NotNull public ElementCount parse(@NotNull InputStream in) throws IOException
	{
		elementCount = new ElementCount();
		doParse(in);

		return elementCount;
	}

	@Override protected void onStartElement()
	{
		if ("tag".equals(getName()))
		{
			String key = getAttribute("k");
			Long value = getLongAttribute("v");
			switch (key)
			{
				case "nodes":     elementCount.nodes = value;     break;
				case "ways":      elementCount.ways = value;      break;
				case "relations": elementCount.relations = value; break;
				case "total":     elementCount.total = value;     break;
			}
		}
	}

	@Override protected void onEndElement() {}
}
