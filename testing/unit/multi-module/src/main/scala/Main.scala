package root

import root.nested1.NestedClass1
import root.NestedClass2

object Main extends App {

  val nestedClass1 = new NestedClass1
  val nestedClass2 = new NestedClass2

  println("Main class maybe loaded")
  println(nestedClass1.nestedClass1variable)
  println(nestedClass2.nestedClass2variable)

  println("We're all good")
}
