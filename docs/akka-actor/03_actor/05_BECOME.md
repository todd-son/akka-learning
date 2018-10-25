## Become/Unbecome

Akka는 런타임중 Actor 메시지 루프의 핫스와핑을 지원한다. become 메소드를 이용하면 되고 become 메소드가 실행되면 새로운 부분함수가 메시지 핸들러가 된다. 
핫스왑된 코드는 Stack에 유지되며 푸쉬되거나 팝될 수 있다.

[HotSwapper](/src/main/scala/actor/ch05/HotSwapActor.scala)

핫스왑은 Finite State Machine(FSM) 같은 걸 구현할 때 유용하다.

## Stash

Stash 트레잇은 말 그대로 메시지를 처리하지 않고 치워버리는 것. become이나 unbecome에 의해 액터의 메시지 핸들러가 변경되면 모든 스태이쉬된 메시지는 unstashed 상태로 메일박스 prepend된다. 

[ActorWithProtocol](/src/main/scala/actor/ch05/ActorWithProtocol.scala)

stash() 메소드를 호출하면 메시지는 actor의 stash에 추가된다. 
stash도 관리를 잘해야 된다. 너무 많이 stash를 하다보면 StashOverflowException 발생한다. 메일 박스 설정의 stash-capacity 설정으로 capacity를 조정할 수 있다.

unstashAll()는 stash에서 메시지를 꺼내서메시지를 메일박스로 집어넣는다. MessageQueueAppendFailedException이 발생할 수 있으며, stash의 메시지는 비워진다.

## Actors and exceptions

액터가 메시지를 처리하다가 오류가 발생하면 각 요소에 아래와 같은 현상이 일어난다.

### What happens to the Message

메시지를 처리하다가 오류가 발생하면 메시지는 잃어버리게 될 것이다. 메일박스로 되돌아 가지 않는 다는 것을 이해하는 것이 중요하다. 재처리가 필요하다고 생각한다면 사용자가 따로 고민을 해야된다.

### What happens to the Mailbox

메시지를 처리하다가 오류가 나면 메일박스에는 아무런 일이 일어나지 않는다. 액터가 재시작되면 똑같은 메일박스가 거기 위치하고 있을 것이다. 모든 메시지는 안전하다.

### What happens to the actor

액터 코드 수행중 예외를 발생하면 액터는 중지되고 supervision process가 시작된다. 정책에 따라 재시작하거나 계속되거나 아에 중지 시킬 수 있다.

## Extending Actors using PartialFunction chaining

[ProducerConsumer](/src/main/scala/actor/ch05/ProducerConsumer.scala)

## Initialization patterns

### Initialization via constructor

생성자를 통한 초기화는 다양한 이점이 있음. val 필드를 통해 수정되지 않는 상태를 만들 수 있고 더 강건한 액터 구현을 할 수 있게 됨. 생성자는 actorOf를 호출할 때 그리고 재시작할때도 호출됨.

### Initialization via preStart

actorOf로 생성할때 호출됨. 재시작의 경우 postRestart에 의해 preStart가 호출됨 그러므로 재시작시에도 항상 호출됨. 하지만 postRestart를 오버라이드하면 preStart를 안 호출되게 할 수 잇음

유용한 케이스는 리스타트시 자식 액터를 재생성하지 않도록 하는 패턴에 활용 될 수 있음.

```scala
override def preStart(): Unit = {
  // Initialize children here
}

// Overriding postRestart to disable the call to preStart()
// after restarts
override def postRestart(reason: Throwable): Unit = ()

// The default implementation of preRestart() stops all the children
// of the actor. To opt-out from stopping the children, we
// have to override preRestart()
override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
  // Keep the call to postStop(), but no stopping of children
  postStop()
}
```

### Initialization via message passing

stash랑 같이 활용하면 괜춘하다.

```scala
var initializeMe: Option[String] = None

override def receive = {
  case "init" ⇒
    initializeMe = Some("Up and running")
    context.become(initialized, discardOld = true)

}

def initialized: Receive = {
  case "U OK?" ⇒ initializeMe foreach { sender() ! _ }
}
```


