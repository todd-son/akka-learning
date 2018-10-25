package actor.ch05

import akka.actor.{Actor, ActorSystem, Props}
import scala.concurrent.duration._

import scala.concurrent.Await

class HotSwapActor extends Actor {
  def angry: Receive = {
    case "foo" => {
      val message = "I am already angry?"
      println(message)
      sender() ! message
    }
    case "bar" => context.become(happy)
    case "oops" =>
      val message = "oops! I am angry"
      println(message)
      sender() ! message
  }

  def happy: Receive = {
    case "bar" =>
      val message = "I am already happy !-)"
      println(message)
      sender() ! message
    case "foo" => context.become(angry)
    case "oops" =>
      val message = "oops! I am happy !"
      println(message)
      sender() ! message
  }

  override def receive: Receive = {
    case "foo" => context.become(angry)
    case "bar" => context.become(happy)
  }
}

object HotSwapActorApp extends App {
  val system = ActorSystem("test")

  val swapper = system.actorOf(Props[HotSwapActor], "swapper")

  swapper ! "foo"
  swapper ! "foo"
  swapper ! "oops"

  Await.result(system.terminate(), 5 seconds)
}
