# osmapi-overpass

osmapi-overpass is a client for the [Overpass API](https://wiki.openstreetmap.org/wiki/Overpass_API), building on top of [osmapi](https://github.com/westnordost/osmapi).

## Copyright and License

© 2019-2023 Tobias Zwick. This library is released under the terms of the [GNU Lesser General Public License](http://www.gnu.org/licenses/lgpl-3.0.html) (LGPL).

## Installation

Add [`de.westnordost:osmapi-overpass:3.0`](https://mavenrepository.com/artifact/de.westnordost/osmapi-overpass/3.0) as a Maven dependency or download the jar from there.

### Android

On Android, you need to exclude kxml2 from the dependencies in your `gradle.kts` since it is already built-in, like so:
```kotlin
configurations {
    all {
        // it's already included in Android
        exclude(group = "net.sf.kxml", module = "kxml2")
        exclude(group = "xmlpull", module = "xmlpull")
    }
}
```

This library uses classes from the Java 8 time API, like [`Instant`](https://developer.android.com/reference/java/time/Instant) etc., so if your app supports Android API levels below 26, you need to enable [Java 8+ API desugaring support](https://developer.android.com/studio/write/java8-support#library-desugaring).

## Example Usage

First, initialize the `OverpassMapDataApi`

```java
	OsmConnection connection = new OsmConnection("https://overpass-api.de/api/", "my user agent");
	OverpassMapDataApi overpass = new OverpassMapDataApi(connection);
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

The query string is just passed through to the Overpass API. For how the query string needs to look like, consult the [documentation for Overpass API Query Language](https://wiki.openstreetmap.org/wiki/Overpass_API/Overpass_QL).
