package core;

import scala.tools.asm.ClassReader;
import scala.tools.asm.ClassVisitor;
import scala.tools.asm.MethodVisitor;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by humblehound on 12.06.17.
 */
public class MyClassVisitor extends ClassVisitor {

  public MyClassVisitor(int api) {
    super(api);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    System.out.println(name);
    return super.visitMethod(access, name, desc, signature, exceptions);
  }
}
