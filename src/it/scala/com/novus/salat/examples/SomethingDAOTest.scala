package com.novus.salat.examples

import org.json4s.JsonAST.JString
import org.scalatest._

class SomethingDAOTest extends FlatSpec with Matchers {

  def createAndInsertSomething(name: String, jstringVal: String) = {
    Something(name = name, jval = JString(jstringVal))
  }

  "SomethingDAO" should "store Something instances" in {
    val sin = createAndInsertSomething("abc","123")
    val id = SomethingDAO.insert(sin)
    id should be('defined)
  }
  it should "find previously stored instances" in {
    val sin = createAndInsertSomething("def","456")
    val id = SomethingDAO.insert(sin)
    val out = id.flatMap(SomethingDAO.findOneById)
    out should be ('defined)
    out.get.name should be("def")
    out.get.jval should be(JString("456"))
  }

}
