package http.ch05

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString

class HttpClientActorFuture extends Actor with ActorLogging {
  import akka.pattern.pipe
  import context.dispatcher

  private implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(
      context.system
    )
  )

  val http = Http(context.system)

  override def preStart(): Unit = {
    http.singleRequest(
      HttpRequest(uri = "https://akka.io")
    ).pipeTo(self)
  }

  override def receive: Receive = {
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach {
        body => log.info("Got response, body: " + body.utf8String)
      }

    case resp @ HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }
}

object HttpClientActorFutureTest extends App {
  val system = ActorSystem("test-system")

  val actorRef = system.actorOf(Props[HttpClientActorFuture], "httpClientActorFuture")

  TimeUnit.SECONDS.sleep(5)

  system.terminate()
}
