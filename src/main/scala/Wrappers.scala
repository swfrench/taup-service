package edu.seismo.TauPService.Wrappers

import edu.sc.seis.TauP.{TauP_Time, TauP_Pierce, TauP_Path}
import java.io.{PipedInputStream,PipedOutputStream,PrintWriter}

import scala.annotation.tailrec

/**
 * Utils for reading Strings from PipedInputStream objs
 */
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

/**
 * A trait for TauP tool wrapper classes (resources for comms w/ TauP objs via
 * java.io, calculation, etc ...)
 */
trait TauPWrapper {

  var inPipe: PipedInputStream = _
  var outPipe: PipedOutputStream = _
  var printWriter: PrintWriter = _

  // left abstract - all supported tools subclass TauP_Time
  val tauP: TauP_Time

  // default buffer size for inPipe (1MB)
  val pipeSize: Int = 2 << 19

  def start() = {
    // initialize java.io objs for communication w/ TauP_* objs
    inPipe = new PipedInputStream(pipeSize)
    outPipe = new PipedOutputStream()
    printWriter = new PrintWriter(outPipe)
    inPipe.connect(outPipe)
  }

  def stop() = {
    // free resources assocated with java.io objs
    inPipe.close()
    outPipe.close()
    printWriter.close()
  }

  def calculate(phase: String, depth: Double, distance: Double): String =
    calculate(List(phase), depth, distance)

  def calculate(phases: List[String], depth: Double, distance: Double): String = {

    // set phase names, source depth
    tauP.clearPhaseNames()
    phases.foreach(tauP.appendPhaseName(_))
    tauP.setSourceDepth(depth)

    // run {time, pierce, path} calculation
    tauP.calculate(distance)

    // fetch the results, via the (piped) PrintWriter obj; return
    tauP.printResult(printWriter)
    printWriter.flush()
    outPipe.flush()
    Util.readFromStream(inPipe)

  }
}

class Time(model: String) extends TauPWrapper {
  val tauP = new TauP_Time(model)
}

class Pierce(model: String) extends TauPWrapper {
  val tauP = new TauP_Pierce(model)
}

class Path(model: String) extends TauPWrapper {
  val tauP = new TauP_Path(model)
}
