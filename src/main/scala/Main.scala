package edu.seismo.TauPService

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

object Main extends App {

  implicit val system = ActorSystem("spray-taup-service")

  val service = system.actorOf(Props[TauPServiceActor], "taup-service")

  IO(Http) ! Http.Bind(service, interface = "localhost", port = 8080)
}
