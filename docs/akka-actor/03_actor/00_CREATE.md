# Actors

액터 모델은 동시성 및 분산 시스템을 작성하는데 고수준의 추상을 제공함. 락킹 및 스레드 관리에서 벗어나 더 쉽게 동시성과 병렬성이 있는 시스템을 쉽게 작성하도록 함.

## Creating Actors 

[MyActor](/src/main/scala/actor/ch01/MyActor.scala)

Actor를 상속하고 receive 메소드를 구현하면 된다. receive에는 리턴 타입은 Unit이며 처리할 수 있는 모든 메시지를 패턴 매치해야 된다. 그렇지 않은면 UnhandledMessage가 액터시스템의 이벤트 스트림으로 발행된다.

### Props

액터 생성시 옵션을 명시한 설정 클래스

```scala
val props = Props(new MyActor)

```

```scala
final class ParentActor extends Actor {
 
  var counter = 0
 
  override def receive = {
    case Increment   => counter++
    case CreateChild => context.actorOf(Props(new ChildActor(counter)))
  }
}
 
final class ChildActor(value: Int) extends Actor {
 
  println(value)
 
  override def receive = Actor.emptyBehavior
}

```

절대 위와 같이 해서는 안된다. 참조를 전달하면 두 클래스간 연관이 생기고 액터 캡슐화가 깨진다.

```scala
def apply[T <: Actor: ClassTag](creator: => T): Props
```

Props object의 apply이 메소드를 보면 값을 넘기는 것이 아니라 참조를 넘긴다. 또한 성크를 넘기기 때문에 메소드 바디에서 첨으로 접근할때 평가가 이루어진다.
다시 말하자면 첫번째 안티 패턴은 생성자를 전달하는 것이고 이후에 평가된다는 것이다.

다시 말해 Props를 직접 호출하면 특정 타입을 리턴하는 메소드에 클로저를 통해서 값을 전달할 수 있다는 것이다. 그럼으로 항상 팩토리 메소드를 사용하도록 하자. 

[DemoActor](/src/main/scala/actor/ch03/DemoActor.scala)


### Creating Actor with props

액터는 이용가능한 ActorSystem 이나 ActorContext의 actorOf 팩토리 메소드를 이용하여 생성 할 수 잇다.

```scala
import akka.actor.ActorSystem

// ActorSystem is a heavy object: create only one per application
val system = ActorSystem("mySystem")
val myActor = system.actorOf(Props[MyActor], "myactor2")

```

ActorSystem을 이용하여 Actor를 생성하면 탑레벨의 액터를 생성할 수 있으며, 액터시스템의 가디언 액터에 의해 관리된다. 
계층형 구조로 액터를 생성하는 것을 권고한다. actorOf의 리턴은 ActorRef 인스턴스이며 이 인스턴스는 상호 통신을 위한 유일한 객체이다. 이는 Serializable해서 원격 네트워크상에서 연결될 수 있다.

name 파라미터는 옵션이지만 되도록 넣기를 권장한다. 그러면 로그 메시지나 액터를 식별할때 사용하기 좋다.

### Dependency Injection 

디펜던시 인젝션 프레임워크에 의해 실제 생성자가 결정될때 처리할때는 다음과 같이 IndirectActorProducer를 사용해야 된다
주의할 점은 빈이 싱글톤 스코프이면 안된다. 

[DependencyInjector](/src/main/scala/actor/ch03/DependencyInjector.scala)




