# Client API

Akka HTTP에서는 HTTP 기반 서비스를 소비할 수 있는 엔드포인트를 제공한다. 

## Request-Level Client-Side API

Request-level API는 가장 추천하고 가장 호율적으로 Akka HTTP 클라이언트 사이드 구성하는 방법. 내부적으로 Host-Level Client API를 기반으로 구성되어 있다. 

Request-level 기본적으로 액터 시스템의 탑레벨 connection pool를 구형한다. 계속적으로 오래 수행되는 request가 유입되면 블락킹을 유발하고 다른 요청들은 기아상태에 빠진다.
이럴 경우 Connection-Level Client-Side API를 사용하거나 extra-pool을 구성하도록 하자.

### Future-Based Variant

Http Client가 어떤 리퀘스트가 단순히 필요하고 스트리밍 인프라를 세팅하기를 원하지 않을 때 사용한다.

Http().singleRequest(...) 메소드를 호출하면 Future[HttpResponse]가 리턴된다. 

#### Using the Future-Based API in Actors

[HttpClientActorFuture](/src/main/scala/http/ch05/HttpClientActorFuture.scala)

항상 entity stream을 소비해야 된다. response.discardEntityBytes()를 호출해서 사용하지 않는 응답된 Sink 시키도록 하자.

### Flow-Based Variant

Http().superPool() 메소드를 호출하면 Flow가 리턴된다. 


## Host-Level Client-Side API

Connection-Level Client-Side API와 반대로 host-level API는 각각의 HTTP connections 들을 직접적으로 처리할 수 있다.

### Requesting a Host Connection Pool

가장 좋은 방법은 타겟 엔드포인트에서 커넥션풀을 유지하는 것. Http().cachedHostConnectionPool() 메소드를 이용해서 세팅할 수 있다.  

