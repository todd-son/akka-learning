## Identifying Actor via Actor Selection

각각 액터는 유니크한 로지컬 패스를 가진다. 

```scala
// will look up this absolute path
context.actorSelection("/user/serviceA/aggregator")
// will look up sibling beneath same supervisor
context.actorSelection("../joe")

```

주의할 점은 ActorSelection 이용해서 커뮤니케이션 하는 것보다 ActorRef를 사용하여 다른 액터와 커뮤니케이션을 하는 것이 항상 더 낫다.
ActorRef는 생성 및 조기화, 부모로의 전달 및 메시지를 ActorRef로 전달해서 다른 액터로 소개하는 등의 모든 것을 할 수 있다.

```scala
// will look all children to serviceB with names starting with worker
context.actorSelection("/user/serviceB/worker*")
// will look up all siblings beneath same supervisor
context.actorSelection("../*")

context.actorSelection("akka.tcp://app@otherhost:1234/user/serviceB")
```

