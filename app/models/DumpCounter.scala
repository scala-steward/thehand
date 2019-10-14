package models

import reportio.Writable

class DumpCounter(val path: String, val counter: Int) extends Writable with Ordered[DumpCounter] {
  override def toWritableSeq: Seq[Any] = {
    List(counter, path)
  }

  def compare(that: DumpCounter): Int = {
    if (counter == that.counter) 0
    else if (counter > that.counter) 1
    else -1
  }
}

object DumpCounter {
  def apply(path: String, counter: Int): DumpCounter = new DumpCounter(path, counter)
}


