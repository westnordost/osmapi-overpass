# osmapi-overpass

osmapi-overpass is a client for the [Overpass API](https://wiki.openstreetmap.org/wiki/Overpass_API), building on top of [osmapi](https://github.com/westnordost/osmapi).

## Copyright and License

© 2019-2023 Tobias Zwick. This library is released under the terms of the [GNU Lesser General Public License](http://www.gnu.org/licenses/lgpl-3.0.html) (LGPL).

## Installation

Add [`de.westnordost:osmapi-overpass:2.3`](https://mavenrepository.com/artifact/de.westnordost/osmapi-overpass/2.0) as a Maven dependency or download the jar from there.

### Android

On Android, you need to exclude kxml2 from the dependencies since it is already built-in, like so:

```gradle
dependencies {
    implementation 'de.westnordost:osmapi-overpass:2.3'
}

configurations {
    // already included in Android
    all*.exclude group: 'net.sf.kxml', module: 'kxml2'
    
    // @NonNull etc annotations are also already included in Android
    cleanedAnnotations
    compile.exclude group: 'org.jetbrains', module:'annotations'
    compile.exclude group: 'com.intellij', module:'annotations'
    compile.exclude group: 'org.intellij', module:'annotations'
}
```

Also, starting with v2.0, this library uses the classes from the Java 8 time API, like [`Instant`](https://developer.android.com/reference/java/time/Instant) etc. instead of `Date` which [leads to about 50% faster parsing times](https://github.com/streetcomplete/StreetComplete/discussions/2740) when receiving a result.

If your app supports Android API levels below 26, you have two options:

1. Either stick to using version 1.x of this library...
2. ...or enable [Java 8+ API desugaring support](https://developer.android.com/studio/write/java8-support#library-desugaring) for your app

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
