package com.novus.salat.examples

import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoConnection

import org.json4s._
import org.bson.types._

import scala.collection.JavaConversions

/* https://github.com/novus/salat/wiki/CustomContext */
import com.novus.salat.examples.globals._


/**
 * Case class with a JValue field.
 * https://groups.google.com/forum/#!topic/scala-salat/BM3GpdjlFRE
 */
case class Something(_id: ObjectId = new ObjectId, name: String, jval: JString)

object Something {
  def apply(_id: ObjectId, name: String, jval: List[String]) = {
    if(jval.size != 1) sys.error(s"Unsupported JValue: $jval")
    new Something(_id, name, JString(jval(0)))
  }
}

@Salat
object SomethingDAO extends SalatDAO[Something, ObjectId](collection = MongoConnection()("test")("somethings"))

case class Location(x: Double, y: Double)
case class Venue(@Key("_id") id: Int,
                 // location: Tuple2[Double, Double], // Salat doesn't support Tuples
                 location: Location,
                 name: String)

object VenueDAO extends SalatDAO[Venue, Int](collection = MongoConnection()("ec")("venue"))

/** Exploring limitations */
case class EitherHolder(either: Either[String, Int])

/** Map of lists...not supported in 1.9.x */
case class NestedCollHolder(lists: Map[String, List[String]] = Map.empty)

/** Not supported. You should just make that maybeList a plain old List[String] with default value Nil. */
case class OptionalColl(_id: ObjectId = new ObjectId, maybeList: Option[List[String]])

case class DoublesHolder(values: List[Double])

class IntHolder(val i: Int) extends AnyVal
case class IntHolderHolder(i: IntHolder)

case class Weird(profile: String, indexingMappings: List[String])

object SalatExamples {

  import org.slf4j._
  val log = LoggerFactory.getLogger(this.getClass)

  import scala.util.control.NonFatal

  def tryAndLogErrors(fn: () => Unit) = try {
    fn()
  } catch {
    case NonFatal(ex) => log.error("An error occurred:", ex)
  }

  def main(args: Array[String]) {
    println("Running all examples...\n\n\n")
    tryAndLogErrors(casbahInsert)
    tryAndLogErrors(daoExample)
    tryAndLogErrors(floatNumberQueryExample)
    tryAndLogErrors(nestedCollections)
    tryAndLogErrors(optionalCollection)
    tryAndLogErrors(listOfDoubles)
    tryAndLogErrors(listOfNulls)
    tryAndLogErrors(anyValHolder)
    tryAndLogErrors(weird)
    println("\n\n...Done")
  }

  def casbahInsert() {
    import com.mongodb.casbah.Imports._
    val mongoClient = MongoClient("localhost", 27017)
    val coll = mongoClient("test")("somethings")
    val value = JString("foo")
    val doc = MongoDBObject("jstring" -> value)
    coll.insert(doc)
    // This works because Casbah converts the case class as follows (see ScalaProductSerializer)
    import scala.collection.JavaConversions._
    val list: java.util.List[Any] = value.productIterator.toList
    println(s"Just inserted the following document: $list")

  }

  def daoExample() {

    println("==== SalatDAO EXAMPLE ====")

    val sin = Something(name = "abc", jval = JString("123"))
    println(s"New Something instance: $sin")

    // Unsupported JSON transformation for class='org.json4s.JsonAST $JString', value='JString(123)'
    //  println(grater[Something].toPrettyJSON(sin))

    println(s"Saving Something instance to $SomethingDAO...")
    val id = SomethingDAO.insert(sin)
    println(s"...saved with _id = $id")

    println("Finding Something instance by ID...")
    val sout = id.flatMap(SomethingDAO.findOneById)
    println(s"Got Something from database: $sout")

    // Same error as above
    val json = sout.map(grater[Something].toPrettyJSON)
    println(json.getOrElse("<Not Found>"))
  }

  def floatNumberQueryExample() {
    val venue = Venue(1, Location(1.0, 1.0), "NYC")
    VenueDAO.save(venue)
    println(s"Saved: $venue")
    val found = VenueDAO.findOne(MongoDBObject("location.x" -> 1.0, "location.y" -> 1.0))
    println(s"Found: $found")
  }

  def nestedCollections() {
    val lists = NestedCollHolder(lists = Map(
      "foo" -> List("a","b","c"),
      "bar" -> List("d","e","f")))

    val json = grater[NestedCollHolder].toPrettyJSON(lists)
    println(json)

    val fromJson = grater[NestedCollHolder].fromJSON(json)
    println(fromJson)
  }

  def optionalCollection() {
    // this actually works...
    val obj = OptionalColl(maybeList = None)

    val json = grater[OptionalColl].toPrettyJSON(obj)
    println(json)

    val fromJson = grater[OptionalColl].fromJSON(json)
    println(fromJson)

    // This...not so much
    // https://github.com/salat/salat/wiki/SupportedTypes
    val obj2 = OptionalColl(maybeList = Some(List("a","b","c")))
    println(obj2)

    val json2 = grater[OptionalColl].toPrettyJSON(obj2)
    println(json2)

    val fromJson2 = grater[OptionalColl].fromJSON(json2)
    println(fromJson2)
  }

  def listOfDoubles() {
    val jsonWithDoubles = """{"values":[1,2,3]}"""
    val fromJson = grater[DoublesHolder].fromJSON(jsonWithDoubles)
    println(fromJson)
  }

  def listOfNulls() {
    val jsonWithNulls = """{"values":[null, null, null]}"""
    val fromJson = grater[DoublesHolder].fromJSON(jsonWithNulls)
    println(fromJson)
  }

  def anyValHolder() {
    val intHolder = new IntHolder(1)
    val model = IntHolderHolder(intHolder)
    val json = grater[IntHolderHolder].toCompactJSON(model)
    println(json)
  }

  /** Attempt to reproduce issue #19 */
  def weird() {
    println("**** Issue #19 reproduction attempt")
    val w = Weird("profile1", List("a","b","c"))
    val json = grater[Weird].toCompactJSON(w)
    println(w)
  }
}

