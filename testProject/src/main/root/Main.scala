package main.root

import main.root.nested1.NestedClass1
import xsbti.compile.ZincCompilerUtil

/**
  * Created by humblehound on 09.11.16.
  */
object Main extends App{

  val nestedClass1 = new NestedClass1
  val compiler = ZincCompilerUtil.defaultIncrementalCompiler()

    println("Main class loaded")
    println(nestedClass1.nestedClass1variable)
    println("We're good")
}
