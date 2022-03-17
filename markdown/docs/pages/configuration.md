# Configuration Properties

Custom properties used by the service, which can be set by any of the various
methods Spring Boot supports setting properties (e.g. `application.properties`).

## Elasticsearch Properties
Controls the service's interaction with Elasticsearch.

| Property                                  | Type            | Description                                                   | Default Value |
|-------------------------------------------|-----------------|---------------------------------------------------------------|---------------|
| `elasticsearch.hosts`                     | list of strings | List of Elasticsearch hostnames to connect to.                | empty         |
| `elasticsearch.maxIndexSizeGb`            | int             | Max size of messages index, in GB. Set to <= 0 for unlimited. | 5             |
| `elasticsearch.persistSpaceCheckInterval` | int             | Number of persist calls in between a disk space check.        | 10            |

## Indexing Properties
Controls the message indexing algorithm.

| Property                                  | Type    | Description                                  | Default Value     |
|-------------------------------------------|---------|----------------------------------------------|-------------------|
| `indexing.runIndexing`                    | boolean | Toggle the indexing functionality on or off. | `true`            |
| `indexing.groupMeApi`                     | string  | Base URL of the GroupMe API to call to.      | `localhost:1080`  |

