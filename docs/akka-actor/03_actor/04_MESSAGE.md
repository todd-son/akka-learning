## Send messages

! 은 tell을 의미한다. 비동기로 메시지를 전송하고 즉시 리턴한다.
? 은 ask를 의마한다. 비동기로 메시지를 전송하고 Future를 리턴하여 응답을 기다린다.

항상 tell이 옳다. 한정적으로 ask를 사용하고 타임아웃등을 항상 고려하여

### Tell : Fire-forget

```scala
actorRef ! message
``` 

### Ask : Send-And-Receive-Future

아래는 3개의 요청을 비동기로 전송하고 그 결과를 조합하여 다른 액터로 전송하는 예이다.

```scala
import akka.pattern.{ ask, pipe }
import system.dispatcher // The ExecutionContext that will be used
final case class Result(x: Int, s: String, d: Double)
case object Request

implicit val timeout = Timeout(5 seconds) // needed for `?` below

val f: Future[Result] =
  for {
    x ← ask(actorA, Request).mapTo[Int] // call pattern directly
    s ← (actorB ask Request).mapTo[String] // call by implicit conversion
    d ← (actorC ? Request).mapTo[Double] // call by symbolic name
  } yield Result(x, s, d)

f pipeTo actorD // .. or ..
pipe(f) to actorD

```

타임 아웃을 설정하는 방법은 다음과 같다. 하나의 명시적인 방법이고 두번째는 암시적인 방법이다. 

```scala
import scala.concurrent.duration._
import akka.pattern.ask
val future = myActor.ask("hello")(5 seconds)
```

```scala
import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.ask
implicit val timeout = Timeout(5 seconds)
val future = myActor ? "hello"
```

### Forward message

```scala
target forward message
```

## Receive messages

액터는 메시지가 응답하기 위한 receive 메소드를 구현해야 한다.

```scala
type Receive = PartialFunction[Any, Unit]

def receive: Actor.Receive
```

## Reply to messages

메시지 전송자에 응답하는 방법은 다음과 같다.

```scala
sender() ! x 
```

## Receive timeout

```scala
import akka.actor.ReceiveTimeout
import scala.concurrent.duration._
import akka.actor.Actor

class MyActor extends Actor {
  // To set an initial delay
  context.setReceiveTimeout(30 milliseconds)
  def receive = {
    case "Hello" ⇒
      // To set in a response to a message
      context.setReceiveTimeout(100 milliseconds)
    case ReceiveTimeout ⇒
      // To turn it off
      context.setReceiveTimeout(Duration.Undefined)
      throw new RuntimeException("Receive timed out")
  }
}
```

### Timers, scheduled messages

```scala
import scala.concurrent.duration._

import akka.actor.Actor
import akka.actor.Timers

object MyActor {
  private case object TickKey
  private case object FirstTick
  private case object Tick
}

class MyActor extends Actor with Timers {
  import MyActor._
  timers.startSingleTimer(TickKey, FirstTick, 500.millis)

  def receive = {
    case FirstTick ⇒
      // do something useful here
      timers.startPeriodicTimer(TickKey, Tick, 1.second)
    case Tick ⇒
    // do something useful here
  }
}
```

### Stopping Actors

ActorContext나 ActorSystem이 상속산 ActorRefFactory의 stop 메소드를 통해서 Actor를 정지시킬 수 있다.
실제 종료는 비동기다. 그래서 stopped가 되기전에 리턴을 받게 된다. context는 자신이나 자식을 종료 할 때 system은 top level actor 전체를 종료할 때 사용된다.
stop 명령은 일반적으로 메시지를 처리중이면 기다리지만 메일박스에 대기중인 메시지는 처리하지 않는다. 기본적으로 ActorSystem에 deadLetters를 발행하지만 메일박스 구현에 따라 달라질 수 있다. 

```scala
import akka.actor.Actor

class MyActor extends Actor {

  val child: ActorRef = ???

  def receive = {
    case "interrupt-child" ⇒
      context stop child

    case "done" ⇒
      context stop self
  }

}
```

당연히 나를 종료하면 계층상 자식들도 종료된다.

#### PoisonPill

akka.actor.PoisonPill 메시지를 전송함으로써 actor를 중지 시킬 수 있다. 해당 메시지는 메일박스로 전송되고 이미 전송된 다른 메세지를 처리한 후에 정지를 수행한다. 

```scala
watch(victim)
victim ! PoisonPill
```

#### Killing an Actor 

```scala
context.watch(victim) // watch the Actor to receive Terminated message once it dies

victim ! Kill

expectMsgPF(hint = "expecting victim to terminate") {
  case Terminated(v) if v == victim ⇒ v // the Actor has indeed terminated
}
```

일반적으로 PoisonPill 이나 Kill 에 의존하는 것은 좋지 못하다. PleaseCleanupAndStop와 같은 메시지로 통신하라.

#### Graceful Stop

종료를 기다리거나 몇몇 액터의 순차적은 종료를 기다린다면 gracefulStop이 유용하다.

```scala 
import akka.pattern.gracefulStop
import scala.concurrent.Await

try {
  val stopped: Future[Boolean] = gracefulStop(actorRef, 5 seconds, Manager.Shutdown)
  Await.result(stopped, 6 seconds)
  // the actor has been stopped
} catch {
  // the actor wasn't stopped within 5 seconds
  case e: akka.pattern.AskTimeoutException ⇒
}
```