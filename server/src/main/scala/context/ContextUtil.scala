package context

import better.files.File

import scala.tools.asm.ClassReader

object ContextUtil {

  def identifyContext(targetClasses: List[File]): List[ContextResolver] = {

    targetClasses.map(file => {
      val ctx = new ContextResolver
      val cl = new MyClassVisitor(ctx)
      val cr = new ClassReader(file.byteArray)
      cr.accept(cl, 0)
      ctx
    }).filter(_.mainMethodFound)
  }
}
