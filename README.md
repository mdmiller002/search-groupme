# GroupMe Search Service
A proof-of-concept backend service for a GroupMe searching application.

***This service is purely a proof-of-concept and is not suitable for any real-world production use.***

-----

A backend service to support a GroupMe searching application. This proof-of-concept serves to improve on the app [Search through GroupMe](https://play.google.com/store/apps/details?id=codswallop.groupmesearch&hl=en_US&gl=US)
in the following ways:
- Offload GroupMe message retrieval to a backend service, so users are not blocked on the frontend during retrieval.
- Offload message searching to a Search Server on the backend to more efficiently search for messages.

## Features

### <img src="markdown/images/spring.png" width="20"/> Spring Boot

Spring Boot is the application framework the search service is built with.

### <img src="markdown/images/elasticsearch.png" width="20"> Elasticsearch

Elasticsearch is the search engine powering the search service.
Messages are retrieved from GroupMe and indexed into Elasticsearch.

### <img src="markdown/images/prometheus.png" width="20"> Prometheus

Prometheus metrics are generated by the service for observability.

----

## [Documentation](markdown/docs/README.md)