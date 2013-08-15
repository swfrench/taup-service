package edu.seismo.TauPService.Wrappers

import edu.sc.seis.TauP.{TauP_Time, TauP_Pierce, TauP_Path}
import java.io.{PipedInputStream,PipedOutputStream,PrintWriter}

import scala.annotation.tailrec


object Util {

  @tailrec
  def readFromStream(in: PipedInputStream, acc: List[Char]): String =
    if (in.available > 0)
      readFromStream(in, in.read.toChar :: acc)
    else
      acc.reverse.mkString("")

  def readFromStream(in: PipedInputStream): String =
    readFromStream(in, List())

}


trait TauPWrapper {

  /*
   * Class variables (wow it feels terrible writing scala like this ...)
   */
  var inPipe: PipedInputStream = _
  var outPipe: PipedOutputStream = _
  var printWriter: PrintWriter = _
  var tauP: TauP_Time = _

  val pipeSize: Int = 2 << 19

  def start = {
    inPipe = new PipedInputStream(pipeSize)
    outPipe = new PipedOutputStream()
    printWriter = new PrintWriter(outPipe)
    inPipe.connect(outPipe)
  }

  def stop = {
    inPipe.close()
    outPipe.close()
    printWriter.close()
  }

  def calculate(phase: String, depth: Double, distance: Double): String =
    calculate(List(phase), depth, distance)

  def calculate(phases: List[String], depth: Double, distance: Double): String = {

    tauP.clearPhaseNames()
    phases.foreach(tauP.appendPhaseName(_))

    tauP.setSourceDepth(depth)

    tauP.calculate(distance)

    tauP.printResult(printWriter)
    printWriter.flush()
    outPipe.flush()

    Util.readFromStream(inPipe)

  }
}

class Time(model: String) extends TauPWrapper {
  tauP = new TauP_Time(model)
}

class Pierce(model: String) extends TauPWrapper {
  tauP = new TauP_Pierce(model)
}

class Path(model: String) extends TauPWrapper {
  tauP = new TauP_Path(model)
}
