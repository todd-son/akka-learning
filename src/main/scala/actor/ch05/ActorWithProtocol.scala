package actor.ch05

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorSystem, Props, Stash}

import scala.concurrent.Await
import scala.concurrent.duration._

class ActorWithProtocol extends Actor with Stash {
  override def receive: Receive = {
    case "open" =>
      unstashAll()
      context.become({
        case "write" => println("write to db!")
        case "close" =>
          unstashAll()
          context.unbecome()
      }, discardOld = false) // push on top instead of replace
    case msg => {
      println("stash message", msg)
      stash()
    }
  }
}

object ActorWithProtocolApp extends App {
  val system = ActorSystem("test")

  val actorRef = system.actorOf(Props[ActorWithProtocol], "protocol")

  actorRef ! "merong"
  actorRef ! "merong"
  actorRef ! "open"
  actorRef ! "merong"
  actorRef ! "write"
  actorRef ! "write"
  actorRef ! "write"
  actorRef ! "write"
  actorRef ! "close"
  actorRef ! "write"

  TimeUnit.SECONDS.sleep(2)

  Await.result(system.terminate(), 5 seconds)
}
