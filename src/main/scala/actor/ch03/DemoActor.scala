package actor.ch03

import akka.actor.{Actor, Props}

object DemoActor {
  // 액터가 받을 수 있는 메시지는 Actor 의 companion object에 정의하는 것은 좋은 습관이다.
  case class Greeting(from: String)
  case object Goobdye

  def props(magicNumber: Int): Props = ???
}

class DemoActor(magicNumber: Int) extends Actor {
  override def receive: Receive = {
    case x: Int => sender() ! (x + magicNumber)
  }
}

class SomeOtherActor extends Actor {
  // Props(new DemoActor(42)) would not be safe
  context.actorOf(DemoActor.props(42), "demo")

  override def receive: Receive = ???
}
