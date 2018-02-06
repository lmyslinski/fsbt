package root

import root.nested1.NestedClass1
import root.NestedClass2

object Main extends App {

  val nestedClass1 = new NestedClass1
  val nestedClass2 = new NestedClass2
  val nestedClass3 = new NestedClass3
  val nestedClass4 = new NestedClass4
  val nestedClass5 = new NestedClass5



  println("Main class maybe loaded")
  println(nestedClass1.nestedClass1variable)
  println(nestedClass2.nestedClass2variable)
  println(nestedClass3.nestedClass3variable)
  println(nestedClass4.nestedClass4variable)
  println(nestedClass5.nestedClass5variable)

  println("We're all good")
}
