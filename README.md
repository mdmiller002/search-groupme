# GroupMe Search Service
A proof-of-concept backend for a GroupMe searching application.

***This service is purely a proof-of-concept and is not suitable for any real-world production use.***

-----

A backend service to support a GroupMe searching application. This proof-of-concept serves to improve on the app [Search through GroupMe](https://play.google.com/store/apps/details?id=codswallop.groupmesearch&hl=en_US&gl=US)
in the following ways:
- Offload GroupMe message retrieval to a backend Java Spring Boot service, so users are not blocked on the frontend during retrieval.
- Offload message searching to a Search Server on the backend to more efficiently search for messages.
