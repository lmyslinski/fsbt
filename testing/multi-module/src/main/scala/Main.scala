/**
  * Copyright (c) 2017 ADVA Optical Networking Sp. z o.o.
  * All rights reserved. Any unauthorized disclosure or publication of the confidential and
  * proprietary information to any other party will constitute an infringement of copyright laws.
  *
  * Author: Łukasz Myśliński <LMyslinski@advaoptical.com>
  *
  * Created: 06/10/2017
  */
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
