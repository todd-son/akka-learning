package actor.test

import org.scalatest.FreeSpec

class TestSpec extends FreeSpec {
  "easy test" in {
    val a = 3
    val b = 3

    assert(a == b)
  }
}

class StringTest extends FreeSpec {
  "split" in {
    val values = "1".split(",")
    assert(values.contains("1"))
  }
}

