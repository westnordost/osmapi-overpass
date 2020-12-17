# osmapi-overpass

osmapi-overpass is a client for the [Overpass API](https://wiki.openstreetmap.org/wiki/Overpass_API), building on top of [osmapi](https://github.com/westnordost/osmapi).

## Copyright and License

© 2019-2020 Tobias Zwick. This library is released under the terms of the [GNU Lesser General Public License](http://www.gnu.org/licenses/lgpl-3.0.html) (LGPL).

## Installation

Add [`de.westnordost:osmapi-overpass:1.3`](https://maven-repository.com/artifact/de.westnordost/osmapi-overpass/1.3) as a Maven dependency or download the jar from there.
On Android, you need to exclude kxml2 from the dependencies since it is already built-in, like so:

```gradle
	compile ('de.westnordost:osmapi-overpass:1.3')
	{
		exclude group: 'net.sf.kxml', module: 'kxml2' // already included in Android
	}
```

## Example Usage

First, initialize the `OverpassMapDataDao`

```java
	OsmConnection connection = new OsmConnection("https://overpass-api.de/api/", "my user agent");
	OverpassMapDataDao overpass = new OverpassMapDataDao(connection);
```

then...

### Get all shops on Malta as OSM elements

```java
    overpass.queryElements(
        "[bbox:13.8,35.5,14.9,36.3]; nwr[shop]; out meta;",
        handler
    );
```

### Get all shops on Malta as OSM elements together with their geometry

```java
    overpass.queryElementsWithGeometry(
        "[bbox:13.8,35.5,14.9,36.3]; nwr[shop]; out meta geom;",
        handler
    );
```

### Get all shops on Malta and return their name plus type of shop as table rows

```java
    overpass.queryTable(
        "[out:csv(name, shop)][bbox:13.8,35.5,14.9,36.3]; nwr[shop]; out body;",
        handler
    );
```

### Count the number of shops on Malta.

```java
    ElementCount count = overpass.queryCount(
        "[bbox:13.8,35.5,14.9,36.3]; nwr[shop]; out count;"
    );
```
