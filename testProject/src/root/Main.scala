package root

import root.nested1.NestedClass1

/**
  * Created by humblehound on 09.11.16.
  */
object Main extends App{

  val nestedClass1 = new NestedClass1

    println("Main class loaded")
    println(nestedClass1.nestedClass1variable)
    println("We're good")
}
