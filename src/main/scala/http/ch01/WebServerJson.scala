package http.ch01

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future
import scala.io.StdIn

case class Item(name: String, id: Long)

case class Order(items: List[Item])

object WebServerJson {
  implicit val system = ActorSystem("test-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  var orders: List[Item] = Nil

  implicit val itemFormat = jsonFormat2(Item)
  implicit val ordderFormat = jsonFormat1(Order)

  def main(args: Array[String]): Unit = {
    val route =
      get {
        pathPrefix("item" / LongNumber) {
          itemId =>
            val maybeItem: Future[Option[Item]] = Future {
              orders.find(o => o.id == itemId)
            }

            onSuccess(maybeItem) {
              case Some(item) => complete(item)
              case None => complete(StatusCodes.NotFound)
            }

        }
      } ~
        post {
          path("create-order") {
            entity(as[Order]) { order =>
              this.orders = order match {
                case Order(items) => items ::: orders
                case _ => orders
              }

              val result = Future(Done)

              onComplete(result) { done => complete("order created") }
            }
          }
        }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println("Server online at http://localhost:8080/")
    StdIn.readLine()

    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}

