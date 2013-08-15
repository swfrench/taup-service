package edu.seismo.TauPService

import spray.util.LoggingContext
import spray.http.StatusCodes._
import spray.routing._
import spray.http._
import MediaTypes._

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

  val helpString: String =
"""
<html>
  <body>
    <h1>taup-service web interface</h1>
    <h2>Supported tools:</h2>
    TauP_Time
    <a href="/time?phase=S&distance=60&depth=15&model=prem">example query</a>
    <br>
    TauP_Pierce
    <a href="/pierce?phase=S&distance=60&depth=15&model=prem">example query</a>
    <br>
    TauP_Path
    <a href="/path?phase=S&distance=60&depth=15&model=prem">example query</a>
    <br>
  </body>
</html>
"""

  val myRoute = {
    path("") {
      get {
        respondWithMediaType(`text/html`) {
          complete {
            helpString
          }
        }
      }
    } ~
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
