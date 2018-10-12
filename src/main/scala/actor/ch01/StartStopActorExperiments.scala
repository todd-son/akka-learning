package actor.ch01

import akka.actor.{Actor, ActorSystem, Props}

import scala.io.StdIn

class StartStopActor1 extends Actor {
  override def preStart() = {
    println("first started")
    context.actorOf(Props[StartStopActor2], "second")
  }

  override def postStop() = println("first stopped")

  override def receive: Receive = {
    case "stop" => context.stop(self)

  }
}

class StartStopActor2 extends Actor {
  override def preStart() = println("second started")

  override def postStop() = println("second stopped")

  override def receive: Receive = Actor.emptyBehavior
}

object StartStopActorExperiments extends App {
  val system = ActorSystem("test")

  val first = system.actorOf(Props[StartStopActor1], "first")

  first ! "stop"

  try StdIn.readLine()

  system.terminate()
}
