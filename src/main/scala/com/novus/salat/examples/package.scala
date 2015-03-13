package com.novus.salat.examples

import com.novus.salat._

package object globals {

  // https://github.com/novus/salat/wiki/TypeHints
  implicit val ctx = new Context {
    val name = "When-Necessary-Context"
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.Always)
  }
}
