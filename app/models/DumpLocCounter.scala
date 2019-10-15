package models

import reportio.Writable

class DumpLocCounter(val path: String, val loc: Long, val counter: Int) extends Writable with Ordered[DumpLocCounter] {
  override def toWritableSeq: Seq[Any] =
    List(counter, loc, path)

  def compare(that: DumpLocCounter): Int =
    if (counter == that.counter) 0
    else if (counter > that.counter) 1
    else -1
}

object DumpLocCounter {
  def apply(path: String, loc: Long, counter: Int): DumpLocCounter = new DumpLocCounter(path, loc, counter)
}

