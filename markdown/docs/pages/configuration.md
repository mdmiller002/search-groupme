# Configuration Properties

Custom properties used by the service, which can be set by any of the various
methods Spring Boot supports setting properties (e.g. `application.properties`).

## Elasticsearch Properties
Controls the service's interaction with Elasticsearch.

| Property                                   | Description                                           | Default Value |
|--------------------------------------------|-------------------------------------------------------|---------------|
| `elasticsearch.hosts`                      | List of Elasticsearch hostnames to connect to         | empty         |
| `elasticsearch.maxIndexSizeInGb`           | Max size of messages index, in GB                     | 5             |
| `elasticsearch.persistSpaceCheckInterval`  | Number of persist calls in between a disk space check | 10            |

## Indexing Properties
Controls the message indexing algorithm.

| Property                                  | Description                                           | Default Value     |
|-------------------------------------------|-------------------------------------------------------|-------------------|
| `indexing.runIndexing`                    | Toggle the indexing functionality on or off (boolean) | `true`            |
| `indexing.groupMeApi`                     | Base URL of the GroupMe API to call to                | `localhost:1080`  |

