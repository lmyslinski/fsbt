package core

import better.files.File

import scala.util.matching.Regex

object FsbtUtil {

  def recursiveListFiles(path: String, r: Regex): List[File] = {
    val these = File(path).listRecursively
    these.filter(f => r.findFirstIn(f.name).isDefined).toList
  }
}
