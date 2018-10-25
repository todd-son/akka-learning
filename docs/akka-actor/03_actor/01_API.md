## Actor API

Actor 트레잇은 하나의 추상 메소드 receive만을 정의하고 있다.

액터가 메세지를 매치하지 못하면 unhandled가 호출되고 기본적으로 akka.actor.UnhandledMessage(message, sender, recipient)가 액터시스템의 이벤트 스트림으로 퍼블리시 된다.

추가적으로 다음과 같은 것도 제공한다.

- self는 자신의 ActorRef를 참조한다.
- sender는 마지막 메시지를 응답한 Actor를 참조한다. 
- supervisorStrategy는 자식 액터들을 관리하는데 사용되는 유저가 오버라이드할 수 있는 설정이다.

- context는 액터와 현재 메시지에 대한 문맥적인 정보를 노출한다.
    - 자식 액터를 만드는 팩토리 메소드(actorOf)
    - 액터가 속한 system
    - 부모 관리자
    - 관리중인 자식들
    - 라이프 사이클 모니터링
    - Actor.HowSwap에 명시된 hotswap behavior 스택
    
마지막으로 시작 전후 라이프사이클 훅을 제공한다.

### Actor Lifecycle

라이브한 액터는 자신의 위치를 나타내는 패스와 UID 있다. 액터를 정확히 식별하기 위해서는 Path와 UID가 있다. 

ActorRef로 참조하고 있을때 아래 케이스에 대해 다른 양상을 가진다. 

- 재시작의 경우 패스를 그대로 유지한 체 Actor의 instance가 교체된다. 이 경우 ActorRef로의 참조는 그대로 유지된다.
- stop하고 액터를 재성성할 경우 ActorRef의 UID가 변경됨으로써 참조는 깨진다.

### Lifecycle Monitoring aka DeathWatch

```scala
import akka.actor.{ Actor, Props, Terminated }

class WatchActor extends Actor {
  val child = context.actorOf(Props.empty, "child")
  context.watch(child) // <-- this is the only call needed for registration
  var lastSender = context.system.deadLetters

  def receive = {
    case "kill" ⇒
      context.stop(child); lastSender = sender()
    case Terminated(`child`) ⇒ lastSender ! "finished"
  }
}
```

 
 
 

 