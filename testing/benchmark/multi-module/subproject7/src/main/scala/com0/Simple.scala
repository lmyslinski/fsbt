package com0


object Simple {
  def ackermann(m: Long, n: Long): Long = {
    if (m == 0) return n + 1
    if (n == 0) return ackermann(m - 1, 1)
    ackermann(m - 1, ackermann(m, n - 1))
  }
}
