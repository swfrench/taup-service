package edu.seismo.TauPService

import Wrappers._

object Responder {

  type Params = Map[String, String]

  def timeResponse(params: Params): String =
    run(new Time(_), params)

  def pierceResponse(params: Params): String =
    run(new Pierce(_), params)

  def pathResponse(params: Params): String =
    run(new Path(_), params)

  def run[T <: TauPWrapper](tauPFactory: String => T, params: Params): String = {

    var resp: String = ""

    val model: String = params.getOrElse("model","iasp91")

    val tp: T = tauPFactory(model)

    val depth: Double =
      try {
        params("depth").toDouble
      } catch {
        case e: Exception =>
          throw new IllegalArgumentException("No valid depth supplied - " ++ e.getMessage)
      }

    val distance: Double =
      try {
        params("distance").toDouble
      } catch {
        case e: Exception =>
          throw new IllegalArgumentException("No valid distance supplied - " ++ e.getMessage)
      }

    val phase: String =
      try {
        params("phase")
      } catch {
        case e: Exception =>
          throw new IllegalArgumentException("No valid phase supplied - " ++ e.getMessage)
      }

    tp.start
    resp = tp.calculate(phase, depth, distance)
    tp.stop

    resp
  }
}


