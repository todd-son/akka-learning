package actor.ch03

import akka.actor.{Actor, IndirectActorProducer, Props}

class DependencyInjector(applicationContext: AnyRef, beanName: String) extends IndirectActorProducer {
  override def produce(): Actor = new Echo(beanName)

  override def actorClass: Class[_ <: Actor] = classOf[Actor]

  def this(beanName: String) = this("", beanName)
}

class Echo(name: String) extends Actor {
  def receive = ???
}

class SomeActor extends Actor {
  val applicatonContext = this
  val actorRef = context.actorOf(Props(classOf[DependencyInjector], applicatonContext, "hello"), "helloBean")
  override def receive: Receive = ???
}
