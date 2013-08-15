package edu.seismo.TauPService

import spray.util.LoggingContext
import spray.http.StatusCodes._
import spray.routing._

class TauPServiceActor extends HttpServiceActor with TauPService {
  def receive = runRoute(myRoute)
}

trait TauPService extends HttpService {

  implicit def myExceptionHandler(implicit log: LoggingContext) =
    ExceptionHandler {
      case e: Exception => ctx =>
        log.warning("Request {} could not be handled normally due to exception: {}",
          ctx.request, e.getMessage)
        ctx.complete(InternalServerError, "Error\n-----\n" ++ e.getMessage)
    }

  val myRoute = {
    path("time") {
      get {
        parameterMap { params =>
          complete {
            Responder.timeResponse(params)
          }
        }
      }
    } ~
    path("pierce") {
      get {
        parameterMap { params =>
          complete {
            Responder.pierceResponse(params)
          }
        }
      }
    } ~
    path("path") {
      get {
        parameterMap { params =>
          complete {
            Responder.pathResponse(params)
          }
        }
      }
    }
  }

}
