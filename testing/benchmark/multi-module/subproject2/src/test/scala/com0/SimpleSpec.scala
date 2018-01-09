package com0

import org.scalatest._

class SimpleSpec extends FlatSpec {

  "A simple test" should "work" in {
    assert("1" === Integer.toString(1))
  }

  "A long test" should "work" in {
    for(i <- 1 to 10){
      assert(4093 + i === Simple.ackermann(3, 9) + i)
    }
  }
}