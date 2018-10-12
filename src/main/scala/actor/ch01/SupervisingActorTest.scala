package actor.ch01

import akka.actor.{Actor, ActorSystem, Props}

import scala.io.StdIn

class SupervisingActor extends Actor {
  val child = context.actorOf(Props[SupervisedActor])

  override def receive: Receive = {
    case "failChild" => child ! "fail"
  }
}

class SupervisedActor extends Actor {
  override def preStart(): Unit = println("supervised actor started")

  override def postStop(): Unit = println("supervised actor stopped")

  override def receive: Receive = {
    case "fail" =>
      println("supervised actor fails now ")
      throw new Exception("I failed!")
  }
}

object SupervisingActorTest extends App {
  val system = ActorSystem("test")
  val supervisingActor = system.actorOf(Props[SupervisingActor], "supervising-actor")

  supervisingActor ! "failChild"

  try StdIn.readLine()
  finally system.terminate()
}
