package core

import java.io.File

import scala.sys.process._
import scala.util.matching.Regex

object Fsbt {

  var config: Config = new Config()
  val scalaRegex = new Regex(".scala$")

  def recursiveListFiles(f: File, r: Regex): Array[File] = {
    val these = f.listFiles
    val good = these.filter(f => r.findFirstIn(f.getName).isDefined)
    good ++ these.filter(_.isDirectory).flatMap(recursiveListFiles(_,r))
  }

  def compile(args: Array[String]): Unit ={
    val filePaths = recursiveListFiles(new File(args(1)), scalaRegex).map( x=> x.getAbsolutePath)
    val command = windowsCompatible(List("scalac") ++ filePaths ++ List("-d", "target"))
    val output = command.!!
    println(output)
  }

  def run(args: Array[String]): Unit = {
    val command = windowsCompatible(List("scala",  "-cp", "target", args(1)))
    val output = command.!!
    println(output)
  }

  def main(args: Array[String]): Unit = {
    if (args.length == 0){
      println("Printing info")
    }else args(0) match {
      case "compile" => compile(args)
      case "run" => run(args)
      case unknown => println ("command not found: " + unknown)
    }
  }

  def windowsCompatible(command: List[String]) = {
    sys.props("os.name").toLowerCase match {
      case x if x contains "windows" => Seq("cmd", "/C") ++ command
      case _ => command
    }
  }

}
