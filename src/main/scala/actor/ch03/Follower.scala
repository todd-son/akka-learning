package actor.ch03

import akka.actor.{Actor, ActorIdentity, ActorRef, ActorSystem, Identify, Props, Terminated}

class Follower extends Actor {
  val identifyId = 1

  println(s"Follower started. ${self}")
  context.actorSelection("/test/user/parent/another") ! Identify(identifyId)

  override def receive: Receive = {
    case ActorIdentity(`identifyId`, Some(ref)) =>
      println("receive1")
      context.watch(ref)
      context.become(active(ref))
    case ActorIdentity(`identifyId`, None) =>
      println("receive2", identifyId)
      context.stop(self)
  }

  def active(another: ActorRef): Receive = {
    case Terminated(`another`) => context.stop(self)
  }
}

class Parent extends Actor {
  val b = context.actorOf(Props(classOf[Follower]), "another")
  context.watch(b)

  override def receive: Receive = {
    case Terminated(ref) =>
      println("test", ref.path)
      context.stop(self)
  }
}

object FollowerTest extends App {
  val system = ActorSystem("test")
  val a = system.actorOf(Props(classOf[Parent]), "parent")

}
