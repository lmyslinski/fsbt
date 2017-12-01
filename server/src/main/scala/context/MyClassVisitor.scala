package context

import scala.tools.asm.{ClassVisitor, MethodVisitor, Opcodes}

class MyClassVisitor(contextResolver: ContextResolver) extends ClassVisitor(Opcodes.ASM4){

  override def visit(version: Int, access: Int, name: String, signature: String, superName: String, interfaces: Array[String]): Unit = {
    contextResolver.className=Some(name)
    contextResolver.superClass=Some(superName)
    super.visit(version,access, name, signature, superName, interfaces)
  }

  override def visitMethod(access: Int, name: String, desc: String, signature: String, exceptions: Array[String]): MethodVisitor ={
    if(name == ContextResolver.mainMethodName && desc == ContextResolver.mainMethodDesc){
      contextResolver.mainMethodFound = true
    }

    super.visitMethod(access, name, desc, signature, exceptions)
  }

  override def visitSource(source: String, debug: String): Unit = {
    contextResolver.fileName = Some(source)
    super.visitSource(source, debug)
  }
}