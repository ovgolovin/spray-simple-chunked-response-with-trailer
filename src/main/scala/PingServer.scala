import akka.actor.{Actor, ActorSystem, Props}
import akka.io.IO
import akka.util.Timeout
import spray.can.Http
import spray.http.HttpMethods._
import spray.http._

import scala.concurrent.duration._

object Main extends App
{
  implicit val system = ActorSystem("simple-ping-server")
  implicit val timeout: Timeout = 5.seconds
  implicit val executionContext = system.dispatcher

  val server = system.actorOf(Server.props, name="server")
  IO(Http) ! Http.Bind(server, interface = "localhost", port = 3535)
}


object Server {
  def props = Props(new Server)
}

class Server extends Actor {
  implicit val timeout: Timeout = 1.second

  def receive: Actor.Receive = {
    case Http.Connected(_, _) => {
      val handler = context.actorOf(RequestHandler.props)
      sender ! Http.Register(handler)
    }
  }
}


object RequestHandler {
  def props = Props(new RequestHandler)
}

class RequestHandler extends Actor {
  implicit val timeout: Timeout = 2.seconds
  implicit val executionContext = context.dispatcher

  def receive: Actor.Receive = {
    case request @ HttpRequest(GET, q @ Uri.Path("/ping"), _, _, _) => {
      val client = sender()
      client ! ChunkedResponseStart(
        HttpResponse(
          status = 200,
          entity = "body_chunk_1\n",
          headers = List(
            HttpHeaders.RawHeader("Key1", "Value1")
          )
        )
      )

      client ! MessageChunk(body = "body_chunk_2\n")

      client ! ChunkedMessageEnd(
        trailer = List(
          HttpHeaders.RawHeader("Key2", "Value2")
        )
      )
    }

    case _: HttpRequest => sender ! HttpResponse(status = 404, entity = "Unknown resource!\n")
  }
}