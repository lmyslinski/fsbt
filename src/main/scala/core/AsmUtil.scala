package core

import better.files.File

import scala.tools.asm.{ClassReader, Opcodes}

/**
  * Created by humblehound on 11.06.17.
  */
object AsmUtil {

  def findMainMethods(targetClasses: Array[File]): scala.List[String] = {

    val cl = new MyClassVisitor(Opcodes.ASM4)

//    val in=ASMHelloWorld.class.getResourceAsStream("/java/lang/String.class");

    val is = targetClasses(1).newInputStream
    println(is.available())

    try{
      val cr = new ClassReader(is)
      cr.accept(cl, 0)
    }catch{
      case e: Exception => {
        val tt = e.getSuppressed()
        println(tt.length)
      }
    }


    var classLoader = new java.net.URLClassLoader(targetClasses.map(p => p.uri.toURL), this.getClass.getClassLoader)

    List()

//    classLoader.loadClass(Module.ModuleClassName + "$")




    /*
     * please note that the suffix "$" is for Scala "object",
     * it's a trick
     */
//    var clazzExModule = classLoader.loadClass(Module.ModuleClassName + "$")

    /*
     * currently, I don't know how to check if clazzExModule is instance of
     * Class[Module], because clazzExModule.isInstanceOf[Class[_]] always
     * returns true,
     * so I use try/catch
     */
//    try {
//      //"MODULE$" is a trick, and I'm not sure about "get(null)"
//      var module = clazzExModule.getField("MODULE$").get(null).asInstanceOf[Module]
//    } catch {
//      case e: java.lang.ClassCastException =>
//        printf(" - %s is not Module\n", clazzExModule)
//    }
  }

}