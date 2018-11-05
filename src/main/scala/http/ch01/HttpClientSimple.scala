package http.ch01

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object HttpClientSimple extends App {

  implicit val system = ActorSystem("test-actor")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val responseFuture = Http().singleRequest(HttpRequest(uri = "http://akka.io"))

  responseFuture
      .onComplete {
        case Success(res) => println(res)
        case Failure(_) => sys.error("something wrong")
      }

  Await.result(responseFuture, 3.seconds)

  system.terminate()
}
