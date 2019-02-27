
package models

object Suffix {
  def apply(sx: String): Suffix = new Suffix(sx)
}

class Suffix(sx: String) {
  val suffix: String = sx
}