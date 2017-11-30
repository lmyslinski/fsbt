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

/**
  * Created by humblehound on 09.11.16.
  */
    object Main extends App{

      val nestedClass1 = new NestedClass1

        println("Main class loaded")
        println(nestedClass1.nestedClass1variable)
        println("We're all good")
    }
